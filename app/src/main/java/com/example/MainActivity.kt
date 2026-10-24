package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Language
import com.example.data.model.UserProfile
import com.example.data.repository.BillingRepository
import com.example.data.repository.CurriculumData
import com.example.data.repository.LingoRepository
import com.example.speech.SpeechManager
import com.example.ui.LingoViewModel
import com.example.ui.components.LingoBottomBar
import com.example.ui.components.LingoScreen
import com.example.ui.components.appSignOut
import com.example.ui.components.attemptAutoSignIn
import com.example.ui.screens.AiVoiceCallScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.LessonExerciseScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SpeakScreen
import com.example.ui.theme.LingoSphereTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private lateinit var speechManager: SpeechManager
  private var billingRepository: BillingRepository? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    speechManager = try {
      SpeechManager(this)
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "SpeechManager initialization error", e)
      SpeechManager(applicationContext)
    }

    setContent {
      LingoSphereTheme {
        LingoAppRoot(
          speechManager = speechManager,
          onInitializeBilling = { onEntitlementVerified ->
            if (billingRepository == null) {
              try {
                billingRepository = BillingRepository(
                  context = this@MainActivity,
                  externalScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main),
                  onEntitlementVerified = onEntitlementVerified
                )
              } catch (e: Throwable) {
                android.util.Log.w("MainActivity", "BillingRepository init failed", e)
              }
            }
            billingRepository
          }
        )
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    speechManager.release()
  }
}

@Composable
fun LingoAppRoot(
  speechManager: SpeechManager,
  onInitializeBilling: (onEntitlementVerified: (String, String) -> Unit) -> BillingRepository?
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  // Observe Firebase Auth State changes
  DisposableEffect(Unit) {
    val authListener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(authListener)
    onDispose {
      Firebase.auth.removeAuthStateListener(authListener)
    }
  }

  // Silent auto sign-in attempt
  LaunchedEffect(Unit) {
    if (currentUser == null) {
      attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = { currentUser = Firebase.auth.currentUser },
        onUnauthenticated = { /* Wait for user tap */ },
        scope = coroutineScope
      )
    }
  }

  val user = currentUser
  if (user == null) {
    // Unauthenticated: Show onboarding and Google Sign-In
    AuthScreen(
      onSignInSuccess = { uid, displayName, email, nativeLang, targetLang ->
        val authUser = Firebase.auth.currentUser
        currentUser = authUser
        if (authUser != null) {
          // Save initial profile if new
          coroutineScope.launch {
            try {
              val repository = LingoRepository(context)
              val existing = repository.getUserProfile(authUser.uid)
              if (existing == null) {
                repository.createUserProfile(
                  UserProfile(
                    userId = authUser.uid,
                    displayName = displayName.ifBlank { authUser.displayName ?: "Language Learner" },
                    nativeLanguageCode = nativeLang,
                    targetLanguageCode = targetLang,
                    activeLanguages = listOf(targetLang)
                  )
                )
                repository.saveUserPrivateEmail(email.ifBlank { authUser.email ?: "" })
                repository.saveSpeakingSession(
                  com.example.data.model.SpeakingSession(
                    userId = authUser.uid,
                    languageCode = targetLang,
                    mode = "Tutor",
                    scenarioTitle = "Welcome Assessment",
                    proficiencyLevel = "Beginner",
                    turnsCount = 1L,
                    fluencyScore = 90L,
                    grammarFeedbackSummary = "Welcome to LingoSphere! Your account is initialized."
                  )
                )
              }
            } catch (e: Exception) {
              android.util.Log.e("MainActivity", "Error creating initial profile: ${e.message}", e)
            }
          }
        }
      },
      onSignInError = { errorMsg ->
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
      }
    )
  } else {
    // Authenticated Session: Inject custom DB ID repository and key ViewModel by current user ID
    val repository = remember(user.uid) { LingoRepository(context) }
    val viewModel: LingoViewModel = viewModel(
      key = user.uid,
      factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return LingoViewModel(repository, user.uid) as T
        }
      }
    )

    val billingRepo = remember(user.uid) {
      onInitializeBilling { tier, token ->
        viewModel.updateSubscription(tier, token)
        Toast.makeText(context, "🎉 Premium Activated! Enjoy higher speaking limits.", Toast.LENGTH_LONG).show()
      }
    }

    AuthenticatedMainContent(
      viewModel = viewModel,
      speechManager = speechManager,
      billingRepository = billingRepo,
      onSignOut = {
        appSignOut(
          context = context,
          credentialManager = credentialManager,
          onSignOutComplete = { currentUser = null },
          scope = coroutineScope
        )
      }
    )
  }
}

