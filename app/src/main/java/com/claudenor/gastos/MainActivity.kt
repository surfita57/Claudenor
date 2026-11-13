package com.claudenor.gastos

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private lateinit var list: LinearLayout
    private lateinit var summary: TextView
    private lateinit var status: TextView
    private val brl = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val fmt = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR"))

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        status = TextView(this)
        val perm = Button(this).apply {
            text = "Permitir acesso às notificações"
            setOnClickListener { startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
        }
        val anyApp = CheckBox(this).apply {
            text = "Capturar qualquer app com valor em R$"
            val p = getSharedPreferences("cfg", MODE_PRIVATE)
            isChecked = p.getBoolean("anyApp", false)
            setOnCheckedChangeListener { _, v -> p.edit().putBoolean("anyApp", v).apply() }
        }
        summary = TextView(this).apply { textSize = 16f; setTypeface(null, Typeface.BOLD) }
        val row = LinearLayout(this)
        val export = Button(this).apply { text = "Exportar CSV"; setOnClickListener { exportCsv() } }
        val clear = Button(this).apply { text = "Apagar tudo"; setOnClickListener { Db(this@MainActivity).clear(); refresh() } }
        row.addView(export); row.addView(clear)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(list) }
        listOf(status, perm, anyApp, summary, row).forEach { root.addView(it) }
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        val on = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        status.text = if (on) "✔ Captura ativa" else "✖ Captura desativada — conceda a permissão"
        val txs = Db(this).all()
        val gasto = txs.filter { it.kind == "gasto" }.sumOf { it.amount ?: 0.0 }
        val entrada = txs.filter { it.kind == "entrada" }.sumOf { it.amount ?: 0.0 }
        summary.text = "${txs.size} registros • Gastos ${brl.format(gasto)} • Entradas ${brl.format(entrada)}"
        list.removeAllViews()
        txs.forEach { t ->
            list.addView(TextView(this).apply {
                setPadding(0, 16, 0, 16)
                val v = t.amount?.let { brl.format(it) } ?: "—"
                text = "${t.bank} • ${t.kind} • $v\n${fmt.format(Date(t.time))}\n${t.title}: ${t.text}"
            })
        }
    }

    private fun exportCsv() {
        val f = File(cacheDir, "gastos.csv")
        fun q(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
        f.writeText("data,banco,tipo,valor,titulo,texto\n" + Db(this).all().joinToString("\n") {
            listOf(fmt.format(Date(it.time)), it.bank, it.kind, it.amount?.toString() ?: "", q(it.title), q(it.text))
                .joinToString(",")
        })
        val uri = FileProvider.getUriForFile(this, "$packageName.files", f)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"; putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Exportar"))
    }
}
