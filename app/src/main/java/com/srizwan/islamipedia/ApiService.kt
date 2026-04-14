import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2,    // Muslim World League method
        @Query("school") school: Int = 1     // Hanafi madhab
    ): Response<PrayerTimesResponse>
}
