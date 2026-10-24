package com.example.data.repository

import com.example.data.model.CurriculumLesson
import com.example.data.model.Exercise
import com.example.data.model.ExerciseType
import com.example.data.model.FlashcardItem

object CurriculumData {

  fun getLessonsForLanguage(targetLangCode: String, nativeLangCode: String = "en"): List<CurriculumLesson> {
    val prefix = targetLangCode.lowercase()
    return when (prefix) {
      "es" -> spanishLessons
      "fr" -> frenchLessons
      "de" -> germanLessons
      "ar" -> arabicLessons
      "ja" -> japaneseLessons
      "zh" -> chineseLessons
      "it" -> italianLessons
      "ru" -> russianLessons
      "ur" -> urduLessons
      else -> genericLessons(targetLangCode)
    }
  }

  fun getStarterFlashcards(targetLangCode: String): List<FlashcardItem> {
    return when (targetLangCode.lowercase()) {
      "es" -> listOf(
        FlashcardItem("es_1", "es", "Hola", "Hello / Hi", "oh-lah", "Hola, ¿cómo estás?", 1),
        FlashcardItem("es_2", "es", "Por favor", "Please", "por fah-vor", "¿Me traes agua, por favor?", 1),
        FlashcardItem("es_3", "es", "Gracias", "Thank you", "grah-see-ahs", "Muchas gracias por tu ayuda.", 2),
        FlashcardItem("es_4", "es", "¿Cuánto cuesta?", "How much does it cost?", "kwan-toh kwes-tah", "¿Cuánto cuesta este café?", 2),
        FlashcardItem("es_5", "es", "Mucho gusto", "Nice to meet you", "moo-choh goos-toh", "Mucho gusto en conocerte.", 1),
        FlashcardItem("es_6", "es", "Buenos días", "Good morning", "bway-nohs dee-ahs", "Buenos días a todos.", 3),
        FlashcardItem("es_7", "es", "¿Dónde está el baño?", "Where is the bathroom?", "dohn-deh es-tah el bah-nyoh", "Disculpe, ¿dónde está el baño?", 1),
        FlashcardItem("es_8", "es", "La cuenta, por favor", "The bill, please", "lah kwen-tah por fah-vor", "La cuenta, por favor, señorita.", 2)
      )
      "fr" -> listOf(
        FlashcardItem("fr_1", "fr", "Bonjour", "Hello / Good day", "bohn-zhoor", "Bonjour, comment allez-vous?", 1),
        FlashcardItem("fr_2", "fr", "Merci beaucoup", "Thank you very much", "mair-see boh-koo", "Merci beaucoup pour le cadeau.", 1),
        FlashcardItem("fr_3", "fr", "S'il vous plaît", "Please (formal)", "seel voo pleh", "Un café, s'il vous plaît.", 2),
        FlashcardItem("fr_4", "fr", "Enchanté", "Nice to meet you", "ahn-shahn-tay", "Enchanté de vous rencontrer.", 1),
        FlashcardItem("fr_5", "fr", "Où sont les toilettes?", "Where are the restrooms?", "oo sohn lay twah-let", "Pardon, où sont les toilettes?", 2)
      )
      "ar" -> listOf(
        FlashcardItem("ar_1", "ar", "مرحباً (Marhaban)", "Hello", "mar-ha-ban", "مرحباً، كيف حالك؟", 1),
        FlashcardItem("ar_2", "ar", "شكراً (Shukran)", "Thank you", "shook-ran", "شكراً جزيلاً لك.", 1),
        FlashcardItem("ar_3", "ar", "من فضلك (Min fadlik)", "Please", "min fad-lik", "كوب ماء من فضلك.", 2),
        FlashcardItem("ar_4", "ar", "أهلاً وسهلاً (Ahlan wa Sahlan)", "Welcome", "ah-lan wa sah-lan", "أهلاً وسهلاً بكم في بيتنا.", 1),
        FlashcardItem("ar_5", "ar", "مع السلامة (Ma'a as-salama)", "Goodbye", "ma-a as-sa-la-ma", "مع السلامة وأتمنى لك يوماً سعيداً.", 2)
      )
      "de" -> listOf(
        FlashcardItem("de_1", "de", "Guten Tag", "Good day / Hello", "goo-ten tahk", "Guten Tag, wie geht es Ihnen?", 1),
        FlashcardItem("de_2", "de", "Bitte", "Please / You're welcome", "bit-teh", "Ein Wasser, bitte.", 1),
        FlashcardItem("de_3", "de", "Danke schön", "Thank you very much", "dahn-keh shoen", "Danke schön für Ihre Hilfe.", 2),
        FlashcardItem("de_4", "de", "Auf Wiedersehen", "Goodbye", "owf vee-der-zay-en", "Auf Wiedersehen und bis bald!", 1)
      )
      "ja" -> listOf(
        FlashcardItem("ja_1", "ja", "こんにちは (Konnichiwa)", "Hello / Good afternoon", "kohn-nee-chee-wah", "皆さん、こんにちは。", 1),
        FlashcardItem("ja_2", "ja", "ありがとう (Arigatou)", "Thank you", "ah-ree-gah-toh", "本当にありがとう。", 1),
        FlashcardItem("ja_3", "ja", "すみません (Sumimasen)", "Excuse me / Sorry", "soo-mee-mah-sen", "すみません、お会計をお願いします。", 2),
        FlashcardItem("ja_4", "ja", "はじめまして (Hajimemashite)", "Nice to meet you", "hah-jee-meh-mah-shee-teh", "はじめまして、田中です。", 1)
      )
      else -> listOf(
        FlashcardItem("gen_1", targetLangCode, "Hello / Welcome", "Greeting", "he-lo", "Hello! Nice to meet you.", 1),
        FlashcardItem("gen_2", targetLangCode, "Thank you", "Gratitude", "than-k-yoo", "Thank you very much.", 1),
        FlashcardItem("gen_3", targetLangCode, "Please", "Polite request", "pleez", "Please could you help me?", 2),
        FlashcardItem("gen_4", targetLangCode, "Goodbye", "Farewell", "good-bye", "Goodbye, see you soon!", 1)
      )
    }
  }

