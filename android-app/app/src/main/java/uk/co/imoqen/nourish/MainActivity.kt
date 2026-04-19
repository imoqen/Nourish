package uk.co.imoqen.nourish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import dagger.hilt.android.AndroidEntryPoint
import uk.co.imoqen.nourish.authentication.AuthenticationViewModel
import uk.co.imoqen.nourish.authentication.LoginScreen
import uk.co.imoqen.nourish.common.uievent.UiEvent
import uk.co.imoqen.nourish.common.uievent.UiEventBus
import uk.co.imoqen.nourish.navigation.AppNavHost
import uk.co.imoqen.nourish.navigation.NourishNavigationBar
import uk.co.imoqen.nourish.navigation.TabDestination
import uk.co.imoqen.nourish.ui.theme.NourishTheme
import javax.inject.Inject

/**
 * The entry point of the Nourish app.
 * Hilt is used to wrap the component activity, creating a dependency container to inject ViewModels
 * and other dependencies e.g. managers and repositories.
 *
 * @author Imogen Pemberton, 2026
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Dependencies for the main activity component:
    // https://developer.android.com/training/dependency-injection/hilt-android
    @Inject lateinit var uiEventBus: UiEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise Firebase and App Check:
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        enableEdgeToEdge()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            // Collect and perform UiEvents across entire app:
            suspend fun onEvent(event: UiEvent) {
                when (event) {
                    is UiEvent.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            // Handle UiEvents: (this is for sending Toasts currently)
            LaunchedEffect(Unit) {
                uiEventBus.uiEvents.collect { event ->
                    onEvent(event)
                }
            }

            // The colour scheme theme:
            NourishTheme {
                MainContent(Modifier, snackbarHostState)
            }
        }
    }
}

/**
 * Main content of the app, different depending on if logged in or not logged in.
 * When logged in, NavHost is used to allow for navigation around the app. Otherwise, the user can
 * only access login/signup views.
 * @param modifier - the modifier
 * @param snackbarHostState - the state for the Snackbar, to enqueue and display alerts to the user.
 */
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    // Navigation when logged in always begins on the recipe discovery view:
    val startDestination = TabDestination.RECIPE_FINDER

    // Authentication ViewModel and App ViewModel are used across the whole app therefore instantiated
    val authenticationViewModel: AuthenticationViewModel = hiltViewModel()
    val appViewModel: AppViewModel = hiltViewModel()
    val authenticationState by appViewModel.authenticationState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    AnimatedContent(
        targetState = authenticationState.loggedIn,
        label = "auth"
    ) { loggedIn ->
        if (!loggedIn) {
            // User is not logged in: display login/signup view
            Scaffold(
                modifier = modifier,
                // Display all potential Snackbar messages:
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                }
            ) { contentPadding ->
                LoginScreen(modifier = Modifier.padding(contentPadding), authenticationViewModel)
            }
        } else {
            // User is logged in: display app.
            // Uses key to ensure different ViewModels are instantiated for each user,
            // for clean state between logins.
            key(authenticationState.uid) {
                // new nav controller for each logged in user
                val navController = rememberNavController()

                // The currently selected tab value to display (dynamically determined based on the route
                // first char of the string x/route), required as RouteDestinations are within tabs.
                // So must determine which tab to display for each route.
                val selectedDestinationTab =
                    navController.currentBackStackEntryAsState()
                        .value?.destination?.route?.firstOrNull()
                        ?: startDestination.route[0]

                Scaffold(
                    modifier = modifier,
                    // Display all potential Snackbar messages:
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    bottomBar = {
                        NourishNavigationBar(
                            TabDestination.entries,
                            selectedDestinationTab,
                            navController
                        )
                    },
                    topBar = {
                        NourishTopBar()
                    }
                ) { contentPadding ->

                    // Navigation around the app is controlled by the app navigation host
                    AppNavHost(
                        navController,
                        startDestination.route,
                        modifier = Modifier.padding(contentPadding)
                    )
                }
            }
        }
    }
    }
}

/**
 * The top bar of the Nourish app, containing padding and the Nourish text logo.
 */
@Composable
fun NourishTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom=4.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.nourish_title_outlined),
            contentDescription = "Nourish Title",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(28.dp) // fixed height as it is the logo
        )
    }
}