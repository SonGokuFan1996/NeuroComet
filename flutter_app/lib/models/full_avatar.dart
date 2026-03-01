import 'package:flutter/material.dart';
import 'package:json_annotation/json_annotation.dart';

part 'full_avatar.g.dart';

/// Full-body avatar model inspired by Xbox/Nintendo Mii style avatars
/// Supports extensive customization of body, face, clothing, and accessories
@JsonSerializable(explicitToJson: true)
class FullAvatar {
  final String id;
  final String name;
  final int schemaVersion;

  // Body
  final AvatarBodyMetrics bodyMetrics;
  final AvatarBodyType bodyType;
  final double height; // 0.0 to 1.0 scale
  final String skinTone;

  // Head/Face Shape
  final AvatarHeadShape headShape;
  final AvatarFaceShape faceShape;

  // Hair
  final AvatarHairStyle2 hairStyle;
  final String hairColor;
  final bool hairHighlights;
  final String? highlightColor;

  // Eyebrows
  final AvatarEyebrowStyle eyebrowStyle;
  final String eyebrowColor;
  final double eyebrowPosition; // -1 to 1 (lower to higher)

  // Eyes
  final AvatarEyeShape eyeShape;
  final String eyeColor;
  final double eyeSize; // 0.5 to 1.5
  final double eyeSpacing; // -1 to 1
  final AvatarEyelashStyle eyelashStyle;

  // Nose
  final AvatarNoseStyle noseStyle;
  final double noseSize; // 0.5 to 1.5

  // Mouth/Lips
  final AvatarMouthShape mouthShape;
  final String lipColor;
  final double mouthSize; // 0.5 to 1.5

  // Facial Features
  final AvatarFacialHair2? facialHair;
  final String? facialHairColor;
  final List<AvatarFacialFeature> facialFeatures; // freckles, moles, scars, etc.
  final double blushIntensity; // 0 to 1

  // Expression/Mood
  final AvatarExpression expression;
  final AvatarPose pose;
  final AvatarAnimationProfile animationProfile;

  // Clothing - Top
  final AvatarTopStyle topStyle;
  final String topPrimaryColor;
  final String? topSecondaryColor;
  final AvatarPattern? topPattern;

  // Clothing - Bottom
  final AvatarBottomStyle bottomStyle;
  final String bottomColor;
  final AvatarPattern? bottomPattern;

  // Clothing - Full outfit (overrides top/bottom)
  final AvatarOutfit? outfit;
  final String? outfitColor;

  // Footwear
  final AvatarFootwear footwear;
  final String footwearColor;

  // Accessories
  final AvatarHeadwear? headwear;
  final String? headwearColor;
  final AvatarEyewear? eyewear;
  final String? eyewearColor;
  final AvatarEarAccessory? earAccessory;
  final String? earAccessoryColor;
  final AvatarNeckAccessory? neckAccessory;
  final String? neckAccessoryColor;
  final AvatarHandAccessory? handAccessory;
  final String? handAccessoryColor;
  final AvatarBackAccessory? backAccessory;
  final String? backAccessoryColor;
  final List<AvatarLayer> layers;

  // Props (held items)
  final AvatarProp? prop;

  // Background for full avatar display
  final AvatarBackground background;
  final String? backgroundCustomColor;

  // Special effects
  final AvatarAura? aura;
  final String? auraColor;

  // Metadata
  final DateTime createdAt;
  final DateTime? updatedAt;
  final bool isPremium; // Some items may be premium-only
  final AvatarUnlockState unlockState;
  final List<String> tags;

