package com.nistapp.voice.index.models;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "SequenceList")
@Indexed
public class SequenceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, length = 11)
    @GenericField
    private long id;

    @FullTextField(analyzer = "english")
    @Column(length = 5000)
    private String name;

    @GenericField
    @Column(length = 500)
    private String usersessionid;

    @Column(length = 5000)
    private String userclicknodelist;

//    @FullTextField(analyzer = "english")
    @KeywordField
    @Column(length = 500)
    private String domain;

    @Column(name = "createdat", nullable = false)
    @GenericField
    private long createdat;

    @OneToMany(mappedBy = "sequenceList", fetch = FetchType.EAGER)
    @IndexedEmbedded
    private List<Userclicknodes> userclicknodesSet;

    @GenericField
    @Column(name = "deleted", columnDefinition = "integer default 0")
    private Integer deleted=0;

    /*@OneToMany(mappedBy = "sequenceList", fetch = FetchType.LAZY)
    @IndexedEmbedded
    private List<SequenceVotes> sequenceVotes;*/

    @PrePersist
    public void preSave() {
//        this.createdat = new Timestamp(System.currentTimeMillis());
        this.createdat = Instant.now().toEpochMilli();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public List<Userclicknodes> getUserclicknodesSet() {
        return userclicknodesSet;
    }

    public void setUserclicknodesSet(List<Userclicknodes> userclicknodesSet) {
        this.userclicknodesSet = userclicknodesSet;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    /*public List<SequenceVotes> getSequenceVotes() {
        return sequenceVotes;
    }

    public void setSequenceVotes(List<SequenceVotes> sequenceVotes) {
        this.sequenceVotes = sequenceVotes;
    }*/
}