@Composable
fun AuthenticatedMainContent(
  viewModel: LingoViewModel,
  speechManager: SpeechManager,
  billingRepository: BillingRepository?,
  onSignOut: () -> Unit
) {
  val context = LocalContext.current
  val userProfile by viewModel.userProfileState.collectAsState()
  val activeLesson by viewModel.activeLesson.collectAsState()
  val flashcards by viewModel.flashcardProgressState.collectAsState()

  var currentScreen by remember { mutableStateOf(LingoScreen.HOME) }
  var isVoiceCallActive by remember { mutableStateOf(false) }

  // RTL layout direction for Arabic, Urdu, Persian, Pashto
  val currentTargetLang = Language.findByCode(userProfile.targetLanguageCode)
  val currentNativeLang = Language.findByCode(userProfile.nativeLanguageCode)
  val isRtlLanguage = currentTargetLang.isRtl || currentNativeLang.isRtl
  val layoutDirection = if (isRtlLanguage) LayoutDirection.Rtl else LayoutDirection.Ltr

  val curriculumLessons = remember(userProfile.targetLanguageCode, userProfile.nativeLanguageCode) {
    CurriculumData.getLessonsForLanguage(userProfile.targetLanguageCode, userProfile.nativeLanguageCode)
  }

  val nextLesson = remember(curriculumLessons, userProfile.completedLessonIds) {
    curriculumLessons.firstOrNull { !userProfile.completedLessonIds.contains(it.id) }
      ?: curriculumLessons.firstOrNull()
  }

  // Handle system back button to return to HOME when on sub-screens
  if (currentScreen != LingoScreen.HOME && activeLesson == null && !isVoiceCallActive) {
    BackHandler {
      currentScreen = LingoScreen.HOME
    }
  }

  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    if (isVoiceCallActive) {
      // Real-Time Full-Screen AI Voice Call
      AiVoiceCallScreen(
        userProfile = userProfile,
        speechManager = speechManager,
        onEndCallAndExit = { isVoiceCallActive = false },
        onSaveSession = { session -> viewModel.saveSpeakingSession(session) }
      )
    } else if (activeLesson != null) {
      // Active interactive exercise screen takes full screen
      LessonExerciseScreen(
        lesson = activeLesson!!,
        speechManager = speechManager,
        onFinishLesson = { score, xp ->
          viewModel.finishActiveLesson(score, xp)
        },
        onNavigateBack = { viewModel.dismissLesson() }
      )
    } else {
      Scaffold(
        bottomBar = {
          LingoBottomBar(
            currentScreen = currentScreen,
            onScreenSelected = { currentScreen = it }
          )
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          AnimatedContent(targetState = currentScreen, label = "screen_transition") { targetScreen ->
            when (targetScreen) {
              LingoScreen.HOME -> {
                HomeScreen(
                  userProfile = userProfile,
                  nextLesson = nextLesson,
                  onNavigateToLearn = { currentScreen = LingoScreen.LEARN },
                  onNavigateToSpeak = { currentScreen = LingoScreen.SPEAK },
                  onNavigateToPractice = { currentScreen = LingoScreen.PRACTICE },
                  onStartLesson = { lesson -> viewModel.startLesson(lesson) },
                  onTargetLanguageChanged = { newCode -> viewModel.changeTargetLanguage(newCode) },
                  onStartVoiceCall = { isVoiceCallActive = true }
                )
              }
              LingoScreen.LEARN -> {
                LearnScreen(
                  userProfile = userProfile,
                  lessons = curriculumLessons,
                  onStartLesson = { lesson -> viewModel.startLesson(lesson) }
                )
              }
              LingoScreen.SPEAK -> {
                SpeakScreen(
                  userProfile = userProfile,
                  speechManager = speechManager,
                  onSaveSession = { session -> viewModel.saveSpeakingSession(session) },
                  onStartVoiceCall = { isVoiceCallActive = true }
                )
              }
              LingoScreen.PRACTICE -> {
                PracticeScreen(
                  userProfile = userProfile,
                  flashcards = flashcards,
                  speechManager = speechManager,
                  onReviewCard = { card, isCorrect -> viewModel.reviewFlashcard(card, isCorrect) }
                )
              }
              LingoScreen.PROFILE -> {
                ProfileScreen(
                  userProfile = userProfile,
                  billingRepository = billingRepository,
                  onSignOut = onSignOut,
                  onNativeLanguageChanged = { newNative -> viewModel.changeNativeLanguage(newNative) },
                  onDeleteAccount = {
                    viewModel.deleteAccount(
                      onSuccess = {
                        Toast.makeText(context, "Account and all data deleted successfully", Toast.LENGTH_LONG).show()
                        onSignOut()
                      },
                      onError = { err ->
                        Toast.makeText(context, "Delete failed: $err", Toast.LENGTH_LONG).show()
                      }
                    )
                  }
                )
              }
            }
          }
        }
      }
    }
  }
}
