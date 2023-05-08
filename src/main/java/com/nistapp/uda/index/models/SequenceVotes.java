package com.nistapp.uda.index.models;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sequencevotes")
public class SequenceVotes extends PanacheEntityBase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	@GenericField
	private long id;

	@GenericField
	@Column(length = 500)
	private String usersessionid;

	@GenericField
	private long sequenceid;

	@GenericField
	private Integer upvote;

	@GenericField
	private Integer downvote;

	@Column(name = "createdat", nullable = false)
	@GenericField
	private long createdat;

	@Column(name = "updatedat", nullable = false)
	@GenericField
	private long updatedat;

	@PrePersist
	public void preSave() {
		this.createdat = this.updatedat = Instant.now().toEpochMilli();
	}

	@PreUpdate
	public void preUpdate(){
		this.updatedat = Instant.now().toEpochMilli();
	}

	/*@ManyToOne
	@JoinColumn(name = "sequenceid", insertable = false, updatable = false, nullable = true)
	private SequenceList sequenceList;*/

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsersessionid() {
		return usersessionid;
	}

	public void setUsersessionid(String usersessionid) {
		this.usersessionid = usersessionid;
	}

	public long getSequenceid() {
		return sequenceid;
	}

	public void setSequenceid(long sequenceid) {
		this.sequenceid = sequenceid;
	}

	public Integer getUpvote() {
		return upvote;
	}

	public void setUpvote(Integer upvote) {
		this.upvote = upvote;
	}

	public Integer getDownvote() {
		return downvote;
	}

	public void setDownvote(Integer downvote) {
		this.downvote = downvote;
	}

	public long getCreatedat() {
		return createdat;
	}

	public void setCreatedat(long createdat) {
		this.createdat = createdat;
	}

	public long getUpdatedat() {
		return updatedat;
	}

	public void setUpdatedat(long updatedat) {
		this.updatedat = updatedat;
	}
}
