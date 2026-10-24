package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UserProfile
import com.example.data.repository.BillingRepository
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.theme.LingoAmber
import com.example.ui.theme.LingoCoral
import com.example.ui.theme.LingoCyan
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald
import com.example.ui.theme.LingoGold
import com.example.ui.theme.LingoViolet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.ui.components.findActivity

@Composable
fun ProfileScreen(
  userProfile: UserProfile,
  billingRepository: BillingRepository?,
  onSignOut: () -> Unit,
  onNativeLanguageChanged: (String) -> Unit,
  onDeleteAccount: () -> Unit
) {
  val context = LocalContext.current
  var showNativePicker by remember { mutableStateOf(false) }
  var showUpgradeModal by remember { mutableStateOf(false) }
  var showDeleteConfirmation by remember { mutableStateOf(false) }
  var showPrivacyPolicy by remember { mutableStateOf(false) }
  var showTermsOfService by remember { mutableStateOf(false) }

  val nativeLang = Language.findByCode(userProfile.nativeLanguageCode)
  val targetLang = Language.findByCode(userProfile.targetLanguageCode)

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)
    ) {
      // User Identity Card
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(20.dp)
        ) {
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(listOf(LingoElectricBlue, LingoViolet))
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = userProfile.displayName.take(1).uppercase().ifBlank { "L" },
              style = MaterialTheme.typography.headlineLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = userProfile.displayName.ifBlank { "Lingo Learner" },
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Level ${userProfile.level} • ${userProfile.proficiencyLevel}",
              style = MaterialTheme.typography.bodySmall,
              color = LingoElectricBlue,
              fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (userProfile.isPremium) LingoGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (userProfile.isPremium) "PREMIUM MEMBER" else "FREE TIER",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (userProfile.isPremium) LingoAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Subscription Status Banner
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (userProfile.isPremium) LingoEmerald.copy(alpha = 0.15f) else LingoElectricBlue.copy(alpha = 0.12f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showUpgradeModal = true }
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(18.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(if (userProfile.isPremium) LingoEmerald else LingoElectricBlue),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (userProfile.isPremium) Icons.Default.Check else Icons.Default.Star,
              contentDescription = "Subscription",
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (userProfile.isPremium) "LingoSphere Premium Active" else "Upgrade to Premium",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (userProfile.isPremium) "Enjoying higher speaking limits and all 8 roleplays." else "Higher daily AI voice practice & all 8 roleplays ($9.99/mo).",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Icon(Icons.Default.ArrowForward, contentDescription = null, tint = LingoElectricBlue)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Statistics Grid
      Text(
        text = "Learning Statistics",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        StatItem(
          icon = "🔥",
          label = "Streak",
          value = "${userProfile.currentStreak} Days",
          modifier = Modifier.weight(1f)
        )
        StatItem(
          icon = "⭐",
          label = "Total XP",
          value = "${userProfile.xpPoints}",
          modifier = Modifier.weight(1f)
        )
        StatItem(
          icon = "📚",
          label = "Lessons",
          value = "${userProfile.completedLessonIds.size}",
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Achievements
      Text(
        text = "Badges & Achievements",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(12.dp))

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AchievementCard(
          icon = "🎯",
          title = "First Step",
          description = "Complete your first language lesson module.",
          unlocked = userProfile.achievements.contains("first_lesson")
        )
        AchievementCard(
          icon = "🎙️",
          title = "Voice Pioneer",
          description = "Practice speaking with the AI Voice Tutor.",
          unlocked = userProfile.achievements.contains("first_speak")
        )
        AchievementCard(
          icon = "🔥",
          title = "3-Day Dynamo",
          description = "Maintain a learning streak for 3 consecutive days.",
          unlocked = userProfile.achievements.contains("streak_3")
        )
        AchievementCard(
          icon = "🏅",
          title = "Diligent Scholar",
          description = "Complete 5 curriculum modules in your target language.",
          unlocked = userProfile.achievements.contains("scholar_5")
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Language & Preferences Settings
      Text(
        text = "Preferences",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(12.dp))

      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showNativePicker = true }
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(16.dp)
        ) {
          Icon(Icons.Default.Language, contentDescription = "Native Language", tint = LingoElectricBlue)
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Native Language for Explanations",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "${nativeLang.flagEmoji} ${nativeLang.name} (${nativeLang.nativeName})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Legal & Privacy Policy
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Data Safety & Policies",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showPrivacyPolicy = true }
              .padding(vertical = 8.dp)
          ) {
            Icon(Icons.Default.Policy, contentDescription = null, tint = LingoElectricBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Privacy Policy", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showTermsOfService = true }
              .padding(vertical = 8.dp)
          ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = LingoElectricBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Terms of Service", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Web Data Deletion Request URL:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surface)
              .padding(8.dp)
          ) {
            Text(
              text = DATA_DELETION_URL,
              style = MaterialTheme.typography.labelSmall,
              color = LingoCyan,
              modifier = Modifier.weight(1f)
            )
            IconButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Delete Account URL", DATA_DELETION_URL)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Sign Out Button
      Button(
        onClick = onSignOut,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      ) {
        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out of LingoSphere", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Delete Account and Data Button
      Button(
        onClick = { showDeleteConfirmation = true },
        colors = ButtonDefaults.buttonColors(
          containerColor = LingoCoral.copy(alpha = 0.15f),
          contentColor = LingoCoral
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      ) {
        Icon(Icons.Default.DeleteForever, contentDescription = "Delete Account", modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Delete Account & All Data", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }

  if (showNativePicker) {
    LanguagePickerDialog(
      title = "Change Native Language",
      currentLanguageCode = userProfile.nativeLanguageCode,
      onLanguageSelected = { selected ->
        onNativeLanguageChanged(selected.code)
      },
      onDismiss = { showNativePicker = false }
    )
  }

  if (showDeleteConfirmation) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirmation = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.DeleteForever, contentDescription = null, tint = LingoCoral)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Delete Account Permanently?", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(
            text = "Are you sure you want to delete your LingoSphere account? This will permanently delete:\n\n" +
              "• Your user profile and language settings\n" +
              "• All completed lessons and earned XP\n" +
              "• All spaced repetition flashcards\n" +
              "• Speaking session history & voice evaluations\n\n" +
              "This action cannot be undone. You can also request deletion at any time online at:\n$DATA_DELETION_URL",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirmation = false
            onDeleteAccount()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = LingoCoral,
            contentColor = Color.White
          )
        ) {
          Text("Delete Permanently")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirmation = false }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showPrivacyPolicy) {
    PrivacyPolicyDialog(
      onDismiss = { showPrivacyPolicy = false },
      onOpenBrowser = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
        context.startActivity(intent)
      }
    )
  }

  if (showTermsOfService) {
    TermsOfServiceDialog(
      onDismiss = { showTermsOfService = false },
      onOpenBrowser = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_OF_SERVICE_URL))
        context.startActivity(intent)
      }
    )
  }

  // Subscription Upgrade Dialog with Google Play Billing
  if (showUpgradeModal) {
    SubscriptionUpgradeDialog(
      onDismiss = { showUpgradeModal = false },
      onPurchase = { productId ->
        showUpgradeModal = false
        val activity = context.findActivity()
        if (activity != null && billingRepository != null) {
          billingRepository.launchBillingFlow(activity, productId)
        }
      }
    )
  }
}

