package com.vibecoder.di

import android.content.Context
import com.vibecoder.preview.PreviewManager
import com.vibecoder.providers.ProviderFactory
import com.vibecoder.runtime.proot.ProotRuntime
import com.vibecoder.storage.git.GitRepository
import com.vibecoder.storage.prefs.PreferencesRepository
import com.vibecoder.versioning.VersionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.Path
import okio.fs
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository {
        return PreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideProviderFactory(): ProviderFactory {
        return ProviderFactory()
    }

    @Provides
    @Singleton
    fun provideProjectRoot(@ApplicationContext context: Context): Path {
        return context.filesDir.toPath().resolve("projects")
    }
}

@Module
@InstallIn(ApplicationComponent::class)
object RuntimeModule {

    @Provides
    @Singleton
    fun provideProotRuntime(@ApplicationContext context: Context, projectRoot: Path): ProotRuntime {
        return ProotRuntime(context, projectRoot)
    }
}

@Module
@InstallIn(ApplicationComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideGitRepository(projectRoot: Path): GitRepository {
        return GitRepository(projectRoot)
    }

    @Provides
    @Singleton
    fun provideVersionManager(projectRoot: Path): VersionManager {
        return VersionManager(projectRoot)
    }
}

@Module
@InstallIn(ApplicationComponent::class)
object PreviewModule {

    @Provides
    @Singleton
    fun providePreviewManager(@ApplicationContext context: Context, projectRoot: Path): PreviewManager {
        return PreviewManager(context, projectRoot)
    }
}