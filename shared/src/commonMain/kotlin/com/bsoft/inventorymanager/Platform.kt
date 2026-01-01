package com.bsoft.inventorymanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform