# Design Specification — Glassmorphism Dark Theme

Visual design system for the CarrierConfig Override Manager (`cco-app`).

---

## 🎨 Design Philosophy

### Core Principles

**1. Glassmorphism Dark Aesthetic**
- Frosted glass surfaces with blur effects
- Subtle transparency layering
- Depth through elevation and shadows
- Vibrant accent colors on dark backgrounds
- Premium, modern, and technical feel

**2. Clarity & Readability**
- High contrast text on glass surfaces
- Clear information hierarchy
- Uncluttered layouts
- Purpose-driven animations

**3. Technical Sophistication**
- Suitable for advanced/power users
- Detailed information display
- Professional diagnostic tool aesthetics
- Root/modding community appeal

**4. Accessibility**
- Readable text sizes (16sp+ for body)
- Sufficient contrast ratios (WCAG AA)
- Touch targets ≥48dp
- Screen reader support

---

## 🌈 Color Palette

### Background Colors

```kotlin
// Primary backgrounds
val BackgroundDeepDark = Color(0xFF0A0E14)        // Main app background
val BackgroundDark = Color(0xFF12161E)            // Secondary background
val BackgroundElevated = Color(0xFF1A1F2B)        // Elevated surfaces

// Glass surface overlays (with alpha)
val GlassSurface = Color(0x1AFFFFFF)              // 10% white - subtle glass
val GlassSurfaceMedium = Color(0x33FFFFFF)        // 20% white - medium glass
val GlassSurfaceStrong = Color(0x4DFFFFFF)        // 30% white - strong glass

// Gradient overlays
val GradientTop = Color(0x0D1B2E)                 // Deep blue-black
val GradientBottom = Color(0x1A0B1E)              // Deep purple-black
```

### Accent Colors

```kotlin
// Primary accent (cyan/electric blue)
val AccentPrimary = Color(0xFF00D9FF)             // Bright cyan
val AccentPrimaryLight = Color(0xFF6FEFFF)        // Light cyan
val AccentPrimaryDark = Color(0xFF0099CC)         // Dark cyan
val AccentPrimaryGlow = Color(0x4D00D9FF)         // Cyan glow (30% alpha)

// Secondary accent (purple/magenta)
val AccentSecondary = Color(0xFFB24BF3)           // Vibrant purple
val AccentSecondaryLight = Color(0xFFD896FF)      // Light purple
val AccentSecondaryDark = Color(0xFF8B2FC9)       // Dark purple
val AccentSecondaryGlow = Color(0x4DB24BF3)       // Purple glow (30% alpha)

// Tertiary accent (green/success)
val AccentSuccess = Color(0xFF00FF88)             // Neon green
val AccentSuccessGlow = Color(0x4D00FF88)         // Green glow

// Warning accent (amber)
val AccentWarning = Color(0xFFFFB020)             // Warm amber
val AccentWarningGlow = Color(0x4DFFB020)         // Amber glow

// Error accent (red/pink)
val AccentError = Color(0xFFFF3366)               // Bright red-pink
val AccentErrorGlow = Color(0x4DFF3366)           // Red glow
```

### Text Colors

```kotlin
// Text hierarchy
val TextPrimary = Color(0xFFFFFFFF)               // Pure white - primary text
val TextSecondary = Color(0xCCFFFFFF)             // 80% white - secondary text
val TextTertiary = Color(0x99FFFFFF)              // 60% white - tertiary text
val TextDisabled = Color(0x66FFFFFF)              // 40% white - disabled text

// Accent text
val TextAccent = Color(0xFF00D9FF)                // Cyan accent text
val TextSuccess = Color(0xFF00FF88)               // Success text
val TextWarning = Color(0xFFFFB020)               // Warning text
val TextError = Color(0xFFFF3366)                 // Error text
```

### Semantic Colors

