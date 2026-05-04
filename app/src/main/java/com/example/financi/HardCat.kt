package com.example.financi

data class LocalCategory(val id: Long, val name: String)

object SampleData {
    val categories = listOf(
        LocalCategory(1L, "Еда"),
        LocalCategory(2L, "Транспорт"),
        LocalCategory(3L, "Зарплата"),
        LocalCategory(4L, "Развлечения")
    )
}