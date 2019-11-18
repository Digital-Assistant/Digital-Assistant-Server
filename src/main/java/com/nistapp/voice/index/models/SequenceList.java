package com.nistapp.voice.index.models;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SequenceList")
public class SequenceList {

	private static final String SEPARATOR = ",";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, length = 11)
	private Integer id;

	private String name;

	private String usersessionid;

	/*@Convert(converter = UserclicknodeConverter.class)
	private List<Userclicknodes> userclicknodelist;*/

	private String userclicknodelist;

	@Column(name = "createdat", nullable = false)
	private Timestamp createdat;

	@PrePersist
	public void preSave() {
		this.createdat = new Timestamp(System.currentTimeMillis());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsersessionid() {
		return usersessionid;
	}

	public void setUsersessionid(String usersessionid) {
		this.usersessionid = usersessionid;
	}

	public String getUserclicknodelist() {
		return userclicknodelist;
	}

	public void setUserclicknodelist(String userclicknodelist) {
		this.userclicknodelist = userclicknodelist;
	}

	public Timestamp getCreatedat() {
		return createdat;
	}

	public void setCreatedat(Timestamp createdat) {
		this.createdat = createdat;
	}

	/*@Transient
	private List<Sequenceuserclicknodemap> sequenceuserclicknodemaps;

	@OneToMany(mappedBy="sequenceList", cascade = {CascadeType.ALL}, fetch=FetchType.EAGER)
	public List<Sequenceuserclicknodemap> getSequenceuserclicknodemaps() {
		return sequenceuserclicknodemaps;
	}

	public void setSequenceuserclicknodemaps(List<Sequenceuserclicknodemap> sequenceuserclicknodemaps) {
		this.sequenceuserclicknodemaps = sequenceuserclicknodemaps;
	}*/

	@Transient
	@JsonbProperty
	public List<UserclicknodesEntity> clicknodesdata;

	public List<UserclicknodesEntity> getClicknodesdata() {
		if(this.userclicknodelist.isEmpty() && this.userclicknodelist!=null){
			String[] pieces = userclicknodelist.split(SEPARATOR);

			if (pieces == null || pieces.length == 0) {
				System.out.println("not able to split");
				return null;
			}

			List<UserclicknodesEntity> userclicknodes = new ArrayList<>();
			for (String clickid : pieces) {
				Long clicknodeid = Long.parseLong(clickid);
				UserclicknodesEntity userclicknode = UserclicknodesEntity.findById(clicknodeid);
				userclicknodes.add(userclicknode);
			}
			System.out.println("data found");
			return userclicknodes;
		} else {
			System.out.println("no data");
			return null;
		}
	}

	public void setClicknodesdata(List<UserclicknodesEntity> userclicknodes){
		this.clicknodesdata = userclicknodes;
	}
}