    const FullAvatar({
    required this.id,
    this.name = 'My Avatar',
    this.schemaVersion = 1,
    this.bodyMetrics = const AvatarBodyMetrics(),
    this.bodyType = AvatarBodyType.average,
    this.height = 0.5,
    this.skinTone = '#EECEB3',
    this.headShape = AvatarHeadShape.oval,
    this.faceShape = AvatarFaceShape.round,
    this.hairStyle = AvatarHairStyle2.short,
    this.hairColor = '#3D2314',
    this.hairHighlights = false,
    this.highlightColor,
    this.eyebrowStyle = AvatarEyebrowStyle.natural,
    this.eyebrowColor = '#3D2314',
    this.eyebrowPosition = 0.0,
    this.eyeShape = AvatarEyeShape.almond,
    this.eyeColor = '#634E34',
    this.eyeSize = 1.0,
    this.eyeSpacing = 0.0,
    this.eyelashStyle = AvatarEyelashStyle.natural,
    this.noseStyle = AvatarNoseStyle.small,
    this.noseSize = 1.0,
    this.mouthShape = AvatarMouthShape.natural,
    this.lipColor = '#E8A0A0',
    this.mouthSize = 1.0,
    this.facialHair,
    this.facialHairColor,
    this.facialFeatures = const [],
    this.blushIntensity = 0.0,
    this.expression = AvatarExpression.happy,
    this.pose = AvatarPose.standing,
    this.animationProfile = AvatarAnimationProfile.idle,
    this.topStyle = AvatarTopStyle.tshirt,
    this.topPrimaryColor = '#4A90D9',
    this.topSecondaryColor,
    this.topPattern,
    this.bottomStyle = AvatarBottomStyle.jeans,
    this.bottomColor = '#2C3E50',
    this.bottomPattern,
    this.outfit,
    this.outfitColor,
    this.footwear = AvatarFootwear.sneakers,
    this.footwearColor = '#FFFFFF',
    this.headwear,
    this.headwearColor,
    this.eyewear,
    this.eyewearColor,
    this.earAccessory,
    this.earAccessoryColor,
    this.neckAccessory,
    this.neckAccessoryColor,
    this.handAccessory,
    this.handAccessoryColor,
    this.backAccessory,
    this.backAccessoryColor,
    this.layers = const [],
    this.prop,
    this.background = AvatarBackground.solid,
    this.backgroundCustomColor,
    this.aura,
    this.auraColor,
    required this.createdAt,
    this.updatedAt,
    this.isPremium = false,
    this.unlockState = AvatarUnlockState.unlocked,
    this.tags = const [],
    });

  factory FullAvatar.fromJson(Map<String, dynamic> json) =>
      _$FullAvatarFromJson(json);
  Map<String, dynamic> toJson() => _$FullAvatarToJson(this);

