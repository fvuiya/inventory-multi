package com.bsoft.inventorymanager.utils

import com.bsoft.inventorymanager.model.Supplier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object SupplierSerializationHelper {
    @JvmStatic
    fun serialize(supplier: Supplier): String = Json.encodeToString(supplier)

    @JvmStatic
    fun deserialize(json: String): Supplier = Json.decodeFromString(json)
}
