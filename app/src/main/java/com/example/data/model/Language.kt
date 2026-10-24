package com.example.data.model

data class Language(
  val code: String,
  val name: String,
  val nativeName: String,
  val flagEmoji: String,
  val localeCode: String,
  val isRtl: Boolean = false,
  val speechRecognitionSupported: Boolean = true,
  val ttsSupported: Boolean = true
) {
  companion object {
    val SUPPORTED_LANGUAGES = listOf(
      Language(
        code = "en",
        name = "English",
        nativeName = "English",
        flagEmoji = "🇺🇸",
        localeCode = "en-US",
        isRtl = false
      ),
      Language(
        code = "es",
        name = "Spanish",
        nativeName = "Español",
        flagEmoji = "🇪🇸",
        localeCode = "es-ES",
        isRtl = false
      ),
      Language(
        code = "fr",
        name = "French",
        nativeName = "Français",
        flagEmoji = "🇫🇷",
        localeCode = "fr-FR",
        isRtl = false
      ),
      Language(
        code = "de",
        name = "German",
        nativeName = "Deutsch",
        flagEmoji = "🇩🇪",
        localeCode = "de-DE",
        isRtl = false
      ),
      Language(
        code = "ar",
        name = "Arabic",
        nativeName = "العربية",
        flagEmoji = "🇸🇦",
        localeCode = "ar-SA",
        isRtl = true
      ),
      Language(
        code = "zh",
        name = "Chinese",
        nativeName = "中文 (普通话)",
        flagEmoji = "🇨🇳",
        localeCode = "zh-CN",
        isRtl = false
      ),
      Language(
        code = "ja",
        name = "Japanese",
        nativeName = "日本語",
        flagEmoji = "🇯🇵",
        localeCode = "ja-JP",
        isRtl = false
      ),
      Language(
        code = "ko",
        name = "Korean",
        nativeName = "한국어",
        flagEmoji = "🇰🇷",
        localeCode = "ko-KR",
        isRtl = false
      ),
      Language(
        code = "hi",
        name = "Hindi",
        nativeName = "हिन्दी",
        flagEmoji = "🇮🇳",
        localeCode = "hi-IN",
        isRtl = false
      ),
      Language(
        code = "ur",
        name = "Urdu",
        nativeName = "اردو",
        flagEmoji = "🇵🇰",
        localeCode = "ur-PK",
        isRtl = true
      ),
      Language(
        code = "pt",
        name = "Portuguese",
        nativeName = "Português",
        flagEmoji = "🇧🇷",
        localeCode = "pt-BR",
        isRtl = false
      ),
      Language(
        code = "it",
        name = "Italian",
        nativeName = "Italiano",
        flagEmoji = "🇮🇹",
        localeCode = "it-IT",
        isRtl = false
      ),
      Language(
        code = "tr",
        name = "Turkish",
        nativeName = "Türkçe",
        flagEmoji = "🇹🇷",
        localeCode = "tr-TR",
        isRtl = false
      ),
      Language(
        code = "ru",
        name = "Russian",
        nativeName = "Русский",
        flagEmoji = "🇷🇺",
        localeCode = "ru-RU",
        isRtl = false
      ),
      Language(
        code = "ps",
        name = "Pashto",
        nativeName = "پښتو",
        flagEmoji = "🇦🇫",
        localeCode = "ps-AF",
        isRtl = true
      ),
      Language(
        code = "fa",
        name = "Persian",
        nativeName = "فارسی",
        flagEmoji = "🇮🇷",
        localeCode = "fa-IR",
        isRtl = true
      )
    )

    fun findByCode(code: String): Language {
      return SUPPORTED_LANGUAGES.firstOrNull { it.code.equals(code, ignoreCase = true) }
        ?: SUPPORTED_LANGUAGES.first()
    }
  }
}
