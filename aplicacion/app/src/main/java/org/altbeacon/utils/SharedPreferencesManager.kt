package org.altbeacon.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {
    private lateinit var sharedPreferences: SharedPreferences

    private const val SHARED_PREFS_FILE_NAME = "MyAppSharedPreferences"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val ADMIN = "false"
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokenAndAdminToSharedPreferences(context: Context, token: String, admin: Boolean) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(ACCESS_TOKEN_KEY, token)
        editor.putBoolean(ADMIN, admin)
        editor.apply()
    }

    fun getTokenFromSharedPreferences(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    fun clearTokenAndAdminFromSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(ACCESS_TOKEN_KEY)
        editor.remove(ADMIN)
        editor.apply()
    }

    fun existsToken(context:Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.contains(ACCESS_TOKEN_KEY)
    }

    fun isAdmin(context: Context): Boolean{
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(ADMIN, false);
    }
}
