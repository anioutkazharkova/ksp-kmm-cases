package com.azharkova.kmmkspcases

import com.azharkova.core.GenUseCase
import com.azharkova.core.RequestType
import com.azharkova.core.UseCase

@GenUseCase(repo = NewsApi::class, request = "loadNews")
class NewsLoadCase

@UseCase(repo = NewsApi::class, request = RequestType.NEWS)
interface SimpleNewsLoadCase


