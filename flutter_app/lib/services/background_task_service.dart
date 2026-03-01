import 'dart:async';
import 'package:flutter/foundation.dart';

/// Background Task Manager for NeuroComet.
/// Mirrors the Kotlin BackgroundTaskManager.kt
///
/// Features:
/// - Task prioritization & tracking
/// - Progress reporting
/// - Cancellation support
/// - Non-blocking UI
class BackgroundTaskService {
  static final BackgroundTaskService _instance =
      BackgroundTaskService._internal();
  factory BackgroundTaskService() => _instance;
  BackgroundTaskService._internal();

  final Map<String, TaskInfo> _activeTasks = {};
  int _taskCounter = 0;

  final ValueNotifier<Map<String, TaskState>> taskStates = ValueNotifier({});
  final ValueNotifier<bool> isLoading = ValueNotifier(false);

  // ═══════════════════════════════════════════════════════════════
  // TASK EXECUTION
  // ═══════════════════════════════════════════════════════════════

  /// Execute a background task with automatic tracking
  Future<TaskResult<T>> executeTask<T>({
    String? taskId,
    TaskPriority priority = TaskPriority.normal,
    TaskType taskType = TaskType.io,
    String description = '',
    required Future<T> Function(TaskContext context) block,
  }) async {
    final id = taskId ?? _generateTaskId();
    final context = TaskContext(id);

    try {
      _registerTask(id, priority, taskType, description);
      _updateLoadingState();

      final result = await block(context);
      _completeTask(id, TaskState.completed);
      return TaskResult.success(result);
    } on TaskCancelledException catch (e) {
      _completeTask(id, TaskState.cancelled);
      return TaskResult.cancelled(e.message);
    } catch (e) {
      _completeTask(id, TaskState.failed);
      debugPrint('Task $id failed: $e');
      return TaskResult.error(e);
    } finally {
      _updateLoadingState();
    }
  }

  /// Cancel a running task
  void cancelTask(String taskId) {
    final task = _activeTasks[taskId];
    if (task != null) {
      task.isCancelled = true;
      _completeTask(taskId, TaskState.cancelled);
    }
  }

  /// Cancel all running tasks
  void cancelAll() {
    for (final task in _activeTasks.values) {
      task.isCancelled = true;
    }
    _activeTasks.clear();
    taskStates.value = {};
    isLoading.value = false;
  }

  /// Get the state of a specific task
  TaskState? getTaskState(String taskId) => taskStates.value[taskId];

  // ═══════════════════════════════════════════════════════════════
  // CONVENIENCE METHODS
  // ═══════════════════════════════════════════════════════════════

  /// Execute a quick IO task
  Future<TaskResult<T>> io<T>({
    String? taskId,
    required Future<T> Function(TaskContext context) block,
  }) {
    return executeTask(
      taskId: taskId,
      taskType: TaskType.io,
      block: block,
    );
  }

  /// Execute a compute-heavy task
  Future<TaskResult<T>> compute<T>({
    String? taskId,
    required Future<T> Function(TaskContext context) block,
  }) {
    return executeTask(
      taskId: taskId,
      taskType: TaskType.compute,
      block: block,
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // INTERNAL
  // ═══════════════════════════════════════════════════════════════

  String _generateTaskId() => 'task_${++_taskCounter}';

  void _registerTask(
      String id, TaskPriority priority, TaskType type, String description) {
    _activeTasks[id] = TaskInfo(
      id: id,
      priority: priority,
      type: type,
      description: description,
      startTime: DateTime.now(),
    );
    final states = Map<String, TaskState>.from(taskStates.value);
    states[id] = TaskState.running;
    taskStates.value = states;
  }

  void _completeTask(String id, TaskState state) {
    _activeTasks.remove(id);
    final states = Map<String, TaskState>.from(taskStates.value);
    states[id] = state;
    taskStates.value = states;
  }

  void _updateLoadingState() {
    isLoading.value = _activeTasks.isNotEmpty;
  }
}

// ═══════════════════════════════════════════════════════════════
// DATA CLASSES
// ═══════════════════════════════════════════════════════════════

enum TaskPriority { low, normal, high, critical }

enum TaskType { io, compute, ui }

enum TaskState { running, completed, failed, cancelled }

class TaskInfo {
  final String id;
  final TaskPriority priority;
  final TaskType type;
  final String description;
  final DateTime startTime;
  bool isCancelled;

  TaskInfo({
    required this.id,
    required this.priority,
    required this.type,
    required this.description,
    required this.startTime,
    this.isCancelled = false,
  });
}

class TaskContext {
  final String taskId;
  TaskContext(this.taskId);

  /// Check if the task has been cancelled
  void checkCancellation() {
    final task = BackgroundTaskService()._activeTasks[taskId];
    if (task?.isCancelled == true) {
      throw TaskCancelledException('Task $taskId was cancelled');
    }
  }
}

class TaskCancelledException implements Exception {
  final String? message;
  TaskCancelledException([this.message]);
}

class TaskResult<T> {
  final T? data;
  final Object? error;
  final String? cancelMessage;
  final bool isSuccess;
  final bool isCancelled;

  TaskResult._({
    this.data,
    this.error,
    this.cancelMessage,
    required this.isSuccess,
    required this.isCancelled,
  });

  factory TaskResult.success(T data) =>
      TaskResult._(data: data, isSuccess: true, isCancelled: false);

  factory TaskResult.error(Object error) =>
      TaskResult._(error: error, isSuccess: false, isCancelled: false);

  factory TaskResult.cancelled([String? message]) => TaskResult._(
      cancelMessage: message, isSuccess: false, isCancelled: true);
}

