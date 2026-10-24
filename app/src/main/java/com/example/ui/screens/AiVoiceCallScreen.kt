package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.CallLearningReport
import com.example.data.model.CallMode
import com.example.data.model.Language
import com.example.data.model.MessageSender
import com.example.data.model.SpeakingSession
import com.example.data.model.TutorVoice
import com.example.data.model.UserProfile
import com.example.data.model.VoiceCallStatus
import com.example.speech.AiVoiceCallManager
import com.example.speech.SpeechManager
import com.example.ui.components.AiCallAvatar
import com.example.ui.components.LiveAudioWaveformBar
import com.example.ui.theme.LingoAmber
import com.example.ui.theme.LingoCoral
import com.example.ui.theme.LingoCyan
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald
import com.example.ui.theme.LingoNavyCard
import com.example.ui.theme.LingoNavyDeep
import com.example.ui.theme.LingoViolet
import com.example.ui.theme.LingoVioletLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVoiceCallScreen(
  userProfile: UserProfile,
  speechManager: SpeechManager,
  initialMode: CallMode = CallMode.FREE_TALK,
  initialVoice: TutorVoice = TutorVoice.AOEDE,
  onEndCallAndExit: () -> Unit,
  onSaveSession: (SpeakingSession) -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val targetLang = Language.findByCode(userProfile.targetLanguageCode)
  val nativeLang = Language.findByCode(userProfile.nativeLanguageCode)

  var hasMicPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasMicPermission = isGranted
  }

  var completedReport by remember { mutableStateOf<CallLearningReport?>(null) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var showTranscriptSheet by remember { mutableStateOf(false) }
  var showTranslationByTurnId by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

  val callManager = remember(userProfile.userId, targetLang.code) {
    AiVoiceCallManager(
      context = context,
      speechManager = speechManager,
      coroutineScope = coroutineScope,
      targetLanguage = targetLang,
      nativeLanguage = nativeLang,
      proficiencyLevel = userProfile.proficiencyLevel,
      initialMode = initialMode,
      initialVoice = initialVoice,
      isPremium = userProfile.isPremium,
      onCallReportGenerated = { report ->
        completedReport = report
      }
    )
  }

  val status by callManager.callStatus.collectAsState()
  val elapsedSeconds by callManager.callSeconds.collectAsState()
  val isQuotaWarning by callManager.isQuotaWarning.collectAsState()
  val isMuted by callManager.isMuted.collectAsState()
  val isSpeakerOn by callManager.isSpeakerOn.collectAsState()
  val currentMode by callManager.currentMode.collectAsState()
  val currentVoice by callManager.currentVoice.collectAsState()
  val transcript by callManager.liveTranscript.collectAsState()
  val livePartialSpeech by callManager.currentPartialSpeech.collectAsState()
  val latestTutorReply by callManager.latestTutorResponse.collectAsState()
  val rmsLevel by speechManager.rmsLevel.collectAsState()

  // Prevent accidental exit during active call
  BackHandler(enabled = status != VoiceCallStatus.ENDED) {
    callManager.endCall()
  }

  LaunchedEffect(hasMicPermission) {
    if (hasMicPermission) {
      callManager.startCall()
    } else {
      permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      callManager.release()
    }
  }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .testTag("ai_voice_call_screen"),
    color = LingoNavyDeep
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Background Gradient Glow
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color(0xFF0F172A),
                Color(0xFF1E1B4B),
                Color(0xFF090D16)
              )
            )
          )
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // --- 1. Top Bar: Header with Mode, Status & Timer ---
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Mode Badge with Tap to Change
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                .clickable { showSettingsSheet = true }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${currentMode.emoji} ${currentMode.title}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Change Mode",
                tint = LingoCyan,
                modifier = Modifier.size(14.dp)
              )
            }

            // Call Duration Timer
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            val limitMinutes = callManager.callLimitSeconds / 60
            val timeText = String.format("%02d:%02d / %02d:00", minutes, seconds, limitMinutes)

            Card(
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isQuotaWarning) LingoAmber else Color(0x44FFFFFF))
            ) {
              Text(
                text = timeText,
                color = if (isQuotaWarning) LingoAmber else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }

          // Quota Warning Banner
          AnimatedVisibility(visible = isQuotaWarning) {
            Box(
              modifier = Modifier
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LingoAmber.copy(alpha = 0.18f))
                .border(1.dp, LingoAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = "⏳ Under 1 min remaining in daily free call. Upgrade to Premium for 50 mins/day.",
                color = LingoAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Connection & Live Status Pill
          val statusText = when (status) {
            VoiceCallStatus.CONNECTING -> "Connecting to AI Language Tutor..."
            VoiceCallStatus.TUTOR_SPEAKING -> "${currentVoice.displayName} is speaking... (Speak to interrupt)"
            VoiceCallStatus.USER_SPEAKING -> "Listening to you... (${targetLang.name})"
            VoiceCallStatus.THINKING -> "Tutor is thinking..."
            VoiceCallStatus.MUTED -> "Microphone Muted"
            VoiceCallStatus.LISTENING -> "Listening... speak naturally in ${targetLang.name}"
            VoiceCallStatus.INTERRUPTED -> "Interrupted! Listening to you..."
            VoiceCallStatus.ENDED -> "Call Finished"
            else -> "Ready"
          }

          val statusColor = when (status) {
            VoiceCallStatus.TUTOR_SPEAKING -> LingoVioletLight
            VoiceCallStatus.USER_SPEAKING -> LingoEmerald
            VoiceCallStatus.MUTED -> LingoAmber
            VoiceCallStatus.THINKING -> LingoElectricBlue
            else -> LingoCyan
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .padding(top = 6.dp)
              .clip(CircleShape)
              .background(statusColor.copy(alpha = 0.15f))
              .padding(horizontal = 14.dp, vertical = 5.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = statusText,
              color = statusColor,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // --- 2. Center Section: Animated Avatar & Visualizer ---
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Animated Call Avatar (Tapping interrupts or speaks)
          AiCallAvatar(
            status = status,
            rmsLevel = rmsLevel,
            emoji = targetLang.flagEmoji,
            modifier = Modifier
              .clickable {
                if (status == VoiceCallStatus.TUTOR_SPEAKING) {
                  callManager.interruptTutor()
                }
              }
              .testTag("ai_call_avatar")
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "${currentVoice.displayName} (${targetLang.name})",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = "${userProfile.proficiencyLevel} Level • ${currentVoice.description}",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Live Waveform Visualizer
          LiveAudioWaveformBar(
            status = status,
            rmsLevel = rmsLevel,
            modifier = Modifier.height(44.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Real-Time Live Transcript Bubble
          val latestTurn = transcript.lastOrNull()
          val displayText = if (livePartialSpeech.isNotBlank()) {
            "You: $livePartialSpeech..."
          } else if (latestTurn != null) {
            val prefix = if (latestTurn.speaker == MessageSender.USER) "You" else currentVoice.displayName.substringBefore(" ")
            "$prefix: ${latestTurn.text}"
          } else {
            "Say hello to start practicing ${targetLang.name}!"
          }

          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33475569)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showTranscriptSheet = true }
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = displayText,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal
              )

              if (latestTurn?.speaker == MessageSender.AI_TUTOR && !latestTurn.phoneticGuide.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "🗣️ ${latestTurn.phoneticGuide}",
                  color = LingoCyan.copy(alpha = 0.85f),
                  fontSize = 12.sp
                )
              }

              if (latestTurn?.speaker == MessageSender.AI_TUTOR && !latestTurn.translation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "🌐 ${latestTurn.translation}",
                  color = Color.White.copy(alpha = 0.5f),
                  fontSize = 12.sp
                )
              }

              if (transcript.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Tap to view full call transcript (${transcript.size} turns) →",
                  color = LingoElectricBlue,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // --- 3. Bottom Controls: Mute, Speaker, Settings & End Call ---
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Speakerphone Toggle
          IconButton(
            onClick = { callManager.toggleSpeaker() },
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(if (isSpeakerOn) Color(0xFF334155) else Color(0xFF1E293B))
              .border(1.dp, Color(0x44FFFFFF), CircleShape)
              .testTag("call_speaker_button")
          ) {
            Icon(
              imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
              contentDescription = "Toggle Speaker",
              tint = if (isSpeakerOn) LingoCyan else Color.White.copy(alpha = 0.6f),
              modifier = Modifier.size(24.dp)
            )
          }

          // Microphone Mute Toggle
          IconButton(
            onClick = { callManager.toggleMute() },
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(if (isMuted) LingoCoral.copy(alpha = 0.25f) else Color(0xFF334155))
              .border(1.dp, if (isMuted) LingoCoral else Color(0x44FFFFFF), CircleShape)
              .testTag("call_mute_button")
          ) {
            Icon(
              imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
              contentDescription = "Toggle Mute",
              tint = if (isMuted) LingoCoral else Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          // In-Call Mode / Voice Settings
          IconButton(
            onClick = { showSettingsSheet = true },
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(Color(0xFF334155))
              .border(1.dp, Color(0x44FFFFFF), CircleShape)
              .testTag("call_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Call Settings",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          // Big Red End Call Button
          IconButton(
            onClick = { callManager.endCall() },
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(LingoCoral)
              .testTag("call_end_button")
          ) {
            Icon(
              imageVector = Icons.Default.CallEnd,
              contentDescription = "End Call",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
        }
      }

      // --- Post-Call Learning Report Dialog ---
      if (completedReport != null) {
        PostCallReportDialog(
          report = completedReport!!,
          targetLang = targetLang,
          onSaveAndClose = {
            onSaveSession(
              SpeakingSession(
                userId = userProfile.userId,
                languageCode = targetLang.code,
                mode = completedReport!!.mode.title,
                scenarioTitle = completedReport!!.mode.title,
                proficiencyLevel = userProfile.proficiencyLevel,
                turnsCount = completedReport!!.turnsCount.toLong(),
                fluencyScore = completedReport!!.fluencyScore.toLong(),
                grammarFeedbackSummary = completedReport!!.overallFeedback
              )
            )
            onEndCallAndExit()
          }
        )
      }

      // --- In-Call Mode & Voice Settings Sheet ---
      if (showSettingsSheet) {
        ModalBottomSheet(
          onDismissRequest = { showSettingsSheet = false },
          containerColor = Color(0xFF0F172A),
          sheetState = rememberModalBottomSheetState()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 16.dp)
          ) {
            Text(
              text = "Conversation Mode",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
              modifier = Modifier.height(200.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(CallMode.entries) { mode ->
                val isSelected = mode == currentMode
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) LingoViolet.copy(alpha = 0.25f) else Color(0xFF1E293B))
                    .border(1.dp, if (isSelected) LingoViolet else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .clickable {
                      callManager.switchMode(mode)
                      showSettingsSheet = false
                    }
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = mode.emoji, fontSize = 24.sp)
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = mode.title,
                      color = Color.White,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 14.sp
                    )
                    Text(
                      text = mode.description,
                      color = Color.White.copy(alpha = 0.6f),
                      fontSize = 11.sp
                    )
                  }
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = LingoVioletLight
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
              text = "Tutor Voice Persona",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
              modifier = Modifier.height(180.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(TutorVoice.entries) { voice ->
                val isSelected = voice == currentVoice
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) LingoCyan.copy(alpha = 0.2f) else Color(0xFF1E293B))
                    .border(1.dp, if (isSelected) LingoCyan else Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .clickable {
                      callManager.switchVoice(voice)
                      showSettingsSheet = false
                    }
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = if (isSelected) LingoCyan else Color.White.copy(alpha = 0.6f)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = voice.displayName,
                      color = Color.White,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 14.sp
                    )
                    Text(
                      text = "${voice.gender} • ${voice.description}",
                      color = Color.White.copy(alpha = 0.6f),
                      fontSize = 11.sp
                    )
                  }
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = LingoCyan
                    )
                  }
                }
              }
            }
          }
        }
      }

      // --- Full Transcript Bottom Sheet ---
      if (showTranscriptSheet) {
        ModalBottomSheet(
          onDismissRequest = { showTranscriptSheet = false },
          containerColor = Color(0xFF0F172A),
          sheetState = rememberModalBottomSheetState()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 12.dp)
          ) {
            Text(
              text = "Live Conversation Transcript",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${transcript.size} turns exchanged in ${currentMode.title}",
              color = Color.White.copy(alpha = 0.6f),
              fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
              modifier = Modifier.height(380.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(transcript) { turn ->
                val isUser = turn.speaker == MessageSender.USER
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                  Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = if (isUser) LingoElectricBlue.copy(alpha = 0.25f) else Color(0xFF1E293B)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                      1.dp,
                      if (isUser) LingoElectricBlue.copy(alpha = 0.6f) else Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth(0.85f)
                  ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                      Text(
                        text = if (isUser) "You" else currentVoice.displayName,
                        color = if (isUser) LingoCyan else LingoVioletLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = turn.text,
                        color = Color.White,
                        fontSize = 14.sp
                      )
                      if (!turn.phoneticGuide.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "🗣️ ${turn.phoneticGuide}",
                          color = LingoCyan.copy(alpha = 0.8f),
                          fontSize = 11.sp
                        )
                      }
                      if (!turn.translation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = "🌐 ${turn.translation}",
                          color = Color.White.copy(alpha = 0.5f),
                          fontSize = 11.sp
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun PostCallReportDialog(
  report: CallLearningReport,
  targetLang: Language,
  onSaveAndClose: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xEE090D16))
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
      border = androidx.compose.foundation.BorderStroke(1.dp, LingoViolet.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("post_call_report_card")
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🎉 Call Complete!",
          color = Color.White,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${report.mode.emoji} ${report.mode.title} • ${targetLang.name}",
          color = LingoCyan,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Metrics Grid (Duration, Turns, Fluency Score)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          MetricItem(title = "Talk Time", value = "${report.durationSeconds / 60}m ${report.durationSeconds % 60}s")
          MetricItem(title = "Turns", value = "${report.turnsCount}")
          MetricItem(title = "Fluency", value = "${report.fluencyScore}%", highlightColor = LingoEmerald)
          MetricItem(title = "XP Gained", value = "+${report.xpEarned}", highlightColor = LingoAmber)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Teacher's overall feedback
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Tutor Coaching Summary",
              color = LingoVioletLight,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = report.overallFeedback,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 13.sp,
              lineHeight = 18.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vocabulary Learned Section
        if (report.vocabularyLearned.isNotEmpty()) {
          Text(
            text = "📚 Key Vocabulary Learned",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyColumn(
            modifier = Modifier.height(110.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(report.vocabularyLearned) { vocab ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF1E293B))
                  .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = vocab.term,
                    color = LingoCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = vocab.exampleSentence,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                  )
                }
                Text(
                  text = vocab.translation,
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 12.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onSaveAndClose,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("save_call_history_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = LingoViolet)
        ) {
          Text(
            text = "Save Session & Return Home",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }
    }
  }
}

@Composable
fun MetricItem(
  title: String,
  value: String,
  highlightColor: Color = Color.White
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF1E293B))
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Text(
      text = value,
      color = highlightColor,
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp
    )
    Text(
      text = title,
      color = Color.White.copy(alpha = 0.6f),
      fontSize = 11.sp
    )
  }
}
