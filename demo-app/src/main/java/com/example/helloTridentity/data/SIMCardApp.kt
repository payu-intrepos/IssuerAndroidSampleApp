package com.example.helloTridentity.data

data class SIMCardApp(
    val id: Int,
    val subscriptionId: Int,
    val label: String,
    val number: String = ""
)
