package com.doximity.yahtzee

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform