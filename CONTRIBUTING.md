# Contributing to CCO Manager

Thank you for considering contributing to the CarrierConfig Override Manager! This guide will help you get started with development and submitting contributions.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Release Process](#release-process)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive experience for everyone. We expect all contributors to:

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment, trolling, or discriminatory comments
- Personal or political attacks
- Publishing others' private information
- Conduct which could reasonably be considered inappropriate

---

## Getting Started

### Prerequisites

- **Development Machine**: macOS, Linux, or Windows with WSL
- **Android Studio**: Latest stable version (Hedgehog or later)
- **JDK**: Version 17 or higher
- **Git**: Version control
- **ADB**: Android Debug Bridge
- **Test Device**: Rooted Samsung phone with Android 13+

### Quick Start

```bash
# 1. Fork the repository on GitHub

# 2. Clone your fork
git clone https://github.com/YOUR_USERNAME/cco-manager.git
cd cco-manager

# 3. Add upstream remote
git remote add upstream https://github.com/ORIGINAL_OWNER/cco-manager.git

# 4. Create a feature branch
git checkout -b feature/your-feature-name

# 5. Open in Android Studio
# File > Open > Select cco-manager/app directory

# 6. Build the project
./scripts/build.sh

# 7. Run tests
./scripts/test.sh
```

---

## Development Setup

### Android Studio Configuration

1. **Import Project**

   ```text
   File > Open > Select app/ directory
   Wait for Gradle sync to complete
   ```

2. **Configure SDK**

   ```text
   Tools > SDK Manager
   Install Android 13 (API 33) SDK
   Install Android SDK Build-Tools 34.0.0
   ```

3. **Install Plugins** (Recommended)
   - Kotlin (bundled)
   - Android Gradle Plugin
   - Jetpack Compose Previews

4. **Code Style**

   ```text
   File > Settings > Editor > Code Style > Kotlin
   Scheme: Kotlin style guide
   Import: Use .editorconfig from project root
   ```

### Build Scripts

The project includes several automation scripts:

```bash
# Development build (debug)
./scripts/dev.sh

# Quick build (release, unsigned)
./scripts/build.sh

# Run all tests
./scripts/test.sh

# Run linter
./scripts/lint.sh

# Integration tests (requires device)
./scripts/integration-test.sh

# Package for release
./scripts/package.sh
```

### Environment Variables

Optional environment variables:

```bash
# Android SDK path (if not default)
export ANDROID_SDK_ROOT=/path/to/android/sdk

# Enable verbose output
export CCO_VERBOSE=1

# Skip tests during build
export CCO_SKIP_TESTS=1
```

---

## Project Structure

```text
magisk-carrier-config-override/
├── app/                          # Android application
│   ├── app/                      # Main app module
│   │   ├── src/main/java/com/supermarsx/carrierconfig/
│   │   │   ├── data/             # Data layer (repositories, models)
│   │   │   ├── di/               # Dependency injection (Hilt)
│   │   │   ├── domain/           # Business logic
│   │   │   └── ui/               # UI layer (screens, components)
│   │   │       ├── components/   # Reusable composables
│   │   │       ├── screens/      # App screens
│   │   │       ├── theme/        # Material 3 theme
│   │   │       └── navigation/   # Navigation setup
│   │   └── build.gradle.kts      # App build configuration
│   ├── scripts/                  # Build automation scripts
│   └── README.md                 # App documentation
├── module/                       # Magisk module
│   ├── install.sh                # Installation script
│   ├── service.sh                # Boot service
│   ├── profiles/                 # Configuration profiles
│   ├── scripts/                  # Dev/test/lint scripts
│   ├── tests/                    # Shell test suite
│   └── docs/                     # Module documentation
├── cli/                          # Command-line utility
│   └── ccoctl                    # CLI tool script
├── instrumentation/              # Frida/LSPosed hooks
│   ├── frida/                    # Frida agent scripts
│   ├── lsposed/                  # LSPosed Xposed module
│   └── shared/                   # Hook profiles database
├── docs/                         # Documentation
│   ├── INSTALL.md                # Installation guide
│   ├── SPEC_COMPLIANCE.md        # Spec compliance & gap analysis
│   ├── TESTING.md                # Testing guide
│   └── TROUBLESHOOTING.md        # User troubleshooting
├── CONTRIBUTING.md               # This file
└── README.md                     # Project overview
```

### Key Directories

- **`data/repository/`**: Data access layer (root commands, system queries)
- **`data/model/`**: Data classes and sealed classes
- **`ui/screens/`**: Screen-level composables and ViewModels
- **`ui/components/`**: Reusable UI components (GlassCard, GlassButton, etc.)
- **`ui/theme/`**: Color palette, typography, Material 3 theme

---

## Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ Good
fun calculateCacheSize(): Long {
    val cacheDir = getCacheDirectory()
    return cacheDir.totalSize()
}

// ❌ Bad
fun CalculateCacheSize():Long{
    val cacheDir=getCacheDirectory()
    return cacheDir.totalSize()
}
```

### Naming Conventions

- **Classes**: PascalCase (`CarrierConfigRepository`)
- **Functions**: camelCase (`clearCache()`)
- **Variables**: camelCase (`cacheSize`)
- **Constants**: SCREAMING_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **Files**: PascalCase matching primary class (`GlassButton.kt`)

### Code Organization

```kotlin
// 1. Package declaration
package com.supermarsx.carrierconfig.ui.screens.settings

// 2. Imports (alphabetically sorted)
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.launch

// 3. Class documentation
/**
 * ViewModel for Settings screen
 * 
 * Manages user preferences and app configuration.
 */

// 4. Class declaration
@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {
    
    // 5. Companion object (if needed)
    companion object {
        private const val TAG = "SettingsViewModel"
    }
    
    // 6. Properties
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    // 7. Init block
    init {
        loadPreferences()
    }
    
    // 8. Public functions
    fun clearCache() { }
    
    // 9. Private functions
    private fun calculateCacheSize(): Long { }
}

// 10. Data classes
data class SettingsState(
    val cacheSize: Long = 0L
)
```

### Compose Best Practices

```kotlin
// ✅ Extract composables for reusability
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()
    
    SettingsContent(
        state = state,
        onClearCache = viewModel::clearCache
    )
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onClearCache: () -> Unit
) {
    // UI implementation
}

