package com.srizwan.islamipedia

import com.google.ai.client.generativeai.GenerativeModel

object GeminiHelper {

    // Reflection ব্যবহার করে GenerativeModel তৈরি করার ফাংশন
    fun createModel(apiKey: String): GenerativeModel {
        return try {
            // Reflection ব্যবহার করে 'GenerativeModel' কনস্ট্রাক্টর অ্যাক্সেস করা
            val constructor = GenerativeModel::class.java.getDeclaredConstructor(
                String::class.java, String::class.java
            )
            constructor.isAccessible = true // Private constructor কে অ্যাক্সেস করার অনুমতি দেওয়া
            constructor.newInstance("gemini-2.0-flash", apiKey)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to create GenerativeModel")
        }
    }
}
