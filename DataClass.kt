package com.example.moneyapp_2

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

data class Transazioni(var data: String? = null, var ora: String?= null, var importo: Double? = null, var causa: String? = null, var piuomeno: Boolean? = null)

@Keep
@IgnoreExtraProperties
data class User(var crono: ArrayList<Transazioni>? = null, val email: String? = null, var saldo: Double? = null, val username: String? = null)