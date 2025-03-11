package com.techyourchance.architecture.screens.questionlist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.techyourchance.architecture.BuildConfig
import com.techyourchance.architecture.common.networking.StackoverflowApi
import com.techyourchance.architecture.question.FetchQuestionsListUseCase
import com.techyourchance.architecture.question.QuestionSchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class QuestionsListViewModel:ViewModel() {

    private val fetchQuestionsListUseCase = FetchQuestionsListUseCase()

    val lastActiveQuestions = MutableStateFlow<List<QuestionSchema>>(listOf())

    suspend fun fetchLastActiveQuestions(forceUpdate: Boolean = false) {
        withContext(Dispatchers.Main.immediate){
            if(forceUpdate || lastActiveQuestions.value.isEmpty()) {
                lastActiveQuestions.value =
                    fetchQuestionsListUseCase.fetchLastActiveQuestions()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("QuestionsListViewModel", "onCleared()")
    }
}