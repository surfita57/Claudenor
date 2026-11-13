package com.claudenor.gastos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Tx(
    val id: Long, val pkg: String, val bank: String, val title: String, val text: String,
    val amount: Double?, val kind: String, val time: Long
)

class Db(ctx: Context) : SQLiteOpenHelper(ctx.applicationContext, "gastos.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE tx(id INTEGER PRIMARY KEY AUTOINCREMENT, pkg TEXT, bank TEXT,
            title TEXT, text TEXT, amount REAL, kind TEXT, time INTEGER,
            UNIQUE(pkg, title, text, time))"""
        )
    }
    override fun onUpgrade(db: SQLiteDatabase, o: Int, n: Int) {}

    fun insert(t: Tx) {
        val v = ContentValues().apply {
            put("pkg", t.pkg); put("bank", t.bank); put("title", t.title); put("text", t.text)
            if (t.amount != null) put("amount", t.amount) else putNull("amount")
            put("kind", t.kind); put("time", t.time)
        }
        writableDatabase.insertWithOnConflict("tx", null, v, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun all(): List<Tx> {
        val out = ArrayList<Tx>()
        readableDatabase.rawQuery(
            "SELECT id,pkg,bank,title,text,amount,kind,time FROM tx ORDER BY time DESC", null
        ).use { c ->
            while (c.moveToNext()) out.add(
                Tx(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4),
                    if (c.isNull(5)) null else c.getDouble(5), c.getString(6), c.getLong(7))
            )
        }
        return out
    }

    fun clear() { writableDatabase.execSQL("DELETE FROM tx") }
}
