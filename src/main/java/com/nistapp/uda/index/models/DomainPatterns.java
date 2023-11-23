package com.nistapp.uda.index.models;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "domainpatterns")
public class DomainPatterns {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	private Integer id;

	@Column(length=500)
	private String domain;

	@Column(length=500)
	private String patterntype;

	@Column(length=500)
	private String patternvalue;

	@Column(length=500)
	private String otherpatterntype;

	//	@JsonIgnore
	@Column(name = "createdat", nullable = false)
	private long createdat;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPatterntype() {
		return patterntype;
	}

	public void setPatterntype(String patterntype) {
		this.patterntype = patterntype;
	}

	public String getPatternvalue() {
		return patternvalue;
	}

	public void setPatternvalue(String patternvalue) {
		this.patternvalue = patternvalue;
	}

	public String getOtherpatterntype() {
		return otherpatterntype;
	}

	public void setOtherpatterntype(String otherpatterntype) {
		this.otherpatterntype = otherpatterntype;
	}

	@PrePersist
	public void preSave() {
		this.createdat = Instant.now().toEpochMilli();
//		this.createdat = new Timestamp(System.currentTimeMillis());
	}

	public long getCreatedat() {
		return createdat;
	}

	public void setCreatedat(long createdat) {
		this.createdat = createdat;
	}

}
