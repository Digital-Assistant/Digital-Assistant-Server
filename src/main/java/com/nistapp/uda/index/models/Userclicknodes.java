package com.nistapp.uda.index.models;

import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
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
	@Column(length=500)
	private String sessionid;

	@FullTextField(analyzer = "english")
	@Column(length=2000)
	private String clickednodename;

	@GenericField(searchable = Searchable.NO)
	@Lob
	private String objectdata;

	@GenericField
	private Integer html5;

	@KeywordField
	@Column(length=3000)
	private String clickedpath;

	@KeywordField
//	@Column(length=5000)
	@Lob
	private String urlpath;

	@KeywordField
	@Column(length=500)
	private String domain;

	//@JsonIgnore
	@Column(name = "createdat", nullable = false)
	@GenericField
	private long createdat;

	@ManyToOne
	@JoinTable(name = "Sequenceuserclicknodemap", joinColumns = @JoinColumn(name = "userclicknodeid"),
			inverseJoinColumns = @JoinColumn(name = "sequencelistid"))
	@JsonbTransient
	private SequenceList sequenceList;

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

	public SequenceList getSequenceList() {
		return sequenceList;
	}

	public void setSequenceList(SequenceList sequenceList) {
		this.sequenceList = sequenceList;
	}
}
