package com.techyourchance.architecture.screens.favoritequestion

import android.util.Log
import androidx.lifecycle.ViewModel
import com.techyourchance.architecture.common.database.FavoriteQuestionDao

class FavoriteQuestionsViewModel(favoriteQuestionDao: FavoriteQuestionDao):ViewModel() {
    val favoriteQuestions = favoriteQuestionDao.observe()



//    private val scope = CoroutineScope(Dispatchers.Main.immediate)
//    val favoriteQuestions = MutableStateFlow<List<FavoriteQuestion>>(listOf())
//    fun observeFavoriteQuestions() {
//        scope.launch {
//            favoriteQuestions.value = favoriteQuestionDao.observe().first()
//        }
//
//    }

    override fun onCleared() {
        super.onCleared()
        Log.i("FavoriteQuestionsViewModel", "onCleared()")
    }
}