package com.nistapp.uda.index.utils;

import java.time.Duration;

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

	/**
	 * Expected outputs recorded from the java.util.regex patterns that RE2/J replaced, so the engine change keeps the
	 * sanitizer's results.
	 */
	@Test
	void testSameResultsAsJavaUtilRegex() {
		String[][] cases = {
				{"a<script src=x>alert(1)</script>b", "ab"},
				{"<SCRIPT>x</script>keep<script>y", "keepy"},
				{"<img src='javascript:alert(1)'>", "<img >"},
				{"src = 'a' b javascript:c' d", "d"},
				{"src\u000B=\u000B'javascript:x' tail", "tail"},
				{"x eval (a)(b) y) z", "x  z"},
				{"Use the expression (a+b) and see (c) end", "Use the  end"},
				{"data: x text/html y text/html z", "z"},
				{"<a onclick =go() onmouseover=x>", "<a go() x>"},
				{"button_onclick=1 x", "button_onclick=1 x"},
				{"\u00E9onload=x", "\u00E9x"},
				{"on\u212A=1", "on\u212A=1"},
				{"<iframe src=x>i</iframe>|<object>o</object>|<embed>e</embed>|<style>s</style>|<link rel=x>", "||||"},
				{"vbscript : x JavaScript\t:y", "x y"},
				{"<script never closed", "<script never closed"},
				{"<iframe>never closed", "<iframe>never closed"},
				{"eval( no close", "eval( no close"},
				{"{\"name\":\"Save\",\"html\":\"<b onclick=\\\"x()\\\">Save</b>\"}",
						"{\"name\":\"Save\",\"html\":\"<b \\\"x()\\\">Save</b>\"}"},
		};
		for (String[] c : cases) {
			assertEquals(c[1], InputSanitizer.sanitize(c[0]), () -> "input: " + c[0]);
		}
	}

	/** RE2/J removes more than java.util.regex did for these inputs (see the InputSanitizer Javadoc). */
	@Test
	void testRe2jDifferencesRemoveMore() {
		assertEquals("y", InputSanitizer.sanitize("<\u017Fcript>x</script>y"));
		assertEquals("alert(1)", InputSanitizer.sanitize("java\u017Fcript:alert(1)"));
		assertEquals("y", InputSanitizer.sanitize("<lin\u212A href=x>y"));
		assertEquals("a\u0301x", InputSanitizer.sanitize("a\u0301onload=x"));
	}

	/**
	 * Inputs from the CodeQL java/polynomial-redos alerts. Under java.util.regex these took quadratic time (several
	 * seconds at this size); RE2/J is linear.
	 */
	@Test
	void testLinearTimeOnReDoSInputs() {
		String[][] inputs = {{"", "<script"}, {"", "<script>"}, {"src='", "a"}, {"src='", "javascript:"},
				{"<script", "<scripta"}, {"eval(", "a"}, {"expression(", "a"}, {"", "data:"}, {"", "bon"},
				{"", "<iframe"}, {"", "<iframe>"}, {"", "<object"}, {"", "<object>"}, {"", "<embed"},
				{"", "<embed>"}, {"", "<style"}, {"", "<style>"}, {"", "<link"}};
		for (String[] in : inputs) {
			String input = in[0] + in[1].repeat(128 * 1024 / in[1].length());
			assertTimeoutPreemptively(Duration.ofSeconds(2), () -> InputSanitizer.sanitize(input),
					() -> "input: " + in[0] + " then " + in[1] + " repeated");
		}
	}
}
