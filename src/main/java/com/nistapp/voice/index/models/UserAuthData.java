package com.nistapp.voice.index.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_auth_data")
public class UserAuthData extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	@GenericField
	private long id;

	@GenericField
	@Column(length = 2000,nullable = false)
	private String authid;

	@GenericField
	@Column(length = 2000)
	private String emailid;

	@GenericField
	@Column(length = 500)
	private String authsource;

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

	public String getAuthid() {
		return authid;
	}

	public void setAuthid(String authid) {
		this.authid = authid;
	}

	public String getEmailid() {
		return emailid;
	}

	public void setEmailid(String emailid) {
		this.emailid = emailid;
	}

	public String getAuthsource() {
		return authsource;
	}

	public void setAuthsource(String authsource) {
		this.authsource = authsource;
	}

	public long getCreatedat() {
		return createdat;
	}

	public void setCreatedat(long createdat) {
		this.createdat = createdat;
	}
}
