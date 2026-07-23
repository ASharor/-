package com.example.data.models

data class User(
    val username: String = "",
    val passwordHash: String = "",
    val name: String = "",
    val displayName: String = "",
    val isVerified: Boolean = false,
    val allowGroup: Boolean = true,
    val registeredAt: String = ""
)

data class Group(
    val id: String = "",
    val name: String = "",
    val owner: String = "",
    val members: List<String> = emptyList(),
    val avatar: String? = null,
    val createdAt: String = ""
)

data class ButtonInfo(
    val text: String = "",
    val url: String = ""
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val date: String = "",
    val active: Boolean = true,
    val button: ButtonInfo? = null
)

data class Profile(
    val username: String = "",
    val avatar: String? = null,
    val bio: String? = null,
    val settings: Map<String, Any>? = null
)

data class MemberList(
    val groupId: String = "",
    val members: List<String> = emptyList()
)

data class Report(
    val id: String = java.util.UUID.randomUUID().toString(),
    val username: String = "",
    val book: String = "",
    val topic: String = "",
    val grade: String = "",
    val duration: Int = 0, // in minutes
    val testCount: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val date: String = "", // ISO 8601 or YYYY-MM-DD
    val note: String = ""
)

data class Status(
    val username: String = "",
    val isOnline: Boolean = false,
    val isStudying: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val currentBook: String? = null,
    val studyStartTime: Long = 0L
)

data class LeaderboardEntry(
    val username: String = "",
    val totalStudy: Int = 0, // in minutes
    val rank: Int = 0,
    val todayStudy: Int = 0
)

data class Invite(
    val id: String = "",
    val groupId: String = "",
    val fromUser: String = "",
    val toUser: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val createdAt: String = ""
)
