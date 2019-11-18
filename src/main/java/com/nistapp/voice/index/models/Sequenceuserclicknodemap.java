package com.nistapp.voice.index.models;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Sequenceuserclicknodemap")
public class Sequenceuserclicknodemap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	private Integer id;

	@Column(name = "createdat", nullable = false)
	private Timestamp createdat;

	@PrePersist
	public void preSave() {
		this.createdat = new Timestamp(System.currentTimeMillis());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getCreatedat() {
		return createdat;
	}

	public void setCreatedat(Timestamp createdat) {
		this.createdat = createdat;
	}

	/*@ManyToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "sequence_id")
	private SequenceList sequenceList;

	@ManyToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "userclicknode_id")
	private Userclicknodes userclicknode;

	public SequenceList getSequenceList() {
		return sequenceList;
	}

	public void setSequenceList(SequenceList sequenceList) {
		this.sequenceList = sequenceList;
	}

	public Userclicknodes getUserclicknode() {
		return userclicknode;
	}

	public void setUserclicknode(Userclicknodes userclicknode) {
		this.userclicknode = userclicknode;
	}*/

}
