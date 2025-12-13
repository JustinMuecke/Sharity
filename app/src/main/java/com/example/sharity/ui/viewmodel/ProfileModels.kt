package com.example.sharity.ui.viewmodel

import androidx.annotation.DrawableRes
import com.example.sharity.R

enum class ProfileImageOption(@DrawableRes val resId: Int) {
    ROCK(R.drawable.guitar_boy),
    POP(R.drawable.singerin),
    JAZZ(R.drawable.vinyl)
}