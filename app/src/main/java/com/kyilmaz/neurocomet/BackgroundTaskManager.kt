package com.kyilmaz.neurocomet

import android.content.Context
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

/**
 * Background Task Manager for NeuroComet
 *
 * Designed for:
 * - Efficient background processing
 * - Peak fluidity and performance
 * - Battery optimization
 * - Task prioritization
 * - Cancellation support
 * - Progress reporting
 *
 * Neurodivergent-friendly:
 * - Non-blocking UI
 * - Smooth animations during background work
 * - Clear progress feedback
 * - No jarring interruptions
 */

object BackgroundTaskManager {

    private const val TAG = "BackgroundTaskManager"

    // Coroutine scopes for different task types
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val computeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Task tracking
    private val activeTasks = ConcurrentHashMap<String, TaskInfo>()
    private val taskCounter = AtomicInteger(0)
    private val taskMutex = Mutex()

    // Task state flow
    private val _taskStates = MutableStateFlow<Map<String, TaskState>>(emptyMap())
    val taskStates: StateFlow<Map<String, TaskState>> = _taskStates.asStateFlow()

    // Overall loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ═══════════════════════════════════════════════════════════════
    // TASK EXECUTION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Execute a background task with automatic tracking
     */
    suspend fun <T> executeTask(
        taskId: String = generateTaskId(),
        priority: TaskPriority = TaskPriority.NORMAL,
        taskType: TaskType = TaskType.IO,
        description: String = "",
        reportProgress: Boolean = true,
        block: suspend TaskContext.() -> T
    ): TaskResult<T> {
        val context = TaskContext(taskId)

        return try {
            // Register task
            registerTask(taskId, priority, taskType, description)

            // Update loading state
            updateLoadingState()

            // Execute on appropriate dispatcher
            val scope = when (taskType) {
                TaskType.IO -> ioScope
                TaskType.COMPUTE -> computeScope
                TaskType.UI -> mainScope
            }

            val result = withContext(scope.coroutineContext) {
                block(context)
            }

            // Mark as completed
            completeTask(taskId, TaskState.COMPLETED)

            TaskResult.Success(result)
        } catch (e: CancellationException) {
            completeTask(taskId, TaskState.CANCELLED)
            TaskResult.Cancelled(e.message)
        } catch (e: Exception) {
            completeTask(taskId, TaskState.FAILED)
            Log.e(TAG, "Task $taskId failed", e)
            TaskResult.Error(e)
        } finally {
            updateLoadingState()
        }
    }

    /**
     * Execute a task without blocking (fire-and-forget)
     */
    fun launchTask(
        taskId: String = generateTaskId(),
        priority: TaskPriority = TaskPriority.NORMAL,
        taskType: TaskType = TaskType.IO,
        description: String = "",
        onComplete: ((TaskResult<Unit>) -> Unit)? = null,
        block: suspend TaskContext.() -> Unit
    ): Job {
        val scope = when (taskType) {
            TaskType.IO -> ioScope
            TaskType.COMPUTE -> computeScope
            TaskType.UI -> mainScope
        }

        return scope.launch {
            val result = executeTask(taskId, priority, taskType, description) {
                block()
            }
            onComplete?.invoke(result)
        }
    }

    /**
     * Execute multiple tasks in parallel
     */
    suspend fun <T> executeParallel(
        tasks: List<suspend () -> T>,
        maxConcurrency: Int = 4
    ): List<TaskResult<T>> {
        return coroutineScope {
            tasks.chunked(maxConcurrency).flatMap { chunk ->
                chunk.map { task ->
                    async {
                        try {
                            TaskResult.Success(task())
                        } catch (e: CancellationException) {
                            TaskResult.Cancelled<T>(e.message)
                        } catch (e: Exception) {
                            TaskResult.Error<T>(e)
                        }
                    }
                }.awaitAll()
            }
        }
    }

