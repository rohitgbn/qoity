package com.example.quoteverse.data.di

import com.example.quoteverse.data.repository.AuthRepository
import com.example.quoteverse.data.repository.AuthRepositoryImpl
import com.example.quoteverse.data.repository.CategoryRepository
import com.example.quoteverse.data.repository.CategoryRepositoryImpl
import com.example.quoteverse.data.repository.InteractionRepository
import com.example.quoteverse.data.repository.InteractionRepositoryImpl
import com.example.quoteverse.data.repository.MockDataRepository
import com.example.quoteverse.data.repository.ModerationRepository
import com.example.quoteverse.data.repository.ModerationRepositoryImpl
import com.example.quoteverse.data.repository.NotificationRepository
import com.example.quoteverse.data.repository.NotificationRepositoryImpl
import com.example.quoteverse.data.repository.PostRepository
import com.example.quoteverse.data.repository.PostRepositoryImpl
import com.example.quoteverse.data.repository.UserRepository
import com.example.quoteverse.data.repository.UserRepositoryImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Set to true for offline preview mode with built-in quotes & shayari.
    // Set to false when connecting to a real, configured Firebase project.
    const val USE_MOCK_DATA = true

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideMockRepository(): MockDataRepository = MockDataRepository()

    @Provides
    @Singleton
    fun provideAuthRepository(
        impl: AuthRepositoryImpl,
        mock: MockDataRepository
    ): AuthRepository = if (USE_MOCK_DATA) mock else impl

    @Provides
    @Singleton
    fun providePostRepository(
        impl: PostRepositoryImpl,
        mock: MockDataRepository
    ): PostRepository = if (USE_MOCK_DATA) mock else impl

    @Provides
    @Singleton
    fun provideInteractionRepository(
        impl: InteractionRepositoryImpl,
        mock: MockDataRepository
    ): InteractionRepository = if (USE_MOCK_DATA) mock else impl

    @Provides
    @Singleton
    fun provideUserRepository(
        impl: UserRepositoryImpl,
        mock: MockDataRepository
    ): UserRepository = if (USE_MOCK_DATA) mock else impl

    @Provides
    @Singleton
    fun provideCategoryRepository(
        impl: CategoryRepositoryImpl,
        mock: MockDataRepository
    ): CategoryRepository = if (USE_MOCK_DATA) mock else impl

    @Provides
    @Singleton
    fun provideNotificationRepository(
        impl: NotificationRepositoryImpl,
        mock: MockDataRepository
    ): NotificationRepository = if (USE_MOCK_DATA) mock else impl

    @Provides
    @Singleton
    fun provideModerationRepository(
        impl: ModerationRepositoryImpl,
        mock: MockDataRepository
    ): ModerationRepository = if (USE_MOCK_DATA) mock else impl
}
