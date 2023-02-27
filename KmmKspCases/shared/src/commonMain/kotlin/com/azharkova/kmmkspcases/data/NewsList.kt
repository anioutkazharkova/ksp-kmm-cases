package com.azharkova.kmmkspcases.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsList(@SerialName("articles") val articles: List<NewsItem>)

@Serializable
data class NewsItem(
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)