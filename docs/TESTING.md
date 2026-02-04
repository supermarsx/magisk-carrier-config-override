# Testing Guide - Carrier Config Override

## Overview

CCO uses a comprehensive testing strategy covering unit tests, integration tests, and UI tests to ensure reliability and correctness.

## Test Structure

```
app/app/src/
├── test/java/                          # Unit Tests
│   └── com/supermarx/carrierconfig/
│       ├── data/
│       │   ├── datastore/
│       │   │   └── PreferencesManagerTest.kt
│       │   └── repository/
│       │       ├── LogcatRepositoryTest.kt
│       │       ├── DumpsysRepositoryTest.kt
│       │       ├── ConnectivityTestRepositoryTest.kt
│       │       └── ExportRepositoryTest.kt
│       └── ui/
│           └── screens/
│               └── diagnostics/
│                   └── DiagnosticsViewModelTest.kt
│
└── androidTest/java/                   # Instrumentation Tests
    └── com/supermarx/carrierconfig/
        ├── integration/
        │   └── RepositoryIntegrationTest.kt
        ├── ui/
        │   ├── screens/
        │   │   └── diagnostics/
        │   │       └── DiagnosticsScreenTest.kt
        │   └── navigation/
        │       └── NavigationTest.kt
        └── ExampleInstrumentedTest.kt
```

## Running Tests

### Quick Test (Unit Only)
```bash
cd app
./scripts/test-enhanced.sh quick
```

### All Unit Tests
```bash
./scripts/test-enhanced.sh unit
```

### Integration Tests
```bash
# Requires connected device or emulator
./scripts/test-enhanced.sh integration
```

### UI Tests
```bash
# Requires connected device or emulator
./scripts/test-enhanced.sh ui
```

### Complete Test Suite
```bash
./scripts/test-enhanced.sh all
```

### With Code Coverage
```bash
./scripts/test-enhanced.sh all coverage
```

## Test Categories

### 1. Unit Tests

#### Repository Tests
- **LogcatRepositoryTest** (235 tests)
  - Log parsing (threadtime format)
  - Category matching (IMS, CarrierConfig, Telephony, WFC)
  - Log level filtering
  - Edge cases (special chars, Unicode, multiline)
  
- **DumpsysRepositoryTest** (95 tests)
  - IMS info extraction
  - Registration state detection
  - Capability parsing
  - Format variations
  
- **ConnectivityTestRepositoryTest** (85 tests)
  - Test result types (Passed, Failed, Error, Skipped)
  - Test case metadata
  - Enum validation
  
- **ExportRepositoryTest** (65 tests)
  - Export format validation
  - Data serialization
  - Special character handling

#### DataStore Tests
- **PreferencesManagerTest** (40 tests)
  - Preference key validation
  - Default value verification
  - Type safety checks

#### ViewModel Tests
- **DiagnosticsViewModelTest** (32 tests)
  - State management
  - Live logcat flow
  - Snapshot loading
  - Test execution
  - Error handling

**Total Unit Tests: 550+**

### 2. Integration Tests

#### RepositoryIntegrationTest
- End-to-end repository workflows
- Cross-repository data flow
- Real parsing scenarios
- Device-agnostic integration

**Total Integration Tests: 20+**

### 3. UI Tests

#### DiagnosticsScreenTest
- Tab navigation
- Filter chip interaction
- FAB functionality
- Empty states
- Loading states

#### NavigationTest
- Bottom navigation
- Screen transitions
- Back navigation
- Deep linking
- State preservation

**Total UI Tests: 25+**

## Code Coverage

Current coverage targets:
- **Repository Layer**: 90%+
- **ViewModel Layer**: 85%+
- **UI Layer**: 70%+
- **Overall Target**: 80%+

