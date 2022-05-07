package com.example.expensesharing

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Person(
    var name: String? = null,
    var uuid: String? = UUID.randomUUID().toString(),
    var paidAmountString: String = "",
    var isAnonymous: Boolean = true,
) : Parcelable {
    val paidAmount: Float get() {
        return paidAmountString.toFloatOrNull() ?: 0f
    }

    fun touched() {
        isAnonymous = false
    }
}