package com.dicoding.capstone.cocodiag.features

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.capstone.cocodiag.data.local.UserPreference
import com.dicoding.capstone.cocodiag.data.repository.AuthRepository
import com.dicoding.capstone.cocodiag.data.repository.ClassificationRepository
import com.dicoding.capstone.cocodiag.data.repository.ConnectivityRepository
import com.dicoding.capstone.cocodiag.data.repository.ForumRepository
import com.dicoding.capstone.cocodiag.data.repository.UserRepository
import com.dicoding.capstone.cocodiag.di.Injection
import com.dicoding.capstone.cocodiag.features.classification.ClassificationViewModel
import com.dicoding.capstone.cocodiag.features.forum.ForumViewModel
import com.dicoding.capstone.cocodiag.features.main.dataStore
import com.dicoding.capstone.cocodiag.features.settings.SettingsViewModel
import com.dicoding.capstone.cocodiag.features.signin.SignInViewModel
import com.dicoding.capstone.cocodiag.features.signup.SignUpViewModel
import com.dicoding.capstone.cocodiag.features.splash.SplashViewModel

class ViewModelFactory(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository,
    private val classRepo: ClassificationRepository,
    private val forumRepo: ForumRepository,
    private val userPref: UserPreference,
    private val connectivity: ConnectivityRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                SplashViewModel(userPref) as T
            }

            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel(authRepo, userPref, connectivity) as T
            }

            modelClass.isAssignableFrom(SignInViewModel::class.java) -> {
                SignInViewModel(authRepo, userPref, connectivity) as T
            }

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(authRepo, userRepo, classRepo, userPref) as T
            }

            modelClass.isAssignableFrom(ClassificationViewModel::class.java) -> {
                ClassificationViewModel(classRepo, userPref, connectivity) as T
            }

            modelClass.isAssignableFrom(ForumViewModel::class.java) -> {
                ForumViewModel(userRepo, forumRepo, userPref) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    Injection.provideAuthRepository(),
                    Injection.provideUserRepository(context.dataStore),
                    Injection.provideClassificationRepository(context.dataStore),
                    Injection.provideForumRepository(context.dataStore),
                    Injection.provideUserPreference(context.dataStore),
                    Injection.provideConnectivityRepository(context)
                )
            }.also { instance = it }
    }
}