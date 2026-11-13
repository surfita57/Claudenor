package com.claudenor.gastos

object Banks {
    val known = mapOf(
        "com.nu.production" to "Nubank",
        "com.itau" to "Itaú",
        "com.bradesco" to "Bradesco",
        "com.santander.app" to "Santander",
        "br.com.bb.android" to "Banco do Brasil",
        "br.com.intermedium" to "Inter",
        "com.c6bank.app" to "C6 Bank",
        "br.com.gabba.Caixa" to "Caixa",
        "com.picpay" to "PicPay",
        "com.mercadopago.wallet" to "Mercado Pago",
        "com.btg.pactual.banking" to "BTG",
        "br.com.neon" to "Neon",
        "com.google.android.apps.walletnfcrel" to "Google Wallet",
        "com.xp.carteira" to "XP",
    )
}

object Parser {
    private val money = Regex("""R\$\s?(\d{1,3}(?:\.\d{3})*,\d{2}|\d+,\d{2}|\d+(?:\.\d{2})?)""")
    private val income = Regex("""recebeu|recebido|received|pix recebido|depósito|deposito|estorno|cashback|crédito em conta""", RegexOption.IGNORE_CASE)
    private val expense = Regex("""compra|pagamento|pagou|paga|débito|debito|enviou|transferência enviada|pix enviado|saque|fatura|aprovad""", RegexOption.IGNORE_CASE)

    fun amount(s: String): Double? {
        val m = money.find(s) ?: return null
        val raw = m.groupValues[1]
        val norm = if (raw.contains(',')) raw.replace(".", "").replace(',', '.') else raw
        return norm.toDoubleOrNull()
    }

    fun kind(s: String): String = when {
        income.containsMatchIn(s) -> "entrada"
        expense.containsMatchIn(s) -> "gasto"
        else -> "outro"
    }
}
