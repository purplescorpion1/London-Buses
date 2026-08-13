package com.example.londonbuses.data

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class CredentialManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("london_buses_prefs", Context.MODE_PRIVATE)

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString("app_key", apiKey.trim()).apply()
    }

    fun getApiKey(): String {
        return prefs.getString("app_key", "") ?: ""
    }
}

class TflApiKeyInterceptor(private val credentialManager: CredentialManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val apiKey = credentialManager.getApiKey()

        if (apiKey.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Append app_key as a query parameter
        val urlWithKey = originalRequest.url.newBuilder()
            .addQueryParameter("app_key", apiKey)
            .build()

        val requestWithKey = originalRequest.newBuilder()
            .url(urlWithKey)
            .build()

        return chain.proceed(requestWithKey)
    }
}