// ❌ Don't put everything in one giant composable
```

### Error Handling

```kotlin
// ✅ Proper error handling
suspend fun clearCache(): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        cacheDir.deleteRecursively()
        Result.success(Unit)
    } catch (e: IOException) {
        Log.e(TAG, "Failed to clear cache", e)
        Result.failure(e)
    }
}

// ❌ Swallowing exceptions
try {
    cacheDir.deleteRecursively()
} catch (e: Exception) {
    // Nothing here!
}
```

### Documentation

```kotlin
/**
 * Calculates the total size of the app cache directory.
 * 
 * This includes both internal and external cache directories.
 * 
 * @return Cache size in bytes, or 0 if calculation fails
 */
fun calculateCacheSize(): Long {
    // Implementation
}
```

---

## Testing

### Test Structure

```text
src/test/              # Unit tests (JVM)
  ├── repository/      # Repository tests
  ├── viewmodel/       # ViewModel tests
  └── util/            # Utility function tests

src/androidTest/       # Instrumented tests (Device)
  ├── ui/              # UI tests (Compose)
  └── integration/     # Integration tests
```

### Writing Tests

```kotlin
// Unit test example
class SettingsViewModelTest {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var mockPreferences: PreferencesManager
    
    @Before
    fun setup() {
        mockPreferences = mock()
        viewModel = SettingsViewModel(mockPreferences)
    }
    