    FullAvatar copyWith({
    String? id,
    String? name,
    int? schemaVersion,
    AvatarBodyMetrics? bodyMetrics,
    AvatarBodyType? bodyType,
    double? height,
    String? skinTone,
    AvatarHeadShape? headShape,
    AvatarFaceShape? faceShape,
    AvatarHairStyle2? hairStyle,
    String? hairColor,
    bool? hairHighlights,
    String? highlightColor,
    AvatarEyebrowStyle? eyebrowStyle,
    String? eyebrowColor,
    double? eyebrowPosition,
    AvatarEyeShape? eyeShape,
    String? eyeColor,
    double? eyeSize,
    double? eyeSpacing,
    AvatarEyelashStyle? eyelashStyle,
    AvatarNoseStyle? noseStyle,
    double? noseSize,
    AvatarMouthShape? mouthShape,
    String? lipColor,
    double? mouthSize,
    AvatarFacialHair2? facialHair,
    String? facialHairColor,
    List<AvatarFacialFeature>? facialFeatures,
    double? blushIntensity,
    AvatarExpression? expression,
    AvatarPose? pose,
    AvatarAnimationProfile? animationProfile,
    AvatarTopStyle? topStyle,
    String? topPrimaryColor,
    String? topSecondaryColor,
    AvatarPattern? topPattern,
    AvatarBottomStyle? bottomStyle,
    String? bottomColor,
    AvatarPattern? bottomPattern,
    AvatarOutfit? outfit,
    String? outfitColor,
    AvatarFootwear? footwear,
    String? footwearColor,
    AvatarHeadwear? headwear,
    String? headwearColor,
    AvatarEyewear? eyewear,
    String? eyewearColor,
    AvatarEarAccessory? earAccessory,
    String? earAccessoryColor,
    AvatarNeckAccessory? neckAccessory,
    String? neckAccessoryColor,
    AvatarHandAccessory? handAccessory,
    String? handAccessoryColor,
    AvatarBackAccessory? backAccessory,
    String? backAccessoryColor,
    List<AvatarLayer>? layers,
    AvatarProp? prop,
    AvatarBackground? background,
    String? backgroundCustomColor,
    AvatarAura? aura,
    String? auraColor,
    DateTime? createdAt,
    DateTime? updatedAt,
    bool? isPremium,
    AvatarUnlockState? unlockState,
    List<String>? tags,
    }) {
    return FullAvatar(
      id: id ?? this.id,
      name: name ?? this.name,
      schemaVersion: schemaVersion ?? this.schemaVersion,
      bodyMetrics: bodyMetrics ?? this.bodyMetrics,
      bodyType: bodyType ?? this.bodyType,
      height: height ?? this.height,
      skinTone: skinTone ?? this.skinTone,
      headShape: headShape ?? this.headShape,
      faceShape: faceShape ?? this.faceShape,
      hairStyle: hairStyle ?? this.hairStyle,
      hairColor: hairColor ?? this.hairColor,
      hairHighlights: hairHighlights ?? this.hairHighlights,
      highlightColor: highlightColor ?? this.highlightColor,
      eyebrowStyle: eyebrowStyle ?? this.eyebrowStyle,
      eyebrowColor: eyebrowColor ?? this.eyebrowColor,
      eyebrowPosition: eyebrowPosition ?? this.eyebrowPosition,
      eyeShape: eyeShape ?? this.eyeShape,
      eyeColor: eyeColor ?? this.eyeColor,
      eyeSize: eyeSize ?? this.eyeSize,
      eyeSpacing: eyeSpacing ?? this.eyeSpacing,
      eyelashStyle: eyelashStyle ?? this.eyelashStyle,
      noseStyle: noseStyle ?? this.noseStyle,
      noseSize: noseSize ?? this.noseSize,
      mouthShape: mouthShape ?? this.mouthShape,
      lipColor: lipColor ?? this.lipColor,
      mouthSize: mouthSize ?? this.mouthSize,
      facialHair: facialHair ?? this.facialHair,
      facialHairColor: facialHairColor ?? this.facialHairColor,
      facialFeatures: facialFeatures ?? this.facialFeatures,
      blushIntensity: blushIntensity ?? this.blushIntensity,
      expression: expression ?? this.expression,
      pose: pose ?? this.pose,
      animationProfile: animationProfile ?? this.animationProfile,
      topStyle: topStyle ?? this.topStyle,
      topPrimaryColor: topPrimaryColor ?? this.topPrimaryColor,
      topSecondaryColor: topSecondaryColor ?? this.topSecondaryColor,
      topPattern: topPattern ?? this.topPattern,
      bottomStyle: bottomStyle ?? this.bottomStyle,
      bottomColor: bottomColor ?? this.bottomColor,
      bottomPattern: bottomPattern ?? this.bottomPattern,
      outfit: outfit ?? this.outfit,
      outfitColor: outfitColor ?? this.outfitColor,
      footwear: footwear ?? this.footwear,
      footwearColor: footwearColor ?? this.footwearColor,
      headwear: headwear ?? this.headwear,
      headwearColor: headwearColor ?? this.headwearColor,
      eyewear: eyewear ?? this.eyewear,
      eyewearColor: eyewearColor ?? this.eyewearColor,
      earAccessory: earAccessory ?? this.earAccessory,
      earAccessoryColor: earAccessoryColor ?? this.earAccessoryColor,
      neckAccessory: neckAccessory ?? this.neckAccessory,
      neckAccessoryColor: neckAccessoryColor ?? this.neckAccessoryColor,
      handAccessory: handAccessory ?? this.handAccessory,
      handAccessoryColor: handAccessoryColor ?? this.handAccessoryColor,
      backAccessory: backAccessory ?? this.backAccessory,
      backAccessoryColor: backAccessoryColor ?? this.backAccessoryColor,
      layers: layers ?? this.layers,
      prop: prop ?? this.prop,
      background: background ?? this.background,
      backgroundCustomColor: backgroundCustomColor ?? this.backgroundCustomColor,
      aura: aura ?? this.aura,
      auraColor: auraColor ?? this.auraColor,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? DateTime.now(),
      isPremium: isPremium ?? this.isPremium,
      unlockState: unlockState ?? this.unlockState,
      tags: tags ?? this.tags,
    );
    }

  /// Create a default avatar
  static FullAvatar createDefault() {
    return FullAvatar(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      createdAt: DateTime.now(),
    );
  }

