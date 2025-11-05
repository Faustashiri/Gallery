package com.example.galleryapp

import androidx.compose.runtime.mutableStateListOf

data class Picture(
    val id: Int,
    val author: String,
    val url: String
)

object PictureRepository {

    private val pictures = mutableStateListOf<Picture>()

    fun getAll(): List<Picture> = pictures

    fun generateSamplePictures() {
        if (pictures.isEmpty()) {
            pictures.addAll(
                listOf(
                    Picture(1, "Алексей Петров", "https://picsum.photos/300?1"),
                    Picture(2, "Мария Зеленоградская", "https://picsum.photos/300?2"),
                    Picture(3, "Дмитрий Ковыркин", "https://picsum.photos/300?3"),
                    Picture(4, "Ольга Шляпина", "https://picsum.photos/300?4"),
                    Picture(5, "Сергей Коковин", "https://picsum.photos/300?5"),
                    Picture(6, "Екатерина Первая", "https://picsum.photos/300?6"),
                    Picture(7, "Иван Толстолобов", "https://picsum.photos/300?7")
                )
            )
        }
    }

    fun addPicture(author: String, url: String): AddResult {
        if (author.isBlank() || url.isBlank()) return AddResult.EmptyFields

        val exists = pictures.any { it.url == url }
        return if (exists) {
            AddResult.Duplicate
        } else {
            val newId = (pictures.maxOfOrNull { it.id } ?: 0) + 1
            pictures.add(Picture(newId, author.trim(), url.trim()))
            AddResult.Success
        }
    }

    fun removePicture(picture: Picture) {
        pictures.remove(picture)
    }

    fun clearAll() {
        pictures.clear()
    }

    // 💡 Удобный enum-результат добавления
    enum class AddResult {
        Success,
        Duplicate,
        EmptyFields
    }
}
