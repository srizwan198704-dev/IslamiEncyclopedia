data class PrayerTimesResponse(
    val data: Data
)

data class Data(
    val timings: Timings,
    val date: DateData
)

data class Timings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

data class DateData(
    val gregorian: GregorianDate,
    val hijri: HijriDate
)

data class GregorianDate(
    val date: String,
    val month: Month,
    val year: String
)

data class HijriDate(
    val date: String,
    val month: Month,
    val year: String
)

data class Month(
    val en: String,   // ইংরেজি মাস
    val bn: String    // বাংলা মাস (আপনার API যদি বাংলা মাস সমর্থন করে)
)