  /// Create a randomized avatar
  static FullAvatar createRandom() {
    final random = DateTime.now().millisecondsSinceEpoch;
    final skinTones = AvatarSkinTones.tones;
    final hairColors = AvatarHairColors.colors;
    final eyeColors = AvatarEyeColors.colors;

    return FullAvatar(
      id: random.toString(),
      bodyType: AvatarBodyType.values[random % AvatarBodyType.values.length],
      height: (random % 100) / 100.0,
      skinTone: skinTones[(random ~/ 2) % skinTones.length],
      headShape: AvatarHeadShape.values[(random ~/ 3) % AvatarHeadShape.values.length],
      hairStyle: AvatarHairStyle2.values[(random ~/ 4) % AvatarHairStyle2.values.length],
      hairColor: hairColors[(random ~/ 5) % hairColors.length],
      eyeShape: AvatarEyeShape.values[(random ~/ 6) % AvatarEyeShape.values.length],
      eyeColor: eyeColors[(random ~/ 7) % eyeColors.length],
      mouthShape: AvatarMouthShape.values[(random ~/ 8) % AvatarMouthShape.values.length],
      expression: AvatarExpression.values[(random ~/ 9) % AvatarExpression.values.length],
      topStyle: AvatarTopStyle.values[(random ~/ 10) % AvatarTopStyle.values.length],
      topPrimaryColor: AvatarClothingColors.colors[(random ~/ 11) % AvatarClothingColors.colors.length],
      bottomStyle: AvatarBottomStyle.values[(random ~/ 12) % AvatarBottomStyle.values.length],
      bottomColor: AvatarClothingColors.colors[(random ~/ 13) % AvatarClothingColors.colors.length],
      createdAt: DateTime.now(),
    );
  }
}

@JsonSerializable()
class AvatarBodyMetrics {
  final double shoulderWidth;
  final double torsoLength;
  final double armLength;
  final double legLength;
  final double waistWidth;
  final double hipWidth;
  final double muscleTone;
  final double bodyFat;

  const AvatarBodyMetrics({
    this.shoulderWidth = 0.5,
    this.torsoLength = 0.5,
    this.armLength = 0.5,
    this.legLength = 0.5,
    this.waistWidth = 0.5,
    this.hipWidth = 0.5,
    this.muscleTone = 0.5,
    this.bodyFat = 0.5,
  });

  AvatarBodyMetrics copyWith({
    double? shoulderWidth,
    double? torsoLength,
    double? armLength,
    double? legLength,
    double? waistWidth,
    double? hipWidth,
    double? muscleTone,
    double? bodyFat,
  }) {
    return AvatarBodyMetrics(
      shoulderWidth: shoulderWidth ?? this.shoulderWidth,
      torsoLength: torsoLength ?? this.torsoLength,
      armLength: armLength ?? this.armLength,
      legLength: legLength ?? this.legLength,
      waistWidth: waistWidth ?? this.waistWidth,
      hipWidth: hipWidth ?? this.hipWidth,
      muscleTone: muscleTone ?? this.muscleTone,
      bodyFat: bodyFat ?? this.bodyFat,
    );
  }

  factory AvatarBodyMetrics.fromJson(Map<String, dynamic> json) =>
      _$AvatarBodyMetricsFromJson(json);
  Map<String, dynamic> toJson() => _$AvatarBodyMetricsToJson(this);
}

@JsonSerializable()
class AvatarLayer {
  final AvatarLayerType type;
  final String assetId;
  final int zIndex;
  final double opacity;
  final bool isTintable;
  final String? tintColor;

  const AvatarLayer({
    required this.type,
    required this.assetId,
    this.zIndex = 0,
    this.opacity = 1,
    this.isTintable = false,
    this.tintColor,
  });

  factory AvatarLayer.fromJson(Map<String, dynamic> json) =>
      _$AvatarLayerFromJson(json);
  Map<String, dynamic> toJson() => _$AvatarLayerToJson(this);
}

enum AvatarLayerType {
  base,
  body,
  face,
  hair,
  clothing,
  accessory,
  prop,
  effect,
  background,
}

enum AvatarAnimationProfile {
  idle,
  breathing,
  waving,
  dancing,
  running,
  sitting,
  floating,
  custom,
}

