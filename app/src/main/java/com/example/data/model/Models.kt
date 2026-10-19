package com.example.data.model

enum class UserRole(val displayName: String, val badgeColorHex: Long) {
    SUPERADMIN("Superadmin", 0xFFD4AF37),
    ADMIN("Administrator", 0xFF1E88E5),
    TEACHER("O'qituvchi", 0xFF00C853),
    STUDENT("O'quvchi", 0xFF2979FF),
    PARENT("Ota-ona", 0xFFAB47BC)
}

enum class UserStatus(val displayName: String) {
    ACTIVE("Faol"),
    BLOCKED("Bloklangan"),
    ARCHIVED("Arxivlangan"),
    PENDING("Kutilmoqda")
}

data class User(
    val id: String,
    val fullName: String,
    val username: String,
    val role: UserRole,
    val status: UserStatus = UserStatus.ACTIVE,
    val organizationId: String? = null,
    val organizationName: String? = null,
    val email: String = "",
    val phone: String = "",
    val assignedClass: String? = null,
    val assignedSubjects: List<String> = emptyList(),
    val linkedStudentIds: List<String> = emptyList(),
    val parentId: String? = null,
    val isActivated: Boolean = true,
    val activationCode: String? = null,
    val activationCodeExpiresAt: Long = 0L,
    val subscriptionPlanId: String = "plan_free",
    val premiumExpiresAt: Long = 0L,
    val avatarUri: String? = null,
    val isVerified: Boolean = false,
    val verifiedReason: String? = null,
    val verifiedAt: Long = 0L,
    val isSoftDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class StudentRankItem(
    val student: User,
    val rank: Int,
    val totalScore: Int,
    val gradesAverage: Double,
    val quizzesCompleted: Int,
    val attendanceRate: Int
)

data class RankingCriteria(
    val gradesWeightPercent: Int = 50,
    val quizzesWeightPercent: Int = 35,
    val attendanceWeightPercent: Int = 15
)

data class Organization(
    val id: String,
    val name: String,
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val adminUserId: String = "",
    val adminUserName: String = "",
    val planId: String = "plan_free",
    val maxStudents: Int = 100,
    val maxTeachers: Int = 10,
    val activeStudentsCount: Int = 0,
    val activeTeachersCount: Int = 0,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Subject(
    val id: String,
    val name: String,
    val description: String = "",
    val iconName: String = "Book",
    val colorHex: Long = 0xFF2979FF,
    val grades: List<String> = emptyList(),
    val teacherIds: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val isArchived: Boolean = false
)

enum class AttendanceStatus(val displayName: String, val colorHex: Long) {
    PRESENT("Bor", 0xFF00C853),
    LATE("Kechikdi", 0xFFFFB300),
    ABSENT("Yo'q", 0xFFE53935),
    EXCUSED("Sababli", 0xFF1E88E5)
}

data class AttendanceRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val subjectId: String,
    val subjectName: String,
    val classGroup: String,
    val date: String,
    val status: AttendanceStatus,
    val recordedByTeacherId: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class GradeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val subjectId: String,
    val subjectName: String,
    val classGroup: String,
    val assignmentTitle: String,
    val score: Int,
    val maxScore: Int = 100,
    val comment: String = "",
    val teacherId: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class QuizDifficulty(val displayName: String) {
    EASY("Oson"),
    MEDIUM("O'rtacha"),
    HARD("Qiyin")
}

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String = "",
    val points: Int = 10
)

data class Quiz(
    val id: String,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val classGroup: String,
    val difficulty: QuizDifficulty,
    val timeLimitMinutes: Int = 15,
    val questions: List<QuizQuestion> = emptyList(),
    val createdByTeacherId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class QuizAttempt(
    val id: String,
    val quizId: String,
    val quizTitle: String,
    val studentId: String,
    val studentName: String,
    val subjectName: String,
    val totalScore: Int,
    val maxScore: Int,
    val correctAnswersCount: Int,
    val totalQuestionsCount: Int,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TelegramBotType(val displayName: String) {
    PARENT_NOTIFICATION("Ota-onalar xabarnomasi"),
    STUDENT_ASSISTANT("O'quvchi yordamchisi"),
    ADMIN_CONTROL("Boshqaruv va audit"),
    TEACHER_NOTIFICATIONS("O'qituvchi bildirishnomalari")
}

data class TelegramBot(
    val id: String,
    val name: String,
    val username: String,
    val tokenMasked: String,
    val type: TelegramBotType,
    val isConnected: Boolean = true,
    val enabledNotifications: List<String> = emptyList(),
    val lastPingTimestamp: Long = System.currentTimeMillis(),
    val totalDeliveredMessages: Int = 0
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val priceUzs: Long,
    val durationDays: Int = 30,
    val maxStudents: Int,
    val maxTeachers: Int,
    val maxClasses: Int,
    val monthlyQuizzesLimit: Int,
    val monthlyAiQueriesLimit: Int,
    val isPopular: Boolean = false,
    val features: List<String> = emptyList()
)

data class AuditLog(
    val id: String,
    val action: String,
    val actorName: String,
    val actorRole: String,
    val entityType: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String,
    val recipientUserId: String,
    val title: String,
    val message: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedSubject: String? = null
)

data class LibraryMaterial(
    val id: String,
    val title: String,
    val subjectName: String,
    val gradeLevel: String,
    val author: String,
    val fileType: String,
    val fileSizeBytes: Long,
    val pageCount: Int,
    val description: String = "",
    val isFavorite: Boolean = false,
    val downloadCount: Int = 0
)

enum class GameCategory(val title: String, val iconRes: String, val colorHex: Long) {
    HISTORY("Tarixiy Voqealar", "History", 0xFFD4AF37),
    GEOGRAPHY("Geografiya va Dunyo", "Public", 0xFF00ACC1),
    MATH("Tezkor Matematika", "Calculate", 0xFF2979FF),
    UZBEK_LIT("Ona Tili va Adabiyot", "MenuBook", 0xFF00C853),
    ENGLISH("Ingliz Tili", "Translate", 0xFFE91E63),
    SCIENCE("Tabiiy Fanlar", "Science", 0xFFFF9800),
    INFORMATICS("Informatika va IT", "Computer", 0xFF9C27B0)
}

data class GameQuestion(
    val id: String,
    val category: GameCategory,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val extraInfo: String = "",
    val points: Int = 20
)

data class MatchPair(
    val left: String,
    val right: String
)

data class TimelineItem(
    val year: Int,
    val yearDisplay: String,
    val event: String,
    val description: String
)

data class GameScore(
    val gameTitle: String,
    val score: Int,
    val maxScore: Int,
    val correctCount: Int,
    val streak: Int,
    val timestamp: Long = System.currentTimeMillis()
)
