package com.nistapp.voice.index.models;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "SequenceList")
public class SequenceList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	private Integer id;

	private String name;

	private String usersessionid;

	/*@Convert(converter = UserclicknodeConverter.class)
	private List<Userclicknodes> userclicknodelist;*/

	private String userclicknodelist;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsersessionid() {
		return usersessionid;
	}

	public void setUsersessionid(String usersessionid) {
		this.usersessionid = usersessionid;
	}

	public String getUserclicknodelist() {
		return userclicknodelist;
	}

	public void setUserclicknodelist(String userclicknodelist) {
		this.userclicknodelist = userclicknodelist;
	}

	public Timestamp getCreatedat() {
		return createdat;
	}

	public void setCreatedat(Timestamp createdat) {
		this.createdat = createdat;
	}
}
