package com.example.data.model

enum class GameCategory(val title: String, val iconRes: String, val colorHex: Long) {
    HISTORY("Tarix — Vaqt chizig'i & Shaxslar", "history", 0xFFD4AF37),
    GEOGRAPHY("Geografiya — Poytaxtlar & Xarita", "public", 0xFF2563EB),
    MATH("Matematika — Tezkor hisob & Mantiq", "calculate", 0xFF059669),
    UZBEK_LIT("Ona tili & Adabiyot durdonalari", "auto_stories", 0xFF9333EA),
    ENGLISH("Ingliz tili — So'z boyligi & Test", "translate", 0xFF0284C7),
    SCIENCE("Kimyo & Fizika — Formulalar", "science", 0xFFEA580C),
    INFORMATICS("Informatika & Algoritmika", "terminal", 0xFF0D9488)
}

data class GameQuestion(
    val id: String,
    val category: GameCategory,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val extraInfo: String,
    val points: Int = 100
)

data class TimelineItem(
    val year: Int,
    val yearDisplay: String,
    val event: String,
    val description: String
)

data class MatchPair(
    val left: String,
    val right: String
)

data class GameScore(
    val gameTitle: String,
    val score: Int,
    val maxScore: Int,
    val correctCount: Int,
    val streak: Int,
    val timestamp: Long = System.currentTimeMillis()
)
