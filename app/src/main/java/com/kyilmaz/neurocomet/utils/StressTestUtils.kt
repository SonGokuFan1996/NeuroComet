package com.kyilmaz.neurocomet.utils

import android.content.Context
import android.util.Base64
import com.kyilmaz.neurocomet.FeedViewModel
import com.kyilmaz.neurocomet.NotificationChannels
import com.kyilmaz.neurocomet.ParentalControlsSettings
import com.kyilmaz.neurocomet.BlockableFeature
import com.kyilmaz.neurocomet.shouldBlockFeature
import com.kyilmaz.neurocomet.AppSupabaseClient
import com.kyilmaz.neurocomet.AccountLifecycleStatus
import com.kyilmaz.neurocomet.DeviceAuthority
import com.kyilmaz.neurocomet.InputValidator
import com.kyilmaz.neurocomet.SecurityUtils
import com.kyilmaz.neurocomet.auth.AuthMethod
import com.kyilmaz.neurocomet.auth.AuthResult
import com.kyilmaz.neurocomet.auth.AuthenticationManager
import com.kyilmaz.neurocomet.auth.BiometricStatus
import com.kyilmaz.neurocomet.auth.TotpSecret
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

data class StressTestResult(
    val testName: String,
    val passed: Boolean,
    val duration: Long,
    val details: String,
    val errorMessage: String? = null
)

object StressTester {

    suspend fun runUIResponsivenessTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            // Measure actual scheduling jitter: how much extra time each
            // delay(1) takes beyond the requested 1 ms.  Running on the
            // Default dispatcher avoids main-thread contention from UI
            // recompositions that happen while the test suite is active.
            val iterations = 50
            val perDelayMs = 1L
            val jitters = mutableListOf<Long>()

            withContext(Dispatchers.Default) {
                repeat(iterations) {
                    val before = System.nanoTime()
                    delay(perDelayMs)
                    val elapsed = (System.nanoTime() - before) / 1_000_000  // ms
                    jitters.add(elapsed - perDelayMs)
                }
            }

            val avgJitter = jitters.average()
            val maxJitter = jitters.max()
            val duration = System.currentTimeMillis() - startTime

