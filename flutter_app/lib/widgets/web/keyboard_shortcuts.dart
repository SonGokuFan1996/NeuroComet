import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Keyboard shortcut handler for web/desktop
/// Provides common keyboard shortcuts for navigation and actions
class KeyboardShortcutHandler extends StatelessWidget {
  final Widget child;
  final VoidCallback? onSearch;
  final VoidCallback? onNewPost;
  final VoidCallback? onHome;
  final VoidCallback? onNotifications;
  final VoidCallback? onProfile;
  final VoidCallback? onSettings;
  final VoidCallback? onEscape;

  const KeyboardShortcutHandler({
    super.key,
    required this.child,
    this.onSearch,
    this.onNewPost,
    this.onHome,
    this.onNotifications,
    this.onProfile,
    this.onSettings,
    this.onEscape,
  });

  @override
  Widget build(BuildContext context) {
    // Only enable keyboard shortcuts on web/desktop
    if (!kIsWeb) return child;

    return Shortcuts(
      shortcuts: <ShortcutActivator, Intent>{
        // Ctrl/Cmd + K for search
        LogicalKeySet(
          LogicalKeyboardKey.control,
          LogicalKeyboardKey.keyK,
        ): const SearchIntent(),
        LogicalKeySet(
          LogicalKeyboardKey.meta,
          LogicalKeyboardKey.keyK,
        ): const SearchIntent(),

        // Ctrl/Cmd + N for new post
        LogicalKeySet(
          LogicalKeyboardKey.control,
          LogicalKeyboardKey.keyN,
        ): const NewPostIntent(),
        LogicalKeySet(
          LogicalKeyboardKey.meta,
          LogicalKeyboardKey.keyN,
        ): const NewPostIntent(),

        // Ctrl/Cmd + H for home
        LogicalKeySet(
          LogicalKeyboardKey.control,
          LogicalKeyboardKey.keyH,
        ): const HomeIntent(),

        // Escape to close dialogs/go back
        LogicalKeySet(LogicalKeyboardKey.escape): const EscapeIntent(),

        // / for quick search focus
        LogicalKeySet(LogicalKeyboardKey.slash): const SearchIntent(),
      },
      child: Actions(
        actions: <Type, Action<Intent>>{
          SearchIntent: CallbackAction<SearchIntent>(
            onInvoke: (_) {
              onSearch?.call();
              return null;
            },
          ),
          NewPostIntent: CallbackAction<NewPostIntent>(
            onInvoke: (_) {
              onNewPost?.call();
              return null;
            },
          ),
          HomeIntent: CallbackAction<HomeIntent>(
            onInvoke: (_) {
              onHome?.call();
              return null;
            },
          ),
          EscapeIntent: CallbackAction<EscapeIntent>(
            onInvoke: (_) {
              onEscape?.call();
              return null;
            },
          ),
        },
        child: Focus(
          autofocus: true,
          child: child,
        ),
      ),
    );
  }
}

/// Intent classes for keyboard shortcuts
class SearchIntent extends Intent {
  const SearchIntent();
}

class NewPostIntent extends Intent {
  const NewPostIntent();
}

class HomeIntent extends Intent {
  const HomeIntent();
}

class EscapeIntent extends Intent {
  const EscapeIntent();
}

/// A tooltip wrapper that shows keyboard shortcuts on web
class ShortcutTooltip extends StatelessWidget {
  final Widget child;
  final String message;
  final String? shortcut;

  const ShortcutTooltip({
    super.key,
    required this.child,
    required this.message,
    this.shortcut,
  });

  @override
  Widget build(BuildContext context) {
    if (!kIsWeb || shortcut == null) {
      return Tooltip(
        message: message,
        child: child,
      );
    }

    return Tooltip(
      richMessage: TextSpan(
        children: [
          TextSpan(text: message),
          TextSpan(
            text: '\n$shortcut',
            style: const TextStyle(
              fontSize: 11,
              color: Colors.grey,
            ),
          ),
        ],
      ),
      child: child,
    );
  }
}

/// Command palette for power users (Ctrl+K style)
class CommandPalette extends StatefulWidget {
  final List<CommandPaletteItem> items;
  final VoidCallback onClose;

  const CommandPalette({
    super.key,
    required this.items,
    required this.onClose,
  });

  /// Show the command palette as an overlay
  static void show(BuildContext context, List<CommandPaletteItem> items) {
    showDialog(
      context: context,
      barrierColor: Colors.black54,
      builder: (context) => CommandPalette(
        items: items,
        onClose: () => Navigator.of(context).pop(),
      ),
    );
  }

  @override
  State<CommandPalette> createState() => _CommandPaletteState();
}

class _CommandPaletteState extends State<CommandPalette> {
  final _searchController = TextEditingController();
  final _focusNode = FocusNode();
  List<CommandPaletteItem> _filteredItems = [];

