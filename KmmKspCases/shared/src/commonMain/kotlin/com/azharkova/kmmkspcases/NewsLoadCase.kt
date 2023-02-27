package com.azharkova.kmmkspcases

import com.azharkova.core.GenUseCase
import com.azharkova.core.RequestType
import com.azharkova.core.UseCase

@GenUseCase(repo = TestApi::class, request = "loadNews")
class NewsLoadCase

@UseCase(repo = TestApi::class, request = RequestType.NEWS)
interface SimpleNewsLoadCase