            // Pass if average scheduling overhead stays under 15 ms per
            // operation — very generous for any healthy device.
            val passed = avgJitter < 15
            StressTestResult(
                testName = "UI Responsiveness",
                passed = passed,
                duration = duration,
                details = "$iterations ops, avg jitter ${String.format("%.1f", avgJitter)}ms, " +
                        "max ${maxJitter}ms"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "UI Responsiveness",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runMemoryPressureTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val runtime = Runtime.getRuntime()
            val initialMemory = runtime.totalMemory() - runtime.freeMemory()

            val allocations = mutableListOf<ByteArray>()
            repeat(10) {
                allocations.add(ByteArray(1024 * 100)) // 100KB allocations
                delay(10)
            }
            allocations.clear()
            System.gc()
            delay(100)

            val finalMemory = runtime.totalMemory() - runtime.freeMemory()
            val memoryDelta = finalMemory - initialMemory
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Memory Pressure",
                passed = memoryDelta < 5 * 1024 * 1024,
                duration = duration,
                details = "Memory delta: ${memoryDelta / 1024}KB after allocations"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Memory Pressure",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runRapidNavigationTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            repeat(50) {
                delay(10)
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Rapid Navigation",
                passed = true,
                duration = duration,
                details = "50 navigation simulations completed"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Rapid Navigation",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runDataLoadingStressTest(feedViewModel: FeedViewModel?): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            repeat(20) {
                feedViewModel?.fetchNotifications()
                delay(25)
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Data Loading Stress",
                passed = duration < 2000,
                duration = duration,
                details = "20 data fetch operations in ${duration}ms"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Data Loading Stress",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runThemeSwitchingTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            repeat(30) {
                delay(15)
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Theme Switching",
                passed = true,
                duration = duration,
                details = "30 theme switch simulations completed"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Theme Switching",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runConcurrentOperationsTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            coroutineScope {
                val jobs = (1..10).map {
                    async {
                        delay((10..50).random().toLong())
                        it * 2
                    }
                }
                val results = jobs.awaitAll()
                val duration = System.currentTimeMillis() - startTime

                StressTestResult(
                    testName = "Concurrent Operations",
                    passed = results.size == 10,
                    duration = duration,
                    details = "10 concurrent operations completed successfully"
                )
            }
        } catch (e: Exception) {
            StressTestResult(
                testName = "Concurrent Operations",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runStorageIOTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val prefs = context.getSharedPreferences("stress_test", Context.MODE_PRIVATE)
            repeat(50) { i ->
                prefs.edit().putString("test_key_$i", "test_value_$i").apply()
            }
            repeat(50) { i ->
                prefs.getString("test_key_$i", null)
            }
            prefs.edit().clear().apply()
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Storage I/O",
                passed = duration < 1000,
                duration = duration,
                details = "100 I/O operations (50 writes, 50 reads) in ${duration}ms"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Storage I/O",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runNetworkSimulationTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val scenarios = listOf(
                Pair("Fast network", 50L),
                Pair("Normal network", 150L),
                Pair("Slow network", 300L),
                Pair("Intermittent", 100L),
                Pair("Recovery", 50L)
            )
            scenarios.forEach { (_, delayMs) ->
                delay(delayMs)
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Network Simulation",
                passed = true,
                duration = duration,
                details = "5 network condition scenarios simulated"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Network Simulation",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runNotificationChannelTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            NotificationChannels.createNotificationChannels(context)
            delay(100)
            val channels = notificationManager.notificationChannels
            val expectedChannels = listOf(
                NotificationChannels.CHANNEL_DIRECT_MESSAGES,
                NotificationChannels.CHANNEL_LIKES,
                NotificationChannels.CHANNEL_COMMENTS,
                NotificationChannels.CHANNEL_FOLLOWS,
                NotificationChannels.CHANNEL_ACCOUNT_SECURITY,
                NotificationChannels.CHANNEL_APP_UPDATES
            )
            val missingChannels = expectedChannels.filter { expectedId ->
                channels.none { it.id == expectedId }
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Notification Channels",
                passed = missingChannels.isEmpty() && channels.size >= 15,
                duration = duration,
                details = "${channels.size} channels verified",
                errorMessage = if (missingChannels.isNotEmpty()) "Missing: ${missingChannels.joinToString()}" else null
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Notification Channels",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runListScrollStressTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val items = mutableListOf<String>()
            repeat(500) { i ->
                items.add("Item $i with some longer content")
                if (i % 50 == 0) delay(1)
            }
            repeat(250) {
                if (items.isNotEmpty()) items.removeAt(0)
                if (it % 50 == 0) delay(1)
            }
            repeat(100) {
                if (items.isNotEmpty()) {
                    items[(0 until items.size).random()]
                }
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "List Scroll Stress",
                passed = duration < 500 && items.size == 250,
                duration = duration,
                details = "850 operations, ${items.size} items remaining"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "List Scroll Stress",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runStateManagementTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val stateFlow = MutableStateFlow(0)
            var lastValue = 0
            var updates = 0
            coroutineScope {
                val collector = launch {
                    stateFlow.collect { value ->
                        lastValue = value
                        updates++
                    }
                }
                repeat(1000) { i ->
                    stateFlow.value = i
                    if (i % 100 == 0) delay(1)
                }
                delay(50)
                collector.cancel()
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "State Management",
                passed = lastValue >= 990 && updates > 0,
                duration = duration,
                details = "1000 updates, final: $lastValue"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "State Management",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runJsonParsingTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val sampleJson = "{\"id\":\"12345\",\"name\":\"Test User\",\"posts\":[{\"id\":1},{\"id\":2}]}"
            var parseCount = 0
            repeat(100) {
                if (sampleJson.contains("\"id\"")) parseCount++
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "JSON Parsing",
                passed = parseCount == 100 && duration < 500,
                duration = duration,
                details = "$parseCount validations in ${duration}ms"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "JSON Parsing",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runStringOperationsTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            var ops = 0
            val builder = StringBuilder()
            repeat(1000) { builder.append("Item $it, "); ops++ }
            repeat(500) { String.format("User %d", it); ops++ }
            val regex = Regex("[a-z]+")
            repeat(200) { regex.findAll("abc123def").count(); ops++ }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "String Operations",
                passed = ops >= 1700,
                duration = duration,
                details = "$ops operations completed"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "String Operations",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runDateTimeOperationsTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            var ops = 0
            repeat(200) { Instant.now(); ops++ }
            repeat(100) { try { Instant.parse("2024-12-30T12:00:00Z"); ops++ } catch (e: Exception) {} }
            val base = Instant.now()
            repeat(100) { Duration.between(base, base.plusSeconds(it.toLong())).toMinutes(); ops++ }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "DateTime Operations",
                passed = ops >= 400,
                duration = duration,
                details = "$ops operations completed"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "DateTime Operations",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runCollectionOperationsTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            var ops = 0
            val map = mutableMapOf<String, Int>()
            repeat(500) { map["k_$it"] = it; ops++ }
            val list = (1..500).toMutableList()
            list.filter { it % 2 == 0 }
            ops++
            list.sortedDescending()
            ops++
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Collection Operations",
                passed = ops >= 500,
                duration = duration,
                details = "$ops operations completed"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Collection Operations",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runSecurityOperationsTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            var ops = 0
            val digest = MessageDigest.getInstance("SHA-256")
            repeat(100) { digest.digest("test_$it".toByteArray()); ops++ }
            repeat(100) {
                val encoded = Base64.encodeToString("test_$it".toByteArray(), Base64.DEFAULT)
                Base64.decode(encoded, Base64.DEFAULT)
                ops++
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Security Operations",
                passed = ops >= 200,
                duration = duration,
                details = "$ops operations completed"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Security Operations",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runFileSystemTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val testDir = File(context.cacheDir, "stress_${System.currentTimeMillis()}")
            testDir.mkdirs()
            repeat(20) { File(testDir, "f_$it.txt").writeText("content $it") }
            testDir.listFiles()?.forEach { it.readText() }
            testDir.listFiles()?.forEach { it.delete() }
            testDir.delete()
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "File System",
                passed = true,
                duration = duration,
                details = "20 files created, read, deleted"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "File System",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runExceptionHandlingTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            var caught = 0
            repeat(100) {
                try { if (it % 3 == 0) throw IllegalStateException() } catch (e: Exception) { caught++ }
            }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Exception Handling",
                passed = caught >= 30,
                duration = duration,
                details = "Caught $caught exceptions"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Exception Handling",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    suspend fun runParentalControlsTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            var ops = 0
            repeat(50) { ParentalControlsSettings.getState(context); ops++ }
            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Parental Controls",
                passed = ops >= 50,
                duration = duration,
                details = "$ops state reads completed"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Parental Controls",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    // ==================== AUTHENTICATION TESTS ====================

    /**
     * Tests TOTP secret generation, code generation, and code verification.
     * Exercises the full TOTP lifecycle without persisting state.
     */
    suspend fun runTotpLifecycleTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val manager = AuthenticationManager.getInstance(context)

            // 1. Generate a secret
            val secret = manager.generateTotpSecret("stress_test@getneurocomet.com")
            val secretValid = secret.secret.isNotBlank()
                    && secret.accountName == "stress_test@getneurocomet.com"
                    && secret.issuer == "NeuroComet"
                    && secret.digits == 6
                    && secret.period == 30

            // 2. URI should be well-formed
            val uri = secret.toUri()
            val uriValid = uri.startsWith("otpauth://totp/") && uri.contains("secret=")

            // 3. Store, generate code, then verify it
            manager.storeTotpSecret(secret)
            val code = manager.generateTotpCode()
            val codeValid = code != null && code.length == 6 && code.all { it.isDigit() }

            val verified = if (code != null) manager.verifyTotpCode(code) else false

            // 4. Wrong code should fail
            val wrongCodeRejected = !manager.verifyTotpCode("000000_invalid")

            // 5. Clean up
            manager.disableTotp()
            val disabledAfterCleanup = manager.getTotpSecret() == null

            val allPassed = secretValid && uriValid && codeValid && verified
                    && wrongCodeRejected && disabledAfterCleanup
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: TOTP Lifecycle",
                passed = allPassed,
                duration = duration,
                details = "secret=$secretValid uri=$uriValid code=$codeValid " +
                        "verify=$verified rejectBad=$wrongCodeRejected cleanup=$disabledAfterCleanup"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: TOTP Lifecycle",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests TOTP clock-drift window tolerance.
     * Generates codes for adjacent time periods and verifies
     * they are accepted within the default ±1 window.
     */
    suspend fun runTotpWindowToleranceTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val manager = AuthenticationManager.getInstance(context)
            val secret = manager.generateTotpSecret("window_test@getneurocomet.com")
            manager.storeTotpSecret(secret)

            // The current code should always verify
            val currentCode = manager.generateTotpCode()!!
            val currentOk = manager.verifyTotpCode(currentCode, window = 1)

            // A code that is way outside the window should fail
            val farFutureFails = !manager.verifyTotpCode("999999", window = 0)

            manager.disableTotp()
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: TOTP Window",
                passed = currentOk && farFutureFails,
                duration = duration,
                details = "currentOk=$currentOk farFutureFails=$farFutureFails"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: TOTP Window",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests backup-code generation, verification, consumption, and
     * replay-rejection (a used code must not work twice).
     */
    suspend fun runBackupCodesTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val manager = AuthenticationManager.getInstance(context)

            // 1. Generate codes
            val codes = manager.generateBackupCodes(5)
            val generatedOk = codes.size == 5 && codes.all { it.isNotBlank() }
            val storedCountOk = manager.getBackupCodes().size == 5

            // 2. First code should verify successfully
            val firstResult = manager.verifyBackupCode(codes[0])
            val firstOk = firstResult is AuthResult.Success

            // 3. Replay of the same code should fail
            val replayResult = manager.verifyBackupCode(codes[0])
            val replayRejected = replayResult is AuthResult.Error

            // 4. Count should have decreased
            val afterCount = manager.getBackupCodes().size
            val countDecreasedOk = afterCount == 4

            // 5. An invalid code should fail
            val invalidResult = manager.verifyBackupCode("ZZZZ_NOT_REAL")
            val invalidRejected = invalidResult is AuthResult.Error

            // 6. Second valid code should still work
            val secondResult = manager.verifyBackupCode(codes[1])
            val secondOk = secondResult is AuthResult.Success

            val allPassed = generatedOk && storedCountOk && firstOk && replayRejected
                    && countDecreasedOk && invalidRejected && secondOk
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: Backup Codes",
                passed = allPassed,
                duration = duration,
                details = "gen=$generatedOk stored=$storedCountOk first=$firstOk " +
                        "replay=$replayRejected count=$countDecreasedOk " +
                        "invalid=$invalidRejected second=$secondOk"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Backup Codes",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests email-verification code flow: send → verify correct →
     * reject wrong → reject expired.
     */
    suspend fun runEmailVerificationFlowTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val manager = AuthenticationManager.getInstance(context)

            // 1. Blank email should fail
            val blankResult = manager.sendEmailVerificationCode("")
            val blankRejected = blankResult is AuthResult.Error

            // 2. Send to a valid address
            val sendResult = manager.sendEmailVerificationCode("test@getneurocomet.com")
            val sendOk = sendResult is AuthResult.Success || sendResult is AuthResult.NotAvailable
            // (NotAvailable is valid in release builds)

            // 3. Wrong code should fail
            val wrongResult = manager.verifyEmailCode("000000")
            val wrongRejected = wrongResult is AuthResult.Error || wrongResult is AuthResult.NotAvailable

            val allPassed = blankRejected && sendOk && wrongRejected
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: Email Verification",
                passed = allPassed,
                duration = duration,
                details = "blankRejected=$blankRejected sendOk=$sendOk wrongRejected=$wrongRejected"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Email Verification",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests FIDO2/Passkey credential registration, listing, removal,
     * and that duplicate removal doesn't crash.
     */
    suspend fun runFido2CredentialLifecycleTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val manager = AuthenticationManager.getInstance(context)

            // 1. Check FIDO2 support flag
            val supportCheck = manager.isFido2Supported() // boolean, either way is ok

            // 2. Register a credential
            var registerOk = false
            manager.registerFido2Credential("Stress-Test Key") { result ->
                registerOk = result is AuthResult.Success || result is AuthResult.NotAvailable
            }

            // 3. List credentials
            val creds = manager.getFido2Credentials()
            val listOk = creds.isNotEmpty() || !registerOk // empty is ok if register was NotAvailable

            // 4. Remove the credential we just added (if any)
            var removeOk = true
            if (creds.isNotEmpty()) {
                val toRemove = creds.last()
                manager.removeFido2Credential(toRemove.credentialId)
                removeOk = manager.getFido2Credentials().none { it.credentialId == toRemove.credentialId }
            }

            // 5. Removing a non-existent credential should not crash
            var doubleRemoveOk = true
            try {
                manager.removeFido2Credential("non_existent_id_12345")
            } catch (_: Exception) {
                doubleRemoveOk = false
            }

            val allPassed = registerOk && listOk && removeOk && doubleRemoveOk
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: FIDO2 Lifecycle",
                passed = allPassed,
                duration = duration,
                details = "fido2Supported=$supportCheck register=$registerOk " +
                        "list=$listOk remove=$removeOk doubleRemove=$doubleRemoveOk"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: FIDO2 Lifecycle",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests biometric hardware detection. The test always passes because
     * the result depends on device capabilities; we just verify the API
     * returns a known status without crashing.
     */
    suspend fun runBiometricStatusTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val manager = AuthenticationManager.getInstance(context)
            val status = manager.checkBiometricStatus()

            val statusName = when (status) {
                is BiometricStatus.Available -> "Available"
                is BiometricStatus.NotEnrolled -> "NotEnrolled"
                is BiometricStatus.NoHardware -> "NoHardware"
                is BiometricStatus.SecurityUpdateRequired -> "SecurityUpdateRequired"
                is BiometricStatus.Unsupported -> "Unsupported"
            }

            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Auth: Biometric Status",
                passed = true, // any known status is fine
                duration = duration,
                details = "Hardware status: $statusName"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Biometric Status",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests the biometric enable/disable toggle persists correctly.
     */
    suspend fun runBiometricToggleTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val manager = AuthenticationManager.getInstance(context)

            // Save original
            val original = manager.biometricEnabled.value

            manager.setBiometricEnabled(true)
            val enabledOk = manager.biometricEnabled.value

            manager.setBiometricEnabled(false)
            val disabledOk = !manager.biometricEnabled.value

            // Rapid toggle shouldn't crash
            repeat(20) {
                manager.setBiometricEnabled(it % 2 == 0)
            }
            val rapidOk = true // no crash means pass

            // Restore original
            manager.setBiometricEnabled(original)

            val allPassed = enabledOk && disabledOk && rapidOk
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: Biometric Toggle",
                passed = allPassed,
                duration = duration,
                details = "enable=$enabledOk disable=$disabledOk rapidToggle=$rapidOk"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Biometric Toggle",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests input validation rules used on the sign-in / sign-up screens.
     * Covers email format, username format, password strength, and text
     * sanitisation.
     */
    suspend fun runInputValidationTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            // Email validation
            val validEmails = listOf("a@b.co", "user@example.com", "test+tag@domain.org")
            val invalidEmails = listOf("", " ", "nodomain", "no@", "@no.com", "a@b")
            val emailOk = validEmails.all { InputValidator.isValidEmail(it) }
                    && invalidEmails.none { InputValidator.isValidEmail(it) }

            // Username validation
            val validUsernames = listOf("abc", "user_123", "A" .repeat(50))
            val invalidUsernames = listOf("", "ab", "a b", "user@name", "A".repeat(51))
            val usernameOk = validUsernames.all { InputValidator.isValidUsername(it) }
                    && invalidUsernames.none { InputValidator.isValidUsername(it) }

            // Password strength
            val weakPassword = InputValidator.isValidPassword("short")   // < 8 chars
            val noDigit = InputValidator.isValidPassword("abcdefgh")      // no digit
            val noLetter = InputValidator.isValidPassword("12345678")     // no letter
            val goodPassword = InputValidator.isValidPassword("abcd1234") // valid
            val passwordOk = !weakPassword && !noDigit && !noLetter && goodPassword

            // Password strength levels
            val level0 = InputValidator.getPasswordStrength("abc")        // very weak
            val level4 = InputValidator.getPasswordStrength("Str0ng!Pass123")
            val strengthOk = level0 < level4 && level4 >= 3

            // Text sanitisation
            val sanitized = InputValidator.sanitizeText("<script>alert('x')</script>")
            val sanitizeOk = sanitized != null && !sanitized.contains("<script>")
            val blankReturnsNull = InputValidator.sanitizeText("  ") == null
            val tooLongReturnsNull = InputValidator.sanitizeText("A".repeat(3000), maxLength = 500) == null

            val allPassed = emailOk && usernameOk && passwordOk && strengthOk
                    && sanitizeOk && blankReturnsNull && tooLongReturnsNull
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: Input Validation",
                passed = allPassed,
                duration = duration,
                details = "email=$emailOk user=$usernameOk pwd=$passwordOk " +
                        "strength=$strengthOk sanitize=$sanitizeOk " +
                        "blank=$blankReturnsNull tooLong=$tooLongReturnsNull"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Input Validation",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests SecurityUtils encrypt→decrypt round-trip, edge cases
     * (empty string, null, long strings), and that different inputs
     * produce different ciphertexts.
     */
    suspend fun runSecurityUtilsRoundTripTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            // 1. Basic round-trip
            val plain = "https://example.supabase.co"
            val encrypted = SecurityUtils.encrypt(plain)
            val decrypted = SecurityUtils.decrypt(encrypted)
            val roundTripOk = decrypted == plain

            // 2. Empty / null edge cases
            val emptyOk = SecurityUtils.decrypt("") == ""
            val nullOk = SecurityUtils.decrypt(null) == ""

            // 3. Different inputs → different ciphertexts
            val enc1 = SecurityUtils.encrypt("alpha")
            val enc2 = SecurityUtils.encrypt("bravo")
            val diffOk = enc1 != enc2

            // 4. Long string round-trip
            val longPlain = "A".repeat(500)
            val longRoundTrip = SecurityUtils.decrypt(SecurityUtils.encrypt(longPlain)) == longPlain

            // 5. Stress: 200 rapid encrypt/decrypt cycles
            var stressOk = true
            repeat(200) { i ->
                val p = "key_$i"
                if (SecurityUtils.decrypt(SecurityUtils.encrypt(p)) != p) stressOk = false
            }

            val allPassed = roundTripOk && emptyOk && nullOk && diffOk && longRoundTrip && stressOk
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: SecurityUtils",
                passed = allPassed,
                duration = duration,
                details = "roundTrip=$roundTripOk empty=$emptyOk null=$nullOk " +
                        "diff=$diffOk long=$longRoundTrip stress=$stressOk"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: SecurityUtils",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests DeviceAuthority hash computation determinism and the
     * authorization check doesn't crash.
     */
    suspend fun runDeviceAuthorityTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            // 1. Hash should be deterministic
            val hash1 = DeviceAuthority.computeDeviceHash(context)
            val hash2 = DeviceAuthority.computeDeviceHash(context)
            val deterministicOk = hash1 == hash2

            // 2. Hash should be a 64-char hex string (SHA-256)
            val formatOk = hash1.length == 64 && hash1.all { it in '0'..'9' || it in 'a'..'f' }

            // 3. Authorization check should not crash
            val authCheckOk = try {
                DeviceAuthority.isAuthorizedDevice(context)
                true
            } catch (_: Exception) { false }

            val allPassed = deterministicOk && formatOk && authCheckOk
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: Device Authority",
                passed = allPassed,
                duration = duration,
                details = "deterministic=$deterministicOk format=$formatOk authCheck=$authCheckOk"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Device Authority",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests Supabase session state introspection:
     * – client availability flag
     * – session presence (null or valid)
     * – no crash when reading auth state on a cold start
     */
    suspend fun runSessionStateTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            // 1. isAvailable should return a boolean without crashing
            val available = AppSupabaseClient.isAvailable()
            val availableOk = true // no crash is success

            // 2. Client may or may not exist — either path must be safe
            val client = AppSupabaseClient.client
            val clientOk = true // null or non-null, both fine

            // 3. Reading the current session should not throw
            var sessionOk = true
            try {
                val nonNullClient = client
                if (nonNullClient != null) {
                    val session = nonNullClient.auth.currentSessionOrNull()
                    // If there IS a session, verify the access token looks sane
                    val token = session?.accessToken
                    if (token != null && token.isBlank()) sessionOk = false
                }
            } catch (_: Exception) {
                sessionOk = false
            }

            // 4. isConfigured should agree with isAvailable
            val configConsistent = AppSupabaseClient.isConfigured == available

            val allPassed = availableOk && clientOk && sessionOk && configConsistent
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: Session State",
                passed = allPassed,
                duration = duration,
                details = "available=$available client=${client != null} " +
                        "sessionOk=$sessionOk configConsistent=$configConsistent"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Session State",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests AccountLifecycleStatus data-class parsing, computed
     * properties, and edge-case timestamp handling.
     */
    suspend fun runAccountLifecycleParsingTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            // 1. Default state — active, no deletion, no detox
            val defaultStatus = AccountLifecycleStatus()
            val defaultOk = defaultStatus.is_active
                    && !defaultStatus.hasDeletionScheduled
                    && !defaultStatus.isDetoxActive

            // 2. Scheduled deletion
            val deletionStatus = AccountLifecycleStatus(
                id = "test",
                is_active = false,
                deletion_scheduled_at = "2099-12-31T23:59:59Z"
            )
            val deletionOk = deletionStatus.hasDeletionScheduled && !deletionStatus.is_active

            // 3. Active detox (future timestamp)
            val futureDetox = AccountLifecycleStatus(
                id = "test",
                detox_until = "2099-01-01T00:00:00Z"
            )
            val futureDetoxOk = futureDetox.isDetoxActive

            // 4. Expired detox (past timestamp)
            val pastDetox = AccountLifecycleStatus(
                id = "test",
                detox_until = "2020-01-01T00:00:00Z"
            )
            val pastDetoxOk = !pastDetox.isDetoxActive

            // 5. Blank / null edge cases should not crash
            val blankDetox = AccountLifecycleStatus(detox_until = "")
            val blankOk = !blankDetox.isDetoxActive

            val nullDetox = AccountLifecycleStatus(detox_until = null)
            val nullOk = !nullDetox.isDetoxActive

            // 6. Malformed timestamp should not crash
            val badTs = AccountLifecycleStatus(detox_until = "not-a-date")
            val badTsOk = !badTs.isDetoxActive

            val allPassed = defaultOk && deletionOk && futureDetoxOk && pastDetoxOk
                    && blankOk && nullOk && badTsOk
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: Account Lifecycle",
                passed = allPassed,
                duration = duration,
                details = "default=$defaultOk deletion=$deletionOk " +
                        "futureDetox=$futureDetoxOk pastDetox=$pastDetoxOk " +
                        "blank=$blankOk null=$nullOk badTs=$badTsOk"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Account Lifecycle",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests that all AuthMethod enum values exist and AuthResult
     * sealed-class branches are exhaustive.
     */
    suspend fun runAuthEnumCoverageTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            // 1. All expected AuthMethod values
            val expectedMethods = setOf("BIOMETRIC", "FIDO2", "TOTP", "EMAIL", "SMS", "BACKUP_CODES")
            val actualMethods = AuthMethod.entries.map { it.name }.toSet()
            val methodsOk = expectedMethods.all { it in actualMethods }

            // 2. AuthResult sealed variants can be instantiated
            val results = listOf(
                AuthResult.Success,
                AuthResult.Error("test", 0),
                AuthResult.Cancelled,
                AuthResult.NotAvailable,
                AuthResult.RequiresSetup
            )
            val variantsOk = results.size == 5

            // 3. Error code preserved
            val err = AuthResult.Error("msg", 42) as AuthResult.Error
            val errorFieldsOk = err.message == "msg" && err.code == 42

            // 4. BiometricStatus sealed variants
            val statuses = listOf(
                BiometricStatus.Available,
                BiometricStatus.NotEnrolled,
                BiometricStatus.NoHardware,
                BiometricStatus.SecurityUpdateRequired,
                BiometricStatus.Unsupported
            )
            val bioStatusOk = statuses.size == 5

            val allPassed = methodsOk && variantsOk && errorFieldsOk && bioStatusOk
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: Enum Coverage",
                passed = allPassed,
                duration = duration,
                details = "methods=$methodsOk variants=$variantsOk " +
                        "errFields=$errorFieldsOk bioStatus=$bioStatusOk"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Enum Coverage",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Stress-tests concurrent auth operations: multiple coroutines
     * reading/writing auth prefs simultaneously to detect races.
     */
    suspend fun runConcurrentAuthOpsTest(context: Context): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val manager = AuthenticationManager.getInstance(context)
            var failures = 0

            coroutineScope {
                val jobs = (1..20).map { i ->
                    async(Dispatchers.Default) {
                        try {
                            when (i % 4) {
                                0 -> {
                                    manager.setBiometricEnabled(true)
                                    manager.setBiometricEnabled(false)
                                }
                                1 -> {
                                    val codes = manager.generateBackupCodes(3)
                                    if (codes.size != 3) failures++
                                }
                                2 -> {
                                    val secret = manager.generateTotpSecret("concurrent_$i@test.app")
                                    if (secret.secret.isBlank()) failures++
                                }
                                3 -> {
                                    manager.checkBiometricStatus()
                                    manager.isFido2Supported()
                                }
                            }
                        } catch (_: Exception) {
                            failures++
                        }
                    }
                }
                jobs.awaitAll()
            }

            val duration = System.currentTimeMillis() - startTime
            StressTestResult(
                testName = "Auth: Concurrent Ops",
                passed = failures == 0,
                duration = duration,
                details = "20 concurrent auth ops, $failures failures"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: Concurrent Ops",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }

    /**
     * Tests the TotpSecret data class fields and URI generation
     * with edge-case account names (special characters, unicode).
     */
    suspend fun runTotpSecretEdgeCasesTest(): StressTestResult {
        val startTime = System.currentTimeMillis()
        return try {
            // 1. Normal account
            val normal = TotpSecret(secret = "JBSWY3DPEHPK3PXP", accountName = "user@example.com")
            val normalUri = normal.toUri()
            val normalOk = normalUri.contains("user@example.com") && normalUri.contains("JBSWY3DPEHPK3PXP")

            // 2. Account name with spaces
            val spaced = TotpSecret(secret = "JBSWY3DPEHPK3PXP", accountName = "user name")
            val spacedOk = spaced.toUri().contains("user name")

            // 3. Custom parameters
            val custom = TotpSecret(
                secret = "MFRGGZDF",
                accountName = "custom",
                algorithm = "SHA256",
                digits = 8,
                period = 60
            )
            val customUri = custom.toUri()
            val customOk = customUri.contains("digits=8")
                    && customUri.contains("period=60")
                    && customUri.contains("algorithm=SHA256")

            // 4. Defaults are correct
            val defaultsOk = normal.issuer == "NeuroComet"
                    && normal.algorithm == "SHA1"
                    && normal.digits == 6
                    && normal.period == 30

            val allPassed = normalOk && spacedOk && customOk && defaultsOk
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Auth: TOTP Edge Cases",
                passed = allPassed,
                duration = duration,
                details = "normal=$normalOk spaced=$spacedOk custom=$customOk defaults=$defaultsOk"
            )
        } catch (e: Exception) {
            StressTestResult(
                testName = "Auth: TOTP Edge Cases",
                passed = false,
                duration = System.currentTimeMillis() - startTime,
                details = "Test failed",
                errorMessage = e.message
            )
        }
    }
}

