package com.snehil.moon_stays_androidapp.core.util

import java.util.Locale

/**
 * Formats a price the same way the Next.js web client does:
 * Indian Rupee symbol with thousands grouping and no decimals,
 * e.g. `₹2,875`. Mirrors `₹${Math.round(x).toLocaleString()}` on the web.
 */
fun formatPrice(amount: Double): String =
    String.format(Locale.US, "₹%,d", Math.round(amount))

fun formatPrice(amount: Int): String =
    String.format(Locale.US, "₹%,d", amount)