    /**
     * Execute tasks sequentially
     */
    suspend fun <T> executeSequential(
        tasks: List<suspend () -> T>,
        stopOnError: Boolean = false
    ): List<TaskResult<T>> {
        val results = mutableListOf<TaskResult<T>>()

        for (task in tasks) {
            val result = try {
                TaskResult.Success(task())
            } catch (e: CancellationException) {
                TaskResult.Cancelled<T>(e.message)
            } catch (e: Exception) {
                TaskResult.Error<T>(e)
            }

            results.add(result)

            if (stopOnError && result is TaskResult.Error) {
                break
            }
        }

        return results
    }

    // ═══════════════════════════════════════════════════════════════
    // DEBOUNCED/THROTTLED EXECUTION
    // ═══════════════════════════════════════════════════════════════

    private val debounceJobs = ConcurrentHashMap<String, Job>()

    /**
     * Execute a task after a delay, cancelling any previous pending execution
     * Useful for search-as-you-type scenarios
     */
    fun debounce(
        key: String,
        delayMs: Long = 300L,
        scope: CoroutineScope = ioScope,
        block: suspend () -> Unit
    ) {
        debounceJobs[key]?.cancel()
        debounceJobs[key] = scope.launch {
            delay(delayMs)
            block()
            debounceJobs.remove(key)
        }
    }

    private val throttleTimestamps = ConcurrentHashMap<String, Long>()

    /**
     * Execute a task at most once per interval
     * Useful for rate-limiting API calls
     */
    fun throttle(
        key: String,
        intervalMs: Long = 1000L,
        scope: CoroutineScope = ioScope,
        block: suspend () -> Unit
    ) {
        val now = System.currentTimeMillis()
        val lastExecution = throttleTimestamps[key] ?: 0L

        if (now - lastExecution >= intervalMs) {
            throttleTimestamps[key] = now
            scope.launch { block() }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TASK MANAGEMENT
    // ═══════════════════════════════════════════════════════════════

    private fun generateTaskId(): String {
        return "task_${System.currentTimeMillis()}_${taskCounter.incrementAndGet()}"
    }

    private suspend fun registerTask(
        taskId: String,
        priority: TaskPriority,
        taskType: TaskType,
        description: String
    ) {
        taskMutex.withLock {
            activeTasks[taskId] = TaskInfo(
                id = taskId,
                priority = priority,
                type = taskType,
                description = description,
                startTime = System.currentTimeMillis()
            )

            _taskStates.value = _taskStates.value + (taskId to TaskState.RUNNING)
        }

        Log.d(TAG, "Task registered: $taskId ($description)")
    }

    private suspend fun completeTask(taskId: String, state: TaskState) {
        taskMutex.withLock {
            activeTasks[taskId]?.let { task ->
                val duration = System.currentTimeMillis() - task.startTime
                Log.d(TAG, "Task completed: $taskId in ${duration}ms with state: $state")
            }

            activeTasks.remove(taskId)
            _taskStates.value = _taskStates.value + (taskId to state)

            // Clean up old states after a delay
            ioScope.launch {
                delay(5000)
                _taskStates.value = _taskStates.value - taskId
            }
        }
    }

    private fun updateLoadingState() {
        _isLoading.value = activeTasks.isNotEmpty()
    }

    /**
     * Cancel a specific task
     */
    fun cancelTask(taskId: String) {
        activeTasks[taskId]?.job?.cancel()
        ioScope.launch {
            completeTask(taskId, TaskState.CANCELLED)
        }
    }

    /**
     * Cancel all running tasks
     */
    fun cancelAllTasks() {
        activeTasks.keys.forEach { taskId ->
            cancelTask(taskId)
        }
    }

    /**
     * Get currently running task count
     */
    fun getActiveTaskCount(): Int = activeTasks.size

    /**
     * Check if a specific task is running
     */
    fun isTaskRunning(taskId: String): Boolean = activeTasks.containsKey(taskId)

    // ═══════════════════════════════════════════════════════════════
    // BATTERY-EFFICIENT SCHEDULING
    // ═══════════════════════════════════════════════════════════════

    /**
     * Schedule a task that respects device power state
     */
    fun scheduleEfficientTask(
        context: Context,
        taskId: String = generateTaskId(),
        block: suspend () -> Unit
    ) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        ioScope.launch {
            // Wait for device to not be in power save mode
            while (powerManager.isPowerSaveMode) {
                delay(30_000) // Check every 30 seconds
            }

            executeTask(taskId, TaskPriority.LOW, TaskType.IO) {
                block()
            }
        }
    }

    /**
     * Execute with automatic retry on failure
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000L,
        maxDelayMs: Long = 30_000L,
        factor: Double = 2.0,
        block: suspend () -> T
    ): TaskResult<T> {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                return TaskResult.Success(block())
            } catch (e: CancellationException) {
                throw e // Don't retry on cancellation
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms", e)

                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
                }
            }
        }

        return TaskResult.Error(lastException ?: Exception("Max retries exceeded"))
    }

    // ═══════════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════════

    /**
     * Clean up all resources
     */
    fun cleanup() {
        cancelAllTasks()
        debounceJobs.values.forEach { it.cancel() }
        debounceJobs.clear()
        throttleTimestamps.clear()
        ioScope.cancel()
        computeScope.cancel()
        Log.d(TAG, "Background task manager cleaned up")
    }
}

// ═══════════════════════════════════════════════════════════════
// DATA CLASSES AND ENUMS
// ═══════════════════════════════════════════════════════════════

enum class TaskPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

enum class TaskType {
    IO,      // Network, file, database operations
    COMPUTE, // CPU-intensive calculations
    UI       // UI updates (use sparingly)
}

enum class TaskState {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class TaskInfo(
    val id: String,
    val priority: TaskPriority,
    val type: TaskType,
    val description: String,
    val startTime: Long,
    val job: Job? = null
)

sealed class TaskResult<out T> {
    data class Success<T>(val data: T) : TaskResult<T>()
    data class Error<T>(val exception: Throwable) : TaskResult<T>()
    data class Cancelled<T>(val reason: String?) : TaskResult<T>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isCancelled: Boolean get() = this is Cancelled

