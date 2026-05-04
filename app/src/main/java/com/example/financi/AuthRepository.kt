package com.example.financi

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val settingsRepo: SettingsRepository
) {
    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByEmail(email)
            ?: return Result.failure(Exception("Пользователь не найден"))
        val result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash)
        if (!result.verified) {
            return Result.failure(Exception("Неверный пароль"))
        }
        settingsRepo.saveUserId(user.id)
        return Result.success(user)
    }

    suspend fun register(email: String, password: String): Result<UserEntity> {
        if (userDao.getUserByEmail(email) != null) {
            return Result.failure(Exception("Email уже используется"))
        }
        val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val newUser = UserEntity(email = email, passwordHash = hash)
        val id = userDao.insert(newUser)
        val user = newUser.copy(id = id)
        settingsRepo.saveUserId(id)
        return Result.success(user)
    }

    suspend fun getCurrentUserId(): Long {
        return settingsRepo.userId.first()
    }

    suspend fun logout() {
        settingsRepo.clearSession()
    }
}