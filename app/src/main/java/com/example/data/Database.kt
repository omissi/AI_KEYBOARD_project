package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyboardDao {
    @Query("SELECT * FROM custom_themes")
    fun getAllThemes(): Flow<List<CustomTheme>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: CustomTheme)

    @Delete
    suspend fun deleteTheme(theme: CustomTheme)

    @Query("SELECT * FROM text_shortcuts")
    fun getAllShortcuts(): Flow<List<TextShortcut>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: TextShortcut)

    @Query("DELETE FROM text_shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Int)

    @Query("SELECT * FROM typing_stats")
    fun getAllStats(): Flow<List<TypingStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStat(stat: TypingStat)

    @Query("SELECT * FROM pending_tasks ORDER BY orderIndex ASC")
    fun getAllTasks(): Flow<List<PendingTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PendingTask)

    @Update
    suspend fun updateTask(task: PendingTask)

    @Query("DELETE FROM pending_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("SELECT COUNT(*) FROM custom_themes")
    suspend fun themeCount(): Int
}

@Database(
    entities = [CustomTheme::class, TextShortcut::class, TypingStat::class, PendingTask::class],
    version = 1,
    exportSchema = false
)
abstract class KeyboardDatabase : RoomDatabase() {
    abstract fun keyboardDao(): KeyboardDao

    companion object {
        @Volatile private var INSTANCE: KeyboardDatabase? = null

        fun getDatabase(context: Context): KeyboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyboardDatabase::class.java,
                    "ai_keyboard_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class KeyboardRepository(private val dao: KeyboardDao) {
    val allThemes: Flow<List<CustomTheme>> = dao.getAllThemes()
    val allShortcuts: Flow<List<TextShortcut>> = dao.getAllShortcuts()
    val allStats: Flow<List<TypingStat>> = dao.getAllStats()
    val allTasks: Flow<List<PendingTask>> = dao.getAllTasks()

    suspend fun insertTheme(theme: CustomTheme) = dao.insertTheme(theme)
    suspend fun deleteTheme(theme: CustomTheme) = dao.deleteTheme(theme)
    suspend fun insertShortcut(shortcut: TextShortcut) = dao.insertShortcut(shortcut)
    suspend fun deleteShortcutById(id: Int) = dao.deleteShortcutById(id)
    suspend fun insertStat(stat: TypingStat) = dao.insertStat(stat)
    suspend fun insertTask(task: PendingTask) = dao.insertTask(task)
    suspend fun updateTask(task: PendingTask) = dao.updateTask(task)
    suspend fun deleteTaskById(id: Int) = dao.deleteTaskById(id)

    suspend fun checkAndPrepopulate() {
        if (dao.themeCount() == 0) {
            dao.insertTheme(CustomTheme(name = "AI Keyboard الافتراضي", bgColor = "#0F172A", keyBgColor = "#1E293B", keyTextColor = "#FFFFFF", accentColor = "#3B82F6", borderRadius = 12, isSystem = true))
            dao.insertTheme(CustomTheme(name = "الوضع النهاري", bgColor = "#F8FAFC", keyBgColor = "#E2E8F0", keyTextColor = "#0F172A", accentColor = "#2563EB", borderRadius = 12, isSystem = true))
            dao.insertTheme(CustomTheme(name = "نيون احترافي", bgColor = "#0A0518", keyBgColor = "#2D0E5E", keyTextColor = "#FFFFFF", accentColor = "#FF007F", borderRadius = 16, isSystem = true))
        }
    }
}
