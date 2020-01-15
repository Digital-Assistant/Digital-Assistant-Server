package com.nistapp.voice.index.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ClickTrack")
public class ClickTrack extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	@GenericField
	private long id;

	@GenericField
	@Column(length = 500)
	private String usersessionid;

	@GenericField
	@Column(length = 200)
	private String clicktype;

	@GenericField
	@Column(length = 2000)
	private String clickedname;

	@GenericField
	private long recordid;

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

	public String getClicktype() {
		return clicktype;
	}

	public void setClicktype(String clicktype) {
		this.clicktype = clicktype;
	}

	public String getClickedname() {
		return clickedname;
	}

	public void setClickedname(String clickedname) {
		this.clickedname = clickedname;
	}

	public long getRecordid() {
		return recordid;
	}

	public void setRecordid(long recordid) {
		this.recordid = recordid;
	}

	public long getCreatedat() {
		return createdat;
	}

	public void setCreatedat(long createdat) {
		this.createdat = createdat;
	}
}
