package com.supermarsx.carrierconfig.data.repository

import com.google.common.truth.Truth.assertThat
import com.supermarsx.carrierconfig.data.model.*
import org.junit.Before
import org.junit.Test

/**
 * Extensive edge-case tests for [CarrierConfigRepository.generateXML]
 * covering XML special-character escaping, boundary inputs, and
 * malicious payloads.
 */
class XmlEscapingEdgeCaseTest {

    private lateinit var repo: CarrierConfigRepository

    @Before
    fun setUp() {
        repo = CarrierConfigRepository(org.mockito.kotlin.mock())
    }

    // =========================================================================
    // XML special character escaping
    // =========================================================================

    @Test
    fun `string value with ampersand is escaped`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("AT&T"))
        ))
        assertThat(xml).contains("AT&amp;T")
        assertThat(xml).doesNotContain("AT&T</")
    }

    @Test
    fun `string value with less-than is escaped`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("a < b"))
        ))
        assertThat(xml).contains("a &lt; b")
        assertThat(xml).doesNotContain("a < b</")
    }

    @Test
    fun `string value with greater-than is escaped`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("a > b"))
        ))
        assertThat(xml).contains("a &gt; b")
    }

    @Test
    fun `string value with double quote is escaped`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("say \"hello\""))
        ))
        assertThat(xml).contains("say &quot;hello&quot;")
    }

    @Test
    fun `string value with single quote is escaped`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("it's"))
        ))
        assertThat(xml).contains("it&apos;s")
    }

    @Test
    fun `string value with all XML specials combined`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("<script>&\"'test"))
        ))
        assertThat(xml).contains("&lt;script&gt;")
        assertThat(xml).contains("&amp;")
        assertThat(xml).contains("&quot;")
        assertThat(xml).contains("&apos;")
        assertThat(xml).doesNotContain("<script>")
    }

    @Test
    fun `key name with special chars is escaped in attribute`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("bad&key<>", ConfigValue.BooleanValue(true))
        ))
        assertThat(xml).contains("bad&amp;key&lt;&gt;")
        assertThat(xml).doesNotContain("bad&key<>")
    }

    @Test
    fun `string array items with special chars are escaped`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("arr", ConfigValue.StringArrayValue(listOf("a&b", "<tag>", "x\"y")))
        ))
        assertThat(xml).contains("<item>a&amp;b</item>")
        assertThat(xml).contains("<item>&lt;tag&gt;</item>")
        assertThat(xml).contains("<item>x&quot;y</item>")
    }

    @Test
    fun `XML injection attempt in string value`() {
        val malicious = "</string><boolean name=\"hacked\" value=\"true\" /><string name=\"x\">"
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue(malicious))
        ))
        // The malicious payload should be fully escaped, not interpreted as XML
        assertThat(xml).doesNotContain("<boolean name=\"hacked\"")
        assertThat(xml).contains("&lt;/string&gt;")
    }

    @Test
    fun `CDATA-like injection attempt is escaped`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("]]><!--hack-->"))
        ))
        assertThat(xml).doesNotContain("<!--hack-->")
        assertThat(xml).contains("&gt;&lt;!--hack--&gt;")
    }

    // =========================================================================
    // Boundary value tests
    // =========================================================================

    @Test
    fun `empty string value produces valid XML`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue(""))
        ))
        assertThat(xml).contains("<string name=\"key\"></string>")
    }

    @Test
    fun `empty key name produces valid XML`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("", ConfigValue.BooleanValue(true))
        ))
        assertThat(xml).contains("name=\"\"")
    }

    @Test
    fun `very long string value is preserved`() {
        val longValue = "x".repeat(10_000)
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue(longValue))
        ))
        assertThat(xml).contains(longValue)
    }

    @Test
    fun `empty string array produces valid wrapper`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("arr", ConfigValue.StringArrayValue(emptyList()))
        ))
        assertThat(xml).contains("<string-array name=\"arr\">")
        assertThat(xml).contains("</string-array>")
        assertThat(xml).doesNotContain("<item>")
    }

    @Test
    fun `unicode string value is preserved`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("日本語テスト 🎉"))
        ))
        assertThat(xml).contains("日本語テスト 🎉")
    }

    @Test
    fun `IntValue with negative number`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.IntValue(-1))
        ))
        assertThat(xml).contains("value=\"-1\"")
    }

    @Test
    fun `IntValue with Int MAX_VALUE`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.IntValue(Int.MAX_VALUE))
        ))
        assertThat(xml).contains("value=\"${Int.MAX_VALUE}\"")
    }

    @Test
    fun `IntValue with Int MIN_VALUE`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.IntValue(Int.MIN_VALUE))
        ))
        assertThat(xml).contains("value=\"${Int.MIN_VALUE}\"")
    }

    @Test
    fun `multiple keys of same type don't collide`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("a_bool", ConfigValue.BooleanValue(true)),
            ConfigKey("b_bool", ConfigValue.BooleanValue(false))
        ))
        assertThat(xml).contains("a_bool")
        assertThat(xml).contains("b_bool")
        assertThat(xml.indexOf("a_bool")).isLessThan(xml.indexOf("b_bool"))
    }

    @Test
    fun `large number of keys are all present`() {
        val keys = (1..100).map { ConfigKey("key_$it", ConfigValue.IntValue(it)) }
        val xml = repo.generateXML(keys)
        for (i in 1..100) {
            assertThat(xml).contains("key_$i")
        }
    }

    @Test
    fun `newlines in string value are preserved`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("line1\nline2\nline3"))
        ))
        assertThat(xml).contains("line1\nline2\nline3")
    }

    @Test
    fun `string value with only special chars`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("key", ConfigValue.StringValue("<>&\"'"))
        ))
        assertThat(xml).contains("&lt;&gt;&amp;&quot;&apos;")
    }

    // =========================================================================
    // XML structure validation
    // =========================================================================

    @Test
    fun `output always starts with XML declaration`() {
        val xml = repo.generateXML(emptyList())
        assertThat(xml.trimStart()).startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
    }

    @Test
    fun `output always has carrier_config root element`() {
        val xml = repo.generateXML(emptyList())
        assertThat(xml).contains("<carrier_config>")
        assertThat(xml).contains("</carrier_config>")
    }

    @Test
    fun `carrier_config close tag appears after open tag`() {
        val xml = repo.generateXML(listOf(
            ConfigKey("k", ConfigValue.BooleanValue(true))
        ))
        val openIdx = xml.indexOf("<carrier_config>")
        val closeIdx = xml.indexOf("</carrier_config>")
        assertThat(openIdx).isLessThan(closeIdx)
    }
}
