import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.dakil.krotapp.ui.screen.ListScreen
import pl.dakil.krotapp.ui.screen.SearchScreen
import pl.dakil.krotapp.viewmodel.ItemsViewModel

@Composable
fun AppNavGraph(itemsViewModel: ItemsViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(itemsViewModel, onNavigateToList = {
                navController.navigate("list")
            })
        }
        composable("list") {
            ListScreen(itemsViewModel, onNavigateBack = {
                navController.popBackStack()
            })
        }
    }
}