```kotlin
// Status indicators
val StatusSuccess = Color(0xFF00FF88)
val StatusWarning = Color(0xFFFFB020)
val StatusError = Color(0xFFFF3366)
val StatusInfo = Color(0xFF00D9FF)
val StatusInactive = Color(0xFF666E7F)

// Background variations for status
val StatusSuccessBg = Color(0x1A00FF88)           // 10% success
val StatusWarningBg = Color(0x1AFFB020)           // 10% warning
val StatusErrorBg = Color(0x1AFF3366)             // 10% error
val StatusInfoBg = Color(0x1A00D9FF)              // 10% info
```

---

## 🔠 Typography

### Font Family

**Primary:** **Inter** or **SF Pro** (system default)
- Clean, modern, highly readable
- Excellent hinting for digital displays
- Wide range of weights

**Monospace:** **JetBrains Mono** or **Fira Code**
- For code snippets, keys, technical values
- Clear distinction between similar characters

### Type Scale

```kotlin
// Display
val DisplayLarge = TextStyle(
    fontSize = 57.sp,
    lineHeight = 64.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = (-0.25).sp
)

val DisplayMedium = TextStyle(
    fontSize = 45.sp,
    lineHeight = 52.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.sp
)

val DisplaySmall = TextStyle(
    fontSize = 36.sp,
    lineHeight = 44.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.sp
)

// Headline
val HeadlineLarge = TextStyle(
    fontSize = 32.sp,
    lineHeight = 40.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp
)

val HeadlineMedium = TextStyle(
    fontSize = 28.sp,
    lineHeight = 36.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp
)

val HeadlineSmall = TextStyle(
    fontSize = 24.sp,
    lineHeight = 32.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp
)

// Title
val TitleLarge = TextStyle(
    fontSize = 22.sp,
    lineHeight = 28.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.sp
)

val TitleMedium = TextStyle(
    fontSize = 16.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.15.sp
)

val TitleSmall = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.1.sp
)

// Body
val BodyLarge = TextStyle(
    fontSize = 16.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.5.sp
)

val BodyMedium = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.25.sp
)

val BodySmall = TextStyle(
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.4.sp
)

// Label
val LabelLarge = TextStyle(
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.1.sp
)

val LabelMedium = TextStyle(
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.5.sp
)

val LabelSmall = TextStyle(
    fontSize = 11.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.5.sp
)

// Monospace (for technical content)
val Monospace = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp
)
```

---

## 📐 Spacing & Layout

### Spacing Scale

```kotlin
val SpaceXXS = 2.dp
val SpaceXS = 4.dp
val SpaceSM = 8.dp
val SpaceMD = 12.dp
val SpaceLG = 16.dp
val SpaceXL = 24.dp
val Space2XL = 32.dp
val Space3XL = 48.dp
val Space4XL = 64.dp
```

### Border Radius

```kotlin
val RadiusXS = 4.dp
val RadiusSM = 8.dp
val RadiusMD = 12.dp
val RadiusLG = 16.dp
val RadiusXL = 20.dp
val Radius2XL = 24.dp
val RadiusFull = 9999.dp      // Fully rounded
```

### Elevation (Z-axis)

```kotlin
val ElevationNone = 0.dp
val ElevationXS = 2.dp
val ElevationSM = 4.dp
val ElevationMD = 8.dp
val ElevationLG = 12.dp
val ElevationXL = 16.dp
val Elevation2XL = 24.dp
```

---

## 🎭 Glassmorphism Effects

### Blur Strength

Use native blur APIs when available (Android 12+):
- **Light blur:** 8-12dp
- **Medium blur:** 16-24dp
- **Strong blur:** 32-48dp

Fallback for older Android: use semi-transparent overlays without blur.

### Glass Surface Recipe

A typical glass surface combines:

1. **Background:** Semi-transparent white/light color
2. **Blur:** Background blur effect
3. **Border:** Subtle bright border (1dp)
4. **Shadow:** Soft shadow for depth
5. **Content:** High-contrast text/icons

### Implementation Example (Jetpack Compose)

