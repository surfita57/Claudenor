package com.claudenor.gastos

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class BankNotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (title.isEmpty() && text.isEmpty()) return

        val bank = Banks.known[pkg]
        val anyApp = getSharedPreferences("cfg", MODE_PRIVATE).getBoolean("anyApp", false)
        val full = "$title $text"
        val amount = Parser.amount(full)
        if (bank == null && !(anyApp && amount != null)) return

        Db(this).insert(
            Tx(0, pkg, bank ?: pkg, title, text, amount, Parser.kind(full), sbn.postTime)
        )
    }
}
