package com.supermarsx.carrierconfig.ui.screens.entitlement

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.instrumentation.ProfileManager
import org.junit.Test

/**
 * Edge-case tests for Entitlement screen state models:
 * [EntitlementState], [EntitlementTab], [HookBackend], [EntitlementPackage],
 * [SessionEvent], and [SessionEventType].
 */
class EntitlementStateEdgeCaseTest {

    // =========================================================================
    // EntitlementTab
    // =========================================================================

    @Test
    fun `EntitlementTab has 3 values`() {
        assertThat(EntitlementTab.values()).hasLength(3)
    }

    @Test
    fun `EntitlementTab displayNames`() {
        assertThat(EntitlementTab.PROFILES.displayName).isEqualTo("Profiles")
        assertThat(EntitlementTab.HOOKS.displayName).isEqualTo("Hooks")
        assertThat(EntitlementTab.SESSION.displayName).isEqualTo("Session")
    }

    @Test
    fun `EntitlementTab valueOf round-trips`() {
        EntitlementTab.values().forEach { tab ->
            assertThat(EntitlementTab.valueOf(tab.name)).isEqualTo(tab)
        }
    }

    // =========================================================================
    // HookBackend
    // =========================================================================

    @Test
    fun `HookBackend has 2 values`() {
        assertThat(HookBackend.values()).hasLength(2)
    }

    @Test
    fun `HookBackend displayNames`() {
        assertThat(HookBackend.FRIDA.displayName).isEqualTo("Frida")
        assertThat(HookBackend.LSPOSED.displayName).isEqualTo("LSPosed")
    }

    // =========================================================================
    // EntitlementState defaults
    // =========================================================================

    @Test
    fun `EntitlementState default has empty profiles`() {
        val state = EntitlementState()
        assertThat(state.profiles).isEmpty()
        assertThat(state.profilesMetadata).isNull()
        assertThat(state.selectedProfileId).isNull()
    }

    @Test
    fun `EntitlementState default tab is PROFILES`() {
        assertThat(EntitlementState().selectedTab).isEqualTo(EntitlementTab.PROFILES)
    }

    @Test
    fun `EntitlementState default backend is FRIDA`() {
        assertThat(EntitlementState().selectedBackend).isEqualTo(HookBackend.FRIDA)
    }

    @Test
    fun `EntitlementState default session not active`() {
        val state = EntitlementState()
        assertThat(state.sessionActive).isFalse()
        assertThat(state.fridaInstalled).isFalse()
        assertThat(state.fridaRunning).isFalse()
        assertThat(state.fridaVersion).isNull()
        assertThat(state.fridaPid).isNull()
    }

    @Test
    fun `EntitlementState default error is null`() {
        assertThat(EntitlementState().error).isNull()
    }

    @Test
    fun `EntitlementState copy updates only target fields`() {
        val state = EntitlementState(
            sessionActive = true,
            fridaInstalled = true,
            fridaRunning = true,
            error = "something"
        )
        val updated = state.copy(error = null, sessionActive = false)
        assertThat(updated.fridaInstalled).isTrue()
        assertThat(updated.fridaRunning).isTrue()
        assertThat(updated.sessionActive).isFalse()
        assertThat(updated.error).isNull()
    }

    // =========================================================================
    // EntitlementPackage
    // =========================================================================

    @Test
    fun `EntitlementPackage equality`() {
        val a = EntitlementPackage("com.sec.ims", "1.0", true, true)
        val b = EntitlementPackage("com.sec.ims", "1.0", true, true)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `EntitlementPackage inequality on hookTarget`() {
        val a = EntitlementPackage("com.sec.ims", "1.0", true, true)
        val b = EntitlementPackage("com.sec.ims", "1.0", true, false)
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `EntitlementPackage with empty packageName`() {
        val pkg = EntitlementPackage("", "", false, false)
        assertThat(pkg.packageName).isEmpty()
    }

    // =========================================================================
    // SessionEvent
    // =========================================================================

    @Test
    fun `SessionEventType has 3 values`() {
        assertThat(SessionEventType.values()).hasLength(3)
    }

    @Test
    fun `SessionEventType values`() {
        assertThat(SessionEventType.values().map { it.name })
            .containsExactly("INFO", "HOOK", "ERROR")
    }

    @Test
    fun `SessionEvent carries all fields`() {
        val event = SessionEvent(
            timestamp = 1234567890L,
            type = SessionEventType.HOOK,
            message = "Hooked isWfcEntitled → true"
        )
        assertThat(event.timestamp).isEqualTo(1234567890L)
        assertThat(event.type).isEqualTo(SessionEventType.HOOK)
        assertThat(event.message).contains("isWfcEntitled")
    }

    @Test
    fun `SessionEvent with empty message`() {
        val event = SessionEvent(0L, SessionEventType.INFO, "")
        assertThat(event.message).isEmpty()
    }

    @Test
    fun `SessionEvent with special characters in message`() {
        val event = SessionEvent(
            0L,
            SessionEventType.ERROR,
            "Error: \"unexpected\" <char> & 'test'"
        )
        assertThat(event.message).contains("\"unexpected\"")
        assertThat(event.message).contains("<char>")
        assertThat(event.message).contains("&")
    }

    @Test
    fun `SessionEvent equality`() {
        val a = SessionEvent(100L, SessionEventType.INFO, "msg")
        val b = SessionEvent(100L, SessionEventType.INFO, "msg")
        assertThat(a).isEqualTo(b)
    }

    // =========================================================================
    // ProfileManager data classes (inner classes)
    // =========================================================================

    @Test
    fun `HookProfile with minimal fields`() {
        val profile = ProfileManager.HookProfile(
            id = "test",
            name = "Test",
            description = "desc",
            oneuiVersions = listOf("*"),
            androidVersions = listOf("*"),
            targets = emptyList()
        )
        assertThat(profile.carriers).isNull()
        assertThat(profile.carrierConfigOverrides).isNull()
        assertThat(profile.settingsOverrides).isNull()
        assertThat(profile.note).isNull()
    }

    @Test
    fun `HookTarget with backtick field names`() {
        val target = ProfileManager.HookTarget(
            `package` = "com.sec.imsservice",
            `class` = "com.sec.ims.ImsManager",
            method = "isWfcEntitled",
            signature = "()Z",
            returnValue = true,
            description = "WFC entitlement"
        )
        assertThat(target.`package`).isEqualTo("com.sec.imsservice")
        assertThat(target.`class`).contains("ImsManager")
    }

    @Test
    fun `Metadata carries version info`() {
        val meta = ProfileManager.Metadata(
            version = "1.0.0",
            lastUpdated = "2026-01-01",
            schemaVersion = "1"
        )
        assertThat(meta.version).isEqualTo("1.0.0")
    }

    @Test
    fun `ProfileDatabase with empty profiles`() {
        val db = ProfileManager.ProfileDatabase(
            profiles = emptyList(),
            metadata = ProfileManager.Metadata("1.0", "2026-01-01", "1")
        )
        assertThat(db.profiles).isEmpty()
    }
}
