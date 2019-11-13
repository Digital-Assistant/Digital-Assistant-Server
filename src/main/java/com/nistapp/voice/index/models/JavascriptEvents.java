package com.nistapp.voice.index.models;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "index_javascript_events")
public class JavascriptEvents {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	private Integer id;

	private String sessionid;

	private String clickednodename;

	private String domain;

	private String urlpath;

	private String data;

	//@JsonIgnore
	@Column(name = "created_at", nullable = false)
	private Date created_at;

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

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	@PrePersist
	public void preSave() {
		this.created_at = new Date();
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