  private val spanishLessons = listOf(
    CurriculumLesson(
      id = "es_lesson_1",
      title = "Essential Greetings & First Words",
      description = "Master everyday hellos, polite manners, and natural introductions in Spanish.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "es",
      estimatedMinutes = 4,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "es_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "How do you say 'Hello' in Spanish?",
          explanationNative = "'Hola' is the universal greeting used at any time of day in all Spanish-speaking countries.",
          targetAudioText = "Hola",
          phoneticGuide = "oh-lah",
          options = listOf("Hola", "Adiós", "Gracias", "Por favor"),
          correctOptionIndex = 0
        ),
        Exercise(
          id = "es_ex_2",
          type = ExerciseType.LISTENING,
          prompt = "Listen to the speaker and choose what they said:",
          explanationNative = "'Mucho gusto' means 'Pleased to meet you' or 'Nice to meet you'.",
          targetAudioText = "Mucho gusto",
          phoneticGuide = "moo-choh goos-toh",
          options = listOf("Buenos días", "Mucho gusto", "Hasta luego", "Buenas noches"),
          correctOptionIndex = 1
        ),
        Exercise(
          id = "es_ex_3",
          type = ExerciseType.GRAMMAR,
          prompt = "Which phrase politely asks 'How are you?' to a new friend?",
          explanationNative = "'¿Cómo estás?' is the informal version used with peers and friends.",
          targetAudioText = "¿Cómo estás?",
          phoneticGuide = "koh-moh es-tahs",
          options = listOf("¿Cómo te llamas?", "¿Cómo estás?", "¿De dónde eres?", "¿Qué hora es?"),
          correctOptionIndex = 1
        ),
        Exercise(
          id = "es_ex_4",
          type = ExerciseType.WRITING,
          prompt = "Type the Spanish word for 'Thank you':",
          explanationNative = "'Gracias' is spelled G-R-A-C-I-A-S.",
          targetAudioText = "Gracias",
          phoneticGuide = "grah-see-ahs",
          acceptableAnswers = listOf("gracias", "muchas gracias")
        )
      )
    ),
    CurriculumLesson(
      id = "es_lesson_2",
      title = "At the Café & Tapas Bar",
      description = "Order coffee, delicious pastries, ask for the bill, and converse with servers.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "es",
      estimatedMinutes = 5,
      xpReward = 40,
      exercises = listOf(
        Exercise(
          id = "es_ex_5",
          type = ExerciseType.VOCABULARY,
          prompt = "What does 'Un café con leche, por favor' mean?",
          explanationNative = "It translates directly to 'A coffee with milk, please'.",
          targetAudioText = "Un café con leche, por favor",
          phoneticGuide = "oon kah-feh kohn leh-cheh, por fah-vor",
          options = listOf("A black tea, please", "A coffee with milk, please", "A cold water, please", "A fruit juice, please"),
          correctOptionIndex = 1
        ),
        Exercise(
          id = "es_ex_6",
          type = ExerciseType.READING,
          prompt = "Read the waiter's question: '¿Desean pedir la comida o solo bebidas?' What is being asked?",
          explanationNative = "'Pedir' means to order; 'comida' is food and 'bebidas' is drinks.",
          targetAudioText = "¿Desean pedir la comida o solo bebidas?",
          phoneticGuide = "deh-seh-ahn peh-deer lah koh-mee-dah",
          options = listOf("Are you ready for the bill?", "Do you wish to order food or just drinks?", "Where would you like to sit?", "Is everything delicious?"),
          correctOptionIndex = 1
        ),
        Exercise(
          id = "es_ex_7",
          type = ExerciseType.WRITING,
          prompt = "Translate 'The bill, please':",
          explanationNative = "'La cuenta, por favor' is the standard polite request at a restaurant.",
          targetAudioText = "La cuenta, por favor",
          phoneticGuide = "lah kwen-tah por fah-vor",
          acceptableAnswers = listOf("la cuenta por favor", "la cuenta, por favor", "la cuenta")
        )
      )
    ),
    CurriculumLesson(
      id = "es_lesson_3",
      title = "Mastering Ser vs. Estar",
      description = "Understand the golden grammar difference between permanent identity and temporary state.",
      category = "Grammar",
      proficiencyLevel = "Intermediate",
      targetLanguageCode = "es",
      estimatedMinutes = 6,
      xpReward = 50,
      exercises = listOf(
        Exercise(
          id = "es_ex_8",
          type = ExerciseType.GRAMMAR,
          prompt = "Complete: 'Yo _____ cansado después de trabajar.' (I am tired after working)",
          explanationNative = "'Estar' is used for temporary physical and emotional states, hence 'estoy'.",
          targetAudioText = "Yo estoy cansado después de trabajar.",
          phoneticGuide = "yoh es-toy kahn-sah-doh",
          options = listOf("soy", "estoy", "tengo", "hago"),
          correctOptionIndex = 1
        ),
        Exercise(
          id = "es_ex_9",
          type = ExerciseType.GRAMMAR,
          prompt = "Complete: 'Ella _____ médica en el hospital.' (She is a doctor)",
          explanationNative = "Professions and inherent characteristics take 'ser' ('es').",
          targetAudioText = "Ella es médica en el hospital.",
          phoneticGuide = "eh-yah es meh-dee-kah",
          options = listOf("está", "es", "tiene", "queda"),
          correctOptionIndex = 1
        )
      )
    ),
    CurriculumLesson(
      id = "es_lesson_4",
      title = "Job Interview & Professional Spanish",
      description = "Present your achievements, discuss work experience, and answer behavioral questions.",
      category = "Reading",
      proficiencyLevel = "Advanced",
      targetLanguageCode = "es",
      estimatedMinutes = 7,
      xpReward = 60,
      exercises = listOf(
        Exercise(
          id = "es_ex_10",
          type = ExerciseType.READING,
          prompt = "Read: 'Tengo cinco años de experiencia liderando proyectos de software.' What is the speaker's background?",
          explanationNative = "They have 5 years of experience leading software development projects.",
          targetAudioText = "Tengo cinco años de experiencia liderando proyectos de software.",
          options = listOf("5 years in sales", "5 years leading software projects", "5 years studying computer science", "5 years as a teacher"),
          correctOptionIndex = 1
        ),
        Exercise(
          id = "es_ex_11",
          type = ExerciseType.QUIZ,
          prompt = "Which response best demonstrates problem-solving in a job interview?",
          explanationNative = "'Resolví el conflicto escuchando las opiniones de todos' demonstrates collaboration and resolution.",
          targetAudioText = "Resolví el conflicto escuchando las opiniones de todos.",
          options = listOf("No me gustan los problemas", "Resolví el conflicto escuchando las opiniones de todos", "Esperé a que mi jefe lo arreglara", "Cambié de proyecto"),
          correctOptionIndex = 1
        )
      )
    )
  )

  private val frenchLessons = listOf(
    CurriculumLesson(
      id = "fr_lesson_1",
      title = "Bonjour! First Steps in French",
      description = "Learn warm French greetings, how to introduce yourself, and everyday courtesies.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "fr",
      estimatedMinutes = 4,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "fr_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "How do you say 'Thank you very much' in French?",
          explanationNative = "'Merci beaucoup' is the polite expression for 'Thank you very much'.",
          targetAudioText = "Merci beaucoup",
          phoneticGuide = "mair-see boh-koo",
          options = listOf("Merci beaucoup", "De rien", "Au revoir", "S'il vous plaît"),
          correctOptionIndex = 0
        ),
        Exercise(
          id = "fr_ex_2",
          type = ExerciseType.LISTENING,
          prompt = "Listen to the phrase and select its meaning:",
          explanationNative = "'Comment vous appelez-vous?' asks 'What is your name?' formally.",
          targetAudioText = "Comment vous appelez-vous?",
          phoneticGuide = "koh-mahn voo zah-play voo",
          options = listOf("Where do you live?", "What is your name?", "How old are you?", "Are you French?"),
          correctOptionIndex = 1
        )
      )
    )
  )

  private val germanLessons = listOf(
    CurriculumLesson(
      id = "de_lesson_1",
      title = "Guten Tag! German Essentials",
      description = "Learn greetings, ordering bread and coffee, and basic sentence construction.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "de",
      estimatedMinutes = 4,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "de_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "What does 'Guten Morgen' mean?",
          explanationNative = "'Guten Morgen' is used to wish someone Good Morning until around 11am.",
          targetAudioText = "Guten Morgen",
          phoneticGuide = "goo-ten mor-gen",
          options = listOf("Good evening", "Good morning", "Goodbye", "See you soon"),
          correctOptionIndex = 1
        )
      )
    )
  )

  private val arabicLessons = listOf(
    CurriculumLesson(
      id = "ar_lesson_1",
      title = "مرحباً! Welcome to Arabic",
      description = "Learn foundational greetings, pleasantries, and polite expressions.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "ar",
      estimatedMinutes = 5,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "ar_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "What is the standard Arabic greeting for 'Hello'?",
          explanationNative = "'مرحباً' (Marhaban) is widely understood across all Arabic-speaking countries.",
          targetAudioText = "مرحباً",
          phoneticGuide = "mar-ha-ban",
          options = listOf("مرحباً", "شكراً", "إلى اللقاء", "نعم"),
          correctOptionIndex = 0
        )
      )
    )
  )

  private val japaneseLessons = listOf(
    CurriculumLesson(
      id = "ja_lesson_1",
      title = "Konnichiwa! Everyday Japanese",
      description = "Essential greetings, saying thank you, and polite everyday phrases.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "ja",
      estimatedMinutes = 4,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "ja_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "How do you say 'Thank you' in Japanese?",
          explanationNative = "'ありがとう' (Arigatou) or politely 'ありがとうございます' (Arigatou gozaimasu).",
          targetAudioText = "ありがとうございます",
          phoneticGuide = "ah-ree-gah-toh goh-zah-ee-mahs",
          options = listOf("さようなら", "ありがとうございます", "おはよう", "すみません"),
          correctOptionIndex = 1
        )
      )
    )
  )

  private val chineseLessons = listOf(
    CurriculumLesson(
      id = "zh_lesson_1",
      title = "Ni Hao! Mandarin Foundations",
      description = "Tones, introductions, and essential words for meeting people.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "zh",
      estimatedMinutes = 4,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "zh_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "What does '你好' (Nǐ hǎo) mean?",
          explanationNative = "'你好' is the standard greeting meaning 'Hello' or 'You well'.",
          targetAudioText = "你好",
          phoneticGuide = "nee-how",
          options = listOf("Goodbye", "Hello", "Thank you", "Sorry"),
          correctOptionIndex = 1
        )
      )
    )
  )

  private val italianLessons = listOf(
    CurriculumLesson(
      id = "it_lesson_1",
      title = "Ciao! Beautiful Italian",
      description = "Greetings, ordering in trattorias, and polite everyday conversation.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "it",
      estimatedMinutes = 4,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "it_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "What does 'Per favore' mean in Italian?",
          explanationNative = "'Per favore' means 'Please'.",
          targetAudioText = "Per favore",
          phoneticGuide = "pehr fah-voh-reh",
          options = listOf("Thank you", "Please", "Excuse me", "Good night"),
          correctOptionIndex = 1
        )
      )
    )
  )

  private val russianLessons = listOf(
    CurriculumLesson(
      id = "ru_lesson_1",
      title = "Привет! Russian Foundations",
      description = "Learn greetings, pleasantries, and basic introductions.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "ru",
      estimatedMinutes = 4,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "ru_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "What is the polite Russian word for 'Thank you'?",
          explanationNative = "'Спасибо' (Spasibo) is the universal word for 'Thank you'.",
          targetAudioText = "Спасибо",
          phoneticGuide = "spah-see-bah",
          options = listOf("Пожалуйста", "Спасибо", "Здравствуйте", "До свидания"),
          correctOptionIndex = 1
        )
      )
    )
  )

  private val urduLessons = listOf(
    CurriculumLesson(
      id = "ur_lesson_1",
      title = "آداب! Urdu Essentials",
      description = "Polite greetings, introductions, and everyday expressions in Urdu.",
      category = "Vocabulary",
      proficiencyLevel = "Beginner",
      targetLanguageCode = "ur",
      estimatedMinutes = 4,
      xpReward = 30,
      exercises = listOf(
        Exercise(
          id = "ur_ex_1",
          type = ExerciseType.VOCABULARY,
          prompt = "What is the traditional polite greeting in Urdu?",
          explanationNative = "'آداب' (Aadaab) or 'السلام علیکم' (Assalam-o-Alaikum) are standard polite greetings.",
          targetAudioText = "السلام علیکم",
          phoneticGuide = "as-sa-lahm oh ah-lay-koom",
          options = listOf("السلام علیکم", "شکریہ", "خدا حافظ", "جی ہاں"),
          correctOptionIndex = 0
        )
      )
    )
  )

  private fun genericLessons(targetLangCode: String): List<CurriculumLesson> {
    return listOf(
      CurriculumLesson(
        id = "${targetLangCode}_lesson_1",
        title = "Essential Vocabulary & Greetings",
        description = "Learn how to greet others, introduce yourself, and say thank you.",
        category = "Vocabulary",
        proficiencyLevel = "Beginner",
        targetLanguageCode = targetLangCode,
        estimatedMinutes = 4,
        xpReward = 30,
        exercises = listOf(
          Exercise(
            id = "${targetLangCode}_ex_1",
            type = ExerciseType.VOCABULARY,
            prompt = "Select the universal polite word for 'Thank you':",
            explanationNative = "Expressing gratitude is the first step in learning any new language.",
            targetAudioText = "Thank you",
            options = listOf("Thank you", "Goodbye", "No", "Where"),
            correctOptionIndex = 0
          ),
          Exercise(
            id = "${targetLangCode}_ex_2",
            type = ExerciseType.LISTENING,
            prompt = "Listen carefully to the audio and select what you heard:",
            explanationNative = "Developing your ear for native pronunciation will accelerate fluency.",
            targetAudioText = "Hello, nice to meet you",
            options = listOf("Hello, nice to meet you", "What is the time?", "I am hungry", "See you tomorrow"),
            correctOptionIndex = 0
          )
        )
      )
    )
  }
}
