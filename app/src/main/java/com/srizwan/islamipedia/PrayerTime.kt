package com.srizwan.islamipedia

data class PrayerTimesResponse(
    val data: List<PrayerTime>
)

data class PrayerTime(
    val date: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)