enum AvatarUnlockState {
  unlocked,
  locked,
  premium,
  eventExclusive,
}

// ============ Body Types ============

enum AvatarBodyType {
  slim,
  average,
  athletic,
  curvy,
  broad,
}

// ============ Head & Face ============

enum AvatarHeadShape {
  round,
  oval,
  square,
  heart,
  oblong,
  diamond,
}

enum AvatarFaceShape {
  round,
  oval,
  angular,
  soft,
}

// ============ Hair Styles ============

enum AvatarHairStyle2 {
  bald,
  buzzCut,
  crewCut,
  short,
  medium,
  long,
  veryLong,
  pixie,
  bob,
  lob,
  shag,
  layers,
  bangs,
  sidePart,
  middlePart,
  slickedBack,
  pompadour,
  undercut,
  mohawk,
  fauxHawk,
  spiky,
  messy,
  curlyShort,
  curlyMedium,
  curlyLong,
  wavyShort,
  wavyMedium,
  wavyLong,
  afro,
  afroShort,
  cornrows,
  braids,
  boxBraids,
  dreadlocks,
  twists,
  ponytail,
  highPonytail,
  lowPonytail,
  sidePonytail,
  pigtails,
  spaceBuns,
  bun,
  topKnot,
  messyBun,
  braidedBun,
  halfUp,
  halfUpBun,
}

// ============ Eyebrows ============

enum AvatarEyebrowStyle {
  natural,
  thick,
  thin,
  arched,
  straight,
  curved,
  angular,
  bushy,
  feathered,
  none,
}

// ============ Eyes ============

enum AvatarEyeShape {
  almond,
  round,
  hooded,
  monolid,
  downturned,
  upturned,
  wide,
  narrow,
  deepSet,
}

enum AvatarEyelashStyle {
  none,
  natural,
  long,
  dramatic,
  wispy,
  doll,
}

// ============ Nose ============

enum AvatarNoseStyle {
  small,
  medium,
  large,
  button,
  pointed,
  rounded,
  wide,
  narrow,
  roman,
  snub,
}

// ============ Mouth ============

enum AvatarMouthShape {
  natural,
  full,
  thin,
  wide,
  small,
  hearted,
  downturned,
  upturned,
}

// ============ Facial Hair ============

enum AvatarFacialHair2 {
  none,
  stubble,
  goatee,
  soulPatch,
  mustache,
  handlebar,
  beardShort,
  beardMedium,
  beardLong,
  beardFull,
  mutton,
  vandyke,
}

// ============ Facial Features ============

enum AvatarFacialFeature {
  freckles,
  frecklesLight,
  moleLeft,
  moleRight,
  beautyMark,
  dimples,
  scarLeft,
  scarRight,
  scarChin,
  birthmark,
  wrinkles,
  crowsFeet,
}

// ============ Expression & Pose ============

enum AvatarExpression {
  neutral,
  happy,
  excited,
  laughing,
  wink,
  smirk,
  confident,
  cool,
  surprised,
  shocked,
  thinking,
  confused,
  sad,
  angry,
  determined,
  sleepy,
  silly,
  love,
  starryEyed,
  crying,
}

enum AvatarPose {
  standing,
  standingRelaxed,
  handOnHip,
  handsOnHips,
  armsCrossed,
  waving,
  peace,
  thumbsUp,
  pointing,
  thinking,
  shrug,
  celebration,
  jumping,
  sitting,
  lounging,
  dancing,
  actionPose,
  heroic,
}

// ============ Clothing - Tops ============

enum AvatarTopStyle {
  none,
  tshirt,
  tshirtVneck,
  tankTop,
  cropTop,
  polo,
  buttonUp,
  blouse,
  sweater,
  hoodie,
  hoodieZip,
  cardigan,
  jacket,
  leatherJacket,
  denimJacket,
  blazer,
  vest,
  turtleneck,
  offShoulder,
  longSleeve,
  jerseyAthletic,
  jerseyBaseball,
}

enum AvatarBottomStyle {
  jeans,
  jeansSkinny,
  jeansWide,
  jeansShorts,
  chinos,
  slacks,
  shorts,
  cargoShorts,
  cargoPants,
  joggers,
  sweatpants,
  leggings,
  skirtMini,
  skirtMidi,
  skirtMaxi,
  skirtPleated,
}

