import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/custom_avatar.dart';
import '../../l10n/app_localizations.dart';

/// A polished, simple avatar maker screen
/// Features:
/// - Clean, simple UI
/// - Real-time preview
/// - Easy customization categories
/// - Smooth animations
class AvatarMakerScreen extends ConsumerStatefulWidget {
  final CustomAvatar? initialAvatar;
  final Function(CustomAvatar) onSave;

  const AvatarMakerScreen({
    super.key,
    this.initialAvatar,
    required this.onSave,
  });

  @override
  ConsumerState<AvatarMakerScreen> createState() => _AvatarMakerScreenState();
}

class _AvatarMakerScreenState extends ConsumerState<AvatarMakerScreen>
    with SingleTickerProviderStateMixin {
  late CustomAvatar _avatar;
  late TabController _tabController;
  bool _hasChanges = false;

  // Simplified categories
  static const _categoryLabels = ['Skin', 'Hair', 'Eyes', 'Face', 'Extras'];

  @override
  void initState() {
    super.initState();
    _avatar = widget.initialAvatar ?? CustomAvatar.createDefault();
    _tabController = TabController(length: _categoryLabels.length, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  void _updateAvatar(CustomAvatar newAvatar) {
    setState(() {
      _avatar = newAvatar;
      _hasChanges = true;
    });
  }

  void _randomizeAvatar() {
    final random = DateTime.now().millisecondsSinceEpoch;
    final skinTones = AvatarSkinTones.tones;
    final hairColors = AvatarHairColors.colors;
    final eyeColors = AvatarEyeColors.colors;
    final bgColors = AvatarBackgroundColors.colors;

    _updateAvatar(CustomAvatar(
      id: _avatar.id,
      shape: AvatarShape.values[random % AvatarShape.values.length],
      backgroundColor: bgColors[random % bgColors.length],
      skinColor: skinTones[(random ~/ 2) % skinTones.length],
      hairStyle: AvatarHairStyle.values[(random ~/ 3) % AvatarHairStyle.values.length],
      hairColor: hairColors[(random ~/ 4) % hairColors.length],
      eyeStyle: AvatarEyeStyle.values[(random ~/ 5) % AvatarEyeStyle.values.length],
      eyeColor: eyeColors[(random ~/ 6) % eyeColors.length],
      mouthStyle: AvatarMouthStyle.values[(random ~/ 7) % AvatarMouthStyle.values.length],
      accessory: random % 3 == 0
          ? AvatarAccessory.values[(random ~/ 8) % AvatarAccessory.values.length]
          : AvatarAccessory.none,
      facialHair: random % 4 == 0
          ? AvatarFacialHair.values[(random ~/ 9) % AvatarFacialHair.values.length]
          : AvatarFacialHair.none,
      createdAt: DateTime.now(),
    ));
  }

  Future<bool> _onWillPop() async {
    if (!_hasChanges) return true;

    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Discard changes?'),
        content: const Text('Your avatar changes will be lost.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Keep Editing'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Discard'),
          ),
        ],
      ),
    );

    return result ?? false;
  }

  void _saveAvatar() {
    widget.onSave(_avatar);
    Navigator.pop(context);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return PopScope(
      canPop: !_hasChanges,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop) return;
        final shouldPop = await _onWillPop();
        if (shouldPop && context.mounted) {
          Navigator.pop(context);
        }
      },
      child: Scaffold(
        backgroundColor: theme.colorScheme.surface,
        appBar: AppBar(
          title: const Text('Create Your Avatar'),
          centerTitle: true,
          leading: IconButton(
            icon: const Icon(Icons.close),
            onPressed: () async {
              if (await _onWillPop()) {
                if (context.mounted) Navigator.pop(context);
              }
            },
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.casino_outlined),
              tooltip: 'Randomize',
              onPressed: _randomizeAvatar,
            ),
            const SizedBox(width: 4),
            Padding(
              padding: const EdgeInsets.only(right: 12),
              child: FilledButton.icon(
                onPressed: _saveAvatar,
                icon: const Icon(Icons.check, size: 18),
                label: Text(l10n.save),
              ),
            ),
          ],
        ),
        body: Column(
          children: [
            // Avatar preview section
            _buildAvatarPreview(theme),

            // Category tabs
            Container(
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                border: Border(
                  bottom: BorderSide(
                    color: theme.colorScheme.outlineVariant.withValues(alpha: 0.5),
                  ),
                ),
              ),
              child: TabBar(
                controller: _tabController,
                tabs: _categoryLabels.map((label) => Tab(text: label)).toList(),
                indicatorSize: TabBarIndicatorSize.label,
                dividerColor: Colors.transparent,
              ),
            ),

            // Category content
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [
                  _buildSkinOptions(),
                  _buildHairOptions(),
                  _buildEyeOptions(),
                  _buildFaceOptions(),
                  _buildExtrasOptions(),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAvatarPreview(ThemeData theme) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
            theme.colorScheme.surface,
          ],
        ),
      ),
      child: Center(
        child: Container(
          width: 140,
          height: 140,
          decoration: BoxDecoration(
            shape: _avatar.shape == AvatarShape.circle ? BoxShape.circle : BoxShape.rectangle,
            borderRadius: _avatar.shape == AvatarShape.rounded
                ? BorderRadius.circular(28)
                : _avatar.shape == AvatarShape.square
                    ? BorderRadius.circular(8)
                    : null,
            color: _avatar.backgroundColor.toColor(),
            boxShadow: [
              BoxShadow(
                color: _avatar.backgroundColor.toColor().withValues(alpha: 0.4),
                blurRadius: 24,
                spreadRadius: 4,
              ),
            ],
          ),
          child: ClipRRect(
            borderRadius: _avatar.shape == AvatarShape.circle
                ? BorderRadius.circular(70)
                : _avatar.shape == AvatarShape.rounded
                    ? BorderRadius.circular(28)
                    : BorderRadius.circular(8),
            child: _SimpleAvatarPreview(avatar: _avatar, size: 140),
          ),
        ),
      ),
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // SKIN OPTIONS
  // ═══════════════════════════════════════════════════════════════
  Widget _buildSkinOptions() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('Skin Tone'),
          const SizedBox(height: 12),
          _buildColorPalette(
            colors: AvatarSkinTones.tones,
            selectedColor: _avatar.skinColor,
            onSelect: (color) => _updateAvatar(_avatar.copyWith(skinColor: color)),
          ),
          const SizedBox(height: 28),
          _buildSectionTitle('Shape'),
          const SizedBox(height: 12),
          _buildShapeSelector(),
          const SizedBox(height: 28),
          _buildSectionTitle('Background'),
          const SizedBox(height: 12),
          _buildColorPalette(
            colors: AvatarBackgroundColors.colors,
            selectedColor: _avatar.backgroundColor,
            onSelect: (color) => _updateAvatar(_avatar.copyWith(backgroundColor: color)),
          ),
        ],
      ),
    );
  }

  Widget _buildShapeSelector() {
    return Row(
      children: AvatarShape.values.map((shape) {
        final isSelected = _avatar.shape == shape;
        return Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: _OptionCard(
              isSelected: isSelected,
              onTap: () => _updateAvatar(_avatar.copyWith(shape: shape)),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: Theme.of(context).colorScheme.primary,
                      shape: shape == AvatarShape.circle ? BoxShape.circle : BoxShape.rectangle,
                      borderRadius: shape == AvatarShape.rounded
                          ? BorderRadius.circular(8)
                          : shape == AvatarShape.square
                              ? BorderRadius.circular(2)
                              : null,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    shape.name[0].toUpperCase() + shape.name.substring(1),
                    style: Theme.of(context).textTheme.labelSmall,
                  ),
                ],
              ),
            ),
          ),
        );
      }).toList(),
    );
  }

  // ═══════════════════════════════════════════════════════════════
  // HAIR OPTIONS
  // ═══════════════════════════════════════════════════════════════
  Widget _buildHairOptions() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('Hair Style'),
          const SizedBox(height: 12),
          _buildHairStyleGrid(),
          const SizedBox(height: 28),
          _buildSectionTitle('Hair Color'),
          const SizedBox(height: 12),
          _buildColorPalette(
            colors: AvatarHairColors.colors,
            selectedColor: _avatar.hairColor,
            onSelect: (color) => _updateAvatar(_avatar.copyWith(hairColor: color)),
          ),
        ],
      ),
    );
  }

  Widget _buildHairStyleGrid() {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 4,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 1.0,
      ),
      itemCount: AvatarHairStyle.values.length,
      itemBuilder: (context, index) {
        final style = AvatarHairStyle.values[index];
        final isSelected = _avatar.hairStyle == style;
        return _OptionCard(
          isSelected: isSelected,
          onTap: () => _updateAvatar(_avatar.copyWith(hairStyle: style)),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(_getHairEmoji(style), style: const TextStyle(fontSize: 24)),
              const SizedBox(height: 4),
              Text(
                _formatName(style.name),
                style: Theme.of(context).textTheme.labelSmall,
                textAlign: TextAlign.center,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        );
      },
    );
  }

  String _getHairEmoji(AvatarHairStyle style) {
    switch (style) {
      case AvatarHairStyle.none: return '🚫';
      case AvatarHairStyle.short: return '✂️';
      case AvatarHairStyle.medium: return '💇';
      case AvatarHairStyle.long: return '💁';
      case AvatarHairStyle.curly: return '🌀';
      case AvatarHairStyle.wavy: return '🌊';
      case AvatarHairStyle.buzz: return '👨‍🦲';
      case AvatarHairStyle.ponytail: return '🎀';
      case AvatarHairStyle.bun: return '🔵';
      case AvatarHairStyle.mohawk: return '🦔';
      case AvatarHairStyle.afro: return '🌳';
      case AvatarHairStyle.spiky: return '⚡';
      case AvatarHairStyle.braids: return '🪢';
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // EYE OPTIONS
  // ═══════════════════════════════════════════════════════════════
  Widget _buildEyeOptions() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('Eye Style'),
          const SizedBox(height: 12),
          _buildEyeStyleGrid(),
          const SizedBox(height: 28),
          _buildSectionTitle('Eye Color'),
          const SizedBox(height: 12),
          _buildColorPalette(
            colors: AvatarEyeColors.colors,
            selectedColor: _avatar.eyeColor,
            onSelect: (color) => _updateAvatar(_avatar.copyWith(eyeColor: color)),
          ),
        ],
      ),
    );
  }

  Widget _buildEyeStyleGrid() {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 5,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 1.0,
      ),
      itemCount: AvatarEyeStyle.values.length,
      itemBuilder: (context, index) {
        final style = AvatarEyeStyle.values[index];
        final isSelected = _avatar.eyeStyle == style;
        return _OptionCard(
          isSelected: isSelected,
          onTap: () => _updateAvatar(_avatar.copyWith(eyeStyle: style)),
          child: Center(
            child: Text(_getEyeEmoji(style), style: const TextStyle(fontSize: 28)),
          ),
        );
      },
    );
  }

  String _getEyeEmoji(AvatarEyeStyle style) {
    switch (style) {
      case AvatarEyeStyle.normal: return '👁️';
      case AvatarEyeStyle.happy: return '😊';
      case AvatarEyeStyle.sleepy: return '😴';
      case AvatarEyeStyle.wink: return '😉';
      case AvatarEyeStyle.surprised: return '😲';
      case AvatarEyeStyle.hearts: return '😍';
      case AvatarEyeStyle.stars: return '🤩';
      case AvatarEyeStyle.glasses: return '🤓';
      case AvatarEyeStyle.sunglasses: return '😎';
      case AvatarEyeStyle.closed: return '😌';
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // FACE OPTIONS (Mouth + Facial Hair)
  // ═══════════════════════════════════════════════════════════════
  Widget _buildFaceOptions() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('Mouth'),
          const SizedBox(height: 12),
          _buildMouthStyleGrid(),
          const SizedBox(height: 28),
          _buildSectionTitle('Facial Hair'),
          const SizedBox(height: 12),
          _buildFacialHairGrid(),
        ],
      ),
    );
  }

  Widget _buildMouthStyleGrid() {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 4,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 1.0,
      ),
      itemCount: AvatarMouthStyle.values.length,
      itemBuilder: (context, index) {
        final style = AvatarMouthStyle.values[index];
        final isSelected = _avatar.mouthStyle == style;
        return _OptionCard(
          isSelected: isSelected,
          onTap: () => _updateAvatar(_avatar.copyWith(mouthStyle: style)),
          child: Center(
            child: Text(_getMouthEmoji(style), style: const TextStyle(fontSize: 28)),
          ),
        );
      },
    );
  }

  String _getMouthEmoji(AvatarMouthStyle style) {
    switch (style) {
      case AvatarMouthStyle.smile: return '😊';
      case AvatarMouthStyle.grin: return '😁';
      case AvatarMouthStyle.neutral: return '😐';
      case AvatarMouthStyle.sad: return '😔';
      case AvatarMouthStyle.surprised: return '😮';
      case AvatarMouthStyle.tongue: return '😛';
      case AvatarMouthStyle.teeth: return '😬';
      case AvatarMouthStyle.smirk: return '😏';
    }
  }

  Widget _buildFacialHairGrid() {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 1.2,
      ),
      itemCount: AvatarFacialHair.values.length,
      itemBuilder: (context, index) {
        final style = AvatarFacialHair.values[index];
        final isSelected = _avatar.facialHair == style;
        return _OptionCard(
          isSelected: isSelected,
          onTap: () => _updateAvatar(_avatar.copyWith(facialHair: style)),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(_getFacialHairEmoji(style), style: const TextStyle(fontSize: 24)),
              const SizedBox(height: 4),
              Text(
                _formatName(style.name),
                style: Theme.of(context).textTheme.labelSmall,
                textAlign: TextAlign.center,
              ),
            ],
          ),
        );
      },
    );
  }

  String _getFacialHairEmoji(AvatarFacialHair style) {
    switch (style) {
      case AvatarFacialHair.none: return '🚫';
      case AvatarFacialHair.stubble: return '🧔‍♂️';
      case AvatarFacialHair.mustache: return '🥸';
      case AvatarFacialHair.goatee: return '🐐';
      case AvatarFacialHair.beard: return '🧔';
      case AvatarFacialHair.fullBeard: return '🧙';
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // EXTRAS (Accessories)
  // ═══════════════════════════════════════════════════════════════
  Widget _buildExtrasOptions() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSectionTitle('Accessories'),
          const SizedBox(height: 12),
          _buildAccessoryGrid(),
        ],
      ),
    );
  }

  Widget _buildAccessoryGrid() {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 4,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 1.0,
      ),
      itemCount: AvatarAccessory.values.length,
      itemBuilder: (context, index) {
        final accessory = AvatarAccessory.values[index];
        final isSelected = _avatar.accessory == accessory;
        return _OptionCard(
          isSelected: isSelected,
          onTap: () => _updateAvatar(_avatar.copyWith(accessory: accessory)),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(_getAccessoryEmoji(accessory), style: const TextStyle(fontSize: 24)),
              const SizedBox(height: 4),
              Text(
                _formatName(accessory.name),
                style: Theme.of(context).textTheme.labelSmall,
                textAlign: TextAlign.center,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        );
      },
    );
  }

  String _getAccessoryEmoji(AvatarAccessory accessory) {
    switch (accessory) {
      case AvatarAccessory.none: return '🚫';
      case AvatarAccessory.glasses: return '👓';
      case AvatarAccessory.sunglasses: return '🕶️';
      case AvatarAccessory.hat: return '🎩';
      case AvatarAccessory.cap: return '🧢';
      case AvatarAccessory.beanie: return '🧶';
      case AvatarAccessory.headband: return '🎽';
      case AvatarAccessory.bow: return '🎀';
      case AvatarAccessory.earrings: return '💎';
      case AvatarAccessory.headphones: return '🎧';
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // COMMON UI COMPONENTS
  // ═══════════════════════════════════════════════════════════════
  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: Theme.of(context).textTheme.titleMedium?.copyWith(
        fontWeight: FontWeight.bold,
      ),
    );
  }

  Widget _buildColorPalette({
    required List<String> colors,
    required String selectedColor,
    required Function(String) onSelect,
  }) {
    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: colors.map((color) {
        final isSelected = color.toUpperCase() == selectedColor.toUpperCase();
        final colorValue = color.toColor();
        return GestureDetector(
          onTap: () => onSelect(color),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: colorValue,
              shape: BoxShape.circle,
              border: Border.all(
                color: isSelected
                    ? Theme.of(context).colorScheme.primary
                    : colorValue.computeLuminance() > 0.8
                        ? Colors.grey.shade300
                        : Colors.transparent,
                width: isSelected ? 3 : 1,
              ),
              boxShadow: isSelected
                  ? [
                      BoxShadow(
                        color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.4),
                        blurRadius: 8,
                        spreadRadius: 2,
                      ),
                    ]
                  : null,
            ),
            child: isSelected
                ? Icon(
                    Icons.check,
                    color: colorValue.computeLuminance() > 0.5 ? Colors.black : Colors.white,
                    size: 20,
                  )
                : null,
          ),
        );
      }).toList(),
    );
  }

  String _formatName(String name) {
    if (name.isEmpty) return name;
    // Convert camelCase to Title Case
    final buffer = StringBuffer();
    for (int i = 0; i < name.length; i++) {
      if (i == 0) {
        buffer.write(name[i].toUpperCase());
      } else if (name[i].toUpperCase() == name[i] && name[i] != name[i].toLowerCase()) {
        buffer.write(' ');
        buffer.write(name[i]);
      } else {
        buffer.write(name[i]);
      }
    }
    return buffer.toString();
  }
}