  @override
  void initState() {
    super.initState();
    _filteredItems = widget.items;
    _searchController.addListener(_onSearchChanged);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _focusNode.requestFocus();
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _onSearchChanged() {
    final query = _searchController.text.toLowerCase();
    setState(() {
      if (query.isEmpty) {
        _filteredItems = widget.items;
      } else {
        _filteredItems = widget.items
            .where((item) =>
                item.label.toLowerCase().contains(query) ||
                (item.keywords?.any((k) => k.toLowerCase().contains(query)) ??
                    false))
            .toList();
      }
    });
  }

  void _executeItem(CommandPaletteItem item) {
    widget.onClose();
    item.onSelected();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Dialog(
      backgroundColor: Colors.transparent,
      insetPadding: const EdgeInsets.symmetric(horizontal: 80, vertical: 100),
      child: Container(
        constraints: const BoxConstraints(maxWidth: 600, maxHeight: 400),
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.black26,
              blurRadius: 20,
              offset: const Offset(0, 10),
            ),
          ],
        ),
        child: Column(
          children: [
            // Search input
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                border: Border(
                  bottom: BorderSide(
                    color: theme.dividerColor,
                  ),
                ),
              ),
              child: TextField(
                controller: _searchController,
                focusNode: _focusNode,
                decoration: InputDecoration(
                  hintText: 'Search commands...',
                  prefixIcon: const Icon(Icons.search),
                  border: InputBorder.none,
                  enabledBorder: InputBorder.none,
                  focusedBorder: InputBorder.none,
                ),
                onSubmitted: (_) {
                  if (_filteredItems.isNotEmpty) {
                    _executeItem(_filteredItems.first);
                  }
                },
              ),
            ),

            // Results list
            Expanded(
              child: _filteredItems.isEmpty
                  ? Center(
                      child: Text(
                        'No commands found',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.outline,
                        ),
                      ),
                    )
                  : ListView.builder(
                      itemCount: _filteredItems.length,
                      itemBuilder: (context, index) {
                        final item = _filteredItems[index];
                        return _CommandPaletteItemTile(
                          item: item,
                          onTap: () => _executeItem(item),
                        );
                      },
                    ),
            ),

            // Keyboard shortcuts hint
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                borderRadius: const BorderRadius.vertical(
                  bottom: Radius.circular(16),
                ),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _KeyboardHint(label: '↑↓', description: 'Navigate'),
                  const SizedBox(width: 16),
                  _KeyboardHint(label: '↵', description: 'Select'),
                  const SizedBox(width: 16),
                  _KeyboardHint(label: 'esc', description: 'Close'),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Individual command palette item
class CommandPaletteItem {
  final IconData icon;
  final String label;
  final String? description;
  final List<String>? keywords;
  final String? shortcut;
  final VoidCallback onSelected;

  const CommandPaletteItem({
    required this.icon,
    required this.label,
    this.description,
    this.keywords,
    this.shortcut,
    required this.onSelected,
  });
}

class _CommandPaletteItemTile extends StatefulWidget {
  final CommandPaletteItem item;
  final VoidCallback onTap;

  const _CommandPaletteItemTile({
    required this.item,
    required this.onTap,
  });

  @override
  State<_CommandPaletteItemTile> createState() =>
      _CommandPaletteItemTileState();
}

class _CommandPaletteItemTileState extends State<_CommandPaletteItemTile> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      cursor: SystemMouseCursors.click,
      child: GestureDetector(
        onTap: widget.onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          color: _isHovered
              ? theme.colorScheme.primaryContainer.withOpacity(0.3)
              : Colors.transparent,
          child: Row(
            children: [
              Icon(
                widget.item.icon,
                size: 20,
                color: theme.colorScheme.primary,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.item.label,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    if (widget.item.description != null)
                      Text(
                        widget.item.description!,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.outline,
                        ),
                      ),
                  ],
                ),
              ),
              if (widget.item.shortcut != null)
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 8,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    widget.item.shortcut!,
                    style: theme.textTheme.labelSmall?.copyWith(
                      color: theme.colorScheme.outline,
                      fontFamily: 'monospace',
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _KeyboardHint extends StatelessWidget {
  final String label;
  final String description;

  const _KeyboardHint({
    required this.label,
    required this.description,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: BorderRadius.circular(4),
            border: Border.all(
              color: theme.colorScheme.outline.withOpacity(0.3),
            ),
          ),
          child: Text(
            label,
            style: theme.textTheme.labelSmall?.copyWith(
              fontFamily: 'monospace',
            ),
          ),
        ),
        const SizedBox(width: 4),
        Text(
          description,
          style: theme.textTheme.labelSmall?.copyWith(
            color: theme.colorScheme.outline,
          ),
        ),
      ],
    );
  }
}