```kotlin
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blur: Dp = 24.dp,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = GlassSurface,
                shape = RoundedCornerShape(RadiusLG)
            )
            .blur(radius = blur) // Requires Android 12+
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(RadiusLG)
            )
            .padding(SpaceLG)
    ) {
        content()
    }
}
```

### Shadow Configuration

```kotlin
// Soft glow shadow for glass surfaces
val GlassShadow = Shadow(
    color = AccentPrimaryGlow,
    offset = Offset(0f, 4f),
    blurRadius = 16f
)

// Elevation shadow
val ElevationShadow = Shadow(
    color = Color.Black.copy(alpha = 0.3f),
    offset = Offset(0f, 8f),
    blurRadius = 24f
)
```

---

## 🧩 Component Specifications

### GlassmorphicCard

**Usage:** Container for related information (device info, SIM info, etc.)

**Specs:**
- Background: `GlassSurface` (10% white)
- Blur: 24dp
- Border: 1dp, `Color.White.copy(alpha = 0.2f)`
- Border radius: `RadiusLG` (16dp)
- Padding: `SpaceLG` (16dp)
- Elevation: `ElevationSM` (4dp shadow)

**Variants:**
- **Elevated:** Stronger glass (`GlassSurfaceMedium`), larger elevation
- **Compact:** Smaller padding (`SpaceMD`)
- **Accent:** Border with accent color glow

### GlassButton

**Primary Button**

**Specs:**
- Background: `LinearGradient(AccentPrimary, AccentPrimaryDark)`
- Border radius: `RadiusMD` (12dp)
- Padding: horizontal `SpaceXL` (24dp), vertical `SpaceMD` (12dp)
- Text: `LabelLarge`, `TextPrimary`
- Shadow: `AccentPrimaryGlow` with blur 16dp
- Height: 48dp minimum

**State variations:**
- **Hover:** Brighter gradient
- **Pressed:** Darker, scale 0.95
- **Disabled:** Gray gradient, 40% opacity

**Secondary Button**

**Specs:**
- Background: `GlassSurface` (transparent glass)
- Border: 1.5dp, `AccentPrimary`
- Border radius: `RadiusMD`
- Text: `LabelLarge`, `AccentPrimary`
- Same padding as primary

**Outlined Button (Tertiary)**

**Specs:**
- Background: Transparent
- Border: 1dp, `TextSecondary`
- Border radius: `RadiusMD`
- Text: `LabelLarge`, `TextSecondary`

### GlassTextField

**Specs:**
- Background: `GlassSurface`
- Border: 1dp, `Color.White.copy(alpha = 0.3f)`
- Border radius: `RadiusMD`
- Padding: `SpaceMD` horizontal, `SpaceSM` vertical
- Text: `BodyLarge`, `TextPrimary`
- Placeholder: `TextTertiary`
- Cursor: `AccentPrimary`

**Focused state:**
- Border: 2dp, `AccentPrimary`
- Glow: `AccentPrimaryGlow` shadow

**Error state:**
- Border: 2dp, `AccentError`
- Glow: `AccentErrorGlow` shadow

### StatusChip

**Specs:**
- Background: Status color at 10% alpha (e.g., `StatusSuccessBg`)
- Border: 1dp, status color at 50% alpha
- Border radius: `RadiusFull` (pill shape)
- Padding: horizontal `SpaceMD`, vertical `SpaceXS`
- Text: `LabelSmall`, status color
- Icon: 16dp, status color (optional)

**Variants:**
- **Success:** Green colors
- **Warning:** Amber colors
- **Error:** Red colors
- **Info:** Cyan colors
- **Inactive:** Gray colors

### InfoPanel

**Usage:** Display key-value information pairs

**Specs:**
- Background: `GlassSurface`
- Blur: 20dp
- Border: 1dp, `Color.White.copy(alpha = 0.15f)`
- Border radius: `RadiusMD`
- Padding: `SpaceLG`
- Row spacing: `SpaceSM`

