package com.nistapp.voice.index.models;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import javax.persistence.*;
import javax.validation.constraints.Max;
import java.sql.Timestamp;
import java.util.Set;

@Entity
@Table(name = "SequenceList")
@Indexed
public class SequenceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, length = 11)
    @GenericField
    private Integer id;

    @FullTextField(analyzer = "english")
    private String name;

    @GenericField
    private String usersessionid;

    private String userclicknodelist;

    @Column(name = "createdat", nullable = false)
    @GenericField
    private Timestamp createdat;
    @OneToMany
    @JoinTable(name = "Sequenceuserclicknodemap", joinColumns = @JoinColumn(name = "sequencelistid"),
            inverseJoinColumns = @JoinColumn(name = "userclicknodeid"))
    @IndexedEmbedded
    private Set<Userclicknodes> userclicknodesSet;

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

    public Set<Userclicknodes> getUserclicknodesSet() {
        return userclicknodesSet;
    }

    public void setUserclicknodesSet(Set<Userclicknodes> userclicknodesSet) {
        this.userclicknodesSet = userclicknodesSet;
    }
}
