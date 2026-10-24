package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.content.ContextCompat
import com.example.ai.GeminiTutorService
import com.example.data.model.ChatMessage
import com.example.data.model.Language
import com.example.data.model.MessageSender
import com.example.data.model.RoleplayScenario
import com.example.data.model.SpeakingSession
import com.example.data.model.UserProfile
import com.example.speech.SpeechManager
import com.example.ui.components.AnimatedVoiceWaveform
import com.example.ui.theme.LingoCoral
import com.example.ui.theme.LingoCyan
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald
import com.example.ui.theme.LingoNavyCard
import com.example.ui.theme.LingoViolet
import com.example.ui.theme.LingoVioletLight
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SpeakScreen(
  userProfile: UserProfile,
  speechManager: SpeechManager,
  onSaveSession: (SpeakingSession) -> Unit,
  onStartVoiceCall: () -> Unit = {}
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val targetLang = Language.findByCode(userProfile.targetLanguageCode)
  val nativeLang = Language.findByCode(userProfile.nativeLanguageCode)

  var selectedTab by remember { mutableIntStateOf(0) } // 0: AI Tutor, 1: Roleplay Scenarios
  var activeScenario by remember { mutableStateOf<RoleplayScenario?>(null) }

  val messages = remember { mutableStateListOf<ChatMessage>() }
  var isAiGenerating by remember { mutableStateOf(false) }
  var typedInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  val isListening by speechManager.isListening.collectAsState()
  val rmsLevel by speechManager.rmsLevel.collectAsState()
  val spokenText by speechManager.spokenText.collectAsState()
  val speechError by speechManager.speechError.collectAsState()

  var hasMicPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasMicPermission = isGranted
    if (isGranted) {
      speechManager.startListening(targetLang.code)
    }
  }

  // Handle incoming speech transcript
  LaunchedEffect(spokenText) {
    if (spokenText.isNotBlank()) {
      val userText = spokenText
      speechManager.clearSpokenText()
      sendMessage(
        userText = userText,
        targetLang = targetLang,
        nativeLang = nativeLang,
        proficiencyLevel = userProfile.proficiencyLevel,
        scenario = activeScenario,
        messages = messages,
        speechManager = speechManager,
        onSessionUpdate = onSaveSession,
        setAiGenerating = { isAiGenerating = it }
      )
    }
  }

  // Initialize opening greeting
  LaunchedEffect(activeScenario, selectedTab) {
    if (messages.isEmpty()) {
      val greetingText = if (activeScenario != null) {
        activeScenario!!.openingGreetingTemplate
      } else {
        when (targetLang.code) {
          "es" -> "¡Hola! Soy tu tutor de IA LingoSphere. ¿De qué te gustaría hablar hoy?"
          "fr" -> "Bonjour ! Je suis votre tuteur d'IA LingoSphere. De quoi aimeriez-vous parler aujourd'hui ?"
          "de" -> "Hallo! Ich bin dein LingoSphere KI-Tutor. Worüber möchtest du heute sprechen?"
          "ar" -> "مرحباً! أنا معلمك الذكي من LingoSphere. عن ماذا ترغب في التحدث اليوم؟"
          "ja" -> "こんにちは！LingoSphere AIチューターです。今日は何について話しましょうか？"
          else -> "Hello! I am your LingoSphere AI tutor. What would you like to practice speaking today?"
        }
      }
      messages.add(
        ChatMessage(
          id = UUID.randomUUID().toString(),
          sender = MessageSender.AI_TUTOR,
          text = greetingText,
          translation = "Welcome! Tap the speaker icon to hear native pronunciation."
        )
      )
    }
  }

  // Scroll to bottom on new message
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header & Navigation Tabs
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(top = 16.dp, start = 16.dp, end = 16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          if (activeScenario != null) {
            IconButton(onClick = { activeScenario = null; messages.clear() }) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Exit Scenario")
            }
          }
          Text(
            text = if (activeScenario != null) activeScenario!!.title else "AI Voice Practice",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(LingoElectricBlue.copy(alpha = 0.15f))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "${targetLang.flagEmoji} ${targetLang.name}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = LingoElectricBlue
            )
          }
        }

        if (activeScenario == null) {
          Spacer(modifier = Modifier.height(12.dp))
          TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = LingoElectricBlue
          ) {
            Tab(
              selected = selectedTab == 0,
              onClick = { selectedTab = 0 },
              text = { Text("AI Tutor", fontWeight = FontWeight.Bold) }
            )
            Tab(
              selected = selectedTab == 1,
              onClick = { selectedTab = 1 },
              text = { Text("8 Roleplays", fontWeight = FontWeight.Bold) }
            )
          }
        }

        // Prominent Real-Time Call AI Tutor Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
                )
              )
              .border(1.dp, LingoEmerald.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
              .padding(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LingoEmerald.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Call, contentDescription = null, tint = LingoEmerald, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "Real-Time AI Voice Call",
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(LingoEmerald)
                    )
                  }
                  Text(
                    text = "Continuous 2-way audio call • Interrupt anytime",
                    color = LingoCyan,
                    fontSize = 11.sp
                  )
                }
              }
              Button(
                onClick = onStartVoiceCall,
                colors = ButtonDefaults.buttonColors(containerColor = LingoEmerald, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(38.dp)
              ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call Now", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
              }
            }
          }
        }
      }

      // Content View
      if (selectedTab == 1 && activeScenario == null) {
        // Roleplay Scenarios Grid
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Text(
              text = "Choose a Real-Life Conversation Scenario",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = "The AI persona adapts and responds to whatever you say in ${targetLang.name}.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
          }

          items(RoleplayScenario.SCENARIOS) { scenario ->
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  activeScenario = scenario
                  messages.clear()
                }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
              ) {
                Text(text = scenario.iconEmoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Role: ${scenario.roleName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = LingoElectricBlue
                  )
                  Text(
                    text = scenario.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      } else {
        // Active Chat & Speaking Screen
        Column(modifier = Modifier.weight(1f)) {
          // Chat message list
          LazyColumn(
            state = listState,
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(messages) { message ->
              ChatBubble(
                message = message,
                targetLangCode = targetLang.code,
                onSpeak = { speechManager.speak(it, targetLang.code) }
              )
            }

            if (isAiGenerating) {
              item {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(8.dp)
                ) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LingoElectricBlue
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "AI is thinking & analyzing pronunciation...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          // Error banner if any
          if (speechError != null) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(LingoCoral.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = speechError!!,
                  style = MaterialTheme.typography.bodySmall,
                  color = LingoCoral,
                  modifier = Modifier.weight(1f)
                )
                Text(
                  text = "Dismiss",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = LingoCoral,
                  modifier = Modifier.clickable { speechManager.clearError() }
                )
              }
            }
          }

          // Soundwave visualizer when listening
          AnimatedVisibility(visible = isListening) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp)
            ) {
              Text(
                text = "Listening... Speak in ${targetLang.name}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LingoElectricBlue
              )
              AnimatedVoiceWaveform(isListening = true, rmsLevel = rmsLevel)
            }
          }

          // Bottom Speaking / Typing Control Panel
          Surface(
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
              // Large Glowing Microphone Button
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(
                    if (isListening) LingoCoral else LingoElectricBlue
                  )
                  .clickable {
                    if (isListening) {
                      speechManager.stopListening()
                    } else {
                      if (hasMicPermission) {
                        speechManager.startListening(targetLang.code)
                      } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                      }
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                  contentDescription = if (isListening) "Stop Listening" else "Start Microphone",
                  tint = Color.White,
                  modifier = Modifier.size(28.dp)
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Text input fallback for quiet environments
              OutlinedTextField(
                value = typedInput,
                onValueChange = { typedInput = it },
                placeholder = { Text("Or type in ${targetLang.code.uppercase()}...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
              )

              Spacer(modifier = Modifier.width(8.dp))

              IconButton(
                onClick = {
                  if (typedInput.isNotBlank()) {
                    val textToSend = typedInput
                    typedInput = ""
                    sendMessage(
                      userText = textToSend,
                      targetLang = targetLang,
                      nativeLang = nativeLang,
                      proficiencyLevel = userProfile.proficiencyLevel,
                      scenario = activeScenario,
                      messages = messages,
                      speechManager = speechManager,
                      onSessionUpdate = onSaveSession,
                      setAiGenerating = { isAiGenerating = it }
                    )
                  }
                },
                enabled = typedInput.isNotBlank() && !isAiGenerating
              ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = LingoElectricBlue)
              }
            }
          }
        }
      }
    }
  }
}

