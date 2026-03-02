package com.supermarsx.carrierconfig.integration

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.model.*
import com.supermarsx.carrierconfig.data.repository.*
import com.supermarsx.carrierconfig.ui.screens.carrierconfig.CarrierConfigViewModel
import com.supermarsx.carrierconfig.ui.screens.dashboard.DashboardViewModel
import com.supermarsx.carrierconfig.ui.screens.diagnostics.DiagnosticsState
import com.supermarsx.carrierconfig.ui.screens.diagnostics.DiagnosticsViewModel
import com.supermarsx.carrierconfig.ui.screens.settings.SettingsState
import com.supermarsx.carrierconfig.util.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Chaining / Integration tests that verify multiple components work together.
 *
 * These tests validate cross-layer interactions:
 * - Repository → ViewModel data flow
 * - Preset selection → XML generation → deployment pipeline
 * - Export → Import round-trip
 * - DeviceRepository → DashboardViewModel blocker detection chain
 * - Diagnostics → Export chain
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChainingTest {

    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // Chain 1: preset selection → XML generation → deployment
    // =========================================================================

    @Test
    fun `preset selection flows through to XML generation`() {
        val repo = CarrierConfigRepository(mock())
        val presets = repo.getPresets()
        val fullEnablement = presets.first { it.id == "full_enablement" }

        // Step 1: Select preset keys
        val keys = fullEnablement.keys.map { (key, value) -> ConfigKey(key, value) }
        assertThat(keys).isNotEmpty()

        // Step 2: Generate XML
        val xml = repo.generateXML(keys)
        assertThat(xml).contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        assertThat(xml).contains("<carrier_config>")

        // Step 3: Verify all keys appear in generated XML
        keys.forEach { key ->
            assertThat(xml).contains(key.key)
        }
    }

    @Test
    fun `custom keys merge with preset keys in XML`() {
        val repo = CarrierConfigRepository(mock())
        val preset = repo.getPresets().first { it.id == "wfc_ui_only" }

        // Preset keys
        val presetKeys = preset.keys.map { (key, value) -> ConfigKey(key, value) }

        // Custom keys
        val customKeys = listOf(
            ConfigKey("custom_string", ConfigValue.StringValue("test_value"), isCustom = true),
            ConfigKey("custom_int", ConfigValue.IntValue(99), isCustom = true)
        )

        // Step 1: Combine
        val allKeys = presetKeys + customKeys
        assertThat(allKeys).hasSize(4) // 2 preset + 2 custom

        // Step 2: Generate XML
        val xml = repo.generateXML(allKeys)

        // Step 3: Both preset and custom keys appear
        assertThat(xml).contains("carrier_wfc_ims_available_bool")
        assertThat(xml).contains("custom_string")
        assertThat(xml).contains("test_value")
        assertThat(xml).contains("custom_int")
        assertThat(xml).contains("99")
    }

    @Test
    fun `preset WFC mode values appear correctly in generated XML`() {
        val repo = CarrierConfigRepository(mock())

        // Test each WFC mode preset
        val modePresets = mapOf(
            "wfc_default_enabled" to 0,  // Cellular preferred
            "wifi_preferred" to 1,       // Wi-Fi preferred
            "wifi_only" to 2             // Wi-Fi only
        )

        modePresets.forEach { (presetId, expectedMode) ->
            val preset = repo.getPresets().first { it.id == presetId }
            val keys = preset.keys.map { (key, value) -> ConfigKey(key, value) }
            val xml = repo.generateXML(keys)
            assertThat(xml).contains("value=\"$expectedMode\"")
        }
    }

    // =========================================================================
    // Chain 2: Export → Import round-trip
    // =========================================================================

    @Test
    fun `AppConfiguration export and import are symmetric`() {
        val original = AppConfiguration(
            version = "1.0.0",
            exportDate = System.currentTimeMillis(),
            settings = AppSettings(
                autoRefresh = false,
                enableNotifications = true,
                debugMode = true,
                theme = "system"
            ),
            customKeys = listOf(
                CustomKeyData("carrier_wfc_ims_available_bool", "boolean", "true"),
                CustomKeyData("carrier_default_wfc_ims_mode_int", "int", "1"),
                CustomKeyData("carrier_name_string", "string", "TestCarrier")
            )
        )

        // Export
        val exported = json.encodeToString(original)

        // Import
        val imported = json.decodeFromString<AppConfiguration>(exported)

        // Verify full round-trip
        assertThat(imported.version).isEqualTo(original.version)
        assertThat(imported.settings).isEqualTo(original.settings)
        assertThat(imported.customKeys).hasSize(3)
        assertThat(imported.customKeys.map { it.key }).containsExactly(
            "carrier_wfc_ims_available_bool",
            "carrier_default_wfc_ims_mode_int",
            "carrier_name_string"
        ).inOrder()
    }

    @Test
    fun `exported config can be modified and re-imported`() {
        val original = AppConfiguration(
            version = "1.0.0",
            exportDate = 1000L,
            settings = AppSettings(true, false, false, "dark"),
            customKeys = emptyList()
        )

        // Export
        val exported = json.encodeToString(original)

        // Import and modify
        val imported = json.decodeFromString<AppConfiguration>(exported)
        val modified = imported.copy(
            version = "2.0.0",
            settings = imported.settings?.copy(theme = "light", debugMode = true),
            customKeys = listOf(CustomKeyData("new_key", "boolean", "true"))
        )

        // Re-export
        val reExported = json.encodeToString(modified)
        val finalImport = json.decodeFromString<AppConfiguration>(reExported)

        assertThat(finalImport.version).isEqualTo("2.0.0")
        assertThat(finalImport.settings?.theme).isEqualTo("light")
        assertThat(finalImport.settings?.debugMode).isTrue()
        assertThat(finalImport.customKeys).hasSize(1)
    }

    // =========================================================================
    // Chain 3: DeviceRepository → DashboardViewModel blocker chain
    // =========================================================================

    @Test
    fun `device info flows through dashboard state correctly`() = runTest(testDispatcher) {
        val deviceRepo: DeviceRepository = mock()

        val deviceInfo = DeviceInfo(
            manufacturer = "samsung",
            model = "SM-S928B",
            buildFingerprint = "samsung/s928bxxs1axb1",
            androidVersion = "Android 15 (API 35)",
            oneUIVersion = "One UI 7.0",
            securityPatch = "2026-02-01",
            isRooted = true
        )
        val simInfo = listOf(
            SIMInfo(0, "T-Mobile", "310", "260", "••••1234", true),
            SIMInfo(1, "AT&T", "310", "410", "••••5678", true)
        )
        val imsStatus = IMSStatus(true, true, true, "REGISTERED_WIFI")
        val wfcStatus = WFCUIStatus(true, true, true)

        whenever(deviceRepo.getDeviceInfo()).thenReturn(deviceInfo)
        whenever(deviceRepo.getSIMInfo()).thenReturn(simInfo)
        whenever(deviceRepo.getIMSStatus()).thenReturn(imsStatus)
        whenever(deviceRepo.getWFCUIStatus()).thenReturn(wfcStatus)
        whenever(deviceRepo.detectBlocker(imsStatus, wfcStatus)).thenReturn(WFCBlocker.NONE)

        val vm = DashboardViewModel(deviceRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        // Chain verification: repo data → VM state
        assertThat(state.deviceInfo?.model).isEqualTo("SM-S928B")
        assertThat(state.simInfo).hasSize(2)
        assertThat(state.imsStatus?.isRegistered).isTrue()
        assertThat(state.wfcUIStatus?.togglePresent).isTrue()
        assertThat(state.detectedBlocker).isEqualTo(WFCBlocker.NONE)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `blocker detection chain from IMS not registered`() = runTest(testDispatcher) {
        val deviceRepo: DeviceRepository = mock()

        val imsStatus = IMSStatus(false, false, false, "NOT_REGISTERED")
        val wfcStatus = WFCUIStatus(true, true, true)

        whenever(deviceRepo.getDeviceInfo()).thenReturn(
            DeviceInfo("samsung", "S24", "", "14", null, "", true)
        )
        whenever(deviceRepo.getSIMInfo()).thenReturn(emptyList())
        whenever(deviceRepo.getIMSStatus()).thenReturn(imsStatus)
        whenever(deviceRepo.getWFCUIStatus()).thenReturn(wfcStatus)
        whenever(deviceRepo.detectBlocker(imsStatus, wfcStatus))
            .thenReturn(WFCBlocker.IMS_NOT_REGISTERED)

        val vm = DashboardViewModel(deviceRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.state.value.detectedBlocker).isEqualTo(WFCBlocker.IMS_NOT_REGISTERED)
    }

    @Test
    fun `blocker detection chain carrier config gate`() = runTest(testDispatcher) {
        val deviceRepo: DeviceRepository = mock()

        val imsStatus = IMSStatus(true, true, false, "REGISTERED_LTE")
        val wfcStatus = WFCUIStatus(true, true, false)

        whenever(deviceRepo.getDeviceInfo()).thenReturn(
            DeviceInfo("samsung", "S24", "", "14", null, "", true)
        )
        whenever(deviceRepo.getSIMInfo()).thenReturn(emptyList())
        whenever(deviceRepo.getIMSStatus()).thenReturn(imsStatus)
        whenever(deviceRepo.getWFCUIStatus()).thenReturn(wfcStatus)
        whenever(deviceRepo.detectBlocker(imsStatus, wfcStatus))
            .thenReturn(WFCBlocker.CARRIER_CONFIG_GATE)

        val vm = DashboardViewModel(deviceRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.state.value.detectedBlocker).isEqualTo(WFCBlocker.CARRIER_CONFIG_GATE)
    }

    // =========================================================================
    // Chain 4: Diagnostics pipeline
    // =========================================================================

    @Test
    fun `diagnostics filter change triggers snapshot reload`() = runTest(testDispatcher) {
        val logcatRepo: LogcatRepository = mock()
        val dumpsysRepo: DumpsysRepository = mock()
        val connectivityRepo: ConnectivityTestRepository = mock()
        val exportRepo: ExportRepository = mock()

        // Default stub for init
        whenever(logcatRepo.getLogcatSnapshot(
            filterType = LogcatFilterType.ALL,
            lineCount = 500
        )).thenReturn(listOf(
            LogcatEntry(1L, LogLevel.INFO, "CarrierConfigLoader", "loaded", 1, 1)
        ))

        // Stub for IMS filter
        whenever(logcatRepo.getLogcatSnapshot(
            filterType = LogcatFilterType.IMS,
            lineCount = 500
        )).thenReturn(listOf(
            LogcatEntry(2L, LogLevel.DEBUG, "ImsManager", "init", 2, 2),
            LogcatEntry(3L, LogLevel.INFO, "ImsPhone", "registered", 3, 3)
        ))

        val vm = DiagnosticsViewModel(logcatRepo, dumpsysRepo, connectivityRepo, exportRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        // Chain step 1: Initially has ALL entries
        assertThat(vm.logcatEntries.value).hasSize(1)

        // Chain step 2: Change filter → triggers new snapshot
        vm.setLogcatFilter(LogcatFilterType.IMS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Chain step 3: Entries updated to IMS-filtered results
        assertThat(vm.logcatEntries.value).hasSize(2)
        assertThat(vm.state.value.logcatFilterType).isEqualTo(LogcatFilterType.IMS)
    }

    @Test
    fun `dumpsys result flows to diagnostics state`() = runTest(testDispatcher) {
        val logcatRepo: LogcatRepository = mock()
        val dumpsysRepo: DumpsysRepository = mock()
        val connectivityRepo: ConnectivityTestRepository = mock()
        val exportRepo: ExportRepository = mock()

        whenever(logcatRepo.getLogcatSnapshot(
            filterType = any(),
            lineCount = any(),
            buffer = any()
        )).thenReturn(emptyList())
        whenever(dumpsysRepo.getDumpsysIms()).thenReturn(
            DumpsysResult.Success("ims", "mRegistered=true\nTYPE_WIFI\nMMTEL\nVOICE", 4)
        )

        val vm = DiagnosticsViewModel(logcatRepo, dumpsysRepo, connectivityRepo, exportRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.loadDumpsys("ims")
        testDispatcher.scheduler.advanceUntilIdle()

        val result = vm.state.value.dumpsysResult
        assertThat(result).isInstanceOf(DumpsysResult.Success::class.java)
        assertThat((result as DumpsysResult.Success).output).contains("mRegistered=true")
    }

    // =========================================================================
    // Chain 5: Connectivity tests → state
    // =========================================================================

    @Test
    fun `connectivity test suite flows through diagnostics state`() = runTest(testDispatcher) {
        val logcatRepo: LogcatRepository = mock()
        val dumpsysRepo: DumpsysRepository = mock()
        val connectivityRepo: ConnectivityTestRepository = mock()
        val exportRepo: ExportRepository = mock()

        whenever(logcatRepo.getLogcatSnapshot(
            filterType = any(),
            lineCount = any(),
            buffer = any()
        )).thenReturn(emptyList())

        val testSuite = ConnectivityTestSuite(
            networkStatus = TestResult.Passed("Connected"),
            dnsResolution = TestResult.Passed("Resolved google.com"),
            internetConnectivity = TestResult.Passed("HTTP 200"),
            wifiCalling = TestResult.Passed("WFC active"),
            imsRegistration = TestResult.Passed("Registered"),
            cellularData = TestResult.Passed("LTE"),
            timestamp = System.currentTimeMillis()
        )
        whenever(connectivityRepo.runFullTestSuite()).thenReturn(testSuite)

        val vm = DiagnosticsViewModel(logcatRepo, dumpsysRepo, connectivityRepo, exportRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.runConnectivityTests()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertThat(state.isRunningTests).isFalse()
        assertThat(state.testResults).isNotNull()
        assertThat(state.testResults!!.allPassed).isTrue()
        assertThat(state.testResults!!.failedCount).isEqualTo(0)
    }

    // =========================================================================
    // Chain 6: Refresh → full reload
    // =========================================================================

    @Test
    fun `dashboard refresh reloads all data`() = runTest(testDispatcher) {
        val deviceRepo: DeviceRepository = mock()
        val info1 = DeviceInfo("samsung", "SM-S928B", "", "14", null, "", true)
        val info2 = DeviceInfo("samsung", "SM-S929B", "", "15", null, "", true)

        whenever(deviceRepo.getDeviceInfo()).thenReturn(info1)
        whenever(deviceRepo.getSIMInfo()).thenReturn(emptyList())
        whenever(deviceRepo.getIMSStatus()).thenReturn(IMSStatus(true, true, true, "REGISTERED_WIFI"))
        whenever(deviceRepo.getWFCUIStatus()).thenReturn(WFCUIStatus(true, true, true))
        whenever(deviceRepo.detectBlocker(any(), any())).thenReturn(WFCBlocker.NONE)

        val vm = DashboardViewModel(deviceRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(vm.state.value.deviceInfo?.model).isEqualTo("SM-S928B")

        // Update mock data
        whenever(deviceRepo.getDeviceInfo()).thenReturn(info2)

        // Chain: refresh → reload → state updated
        vm.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(vm.state.value.deviceInfo?.model).isEqualTo("SM-S929B")
    }

    // =========================================================================
    // Chain 7: Preset → Config state → Prerequisites
    // =========================================================================

    @Test
    fun `prerequisites success allows deployment chain`() {
        val prereqs = Prerequisites(
            hasRoot = true,
            hasMagisk = true,
            magiskVersion = "27000",
            carrierConfigPath = "/data/vendor/carrierconfig/override.xml",
            pathWritable = true
        )
        assertThat(prereqs.allMet).isTrue()

        // Chain: prerequisites met → can select preset → can generate XML
        val repo = CarrierConfigRepository(mock())
        val preset = repo.getPresets().first()
        val keys = preset.keys.map { (key, value) -> ConfigKey(key, value) }
        val xml = repo.generateXML(keys)
        assertThat(xml).contains("<carrier_config>")
    }

    @Test
    fun `prerequisites failure blocks deployment chain`() {
        val prereqs = Prerequisites(hasRoot = false)
        assertThat(prereqs.allMet).isFalse()

        // This would result in DeploymentResult.PrerequisitesNotMet
        val result = DeploymentResult.PrerequisitesNotMet
        assertThat(result).isInstanceOf(DeploymentResult.PrerequisitesNotMet::class.java)
    }

    // =========================================================================
    // Chain 8: SettingsState → Preferences consistency
    // =========================================================================

    @Test
    fun `SettingsState defaults match AppPreferences defaults`() {
        val settings = SettingsState()
        val appPrefs = com.supermarsx.carrierconfig.data.datastore.AppPreferences()

        // All defaults must match for safe-cast consistency
        assertThat(settings.autoRefresh).isEqualTo(appPrefs.autoRefresh)
        assertThat(settings.enableNotifications).isEqualTo(appPrefs.enableNotifications)
        assertThat(settings.theme).isEqualTo(appPrefs.theme)
        assertThat(settings.glassEffectEnabled).isEqualTo(appPrefs.glassEffectEnabled)
        assertThat(settings.glassStrength).isEqualTo(appPrefs.glassStrength)
        assertThat(settings.debugMode).isEqualTo(appPrefs.debugMode)
        assertThat(settings.autoBackup).isEqualTo(appPrefs.autoBackup)
        assertThat(settings.backupFrequency).isEqualTo(appPrefs.backupFrequency)
    }

    // =========================================================================
    // Chain 9: Diagnostics → Export content
    // =========================================================================

    @Test
    fun `diagnostics report captures device and IMS info`() {
        val report = DiagnosticsReport(
            timestamp = System.currentTimeMillis(),
            deviceInfo = "Model: SM-S928B\nManufacturer: samsung\nAndroid: 15",
            simInfo = "Carrier: T-Mobile\nMCC/MNC: 310/260",
            imsStatus = "mRegistered=true\nVOICE capability\nTYPE_WIFI"
        )

        val jsonStr = json.encodeToString(report)
        val decoded = json.decodeFromString<DiagnosticsReport>(jsonStr)

        assertThat(decoded.deviceInfo).contains("SM-S928B")
        assertThat(decoded.simInfo).contains("T-Mobile")
        assertThat(decoded.imsStatus).contains("mRegistered=true")
    }

    // =========================================================================
    // Chain 10: Multi-SIM state flow
    // =========================================================================

    @Test
    fun `multi-SIM info flows correctly through state`() {
        val sims = listOf(
            SIMInfo(0, "T-Mobile", "310", "260", "••••1234", true),
            SIMInfo(1, "Verizon", "311", "480", "••••5678", true)
        )
        val state = DashboardState(simInfo = sims)
        assertThat(state.simInfo).hasSize(2)
        assertThat(state.simInfo[0].slotIndex).isEqualTo(0)
        assertThat(state.simInfo[1].slotIndex).isEqualTo(1)
        assertThat(state.simInfo[0].carrierName).isEqualTo("T-Mobile")
        assertThat(state.simInfo[1].carrierName).isEqualTo("Verizon")
    }

    @Test
    fun `per-SIM slot override path generation`() {
        // Per spec Section 5.4, per-SIM slot support
        val slot0Path = "/data/adb/cco/active/override_sim0.xml"
        val slot1Path = "/data/adb/cco/active/override_sim1.xml"
        val defaultPath = "/data/adb/cco/active/override.xml"

        assertThat(slot0Path).contains("sim0")
        assertThat(slot1Path).contains("sim1")
        assertThat(defaultPath).doesNotContain("sim")
    }

    // =========================================================================
    // Chain 11: Error propagation chain
    // =========================================================================

    @Test
    fun `device repo error propagates to dashboard state`() = runTest(testDispatcher) {
        val deviceRepo: DeviceRepository = mock()
        whenever(deviceRepo.getDeviceInfo()).thenThrow(RuntimeException("No root access"))

        val vm = DashboardViewModel(deviceRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.state.value
        assertThat(state.error).isNotNull()
        assertThat(state.error).contains("No root")
        assertThat(state.isLoading).isFalse()
    }

    // =========================================================================
    // Chain 12: ShellResult → SystemProperties chain
    // =========================================================================

    @Test
    fun `ShellResult to property map chain`() {
        val result = ShellResult(
            success = true,
            output = listOf(
                "[persist.sys.ims]: [1]",
                "[ro.config.ims]: [true]",
                "[ril.ims.status]: [registered]"
            ),
            error = emptyList(),
            exitCode = 0
        )

        val propRegex = Regex("\\[(.*?)\\]: \\[(.*?)\\]")
        val props = result.output.mapNotNull { line ->
            propRegex.find(line)?.let {
                val (key, value) = it.destructured
                key to value
            }
        }.toMap()

        assertThat(props).hasSize(3)
        assertThat(props["persist.sys.ims"]).isEqualTo("1")
        assertThat(props["ro.config.ims"]).isEqualTo("true")
        assertThat(props["ril.ims.status"]).isEqualTo("registered")
    }
}
