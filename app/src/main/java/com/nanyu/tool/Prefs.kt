package com.nanyu.tool

import android.content.Context

object Prefs {
    private const val NAME = "nanyu_prefs"
    private const val KEY_SCRIPT_1 = "script_1"
    private const val KEY_SCRIPT_2 = "script_2"

    fun setScript1(ctx: Context, path: String) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_SCRIPT_1, path).apply()
    }
    fun getScript1(ctx: Context): String =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getString(KEY_SCRIPT_1, "") ?: ""

    fun setScript2(ctx: Context, path: String) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_SCRIPT_2, path).apply()
    }
    fun getScript2(ctx: Context): String =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getString(KEY_SCRIPT_2, "") ?: ""
}
