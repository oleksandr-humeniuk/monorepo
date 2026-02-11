package com.oho.hiit_timer.data.storage

import com.oho.hiit_timer.domain.RestAfterLastWorkPolicy

private const val POLICY_SAME = 0
private const val POLICY_NONE = 1
private const val POLICY_CUSTOM = 2

fun RestAfterLastWorkPolicy.toDb(): Pair<Int, Int?> = when (this) {
    RestAfterLastWorkPolicy.SameAsRegular -> POLICY_SAME to null
    RestAfterLastWorkPolicy.None -> POLICY_NONE to null
    is RestAfterLastWorkPolicy.Custom -> POLICY_CUSTOM to this.seconds
}

fun policyFromDb(type: Int, custom: Int?): RestAfterLastWorkPolicy = when (type) {
    POLICY_SAME -> RestAfterLastWorkPolicy.SameAsRegular
    POLICY_NONE -> RestAfterLastWorkPolicy.None
    POLICY_CUSTOM -> RestAfterLastWorkPolicy.Custom(custom ?: 0)
    else -> RestAfterLastWorkPolicy.SameAsRegular
}

