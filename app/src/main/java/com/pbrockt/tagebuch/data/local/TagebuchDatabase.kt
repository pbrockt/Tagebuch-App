package com.pbrockt.tagebuch.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.pbrockt.tagebuch.data.local.dao.DiaryDao
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [DiaryDay::class, DiaryPage::class],
    version = 1,
    exportSchema = false
)
abstract class TagebuchDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao

    companion object {
        fun create(context: Context, passphrase: ByteArray): TagebuchDatabase {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                TagebuchDatabase::class.java,
                "tagebuch.db"
            )
                .openHelperFactory(factory)
                .build()
        }
    }
}