enum AvatarOutfit {
  dress,
  dressLong,
  dressCocktail,
  jumpsuit,
  romper,
  overalls,
  suit,
  suitCasual,
  uniform,
  athletic,
  swimsuit,
  pajamas,
  onesie,
  costumeSuperhero,
  costumePrincess,
  costumePirate,
  costumeNinja,
  costumeAstronaut,
}

enum AvatarPattern {
  solid,
  stripes,
  stripesHorizontal,
  plaid,
  polkaDots,
  floral,
  geometric,
  camo,
  tieDye,
  gradient,
  colorBlock,
  graphic,
  logoSmall,
  logoLarge,
}

// ============ Footwear ============

enum AvatarFootwear {
  barefoot,
  sneakers,
  sneakersHighTop,
  runningShoes,
  loafers,
  oxfords,
  boots,
  bootsAnkle,
  bootsKnee,
  heels,
  heelsHigh,
  wedges,
  sandals,
  flipFlops,
  slippers,
}

// ============ Accessories ============

enum AvatarHeadwear {
  none,
  cap,
  capBackward,
  beanie,
  fedora,
  sunHat,
  bucket,
  beret,
  headband,
  headbandAthletic,
  bandana,
  bow,
  bowLarge,
  hairClips,
  crown,
  tiara,
  partyHat,
  antennaBoppers,
  catEars,
  bunnyEars,
  devilHorns,
  halo,
  flowers,
  helmet,
}

enum AvatarEyewear {
  none,
  glasses,
  glassesRound,
  glassesSquare,
  glassesCatEye,
  glassesAviator,
  sunglasses,
  sunglassesAviator,
  sunglassesSport,
  sunglassesOversized,
  monocle,
  eyepatch,
  mask,
  maskMasquerade,
}

enum AvatarEarAccessory {
  none,
  studsSmall,
  studsMedium,
  hoopsSmall,
  hoopsMedium,
  hoopsLarge,
  dangles,
  cuffs,
  plugs,
  clipOns,
}

enum AvatarNeckAccessory {
  none,
  necklaceSimple,
  necklaceChain,
  necklaceChunky,
  necklacePendant,
  necklaceChoker,
  tie,
  tieSkinny,
  bowtie,
  scarf,
  scarfWinter,
  bandana,
  headphones,
}

enum AvatarHandAccessory {
  none,
  watch,
  watchSmart,
  bracelet,
  bracelets,
  bangles,
  ring,
  rings,
  gloves,
  wristband,
}

enum AvatarBackAccessory {
  none,
  backpack,
  backpackSmall,
  messenger,
  purse,
  duffel,
  wings,
  wingsAngel,
  wingsBat,
  wingsFairy,
  cape,
  capeSuperHero,
}

// ============ Props ============

enum AvatarProp {
  none,
  phone,
  tablet,
  laptop,
  book,
  coffee,
  soda,
  balloon,
  balloons,
  flower,
  bouquet,
  flag,
  sign,
  sword,
  shield,
  wand,
  staff,
  guitar,
  microphone,
  basketball,
  football,
  soccer,
  baseball,
  skateboard,
  camera,
  paintbrush,
  pencil,
  gameController,
  trophy,
  medal,
  heart,
  star,
  sparkler,
  umbrella,
  petDog,
  petCat,
  petBird,
  petFish,
}

// ============ Background & Effects ============

enum AvatarBackground {
  none,
  solid,
  gradient,
  pattern,
  sceneBeach,
  sceneMountains,
  sceneCity,
  scenePark,
  sceneSpace,
  sceneClouds,
  sceneRainbow,
  sceneStars,
  sceneHearts,
  sceneConfetti,
  sceneGaming,
  sceneSports,
  sceneMusic,
  sceneArt,
}

enum AvatarAura {
  none,
  glow,
  sparkle,
  flames,
  lightning,
  bubbles,
  hearts,
  stars,
  music,
  rainbow,
  cosmic,
  nature,
}

// ============ Color Palettes ============

