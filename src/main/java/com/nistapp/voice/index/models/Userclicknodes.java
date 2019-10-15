package com.nistapp.voice.index.models;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "Userclicknodes")
public class Userclicknodes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	private Integer id;

	private String sessionid;

	private String clickednodename;

	private String urlpath;

	private String domain;

	//@JsonIgnore
	@Column(name = "createdat", nullable = false)
	private long createdat;

	@PrePersist
	public void preSave() {
		this.createdat = Instant.now().toEpochMilli();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public String getClickednodename() {
		return clickednodename;
	}

	public void setClickednodename(String clickednodename) {
		this.clickednodename = clickednodename;
	}

	public String getUrlpath() {
		return urlpath;
	}

	public void setUrlpath(String urlpath) {
		this.urlpath = urlpath;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public long getCreatedat() {
		return createdat;
	}

	public void setCreatedat(long createdat) {
		this.createdat = createdat;
	}

}