View coverage report:
```bash
./scripts/test-enhanced.sh all coverage
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## Testing Best Practices

### 1. Test Naming Convention
```kotlin
fun `function name with specific condition produces expected result`()
```

Examples:
- ✅ `fun parseLogEntry with valid threadtime format()`
- ✅ `fun matchesCategory with IMS category()`
- ❌ `fun test1()`
- ❌ `fun testParsing()`

### 2. Test Structure (AAA Pattern)
```kotlin
@Test
fun `descriptive test name`() {
    // Arrange - Setup test data
    val input = "test data"
    
    // Act - Execute the function
    val result = repository.process(input)
    
    // Assert - Verify the result
    assertEquals(expected, result)
}
```

### 3. Use Descriptive Assertions
```kotlin
// Good
assertTrue("Expected IMS tag to match IMS category", 
          repository.matchesCategory("ImsManager", LogCategory.IMS))

// Bad
assertTrue(repository.matchesCategory("ImsManager", LogCategory.IMS))
```

### 4. Test Edge Cases
Always test:
- Empty input
- Null values
- Special characters
- Unicode characters
- Very large datasets
- Malformed input
- Boundary conditions

### 5. Mock External Dependencies
```kotlin
@Before
fun setup() {
    context = mock()
    connectivityManager = mock()
    whenever(context.getSystemService(Context.CONNECTIVITY_SERVICE))
        .thenReturn(connectivityManager)
}
```

## Continuous Integration

### GitHub Actions Workflow
```yaml
name: Android Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run Unit Tests
        run: ./gradlew test
      - name: Run Integration Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 33
          script: ./gradlew connectedAndroidTest
```

## Test Reports

After running tests, view reports at:

**Unit Tests:**
```
file://app/build/reports/tests/testDebugUnitTest/index.html
```

**Android Tests:**
```
file://app/build/reports/androidTests/connected/index.html
```

**Code Coverage:**
```
file://app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## Troubleshooting

### Tests Won't Run
```bash
# Clean and rebuild
./gradlew clean
./gradlew test
```

### Device Connection Issues
```bash
# Check devices
adb devices

# Restart ADB
adb kill-server
adb start-server
```

### Flaky UI Tests
- Ensure device is unlocked
- Disable animations: Developer Options → Animation scales → Off
- Use larger timeouts for slow devices

### Memory Issues
```bash
# Increase Gradle memory
export GRADLE_OPTS="-Xmx4096m -XX:MaxPermSize=512m"
```

## Test Development Guidelines

### Adding New Tests

1. **Create test file** matching source structure:
   ```
   src/main/java/com/example/MyClass.kt
   src/test/java/com/example/MyClassTest.kt
   ```

2. **Use proper imports:**
   ```kotlin
   import org.junit.Test
   import org.junit.Assert.*
   import org.junit.Before
   import kotlinx.coroutines.test.runTest
   ```

3. **Follow naming conventions**
4. **Add to test suite**
5. **Verify tests pass locally**
6. **Check coverage impact**

### Test-Driven Development (TDD)

1. Write failing test
2. Implement minimal code to pass
3. Refactor
4. Repeat

## Performance Testing

### Benchmark Tests
```kotlin
@Test
fun `performance test handles large dataset in reasonable time`() {
    val startTime = System.currentTimeMillis()
    
    // Execute operation
    repository.processLargeDataset()
    
    val duration = System.currentTimeMillis() - startTime
    assertTrue("Took ${duration}ms, expected < 1000ms", duration < 1000)
}
```

## Test Metrics

Track these metrics over time:
- Total test count
- Test pass rate
- Code coverage percentage
- Average test duration
- Flaky test count

## Future Improvements

- [ ] Add snapshot testing for UI
- [ ] Implement property-based testing
- [ ] Add load testing for repositories
- [ ] Automated performance regression detection
- [ ] Visual regression testing
- [ ] Add mutation testing

## Resources

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Compose Testing Guide](https://developer.android.com/jetpack/compose/testing)
- [Coroutines Testing](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/)

---

**Last Updated:** February 4, 2026  
**Test Count:** 595+ tests across all layers  
**Coverage:** 80%+ overall
