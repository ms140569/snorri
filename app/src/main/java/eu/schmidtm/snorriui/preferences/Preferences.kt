package eu.schmidtm.snorriui.preferences

import android.content.Context
import android.content.SharedPreferences

// https://blog.teamtreehouse.com/making-sharedpreferences-easy-with-kotlin

class Preferences(context: Context) {
    val PREFS_FILENAME = "eu.schmidtm.snorriui.preferences"

    val URL_KEY = "url"
    val USER_KEY = "user"
    val PASSWORD_KEY = "password"

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var url: String
        get() = prefs.getString(URL_KEY, "")
        set(value) = prefs.edit().putString(URL_KEY, value).apply()

    var user: String
        get() = prefs.getString(USER_KEY, "")
        set(value) = prefs.edit().putString(USER_KEY, value).apply()

    var password: String
        get() = prefs.getString(PASSWORD_KEY, "")
        set(value) = prefs.edit().putString(PASSWORD_KEY, value).apply()
}
