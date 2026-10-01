package com.nanyu.tool

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var tvScript1: TextView
    private lateinit var tvScript2: TextView
    private lateinit var tvRootState: TextView

    private val pickScript1 = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val path = copyToTmp(uri, "nanyu_script_1.sh")
                Prefs.setScript1(this, path)
                refreshScriptLabels()
                Toast.makeText(this, "脚本1已保存", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickScript2 = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val path = copyToTmp(uri, "nanyu_script_2.sh")
                Prefs.setScript2(this, path)
                refreshScriptLabels()
                Toast.makeText(this, "脚本2已保存", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvRootState = findViewById(R.id.tvRootState)
        tvScript1 = findViewById(R.id.tvScript1)
        tvScript2 = findViewById(R.id.tvScript2)

        tvRootState.text = "Root：检测中…"
        Thread {
            val rooted = RootShell.isRooted()
            runOnUiThread {
                tvRootState.text = if (rooted) "Root：已获取 ✓" else "Root：未获取 ✗"
            }
        }.start()

        findViewById<MaterialButton>(R.id.btnReboot).setOnClickListener {
            confirmAndRun("重启手机", "reboot")
        }
        findViewById<MaterialButton>(R.id.btnRecovery).setOnClickListener {
            confirmAndRun("重启到 Recovery", "reboot recovery")
        }
        findViewById<MaterialButton>(R.id.btnFastboot).setOnClickListener {
            confirmAndRun("重启到 Fastboot", "reboot bootloader")
        }
        findViewById<MaterialButton>(R.id.btnFastbootd).setOnClickListener {
            confirmAndRun("重启到 Fastbootd", "reboot fastboot")
        }
        findViewById<MaterialButton>(R.id.btnShutdown).setOnClickListener {
            confirmAndRun("关机", "reboot -p")
        }

        findViewById<MaterialButton>(R.id.btnPick1).setOnClickListener {
            val i = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickScript1.launch(i)
        }
        findViewById<MaterialButton>(R.id.btnPick2).setOnClickListener {
            val i = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickScript2.launch(i)
        }

        findViewById<MaterialButton>(R.id.btnRunAll).setOnClickListener {
            val s1 = Prefs.getScript1(this)
            val s2 = Prefs.getScript2(this)
            if (s1.isEmpty() && s2.isEmpty()) {
                Toast.makeText(this, "请先选择脚本", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Thread {
                val log = StringBuilder()
                if (s1.isNotEmpty()) {
                    log.append("== 脚本1 ==\n")
                    log.append(RootShell.runScriptFile(s1))
                }
                if (s2.isNotEmpty()) {
                    log.append("\n== 脚本2 ==\n")
                    log.append(RootShell.runScriptFile(s2))
                }
                runOnUiThread { showResult(log.toString()) }
            }.start()
        }

        refreshScriptLabels()
    }

    private fun refreshScriptLabels() {
        val s1 = Prefs.getScript1(this)
        val s2 = Prefs.getScript2(this)
        tvScript1.text = if (s1.isEmpty()) "脚本1：未选择" else "脚本1：${File(s1).name}"
        tvScript2.text = if (s2.isEmpty()) "脚本2：未选择" else "脚本2：${File(s2).name}"
    }

    private fun copyToTmp(uri: Uri, name: String): String {
        val dst = File("/data/local/tmp", name)
        contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(dst).use { output ->
                input?.copyTo(output)
            }
        }
        RootShell.exec("chmod 755 ${dst.absolutePath}")
        return dst.absolutePath
    }

    private fun confirmAndRun(title: String, suCmd: String) {
        AlertDialog.Builder(this)
            .setTitle("确认操作")
            .setMessage("确定要执行：$title 吗？")
            .setPositiveButton("执行") { _, _ ->
                Thread {
                    RootShell.exec(suCmd)
                    runOnUiThread {
                        Toast.makeText(this, "$title 已发送", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showResult(log: String) {
        AlertDialog.Builder(this)
            .setTitle("执行结果")
            .setMessage(log.ifBlank { "（无输出）" })
            .setPositiveButton("OK", null)
            .show()
    }
}
