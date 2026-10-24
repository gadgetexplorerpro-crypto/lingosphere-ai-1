package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.data.model.SubscriptionPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingRepository(
  private val context: Context,
  private val externalScope: CoroutineScope,
  private val onEntitlementVerified: (tier: String, purchaseToken: String) -> Unit
) : PurchasesUpdatedListener {

  private val TAG = "BillingRepository"

  private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
  val productDetailsMap = _productDetailsMap.asStateFlow()

  private val _billingConnected = MutableStateFlow(false)
  val billingConnected = _billingConnected.asStateFlow()

  private val _billingError = MutableStateFlow<String?>(null)
  val billingError = _billingError.asStateFlow()

  private val billingClient: BillingClient = BillingClient.newBuilder(context)
    .setListener(this)
    .enablePendingPurchases(
      PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()
    )
    .build()

  init {
    startBillingConnection()
  }

  fun startBillingConnection() {
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          Log.i(TAG, "Billing client connected successfully")
          _billingConnected.value = true
          _billingError.value = null
          querySubscriptionProducts()
          queryExistingPurchases()
        } else {
          Log.w(TAG, "Billing setup finished with code: ${billingResult.responseCode} - ${billingResult.debugMessage}")
          _billingConnected.value = false
          _billingError.value = "Google Play Billing setup code: ${billingResult.responseCode}"
        }
      }

      override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Billing service disconnected")
        _billingConnected.value = false
      }
    })
  }

  fun querySubscriptionProducts() {
    if (!billingClient.isReady) return

    val productList = listOf(
      QueryProductDetailsParams.Product.newBuilder()
        .setProductId(SubscriptionPlan.PRODUCT_ID_MONTHLY)
        .setProductType(BillingClient.ProductType.SUBS)
        .build(),
      QueryProductDetailsParams.Product.newBuilder()
        .setProductId(SubscriptionPlan.PRODUCT_ID_YEARLY)
        .setProductType(BillingClient.ProductType.SUBS)
        .build()
    )

    val params = QueryProductDetailsParams.newBuilder()
      .setProductList(productList)
      .build()

    billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        val map = queryProductDetailsResult.productDetailsList.associateBy { it.productId }
        _productDetailsMap.value = map
        Log.i(TAG, "Queried ${map.size} products from Google Play")
      } else {
        Log.w(TAG, "Query product details failed: ${billingResult.debugMessage}")
      }
    }
  }

  fun launchBillingFlow(activity: Activity, productId: String) {
    val details = _productDetailsMap.value[productId]
    if (details == null) {
      _billingError.value = "Subscription product ($productId) not available from Google Play in this region or store account."
      return
    }

    val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
    if (offerToken == null) {
      _billingError.value = "No active subscription offer found for $productId"
      return
    }

    val productDetailsParamsList = listOf(
      BillingFlowParams.ProductDetailsParams.newBuilder()
        .setProductDetails(details)
        .setOfferToken(offerToken)
        .build()
    )

    val billingFlowParams = BillingFlowParams.newBuilder()
      .setProductDetailsParamsList(productDetailsParamsList)
      .build()

    billingClient.launchBillingFlow(activity, billingFlowParams)
  }

  override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      for (purchase in purchases) {
        handlePurchase(purchase)
      }
    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
      Log.i(TAG, "User canceled billing flow")
    } else {
      Log.e(TAG, "Billing purchase update error: ${billingResult.responseCode} - ${billingResult.debugMessage}")
      _billingError.value = "Purchase error: ${billingResult.debugMessage}"
    }
  }

  private fun handlePurchase(purchase: Purchase) {
    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
      if (!purchase.isAcknowledged) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
          .setPurchaseToken(purchase.purchaseToken)
          .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
          if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            verifyAndUnlockEntitlement(purchase)
          } else {
            Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
          }
        }
      } else {
        verifyAndUnlockEntitlement(purchase)
      }
    }
  }

  private fun verifyAndUnlockEntitlement(purchase: Purchase) {
    val product = purchase.products.firstOrNull() ?: return
    val tier = when (product) {
      SubscriptionPlan.PRODUCT_ID_YEARLY -> "premium_yearly"
      SubscriptionPlan.PRODUCT_ID_MONTHLY -> "premium_monthly"
      else -> "free"
    }

    if (tier != "free") {
      externalScope.launch(Dispatchers.Main) {
        onEntitlementVerified(tier, purchase.purchaseToken)
      }
    }
  }

  fun queryExistingPurchases() {
    if (!billingClient.isReady) return

    val params = QueryPurchasesParams.newBuilder()
      .setProductType(BillingClient.ProductType.SUBS)
      .build()

    billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        for (purchase in purchasesList) {
          if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            handlePurchase(purchase)
          }
        }
      }
    }
  }

  fun clearError() {
    _billingError.value = null
  }
}
