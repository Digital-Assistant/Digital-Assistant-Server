package com.nistapp.uda.index.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PrefixIndexLayoutStrategyTest {

	@Test
	void emptyPrefixKeepsTheNamesProdUsesToday() {
		PrefixIndexLayoutStrategy s = new PrefixIndexLayoutStrategy("");
		assertEquals("sequencelist-000001", s.createInitialElasticsearchIndexName("sequencelist"));
		assertEquals("sequencelist-read", s.createReadAlias("sequencelist"));
		assertEquals("sequencelist-write", s.createWriteAlias("sequencelist"));
		assertEquals("sequencelist", s.extractUniqueKeyFromHibernateSearchIndexName("sequencelist"));
		assertEquals("sequencelist", s.extractUniqueKeyFromElasticsearchIndexName("sequencelist-000001"));
	}

	@Test
	void prefixIsAddedToIndexAndBothAliases() {
		PrefixIndexLayoutStrategy s = new PrefixIndexLayoutStrategy("test_");
		assertEquals("test_clickTrack-000001", s.createInitialElasticsearchIndexName("clickTrack"));
		assertEquals("test_clickTrack-read", s.createReadAlias("clickTrack"));
		assertEquals("test_clickTrack-write", s.createWriteAlias("clickTrack"));
	}

	@Test
	void uniqueKeysOfHibernateSearchAndElasticsearchSidesMatchIgnoringCase() {
		PrefixIndexLayoutStrategy s = new PrefixIndexLayoutStrategy("test_");
		assertEquals("test_clicktrack", s.extractUniqueKeyFromElasticsearchIndexName("test_clicktrack-000001"));
		assertEquals("test_clicktrack",
				s.extractUniqueKeyFromHibernateSearchIndexName("clickTrack").toLowerCase());
	}

	@Test
	void badPrefixIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new PrefixIndexLayoutStrategy("Test "));
	}
}
