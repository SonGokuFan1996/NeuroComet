import 'package:flutter/material.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_avatar.g.dart';

/// Model representing a custom-made avatar
@JsonSerializable()
class CustomAvatar {
  final String id;
  final AvatarShape shape;
  final String backgroundColor;
  final String skinColor;
  final AvatarHairStyle hairStyle;
  final String hairColor;
  final AvatarEyeStyle eyeStyle;
  final String eyeColor;
  final AvatarMouthStyle mouthStyle;
  final AvatarAccessory? accessory;
  final String? accessoryColor;
  final AvatarFacialHair? facialHair;
  final String? facialHairColor;
  final DateTime createdAt;

  const CustomAvatar({
    required this.id,
    this.shape = AvatarShape.circle,
    this.backgroundColor = '#7C4DFF',
    this.skinColor = '#FFDAB9',
    this.hairStyle = AvatarHairStyle.short,
    this.hairColor = '#3D2314',
    this.eyeStyle = AvatarEyeStyle.normal,
    this.eyeColor = '#634E34',
    this.mouthStyle = AvatarMouthStyle.smile,
    this.accessory,
    this.accessoryColor,
    this.facialHair,
    this.facialHairColor,
    required this.createdAt,
  });

  factory CustomAvatar.fromJson(Map<String, dynamic> json) =>
      _$CustomAvatarFromJson(json);
  Map<String, dynamic> toJson() => _$CustomAvatarToJson(this);

  CustomAvatar copyWith({
    String? id,
    AvatarShape? shape,
    String? backgroundColor,
    String? skinColor,
    AvatarHairStyle? hairStyle,
    String? hairColor,
    AvatarEyeStyle? eyeStyle,
    String? eyeColor,
    AvatarMouthStyle? mouthStyle,
    AvatarAccessory? accessory,
    String? accessoryColor,
    AvatarFacialHair? facialHair,
    String? facialHairColor,
    DateTime? createdAt,
  }) {
    return CustomAvatar(
      id: id ?? this.id,
      shape: shape ?? this.shape,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      skinColor: skinColor ?? this.skinColor,
      hairStyle: hairStyle ?? this.hairStyle,
      hairColor: hairColor ?? this.hairColor,
      eyeStyle: eyeStyle ?? this.eyeStyle,
      eyeColor: eyeColor ?? this.eyeColor,
      mouthStyle: mouthStyle ?? this.mouthStyle,
      accessory: accessory ?? this.accessory,
      accessoryColor: accessoryColor ?? this.accessoryColor,
      facialHair: facialHair ?? this.facialHair,
      facialHairColor: facialHairColor ?? this.facialHairColor,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  /// Generate a default avatar
  static CustomAvatar createDefault() {
    return CustomAvatar(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      createdAt: DateTime.now(),
    );
  }
}

/// Avatar background shape
enum AvatarShape {
  circle,
  rounded,
  square,
}

/// Available hair styles
enum AvatarHairStyle {
  none,
  short,
  medium,
  long,
  curly,
  wavy,
  buzz,
  ponytail,
  bun,
  mohawk,
  afro,
  spiky,
  braids,
}

/// Available eye styles
enum AvatarEyeStyle {
  normal,
  happy,
  sleepy,
  wink,
  surprised,
  hearts,
  stars,
  glasses,
  sunglasses,
  closed,
}

/// Available mouth styles
enum AvatarMouthStyle {
  smile,
  grin,
  neutral,
  sad,
  surprised,
  tongue,
  teeth,
  smirk,
}

/// Available accessories
enum AvatarAccessory {
  none,
  glasses,
  sunglasses,
  hat,
  cap,
  beanie,
  headband,
  bow,
  earrings,
  headphones,
}

/// Available facial hair options
enum AvatarFacialHair {
  none,
  stubble,
  mustache,
  goatee,
  beard,
  fullBeard,
}

/// Predefined skin tone colors
class AvatarSkinTones {
  static const List<String> tones = [
    '#FFDFC4', // Light
    '#F0D5BE', // Fair
    '#EECEB3', // Medium light
    '#E0B69D', // Medium
    '#D4A574', // Olive
    '#C68642', // Tan
    '#8D5524', // Brown
    '#5C3A21', // Dark brown
  ];
}

/// Predefined hair colors
class AvatarHairColors {
  static const List<String> colors = [
    '#000000', // Black
    '#3D2314', // Dark brown
    '#6B4423', // Brown
    '#A67B5B', // Light brown
    '#E5C29F', // Blonde
    '#FFDD89', // Light blonde
    '#B55239', // Auburn
    '#FF6B35', // Ginger
    '#8B0000', // Dark red
    '#808080', // Gray
    '#FFFFFF', // White
    '#FF69B4', // Pink
    '#7C4DFF', // Purple
    '#00BFA5', // Teal
    '#3B82F6', // Blue
  ];
}

/// Predefined eye colors
class AvatarEyeColors {
  static const List<String> colors = [
    '#634E34', // Brown
    '#3D2314', // Dark brown
    '#1C7847', // Green
    '#6082B6', // Blue gray
    '#1E90FF', // Blue
    '#808080', // Gray
    '#8B4513', // Hazel
    '#7C4DFF', // Purple
    '#000000', // Black
  ];
}

/// Predefined background colors
class AvatarBackgroundColors {
  static const List<String> colors = [
    '#7C4DFF', // Primary purple
    '#00BFA5', // Teal
    '#FF6E40', // Orange
    '#64B5F6', // Calm blue
    '#81C784', // Calm green
    '#F48FB1', // Calm pink
    '#FFD54F', // Calm yellow
    '#CE93D8', // Calm lavender
    '#FF7043', // ADHD color
    '#42A5F5', // Autism color
    '#66BB6A', // Dyslexia color
    '#AB47BC', // Anxiety color
    '#E0E0E0', // Light gray
    '#424242', // Dark gray
  ];
}

/// Extension to convert hex strings to colors
extension HexColorExtension on String {
  Color toColor() {
    String hex = replaceFirst('#', '');
    if (hex.length == 6) {
      hex = 'FF$hex';
    }
    return Color(int.parse(hex, radix: 16));
  }
}

/// Extension to convert colors to hex strings
extension ColorHexExtension on Color {
  String toHexString() {
    return '#${(value & 0xFFFFFF).toRadixString(16).padLeft(6, '0').toUpperCase()}';
  }
}
