package com.techyourchance.architecture.question

import com.techyourchance.architecture.BuildConfig
import com.techyourchance.architecture.common.networking.StackoverflowApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class FetchQuestionsListUseCase {
    private val retrofit by lazy {
        val httpClient = OkHttpClient.Builder().run {
            addInterceptor(HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            })
            build()
        }

        Retrofit.Builder()
            .baseUrl("http://api.stackexchange.com/2.3/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(httpClient)
            .build()
    }

    private val stackoverflowApi by lazy {
        retrofit.create(StackoverflowApi::class.java)
    }



    var questions : List<QuestionSchema> = emptyList()

    private var lastNetworkRequestNano = 0L

    suspend fun fetchLastActiveQuestions():List<QuestionSchema> {
        return withContext(Dispatchers.IO){
             if(hasEnoughTimePassed()) {
                lastNetworkRequestNano = System.nanoTime()
                questions = stackoverflowApi.fetchLastActiveQuestions(20)!!.questions
                questions
            }else{
                questions
            }
        }

    }

    private fun hasEnoughTimePassed(): Boolean {
        return System.nanoTime() - lastNetworkRequestNano > THROTTLE_TIMEOUT_MS
    }

    companion object{
        const val  THROTTLE_TIMEOUT_MS = 5000L
    }
}