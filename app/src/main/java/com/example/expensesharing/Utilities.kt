package com.example.expensesharing

import java.math.RoundingMode

fun Float.round() = this.toBigDecimal().setScale(2, RoundingMode.UP).toFloat()