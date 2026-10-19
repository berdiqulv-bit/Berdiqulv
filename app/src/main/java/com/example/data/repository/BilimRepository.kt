package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class BilimRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _rankingCriteria = MutableStateFlow(RankingCriteria(50, 35, 15))
    val rankingCriteria: StateFlow<RankingCriteria> = _rankingCriteria.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _organizations = MutableStateFlow<List<Organization>>(emptyList())
    val organizations: StateFlow<List<Organization>> = _organizations.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

    private val _grades = MutableStateFlow<List<GradeRecord>>(emptyList())
    val grades: StateFlow<List<GradeRecord>> = _grades.asStateFlow()

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _quizAttempts = MutableStateFlow<List<QuizAttempt>>(emptyList())
    val quizAttempts: StateFlow<List<QuizAttempt>> = _quizAttempts.asStateFlow()

    private val _plans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val plans: StateFlow<List<SubscriptionPlan>> = _plans.asStateFlow()

    private val _bots = MutableStateFlow<List<TelegramBot>>(emptyList())
    val bots: StateFlow<List<TelegramBot>> = _bots.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _libraryMaterials = MutableStateFlow<List<LibraryMaterial>>(emptyList())
    val libraryMaterials: StateFlow<List<LibraryMaterial>> = _libraryMaterials.asStateFlow()

    private val _isMaintenanceMode = MutableStateFlow(false)
    val isMaintenanceMode: StateFlow<Boolean> = _isMaintenanceMode.asStateFlow()

    private var backupSnapshotUsers = listOf<User>()
    private var backupSnapshotGrades = listOf<GradeRecord>()
    private var backupSnapshotAttendance = listOf<AttendanceRecord>()

    init {
        initializeInitialData()
    }

    private fun today(): String = dateFormat.format(Date())

    fun login(user: User) {
        _currentUser.value = user
        addAuditLog("Tizimga kirish", user.fullName, user.role.displayName, "Xavfsizlik", "Muvaffaqiyatli tizimga kirdi (@${user.username})")
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            addAuditLog("Tizimdan chiqish", user.fullName, user.role.displayName, "Xavfsizlik", "Tizimdan chiqdi")
        }
        _currentUser.value = null
    }

    fun switchRoleForTesting(role: UserRole) {
        val current = _currentUser.value
        val target = _users.value.firstOrNull { it.role == role && !it.isSoftDeleted }
            ?: User(
                id = "demo_${role.name.lowercase()}",
                fullName = "Demo ${role.displayName}",
                username = "demo_${role.name.lowercase()}",
                role = role,
                organizationName = "Toshkent Prezident Maktabi"
            )
        _currentUser.value = target
        addAuditLog("Rol almashtirildi", current?.fullName ?: "Foydalanuvchi", role.displayName, "Navigatsiya", "${role.displayName} paneliga o'tildi")
    }

    fun addUser(user: User): Boolean {
        if (_users.value.any { it.username.equals(user.username, ignoreCase = true) }) {
            return false
        }
        _users.value = _users.value + user
        val actor = _currentUser.value
        addAuditLog("Yangi foydalanuvchi yaratildi", actor?.fullName ?: "Superadmin", actor?.role?.displayName ?: "Superadmin", "Foydalanuvchilar", "${user.fullName} (${user.role.displayName}) qo'shildi")
        return true
    }

    fun updateUser(updated: User) {
        _users.value = _users.value.map { if (it.id == updated.id) updated else it }
        if (_currentUser.value?.id == updated.id) {
            _currentUser.value = updated
        }
        val actor = _currentUser.value
        addAuditLog("Foydalanuvchi tahrirlandi", actor?.fullName ?: "Admin", actor?.role?.displayName ?: "Admin", "Foydalanuvchilar", "${updated.fullName} ma'lumotlari yangilandi")
    }

    fun toggleUserStatus(userId: String) {
        val user = _users.value.firstOrNull { it.id == userId } ?: return
        val newStatus = if (user.status == UserStatus.ACTIVE) UserStatus.BLOCKED else UserStatus.ACTIVE
        val updated = user.copy(status = newStatus)
        updateUser(updated)
        val actor = _currentUser.value
        addAuditLog("Hisob holati o'zgardi", actor?.fullName ?: "Admin", actor?.role?.displayName ?: "Admin", "Xavfsizlik", "${user.fullName} holati ${newStatus.displayName} ga o'zgartirildi")
    }

    fun archiveUser(userId: String) {
        val user = _users.value.firstOrNull { it.id == userId } ?: return
        updateUser(user.copy(status = UserStatus.ARCHIVED, isSoftDeleted = true))
    }

    fun restoreUser(userId: String) {
        val user = _users.value.firstOrNull { it.id == userId } ?: return
        updateUser(user.copy(status = UserStatus.ACTIVE, isSoftDeleted = false))
    }

    fun addOrganization(org: Organization) {
        _organizations.value = _organizations.value + org
        val actor = _currentUser.value
        addAuditLog("Muassasa qo'shildi", actor?.fullName ?: "Superadmin", "Superadmin", "Tashkilotlar", "${org.name} ro'yxatga olindi")
    }

    fun updateOrganization(updated: Organization) {
        _organizations.value = _organizations.value.map { if (it.id == updated.id) updated else it }
    }

    fun addSubject(subject: Subject) {
        _subjects.value = _subjects.value + subject
    }

    fun updateSubject(updated: Subject) {
        _subjects.value = _subjects.value.map { if (it.id == updated.id) updated else it }
    }

    fun toggleMaintenanceMode() {
        val newMode = !_isMaintenanceMode.value
        _isMaintenanceMode.value = newMode
        val actor = _currentUser.value
        addAuditLog(if (newMode) "Texnik tanaffus yoqildi" else "Texnik tanaffus o'chirildi", actor?.fullName ?: "Superadmin", "Superadmin", "Tizim", "Tizim holati yangilandi")
    }

    fun createBackupSnapshot(): String {
        backupSnapshotUsers = _users.value
        backupSnapshotGrades = _grades.value
        backupSnapshotAttendance = _attendance.value
        val actor = _currentUser.value
        addAuditLog("Zaxira nusxa olindi", actor?.fullName ?: "Superadmin", "Superadmin", "Zaxira", "${backupSnapshotUsers.size} ta hisob zaxiraga nusxalandi")
        return "Tizim ma'lumotlari zaxira nusxasi muvaffaqiyatli saqlandi (${backupSnapshotUsers.size} foydalanuvchi)."
    }

    fun restoreFromBackup(): String {
        if (backupSnapshotUsers.isEmpty()) return "Zaxira nusxasi topilmadi!"
        _users.value = backupSnapshotUsers
        _grades.value = backupSnapshotGrades
        _attendance.value = backupSnapshotAttendance
        val actor = _currentUser.value
        addAuditLog("Zaxiradan tiklandi", actor?.fullName ?: "Superadmin", "Superadmin", "Zaxira", "Tizim avvalgi holatga qaytarildi")
        return "Barcha ma'lumotlar avvalgi zaxiradan muvaffaqiyatli tiklandi."
    }

    fun cleanDemoData(backupFirst: Boolean): String {
        if (backupFirst) {
            createBackupSnapshot()
        }
        val superAdmin = _users.value.firstOrNull { it.role == UserRole.SUPERADMIN }
        val admin = _users.value.firstOrNull { it.role == UserRole.ADMIN }
        _users.value = listOfNotNull(superAdmin, admin)
        _grades.value = emptyList()
        _attendance.value = emptyList()
        _quizAttempts.value = emptyList()
        val actor = _currentUser.value
        addAuditLog("Sinov ma'lumotlari tozalandi", actor?.fullName ?: "Superadmin", "Superadmin", "Tizim", "Barcha test o'quvchi va baholar tozalandi")
        return "Sinov ma'lumotlari tozalandi. Faqat ma'muriy hisoblar saqlandi."
    }

    fun addGrade(grade: GradeRecord) {
        _grades.value = _grades.value + grade
        val actor = _currentUser.value
        addAuditLog("Baho qo'yildi", actor?.fullName ?: "O'qituvchi", "O'qituvchi", "Baholar", "${grade.studentName}: ${grade.assignmentTitle} - ${grade.score}/${grade.maxScore}")
    }

    fun recordAttendance(record: AttendanceRecord) {
        _attendance.value = _attendance.value.filterNot { it.studentId == record.studentId && it.subjectId == record.subjectId && it.date == record.date } + record
    }

    fun addQuiz(quiz: Quiz) {
        _quizzes.value = _quizzes.value + quiz
    }

    fun submitQuizAttempt(attempt: QuizAttempt) {
        _quizAttempts.value = _quizAttempts.value + attempt
        val actor = _currentUser.value
        addAuditLog("Test topshirildi", actor?.fullName ?: attempt.studentName, "O'quvchi", "Testlar", "${attempt.quizTitle}: ${attempt.totalScore}/${attempt.maxScore}")
    }

    fun addTelegramBot(bot: TelegramBot) {
        _bots.value = _bots.value + bot
    }

    fun toggleBotConnection(botId: String) {
        _bots.value = _bots.value.map { if (it.id == botId) it.copy(isConnected = !it.isConnected, lastPingTimestamp = System.currentTimeMillis()) else it }
    }

    fun sendTestBotNotification(botId: String, recipientName: String, text: String): Boolean {
        val bot = _bots.value.firstOrNull { it.id == botId } ?: return false
        _bots.value = _bots.value.map { if (it.id == botId) it.copy(totalDeliveredMessages = it.totalDeliveredMessages + 1) else it }
        val actor = _currentUser.value
        addAuditLog("Telegram bot xabari yuborildi", actor?.fullName ?: "Admin", "Telegram", "Bot", "${bot.name} orqali $recipientName ga yuborildi: $text")
        return true
    }

    fun addPlan(newPlan: SubscriptionPlan) {
        _plans.value = _plans.value + newPlan
    }

    fun updatePlan(updated: SubscriptionPlan) {
        _plans.value = _plans.value.map { if (it.id == updated.id) updated else it }
    }

    fun assignPlanToUser(userId: String, planId: String, durationDays: Int) {
        val user = _users.value.firstOrNull { it.id == userId } ?: return
        val expiry = System.currentTimeMillis() + (durationDays.toLong() * 24 * 3600 * 1000)
        updateUser(user.copy(subscriptionPlanId = planId, premiumExpiresAt = expiry))
    }

    fun cancelUserSubscription(userId: String) {
        val user = _users.value.firstOrNull { it.id == userId } ?: return
        updateUser(user.copy(subscriptionPlanId = "plan_free", premiumExpiresAt = 0L))
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
    }

    fun updateUserAvatar(userId: String, avatarUri: String) {
        val user = _users.value.firstOrNull { it.id == userId } ?: return
        val updated = user.copy(avatarUri = avatarUri)
        updateUser(updated)
        addAuditLog("Profil rasmi yangilandi", updated.fullName, updated.role.displayName, "Profil", "Yangi profil fotosurati saqlandi")
    }

    fun setVerificationStatus(userId: String, isVerified: Boolean, reason: String) {
        val actor = _currentUser.value
        if (actor == null || actor.role != UserRole.SUPERADMIN) return
        val user = _users.value.firstOrNull { it.id == userId } ?: return
        val updated = user.copy(
            isVerified = isVerified,
            verifiedReason = if (isVerified) reason else null,
            verifiedAt = if (isVerified) System.currentTimeMillis() else 0L
        )
        updateUser(updated)
        val actionTitle = if (isVerified) "Foydalanuvchi tasdiqlandi (Nishon berildi)" else "Tasdiq belgisi olib tashlandi"
        addAuditLog(actionTitle, actor.fullName, actor.role.displayName, "Tasdiqlash", "${user.fullName} (@${user.username}). Sabab: $reason")
    }

    fun deleteUserSecurely(userId: String, adminPass: String): Result<String> {
        val actor = _currentUser.value ?: return Result.failure(Exception("Autentifikatsiya talab qilinadi"))
        if (actor.role != UserRole.SUPERADMIN) {
            return Result.failure(Exception("Foydalanuvchini o'chirish faqat Superadmin vakolatida!"))
        }
        if (adminPass != "superadmin") {
            return Result.failure(Exception("Superadmin paroli noto'g'ri kiritildi!"))
        }
        if (actor.id == userId) {
            return Result.failure(Exception("Superadmin o'z hisobini o'chira olmaydi!"))
        }
        val target = _users.value.firstOrNull { it.id == userId } ?: return Result.failure(Exception("Foydalanuvchi topilmadi"))
        if (target.role == UserRole.SUPERADMIN) {
            val superadminCount = _users.value.count { it.role == UserRole.SUPERADMIN && !it.isSoftDeleted }
            if (superadminCount <= 1) {
                return Result.failure(Exception("Tizimdagi so'nggi Superadmin hisobini o'chirib bo'lmaydi!"))
            }
        }
        _users.value = _users.value.filterNot { it.id == userId }
        addAuditLog("Foydalanuvchi xavfsiz o'chirildi", actor.fullName, actor.role.displayName, "Xavfsizlik", "${target.fullName} (${target.role.displayName}, @${target.username}) hisobi o'chirildi.")
        return Result.success("${target.fullName} hisobi muvaffaqiyatli o'chirildi")
    }

    fun deleteUsersBatchSecurely(userIds: List<String>, adminPass: String): Result<String> {
        val actor = _currentUser.value ?: return Result.failure(Exception("Autentifikatsiya talab qilinadi"))
        if (actor.role != UserRole.SUPERADMIN) {
            return Result.failure(Exception("Ommaviy o'chirish faqat Superadmin vakolatida!"))
        }
        if (adminPass != "superadmin") {
            return Result.failure(Exception("Superadmin paroli tasdiqlanmadi!"))
        }
        val filteredIds = userIds.filterNot { it == actor.id }
        if (filteredIds.isEmpty()) {
            return Result.failure(Exception("O'chirish uchun mos foydalanuvchi tanlanmadi"))
        }
        val targets = _users.value.filter { filteredIds.contains(it.id) }
        _users.value = _users.value.filterNot { filteredIds.contains(it.id) }
        addAuditLog("Ommaviy hisoblar o'chirildi", actor.fullName, actor.role.displayName, "Xavfsizlik", "${targets.size} nafar foydalanuvchi o'chirildi.")
        return Result.success("${targets.size} nafar hisob muvaffaqiyatli o'chirildi")
    }

    fun updateRankingCriteria(criteria: RankingCriteria) {
        _rankingCriteria.value = criteria
        val actor = _currentUser.value
        val name = actor?.fullName ?: "Administrator"
        val role = actor?.role?.displayName ?: "Admin"
        addAuditLog("Reyting mezonlari yangilandi", name, role, "Reyting", "Baholar: ${criteria.gradesWeightPercent}%, Testlar: ${criteria.quizzesWeightPercent}%, Davomat: ${criteria.attendanceWeightPercent}%.")
    }

    fun getTopStudentsRanking(criteria: RankingCriteria = _rankingCriteria.value): List<StudentRankItem> {
        val studentUsers = _users.value.filter { it.role == UserRole.STUDENT && !it.isSoftDeleted }
        val rankedList = studentUsers.map { student ->
            val studentGrades = _grades.value.filter { it.studentId == student.id }
            val gradesAvg = if (studentGrades.isEmpty()) 85.0 else studentGrades.map { it.score }.average()

            val studentAttempts = _quizAttempts.value.filter { it.studentId == student.id }
            val quizAvg = if (studentAttempts.isEmpty()) 88.0 else studentAttempts.map { (it.totalScore.toDouble() / it.maxScore) * 100.0 }.average()

            val studentAtt = _attendance.value.filter { it.studentId == student.id }
            val attRate = if (studentAtt.isEmpty()) 100 else {
                val presentCount = studentAtt.count { it.status == AttendanceStatus.PRESENT }
                ((presentCount.toDouble() / studentAtt.size) * 100.0).toInt()
            }

            val totalScore = ((gradesAvg * (criteria.gradesWeightPercent / 100.0)) +
                    (quizAvg * (criteria.quizzesWeightPercent / 100.0)) +
                    (attRate * (criteria.attendanceWeightPercent / 100.0))).toInt()

            StudentRankItem(
                student = student,
                rank = 0,
                totalScore = totalScore,
                gradesAverage = gradesAvg,
                quizzesCompleted = studentAttempts.size,
                attendanceRate = attRate
            )
        }

        val sorted = rankedList.sortedWith(
            compareByDescending<StudentRankItem> { it.totalScore }
                .thenByDescending { it.gradesAverage }
                .thenByDescending { it.attendanceRate }
                .thenBy { it.student.fullName }
        ).take(5)

        return sorted.mapIndexed { index, item -> item.copy(rank = index + 1) }
    }

    private fun addAuditLog(action: String, actorName: String, actorRole: String, entityType: String, details: String) {
        val log = AuditLog(
            id = "audit_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            action = action,
            actorName = actorName,
            actorRole = actorRole,
            entityType = entityType,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    private fun initializeInitialData() {
        val org1 = Organization(
            id = "org_1",
            name = "Toshkent Prezident Maktabi",
            address = "Toshkent shahri, Yunusobod tumani, Amir Temur shox ko'chasi 14",
            phone = "+998 71 200 44 88",
            email = "info@prezidentmaktabi.uz",
            adminUserName = "Aziz Rahimov",
            planId = "plan_enterprise",
            maxStudents = 1200,
            maxTeachers = 120,
            activeStudentsCount = 850,
            activeTeachersCount = 74
        )
        val org2 = Organization(
            id = "org_2",
            name = "Samarqand 1-sonli Ixtisoslashtirilgan Maktab-Internati",
            address = "Samarqand shahri, Registon ko'chasi 25",
            phone = "+998 66 233 11 22",
            email = "info@saminternat1.uz",
            adminUserName = "Nodir Karimov",
            planId = "plan_pro",
            maxStudents = 600,
            maxTeachers = 50,
            activeStudentsCount = 420,
            activeTeachersCount = 38
        )
        _organizations.value = listOf(org1, org2)

        val superAdmin = User(
            id = "user_superadmin",
            fullName = "Alisher Vohidov",
            username = "superadmin",
            role = UserRole.SUPERADMIN,
            organizationId = "org_1",
            organizationName = "Bilim Markazi Bosh Boshqarmasi",
            email = "superadmin@bilimmarkazi.uz",
            phone = "+998 90 123 45 67",
            subscriptionPlanId = "plan_enterprise",
            isVerified = true,
            verifiedReason = "Bosh boshqaruvchi va tizim asoschisi",
            verifiedAt = System.currentTimeMillis()
        )
        val adminUser = User(
            id = "user_admin",
            fullName = "Aziz Rahimov",
            username = "admin_toshkent",
            role = UserRole.ADMIN,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "aziz.rahimov@prezidentmaktabi.uz",
            phone = "+998 93 321 00 11",
            isVerified = true,
            verifiedReason = "Prezident Maktabi Rasmiy Administratori",
            verifiedAt = System.currentTimeMillis()
        )
        val teacher1 = User(
            id = "user_teacher1",
            fullName = "Dilshod Saidov",
            username = "ustoz_tarix",
            role = UserRole.TEACHER,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "dilshod.saidov@maktab.uz",
            phone = "+998 94 555 12 34",
            assignedClass = "9-A, 10-B",
            assignedSubjects = listOf("Tarix", "Huquq"),
            isVerified = true,
            verifiedReason = "Oliy toifali Tarix fani o'qituvchisi",
            verifiedAt = System.currentTimeMillis()
        )
        val teacher2 = User(
            id = "user_teacher2",
            fullName = "Zilola Mahmudova",
            username = "ustoz_matematika",
            role = UserRole.TEACHER,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "zilola.m@maktab.uz",
            phone = "+998 97 888 44 22",
            assignedClass = "9-A, 11-A",
            assignedSubjects = listOf("Matematika", "Informatika"),
            isVerified = true,
            verifiedReason = "Xalqaro matematika fani o'qituvchisi",
            verifiedAt = System.currentTimeMillis()
        )
        val student1 = User(
            id = "user_student1",
            fullName = "Javohir Qodirov",
            username = "javohir_9a",
            role = UserRole.STUDENT,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "javohir.q@maktab.uz",
            phone = "+998 99 777 99 11",
            assignedClass = "9-A",
            parentId = "user_parent1",
            isVerified = true,
            verifiedReason = "Respublika Tarix olimpiadasi mutlaq g'olibi",
            verifiedAt = System.currentTimeMillis()
        )
        val student2 = User(
            id = "user_student2",
            fullName = "Madina Aliyeva",
            username = "madina_9a",
            role = UserRole.STUDENT,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "madina.a@maktab.uz",
            phone = "+998 99 666 88 22",
            assignedClass = "9-A",
            parentId = "user_parent1",
            isVerified = true,
            verifiedReason = "Prezident stipendiyasi sovrindori",
            verifiedAt = System.currentTimeMillis()
        )
        val student3 = User(
            id = "user_student3",
            fullName = "Temur Karimov",
            username = "temur_9a",
            role = UserRole.STUDENT,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "temur.k@maktab.uz",
            phone = "+998 99 555 77 33",
            assignedClass = "9-A",
            isVerified = false
        )
        val student4 = User(
            id = "user_student4",
            fullName = "Fotima Yusupova",
            username = "fotima_9a",
            role = UserRole.STUDENT,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "fotima.y@maktab.uz",
            phone = "+998 99 444 66 44",
            assignedClass = "9-A",
            isVerified = true,
            verifiedReason = "Aniq fanlar bo'yicha namunali o'quvchi",
            verifiedAt = System.currentTimeMillis()
        )
        val student5 = User(
            id = "user_student5",
            fullName = "Sardor Rustamov",
            username = "sardor_9a",
            role = UserRole.STUDENT,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "sardor.r@maktab.uz",
            phone = "+998 99 333 55 55",
            assignedClass = "9-A",
            isVerified = false
        )
        val student6 = User(
            id = "user_student6",
            fullName = "Nodira Vohidova",
            username = "nodira_9a",
            role = UserRole.STUDENT,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "nodira.v@maktab.uz",
            phone = "+998 99 222 44 66",
            assignedClass = "9-A",
            isVerified = false
        )
        val parent1 = User(
            id = "user_parent1",
            fullName = "Qodir Vohidov",
            username = "ota_qodir",
            role = UserRole.PARENT,
            organizationId = "org_1",
            organizationName = org1.name,
            email = "qodir.v@mail.uz",
            phone = "+998 90 999 11 00",
            linkedStudentIds = listOf("user_student1", "user_student2"),
            isVerified = true,
            verifiedReason = "Maktab Ota-onalar Qo'mitasi Raisi",
            verifiedAt = System.currentTimeMillis()
        )

        _users.value = listOf(superAdmin, adminUser, teacher1, teacher2, student1, student2, student3, student4, student5, student6, parent1)

        val subHistory = Subject("sub_hist", "O'zbekiston Tarixi", "Vatanimizning boy va shonli o'tmishi", "History", 0xFFD4AF37, listOf("9-A", "10-B"), listOf("user_teacher1"))
        val subMath = Subject("sub_math", "Algebra va Geometriya", "Aniq hisob-kitoblar va mantiqiy fikrlash", "Calculate", 0xFF2979FF, listOf("9-A", "11-A"), listOf("user_teacher2"))
        val subLaw = Subject("sub_law", "Davlat va Huquq Asoslari", "Qonunlar va fuqarolik burchlari", "Gavel", 0xFF00C853, listOf("9-A"), listOf("user_teacher1"))
        val subIT = Subject("sub_it", "Informatika va Axborot Texnologiyalari", "Dasturlash, ma'lumotlar va AI asoslari", "Computer", 0xFF7C4DFF, listOf("9-A"), listOf("user_teacher2"))
        _subjects.value = listOf(subHistory, subMath, subLaw, subIT)

        _grades.value = listOf(
            GradeRecord("g1", "user_student1", "Javohir Qodirov", "sub_hist", "O'zbekiston Tarixi", "9-A", "Amir Temur davlati tahlili", 98, 100, "A'lo darajada yoritilgan", "user_teacher1", "2026-10-10"),
            GradeRecord("g2", "user_student1", "Javohir Qodirov", "sub_math", "Algebra va Geometriya", "9-A", "Kvadrat tenglamalar nazorati", 95, 100, "To'liq yechim", "user_teacher2", "2026-10-11"),
            GradeRecord("g3", "user_student2", "Madina Aliyeva", "sub_hist", "O'zbekiston Tarixi", "9-A", "Buyuk Ipak Yo'li inshosi", 96, 100, "Juda chuqur fikrlar", "user_teacher1", "2026-10-10"),
            GradeRecord("g4", "user_student2", "Madina Aliyeva", "sub_math", "Algebra va Geometriya", "9-A", "Funksiyalar grafigi", 94, 100, "Aniq grafiklar", "user_teacher2", "2026-10-11"),
            GradeRecord("g5", "user_student3", "Temur Karimov", "sub_hist", "O'zbekiston Tarixi", "9-A", "Amir Temur davlati tahlili", 88, 100, "Yaxshi", "user_teacher1", "2026-10-10"),
            GradeRecord("g6", "user_student3", "Temur Karimov", "sub_math", "Algebra va Geometriya", "9-A", "Kvadrat tenglamalar nazorati", 85, 100, "Yaxshi", "user_teacher2", "2026-10-11"),
            GradeRecord("g7", "user_student4", "Fotima Yusupova", "sub_hist", "O'zbekiston Tarixi", "9-A", "Amir Temur davlati tahlili", 92, 100, "Juda yaxshi", "user_teacher1", "2026-10-10"),
            GradeRecord("g8", "user_student5", "Sardor Rustamov", "sub_hist", "O'zbekiston Tarixi", "9-A", "Amir Temur davlati tahlili", 82, 100, "Qoniqarli", "user_teacher1", "2026-10-10")
        )

        _attendance.value = listOf(
            AttendanceRecord("att1", "user_student1", "Javohir Qodirov", "sub_hist", "O'zbekiston Tarixi", "9-A", today(), AttendanceStatus.PRESENT, "user_teacher1"),
            AttendanceRecord("att2", "user_student2", "Madina Aliyeva", "sub_hist", "O'zbekiston Tarixi", "9-A", today(), AttendanceStatus.PRESENT, "user_teacher1"),
            AttendanceRecord("att3", "user_student3", "Temur Karimov", "sub_hist", "O'zbekiston Tarixi", "9-A", today(), AttendanceStatus.PRESENT, "user_teacher1"),
            AttendanceRecord("att4", "user_student4", "Fotima Yusupova", "sub_hist", "O'zbekiston Tarixi", "9-A", today(), AttendanceStatus.PRESENT, "user_teacher1"),
            AttendanceRecord("att5", "user_student5", "Sardor Rustamov", "sub_hist", "O'zbekiston Tarixi", "9-A", today(), AttendanceStatus.LATE, "user_teacher1", "5 daqiqa kechikdi"),
            AttendanceRecord("att6", "user_student6", "Nodira Vohidova", "sub_hist", "O'zbekiston Tarixi", "9-A", today(), AttendanceStatus.EXCUSED, "user_teacher1", "Shifokor ma'lumotnomasi bor")
        )

        val q1 = QuizQuestion("qq1", "Amir Temur qachon tavallud topgan?", listOf("1336-yil 9-aprel", "1370-yil 1-may", "1405-yil 18-fevral", "1342-yil 12-avgust"), 0, "Amir Temur 1336-yil 9-aprelda Kesh yaqinida tavallud topgan.")
        val q2 = QuizQuestion("qq2", "Temuriylar davlatining poytaxti qaysi shahar bo'lgan?", listOf("Samarqand", "Buxoro", "Hirot", "Kesh"), 0, "Amir Temur saltanatining bosh poytaxti Samarqand shahri bo'lgan.")
        val quiz1 = Quiz("quiz_1", "Temuriylar saltanati davri sinovi", "sub_hist", "O'zbekiston Tarixi", "9-A", QuizDifficulty.MEDIUM, 15, listOf(q1, q2), "user_teacher1")
        _quizzes.value = listOf(quiz1)

        _quizAttempts.value = listOf(
            QuizAttempt("qa1", "quiz_1", "Temuriylar saltanati davri sinovi", "user_student1", "Javohir Qodirov", "O'zbekiston Tarixi", 100, 100, 2, 2, "2026-10-12"),
            QuizAttempt("qa2", "quiz_1", "Temuriylar saltanati davri sinovi", "user_student2", "Madina Aliyeva", "O'zbekiston Tarixi", 95, 100, 2, 2, "2026-10-12"),
            QuizAttempt("qa3", "quiz_1", "Temuriylar saltanati davri sinovi", "user_student4", "Fotima Yusupova", "O'zbekiston Tarixi", 90, 100, 2, 2, "2026-10-12"),
            QuizAttempt("qa4", "quiz_1", "Temuriylar saltanati davri sinovi", "user_student3", "Temur Karimov", "O'zbekiston Tarixi", 85, 100, 2, 2, "2026-10-12")
        )

        _plans.value = listOf(
            SubscriptionPlan("plan_free", "Start (Bepul)", "Kichik to'garak va individual o'quvchilar uchun", 0L, 30, 50, 5, 2, 20, 50, false, listOf("50 nafar o'quvchi", "Asosiy elektron jurnal", "Cheklangan Telegram xabarlari")),
            SubscriptionPlan("plan_pro", "Professional", "Ixtisoslashtirilgan maktablar va o'quv markazlari uchun", 450000L, 30, 500, 40, 20, 500, 2000, true, listOf("500 o'quvchi va 40 o'qituvchi", "To'liq Telegram botlar integratsiyasi", "Ota-onalar mobil kabineti", "AI Yordamchi moduli")),
            SubscriptionPlan("plan_enterprise", "Prezident & Davlat Muassasalari", "Katta ta'lim majmualari va tarmoq maktablar uchun", 1200000L, 30, 5000, 300, 150, 5000, 10000, false, listOf("Cheksiz o'quvchilar va guruhlar", "Maxsus server va alohida ma'lumotlar bazasi", "24/7 Shaxsiy muhandislik yordami", "Barcha Premium nishonlar"))
        )

        _bots.value = listOf(
            TelegramBot("bot_1", "Bilim Ota-ona Xabarnoma Boti", "@BilimMarkaziParentBot", "712839****:AAH9f****", TelegramBotType.PARENT_NOTIFICATION, true, listOf("Baholar", "Davomat", "Muhim e'lonlar"), System.currentTimeMillis(), 1420),
            TelegramBot("bot_2", "Bilim O'quvchi AI Repetitor Boti", "@BilimMarkaziAIBot", "748392****:BBJ8k****", TelegramBotType.STUDENT_ASSISTANT, true, listOf("Uy vazifalariga yordam", "Kunlik testlar"), System.currentTimeMillis(), 890),
            TelegramBot("bot_3", "Bilim Superadmin Nazorat Boti", "@BilimMarkaziAdminBot", "789123****:CCK7m****", TelegramBotType.ADMIN_CONTROL, true, listOf("Xavfsizlik signallari", "To'lovlar auditi"), System.currentTimeMillis(), 315)
        )

        _libraryMaterials.value = listOf(
            LibraryMaterial("lib_1", "O'zbekiston Tarixi 9-sinf darsligi", "Tarix", "9-sinf", "G'. Karimov", "PDF", 14500000L, 240, "Respublika ta'lim vazirligi tasdiqlagan darslik", true, 420),
            LibraryMaterial("lib_2", "Algebra va Analiz Asoslari", "Matematika", "9-11 sinf", "M. Mirzayev", "PDF", 18200000L, 320, "Olimpiada va murakkab masalalar to'plami", true, 650),
            LibraryMaterial("lib_3", "Boburnoma audio va matnli qo'llanma", "Adabiyot", "9-sinf", "Z.M. Bobur", "EPUB", 8400000L, 180, "Sharhlar va qomusiy lug'at bilan", false, 190)
        )

        _notifications.value = listOf(
            NotificationItem("notif_1", "user_superadmin", "Tizim xavfsizligi", "Barcha audit xizmatlari barqaror ishlamoqda.", "Tizim", System.currentTimeMillis(), false),
            NotificationItem("notif_2", "user_student1", "Yangi baho", "O'zbekiston Tarixi fanidan '98' ball qo'yildi.", "Baho", System.currentTimeMillis() - 3600000, false, "sub_hist"),
            NotificationItem("notif_3", "user_parent1", "Davomat xabarnomasi", "Farzandingiz Javohir Qodirov 9-A darsida qatnashdi.", "Davomat", System.currentTimeMillis() - 7200000, true)
        )

        addAuditLog("Tizim ishga tushirildi", "Tizim Serveri", "Avtomatik", "Boshqaruv", "BILIM MARKAZI PRO moduli barqaror ishga tushirildi.")
    }
}