@Composable
private fun StatItem(
  icon: String,
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = modifier
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Text(text = icon, fontSize = 24.sp)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun AchievementCard(
  icon: String,
  title: String,
  description: String,
  unlocked: Boolean
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (unlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (unlocked) LingoGold.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Text(text = if (unlocked) icon else "🔒", fontSize = 22.sp)
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      if (unlocked) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(LingoEmerald.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(text = "UNLOCKED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LingoEmerald)
        }
      }
    }
  }
}

@Composable
private fun SubscriptionUpgradeDialog(
  onDismiss: () -> Unit,
  onPurchase: (String) -> Unit
) {
  androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(20.dp)
      ) {
        Text(text = "👑", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "LingoSphere Premium",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Unlock higher AI speaking practice, all 8 roleplay scenarios, and full accent feedback.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Monthly Option
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onPurchase(SubscriptionPlan.PRODUCT_ID_MONTHLY) }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = "Monthly Plan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Text(text = "$9.99 / month • Cancel anytime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
              onClick = { onPurchase(SubscriptionPlan.PRODUCT_ID_MONTHLY) },
              colors = ButtonDefaults.buttonColors(containerColor = LingoElectricBlue)
            ) {
              Text("Choose")
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Annual Option (Best Value)
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = LingoElectricBlue.copy(alpha = 0.12f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onPurchase(SubscriptionPlan.PRODUCT_ID_YEARLY) }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Annual Plan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LingoAmber)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                  Text(text = "SAVE 50%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
              }
              Text(text = "$59.99 / year ($4.99/mo)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
              onClick = { onPurchase(SubscriptionPlan.PRODUCT_ID_YEARLY) },
              colors = ButtonDefaults.buttonColors(containerColor = LingoElectricBlue)
            ) {
              Text("Choose")
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Subscription purchases are securely processed through Google Play Billing. Entitlements are verified before unlocking features.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.textButtonColors(),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Maybe Later")
        }
      }
    }
  }
}
