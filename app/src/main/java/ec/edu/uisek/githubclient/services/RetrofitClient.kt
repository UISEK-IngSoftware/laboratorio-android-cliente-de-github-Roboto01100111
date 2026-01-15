package ec.edu.uisek.githubclient.services

import android.util.Log
import ec.edu.uisek.githubclient.BuildConfig
import ec.edu.uisek.githubclient.interceptors.BasicAuthinterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto singleton que configura y proporciona la instancia de Retrofit
 * para conectarse a la API de GitHub
 */
object RetrofitClient {

    // URL base de la API de GitHub
    private const val BASE_URL = "https://api.github.com/"
    private var apiService: GitHubApiService? = null

    fun createAuthenticatedClient(user: String, pass: String): GitHubApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(BasicAuthinterceptor(user, pass))
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Serializar y deserializar importante
            .build()

        apiService = retrofit.create(GitHubApiService::class.java)
        return apiService!! // las 2 exclamaciones es para que no devuelva null
    }

    fun getApiService(): GitHubApiService {
        return apiService ?: throw IllegalStateException("ApiService no pudo ser inicializado")
    }
}