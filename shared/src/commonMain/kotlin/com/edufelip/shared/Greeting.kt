package com.edufelip.shared

class Greeting {
    fun greet(): String = "Hello from Shared (${platformName()})"
}

expect fun platformName(): String

