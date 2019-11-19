package com.nistapp.voice.index.models;

import javax.persistence.*;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "SequenceList")
public class SequenceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, length = 11)
    private Integer id;

    private String name;

    private String usersessionid;

    private String userclicknodelist;

    @Column(name = "createdat", nullable = false)
    private long createdat;

    @OneToMany
    @JoinTable(name = "Sequenceuserclicknodemap", joinColumns = @JoinColumn(name = "sequencelistid"),
            inverseJoinColumns = @JoinColumn(name = "userclicknodeid"))
    private Set<Userclicknodes> userclicknodesSet;

    @PrePersist
    public void preSave() {
//        this.createdat = new Timestamp(System.currentTimeMillis());
        this.createdat = Instant.now().toEpochMilli();
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

    public long getCreatedat() {
        return createdat;
    }

    public void setCreatedat(long createdat) {
        this.createdat = createdat;
    }

    public Set<Userclicknodes> getUserclicknodesSet() {
        return userclicknodesSet;
    }

    public void setUserclicknodesSet(Set<Userclicknodes> userclicknodesSet) {
        this.userclicknodesSet = userclicknodesSet;
    }
}
