package com.nistapp.uda.index.services;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.models.SequenceVotes;
import com.nistapp.uda.index.models.Userclicknodes;
import com.nistapp.uda.index.users.UserAccountService;
import com.nistapp.uda.index.utils.SequenceListStatus;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Ownership of recordings, click nodes and votes is decided by the caller from the token (CurrentUser), never by a
 * {@code usersessionid} in the body, query or path. A and B carry tokens of the legacy confidential client
 * ({@code azp = backend-service}), so their owner value is their authid, like today's extension users. Data is set
 * up directly in the database so that B can be pointed at A's rows.
 */
@QuarkusTest
public class OwnershipTest {

	static final String USER_A = "pk03-test-user-a";
	static final String USER_B = "pk03-test-user-b";

	@Inject
	EntityManager em;

	@Inject
	UserAccountService userAccountService;

	@Inject
	SequenceListStatus sequenceListStatus;

	@Inject
	SearchMapping searchMapping;

	// --- fixtures

	private static String domain() {
		return "pk03-" + UUID.randomUUID() + ".local";
	}

	/** A recording owned by {@code owner}, with one click node and the owner's 0/0 vote, as recordsequencedata makes. */
	private long recording(String owner, String domain, boolean draft) {
		return QuarkusTransaction.requiringNew().call(() -> {
			Userclicknodes node = new Userclicknodes();
			node.setSessionid("pk03-session");
			node.setDomain(domain);
			node.setUrlpath("/");
			node.setClickednodename("pk03 node");
			node.setOwner(owner);
			em.persist(node);
			SequenceList s = new SequenceList();
			s.setName("pk03 recording");
			s.setDomain(domain);
			s.setUsersessionid(owner);
			s.setIsValid(1);
			s.setIsIgnored(0);
			s.setUserclicknodelist(String.valueOf(node.getId()));
			Map<String, Object> params = new HashMap<>();
			if (draft) {
				params.put("status", sequenceListStatus.getDraftStatusId().toString());
			}
			s.setAdditionalParams(params);
			s.setUserclicknodesSet(new HashSet<>(List.of(node)));
			s.setSequenceVotes(new HashSet<>());
			em.persist(s);
			node.setSequenceList(s);
			vote(s.getId(), owner, 0);
			return s.getId();
		});
	}

	private void vote(long sequenceId, String owner, int up) {
		SequenceVotes v = new SequenceVotes();
		v.setSequenceid(sequenceId);
		v.setUsersessionid(owner);
		v.setUpvote(up);
		v.setDownvote(0);
		em.persist(v);
	}

	private void addVote(long sequenceId, String owner, int up) {
		QuarkusTransaction.requiringNew().run(() -> vote(sequenceId, owner, up));
	}

	/** Indexing is write-sync: written, not yet visible to searches. Make it visible. */
	private void refreshIndex() {
		searchMapping.scope(SequenceList.class).workspace().refresh();
	}

	private SequenceList load(long id) {
		return QuarkusTransaction.requiringNew().call(() -> em.find(SequenceList.class, id));
	}

	private Userclicknodes node(long sequenceId) {
		return QuarkusTransaction.requiringNew().call(() -> em.find(SequenceList.class, sequenceId)
				.getUserclicknodesSet().iterator().next());
	}

	private long votes(long sequenceId, String owner) {
		return QuarkusTransaction.requiringNew().call(() -> em.createQuery(
				"select count(v) from SequenceVotes v where v.sequenceid=:s and v.usersessionid=:u", Long.class)
				.setParameter("s", sequenceId).setParameter("u", owner).getSingleResult());
	}

	private static String body(long id, String usersessionid) {
		return "{\"id\":" + id + ",\"usersessionid\":\"" + usersessionid + "\",\"name\":\"renamed by caller\"}";
	}

