package com.amedick.hospitalapp.utils

import android.content.Context

class Prefs(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE)

    fun saveLoginSession(userId: String, token: String) {
        sharedPreferences.edit()
            .putString(Constants.USER_ID_KEY, userId)
            .putString(Constants.TOKEN_KEY, token)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.contains(Constants.USER_ID_KEY)
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
