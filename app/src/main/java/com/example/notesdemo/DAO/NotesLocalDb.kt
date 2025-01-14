// FIXME имя пакета не соответствует кодстайлу
package com.example.notesdemo.DAO

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notesdemo.model.Notes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@Database(entities = [Notes::class], version = 1)
@TypeConverters(Converters::class)
abstract class NotesLocalDb : RoomDatabase() {

    // FIXME нейминг невалидный - надо исправить в соответствии с кодстайлом kotlin
    abstract fun LocalNotesDao(): NotesDao

    companion object {
        var INSTANCE: NotesLocalDb? = null

        fun getInstance(context: Context, scope: CoroutineScope): NotesLocalDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotesLocalDb::class.java, "notesLocal.db"
                ).addCallback(NotesItemsCallback(scope))
                    .build()

                INSTANCE = instance
                instance
            }


        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

private class NotesItemsCallback(val scope: CoroutineScope) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // FIXME лучше это выделить в отдельную функцию (приватную в этом классе)
        //  createInitialNote(): Notes
        val initialNote = Notes(noteName = "Добро пожаловать!",noteText = """
            Перед Вами простое приложение для создания заметок.
            Это могут быть различные записи которые Вы хотели бы сохранить в Вашем телефоне.
            Записи могут содержать "Заголовок", "Текст" и "Изображение" из Вашей галереи.
            К сожаления т.к. это пока первая версия программы данные могут храниться только локально, на Вашем телефоне.
            Если приложение получит поддержку и будет востребованно оно обзаведётся различными возможностями по хранению данных 
            и их переносу в сеть между Вашими устройствами.
            Желаю приятной работы.
        """.trimIndent(), null ,Date())
        // FIXME выглядит хаком и ненадежно.
        //  задача пред-заполнения базы данных решается иначе, хоть и не так удобно (без дао)
        //  https://developer.android.com/training/data-storage/room/prepopulate
        //  я бы поискал более надежное и аккуратно выглядящее решение для заполнения данными через dao
        Executors.newSingleThreadExecutor().execute {
            NotesLocalDb.INSTANCE?.let {
                // FIXME тут должен использоваться или скоуп и корутины или потоки, а не всё вместе
                scope.launch {
                    it.LocalNotesDao().InsertNote(initialNote)
                }
            }
        }

    }
}
