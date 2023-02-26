package com.azharkova.kmmkspcases

import com.azharkova.core.GenUseCase

@GenUseCase(repo = TestApi::class, request = "test")
class TestCase

@GenUseCase(repo = TestApi::class, request = "loadNews")
class NewsLoadCase