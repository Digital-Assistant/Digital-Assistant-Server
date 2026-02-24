package com.nistapp.uda.index.utils;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user input to prevent XSS, script injection,
 * and other common attack vectors.
 * <p>
 * This sanitizer strips dangerous HTML tags and JavaScript constructs from
 * string inputs. It is designed to be used both in JAX-RS filters and
 * directly in service methods where needed.
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

    // Compiled patterns for performance (compiled once, reused)
    private static final Pattern SCRIPT_TAG = Pattern.compile(
            "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SCRIPT_SRC = Pattern.compile(
            "src\\s*=\\s*'(.*?javascript:.*?)'", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SCRIPT_OPEN_TAG = Pattern.compile(
            "<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SCRIPT_CLOSE_TAG = Pattern.compile(
            "</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVAL = Pattern.compile(
            "eval\\s*\\((.*)\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EXPRESSION = Pattern.compile(
            "expression\\s*\\((.*)\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PROTO = Pattern.compile(
            "javascript\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PROTO = Pattern.compile(
            "vbscript\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_PROTO = Pattern.compile(
            "data\\s*:.*text/html", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ON_EVENT = Pattern.compile(
            "\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern IFRAME_TAG = Pattern.compile(
            "<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern OBJECT_TAG = Pattern.compile(
            "<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EMBED_TAG = Pattern.compile(
            "<embed[^>]*>.*?</embed>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern STYLE_TAG = Pattern.compile(
            "<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern LINK_TAG = Pattern.compile(
            "<link[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Sanitizes the given string by removing dangerous HTML/JS constructs.
     * <p>
     * Returns {@code null} if the input is {@code null}.
     * Returns the input unchanged if it is empty or blank.
     *
     * @param input the raw user input
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
     * @param input the raw user input
     * @return {@code true} if dangerous patterns are detected
     */
    public static boolean containsDangerousContent(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return !input.equals(sanitize(input));
    }
}
