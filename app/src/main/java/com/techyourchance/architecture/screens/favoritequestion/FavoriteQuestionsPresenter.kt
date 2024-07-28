package com.techyourchance.architecture.screens.favoritequestion

import com.techyourchance.architecture.common.database.FavoriteQuestionDao

class FavoriteQuestionsPresenter(favoriteQuestionDao: FavoriteQuestionDao) {
    val favoriteQuestions = favoriteQuestionDao.observe()



//    private val scope = CoroutineScope(Dispatchers.Main.immediate)
//    val favoriteQuestions = MutableStateFlow<List<FavoriteQuestion>>(listOf())
//    fun observeFavoriteQuestions() {
//        scope.launch {
//            favoriteQuestions.value = favoriteQuestionDao.observe().first()
//        }
//
//    }
}