// ═══════════════════════════════════════════════════════════════
// HELPER WIDGETS
// ═══════════════════════════════════════════════════════════════

/// A simple option card with selection state
class _OptionCard extends StatelessWidget {
  final bool isSelected;
  final VoidCallback onTap;
  final Widget child;

  const _OptionCard({
    required this.isSelected,
    required this.onTap,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(12),
          color: isSelected
              ? theme.colorScheme.primaryContainer
              : theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
          border: Border.all(
            color: isSelected
                ? theme.colorScheme.primary
                : theme.colorScheme.outlineVariant.withValues(alpha: 0.5),
            width: isSelected ? 2 : 1,
          ),
        ),
        child: child,
      ),
    );
  }
}

/// Simple avatar preview that uses basic drawing
class _SimpleAvatarPreview extends StatelessWidget {
  final CustomAvatar avatar;
  final double size;

  const _SimpleAvatarPreview({required this.avatar, required this.size});

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      size: Size(size, size),
      painter: _SimpleAvatarPainter(avatar),
    );
  }
}

/// A simplified avatar painter that draws the avatar components
class _SimpleAvatarPainter extends CustomPainter {
  final CustomAvatar avatar;

  _SimpleAvatarPainter(this.avatar);

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final faceRadius = size.width * 0.32;

