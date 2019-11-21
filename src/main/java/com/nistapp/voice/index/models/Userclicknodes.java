package com.nistapp.voice.index.models;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "Userclicknodes")
@Indexed
public class Userclicknodes {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	@GenericField
	private Integer id;

	@GenericField
	private String sessionid;

	@KeywordField
	private String clickednodename;

	@FullTextField(analyzer = "english")
	private String objectdata;

	@GenericField
	private Integer html5;

	@KeywordField
	private String clickedpath;

	@KeywordField
	private String urlpath;

	@KeywordField
	private String domain;

	//@JsonIgnore
	@Column(name = "createdat", nullable = false)
	@GenericField
	private long createdat;

	@PrePersist
	public void preSave() {
//		this.createdat = Instant.now().toEpochMilli();
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

	public String getObjectdata() {
		return objectdata;
	}

	public void setObjectdata(String objectdata) {
		this.objectdata = objectdata;
	}

	public Integer getHtml5() {
		return html5;
	}

	public void setHtml5(Integer html5) {
		this.html5 = html5;
	}

	public String getClickedpath() {
		return clickedpath;
	}

	public void setClickedpath(String clickedpath) {
		this.clickedpath = clickedpath;
	}

}
