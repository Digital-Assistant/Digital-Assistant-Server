package com.nistapp.uda.index.utils;

import com.google.re2j.Pattern;

/**
 * Utility class for sanitizing user input to prevent XSS, script injection,
 * and other common attack vectors.
 * <p>
 * This sanitizer strips dangerous HTML tags and JavaScript constructs from
 * string inputs. It is designed to be used both in JAX-RS filters and
 * directly in service methods where needed.
 * <p>
 * The patterns use RE2/J, whose matching time is linear in the input length. The
 * sanitizer runs on request bodies of up to several megabytes, where several of
 * these patterns take quadratic time under {@code java.util.regex} (ReDoS). RE2/J
 * gives the same results as {@code java.util.regex} for these patterns, with two
 * exceptions, and in both cases more is removed: case-insensitive keywords also
 * match the Unicode case variants U+017F (long s) and U+212A (Kelvin sign), and
 * {@code \b} does not treat a combining mark after a letter as part of a word.
 * Do not use {@code java.util.regex} here.
 *
 * <h3>Attack vectors covered:</h3>
 * <ul>
 * <li>HTML script tags ({@code <script>})</li>
 * <li>HTML event handlers ({@code onload=, onclick=, onerror=})</li>
 * <li>JavaScript protocol URIs ({@code javascript:})</li>
 * <li>HTML object/embed/iframe tags</li>
 * <li>VBScript protocol URIs</li>
 * <li>Expression evaluation ({@code eval(...)})</li>
 * <li>Data URIs with scripts ({@code data:text/html})</li>
 * </ul>
 *
 * @see <a href=
 *      "https://owasp.org/www-community/xss-filter-evasion-cheatsheet">OWASP
 *      XSS Filter Evasion</a>
 */
public final class InputSanitizer {

	private InputSanitizer() {
		// Utility class — prevent instantiation
	}

	/**
	 * Optional whitespace, as {@code \s*} in {@code java.util.regex}. Written out
	 * because RE2 {@code \s} does not include {@code \x0B}.
	 */
	private static final String WS = "[\\t\\n\\x0B\\f\\r ]*";

	private static final int CI = Pattern.CASE_INSENSITIVE;
	private static final int CI_DOTALL = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;

	// Compiled patterns for performance (compiled once, reused)
	private static final Pattern SCRIPT_TAG = Pattern.compile("<script[^>]*>.*?</script>", CI_DOTALL);
	private static final Pattern SCRIPT_SRC = Pattern.compile("src" + WS + "=" + WS + "'(.*?javascript:.*?)'",
			CI_DOTALL);
	private static final Pattern SCRIPT_OPEN_TAG = Pattern.compile("<script(.*?)>", CI_DOTALL);
	private static final Pattern SCRIPT_CLOSE_TAG = Pattern.compile("</script>", CI);
	private static final Pattern EVAL = Pattern.compile("eval" + WS + "\\((.*)\\)", CI_DOTALL);
	private static final Pattern EXPRESSION = Pattern.compile("expression" + WS + "\\((.*)\\)", CI_DOTALL);
	private static final Pattern JAVASCRIPT_PROTO = Pattern.compile("javascript" + WS + ":", CI);
	private static final Pattern VBSCRIPT_PROTO = Pattern.compile("vbscript" + WS + ":", CI);
	private static final Pattern DATA_PROTO = Pattern.compile("data" + WS + ":.*text/html", CI_DOTALL);
	// Case-insensitive for "on" only: under the CASE_INSENSITIVE flag RE2 \w would also match U+017F and U+212A
	private static final Pattern ON_EVENT = Pattern.compile("\\b(?i:on)\\w+" + WS + "=");
	private static final Pattern IFRAME_TAG = Pattern.compile("<iframe[^>]*>.*?</iframe>", CI_DOTALL);
	private static final Pattern OBJECT_TAG = Pattern.compile("<object[^>]*>.*?</object>", CI_DOTALL);
	private static final Pattern EMBED_TAG = Pattern.compile("<embed[^>]*>.*?</embed>", CI_DOTALL);
	private static final Pattern STYLE_TAG = Pattern.compile("<style[^>]*>.*?</style>", CI_DOTALL);
	private static final Pattern LINK_TAG = Pattern.compile("<link[^>]*>", CI_DOTALL);

	/**
	 * Sanitizes the given string by removing dangerous HTML/JS constructs.
	 * <p>
	 * Returns {@code null} if the input is {@code null}.
	 * Returns the input unchanged if it is empty or blank.
	 *
	 * @param input
	 *            the raw user input
	 * @return sanitized string with dangerous patterns removed
	 */
	public static String sanitize(String input) {
		if (input == null) {
			return null;
		}
		if (input.isBlank()) {
			return input;
		}

		String sanitized = input;

		// Remove script tags and their content
		sanitized = SCRIPT_TAG.matcher(sanitized).replaceAll("");
		sanitized = SCRIPT_SRC.matcher(sanitized).replaceAll("");
		sanitized = SCRIPT_OPEN_TAG.matcher(sanitized).replaceAll("");
		sanitized = SCRIPT_CLOSE_TAG.matcher(sanitized).replaceAll("");

		// Remove eval() and expression()
		sanitized = EVAL.matcher(sanitized).replaceAll("");
		sanitized = EXPRESSION.matcher(sanitized).replaceAll("");

		// Remove protocol handlers
		sanitized = JAVASCRIPT_PROTO.matcher(sanitized).replaceAll("");
		sanitized = VBSCRIPT_PROTO.matcher(sanitized).replaceAll("");
		sanitized = DATA_PROTO.matcher(sanitized).replaceAll("");

		// Remove event handlers (onclick, onload, onerror, etc.)
		sanitized = ON_EVENT.matcher(sanitized).replaceAll("");

		// Remove dangerous HTML elements
		sanitized = IFRAME_TAG.matcher(sanitized).replaceAll("");
		sanitized = OBJECT_TAG.matcher(sanitized).replaceAll("");
		sanitized = EMBED_TAG.matcher(sanitized).replaceAll("");
		sanitized = STYLE_TAG.matcher(sanitized).replaceAll("");
		sanitized = LINK_TAG.matcher(sanitized).replaceAll("");

		return sanitized.trim();
	}

	/**
	 * Checks if the given input contains any potentially dangerous patterns.
	 *
	 * @param input
	 *            the raw user input
	 * @return {@code true} if dangerous patterns are detected
	 */
	public static boolean containsDangerousContent(String input) {
		if (input == null || input.isBlank()) {
			return false;
		}
		return !input.equals(sanitize(input));
	}
}