    fun getOrNull(): T? = (this as? Success)?.data
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Cancelled -> throw CancellationException(reason)
    }

    fun <R> map(transform: (T) -> R): TaskResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception)
        is Cancelled -> Cancelled(reason)
    }

    suspend fun <R> flatMap(transform: suspend (T) -> TaskResult<R>): TaskResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> Error(exception)
        is Cancelled -> Cancelled(reason)
    }
}

/**
 * Context for task execution with progress reporting
 */
class TaskContext(val taskId: String) {
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    /**
     * Report progress (0.0 to 1.0)
     */
    fun reportProgress(value: Float) {
        _progress.value = value.coerceIn(0f, 1f)
    }

    /**
     * Report progress with step count
     */
    fun reportProgress(current: Int, total: Int) {
        if (total > 0) {
            _progress.value = (current.toFloat() / total).coerceIn(0f, 1f)
        }
    }

    /**
     * Check if the task has been cancelled
     */
    suspend fun isCancelled(): Boolean {
        return !currentCoroutineContext().isActive
    }

    /**
     * Yield if cancelled
     */
    suspend fun checkCancellation() {
        yield()
    }
}

// ═══════════════════════════════════════════════════════════════
// EXTENSION FUNCTIONS
// ═══════════════════════════════════════════════════════════════

/**
 * Execute a suspending function with loading indicator
 */
suspend fun <T> withLoading(
    loadingState: MutableStateFlow<Boolean>,
    block: suspend () -> T
): T {
    loadingState.value = true
    return try {
        block()
    } finally {
        loadingState.value = false
    }
}

/**
 * Collect flow with automatic error handling
 */
suspend fun <T> Flow<T>.collectSafely(
    onError: (Throwable) -> Unit = {},
    onSuccess: (T) -> Unit
) {
    catch { e -> onError(e) }
        .collect { onSuccess(it) }
}

/**
 * Convert a list of TaskResults to a single result
 */
fun <T> List<TaskResult<T>>.combine(): TaskResult<List<T>> {
    val results = mutableListOf<T>()
    for (result in this) {
        when (result) {
            is TaskResult.Success -> results.add(result.data)
            is TaskResult.Error -> return TaskResult.Error(result.exception)
            is TaskResult.Cancelled -> return TaskResult.Cancelled(result.reason)
        }
    }
    return TaskResult.Success(results)
}

