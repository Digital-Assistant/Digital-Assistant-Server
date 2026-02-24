package com.nistapp.uda.index.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InputSanitizer}.
 * Covers XSS, script injection, event handlers, protocol attacks, and safe
 * inputs.
 */
class InputSanitizerTest {

    @Test
    void testNullInput() {
        assertNull(InputSanitizer.sanitize(null));
    }

    @Test
    void testEmptyInput() {
        assertEquals("", InputSanitizer.sanitize(""));
    }

    @Test
    void testSafeInput() {
        String safe = "Hello World! This is a normal query string.";
        assertEquals(safe, InputSanitizer.sanitize(safe));
    }

    @Test
    void testScriptTagRemoval() {
        String malicious = "Hello<script>alert('XSS')</script>World";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("<script>"));
        assertFalse(result.contains("</script>"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World"));
    }

    @Test
    void testScriptTagCaseInsensitive() {
        String malicious = "<SCRIPT>document.cookie</SCRIPT>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.toLowerCase().contains("<script"));
    }

    @Test
    void testJavascriptProtocol() {
        String malicious = "javascript:alert('XSS')";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("javascript:"));
    }

    @Test
    void testEventHandlerRemoval() {
        String malicious = "<img onerror=\"alert('XSS')\" src='x'/>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("onerror="));
    }

    @Test
    void testOnClickRemoval() {
        String malicious = "<div onclick=\"stealCookies();\">Click me</div>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("onclick="));
    }

    @Test
    void testIframeRemoval() {
        String malicious = "<iframe src='http://evil.com'></iframe>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("<iframe"));
    }

    @Test
    void testEvalRemoval() {
        String malicious = "eval(document.cookie)";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("eval("));
    }

    @Test
    void testVbscriptRemoval() {
        String malicious = "vbscript:msgbox('XSS')";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("vbscript:"));
    }

    @Test
    void testDataProtocolRemoval() {
        String malicious = "data:text/html,<script>alert('XSS')</script>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("data:text/html"));
    }

    @Test
    void testObjectTagRemoval() {
        String malicious = "<object data='malicious.swf'></object>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("<object"));
    }

    @Test
    void testEmbedTagRemoval() {
        String malicious = "<embed src='malicious.swf'></embed>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("<embed"));
    }

    @Test
    void testContainsDangerousContent() {
        assertTrue(InputSanitizer.containsDangerousContent("<script>alert('XSS')</script>"));
        assertTrue(InputSanitizer.containsDangerousContent("javascript:void(0)"));
        assertTrue(InputSanitizer.containsDangerousContent("<img onerror=\"alert('XSS')\"/>"));
        assertFalse(InputSanitizer.containsDangerousContent("normal text"));
        assertFalse(InputSanitizer.containsDangerousContent(null));
        assertFalse(InputSanitizer.containsDangerousContent(""));
    }

    @Test
    void testJsonPayloadSanitization() {
        String maliciousJson = "{\"name\":\"<script>alert('XSS')</script>Test\",\"domain\":\"example.com\"}";
        String result = InputSanitizer.sanitize(maliciousJson);
        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("\"name\""));
        assertTrue(result.contains("example.com"));
    }

    @Test
    void testStyleTagRemoval() {
        String malicious = "<style>body{background:url('javascript:alert(1)')}</style>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.contains("<style"));
    }

    @Test
    void testNestedAttack() {
        String malicious = "<scr<script>ipt>alert('XSS')</scr</script>ipt>";
        String result = InputSanitizer.sanitize(malicious);
        assertFalse(result.toLowerCase().contains("<script"));
    }
}