**Content layout:**
- Key: `LabelMedium`, `TextSecondary`
- Value: `BodyLarge`, `TextPrimary`
- Divider: 1dp, `Color.White.copy(alpha = 0.1f)` between rows

### Toggle Switch

**Specs:**
- Track width: 52dp
- Track height: 32dp
- Track radius: `RadiusFull`
- Thumb size: 28dp circle
- Track color (off): `GlassSurface` with border
- Track color (on): `LinearGradient(AccentPrimary, AccentPrimaryDark)`
- Thumb color: `Color.White`
- Thumb shadow: Soft shadow
- Glow (on): `AccentPrimaryGlow`

### Dropdown Menu

**Specs:**
- Background: `BackgroundElevated` with `GlassSurfaceMedium` overlay
- Blur: 32dp
- Border: 1dp, `Color.White.copy(alpha = 0.3f)`
- Border radius: `RadiusMD`
- Elevation: `ElevationXL`
- Item padding: `SpaceMD` horizontal, `SpaceSM` vertical
- Item height: 48dp
- Item hover: `GlassSurface` background
- Divider: 1dp, `Color.White.copy(alpha = 0.1f)`

### NavigationBar (Bottom)

**Specs:**
- Background: `BackgroundDark` with `GlassSurfaceMedium` overlay
- Blur: 24dp
- Border top: 1dp, `Color.White.copy(alpha = 0.1f)`
- Height: 64dp
- Item icon: 24dp
- Item label: `LabelSmall`
- Active color: `AccentPrimary` with glow
- Inactive color: `TextTertiary`

### TopAppBar

**Specs:**
- Background: `BackgroundDeepDark.copy(alpha = 0.8f)` with blur
- Blur: 16dp
- Border bottom: 1dp, `Color.White.copy(alpha = 0.1f)`
- Height: 64dp
- Title: `TitleLarge`, `TextPrimary`
- Icons: 24dp, `TextPrimary`
- Padding: `SpaceLG` horizontal

### Loading Indicator

**Circular Progress**

**Specs:**
- Size: 48dp
- Stroke width: 4dp
- Color: `LinearGradient(AccentPrimary, AccentSecondary)`
- Background track: `Color.White.copy(alpha = 0.1f)`
- Glow: `AccentPrimaryGlow`

**Linear Progress**

**Specs:**
- Height: 4dp
- Border radius: `RadiusFull`
- Color: `LinearGradient(AccentPrimary, AccentSecondary)`
- Background track: `Color.White.copy(alpha = 0.1f)`

### Dialog / BottomSheet

**Specs:**
- Background: `BackgroundElevated` with `GlassSurfaceMedium` overlay
- Blur: 48dp
- Border: 1dp, `Color.White.copy(alpha = 0.3f)`
- Border radius: `RadiusXL` (20dp for dialog), `RadiusLG` (top corners for bottom sheet)
- Elevation: `Elevation2XL`
- Padding: `SpaceXL`
- Title: `HeadlineSmall`, `TextPrimary`
- Content: `BodyMedium`, `TextSecondary`
- Actions: GlassButton components

---

## 🎬 Motion & Animation

### Animation Duration

```kotlin
val AnimationFast = 150 // milliseconds
val AnimationNormal = 300
val AnimationSlow = 500
```

### Easing Curves

```kotlin
// Material easing
val EaseInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
val EaseOut = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
val EaseIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

// Emphasized easing (Material You)
val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
```

### Transitions

**Screen transitions:**
- Fade + Slide (300ms, `EmphasizedDecelerate`)
- Slide distance: 32dp

**Card entrance:**
- Fade in + Scale (from 0.9 to 1.0)
- Duration: 300ms
- Stagger: 50ms per card

**Button press:**
- Scale to 0.95
- Duration: 150ms
- Easing: `EaseOut`

**Toggle switch:**
- Thumb slide: 300ms, `EmphasizedDecelerate`
- Track color: 300ms, `EaseInOut`

