package com.nistapp.uda.index.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.PropertyBinderRef;
import org.hibernate.search.mapper.pojo.extractor.mapping.annotation.ContainerExtract;
import org.hibernate.search.mapper.pojo.extractor.mapping.annotation.ContainerExtraction;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import javax.inject.Inject;
import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * we are changing the index name due to the conflict issue with the two instances running on the same server.
 */
@Entity
@Table(name = "SequenceList")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
@Indexed(index = "test_sequencelist")
public class SequenceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, length = 11)
    @GenericField
    private Integer id;

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
    @GenericField(name = "createdat_sort", sortable = Sortable.YES)
    private long createdat;

    @OneToMany(mappedBy = "sequenceList", fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    @IndexedEmbedded
    private Set<Userclicknodes> userclicknodesSet;

    /**
     * Declaring Sequence Votes variable
     */
    @OneToMany(mappedBy = "sequenceList", fetch = FetchType.EAGER)
    @IndexedEmbedded
    private Set<SequenceVotes> sequenceVotes;

    @GenericField
    @Column(name = "deleted", columnDefinition = "integer default 0")
    private Integer deleted=0;

    @GenericField
    @Column(name = "isValid")
    private Integer isValid;

    @GenericField
    @Column(name = "isIgnored")
    private Integer isIgnored;


    /***
     * Saving additional info
     */
    @Column(name = "additionalParams", columnDefinition = "LONGTEXT")
    @Basic
    @Type(type = "json")
    @PropertyBinding(binder = @PropertyBinderRef(type = AdditionalParamsBinder.class))
    @IndexingDependency(
            //TODO create appropriate dependencies check this stackoverflow https://stackoverflow.com/questions/68532341/index-hashmap-using-keys-as-fields-names-hibernatesearch
            derivedFrom = {}, extraction = @ContainerExtraction(extract = ContainerExtract.NO))
    private Map<String, Object> additionalParams;

    @PrePersist
    public void preSave() {
//        this.createdat = new Timestamp(System.currentTimeMillis());
        this.createdat = Instant.now().toEpochMilli();
    }

    @Transient
    public String SharableLink;

    public String getSharableLink() {
        List<Userclicknodes> userClickNodes = new ArrayList<>(this.getUserclicknodesSet());
        String url = "";
        if(!userClickNodes.isEmpty()) {
            Userclicknodes userClickNode = userClickNodes.get(0);
            url = userClickNode.getDomain()+((!userClickNode.getUrlpath().isEmpty())?userClickNode.getUrlpath():'/');
            if(url.indexOf('?') != -1){
                url = url+"&UDA_Sequence_id="+this.id;
            } else {
                url = url+"?UDA_Sequence_id="+this.id;
            }
        } else {
            url = "https://"+this.domain+"?UDA_Sequence_id="+this.id;
        }
        return url;
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

    public Set<Userclicknodes> getUserclicknodesSet() {
        return userclicknodesSet;
    }

    public void setUserclicknodesSet(Set<Userclicknodes> userclicknodesSet) {
        this.userclicknodesSet = userclicknodesSet;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public Integer getIsIgnored() {
        return isIgnored;
    }

    public void setIsIgnored(Integer isIgnored) {
        this.isIgnored = isIgnored;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, Object> additionalParams) {
        this.additionalParams = additionalParams;
    }

    /**
     * Getter for sequenceVote
     * @return
     */
    public Set<SequenceVotes> getSequenceVotes() {
        return sequenceVotes;
    }

    /**
     * Setter for sequenceVote
     * @return
     */
    public void setSequenceVotes(Set<SequenceVotes> sequenceVotes) {
        this.sequenceVotes = sequenceVotes;
    }

    /**
     * Declaration of upvote count and downvote count
     */
    @Transient
    public Integer upVoteCount = 0;

    public Integer getUpVoteCount(){
        Integer totalLikes = 0;
        for(SequenceVotes sequenceVote:this.sequenceVotes){
            if(sequenceVote.getUpvote() == 1) {
                totalLikes++;
            }
        }

        return totalLikes;
    }

    @Transient
    public Integer downVoteCount;

    public Integer getDownVoteCount(){
        Integer totalDisLikes = 0;
        for(SequenceVotes sequenceVote:this.sequenceVotes){
            if(sequenceVote.getDownvote() == 1) {
                totalDisLikes++;
            }
        }

        return totalDisLikes;
    }

}
