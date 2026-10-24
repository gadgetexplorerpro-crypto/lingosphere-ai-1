package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoVioletLight
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun Context.findActivity(): Activity? {
  var currentContext = this
  while (currentContext is ContextWrapper) {
    if (currentContext is Activity) return currentContext
    currentContext = currentContext.baseContext
  }
  return null
}

@Composable
fun GoogleSignInButton(
  modifier: Modifier = Modifier,
  isSignUpMode: Boolean = false,
  onAuthSuccess: (userId: String, displayName: String, email: String) -> Unit,
  onAuthError: (String) -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }

  if (isSignUpMode) {
    OutlinedButton(
      onClick = {
        isLoading = true
        onGoogleSignInClicked(
          context = context,
          credentialManager = credentialManager,
          onAuthSuccess = { uid, name, email ->
            isLoading = false
            onAuthSuccess(uid, name, email)
          },
          onAuthError = { errorMsg ->
            isLoading = false
            onAuthError(errorMsg)
          },
          scope = coroutineScope,
          onAuthCancelled = { isLoading = false }
        )
      },
      enabled = !isLoading,
      border = BorderStroke(1.5.dp, LingoElectricBlue),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = LingoElectricBlue
      ),
      modifier = modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("google_sign_up_button")
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = LingoElectricBlue,
          strokeWidth = 2.dp
        )
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Gmail / Google Sign Up",
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Sign Up with Google / Gmail",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  } else {
    Button(
      onClick = {
        isLoading = true
        onGoogleSignInClicked(
          context = context,
          credentialManager = credentialManager,
          onAuthSuccess = { uid, name, email ->
            isLoading = false
            onAuthSuccess(uid, name, email)
          },
          onAuthError = { errorMsg ->
            isLoading = false
            onAuthError(errorMsg)
          },
          scope = coroutineScope,
          onAuthCancelled = { isLoading = false }
        )
      },
      enabled = !isLoading,
      colors = ButtonDefaults.buttonColors(
        containerColor = LingoElectricBlue,
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(14.dp),
      modifier = modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("google_sign_in_button")
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = Color.White,
          strokeWidth = 2.dp
        )
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Gmail / Google Sign In",
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Sign In with Google / Gmail",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

fun onGoogleSignInClicked(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: (userId: String, displayName: String, email: String) -> Unit,
  onAuthError: (String) -> Unit,
  scope: CoroutineScope,
  onAuthCancelled: () -> Unit = {}
) {
  val activity = context.findActivity()
  if (activity == null) {
    onAuthError("Unable to locate active Window for Google Sign-In")
    return
  }

  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
    return
  }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(activity, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = Firebase.auth.signInWithCredential(authCredential).await()
        val user = authResult.user
        if (user != null) {
          onAuthSuccess(user.uid, user.displayName ?: "Lingo Learner", user.email ?: "")
        } else {
          onAuthError("Failed to obtain signed-in user session")
        }
      } else {
        onAuthError("Unexpected credential type returned")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
      onAuthCancelled()
    } catch (e: Exception) {
      Log.e("Auth", "Google Sign-In failed", e)
      onAuthError(e.localizedMessage ?: "Sign in failed")
    }
  }
}

fun attemptAutoSignIn(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: (userId: String) -> Unit,
  onUnauthenticated: () -> Unit,
  scope: CoroutineScope
) {
  val currentUser = Firebase.auth.currentUser
  if (currentUser != null) {
    onAuthSuccess(currentUser.uid)
    return
  }

  val activity = context.findActivity()
  if (activity == null) {
    onUnauthenticated()
    return
  }

  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onUnauthenticated()
    return
  }

  val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(true)
    .setServerClientId(clientId)
    .setAutoSelectEnabled(true)
    .build()

  val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(activity, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = Firebase.auth.signInWithCredential(authCredential).await()
        val user = authResult.user
        if (user != null) {
          onAuthSuccess(user.uid)
        } else {
          onUnauthenticated()
        }
      } else {
        onUnauthenticated()
      }
    } catch (e: Exception) {
      onUnauthenticated()
    }
  }
}

fun appSignOut(
  context: Context,
  credentialManager: CredentialManager,
  onSignOutComplete: () -> Unit,
  scope: CoroutineScope
) {
  Firebase.auth.signOut()
  scope.launch {
    try {
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
      Log.e("Auth", "Failed to clear credential state", e)
    } finally {
      onSignOutComplete()
    }
  }
}