**Loading states:**
- Skeleton shimmer: 1500ms loop
- Progress circular rotation: 1333ms loop

---

## 🖼️ Iconography

### Icon System

**Source:** Material Symbols (Rounded variant) or Phosphor Icons

**Sizes:**
- Small: 16dp
- Medium: 24dp (default)
- Large: 32dp
- XLarge: 48dp

**Style:**
- Rounded corners
- 2dp stroke weight
- Consistent visual weight

### Key Icons

| Element | Icon | Usage |
|---------|------|-------|
| Dashboard | home / dashboard | Home tab |
| Method 1 | settings / tune | CarrierConfig overrides |
| Method 2 | code / terminal | Runtime instrumentation |
| Diagnostics | monitoring / bug_report | Diagnostics & logs |
| Settings | settings_applications | App settings |
| SIM | sim_card | SIM information |
| IMS | wifi_calling | IMS/VoWiFi status |
| Success | check_circle | Success state |
| Warning | warning | Warning state |
| Error | error | Error state |
| Info | info | Info state |
| Export | download / save | Export report |
| Refresh | refresh | Reload data |
| Root | admin_panel_settings | Root status |
| Magisk | developer_mode | Magisk status |
| Profile | account_circle / person | User profile |
| Play | play_arrow | Start session |
| Stop | stop | Stop session |
| Edit | edit | Edit action |
| Delete | delete | Delete action |
| Add | add | Add action |
| Chevron Right | chevron_right | Navigation forward |
| Chevron Down | expand_more | Dropdown expand |

---

## 📱 Screen Layouts

### 4.1 Home Dashboard

**Layout Structure:**

```
┌─────────────────────────────────────┐
│ [Top App Bar]                       │
│ ┌─────────────────────────────────┐ │
│ │ Device Info Card (Glass)        │ │
│ │ • Model, One UI, Build          │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ SIM Info Card (Glass)           │ │
│ │ • Slot, MCC/MNC, Carrier        │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ IMS Status Card (Glass)         │ │
│ │ • IMS Registered [Chip]         │ │
│ │ • VoLTE Available [Chip]        │ │
│ │ • VoWiFi Available [Chip]       │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ WFC UI Status Card (Glass)      │ │
│ │ • Settings Activity [Chip]      │ │
│ │ • Page Populates [Chip]         │ │
│ │ • Toggle Present [Chip]         │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Blocker Analysis (Glass)        │ │
│ │ ⚠ Likely Blocker: [Type]        │ │
│ │ [Description]                   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Primary Button: Run Diagnostic]    │
│ [Secondary: Open WFC Settings]      │
│ [Outlined: Export Report]           │
│                                     │
│ [Bottom Nav Bar (Glass)]            │
└─────────────────────────────────────┘
```

**Visual Elements:**
- Gradient background (top to bottom)
- Cards with glass effect, staggered entrance
- Status chips with appropriate colors
- Prominent CTAs at bottom
- Pull-to-refresh at top

### 4.2 Method 1 — CarrierConfig Screen

**Tab Layout:** Horizontal tabs with glassmorphism indicator

**Presets Tab:**

```
┌─────────────────────────────────────┐
│ [Tabs: Presets | Keys | Deploy]     │
│─────────────────────────────────────│
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Preset: Expose WFC UI (Glass)   │ │
│ │ Makes WFC settings visible      │ │
│ │ [Apply Preset] [View Details]   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Preset: WFC Default Enabled     │ │
│ │ Enables WFC by default          │ │
│ │ [Apply Preset] [View Details]   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [+ Create Custom Preset]            │
│                                     │
│ [Bottom Nav Bar (Glass)]            │
└─────────────────────────────────────┘
```

**Keys Tab:**

