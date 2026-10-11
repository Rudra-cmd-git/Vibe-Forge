package com.vibecoder.di

import android.content.Context
import com.vibecoder.data.github.GitHubApiService
import com.vibecoder.data.github.GitHubRepository
import com.vibecoder.data.github.GitHubTokenManager
import com.vibecoder.data.git.GitIgnoreGenerator
import com.vibecoder.data.git.JGitRepositoryManager
import com.vibecoder.data.secrets.SecretScanner
import com.vibecoder.data.storage.Database
import com.vibecoder.data.storage.PreferencesManager
import com.vibecoder.agent.AgentExecutor
import com.vibecoder.agent.AgentConfig
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGitHubApiService(retrofit: Retrofit): GitHubApiService {
        return retrofit.create(GitHubApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGitHubTokenManager(@ApplicationContext context: Context): GitHubTokenManager {
        return GitHubTokenManager(context)
    }

    @Provides
    @Singleton
    fun provideJGitRepositoryManager(): JGitRepositoryManager {
        return JGitRepositoryManager()
    }

    @Provides
    @Singleton
    fun provideSecretScanner(): SecretScanner {
        return SecretScanner()
    }

    @Provides
    @Singleton
    fun provideGitIgnoreGenerator(): GitIgnoreGenerator {
        return GitIgnoreGenerator()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Database.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideGitHubRepository(
        apiService: GitHubApiService,
        tokenManager: GitHubTokenManager,
        gitManager: JGitRepositoryManager,
        @ApplicationContext context: Context
    ): GitHubRepository {
        return GitHubRepository(apiService, tokenManager, gitManager, context)
    }

    @Provides
    @Singleton
    fun provideAgentConfig(preferencesManager: PreferencesManager): AgentConfig {
        return AgentConfig(
            model = preferencesManager.agentModel.value,
            temperature = preferencesManager.agentTemperature.value,
            maxTokens = preferencesManager.agentMaxTokens.value,
            enableAutoCommit = preferencesManager.autoCommit.value,
            enableAutoPush = preferencesManager.autoPush.value
        )
    }

    @Provides
    @Singleton
    fun provideAgentExecutor(
        gitManager: JGitRepositoryManager,
        secretScanner: SecretScanner,
        database: Database,
        config: AgentConfig
    ): AgentExecutor {
        return AgentExecutor(gitManager, secretScanner, database, config)
    }
}

// IO Dispatcher for Hilt
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @Singleton
    @dagger.hilt.android.qualifiers.ApplicationContext
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}