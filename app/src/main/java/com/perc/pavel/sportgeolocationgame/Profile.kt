package com.perc.pavel.sportgeolocationgame

import java.io.Serializable

/**
 * Профиль текущего либо любого друого пользователя
 * Created by pavel on 21.01.2018.
 */

class Profile(
        val name: String,
        val login: String,
        private val password: String?
) : Serializable {
}
