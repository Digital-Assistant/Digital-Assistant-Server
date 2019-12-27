package com.nistapp.voice.index.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_session_data")
public class UserSessionData extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	@GenericField
	private long id;

	@GenericField
	@Column(nullable = false)
	private long userauthid;

	@GenericField
	@Column(length = 500)
	private String usersessionid;

	@Column(name = "createdat", nullable = false)
	@GenericField
	private long createdat;

	@PrePersist
	public void preSave() {
		this.createdat = Instant.now().toEpochMilli();
	}

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
	public long getUserauthid() {
		return userauthid;
	}

	public void setUserauthid(long userauthid) {
		this.userauthid = userauthid;
	}

	public long getCreatedat() {
		return createdat;
	}

	public void setCreatedat(long createdat) {
		this.createdat = createdat;
	}
}
