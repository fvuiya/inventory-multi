package com.bsoft.inventorymanager.model

interface Person {
    var documentId: String
    var name: String
    var contactNumber: String
    var address: String
    var age: Int
    var photo: String? // Nullable in Kotlin usually safer
    var isActive: Boolean
}