```
┌─────────────────────────────────────┐
│ [Search/Filter Bar (Glass)]         │
│─────────────────────────────────────│
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Key: carrier_wfc_ims_...  [⋮]  │ │
│ │ Type: Boolean                   │ │
│ │ Value: true              [Edit] │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [More keys...]                      │
│                                     │
│ [+ Add Key] [Import JSON]           │
│                                     │
└─────────────────────────────────────┘
```

**Deploy Tab:**

```
┌─────────────────────────────────────┐
│ ┌─────────────────────────────────┐ │
│ │ Prerequisites (Glass)           │ │
│ │ ✓ Root Access                   │ │
│ │ ✓ Magisk Installed              │ │
│ │ ✓ Paths Validated               │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Deployment Options              │ │
│ │ SIM Slot: [Dropdown]            │ │
│ │ Backup: [Toggle]                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Primary: Install/Update Module]    │
│ [Primary: Deploy Overrides]         │
│ [Outlined: Revert Changes]          │
│                                     │
└─────────────────────────────────────┘
```

### 4.3 Method 2 — Entitlement Simulation Screen

**Profiles Tab:**

```
┌─────────────────────────────────────┐
│ [Tabs: Profiles | Hooks | Session]  │
│─────────────────────────────────────│
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ ● Generic Samsung IMS (Glass)   │ │
│ │   One UI 6.x compatible         │ │
│ │   [Select Profile]              │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │   Carrier Plugin: VZW (Glass)   │ │
│ │   Verizon-specific hooks        │ │
│ │   [Select Profile]              │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [+ Create Custom Profile]           │
│                                     │
└─────────────────────────────────────┘
```

**Session Tab:**

```
┌─────────────────────────────────────┐
│ ┌─────────────────────────────────┐ │
│ │ Session Status: Inactive        │ │
│ │ Profile: Generic Samsung IMS    │ │
│ │ Backend: Frida Server           │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Large Primary: Start Session]      │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Live Events (Glass, scrollable) │ │
│ │ [No events yet]                 │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Secondary: Export Trace]           │
│                                     │
└─────────────────────────────────────┘
```

### 4.4 Diagnostics & Logs Screen

```
┌─────────────────────────────────────┐
│ [Top App Bar: Diagnostics]          │
│─────────────────────────────────────│
│                                     │
│ Snapshot Tools (Glass Card)         │
│ ┌───────────┬───────────┬─────────┐ │
│ │[Logcat]   │[dumpsys]  │[getprop]│ │
│ │ Radio     │  IMS      │Filtered │ │
│ └───────────┴───────────┴─────────┘ │
│ ┌───────────┬───────────┬─────────┐ │
│ │[dumpsys]  │[Export]   │[Clear]  │ │
│ │CarrierCfg │  All ZIP  │  Logs   │ │
│ └───────────┴───────────┴─────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Log Viewer (Glass, monospace)   │ │
│ │ [Log content with syntax colors]│ │
│ │                                 │ │
│ └─────────────────────────────────┘ │
│                                     │
│ [Toggle: Privacy Filter ON/OFF]     │
│                                     │
└─────────────────────────────────────┘
```

---

## 🌐 Responsive Considerations

### Orientation Support

**Portrait (Primary):**
- Vertical scrolling cards
- Full-width buttons
- Stacked layout

**Landscape:**
- Two-column layout for cards (where applicable)
- Horizontal button groups
- Wider dialogs

### Screen Sizes

**Small (Phone):**
- Single column
- Compact spacing
- Bottom navigation

**Medium (Large Phone/Small Tablet):**
- Maintain single column
- More generous spacing
- Optional side navigation

**Large (Tablet):**
- Two-column layout
- Side navigation rail
- Master-detail views

### Foldable Support

- Adapt to hinge position
- Utilize extra screen real estate
- Support multi-window

---

## ♿ Accessibility

### Contrast Ratios

All text must meet WCAG AA standards:
- **Normal text (< 18sp):** 4.5:1 minimum
- **Large text (≥ 18sp):** 3:1 minimum
- **UI components:** 3:1 minimum

### Touch Targets

