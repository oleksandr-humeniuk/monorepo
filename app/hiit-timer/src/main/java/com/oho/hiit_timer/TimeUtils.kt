package com.oho.hiit_timer

fun formatSec(totalSec: Int): String {
    val sec = totalSec.coerceAtLeast(0)
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
}
