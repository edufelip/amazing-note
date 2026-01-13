package com.edufelip.shared.data.network

import io.ktor.client.HttpClient

expect fun provideHttpClient(): HttpClient
