package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.BilimRepository
import com.example.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BilimViewModel(
    val repository: BilimRepository = BilimRepository()
) : ViewModel() {

    private val screenBackStack = mutableListOf<AppScreen>()

    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val currentUser: StateFlow<User?> = repository.currentUser
    val organizations: StateFlow<List<Organization>> = repository.organizations
    val users: StateFlow<List<User>> = repository.users
    val subjects: StateFlow<List<Subject>> = repository.subjects
    val attendance: StateFlow<List<AttendanceRecord>> = repository.attendance
    val grades: StateFlow<List<GradeRecord>> = repository.grades
    val quizzes: StateFlow<List<Quiz>> = repository.quizzes
    val quizAttempts: StateFlow<List<QuizAttempt>> = repository.quizAttempts
    val plans: StateFlow<List<SubscriptionPlan>> = repository.plans
    val bots: StateFlow<List<TelegramBot>> = repository.bots
    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val libraryMaterials: StateFlow<List<LibraryMaterial>> = repository.libraryMaterials
    val isMaintenanceMode: StateFlow<Boolean> = repository.isMaintenanceMode
    val rankingCriteria: StateFlow<RankingCriteria> = repository.rankingCriteria

    val topStudentsRanking: StateFlow<List<StudentRankItem>> = combine(
        repository.users,
        repository.grades,
        repository.quizAttempts,
        repository.attendance,
        repository.rankingCriteria
    ) { _, _, _, _, criteria ->
        repository.getTopStudentsRanking(criteria)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getTopStudentsRanking())

    val userSearchQuery = MutableStateFlow("")
    val userRoleFilter = MutableStateFlow<UserRole?>(null)

    val filteredUsers: StateFlow<List<User>> = combine(
        users,
        userSearchQuery,
        userRoleFilter
    ) { allUsers, query, role ->
        allUsers.filter { user ->
            val matchesQuery = query.isBlank() ||
                    user.fullName.contains(query, ignoreCase = true) ||
                    user.username.contains(query, ignoreCase = true) ||
                    (user.assignedClass?.contains(query, ignoreCase = true) == true) ||
                    user.phone.contains(query)
            val matchesRole = role == null || user.role == role
            matchesQuery && matchesRole && !user.isSoftDeleted
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Quiz state
    private val _activeQuiz = MutableStateFlow<Quiz?>(null)
    val activeQuiz: StateFlow<Quiz?> = _activeQuiz.asStateFlow()

    private val _activeQuizQuestionIndex = MutableStateFlow(0)
    val activeQuizQuestionIndex: StateFlow<Int> = _activeQuizQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers.asStateFlow()

    private val _lastQuizAttemptResult = MutableStateFlow<QuizAttempt?>(null)
    val lastQuizAttemptResult: StateFlow<QuizAttempt?> = _lastQuizAttemptResult.asStateFlow()

    // Games state
    private val _selectedGameCategory = MutableStateFlow(GameCategory.HISTORY)
    val selectedGameCategory: StateFlow<GameCategory> = _selectedGameCategory.asStateFlow()

    private val _currentGameQuestionIndex = MutableStateFlow(0)
    val currentGameQuestionIndex: StateFlow<Int> = _currentGameQuestionIndex.asStateFlow()

    private val _gameScore = MutableStateFlow(0)
    val gameScore: StateFlow<Int> = _gameScore.asStateFlow()

    private val _gameStreak = MutableStateFlow(0)
    val gameStreak: StateFlow<Int> = _gameStreak.asStateFlow()

    private val _lastGameFeedback = MutableStateFlow<String?>(null)
    val lastGameFeedback: StateFlow<String?> = _lastGameFeedback.asStateFlow()

    // AI Chat state
    private val _aiMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Assalomu alaykum! Men BILIM MARKAZI PRO sun'iy intellekt ta'lim yordamchisiman. Darslar, fanlar, testlar yoki qoidalar bo'yicha qanday savolingiz bor?" to false
        )
    )
    val aiMessages: StateFlow<List<Pair<String, Boolean>>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    val aiQueriesUsed = MutableStateFlow(12)

    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    fun showNotice(msg: String) {
        _uiNotice.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_uiNotice.value == msg) {
                _uiNotice.value = null
            }
        }
    }

    fun clearNotice() {
        _uiNotice.value = null
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val prev = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = prev
            return true
        }
        if (_currentScreen.value != AppScreen.LOGIN && _currentScreen.value != AppScreen.DASHBOARD) {
            _currentScreen.value = AppScreen.DASHBOARD
            return true
        }
        return false
    }

    fun loginWithCredentials(username: String, pass: String): Boolean {
        val trimmedUser = username.trim()
        val user = repository.users.value.firstOrNull {
            it.username.equals(trimmedUser, ignoreCase = true) ||
                    (it.email.isNotBlank() && it.email.equals(trimmedUser, ignoreCase = true))
        }

        if (user != null) {
            if (user.status == UserStatus.BLOCKED) {
                showNotice("Ushbu hisob ma'muriyat tomonidan bloklangan!")
                return false
            }
            repository.login(user)
            screenBackStack.clear()
            _currentScreen.value = when (user.role) {
                UserRole.SUPERADMIN -> AppScreen.SUPERADMIN
                UserRole.ADMIN -> AppScreen.DASHBOARD
                UserRole.TEACHER -> AppScreen.TEACHER_PORTAL
                UserRole.STUDENT -> AppScreen.STUDENT_PORTAL
                UserRole.PARENT -> AppScreen.PARENT_PORTAL
            }
            showNotice("Xush kelibsiz, ${user.fullName}!")
            return true
        } else {
            showNotice("Login yoki parol noto'g'ri kiritildi")
            return false
        }
    }

    fun switchRole(role: UserRole) {
        repository.switchRoleForTesting(role)
        val user = repository.currentUser.value
        _currentScreen.value = when (role) {
            UserRole.SUPERADMIN -> AppScreen.SUPERADMIN
            UserRole.ADMIN -> AppScreen.DASHBOARD
            UserRole.TEACHER -> AppScreen.TEACHER_PORTAL
            UserRole.STUDENT -> AppScreen.STUDENT_PORTAL
            UserRole.PARENT -> AppScreen.PARENT_PORTAL
        }
        showNotice("${role.displayName} paneliga o'tildi")
    }

    fun logout() {
        repository.logout()
        screenBackStack.clear()
        _currentScreen.value = AppScreen.LOGIN
        showNotice("Tizimdan muvaffaqiyatli chiqildi")
    }

    fun updateRankingCriteria(criteria: RankingCriteria) {
        repository.updateRankingCriteria(criteria)
        showNotice("Reyting mezonlari yangilandi: Baholar ${criteria.gradesWeightPercent}%, Testlar ${criteria.quizzesWeightPercent}%, Davomat ${criteria.attendanceWeightPercent}%")
    }

    fun updateUserAvatar(userId: String, avatarUri: String) {
        repository.updateUserAvatar(userId, avatarUri)
        showNotice("Profil rasmi muvaffaqiyatli saqlandi")
    }

    fun setVerificationStatus(userId: String, isVerified: Boolean, reason: String) {
        repository.setVerificationStatus(userId, isVerified, reason)
        val msg = if (isVerified) "Tasdiqlash nishoni berildi!" else "Tasdiq belgisi olib tashlandi"
        showNotice(msg)
    }

    fun deleteUserSecurely(userId: String, adminPass: String): Boolean {
        val result = repository.deleteUserSecurely(userId, adminPass)
        return result.fold(
            onSuccess = {
                showNotice(it)
                true
            },
            onFailure = {
                showNotice(it.message ?: "Xatolik yuz berdi")
                false
            }
        )
    }

    fun deleteUsersBatchSecurely(userIds: List<String>, adminPass: String): Boolean {
        val result = repository.deleteUsersBatchSecurely(userIds, adminPass)
        return result.fold(
            onSuccess = {
                showNotice(it)
                true
            },
            onFailure = {
                showNotice(it.message ?: "Ommaviy o'chirishda xatolik")
                false
            }
        )
    }

    fun createNewUser(user: User): Boolean {
        val ok = repository.addUser(user)
        if (ok) {
            showNotice("Yangi foydalanuvchi (${user.fullName}) qo'shildi")
        } else {
            showNotice("Ushbu login band: @${user.username}")
        }
        return ok
    }

    fun updateUser(user: User) {
        repository.updateUser(user)
        showNotice("${user.fullName} ma'lumotlari yangilandi")
    }

    fun toggleUserStatus(userId: String) {
        repository.toggleUserStatus(userId)
        showNotice("Foydalanuvchi holati o'zgartirildi")
    }

    fun archiveUser(userId: String) {
        repository.archiveUser(userId)
        showNotice("Hisob arxivlandi")
    }

    fun restoreUser(userId: String) {
        repository.restoreUser(userId)
        showNotice("Hisob qayta tiklandi")
    }

    fun createOrganization(org: Organization) {
        repository.addOrganization(org)
        showNotice("Muassasa qo'shildi: ${org.name}")
    }

    fun createSubject(subject: Subject) {
        repository.addSubject(subject)
        showNotice("Yangi fan qo'shildi: ${subject.name}")
    }

    fun toggleMaintenanceMode() {
        repository.toggleMaintenanceMode()
        val status = if (repository.isMaintenanceMode.value) "yoqildi" else "o'chirildi"
        showNotice("Texnik tanaffus holati $status")
    }

    fun createBackup() {
        val res = repository.createBackupSnapshot()
        showNotice(res)
    }

    fun restoreBackup() {
        val res = repository.restoreFromBackup()
        showNotice(res)
    }

    fun cleanDemoData(backupFirst: Boolean) {
        val res = repository.cleanDemoData(backupFirst)
        showNotice(res)
    }

    fun recordGrade(studentId: String, subjectName: String, classGroup: String, assignment: String, score: Int, comment: String) {
        val student = repository.users.value.firstOrNull { it.id == studentId } ?: return
        val current = repository.currentUser.value
        val record = GradeRecord(
            id = "g_${System.currentTimeMillis()}",
            studentId = studentId,
            studentName = student.fullName,
            subjectId = "sub_custom",
            subjectName = subjectName,
            classGroup = classGroup,
            assignmentTitle = assignment,
            score = score,
            comment = comment,
            teacherId = current?.id ?: "ustoz",
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
        repository.addGrade(record)
        showNotice("${student.fullName} ga $score ball qo'yildi")
    }

    fun recordAttendance(studentId: String, subjectName: String, classGroup: String, status: AttendanceStatus, note: String) {
        val student = repository.users.value.firstOrNull { it.id == studentId } ?: return
        val current = repository.currentUser.value
        val record = AttendanceRecord(
            id = "att_${System.currentTimeMillis()}",
            studentId = studentId,
            studentName = student.fullName,
            subjectId = "sub_custom",
            subjectName = subjectName,
            classGroup = classGroup,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            status = status,
            recordedByTeacherId = current?.id ?: "ustoz",
            note = note
        )
        repository.recordAttendance(record)
        showNotice("${student.fullName}: Davomat belgilandi (${status.displayName})")
    }

    fun publishQuiz(quiz: Quiz) {
        repository.addQuiz(quiz)
        showNotice("Yangi test e'lon qilindi: ${quiz.title}")
    }

    fun startQuiz(quiz: Quiz) {
        _activeQuiz.value = quiz
        _activeQuizQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _lastQuizAttemptResult.value = null
        navigateTo(AppScreen.QUIZ_RUNNER)
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        _selectedAnswers.value = _selectedAnswers.value + (questionIndex to optionIndex)
    }

    fun nextQuizQuestion() {
        val quiz = _activeQuiz.value ?: return
        if (_activeQuizQuestionIndex.value < quiz.questions.size - 1) {
            _activeQuizQuestionIndex.value += 1
        }
    }

    fun prevQuizQuestion() {
        if (_activeQuizQuestionIndex.value > 0) {
            _activeQuizQuestionIndex.value -= 1
        }
    }

    fun submitQuiz() {
        val quiz = _activeQuiz.value ?: return
        val student = repository.currentUser.value ?: return
        var correct = 0
        quiz.questions.forEachIndexed { index, q ->
            if (_selectedAnswers.value[index] == q.correctOptionIndex) {
                correct++
            }
        }
        val maxPoints = quiz.questions.size * 10
        val earnedPoints = correct * 10
        val attempt = QuizAttempt(
            id = "attempt_${System.currentTimeMillis()}",
            quizId = quiz.id,
            quizTitle = quiz.title,
            studentId = student.id,
            studentName = student.fullName,
            subjectName = quiz.subjectName,
            totalScore = earnedPoints,
            maxScore = maxPoints,
            correctAnswersCount = correct,
            totalQuestionsCount = quiz.questions.size,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
        repository.submitQuizAttempt(attempt)
        _lastQuizAttemptResult.value = attempt
        showNotice("Test yakunlandi! Natija: $earnedPoints / $maxPoints ball")
    }

    fun setGameCategory(category: GameCategory) {
        _selectedGameCategory.value = category
        _currentGameQuestionIndex.value = 0
        _lastGameFeedback.value = null
    }

    fun answerGameQuestion(chosenOptionIndex: Int) {
        val categoryQuestions = GameRepository.gameQuestions.filter { it.category == _selectedGameCategory.value }
        if (categoryQuestions.isEmpty()) return
        val currentQ = categoryQuestions[_currentGameQuestionIndex.value % categoryQuestions.size]

        if (chosenOptionIndex == currentQ.correctIndex) {
            _gameScore.value += currentQ.points
            _gameStreak.value += 1
            _lastGameFeedback.value = "To'g'ri javob! +${currentQ.points} ball. ${currentQ.extraInfo}"
        } else {
            _gameStreak.value = 0
            _lastGameFeedback.value = "Noto'g'ri. To'g'ri javob: ${currentQ.options[currentQ.correctIndex]}. ${currentQ.extraInfo}"
        }

        viewModelScope.launch {
            delay(1500)
            if (_currentGameQuestionIndex.value < categoryQuestions.size - 1) {
                _currentGameQuestionIndex.value += 1
            } else {
                _currentGameQuestionIndex.value = 0
            }
        }
    }

    fun resetGame() {
        _gameScore.value = 0
        _gameStreak.value = 0
        _currentGameQuestionIndex.value = 0
        _lastGameFeedback.value = null
    }

    fun registerTelegramBot(bot: TelegramBot) {
        repository.addTelegramBot(bot)
        showNotice("Yangi Telegram bot ulandi: ${bot.name}")
    }

    fun toggleBotConnection(botId: String) {
        repository.toggleBotConnection(botId)
        showNotice("Bot ulanish holati yangilandi")
    }

    fun testSendBotNotification(botId: String, recipient: String, text: String) {
        val ok = repository.sendTestBotNotification(botId, recipient, text)
        if (ok) {
            showNotice("Telegram orqali sinov xabari yuborildi!")
        } else {
            showNotice("Bot faol emas yoki xatolik yuz berdi")
        }
    }

    fun createPlan(plan: SubscriptionPlan) {
        repository.addPlan(plan)
        showNotice("Yangi tarif rejasi yaratildi: ${plan.name}")
    }

    fun updatePlan(plan: SubscriptionPlan) {
        repository.updatePlan(plan)
        showNotice("Tarif rejasi yangilandi")
    }

    fun assignPlanToUser(userId: String, planId: String, days: Int) {
        repository.assignPlanToUser(userId, planId, days)
        showNotice("Foydalanuvchiga tarif va premium muddat belgilandi")
    }

    fun cancelUserSubscription(userId: String) {
        repository.cancelUserSubscription(userId)
        showNotice("Obuna bekor qilindi")
    }

    fun markNotificationAsRead(id: String) {
        repository.markNotificationAsRead(id)
    }

    fun sendAiQuestion(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank()) return
        _aiMessages.value = _aiMessages.value + (trimmed to true)
        _isAiLoading.value = true
        aiQueriesUsed.value += 1

        viewModelScope.launch {
            delay(1200)
            val response = generateEducationalResponse(trimmed)
            _aiMessages.value = _aiMessages.value + (response to false)
            _isAiLoading.value = false
        }
    }

    fun generateEducationalResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("temur") || p.contains("sohibqiron") ->
                "Amir Temur (1336-1405) — buyuk sarkarda va davlat arbobi, Markaziy Osiyo xalqlari madaniyatini yuksaltirgan, 'Kuch — adolatdadir' shiori ostida qudratli Temuriylar saltanatiga asos solgan zotdir."
            p.contains("navoiy") || p.contains("xamsa") ->
                "Alisher Navoiy (1441-1501) — o'zbek adabiy tilining asoschisi, g'azal mulkining sultoni. Uning 'Xamsa'si turkiy tildagi birinchi besh doston to'plami hisoblanadi."
            p.contains("pifagor") || p.contains("uchburchak") ->
                "Pifagor teoremasi: to'g'ri burchakli uchburchakda gipotenuza kvadratining yig'indisi katetlar kvadratlarining yig'indisiga teng: c² = a² + b²."
            p.contains("algebra") || p.contains("tenglama") ->
                "Kvadrat tenglama umumiy ko'rinishi: ax² + bx + c = 0. Diskriminant formulasi: D = b² - 4ac. Agar D > 0 bo'lsa, 2 ta haqiqiy ildiz mavjud."
            p.contains("ingliz") || p.contains("english") || p.contains("grammar") ->
                "Ingliz tilida grammatika qoidalari: Present Simple (odatdagi ish-harakatlar), Present Continuous (hozir bajarilayotgan), Present Perfect (natijasi mavjud ish-harakatlar)."
            else ->
                "Savolingiz qabul qilindi. '$prompt' mavzusi bo'yicha BILIM MARKAZI PRO o'quv dasturi asosida tahlil tayyorlandi. Qo'shimcha test va nazorat mashqlarini 'O'yinlar va Testlar' bo'limida bajarishingiz mumkin."
        }
    }
}
