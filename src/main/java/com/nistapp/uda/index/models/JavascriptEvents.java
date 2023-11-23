package com.nistapp.uda.index.models;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "index_javascript_events")
public class JavascriptEvents {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	private Integer id;

	@Column(length=500)
	private String sessionid;

	@Basic
	@Column(length = 5000)
	private String clickednodename;

	@Column(length=500)
	private String domain;

	@Lob
	private String urlpath;

	@Lob
	private String data;

	//@JsonIgnore
	@Column(name = "created_at", nullable = false)
	private long created_at;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrlpath() {
		return urlpath;
	}

	public void setUrlpath(String url) {
		this.urlpath = url;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getCreated_at() {
		return created_at;
	}

	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}

	@PrePersist
	public void preSave() {
//		this.created_at = new Date();
		this.created_at = Instant.now().toEpochMilli();
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

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}



}
