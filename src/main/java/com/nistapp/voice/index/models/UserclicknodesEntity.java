package com.nistapp.voice.index.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "Userclicknodes")
public class UserclicknodesEntity extends PanacheEntity {
	private Integer id;

	private String sessionid;

	private String clickednodename;

	private String objectdata;

	private Integer html5;

	private String clickedpath;

	private String urlpath;

	private String domain;

	private Timestamp createdat;
}