	// --- recordings: delete

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void bCannotDeleteAsRecordingEvenWithAsIdInTheBody() {
		long seq = recording(USER_A, domain(), false);
		given().contentType(ContentType.JSON).body(body(seq, USER_A))
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(403);
		assertEquals(0, load(seq).getDeleted());
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void aCanDeleteOwnRecordingWhateverTheBodySays() {
		long seq = recording(USER_A, domain(), false);
		given().contentType(ContentType.JSON).body(body(seq, USER_B))
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(200)
				.body("deleted", equalTo(1))
				.body("usersessionid", equalTo(USER_A));
		assertEquals(1, load(seq).getDeleted());
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void deletingAnUnknownRecordingIs404() {
		given().contentType(ContentType.JSON).body(body(Long.MAX_VALUE, USER_A))
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(404);
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void deletingWithoutIdIs400() {
		given().contentType(ContentType.JSON).body("{\"usersessionid\":\"" + USER_A + "\"}")
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(400);
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void oldRowsOwnedByTheNumericUserIdStillBelongToTheirUser() {
		long ownRowId = userAccountService.findOrCreate(USER_A, "google", null).getId();
		long seq = recording(String.valueOf(ownRowId), domain(), false);
		given().contentType(ContentType.JSON).body(body(seq, "anything"))
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(200);
	}

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void oldRowsOwnedByAnotherNumericUserIdAreNotTheCallers() {
		long victimRowId = userAccountService.findOrCreate(USER_A, "google", null).getId();
		long seq = recording(String.valueOf(victimRowId), domain(), false);
		given().contentType(ContentType.JSON).body(body(seq, String.valueOf(victimRowId)))
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(403);
	}

	@Test
	@TestSecurity(user = "424242")
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-num")})
	void aNumericUsernameDoesNotPassAsTheUserWithThatRowId() {
		// A token for username "<A's row id>" must not own rows that hold A's numeric row id.
		long victimRowId = userAccountService.findOrCreate(USER_A, "google", null).getId();
		long seq = recording(String.valueOf(victimRowId), domain(), false);
		// the caller's own authid is "424242", not victimRowId, but a digits-only authid is never compared as text
		given().contentType(ContentType.JSON).body(body(seq, String.valueOf(victimRowId)))
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(403);
		long seq2 = recording("424242", domain(), false);
		given().contentType(ContentType.JSON).body(body(seq2, "424242"))
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(403);
	}

	// --- recordings: update (both paths)

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void bCannotUpdateAsRecording() {
		long seq = recording(USER_A, domain(), true);
		given().contentType(ContentType.JSON).body(body(seq, USER_A))
				.when().post("/api/sequence/update")
				.then().statusCode(403);
		given().contentType(ContentType.JSON)
				.body("{\"id\":" + seq + ",\"usersessionid\":\"" + USER_A + "\",\"additionalParams\":{}}")
				.when().post("/api/clickevents/updatesequencedata")
				.then().statusCode(403);
		SequenceList after = load(seq);
		assertEquals("pk03 recording", after.getName());
		assertEquals(sequenceListStatus.getDraftStatusId().toString(), after.getAdditionalParams().get("status"),
				"the draft must stay a draft");
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void aCanUpdateOwnRecording() {
		long seq = recording(USER_A, domain(), false);
		given().contentType(ContentType.JSON).body(body(seq, USER_B))
				.when().post("/api/sequence/update")
				.then().statusCode(200)
				.body("name", equalTo("renamed by caller"));
		given().contentType(ContentType.JSON)
				.body("{\"id\":" + seq + ",\"additionalParams\":{\"pk03\":\"yes\"}}")
				.when().post("/api/clickevents/updatesequencedata")
				.then().statusCode(200);
		assertEquals("yes", load(seq).getAdditionalParams().get("pk03"));
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void updatingAnUnknownRecordingIs404() {
		given().contentType(ContentType.JSON).body(body(Long.MAX_VALUE, USER_A))
				.when().post("/api/sequence/update")
				.then().statusCode(404);
		given().contentType(ContentType.JSON).body(body(Long.MAX_VALUE, USER_A))
				.when().post("/api/clickevents/updatesequencedata")
				.then().statusCode(404);
	}

	// --- click nodes

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void bCannotChangeAsClickNode() {
		long seq = recording(USER_A, domain(), false);
		Userclicknodes node = node(seq);
		given().contentType(ContentType.JSON)
				.body("{\"id\":" + node.getId() + ",\"sessionid\":\"pk03-session\",\"clickednodename\":\"hijacked\"}")
				.when().post("/api/clickevents/updateclickednode")
				.then().statusCode(403);
		assertEquals("pk03 node", node(seq).getClickednodename());
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void aCanChangeOwnClickNodeFromAnotherSession() {
		long seq = recording(USER_A, domain(), false);
		Userclicknodes node = node(seq);
		given().contentType(ContentType.JSON)
				.body("{\"id\":" + node.getId() + ",\"sessionid\":\"a-later-session\",\"clickednodename\":\"renamed\"}")
				.when().post("/api/clickevents/updateclickednode")
				.then().statusCode(200)
				.body("clickednodename", equalTo("renamed"))
				.body("$", not(hasKey("owner")))
				.body("$", not(hasKey("usersessionid")));
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void changingAnUnknownClickNodeIs404() {
		given().contentType(ContentType.JSON)
				.body("{\"id\":" + Long.MAX_VALUE + ",\"sessionid\":\"x\",\"clickednodename\":\"x\"}")
				.when().post("/api/clickevents/updateclickednode")
				.then().statusCode(404);
	}

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void oldClickNodeWithoutOwnerFollowsItsRecording() {
		long seq = recording(USER_A, domain(), false);
		long nodeId = node(seq).getId();
		QuarkusTransaction.requiringNew().run(() -> em.find(Userclicknodes.class, nodeId).setOwner(null));
		given().contentType(ContentType.JSON)
				.body("{\"id\":" + nodeId + ",\"sessionid\":\"pk03-session\",\"clickednodename\":\"hijacked\"}")
				.when().post("/api/clickevents/updateclickednode")
				.then().statusCode(403);
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void newClickNodeIsOwnedByTheCallerNotTheBody() {
		Number id = given().contentType(ContentType.JSON)
				.body("{\"sessionid\":\"pk03-new\",\"usersessionid\":\"" + USER_B + "\",\"domain\":\"" + domain()
						+ "\",\"urlpath\":\"/\",\"clickednodename\":\"n\"}")
				.when().post("/api/clickevents/clickednode")
				.then().statusCode(200)
				.body("$", not(hasKey("owner")))
				.extract().path("id");
		Userclicknodes stored = QuarkusTransaction.requiringNew()
				.call(() -> em.find(Userclicknodes.class, id.longValue()));
		assertEquals(USER_A, stored.getOwner());
	}

	// --- record sequence

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void recordingIsOwnedByTheCallerNotTheBody() {
		String d = domain();
		Number nodeId = given().contentType(ContentType.JSON)
				.body("{\"sessionid\":\"pk03-rec\",\"domain\":\"" + d + "\",\"urlpath\":\"/\",\"clickednodename\":\"n\"}")
				.when().post("/api/clickevents/clickednode")
				.then().statusCode(200).extract().path("id");
		Number seq = given().contentType(ContentType.JSON)
				.body("{\"name\":\"pk03 rec\",\"domain\":\"" + d + "\",\"usersessionid\":\"" + USER_B
						+ "\",\"isValid\":1,\"isIgnored\":0,\"userclicknodelist\":\"" + nodeId
						+ "\",\"userclicknodesSet\":[{\"id\":" + nodeId + "}]}")
				.when().post("/api/clickevents/recordsequencedata")
				.then().statusCode(200)
				.body("usersessionid", equalTo(USER_A))
				.body("sequenceVotes[0]", not(hasKey("usersessionid")))
				.extract().path("id");
		assertEquals(USER_A, load(seq.longValue()).getOwner());
		assertEquals(1, votes(seq.longValue(), USER_A));
		assertEquals(0, votes(seq.longValue(), USER_B));
	}

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void bCannotMoveAsClickNodesIntoBsRecording() {
		String d = domain();
		long seq = recording(USER_A, d, false);
		long nodeId = node(seq).getId();
		given().contentType(ContentType.JSON)
				.body("{\"name\":\"stolen\",\"domain\":\"" + d + "\",\"isValid\":1,\"isIgnored\":0,"
						+ "\"userclicknodesSet\":[{\"id\":" + nodeId + "}]}")
				.when().post("/api/clickevents/recordsequencedata")
				.then().statusCode(403);
		assertEquals(seq, QuarkusTransaction.requiringNew()
				.call(() -> em.find(Userclicknodes.class, nodeId).getSequenceList().getId()));
	}

	@Test
	@TestSecurity(user = "pk03-sub-0001")
	@OidcSecurity(claims = {@Claim(key = "azp", value = "udan-extension"), @Claim(key = "sub", value = "pk03-sub-0001")})
	void nonLegacyCallerOwnsByNumericUserId() {
		long rowId = userAccountService.findOrCreate("pk03-sub-0001", "keycloak", null).getId();
		Number seq = given().contentType(ContentType.JSON)
				.body("{\"name\":\"pk03 kc\",\"domain\":\"" + domain() + "\",\"isValid\":1,\"isIgnored\":0,"
						+ "\"userclicknodesSet\":[]}")
				.when().post("/api/clickevents/recordsequencedata")
				.then().statusCode(200)
				.body("usersessionid", equalTo(String.valueOf(rowId)))
				.extract().path("id");
		assertEquals(String.valueOf(rowId), load(seq.longValue()).getOwner());
		given().contentType(ContentType.JSON).body(body(seq.longValue(), "x"))
				.when().post("/api/clickevents/sequence/delete")
				.then().statusCode(200);
	}

	// --- votes

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void bCannotRemoveAsVote() {
		long seq = recording(USER_A, domain(), false);
		given().when().delete("/api/votes/deleteVote/" + seq + "/" + USER_A)
				.then().statusCode(404);
		assertEquals(1, votes(seq, USER_A));
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void deleteVoteRemovesOnlyTheCallersVoteOnThatRecording() {
		String d = domain();
		long seq1 = recording(USER_B, d, false);
		long seq2 = recording(USER_B, d, false);
		addVote(seq1, USER_A, 1);
		addVote(seq2, USER_A, 1);
		// The path user is ignored; the path id is honoured (the old code ignored it).
		given().when().delete("/api/votes/deleteVote/" + seq1 + "/" + USER_B)
				.then().statusCode(204);
		assertEquals(0, votes(seq1, USER_A));
		assertEquals(1, votes(seq1, USER_B), "the recorder's own vote row stays");
		assertEquals(1, votes(seq2, USER_A), "the vote on another recording stays");
	}

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void addVoteRecordsTheCallersVoteNotTheBodyUsers() {
		long seq = recording(USER_A, domain(), false);
		given().contentType(ContentType.JSON)
				.body("{\"sequenceid\":" + seq + ",\"usersessionid\":\"" + USER_A + "\",\"upvote\":1,\"downvote\":0}")
				.when().post("/api/votes/addVote")
				.then().statusCode(200)
				.body("upVoteCount", equalTo(1))
				.body("usersessionid", nullValue());
		assertEquals(1, votes(seq, USER_B));
		SequenceList after = load(seq);
		SequenceVotes aVote = after.getSequenceVotes().stream()
				.filter(v -> USER_A.equals(v.getUsersessionid())).findFirst().orElseThrow();
		assertEquals(0, aVote.getUpvote(), "A's own vote row is untouched");
		// voting again updates the same row
		given().contentType(ContentType.JSON)
				.body("{\"sequenceid\":" + seq + ",\"upvote\":0,\"downvote\":1}")
				.when().post("/api/votes/addVote")
				.then().statusCode(200)
				.body("downVoteCount", equalTo(1));
		assertEquals(1, votes(seq, USER_B));
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void addVoteOnUnknownRecordingIs404() {
		given().contentType(ContentType.JSON)
				.body("{\"sequenceid\":" + Long.MAX_VALUE + ",\"upvote\":1,\"downvote\":0}")
				.when().post("/api/votes/addVote")
				.then().statusCode(404);
	}

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void voteRecordIsTheCallersAndVoterIdsAreHidden() {
		long seq = recording(USER_A, domain(), false);
		addVote(seq, USER_A, 1);
		// Asking for A's vote returns B's own (none yet: 204)
		given().when().get("/api/votes/" + seq + "/" + USER_A).then().statusCode(204);
		addVote(seq, USER_B, 1);
		given().when().get("/api/votes/" + seq + "/" + USER_A)
				.then().statusCode(200)
				.body("upvote", equalTo(1))
				.body("$", not(hasKey("usersessionid")));
		given().when().get("/api/votes/" + seq)
				.then().statusCode(200)
				.body("size()", equalTo(3))
				.body("findAll { it.usersessionid != null }", empty());
	}

	// --- search: own drafts from the token, owner ids only to the owner

	@Test
	@TestSecurity(user = USER_B)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-b")})
	void bCannotListAsDraftsBySendingAsId() {
		String d = domain();
		long draft = recording(USER_A, d, true);
		long published = recording(USER_A, d, false);
		refreshIndex();
		given().queryParam("domain", d).queryParam("userSessionId", USER_A).queryParam("query", "")
				.when().get("/api/search/withPermissions")
				.then().statusCode(200)
				.body("size()", equalTo(1))
				.body("[0].id", equalTo((int) published))
				.body("[0].usersessionid", nullValue())
				.body("[0].sequenceVotes.findAll { it.usersessionid != null }", empty());
		given().queryParam("domain", d).queryParam("userSessionId", USER_A)
				.when().get("/api/search/all")
				.then().statusCode(200)
				.body("size()", equalTo(1));
		given().queryParam("domain", d).queryParam("userSessionId", USER_A)
				.when().get("/api/search/withPermissions/" + draft)
				.then().statusCode(anyOf(is(200), is(204)))
				.body(not(containsString("\"id\"")));
		given().queryParam("domain", d)
				.when().get("/api/search/" + published)
				.then().statusCode(200)
				.body("usersessionid", nullValue());
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void aSeesOwnDraftsAndOwnIdWhateverTheParameterSays() {
		String d = domain();
		long draft = recording(USER_A, d, true);
		recording(USER_A, d, false);
		refreshIndex();
		given().queryParam("domain", d).queryParam("userSessionId", USER_B).queryParam("query", "")
				.when().get("/api/search/withPermissions")
				.then().statusCode(200)
				.body("size()", equalTo(2))
				.body("usersessionid", everyItem(equalTo(USER_A)));
		given().queryParam("domain", d).queryParam("userSessionId", USER_B)
				.when().get("/api/search/withPermissions/" + draft)
				.then().statusCode(200)
				.body("id", equalTo((int) draft));
		// search/{id} (no draft filter): the owner id is shown to the owner only
		given().queryParam("domain", d)
				.when().get("/api/search/" + draft)
				.then().statusCode(200)
				.body("usersessionid", equalTo(USER_A));
	}

	@Test
	@TestSecurity(user = USER_A)
	@OidcSecurity(claims = {@Claim(key = "azp", value = "backend-service"), @Claim(key = "sub", value = "kc-sub-a")})
	void ownUserRowDoesNotReturnTheEmailHash() {
		given().contentType(ContentType.JSON)
				.body("{\"authid\":\"" + USER_A + "\",\"authsource\":\"google\",\"emailid\":\"pk03-hash\"}")
				.when().post("/api/user/checkauthid")
				.then().statusCode(200)
				.body("authid", equalTo(USER_A))
				.body("$", not(hasKey("emailid")));
		// the body value is still read: a row without an email hash gets it
		assertNotNull(userAccountService.findOrCreate(USER_A, "google", null).getEmailid());
	}

	@Test
	void anonymousCallsAreStill401() {
		given().contentType(ContentType.JSON).body(body(1, USER_A))
				.when().post("/api/clickevents/updateclickednode")
				.then().statusCode(401);
	}
}
