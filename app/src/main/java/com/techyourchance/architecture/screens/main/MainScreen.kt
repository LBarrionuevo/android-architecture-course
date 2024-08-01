package com.techyourchance.architecture.screens.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.techyourchance.architecture.common.database.FavoriteQuestionDao
import com.techyourchance.architecture.common.networking.StackoverflowApi
import com.techyourchance.architecture.screens.Route
import com.techyourchance.architecture.screens.ScreensNavigator
import com.techyourchance.architecture.screens.favoritequestion.FavoriteQuestionsPresenter
import com.techyourchance.architecture.screens.favoritequestion.FavoriteQuestionsScreen
import com.techyourchance.architecture.screens.questiondetails.QuestionDetailsViewModel
import com.techyourchance.architecture.screens.questiondetails.QuestionDetailsScreen
import com.techyourchance.architecture.screens.questionlist.QuestionsListScreen
import kotlinx.coroutines.flow.map

@Composable
fun MainScreen(
    stackoverflowApi: StackoverflowApi,
    favoriteQuestionDao: FavoriteQuestionDao,
) {

    val screenNavigator = remember {
        ScreensNavigator()
    }

    val currentBottomTab = screenNavigator.currentBottomTab.collectAsState()
    val currentRoute = screenNavigator.currentRoute.collectAsState()
    val isRootRoute = screenNavigator.isRootRoute.collectAsState()

    val isShowFavoriteButton = screenNavigator.currentRoute.map { route ->
        route is Route.QuestionDetailsScreen
    }.collectAsState(initial = false)



    val questionIdAndTitle = remember(currentRoute.value) {
        if (currentRoute.value is Route.QuestionDetailsScreen) {
            Pair(
                (currentRoute.value as Route.QuestionDetailsScreen).questionId,
                (currentRoute.value as Route.QuestionDetailsScreen).questionTitle,
            )
        } else {
            Pair("", "")
        }
    }

    var isFavoriteQuestion by remember { mutableStateOf(false) }

    if (isShowFavoriteButton.value && questionIdAndTitle.first.isNotEmpty()) {
        // Since collectAsState can't be conditionally called, use LaunchedEffect for conditional logic
        LaunchedEffect(questionIdAndTitle) {
            favoriteQuestionDao.observeById(questionIdAndTitle.first).collect { favoriteQuestion ->
                isFavoriteQuestion = favoriteQuestion != null
            }
        }
    }

    Scaffold(
        topBar = {
            MyTopAppBar(
                favoriteQuestionDao = favoriteQuestionDao,
                isRootRoute = isRootRoute.value,
                isShowFavoriteButton = isShowFavoriteButton.value,
                isFavoriteQuestion = isFavoriteQuestion,
                questionIdAndTitle = questionIdAndTitle,
                onBackClicked = {
                    screenNavigator.navigateBack()
                }
            )
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier) {
                MyBottomTabsBar(
                    bottomTabs = ScreensNavigator.BOTTOM_TABS,
                    currentBottomTab = currentBottomTab.value,
                    onTabClicked = { bottomTab ->
                        screenNavigator.toTab(bottomTab)
                    }
                )
            }
        },
        content = { padding ->
            MainScreenContent(
                padding = padding,
                screenNavigator = screenNavigator,
                stackoverflowApi = stackoverflowApi,
                favoriteQuestionDao = favoriteQuestionDao,
            )
        }
    )
}
@Composable
private fun MainScreenContent(
    padding: PaddingValues,
    screenNavigator: ScreensNavigator,
    stackoverflowApi: StackoverflowApi,
    favoriteQuestionDao: FavoriteQuestionDao,
) {
    val parentNavController = rememberNavController()
    screenNavigator.setParentNavController(parentNavController)

    val viewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(QuestionDetailsViewModel::class.java)){
                return QuestionDetailsViewModel(stackoverflowApi, favoriteQuestionDao) as T
            }
            return super.create(modelClass)
        }
    }

    Surface(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 12.dp),
    ) {

        val favoritePresenter = remember {
            FavoriteQuestionsPresenter(favoriteQuestionDao)
        }
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = parentNavController,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            startDestination = Route.MainTab.routeName,
        ) {
            composable(route = Route.MainTab.routeName) {
                val mainNestedNavController = rememberNavController()
                screenNavigator.setNestedNavController(mainNestedNavController)

                NavHost(navController = mainNestedNavController, startDestination = Route.QuestionsListScreen.routeName) {
                    composable(route = Route.QuestionsListScreen.routeName) {
                        QuestionsListScreen(
                            onQuestionClicked = { clickedQuestionId, clickedQuestionTitle ->
                                screenNavigator.toRoute(Route.QuestionDetailsScreen(
                                    questionId = clickedQuestionId,
                                    questionTitle = clickedQuestionTitle
                                ))
                            },
                        )
                    }
                    composable(route = Route.QuestionDetailsScreen().routeName) { backStackEntry ->
                        val questionId = remember {
                            (screenNavigator.currentRoute.value as Route.QuestionDetailsScreen).questionId
                        }
                        QuestionDetailsScreen(
                            viewModelFactory = viewModelFactory,
                            questionId = questionId,
                            onError = {
                                mainNestedNavController.popBackStack()
                            }
                        )
                    }
                }

            }

            composable(route = Route.FavoritesTab.routeName) {
                val favoriteNestedNavController = rememberNavController()
                screenNavigator.setNestedNavController(favoriteNestedNavController)

                NavHost(navController = favoriteNestedNavController, startDestination = Route.FavoriteQuestionsScreen.routeName) {
                    composable(route = Route.FavoriteQuestionsScreen.routeName) {
                        FavoriteQuestionsScreen(
                            favoritePresenter = favoritePresenter,
                            onQuestionClicked = {favoriteQuestionId, favoriteQuestionTitle ->
                                screenNavigator.toRoute(Route.QuestionDetailsScreen(
                                    questionId = favoriteQuestionId,
                                    questionTitle = favoriteQuestionTitle
                                ))
                            }
                        )
                    }
                    composable(route = Route.QuestionDetailsScreen().routeName) { backStackEntry ->
                        val questionId = remember {
                            (screenNavigator.currentRoute.value as Route.QuestionDetailsScreen).questionId
                        }
                        QuestionDetailsScreen(
                            viewModelFactory = viewModelFactory,
                            questionId = questionId,
                            onError = {
                                screenNavigator.navigateBack()
                            }
                        )
                    }
                }
            }
        }
    }
}