- Minimum: 48dp × 48dp
- Recommended: 56dp × 56dp for primary actions
- Spacing between targets: ≥8dp

### Screen Reader Support

- Meaningful content descriptions for all interactive elements
- Proper heading hierarchy
- Announce state changes (loading, success, error)
- Group related content

### Focus Indicators

- Visible focus ring: 2dp border, `AccentPrimary`
- Focus order follows visual layout
- Skip to content option

### Reduce Motion

- Respect system reduce motion setting
- Disable/simplify animations when enabled
- Keep essential state feedback

---

## 🎨 Implementation Guidelines

### Compose Theme Setup

```kotlin
@Composable
fun CCOTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = AccentPrimary,
        onPrimary = Color.White,
        secondary = AccentSecondary,
        onSecondary = Color.White,
        tertiary = AccentSuccess,
        onTertiary = Color.Black,
        background = BackgroundDeepDark,
        onBackground = TextPrimary,
        surface = GlassSurface,
        onSurface = TextPrimary,
        error = AccentError,
        onError = Color.White
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CCOTypography,
        content = content
    )
}
```

### Blur Effect (Android 12+)

```kotlin
@Composable
fun BlurEffect(
    blur: Dp,
    content: @Composable () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    renderEffect = BlurEffect(
                        radiusX = blur.toPx(),
                        radiusY = blur.toPx(),
                        edgeTreatment = TileMode.Clamp
                    ).asComposeRenderEffect()
                }
        ) {
            content()
        }
    } else {
        // Fallback: just use transparency
        content()
    }
}
```

### Glass Surface Modifier

```kotlin
fun Modifier.glassSurface(
    backgroundColor: Color = GlassSurface,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderRadius: Dp = RadiusLG,
    blur: Dp = 24.dp
): Modifier = this
    .background(
        color = backgroundColor,
        shape = RoundedCornerShape(borderRadius)
    )
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.blur(radius = blur)
        } else {
            Modifier
        }
    )
    .border(
        width = 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(borderRadius)
    )
```

### Gradient Background

```kotlin
@Composable
fun GradientBackground(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B2E),
                        Color(0xFF0A0E14),
                        Color(0xFF1A0B1E)
                    )
                )
            )
    ) {
        content()
    }
}
```

---

## 📚 Design Assets

### Required Assets

- [ ] App icon (adaptive, foreground + background)
- [ ] Feature graphic (1024×500)
- [ ] Screenshots (phone, tablet, various states)
- [ ] Promo images
- [ ] Status icon set (24dp SVG)
- [ ] Logo/wordmark (if applicable)

### Asset Formats

- **Icons:** Vector (SVG/XML)
- **Illustrations:** Vector (SVG) or high-res PNG
- **App icon:** PNG at multiple densities (mdpi to xxxhdpi)

---

## 🔍 Design Review Checklist

Before implementation:

- [ ] Color palette has sufficient contrast
- [ ] Typography scale is readable at all sizes
- [ ] Spacing is consistent throughout
- [ ] Components are reusable
- [ ] Glassmorphism effects are not overdone
- [ ] All interactive elements have clear states (default, hover, pressed, disabled)
- [ ] Error states are clearly indicated
- [ ] Loading states are shown appropriately
- [ ] Accessibility requirements are met
- [ ] Motion is purposeful and not distracting
- [ ] Design scales across screen sizes
- [ ] Theme can be maintained long-term

---

## 🎯 Key Design Goals

1. **Premium & Technical:** Convey sophistication appropriate for root users
2. **Clear Information Hierarchy:** Users should immediately understand system state
3. **Glassmorphism Done Right:** Subtle, not overwhelming; functional, not just decorative
4. **High Readability:** Despite dark theme and transparency, text must be crisp
5. **Performance:** Blur effects should not cause jank; provide fallbacks
6. **Consistency:** Every screen follows the same design language

---

**Last Updated:** February 4, 2026
**Version:** 1.0
**Status:** Design specification
