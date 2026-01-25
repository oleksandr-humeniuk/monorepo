package com.oho.utils.domain

private const val DAY_MS = 24L * 60L * 60L * 1000L

data class DataGate(
    val minPostedAt: Long?, // null => Pro
    val limit: Int,
) {
    companion object {
        fun free(nowMs: Long = System.currentTimeMillis()): DataGate =
            DataGate(minPostedAt = nowMs - DAY_MS, limit = 50)

        fun pro(): DataGate =
            DataGate(minPostedAt = null, limit = 10_000) // or paging later
    }
}