package com.nanyu.tool

import java.io.BufferedReader
import java.io.InputStreamReader

object RootShell {

    fun exec(cmd: String): String {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val out = BufferedReader(InputStreamReader(p.inputStream))
            val err = BufferedReader(InputStreamReader(p.errorStream))
            val sb = StringBuilder()
            var line: String?
            while (out.readLine().also { line = it } != null) sb.append(line).append("\n")
            while (err.readLine().also { line = it } != null) sb.append(line).append("\n")
            p.waitFor()
            sb.toString()
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }

    fun isRooted(): Boolean {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val r = BufferedReader(InputStreamReader(p.inputStream))
            val line = r.readLine() ?: ""
            p.waitFor()
            line.contains("uid=0")
        } catch (e: Exception) { false }
    }

    fun runScriptFile(scriptPath: String): String {
        return exec("sh \"$scriptPath\"")
    }
}
