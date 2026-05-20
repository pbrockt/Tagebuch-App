package com.pbrockt.tagebuch.di

import android.content.Context
import com.pbrockt.tagebuch.data.local.TagebuchDatabase
import com.pbrockt.tagebuch.data.local.crypto.CryptoManager
import com.pbrockt.tagebuch.data.local.dao.DiaryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): TagebuchDatabase {
        val passphrase = cryptoManager.generateDbPassphrase()
        return TagebuchDatabase.create(context, passphrase)
    }

    @Provides
    @Singleton
    fun provideDiaryDao(db: TagebuchDatabase): DiaryDao = db.diaryDao()
}