    // Draw face
    final facePaint = Paint()
      ..color = avatar.skinColor.toColor()
      ..style = PaintingStyle.fill;

    canvas.drawOval(
      Rect.fromCenter(
        center: Offset(center.dx, center.dy + faceRadius * 0.15),
        width: faceRadius * 2,
        height: faceRadius * 2.2,
      ),
      facePaint,
    );

    // Draw hair
    _drawHair(canvas, center, faceRadius);

    // Draw eyes
    _drawEyes(canvas, center, faceRadius);

    // Draw mouth
    _drawMouth(canvas, center, faceRadius);

    // Draw facial hair
    if (avatar.facialHair != null && avatar.facialHair != AvatarFacialHair.none) {
      _drawFacialHair(canvas, center, faceRadius);
    }

    // Draw accessory
    if (avatar.accessory != null && avatar.accessory != AvatarAccessory.none) {
      _drawAccessory(canvas, center, faceRadius, size);
    }
  }

  void _drawHair(Canvas canvas, Offset center, double faceRadius) {
    if (avatar.hairStyle == AvatarHairStyle.none) return;

    final hairPaint = Paint()
      ..color = avatar.hairColor.toColor()
      ..style = PaintingStyle.fill;

    final hairTop = center.dy - faceRadius * 0.3;
    final hairWidth = faceRadius * 2.2;

    switch (avatar.hairStyle) {
      case AvatarHairStyle.short:
      case AvatarHairStyle.buzz:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, hairTop),
            width: hairWidth,
            height: hairWidth * 0.8,
          ),
          3.14, 3.14, true, hairPaint,
        );
        break;
      case AvatarHairStyle.medium:
      case AvatarHairStyle.wavy:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, hairTop),
            width: hairWidth,
            height: hairWidth,
          ),
          3.14, 3.14, true, hairPaint,
        );
        // Side hair
        canvas.drawRect(
          Rect.fromLTWH(
            center.dx - hairWidth / 2,
            hairTop,
            hairWidth * 0.15,
            faceRadius * 1.5,
          ),
          hairPaint,
        );
        canvas.drawRect(
          Rect.fromLTWH(
            center.dx + hairWidth / 2 - hairWidth * 0.15,
            hairTop,
            hairWidth * 0.15,
            faceRadius * 1.5,
          ),
          hairPaint,
        );
        break;
      case AvatarHairStyle.long:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, hairTop),
            width: hairWidth,
            height: hairWidth,
          ),
          3.14, 3.14, true, hairPaint,
        );
        // Long side hair
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(
              center.dx - hairWidth / 2,
              hairTop - faceRadius * 0.2,
              hairWidth * 0.2,
              faceRadius * 2.5,
            ),
            const Radius.circular(8),
          ),
          hairPaint,
        );
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(
              center.dx + hairWidth / 2 - hairWidth * 0.2,
              hairTop - faceRadius * 0.2,
              hairWidth * 0.2,
              faceRadius * 2.5,
            ),
            const Radius.circular(8),
          ),
          hairPaint,
        );
        break;
      case AvatarHairStyle.afro:
        canvas.drawCircle(
          Offset(center.dx, hairTop + faceRadius * 0.1),
          faceRadius * 1.4,
          hairPaint,
        );
        break;
      case AvatarHairStyle.mohawk:
        final path = Path();
        path.moveTo(center.dx - faceRadius * 0.2, hairTop + faceRadius * 0.3);
        path.lineTo(center.dx, hairTop - faceRadius * 0.8);
        path.lineTo(center.dx + faceRadius * 0.2, hairTop + faceRadius * 0.3);
        path.close();
        canvas.drawPath(path, hairPaint);
        break;
      case AvatarHairStyle.curly:
        // Draw curly hair as multiple circles
        for (int i = 0; i < 8; i++) {
          final angle = 3.14 + (i / 7) * 3.14;
          final x = center.dx + (faceRadius * 0.9) * (angle - 3.14 - 1.57).abs() / 1.57 * (i < 4 ? -1 : 1);
          final y = hairTop + faceRadius * 0.1;
          canvas.drawCircle(
            Offset(x, y - (i % 2 == 0 ? 0 : faceRadius * 0.15)),
            faceRadius * 0.3,
            hairPaint,
          );
        }
        break;
      case AvatarHairStyle.spiky:
        for (int i = 0; i < 5; i++) {
          final spikeX = center.dx + (i - 2) * faceRadius * 0.4;
          final path = Path();
          path.moveTo(spikeX - faceRadius * 0.1, hairTop + faceRadius * 0.2);
          path.lineTo(spikeX, hairTop - faceRadius * 0.4 - (i == 2 ? faceRadius * 0.2 : 0));
          path.lineTo(spikeX + faceRadius * 0.1, hairTop + faceRadius * 0.2);
          path.close();
          canvas.drawPath(path, hairPaint);
        }
        break;
      case AvatarHairStyle.ponytail:
      case AvatarHairStyle.bun:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, hairTop),
            width: hairWidth,
            height: hairWidth * 0.8,
          ),
          3.14, 3.14, true, hairPaint,
        );
        // Bun/ponytail on top
        canvas.drawCircle(
          Offset(center.dx, hairTop - faceRadius * 0.4),
          faceRadius * 0.35,
          hairPaint,
        );
        break;
      case AvatarHairStyle.braids:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, hairTop),
            width: hairWidth,
            height: hairWidth * 0.8,
          ),
          3.14, 3.14, true, hairPaint,
        );
        // Braids on sides
        for (int side = -1; side <= 1; side += 2) {
          for (int i = 0; i < 4; i++) {
            canvas.drawOval(
              Rect.fromCenter(
                center: Offset(
                  center.dx + side * faceRadius * 0.9,
                  hairTop + i * faceRadius * 0.5,
                ),
                width: faceRadius * 0.25,
                height: faceRadius * 0.4,
              ),
              hairPaint,
            );
          }
        }
        break;
      default:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, hairTop),
            width: hairWidth,
            height: hairWidth * 0.8,
          ),
          3.14, 3.14, true, hairPaint,
        );
    }
  }

  void _drawEyes(Canvas canvas, Offset center, double faceRadius) {
    final eyeY = center.dy;
    final eyeSpacing = faceRadius * 0.4;
    final eyeRadius = faceRadius * 0.12;

    final eyeWhitePaint = Paint()
      ..color = Colors.white
      ..style = PaintingStyle.fill;

    final eyePaint = Paint()
      ..color = avatar.eyeColor.toColor()
      ..style = PaintingStyle.fill;

    final pupilPaint = Paint()
      ..color = Colors.black
      ..style = PaintingStyle.fill;

    // Left eye
    final leftEyeCenter = Offset(center.dx - eyeSpacing, eyeY);
    // Right eye
    final rightEyeCenter = Offset(center.dx + eyeSpacing, eyeY);

    switch (avatar.eyeStyle) {
      case AvatarEyeStyle.closed:
        final closedPaint = Paint()
          ..color = Colors.black
          ..style = PaintingStyle.stroke
          ..strokeWidth = 2;
        canvas.drawArc(
          Rect.fromCenter(center: leftEyeCenter, width: eyeRadius * 2, height: eyeRadius),
          0, 3.14, false, closedPaint,
        );
        canvas.drawArc(
          Rect.fromCenter(center: rightEyeCenter, width: eyeRadius * 2, height: eyeRadius),
          0, 3.14, false, closedPaint,
        );
        break;
      case AvatarEyeStyle.happy:
        final happyPaint = Paint()
          ..color = Colors.black
          ..style = PaintingStyle.stroke
          ..strokeWidth = 2;
        canvas.drawArc(
          Rect.fromCenter(center: leftEyeCenter, width: eyeRadius * 2, height: eyeRadius),
          3.14, 3.14, false, happyPaint,
        );
        canvas.drawArc(
          Rect.fromCenter(center: rightEyeCenter, width: eyeRadius * 2, height: eyeRadius),
          3.14, 3.14, false, happyPaint,
        );
        break;
      case AvatarEyeStyle.wink:
        // Left eye (closed/winking)
        final winkPaint = Paint()
          ..color = Colors.black
          ..style = PaintingStyle.stroke
          ..strokeWidth = 2;
        canvas.drawArc(
          Rect.fromCenter(center: leftEyeCenter, width: eyeRadius * 2, height: eyeRadius),
          0, 3.14, false, winkPaint,
        );
        // Right eye (open)
        canvas.drawCircle(rightEyeCenter, eyeRadius * 1.2, eyeWhitePaint);
        canvas.drawCircle(rightEyeCenter, eyeRadius * 0.8, eyePaint);
        canvas.drawCircle(rightEyeCenter, eyeRadius * 0.4, pupilPaint);
        break;
      case AvatarEyeStyle.hearts:
        final heartPaint = Paint()
          ..color = Colors.red
          ..style = PaintingStyle.fill;
        _drawHeart(canvas, leftEyeCenter, eyeRadius * 1.5, heartPaint);
        _drawHeart(canvas, rightEyeCenter, eyeRadius * 1.5, heartPaint);
        break;
      case AvatarEyeStyle.stars:
        final starPaint = Paint()
          ..color = Colors.amber
          ..style = PaintingStyle.fill;
        _drawStar(canvas, leftEyeCenter, eyeRadius * 1.5, starPaint);
        _drawStar(canvas, rightEyeCenter, eyeRadius * 1.5, starPaint);
        break;
      case AvatarEyeStyle.glasses:
      case AvatarEyeStyle.sunglasses:
        // Draw glasses frame
        final glassPaint = Paint()
          ..color = avatar.eyeStyle == AvatarEyeStyle.sunglasses ? Colors.black : Colors.brown
          ..style = PaintingStyle.stroke
          ..strokeWidth = 2;
        final glassFillPaint = Paint()
          ..color = avatar.eyeStyle == AvatarEyeStyle.sunglasses
              ? Colors.black.withValues(alpha: 0.7)
              : Colors.transparent
          ..style = PaintingStyle.fill;

        canvas.drawCircle(leftEyeCenter, eyeRadius * 2, glassFillPaint);
        canvas.drawCircle(rightEyeCenter, eyeRadius * 2, glassFillPaint);
        canvas.drawCircle(leftEyeCenter, eyeRadius * 2, glassPaint);
        canvas.drawCircle(rightEyeCenter, eyeRadius * 2, glassPaint);
        canvas.drawLine(
          Offset(leftEyeCenter.dx + eyeRadius * 2, leftEyeCenter.dy),
          Offset(rightEyeCenter.dx - eyeRadius * 2, rightEyeCenter.dy),
          glassPaint,
        );

        if (avatar.eyeStyle == AvatarEyeStyle.glasses) {
          // Draw eyes behind glasses
          canvas.drawCircle(leftEyeCenter, eyeRadius * 0.8, eyePaint);
          canvas.drawCircle(rightEyeCenter, eyeRadius * 0.8, eyePaint);
          canvas.drawCircle(leftEyeCenter, eyeRadius * 0.4, pupilPaint);
          canvas.drawCircle(rightEyeCenter, eyeRadius * 0.4, pupilPaint);
        }
        break;
      default:
        // Normal eyes
        canvas.drawCircle(leftEyeCenter, eyeRadius * 1.2, eyeWhitePaint);
        canvas.drawCircle(rightEyeCenter, eyeRadius * 1.2, eyeWhitePaint);
        canvas.drawCircle(leftEyeCenter, eyeRadius * 0.8, eyePaint);
        canvas.drawCircle(rightEyeCenter, eyeRadius * 0.8, eyePaint);
        canvas.drawCircle(leftEyeCenter, eyeRadius * 0.4, pupilPaint);
        canvas.drawCircle(rightEyeCenter, eyeRadius * 0.4, pupilPaint);
        // Highlight
        final highlightPaint = Paint()
          ..color = Colors.white
          ..style = PaintingStyle.fill;
        canvas.drawCircle(
          Offset(leftEyeCenter.dx - eyeRadius * 0.2, leftEyeCenter.dy - eyeRadius * 0.2),
          eyeRadius * 0.2,
          highlightPaint,
        );
        canvas.drawCircle(
          Offset(rightEyeCenter.dx - eyeRadius * 0.2, rightEyeCenter.dy - eyeRadius * 0.2),
          eyeRadius * 0.2,
          highlightPaint,
        );
    }
  }

  void _drawHeart(Canvas canvas, Offset center, double size, Paint paint) {
    final path = Path();
    path.moveTo(center.dx, center.dy + size * 0.3);
    path.cubicTo(
      center.dx - size, center.dy - size * 0.3,
      center.dx - size * 0.5, center.dy - size,
      center.dx, center.dy - size * 0.3,
    );
    path.cubicTo(
      center.dx + size * 0.5, center.dy - size,
      center.dx + size, center.dy - size * 0.3,
      center.dx, center.dy + size * 0.3,
    );
    canvas.drawPath(path, paint);
  }

  void _drawStar(Canvas canvas, Offset center, double size, Paint paint) {
    // Draw a simple filled star/circle
    canvas.drawCircle(center, size * 0.8, paint);
  }

  void _drawMouth(Canvas canvas, Offset center, double faceRadius) {
    final mouthY = center.dy + faceRadius * 0.5;
    final mouthWidth = faceRadius * 0.5;

    final mouthPaint = Paint()
      ..color = Colors.black
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2
      ..strokeCap = StrokeCap.round;

    final mouthFillPaint = Paint()
      ..color = const Color(0xFFCC6666)
      ..style = PaintingStyle.fill;

    switch (avatar.mouthStyle) {
      case AvatarMouthStyle.smile:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY - faceRadius * 0.1),
            width: mouthWidth,
            height: mouthWidth * 0.6,
          ),
          0, 3.14, false, mouthPaint,
        );
        break;
      case AvatarMouthStyle.grin:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY - faceRadius * 0.1),
            width: mouthWidth * 1.2,
            height: mouthWidth * 0.8,
          ),
          0, 3.14, true, mouthFillPaint,
        );
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY - faceRadius * 0.1),
            width: mouthWidth * 1.2,
            height: mouthWidth * 0.8,
          ),
          0, 3.14, false, mouthPaint,
        );
        break;
      case AvatarMouthStyle.neutral:
        canvas.drawLine(
          Offset(center.dx - mouthWidth / 2, mouthY),
          Offset(center.dx + mouthWidth / 2, mouthY),
          mouthPaint,
        );
        break;
      case AvatarMouthStyle.sad:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY + faceRadius * 0.15),
            width: mouthWidth,
            height: mouthWidth * 0.6,
          ),
          3.14, 3.14, false, mouthPaint,
        );
        break;
      case AvatarMouthStyle.surprised:
        canvas.drawOval(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY),
            width: mouthWidth * 0.5,
            height: mouthWidth * 0.7,
          ),
          mouthFillPaint,
        );
        canvas.drawOval(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY),
            width: mouthWidth * 0.5,
            height: mouthWidth * 0.7,
          ),
          mouthPaint,
        );
        break;
      case AvatarMouthStyle.tongue:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY - faceRadius * 0.05),
            width: mouthWidth,
            height: mouthWidth * 0.5,
          ),
          0, 3.14, false, mouthPaint,
        );
        // Tongue
        final tonguePaint = Paint()
          ..color = const Color(0xFFFF8888)
          ..style = PaintingStyle.fill;
        canvas.drawOval(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY + faceRadius * 0.08),
            width: mouthWidth * 0.4,
            height: mouthWidth * 0.3,
          ),
          tonguePaint,
        );
        break;
      case AvatarMouthStyle.teeth:
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY - faceRadius * 0.1),
            width: mouthWidth,
            height: mouthWidth * 0.6,
          ),
          0, 3.14, true, Paint()..color = Colors.white,
        );
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY - faceRadius * 0.1),
            width: mouthWidth,
            height: mouthWidth * 0.6,
          ),
          0, 3.14, false, mouthPaint,
        );
        // Teeth lines
        for (int i = 1; i < 4; i++) {
          canvas.drawLine(
            Offset(center.dx - mouthWidth / 2 + i * mouthWidth / 4, mouthY - faceRadius * 0.1),
            Offset(center.dx - mouthWidth / 2 + i * mouthWidth / 4, mouthY + faceRadius * 0.05),
            Paint()..color = Colors.grey.shade300..strokeWidth = 1,
          );
        }
        break;
      case AvatarMouthStyle.smirk:
        final path = Path();
        path.moveTo(center.dx - mouthWidth / 2, mouthY);
        path.quadraticBezierTo(
          center.dx, mouthY - faceRadius * 0.1,
          center.dx + mouthWidth / 2, mouthY - faceRadius * 0.15,
        );
        canvas.drawPath(path, mouthPaint);
        break;
    }
  }

  void _drawFacialHair(Canvas canvas, Offset center, double faceRadius) {
    final facialHairPaint = Paint()
      ..color = (avatar.facialHairColor ?? avatar.hairColor).toColor()
      ..style = PaintingStyle.fill;

    final mouthY = center.dy + faceRadius * 0.5;

    switch (avatar.facialHair) {
      case AvatarFacialHair.stubble:
        // Draw dots for stubble
        final stubblePaint = Paint()
          ..color = facialHairPaint.color.withValues(alpha: 0.5)
          ..style = PaintingStyle.fill;
        for (int i = 0; i < 20; i++) {
          final x = center.dx + (i % 5 - 2) * faceRadius * 0.15;
          final y = mouthY + (i ~/ 5) * faceRadius * 0.1;
          canvas.drawCircle(Offset(x, y), 1.5, stubblePaint);
        }
        break;
      case AvatarFacialHair.mustache:
        final path = Path();
        path.moveTo(center.dx - faceRadius * 0.4, mouthY - faceRadius * 0.15);
        path.quadraticBezierTo(
          center.dx - faceRadius * 0.2, mouthY - faceRadius * 0.25,
          center.dx, mouthY - faceRadius * 0.18,
        );
        path.quadraticBezierTo(
          center.dx + faceRadius * 0.2, mouthY - faceRadius * 0.25,
          center.dx + faceRadius * 0.4, mouthY - faceRadius * 0.15,
        );
        path.quadraticBezierTo(
          center.dx + faceRadius * 0.2, mouthY - faceRadius * 0.1,
          center.dx, mouthY - faceRadius * 0.12,
        );
        path.quadraticBezierTo(
          center.dx - faceRadius * 0.2, mouthY - faceRadius * 0.1,
          center.dx - faceRadius * 0.4, mouthY - faceRadius * 0.15,
        );
        canvas.drawPath(path, facialHairPaint);
        break;
      case AvatarFacialHair.goatee:
        canvas.drawOval(
          Rect.fromCenter(
            center: Offset(center.dx, mouthY + faceRadius * 0.3),
            width: faceRadius * 0.4,
            height: faceRadius * 0.5,
          ),
          facialHairPaint,
        );
        break;
      case AvatarFacialHair.beard:
        final path = Path();
        path.moveTo(center.dx - faceRadius * 0.6, mouthY - faceRadius * 0.2);
        path.quadraticBezierTo(
          center.dx - faceRadius * 0.7, mouthY + faceRadius * 0.3,
          center.dx, mouthY + faceRadius * 0.6,
        );
        path.quadraticBezierTo(
          center.dx + faceRadius * 0.7, mouthY + faceRadius * 0.3,
          center.dx + faceRadius * 0.6, mouthY - faceRadius * 0.2,
        );
        path.close();
        canvas.drawPath(path, facialHairPaint);
        break;
      case AvatarFacialHair.fullBeard:
        final path = Path();
        path.moveTo(center.dx - faceRadius * 0.8, center.dy);
        path.quadraticBezierTo(
          center.dx - faceRadius * 0.9, mouthY + faceRadius * 0.4,
          center.dx, mouthY + faceRadius * 0.8,
        );
        path.quadraticBezierTo(
          center.dx + faceRadius * 0.9, mouthY + faceRadius * 0.4,
          center.dx + faceRadius * 0.8, center.dy,
        );
        path.close();
        canvas.drawPath(path, facialHairPaint);
        break;
      default:
        break;
    }
  }

  void _drawAccessory(Canvas canvas, Offset center, double faceRadius, Size size) {
    switch (avatar.accessory) {
      case AvatarAccessory.hat:
        final hatPaint = Paint()
          ..color = (avatar.accessoryColor ?? '#000000').toColor()
          ..style = PaintingStyle.fill;
        // Hat brim
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromCenter(
              center: Offset(center.dx, center.dy - faceRadius * 0.9),
              width: faceRadius * 2.5,
              height: faceRadius * 0.2,
            ),
            const Radius.circular(4),
          ),
          hatPaint,
        );
        // Hat top
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(
              center.dx - faceRadius * 0.7,
              center.dy - faceRadius * 1.8,
              faceRadius * 1.4,
              faceRadius * 0.9,
            ),
            const Radius.circular(4),
          ),
          hatPaint,
        );
        break;
      case AvatarAccessory.cap:
        final capPaint = Paint()
          ..color = (avatar.accessoryColor ?? '#3B82F6').toColor()
          ..style = PaintingStyle.fill;
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, center.dy - faceRadius * 0.5),
            width: faceRadius * 2,
            height: faceRadius * 1.4,
          ),
          3.14, 3.14, true, capPaint,
        );
        // Bill
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(
              center.dx - faceRadius * 1.1,
              center.dy - faceRadius * 0.5,
              faceRadius * 0.9,
              faceRadius * 0.25,
            ),
            const Radius.circular(4),
          ),
          capPaint,
        );
        break;
      case AvatarAccessory.beanie:
        final beaniePaint = Paint()
          ..color = (avatar.accessoryColor ?? '#FF6B6B').toColor()
          ..style = PaintingStyle.fill;
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, center.dy - faceRadius * 0.4),
            width: faceRadius * 2,
            height: faceRadius * 1.6,
          ),
          3.14, 3.14, true, beaniePaint,
        );
        // Fold
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromCenter(
              center: Offset(center.dx, center.dy - faceRadius * 0.35),
              width: faceRadius * 2,
              height: faceRadius * 0.3,
            ),
            const Radius.circular(4),
          ),
          Paint()..color = beaniePaint.color.withValues(alpha: 0.7),
        );
        // Pom pom
        canvas.drawCircle(
          Offset(center.dx, center.dy - faceRadius * 1.2),
          faceRadius * 0.2,
          beaniePaint,
        );
        break;
      case AvatarAccessory.headband:
        final headbandPaint = Paint()
          ..color = (avatar.accessoryColor ?? '#FF69B4').toColor()
          ..style = PaintingStyle.fill;
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromCenter(
              center: Offset(center.dx, center.dy - faceRadius * 0.5),
              width: faceRadius * 2,
              height: faceRadius * 0.2,
            ),
            const Radius.circular(4),
          ),
          headbandPaint,
        );
        break;
      case AvatarAccessory.bow:
        final bowPaint = Paint()
          ..color = (avatar.accessoryColor ?? '#FF69B4').toColor()
          ..style = PaintingStyle.fill;
        // Left loop
        canvas.drawOval(
          Rect.fromCenter(
            center: Offset(center.dx - faceRadius * 0.15, center.dy - faceRadius * 0.8),
            width: faceRadius * 0.3,
            height: faceRadius * 0.2,
          ),
          bowPaint,
        );
        // Right loop
        canvas.drawOval(
          Rect.fromCenter(
            center: Offset(center.dx + faceRadius * 0.15, center.dy - faceRadius * 0.8),
            width: faceRadius * 0.3,
            height: faceRadius * 0.2,
          ),
          bowPaint,
        );
        // Center
        canvas.drawCircle(
          Offset(center.dx, center.dy - faceRadius * 0.8),
          faceRadius * 0.08,
          bowPaint,
        );
        break;
      case AvatarAccessory.earrings:
        final earringPaint = Paint()
          ..color = (avatar.accessoryColor ?? '#FFD700').toColor()
          ..style = PaintingStyle.fill;
        canvas.drawCircle(
          Offset(center.dx - faceRadius * 0.85, center.dy + faceRadius * 0.2),
          faceRadius * 0.08,
          earringPaint,
        );
        canvas.drawCircle(
          Offset(center.dx + faceRadius * 0.85, center.dy + faceRadius * 0.2),
          faceRadius * 0.08,
          earringPaint,
        );
        break;
      case AvatarAccessory.headphones:
        final headphonePaint = Paint()
          ..color = (avatar.accessoryColor ?? '#333333').toColor()
          ..style = PaintingStyle.fill;
        final headphoneStrokePaint = Paint()
          ..color = (avatar.accessoryColor ?? '#333333').toColor()
          ..style = PaintingStyle.stroke
          ..strokeWidth = faceRadius * 0.08;
        // Band
        canvas.drawArc(
          Rect.fromCenter(
            center: Offset(center.dx, center.dy - faceRadius * 0.2),
            width: faceRadius * 2.2,
            height: faceRadius * 1.8,
          ),
          3.14, 3.14, false, headphoneStrokePaint,
        );
        // Ear cups
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromCenter(
              center: Offset(center.dx - faceRadius * 1.0, center.dy),
              width: faceRadius * 0.35,
              height: faceRadius * 0.5,
            ),
            const Radius.circular(8),
          ),
          headphonePaint,
        );
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromCenter(
              center: Offset(center.dx + faceRadius * 1.0, center.dy),
              width: faceRadius * 0.35,
              height: faceRadius * 0.5,
            ),
            const Radius.circular(8),
          ),
          headphonePaint,
        );
        break;
      default:
        break;
    }
  }

  @override
  bool shouldRepaint(covariant _SimpleAvatarPainter oldDelegate) {
    return oldDelegate.avatar != avatar;
  }
}
