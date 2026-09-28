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

    // True only while the system prompt is showing. It stops the app relocking itself
    // when the device credential screen briefly sends the activity to the background.
    private var promptInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FlareEnoughTheme {
                Gate()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!promptInProgress) {
            authenticated = false
        }
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
        if (promptInProgress) return
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    promptInProgress = false
                    authenticated = true
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Left locked. The lock screen keeps its own Unlock button to retry.
                    promptInProgress = false
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_lock_prompt_title, getString(R.string.app_name)))
            .setSubtitle(getString(R.string.app_lock_prompt_subtitle))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
            .build()
        promptInProgress = true
        prompt.authenticate(info)
    }
}
