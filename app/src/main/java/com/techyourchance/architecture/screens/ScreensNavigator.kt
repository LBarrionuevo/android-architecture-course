package com.techyourchance.architecture.screens

import android.os.Bundle
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ScreensNavigator {

    private val scoope = CoroutineScope(Dispatchers.Main.immediate)
    private lateinit var parentNavController : NavHostController
    private lateinit var nestedNavController : NavHostController
    private var nestedNavControllerObserverJob : Job? = null

    val currentBottomTab = MutableStateFlow<BottomTab?>(null) //BottomTab.Main
    val currentRoute =  MutableStateFlow<Route?>(null) //Route.QuestionsListScreen
    val isRootRoute = MutableStateFlow(false)

    val argument = MutableStateFlow<Bundle?>(null)

    fun navigateBack() {
        if (!nestedNavController.popBackStack()) {
            parentNavController.popBackStack()
        }
    }

    fun toTab(bottomTab: BottomTab) {
        val route = when(bottomTab) {
            BottomTab.Main -> Route.MainTab
            BottomTab.Favorites -> Route.FavoritesTab

        }
        parentNavController.navigate(route.routeName) {
            parentNavController.graph.startDestinationRoute?.let { startRoute ->
                popUpTo(startRoute) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun setParentNavController(navController: NavHostController) {
        parentNavController = navController
        parentNavController.currentBackStackEntryFlow.map { backStackEntry ->
            val bottomTab = when(val routeName = backStackEntry.destination.route){
                Route.MainTab.routeName -> Route.MainTab
                Route.FavoritesTab.routeName -> Route.FavoritesTab
                null -> null
                else -> throw RuntimeException("unsupported bottom tab: $routeName")
            }
        }
    }

    fun setNestedNavController(navController: NavHostController) {
        nestedNavController = navController

        nestedNavControllerObserverJob?.cancel()
        nestedNavControllerObserverJob  = scoope.launch {


            nestedNavController.currentBackStackEntryFlow.map { backStackEntry ->
                val route = when (val routeName = backStackEntry.destination.route) {
                    Route.MainTab.routeName -> Route.MainTab
                    Route.FavoritesTab.routeName -> Route.FavoritesTab
                    Route.QuestionsListScreen.routeName -> Route.QuestionsListScreen
                    Route.QuestionDetailsScreen.routeName -> Route.QuestionDetailsScreen
                    Route.FavoriteQuestionsScreen.routeName -> Route.FavoriteQuestionsScreen
                    null -> null
                    else -> throw RuntimeException("unsupported route: $routeName")
                }
                currentRoute.value = route
                isRootRoute.value = route == Route.QuestionsListScreen
                argument.value = backStackEntry.arguments
            }.collect()
        }
    }

    companion object{
        val BOTTOM_TABS = listOf(BottomTab.Main, BottomTab.Favorites)
    }

}