class AvatarSkinTones {
  static const List<String> tones = [
    '#FFF5E6', // Very light
    '#FFDFC4', // Light
    '#F0D5BE', // Fair
    '#EECEB3', // Light medium
    '#E0B69D', // Medium
    '#D4A574', // Olive
    '#C68642', // Tan
    '#A66D3C', // Medium brown
    '#8D5524', // Brown
    '#6B4423', // Dark brown
    '#5C3A21', // Very dark brown
    '#4A2912', // Deep brown
  ];
}

class AvatarHairColors {
  static const List<String> colors = [
    '#000000', // Black
    '#1C1C1C', // Soft black
    '#3D2314', // Dark brown
    '#5A3825', // Medium brown
    '#6B4423', // Brown
    '#8B5E3C', // Light brown
    '#A67B5B', // Caramel
    '#D4A574', // Honey
    '#E5C29F', // Blonde
    '#F5DEB3', // Light blonde
    '#FFDD89', // Platinum
    '#FFFACD', // White blonde
    '#B55239', // Auburn
    '#8B4513', // Chestnut
    '#FF6B35', // Ginger
    '#FF4500', // Bright red
    '#8B0000', // Dark red
    '#4A0000', // Burgundy
    '#808080', // Gray
    '#C0C0C0', // Silver
    '#FFFFFF', // White
    '#FF69B4', // Pink
    '#FF1493', // Hot pink
    '#FF00FF', // Magenta
    '#9400D3', // Violet
    '#7C4DFF', // Purple
    '#4169E1', // Royal blue
    '#00BFFF', // Sky blue
    '#00CED1', // Cyan
    '#00BFA5', // Teal
    '#32CD32', // Lime
    '#228B22', // Forest green
  ];
}

class AvatarEyeColors {
  static const List<String> colors = [
    '#634E34', // Brown
    '#3D2314', // Dark brown
    '#8B4513', // Hazel/Amber
    '#1C7847', // Green
    '#228B22', // Forest green
    '#6082B6', // Blue gray
    '#4169E1', // Blue
    '#1E90FF', // Bright blue
    '#87CEEB', // Light blue
    '#808080', // Gray
    '#A9A9A9', // Light gray
    '#000000', // Black
    '#7C4DFF', // Purple
    '#FF69B4', // Pink
    '#FF0000', // Red
    '#FFD700', // Gold
  ];
}

class AvatarClothingColors {
  static const List<String> colors = [
    '#FFFFFF', // White
    '#F5F5F5', // Off-white
    '#D3D3D3', // Light gray
    '#808080', // Gray
    '#505050', // Dark gray
    '#2C2C2C', // Charcoal
    '#000000', // Black
    '#FFF8DC', // Cream
    '#F5DEB3', // Beige
    '#D2B48C', // Tan
    '#8B4513', // Brown
    '#4A2912', // Dark brown
    '#8B0000', // Maroon
    '#DC143C', // Crimson
    '#FF0000', // Red
    '#FF6347', // Tomato
    '#FF4500', // Orange red
    '#FF8C00', // Dark orange
    '#FFA500', // Orange
    '#FFD700', // Gold
    '#FFFF00', // Yellow
    '#F0E68C', // Khaki
    '#9ACD32', // Yellow green
    '#32CD32', // Lime
    '#228B22', // Forest green
    '#006400', // Dark green
    '#008080', // Teal
    '#00CED1', // Cyan
    '#00BFFF', // Sky blue
    '#1E90FF', // Dodger blue
    '#4169E1', // Royal blue
    '#000080', // Navy
    '#4B0082', // Indigo
    '#7C4DFF', // Purple
    '#9400D3', // Violet
    '#FF00FF', // Magenta
    '#FF69B4', // Pink
    '#FFB6C1', // Light pink
    '#DDA0DD', // Plum
  ];
}

class AvatarLipColors {
  static const List<String> colors = [
    '#E8A0A0', // Natural pink
    '#D4847C', // Dusty rose
    '#CC7777', // Rose
    '#C0666F', // Mauve
    '#B85555', // Berry
    '#A04050', // Wine
    '#8B3040', // Burgundy
    '#FF6B6B', // Coral
    '#FF4444', // Red
    '#CC0000', // Deep red
    '#800020', // Oxblood
    '#FF69B4', // Hot pink
    '#FF1493', // Deep pink
    '#C71585', // Magenta
    '#8B4513', // Brown nude
    '#A67B5B', // Nude
    '#DEB887', // Light nude
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

