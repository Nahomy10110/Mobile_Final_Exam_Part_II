package com.example.cattlerotation.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateString(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es")).format(Date(this))