private fun sendMessage(
  userText: String,
  targetLang: Language,
  nativeLang: Language,
  proficiencyLevel: String,
  scenario: RoleplayScenario?,
  messages: MutableList<ChatMessage>,
  speechManager: SpeechManager,
  onSessionUpdate: (SpeakingSession) -> Unit,
  setAiGenerating: (Boolean) -> Unit
) {
  messages.add(
    ChatMessage(
      id = UUID.randomUUID().toString(),
      sender = MessageSender.USER,
      text = userText
    )
  )

  setAiGenerating(true)
  kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
    val history = messages.map {
      (if (it.sender == MessageSender.USER) "USER" else "MODEL") to it.text
    }

    val tutorResponse = GeminiTutorService.chatWithTutor(
      userSpeechInput = userText,
      targetLanguage = targetLang,
      nativeLanguage = nativeLang,
      proficiencyLevel = proficiencyLevel,
      scenarioTitle = scenario?.title,
      scenarioInstruction = scenario?.systemPromptInstruction,
      conversationHistory = history
    )

    setAiGenerating(false)
    messages.add(
      ChatMessage(
        id = UUID.randomUUID().toString(),
        sender = MessageSender.AI_TUTOR,
        text = tutorResponse.replyInTargetLanguage,
        phoneticGuide = tutorResponse.phoneticGuide,
        translation = tutorResponse.translationInNativeLanguage,
        grammarFeedback = tutorResponse.grammarFeedback
      )
    )

    // Automatically speak AI reply
    speechManager.speak(tutorResponse.replyInTargetLanguage, targetLang.code)

    // Save session to Firestore
    onSessionUpdate(
      SpeakingSession(
        languageCode = targetLang.code,
        mode = if (scenario != null) "Roleplay" else "Tutor",
        scenarioTitle = scenario?.title ?: "Open Conversation",
        proficiencyLevel = proficiencyLevel,
        turnsCount = messages.size.toLong(),
        fluencyScore = 88L,
        grammarFeedbackSummary = tutorResponse.grammarFeedback ?: "Good conversational flow."
      )
    )
  }
}

