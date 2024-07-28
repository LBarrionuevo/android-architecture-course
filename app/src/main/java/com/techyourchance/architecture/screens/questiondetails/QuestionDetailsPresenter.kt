package com.techyourchance.architecture.screens.questiondetails

import com.techyourchance.architecture.common.database.FavoriteQuestionDao
import com.techyourchance.architecture.common.networking.StackoverflowApi
import com.techyourchance.architecture.question.QuestionWithBodySchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestionDetailsPresenter(
    private val stackoverflowApi: StackoverflowApi,
    private val favoriteQuestionDao: FavoriteQuestionDao,
    ) {

    sealed class QuestionDetailResult{
        data object None: QuestionDetailResult()
        data class Success(val questionDetails: QuestionWithBodySchema, val isFavorite:Boolean):QuestionDetailResult()
        data object Error: QuestionDetailResult()
    }

    val questionDetails = MutableStateFlow<QuestionDetailResult>(QuestionDetailResult.None)

    suspend fun fetchQuestionDetails(questionId: String){
        withContext(Dispatchers.Main.immediate) {
            combine(
                flow = flow {
                    emit(stackoverflowApi.fetchQuestionDetails(questionId))
                },
                flow2 = favoriteQuestionDao.observeById(questionId),
            ) { questionDetails, favoriteQuestion ->
                if (questionDetails != null
                    && questionDetails.questions.isNotEmpty()
                ) {
                    QuestionDetailResult.Success(
                        questionDetails.questions[0],
                        favoriteQuestion != null
                    )
                } else {
                    QuestionDetailResult.Error
                }
            }.catch {
                QuestionDetailResult.Error
            }.collect {
                questionDetails.value = it
            }
        }

    }
}