    @Test
    fun `clearCache updates state correctly`() = runTest {
        // Given
        val initialState = viewModel.state.value
        
        // When
        viewModel.clearCache()
        advanceUntilIdle()
        
        // Then
        val finalState = viewModel.state.value
        assertThat(finalState.cacheSize).isEqualTo(0L)
    }
}
```

### Running Tests

```bash
# Run all tests
./scripts/test.sh

# Run unit tests only
./gradlew test

# Run UI tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests "SettingsViewModelTest"

# Generate coverage report
./gradlew jacocoTestReport
```

### Test Coverage

Aim for:

- **80%+ overall coverage**
- **90%+ for business logic**
- **70%+ for UI code**

Check coverage:

```bash
./gradlew jacocoTestReport
open app/build/reports/jacoco/test/html/index.html
```

---

## Submitting Changes

### Branch Naming

- **Feature**: `feature/add-cache-management`
- **Bug Fix**: `fix/crash-on-startup`
- **Documentation**: `docs/update-install-guide`
- **Refactor**: `refactor/repository-layer`

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```text
feat(settings): add cache size calculation

- Implement calculateCacheSize() in ViewModel
- Display formatted size in Settings screen
- Add refreshCacheSize() function
- Update tests for cache management

Closes #123
```

Types:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process, dependencies

### Pull Request Process

1. **Update Your Branch**

   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run Tests**

   ```bash
   ./scripts/test.sh
   ./scripts/lint.sh
   ```

3. **Push to Your Fork**

   ```bash
   git push origin feature/your-feature-name
   ```

4. **Create Pull Request**
   - Go to GitHub repository
   - Click "New Pull Request"
   - Select your branch
   - Fill in the PR template:
     - **Description**: What does this PR do?
     - **Related Issues**: Closes #123
     - **Testing**: How did you test this?
     - **Screenshots**: (if UI changes)

5. **Code Review**
   - Address reviewer feedback
   - Push updates to your branch
   - PR updates automatically

6. **Merge**
   - Squash and merge (preferred)
   - Delete branch after merge

### Pull Request Checklist

- [ ] Tests pass locally
- [ ] New tests added (if applicable)
- [ ] Documentation updated
- [ ] Code follows style guide
- [ ] Commit messages are clear
- [ ] No merge conflicts
- [ ] PR description is complete

---

## Release Process

### Version Numbering

Follow [Semantic Versioning](https://semver.org/):

```text
MAJOR.MINOR.PATCH (e.g., 1.2.3)

MAJOR: Breaking changes
MINOR: New features (backward compatible)
PATCH: Bug fixes
```

### Release Workflow

1. **Update Version**

   ```kotlin
   // app/build.gradle.kts
   versionCode = 10
   versionName = "1.2.3"
   ```

2. **Update Changelog**

   ```markdown
   # docs/CHANGELOG.md
   
   ## [1.2.3] - 2026-02-05
   
   ### Added
   - Cache size calculation in Settings
   
   ### Fixed
   - Crash on startup when root denied
   ```

3. **Create Release Tag**

   ```bash
   git tag -a v1.2.3 -m "Release version 1.2.3"
   git push upstream v1.2.3
   ```

4. **Build Release APK**

   ```bash
   ./scripts/package.sh
   ```

5. **GitHub Release**
   - Go to Releases page
   - Create new release from tag
   - Upload APK
   - Copy changelog to description

---

## Getting Help

- **Questions**: [GitHub Discussions](https://github.com/yourusername/cco-manager/discussions)
- **Issues**: [GitHub Issues](https://github.com/yourusername/cco-manager/issues)
- **Chat**: [Telegram Group](https://t.me/cco_manager_dev)

---

## Recognition

Contributors are recognized in:

- [docs/CHANGELOG.md](docs/CHANGELOG.md)
- [README.md](README.md) Contributors section
- GitHub commit history

Thank you for contributing to CCO Manager! 🎉