@Composable
private fun ChatBubble(
  message: ChatMessage,
  targetLangCode: String,
  onSpeak: (String) -> Unit
) {
  val isUser = message.sender == MessageSender.USER
  var showTranslation by remember { mutableStateOf(false) }

  Row(
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    modifier = Modifier.fillMaxWidth()
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(LingoElectricBlue),
        contentAlignment = Alignment.Center
      ) {
        Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Card(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
      ),
      colors = CardDefaults.cardColors(
        containerColor = if (isUser) LingoElectricBlue else MaterialTheme.colorScheme.surfaceVariant
      ),
      modifier = Modifier.fillMaxWidth(0.85f)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          if (!isUser) {
            IconButton(
              onClick = { onSpeak(message.text) },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = LingoElectricBlue)
            }
          }
        }

        // Phonetic guide
        if (!message.phoneticGuide.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "🔊 ${message.phoneticGuide}",
            style = MaterialTheme.typography.bodySmall,
            color = LingoCyan,
            fontSize = 11.sp
          )
        }

        // Translation toggle
        if (!message.translation.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (showTranslation) "Hide translation" else "Translate to native",
            style = MaterialTheme.typography.labelSmall,
            color = if (isUser) Color.White.copy(alpha = 0.8f) else LingoElectricBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { showTranslation = !showTranslation }
          )
          if (showTranslation) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = message.translation,
              style = MaterialTheme.typography.bodySmall,
              color = if (isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Grammar & Pronunciation Feedback Card
        if (!message.grammarFeedback.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(LingoEmerald.copy(alpha = 0.15f))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "💡 Coach Feedback",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = LingoEmerald
              )
              Text(
                text = message.grammarFeedback,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }
  }
}
