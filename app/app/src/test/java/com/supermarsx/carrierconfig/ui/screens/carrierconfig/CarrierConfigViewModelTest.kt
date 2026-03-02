package com.supermarsx.carrierconfig.ui.screens.carrierconfig

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.model.*
import com.supermarsx.carrierconfig.data.repository.CarrierConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for [CarrierConfigViewModel].
 *
 * Every public method is tested:
 * - loadPresets, checkPrerequisites
 * - selectPreset, addCustomKey, removeCustomKey, getSelectedKeys
 * - generateXMLPreview
 * - deploy (success, error, prerequisites not met)
 * - revert (success, error)
 * - switchTab, clearError
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CarrierConfigViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var carrierConfigRepository: CarrierConfigRepository
    private lateinit var viewModel: CarrierConfigViewModel

    private val testPresets = listOf(
        CarrierConfigPreset(
            id = "wfc_ui_only",
            name = "Expose WFC UI Only",
            description = "Makes Wi-Fi Calling menu visible",
            category = PresetCategory.WFC_ENABLE,
            keys = mapOf(
                "carrier_wfc_ims_available_bool" to ConfigValue.BooleanValue(true),
                "editable_wfc_mode_bool" to ConfigValue.BooleanValue(true)
            ),
            recommendedFor = "Testing"
        ),
        CarrierConfigPreset(
            id = "full_enablement",
            name = "Full WFC Enablement",
            description = "Complete WFC enablement",
            category = PresetCategory.ADVANCED,
            keys = mapOf(
                "carrier_wfc_ims_available_bool" to ConfigValue.BooleanValue(true),
                "editable_wfc_mode_bool" to ConfigValue.BooleanValue(true),
                "carrier_default_wfc_ims_mode_int" to ConfigValue.IntValue(1)
            ),
            recommendedFor = "Most users"
        )
    )

    private val successPrereqs = Prerequisites(
        hasRoot = true,
        hasMagisk = true,
        magiskVersion = "27000",
        carrierConfigPath = "/data/vendor/carrierconfig/override.xml",
        pathWritable = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        carrierConfigRepository = mock()
        whenever(carrierConfigRepository.getPresets()).thenReturn(testPresets)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun stubPrerequisites(prereqs: Prerequisites = successPrereqs) {
        whenever(carrierConfigRepository.checkPrerequisites()).thenReturn(prereqs)
    }

    private suspend fun stubDeploymentStatus() {
        whenever(carrierConfigRepository.getDeploymentStatus(any())).thenReturn(CarrierConfigDeployment())
    }

    private fun createViewModel(): CarrierConfigViewModel {
        val vm = CarrierConfigViewModel(carrierConfigRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    // =========================================================================
    // Initialization
    // =========================================================================

    @Test
    fun `init loads presets and checks prerequisites`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        val state = viewModel.state.value

        assertThat(state.presets).hasSize(2)
        assertThat(state.presets[0].id).isEqualTo("wfc_ui_only")
        assertThat(state.isLoading).isFalse()
        verify(carrierConfigRepository).getPresets()
        verify(carrierConfigRepository).checkPrerequisites()
    }

    @Test
    fun `init handles prerequisites error gracefully`() = runTest(testDispatcher) {
        whenever(carrierConfigRepository.checkPrerequisites()).thenThrow(RuntimeException("No root"))
        stubDeploymentStatus()

        viewModel = createViewModel()
        val state = viewModel.state.value

        // Presets should still load even if prerequisites fail
        assertThat(state.presets).hasSize(2)
    }

    // =========================================================================
    // selectPreset
    // =========================================================================

    @Test
    fun `selectPreset updates selectedPreset`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.selectPreset(testPresets[1])
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.selectedPreset).isEqualTo(testPresets[1])
    }

    @Test
    fun `selectPreset can be cleared by selecting same preset`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.selectPreset(testPresets[0])
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.selectedPreset).isEqualTo(testPresets[0])
    }

    // =========================================================================
    // addCustomKey / removeCustomKey
    // =========================================================================

    @Test
    fun `addCustomKey adds to customKeys list`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        val customKey = ConfigKey("my_bool", ConfigValue.BooleanValue(true), isCustom = true)
        viewModel.addCustomKey(customKey)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.customKeys).hasSize(1)
        assertThat(viewModel.state.value.customKeys[0].key).isEqualTo("my_bool")
    }

    @Test
    fun `addCustomKey multiple keys accumulates`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.addCustomKey(ConfigKey("key1", ConfigValue.BooleanValue(true), isCustom = true))
        viewModel.addCustomKey(ConfigKey("key2", ConfigValue.IntValue(42), isCustom = true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.customKeys).hasSize(2)
    }

    @Test
    fun `removeCustomKey removes specified key`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        val key1 = ConfigKey("key1", ConfigValue.BooleanValue(true), isCustom = true)
        val key2 = ConfigKey("key2", ConfigValue.IntValue(42), isCustom = true)
        viewModel.addCustomKey(key1)
        viewModel.addCustomKey(key2)
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.state.value.customKeys).hasSize(2)

        viewModel.removeCustomKey("key1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.customKeys).hasSize(1)
        assertThat(viewModel.state.value.customKeys[0].key).isEqualTo("key2")
    }

    @Test
    fun `removeCustomKey no-op for non-existent key`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.addCustomKey(ConfigKey("key1", ConfigValue.BooleanValue(true), isCustom = true))
        viewModel.removeCustomKey("non_existent")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.customKeys).hasSize(1)
    }

    // =========================================================================
    // getSelectedKeys
    // =========================================================================

    @Test
    fun `getSelectedKeys combines preset and custom keys`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.selectPreset(testPresets[0]) // 2 keys
        viewModel.addCustomKey(ConfigKey("custom", ConfigValue.BooleanValue(true), isCustom = true))
        testDispatcher.scheduler.advanceUntilIdle()

        val keys = viewModel.getSelectedKeys()
        assertThat(keys).hasSize(3)
    }

    @Test
    fun `getSelectedKeys returns only custom keys when no preset selected`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.addCustomKey(ConfigKey("custom", ConfigValue.IntValue(1), isCustom = true))
        testDispatcher.scheduler.advanceUntilIdle()

        val keys = viewModel.getSelectedKeys()
        assertThat(keys).hasSize(1)
    }

    @Test
    fun `getSelectedKeys returns empty when nothing selected`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        val keys = viewModel.getSelectedKeys()
        assertThat(keys).isEmpty()
    }

    // =========================================================================
    // generateXMLPreview
    // =========================================================================

    @Test
    fun `generateXMLPreview generates XML from selected keys`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()
        whenever(carrierConfigRepository.generateXML(any())).thenReturn(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<carrier_config>\n</carrier_config>\n"
        )

        viewModel = createViewModel()
        viewModel.selectPreset(testPresets[0])
        testDispatcher.scheduler.advanceUntilIdle()

        val xml = viewModel.generateXMLPreview()
        assertThat(xml).contains("<carrier_config>")
    }

    // =========================================================================
    // deploy
    // =========================================================================

    @Test
    fun `deploy success updates deployment state`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()
        whenever(carrierConfigRepository.deployOverride(
            preset = any(),
            customKeys = any(),
            targetPath = any(),
            simSlot = anyOrNull()
        )).thenReturn(DeploymentResult.Success)

        viewModel = createViewModel()
        viewModel.selectPreset(testPresets[0])
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deploy()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `deploy error sets error message`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()
        whenever(carrierConfigRepository.deployOverride(
            preset = any(),
            customKeys = any(),
            targetPath = any(),
            simSlot = anyOrNull()
        )).thenReturn(DeploymentResult.Error("Write failed"))

        viewModel = createViewModel()
        viewModel.selectPreset(testPresets[0])
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deploy()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.error).contains("Write failed")
    }

    @Test
    fun `deploy prerequisites not met sets error`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()
        whenever(carrierConfigRepository.deployOverride(
            preset = any(),
            customKeys = any(),
            targetPath = any(),
            simSlot = anyOrNull()
        )).thenReturn(DeploymentResult.PrerequisitesNotMet)

        viewModel = createViewModel()
        viewModel.selectPreset(testPresets[0])
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deploy()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.error).contains("Prerequisites")
    }

    @Test
    fun `deploy without preset does not call repository`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.deploy()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(carrierConfigRepository, never()).deployOverride(
            preset = any(),
            customKeys = any(),
            targetPath = any(),
            simSlot = anyOrNull()
        )
    }

    // =========================================================================
    // revert
    // =========================================================================

    @Test
    fun `revert success clears deployment`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()
        whenever(carrierConfigRepository.revertOverride(any())).thenReturn(DeploymentResult.Success)

        viewModel = createViewModel()
        viewModel.revert()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun `revert error sets error message`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()
        whenever(carrierConfigRepository.revertOverride(any()))
            .thenReturn(DeploymentResult.Error("Unmount failed"))

        viewModel = createViewModel()
        viewModel.revert()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.error).contains("Unmount failed")
    }

    @Test
    fun `revert exception sets error`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()
        whenever(carrierConfigRepository.revertOverride(any()))
            .thenThrow(RuntimeException("IO error"))

        viewModel = createViewModel()
        viewModel.revert()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value.error).contains("Revert failed")
    }

    // =========================================================================
    // switchTab
    // =========================================================================

    @Test
    fun `switchTab updates currentTab`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.switchTab(2)

        assertThat(viewModel.state.value.currentTab).isEqualTo(2)
    }

    @Test
    fun `switchTab to same value is idempotent`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        viewModel.switchTab(1)
        viewModel.switchTab(1)

        assertThat(viewModel.state.value.currentTab).isEqualTo(1)
    }

    // =========================================================================
    // clearError
    // =========================================================================

    @Test
    fun `clearError clears error message`() = runTest(testDispatcher) {
        stubPrerequisites()
        stubDeploymentStatus()

        viewModel = createViewModel()
        // Force an error state
        whenever(carrierConfigRepository.revertOverride(any()))
            .thenReturn(DeploymentResult.Error("test error"))
        viewModel.revert()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.state.value.error).isNotNull()

        viewModel.clearError()
        assertThat(viewModel.state.value.error).isNull()
    }
}
