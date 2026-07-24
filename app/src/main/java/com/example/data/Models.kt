package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "custom_themes")
data class CustomTheme(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val bgColor: String,
    val keyBgColor: String,
    val keyTextColor: String,
    val accentColor: String,
    val borderRadius: Int,
    val fontFamily: String = "Sans-Serif",
    val isSystem: Boolean = false
)

@Immutable
@Entity(tableName = "text_shortcuts")
data class TextShortcut(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shortcut: String,
    val expandedText: String
)

@Immutable
@Entity(tableName = "typing_stats")
data class TypingStat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,
    val wordsTyped: Int = 0,
    val charsTyped: Int = 0,
    val backspacesPressed: Int = 0,
    val activeTimeSeconds: Int = 0
)

@Immutable
@Entity(tableName = "pending_tasks")
data class PendingTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)
