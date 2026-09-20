package com.qfinopt.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.qfinopt.app.data.model.CustomReminder
import com.qfinopt.app.data.model.UserResponse

/**
 * Manages persistent user session storage on the device.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "qfinopt_user_session"
        private const val KEY_USER_JSON = "user_json"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_PRIVACY_MODE = "privacy_mode"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_APP_LOCK_PIN = "app_lock_pin"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_REMINDERS_JSON = "reminders_json"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun isPrivacyModeEnabled(): Boolean = prefs.getBoolean(KEY_PRIVACY_MODE, false)

    fun setPrivacyModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_MODE, enabled).apply()
    }

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    fun getAppLockPin(): String = prefs.getString(KEY_APP_LOCK_PIN, "1234") ?: "1234"

    fun setAppLockPin(pin: String) {
        prefs.edit().putString(KEY_APP_LOCK_PIN, pin).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun saveUser(user: UserResponse) {
        val userJson = gson.toJson(user)
        prefs.edit()
            .putString(KEY_USER_JSON, userJson)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUser(): UserResponse? {
        val userJson = prefs.getString(KEY_USER_JSON, null) ?: return null
        return try {
            gson.fromJson(userJson, UserResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUser() != null
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_JSON)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    fun getReminders(): List<CustomReminder> {
        val json = prefs.getString(KEY_REMINDERS_JSON, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CustomReminder>>() {}.type
            gson.fromJson<List<CustomReminder>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveReminders(reminders: List<CustomReminder>) {
        val json = gson.toJson(reminders)
        prefs.edit().putString(KEY_REMINDERS_JSON, json).apply()
    }

    fun addReminder(reminder: CustomReminder) {
        val list = getReminders().toMutableList()
        list.add(0, reminder)
        saveReminders(list)
    }

    fun deleteReminder(reminderId: String) {
        val list = getReminders().filter { it.id != reminderId }
        saveReminders(list)
    }

    fun toggleReminder(reminderId: String) {
        val list = getReminders().map {
            if (it.id == reminderId) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveReminders(list)
    }
}
