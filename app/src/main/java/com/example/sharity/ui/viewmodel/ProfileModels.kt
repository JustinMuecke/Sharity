package com.example.sharity.ui.viewmodel

import androidx.annotation.DrawableRes
import com.example.sharity.R


data class UserStats(
    val songs: Int,
    val sent: Int,
    val received: Int
)

data class Badge(
    val label: String
)

enum class ProfileImageOption(@DrawableRes val resId: Int) {
    ROCK(R.drawable.guitar_boy),
    POP(R.drawable.singerin),
    JAZZ(R.drawable.vinyl)
}