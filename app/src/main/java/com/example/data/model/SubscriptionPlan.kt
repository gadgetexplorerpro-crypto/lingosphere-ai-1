package com.example.data.model

data class SubscriptionPlan(
  val id: String,
  val title: String,
  val billingPeriod: String,
  val priceFormatted: String,
  val savingsBadge: String? = null,
  val features: List<String>
) {
  companion object {
    const val PRODUCT_ID_MONTHLY = "lingosphere_premium_monthly"
    const val PRODUCT_ID_YEARLY = "lingosphere_premium_yearly"

    val MONTHLY = SubscriptionPlan(
      id = PRODUCT_ID_MONTHLY,
      title = "Monthly Premium",
      billingPeriod = "Billed monthly ($9.99/mo)",
      priceFormatted = "$9.99 / mo",
      savingsBadge = null,
      features = listOf(
        "Unlimited AI Speaking Tutor conversations",
        "All 8 interactive AI Roleplay scenarios",
        "Personalized accent & grammar correction",
        "Advanced vocabulary flashcard mastery",
        "Complete offline download mode",
        "Full progress analytics & certificate"
      )
    )

    val YEARLY = SubscriptionPlan(
      id = PRODUCT_ID_YEARLY,
      title = "Annual Premium (Best Value)",
      billingPeriod = "Billed annually ($59.99/yr)",
      priceFormatted = "$4.99 / mo ($59.99/yr)",
      savingsBadge = "SAVE 50%",
      features = listOf(
        "Everything in Monthly Premium",
        "50% discount compared to monthly",
        "Early access to new languages",
        "Priority AI voice response latency",
        "VIP learning streak freeze shields"
      )
    )
  }
}
