package club.hiraeth.flareenough

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import club.hiraeth.flareenough.ui.FlareApp
import club.hiraeth.flareenough.ui.lock.LockScreen
import club.hiraeth.flareenough.ui.lock.deviceCanAuthenticate
import club.hiraeth.flareenough.ui.theme.FlareEnoughTheme

/**
 * The single Activity. It hosts all Compose UI. There are no other activities.
 *
 * It is a FragmentActivity so it can show the system unlock prompt for the optional
 * app lock. When app lock is on and the device has an unlock set, the app shows a lock
 * screen until the person authenticates, and locks again when it leaves the foreground.
 */
class MainActivity : FragmentActivity() {

    private val container get() = (application as FlareApp).container

    // Whether the person has unlocked the app this time it is in the foreground.
    private var authenticated by mutableStateOf(false)

    // Built once and reused. Creating a fresh prompt per tap left a stale one behind
    // and could leave the Unlock button doing nothing.
    private lateinit var biometricPrompt: BiometricPrompt

    private val promptInfo: BiometricPrompt.PromptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_lock_prompt_title, getString(R.string.app_name)))
            .setSubtitle(getString(R.string.app_lock_prompt_subtitle))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authenticated = true
                }
                // Errors (including the person cancelling) leave the app locked. The
                // lock screen keeps its Unlock button so they can try again.
            },
        )

        setContent {
            FlareEnoughTheme {
                Gate()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Re-lock when the app leaves the foreground. While it is still locked this is
        // a no op, so it only takes effect once the app has actually been opened.
        authenticated = false
    }

    @Composable
    private fun Gate() {
        // Null until the stored setting is read, so the app never flashes its content
        // for an instant before locking.
        val lockEnabled by produceState<Boolean?>(initialValue = null) {
            container.settingsRepository.observeAppLockEnabled().collect { value = it }
        }

        val mustLock = lockEnabled == true && deviceCanAuthenticate(this) && !authenticated

        when {
            lockEnabled == null -> Unit
            mustLock -> {
                LaunchedEffect(Unit) { promptUnlock() }
                LockScreen(onUnlock = { promptUnlock() })
            }
            else -> FlareApp()
        }
    }

    private fun promptUnlock() {
        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (t: Throwable) {
            // If the prompt cannot be shown, stay on the lock screen with its button.
        }
    }
}
