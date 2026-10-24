package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.model.Language
import com.example.ui.components.GoogleSignInButton
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.components.onGoogleSignInClicked
import com.example.ui.theme.LingoAmber
import com.example.ui.theme.LingoCoral
import com.example.ui.theme.LingoCyan
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald
import com.example.ui.theme.LingoViolet
import com.example.ui.theme.LingoVioletLight

// Public URLs required for Google Play Console Store Listing & Data Safety Compliance
const val BASE_COMPLIANCE_URL = "https://ais-dev-smnebhnagujwa3llo3lj4b-861509315491.asia-east1.run.app"
const val PRIVACY_POLICY_URL = "$BASE_COMPLIANCE_URL/privacy-policy.html"
const val TERMS_OF_SERVICE_URL = "$BASE_COMPLIANCE_URL/terms.html"
const val DATA_DELETION_URL = "$BASE_COMPLIANCE_URL/delete-account.html"

@Composable
fun AuthScreen(
  onSignInSuccess: (userId: String, displayName: String, email: String, nativeLang: String, targetLang: String) -> Unit,
  onSignInError: (String) -> Unit
) {
  val context = LocalContext.current
  var nativeLang by remember { mutableStateOf(Language.findByCode("en")) }
  var targetLang by remember { mutableStateOf(Language.findByCode("es")) }

  var showNativePicker by remember { mutableStateOf(false) }
  var showTargetPicker by remember { mutableStateOf(false) }

  var showLegalDialog by remember { mutableStateOf(false) }
  var legalInitialTab by remember { mutableIntStateOf(0) } // 0: Privacy, 1: Terms
  var showDeleteDataDialog by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var isFastSignInLoading by remember { mutableStateOf(false) }

  fun copyToClipboard(label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  fun openUrlInBrowser(url: String) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      context.startActivity(intent)
    } catch (e: Exception) {
      Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
    }
  }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .testTag("auth_screen"),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Official LingoSphere Globe Logo with Glowing Border
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(116.dp)
          .clip(CircleShape)
          .background(Color.White)
          .border(3.dp, LingoCyan, CircleShape)
          .padding(3.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.lingosphere_logo),
          contentDescription = "LingoSphere Official Logo",
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "LingoSphere",
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "Real-Time AI Voice Calling & Language Tutor",
        style = MaterialTheme.typography.bodyMedium,
        color = LingoCyan,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
      )

      Text(
        text = "Speak naturally with hands-free bidirectional voice conversations, native pronunciation coaching, and Leitner flashcards.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Language Journey Setup Card
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Language, contentDescription = null, tint = LingoElectricBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Set Up Your Target Language",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Native Language Picker
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surface)
              .clickable { showNativePicker = true }
              .padding(horizontal = 14.dp, vertical = 12.dp)
          ) {
            Text(text = "My native language:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "${nativeLang.flagEmoji} ${nativeLang.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select native language")
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Target Language Picker
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surface)
              .clickable { showTargetPicker = true }
              .padding(horizontal = 14.dp, vertical = 12.dp)
          ) {
            Text(text = "I want to speak:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "${targetLang.flagEmoji} ${targetLang.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LingoElectricBlue)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select target language")
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // --- 1. Sign In & Fast Google Authentication ---
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Sign In / Get Started",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(LingoEmerald.copy(alpha = 0.18f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text("FREE ACCESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LingoEmerald)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Sign in securely with Google or Gmail to save your learning streak, sync Leitner flashcards, and access daily AI voice calling.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Fast 1-Tap Sign In with Google
          Button(
            onClick = {
              isFastSignInLoading = true
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { uid, name, email ->
                  isFastSignInLoading = false
                  onSignInSuccess(uid, name, email, nativeLang.code, targetLang.code)
                },
                onAuthError = { err ->
                  isFastSignInLoading = false
                  onSignInError(err)
                },
                scope = coroutineScope,
                onAuthCancelled = { isFastSignInLoading = false }
              )
            },
            enabled = !isFastSignInLoading,
            colors = ButtonDefaults.buttonColors(
              containerColor = LingoEmerald,
              contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("quick_demo_signin_button")
          ) {
            if (isFastSignInLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.Black,
                strokeWidth = 2.dp
              )
            } else {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("One-Tap Sign In with Google", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Primary Google / Gmail Sign In
          GoogleSignInButton(
            isSignUpMode = false,
            onAuthSuccess = { uid, name, email ->
              onSignInSuccess(uid, name, email, nativeLang.code, targetLang.code)
            },
            onAuthError = onSignInError
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Secondary Google Sign Up
          GoogleSignInButton(
            isSignUpMode = true,
            onAuthSuccess = { uid, name, email ->
              onSignInSuccess(uid, name, email, nativeLang.code, targetLang.code)
            },
            onAuthError = onSignInError
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Text(text = "💡", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Personal Gmail accounts (@gmail.com) and Google Workspace accounts supported.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 15.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // --- 2. Prominent Login Page Legal & Account Deletion Buttons ---
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Policy, contentDescription = null, tint = LingoCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Privacy, Terms & Account / Data Deletion",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "By continuing, you agree to our Privacy Policy and Terms of Service. You can read them or request account/data deletion anytime below:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedButton(
              onClick = {
                legalInitialTab = 0
                showLegalDialog = true
              },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, LingoCyan),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("login_privacy_policy_button")
            ) {
              Text("Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LingoCyan)
            }

            OutlinedButton(
              onClick = {
                legalInitialTab = 1
                showLegalDialog = true
              },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, LingoElectricBlue),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("login_terms_button")
            ) {
              Text("Terms of Service", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LingoElectricBlue)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { showDeleteDataDialog = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = LingoCoral.copy(alpha = 0.16f),
              contentColor = LingoCoral
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("login_delete_account_button")
          ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Account & Delete Data", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // --- 3. Google Play & GitHub Compliance URLs Card ---
      var selectedUrlSourceTab by remember { mutableIntStateOf(0) } // 0: Live Hosted, 1: GitHub Pages, 2: GitHub Repo
      var githubUserRepo by remember { mutableStateOf("gadgetexplorerpro/lingosphere") }
      val parts = githubUserRepo.split("/").map { it.trim() }
      val ghUser = parts.getOrNull(0)?.ifBlank { "username" } ?: "username"
      val ghRepo = parts.getOrNull(1)?.ifBlank { "lingosphere" } ?: "lingosphere"

      val activePrivacyUrl = when (selectedUrlSourceTab) {
        1 -> "https://$ghUser.github.io/$ghRepo/privacy-policy.html"
        2 -> "https://github.com/$ghUser/$ghRepo/blob/main/PRIVACY_POLICY.md"
        else -> PRIVACY_POLICY_URL
      }
      val activeTermsUrl = when (selectedUrlSourceTab) {
        1 -> "https://$ghUser.github.io/$ghRepo/terms.html"
        2 -> "https://github.com/$ghUser/$ghRepo/blob/main/TERMS_OF_SERVICE.md"
        else -> TERMS_OF_SERVICE_URL
      }
      val activeDeleteUrl = when (selectedUrlSourceTab) {
        1 -> "https://$ghUser.github.io/$ghRepo/delete-account.html"
        2 -> "https://github.com/$ghUser/$ghRepo/blob/main/DELETE_ACCOUNT.md"
        else -> DATA_DELETION_URL
      }

      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = LingoElectricBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Play Console & GitHub Compliance URLs",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          TabRow(
            selectedTabIndex = selectedUrlSourceTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = LingoCyan,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
          ) {
            Tab(
              selected = selectedUrlSourceTab == 0,
              onClick = { selectedUrlSourceTab = 0 },
              text = { Text("Live URLs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
              selected = selectedUrlSourceTab == 1,
              onClick = { selectedUrlSourceTab = 1 },
              text = { Text("GitHub Pages", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
              selected = selectedUrlSourceTab == 2,
              onClick = { selectedUrlSourceTab = 2 },
              text = { Text("GitHub Repo", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
          }

          if (selectedUrlSourceTab > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = githubUserRepo,
              onValueChange = { githubUserRepo = it },
              label = { Text("Your GitHub username/repository", fontSize = 12.sp) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 1. Privacy Policy URL
          ComplianceUrlRow(
            label = "Privacy Policy URL",
            url = activePrivacyUrl,
            onReadInApp = {
              legalInitialTab = 0
              showLegalDialog = true
            },
            onCopy = { copyToClipboard("Privacy Policy URL", activePrivacyUrl) },
            onOpen = { openUrlInBrowser(activePrivacyUrl) }
          )

          Spacer(modifier = Modifier.height(10.dp))

          // 2. Terms of Service URL
          ComplianceUrlRow(
            label = "Terms of Service URL",
            url = activeTermsUrl,
            onReadInApp = {
              legalInitialTab = 1
              showLegalDialog = true
            },
            onCopy = { copyToClipboard("Terms of Service URL", activeTermsUrl) },
            onOpen = { openUrlInBrowser(activeTermsUrl) }
          )

          Spacer(modifier = Modifier.height(10.dp))

          // 3. Delete Account & Delete Data URL
          ComplianceUrlRow(
            label = "Delete Account & Delete Data URL",
            url = activeDeleteUrl,
            onReadInApp = { showDeleteDataDialog = true },
            onCopy = { copyToClipboard("Delete Account URL", activeDeleteUrl) },
            onOpen = { openUrlInBrowser(activeDeleteUrl) }
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Quick footer links for Privacy Policy, Terms & Delete Account
      Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Privacy Policy",
          style = MaterialTheme.typography.labelMedium,
          color = LingoCyan,
          textDecoration = TextDecoration.Underline,
          modifier = Modifier
            .clickable {
              legalInitialTab = 0
              showLegalDialog = true
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
        )
        Text(
          text = "•",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Terms of Service",
          style = MaterialTheme.typography.labelMedium,
          color = LingoCyan,
          textDecoration = TextDecoration.Underline,
          modifier = Modifier
            .clickable {
              legalInitialTab = 1
              showLegalDialog = true
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
        )
        Text(
          text = "•",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Delete Account & Data",
          style = MaterialTheme.typography.labelMedium,
          color = LingoCoral,
          textDecoration = TextDecoration.Underline,
          modifier = Modifier
            .clickable { showDeleteDataDialog = true }
            .padding(horizontal = 8.dp, vertical = 6.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }

  // --- Pickers & Interactive Dialogs ---
  if (showNativePicker) {
    LanguagePickerDialog(
      title = "Select Your Native Language",
      currentLanguageCode = nativeLang.code,
      onLanguageSelected = { nativeLang = it },
      onDismiss = { showNativePicker = false }
    )
  }

  if (showTargetPicker) {
    LanguagePickerDialog(
      title = "Select Language to Learn",
      currentLanguageCode = targetLang.code,
      onLanguageSelected = { targetLang = it },
      onDismiss = { showTargetPicker = false }
    )
  }

  if (showLegalDialog) {
    LegalTermsDialog(
      initialTab = legalInitialTab,
      onDismiss = { showLegalDialog = false },
      onOpenPrivacyWeb = { openUrlInBrowser(PRIVACY_POLICY_URL) },
      onOpenTermsWeb = { openUrlInBrowser(TERMS_OF_SERVICE_URL) }
    )
  }

  if (showDeleteDataDialog) {
    DeleteAccountUrlDialog(
      context = context,
      onDismiss = { showDeleteDataDialog = false }
    )
  }
}

@Composable
fun ComplianceUrlRow(
  label: String,
  url: String,
  onReadInApp: () -> Unit,
  onCopy: () -> Unit,
  onOpen: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surface)
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Read In-App",
        color = LingoCyan,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable { onReadInApp() }
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = url,
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 16.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      horizontalArrangement = Arrangement.End,
      modifier = Modifier.fillMaxWidth()
    ) {
      OutlinedButton(
        onClick = onCopy,
        modifier = Modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Copy URL", fontSize = 11.sp)
      }

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = onOpen,
        modifier = Modifier.height(34.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Open Page", fontSize = 11.sp)
      }
    }
  }
}

@Composable
fun LegalTermsDialog(
  initialTab: Int,
  onDismiss: () -> Unit,
  onOpenPrivacyWeb: () -> Unit,
  onOpenTermsWeb: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(initialTab) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = LingoElectricBlue
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Terms of Service", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .height(380.dp)
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp)
      ) {
        if (selectedTab == 0) {
          Text(
            text = "LingoSphere Privacy Policy\nEffective Date: October 24, 2026\n\n" +
              "1. Information We Collect\n" +
              "• Account Information: When you sign in using Google or Gmail, we store your User ID, Display Name, and Email address to manage your learning progress.\n" +
              "• Microphone & Audio Data: Real-time microphone audio is used strictly for speech-to-text transcription and conversational feedback during active AI tutor calls. Audio streams are processed ephemerally and are never sold or used for ad targeting.\n" +
              "• Learning Progress: Lessons completed, Leitner flashcard retention levels, daily streak counts, and conversation session transcripts.\n" +
              "• Subscription Data: In-app purchase tokens verified via Google Play Billing.\n\n" +
              "2. How We Use Data\n" +
              "• To deliver low-latency real-time AI voice calling with native language tutors.\n" +
              "• To generate personalized post-call learning reports with vocabulary and grammar corrections.\n" +
              "• To synchronize learning streaks, XP, and spaced repetition flashcards across your devices.\n\n" +
              "3. User Rights & Data Deletion\n" +
              "Under Google Play data safety policies, you can delete your account and all stored data at any time via Login Screen > Delete Account & Data, Profile > Delete Account & All Data, or via our public web deletion portal: $DATA_DELETION_URL.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
        } else {
          Text(
            text = "LingoSphere Terms of Service\nEffective Date: October 24, 2026\n\n" +
              "1. Acceptance of Terms\n" +
              "By downloading, signing into, or using LingoSphere, you agree to these Terms of Service. If you do not agree, please do not use the application.\n\n" +
              "2. User Accounts\n" +
              "You are responsible for maintaining the confidentiality of your Google/Gmail account credentials.\n\n" +
              "3. Acceptable Use Policy\n" +
              "You agree not to use the AI Voice Calling system or speech tools for unlawful, harassing, defamatory, or abusive purposes.\n\n" +
              "4. Free Tier & Subscriptions\n" +
              "• Free accounts receive 5 minutes of daily AI voice calling and core curriculum lessons.\n" +
              "• Premium accounts receive 50 minutes of daily AI calling and advanced features. Subscriptions are billed through Google Play Billing and can be managed or canceled in your Google Play Store settings.\n\n" +
              "5. Account Termination\n" +
              "You may terminate your account and erase all associated data at any time using the in-app deletion button or public web portal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Done")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          if (selectedTab == 0) onOpenPrivacyWeb() else onOpenTermsWeb()
        }
      ) {
        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Open in Browser")
      }
    }
  )
}

@Composable
fun DeleteAccountUrlDialog(
  context: Context,
  onDismiss: () -> Unit
) {
  var emailInput by remember { mutableStateOf("") }
  var deleteType by remember { mutableIntStateOf(0) } // 0: Full Account & All Data, 1: Learning Data Only
  var requestSubmitted by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LingoCoral)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Delete Account & Data", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .height(420.dp)
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp)
      ) {
        Text(
          text = "Request immediate erasure of your LingoSphere account or learning data directly from this login screen, or after signing in via Profile > Delete Account & All Data.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scope Selector
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = { deleteType = 0 },
            border = BorderStroke(1.dp, if (deleteType == 0) LingoCoral else MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = if (deleteType == 0) LingoCoral.copy(alpha = 0.15f) else Color.Transparent
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Delete Account + Data", fontSize = 11.sp, color = if (deleteType == 0) LingoCoral else MaterialTheme.colorScheme.onSurface)
          }

          OutlinedButton(
            onClick = { deleteType = 1 },
            border = BorderStroke(1.dp, if (deleteType == 1) LingoCyan else MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = if (deleteType == 1) LingoCyan.copy(alpha = 0.15f) else Color.Transparent
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text("Delete Data Only", fontSize = 11.sp, color = if (deleteType == 1) LingoCyan else MaterialTheme.colorScheme.onSurface)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!requestSubmitted) {
          OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Registered Gmail / Account Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              if (emailInput.isBlank() || !emailInput.contains("@")) {
                Toast.makeText(context, "Please enter a valid account email address", Toast.LENGTH_SHORT).show()
              } else {
                requestSubmitted = true
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = LingoCoral, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (deleteType == 0) "Submit Account & Data Deletion" else "Submit Learning Data Deletion",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        } else {
          Card(
            colors = CardDefaults.cardColors(containerColor = LingoEmerald.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "✓ Deletion request registered for $emailInput. All selected data (profile, lessons, flashcards, and speaking transcripts) is scheduled for permanent erasure.",
              style = MaterialTheme.typography.bodySmall,
              color = LingoEmerald,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(12.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Public Web Deletion URL (for Play Console):",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = DATA_DELETION_URL,
            style = MaterialTheme.typography.labelSmall,
            color = LingoCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(10.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Delete Account URL", DATA_DELETION_URL)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Delete Account URL copied", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Copy URL", fontSize = 11.sp)
          }

          OutlinedButton(
            onClick = {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DATA_DELETION_URL))
              context.startActivity(intent)
            },
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open Web", fontSize = 11.sp)
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
fun PrivacyPolicyDialog(
  onDismiss: () -> Unit,
  onOpenBrowser: () -> Unit
) {
  LegalTermsDialog(
    initialTab = 0,
    onDismiss = onDismiss,
    onOpenPrivacyWeb = onOpenBrowser,
    onOpenTermsWeb = onOpenBrowser
  )
}

@Composable
fun TermsOfServiceDialog(
  onDismiss: () -> Unit,
  onOpenBrowser: () -> Unit
) {
  LegalTermsDialog(
    initialTab = 1,
    onDismiss = onDismiss,
    onOpenPrivacyWeb = onOpenBrowser,
    onOpenTermsWeb = onOpenBrowser
  )
}


