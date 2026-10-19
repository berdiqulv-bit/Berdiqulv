package com.example

import com.example.data.model.*
import com.example.data.repository.BilimRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BilimPlatformUnitTest {

    private lateinit var repository: BilimRepository

    @Before
    fun setUp() {
        repository = BilimRepository()
    }

    @Test
    fun testInitialSuperadminAndOrganizationsLoaded() {
        val user = repository.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.SUPERADMIN, user?.role)

        val orgs = repository.organizations.value
        assertTrue(orgs.isNotEmpty())
        assertEquals("Toshkent Prezident Maktabi", orgs.first().name)
    }

    @Test
    fun testUserManagementAddAndToggleStatus() {
        val newUser = User(
            id = "test_user_1",
            fullName = "Sanjar Eshmatov",
            username = "sanjar_test",
            role = UserRole.STUDENT,
            assignedClass = "9-B"
        )
        val added = repository.addUser(newUser)
        assertTrue(added)

        val found = repository.users.value.firstOrNull { it.id == "test_user_1" }
        assertNotNull(found)
        assertEquals(UserStatus.ACTIVE, found?.status)

        // Toggle to blocked
        repository.toggleUserStatus("test_user_1")
        val blocked = repository.users.value.firstOrNull { it.id == "test_user_1" }
        assertEquals(UserStatus.BLOCKED, blocked?.status)
    }

    @Test
    fun testTeacherGradeAndParentNotification() {
        val student = repository.users.value.first { it.id == "user_student1" }
        assertNotNull(student.parentId)

        val grade = GradeRecord(
            studentId = student.id,
            studentName = student.fullName,
            subjectId = "subj_history",
            subjectName = "Tarix",
            classGroup = "9-A",
            assignmentTitle = "Oraliq test",
            score = 96,
            maxScore = 100,
            teacherId = "user_teacher1",
            teacherName = "Dilshod Saidov",
            date = "2026-10-12"
        )

        val initialNotifsCount = repository.notifications.value.size
        repository.addGrade(grade)

        val updatedGrades = repository.grades.value
        assertEquals(grade.id, updatedGrades.first().id)

        // Linked parent should have received a notification
        val updatedNotifs = repository.notifications.value
        assertTrue(updatedNotifs.size > initialNotifsCount)
        val latestNotif = updatedNotifs.first()
        assertEquals(student.parentId, latestNotif.recipientUserId)
        assertTrue(latestNotif.message.contains("96/100"))
    }

    @Test
    fun testAttendanceNoDuplicates() {
        val rec1 = AttendanceRecord(
            studentId = "user_student1",
            studentName = "Javohir Qodirov",
            subjectId = "subj_math",
            subjectName = "Matematika",
            classGroup = "9-A",
            date = "2026-10-12",
            status = AttendanceStatus.PRESENT,
            recordedByTeacherId = "user_teacher2"
        )
        repository.recordAttendance(rec1)

        val countAfterFirst = repository.attendance.value.count {
            it.studentId == "user_student1" && it.subjectId == "subj_math" && it.date == "2026-10-12"
        }
        assertEquals(1, countAfterFirst)

        // Record update for same date and student
        val rec2 = rec1.copy(status = AttendanceStatus.LATE)
        repository.recordAttendance(rec2)

        val countAfterSecond = repository.attendance.value.count {
            it.studentId == "user_student1" && it.subjectId == "subj_math" && it.date == "2026-10-12"
        }
        assertEquals(1, countAfterSecond)
        val currentStatus = repository.attendance.value.first {
            it.studentId == "user_student1" && it.subjectId == "subj_math" && it.date == "2026-10-12"
        }.status
        assertEquals(AttendanceStatus.LATE, currentStatus)
    }

    @Test
    fun testBackupAndRestore() {
        val backupMsg = repository.createBackupSnapshot()
        assertTrue(backupMsg.contains("Zaxira nusxasi"))

        val initialUsers = repository.users.value.size
        // Add a temporary user
        repository.addUser(User(fullName = "Vaqtinchalik O'quvchi", username = "temp_user", role = UserRole.STUDENT))
        assertEquals(initialUsers + 1, repository.users.value.size)

        // Restore
        val restoreMsg = repository.restoreFromBackup()
        assertTrue(restoreMsg.contains("muvaffaqiyatli tiklandi"))
        assertEquals(initialUsers, repository.users.value.size)
    }

    @Test
    fun testTelegramBotToggle() {
        val bot = repository.bots.value.first()
        val originalStatus = bot.isConnected

        repository.toggleBotConnection(bot.id)
        val toggled = repository.bots.value.first { it.id == bot.id }
        assertEquals(!originalStatus, toggled.isConnected)
    }
}
