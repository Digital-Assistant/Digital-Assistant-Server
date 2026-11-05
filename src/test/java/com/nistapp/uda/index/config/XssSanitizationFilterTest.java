package com.nistapp.uda.index.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nistapp.uda.index.utils.InputSanitizer;
import io.quarkus.runtime.configuration.MemorySize;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.jboss.logmanager.LogContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class XssSanitizationFilterTest {

	private static final long LIMIT = 64;
	private static final String PATH = "clickevents/recordsequencedata";
	private static final String MARKER = "SECRETMARKERTEXT";

	private XssSanitizationFilter filter;

	@BeforeEach
	void setUp() {
		filter = new XssSanitizationFilter();
		filter.maxBodySize = new MemorySize(BigInteger.valueOf(LIMIT));
	}

	/** Counts the bytes handed out by the wrapped stream. */
	static class XssSanitizationCountingStream extends InputStream {
		private final InputStream delegate;
		long bytesRead;
		boolean touched;

		XssSanitizationCountingStream(byte[] data) {
			this.delegate = new ByteArrayInputStream(data);
		}

		@Override
		public int read() throws IOException {
			touched = true;
			int b = delegate.read();
			if (b >= 0) {
				bytesRead++;
			}
			return b;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			touched = true;
			int n = delegate.read(b, off, len);
			if (n > 0) {
				bytesRead += n;
			}
			return n;
		}
	}

	/** Hand-written stub state of a ContainerRequestContext. */
	static class XssSanitizationStubRequest {
		String method = "POST";
		MediaType mediaType = new MediaType("application", "json");
		Map<String, String> headers = new HashMap<>();
		XssSanitizationCountingStream stream;
		InputStream replacedStream;
		Response aborted;
		ContainerRequestContext context;

		XssSanitizationStubRequest(String method, MediaType mediaType, byte[] body) {
			this.method = method;
			this.mediaType = mediaType;
			this.stream = new XssSanitizationCountingStream(body);
			UriInfo uriInfo = (UriInfo) Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[]{UriInfo.class},
					(proxy, m, args) -> {
						switch (m.getName()) {
							case "getQueryParameters" :
								return new MultivaluedHashMap<String, String>();
							case "getPath" :
								return PATH;
							default :
								throw new UnsupportedOperationException(m.getName());
						}
					});
			this.context = (ContainerRequestContext) Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[]{ContainerRequestContext.class}, (proxy, m, args) -> {
						switch (m.getName()) {
							case "getMethod" :
								return this.method;
							case "getMediaType" :
								return this.mediaType;
							case "getHeaderString" :
								return headers.get((String) args[0]);
							case "getEntityStream" :
								return stream;
							case "setEntityStream" :
								replacedStream = (InputStream) args[0];
								return null;
							case "getUriInfo" :
								return uriInfo;
							case "abortWith" :
								aborted = (Response) args[0];
								return null;
							default :
								throw new UnsupportedOperationException(m.getName());
						}
					});
		}
	}

	private static XssSanitizationStubRequest json(String method, byte[] body) {
		return new XssSanitizationStubRequest(method, new MediaType("application", "json"), body);
	}

	private static byte[] bytes(int n, char c) {
		byte[] b = new byte[n];
		Arrays.fill(b, (byte) c);
		return b;
	}

	private static byte[] overLimitBody(long factor) {
		byte[] body = bytes((int) (LIMIT * factor), 'a');
		byte[] marker = MARKER.getBytes(StandardCharsets.UTF_8);
		System.arraycopy(marker, 0, body, 0, marker.length);
		return body;
	}

	private static void assertTooLarge(XssSanitizationStubRequest req) {
		assertNotNull(req.aborted, "request should be aborted");
		assertEquals(413, req.aborted.getStatus());
		assertNull(req.replacedStream);
	}

	@Test
	void underLimitWithoutXssPassesSameBytes() throws IOException {
		byte[] body = "{\"a\":\"hello\"}".getBytes(StandardCharsets.UTF_8);
		XssSanitizationStubRequest req = json("POST", body);
		filter.filter(req.context);
		assertNull(req.aborted);
		assertArrayEquals(body, req.replacedStream.readAllBytes());
	}

	@Test
	void exactlyAtLimitPasses() throws IOException {
		byte[] body = bytes((int) LIMIT, 'a');
		XssSanitizationStubRequest req = json("PUT", body);
		req.headers.put("Content-Length", String.valueOf(LIMIT));
		filter.filter(req.context);
		assertNull(req.aborted);
		assertArrayEquals(body, req.replacedStream.readAllBytes());
	}

	@Test
	void underLimitWithXssIsSanitized() throws IOException {
		String text = "{\"a\":\"<script>alert(1)</script>\"}";
		String expected = InputSanitizer.sanitize(text);
		assertTrue(!text.equals(expected));
		XssSanitizationStubRequest req = json("PATCH", text.getBytes(StandardCharsets.UTF_8));
		filter.filter(req.context);
		assertNull(req.aborted);
		assertEquals(expected, new String(req.replacedStream.readAllBytes(), StandardCharsets.UTF_8));
	}

	@Test
	void contentLengthAboveLimitReadsNothing() throws IOException {
		XssSanitizationStubRequest req = json("POST", overLimitBody(100));
		req.headers.put("Content-Length", String.valueOf(LIMIT * 100));
		filter.filter(req.context);
		assertTooLarge(req);
		assertFalse(req.stream.touched);
		assertEquals(0, req.stream.bytesRead);
	}

	@Test
	void noContentLengthBoundedRead() throws IOException {
		XssSanitizationStubRequest req = json("POST", overLimitBody(100));
		filter.filter(req.context);
		assertTooLarge(req);
		assertTrue(req.stream.bytesRead <= LIMIT + 1, "read " + req.stream.bytesRead);
	}

	@Test
	void contentLengthSmallerThanBodyBoundedRead() throws IOException {
		XssSanitizationStubRequest req = json("POST", overLimitBody(100));
		req.headers.put("Content-Length", "10");
		filter.filter(req.context);
		assertTooLarge(req);
		assertTrue(req.stream.bytesRead <= LIMIT + 1, "read " + req.stream.bytesRead);
	}

	@Test
	void invalidContentLengthUsesBoundedRead() throws IOException {
		for (String value : new String[]{"abc", "-5"}) {
			XssSanitizationStubRequest req = json("POST", overLimitBody(100));
			req.headers.put("Content-Length", value);
			filter.filter(req.context);
			assertTooLarge(req);
			assertTrue(req.stream.touched, value);
			assertTrue(req.stream.bytesRead <= LIMIT + 1, "read " + req.stream.bytesRead);
		}
	}

	@Test
	void rejectionResponseIsProblemJson() throws IOException {
		XssSanitizationStubRequest req = json("POST", overLimitBody(10));
		filter.filter(req.context);
		assertTooLarge(req);
		assertEquals(MediaType.APPLICATION_JSON_TYPE, req.aborted.getMediaType());
		@SuppressWarnings("unchecked")
		Map<String, Object> entity = (Map<String, Object>) req.aborted.getEntity();
		assertEquals("https://tools.ietf.org/html/rfc7807", entity.get("type"));
		assertEquals("Request Entity Too Large", entity.get("title"));
		assertEquals(413, entity.get("status"));
		assertNotNull(entity.get("detail"));
		assertNotNull(entity.get("timestamp"));
		assertFalse(entity.toString().contains(MARKER));
	}

	@Test
	void getAndNonJsonAreNotChecked() throws IOException {
		XssSanitizationStubRequest get = json("GET", overLimitBody(100));
		filter.filter(get.context);
		assertNull(get.aborted);
		assertFalse(get.stream.touched);

		XssSanitizationStubRequest text = new XssSanitizationStubRequest("POST", MediaType.TEXT_PLAIN_TYPE,
				overLimitBody(100));
		text.headers.put("Content-Length", String.valueOf(LIMIT * 100));
		filter.filter(text.context);
		assertNull(text.aborted);
		assertFalse(text.stream.touched);
	}

	/** Collects the formatted messages of log records. */
	static class XssSanitizationLogCapture extends Handler {
		final List<String> messages = new ArrayList<>();

		@Override
		public void publish(LogRecord record) {
			if (record.getLevel().intValue() >= java.util.logging.Level.WARNING.intValue()) {
				messages.add(record instanceof org.jboss.logmanager.ExtLogRecord ext
						? ext.getFormattedMessage()
						: record.getMessage());
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() {
		}
	}

	private List<String> captureWarnings(XssSanitizationStubRequest req) throws IOException {
		org.jboss.logmanager.Logger logger = LogContext.getLogContext()
				.getLogger(XssSanitizationFilter.class.getName());
		XssSanitizationLogCapture capture = new XssSanitizationLogCapture();
		logger.addHandler(capture);
		try {
			filter.filter(req.context);
		} finally {
			logger.removeHandler(capture);
		}
		return capture.messages;
	}

	@Test
	void contentLengthRejectionLogsOneWarnWithoutBody() throws IOException {
		XssSanitizationStubRequest req = json("POST", overLimitBody(100));
		req.headers.put("Content-Length", String.valueOf(LIMIT * 100));
		List<String> lines = captureWarnings(req);
		assertEquals(1, lines.size(), lines.toString());
		assertTrue(lines.get(0).contains(PATH));
		assertTrue(lines.get(0).contains("POST"));
		assertTrue(lines.get(0).contains(String.valueOf(LIMIT * 100)));
		assertTrue(lines.get(0).contains("unknown"));
		assertFalse(lines.get(0).contains(MARKER));
	}

	@Test
	void boundedReadRejectionLogsOneWarnWithoutBody() throws IOException {
		XssSanitizationStubRequest req = json("POST", overLimitBody(100));
		List<String> lines = captureWarnings(req);
		assertEquals(1, lines.size(), lines.toString());
		assertTrue(lines.get(0).contains(PATH));
		assertTrue(lines.get(0).contains("over limit"));
		assertFalse(lines.get(0).contains(MARKER));
	}
}
