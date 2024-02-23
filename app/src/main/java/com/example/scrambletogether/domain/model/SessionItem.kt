package com.example.scrambletogether.domain.model

data class SessionItem(
    val id: String,
    val winTotal: Int,
    val loseTotal: Int,
    val drawTotal: Int,
    val gamemode: String = ""
)
