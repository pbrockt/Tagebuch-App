package com.pbrockt.tagebuch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pbrockt.tagebuch.navigation.AppNavigation
import com.pbrockt.tagebuch.ui.auth.AuthScreen
import com.pbrockt.tagebuch.ui.theme.TagebuchTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Die einzige Activity der App — der "Rahmen" für alle Bildschirme.
 *
 * In moderner Android-Entwicklung (Single Activity Architecture) gibt es
 * nur eine Activity. Alle "Seiten" der App sind Compose-Composables, die
 * über Navigation gewechselt werden — kein Laden neuer Activities nötig.
 *
 * @AndroidEntryPoint: Hilt kann jetzt Abhängigkeiten in diese Activity injizieren.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Von Hilt injiziert — verwaltet ob die App gesperrt ist */
    @Inject lateinit var appLockManager: AppLockManager

    /**
     * viewModels() ist eine Kotlin-Extension die ein ViewModel erstellt
     * und an den Lifecycle der Activity bindet. Das ViewModel überlebt
     * Bildschirmrotationen, die Activity selbst nicht.
     */
    private val mainViewModel: MainViewModel by viewModels()

    /**
     * onStop() wird aufgerufen wenn die Activity komplett verdeckt ist —
     * z.B. beim App-Wechsel oder wenn eine andere App in den Vordergrund tritt.
     *
     * Hier sperren wir die App damit beim nächsten Öffnen der PIN erscheint.
     * onStop() ist zuverlässiger als onPause() weil es nicht bei jedem
     * System-Dialog (Benachrichtigungen etc.) feuert.
     */
    override fun onStop() {
        super.onStop()
        appLockManager.lockApp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-Edge: App nutzt den gesamten Bildschirm inkl. Statusleiste
        enableEdgeToEdge()

        /**
         * setContent startet die Compose-UI.
         * Alles innerhalb ist "reaktiv" — wenn sich ein State ändert,
         * wird nur der betroffene Teil der UI neu gezeichnet (Recomposition).
         */
        setContent {
            // collectAsState() abonniert den StateFlow und löst bei Änderung
            // automatisch eine Recomposition aus
            val themeChoice by mainViewModel.themeChoice.collectAsState()
            val accentColor by mainViewModel.accentColor.collectAsState()
            val fontChoice by mainViewModel.fontChoice.collectAsState()
            val isLocked by appLockManager.isLocked.collectAsState()

            // Das Theme wrappt die gesamte App mit Farben und Schriften
            TagebuchTheme(
                themeChoice = themeChoice,
                accentColor = accentColor,
                fontChoice = fontChoice
            ) {
                // Wenn gesperrt: PIN-Screen zeigen. Sonst: normale App
                if (isLocked) {
                    AuthScreen(onAuthenticated = { appLockManager.unlockApp() })
                } else {
                    AppNavigation(onThemeChanged = { mainViewModel.refresh() })
                }
            }
        }
    }
}
