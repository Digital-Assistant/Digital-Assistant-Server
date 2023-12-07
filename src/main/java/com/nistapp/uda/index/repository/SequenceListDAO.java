package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.models.Userclicknodes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SequenceListDAO implements PanacheRepositoryBase<SequenceList, Integer> {

    public SequenceList findById(String sessionid, Integer nodeId){
        return find("usersessionid=:sessionId and id=:nodeId", Parameters.with("sessionId",sessionid).and("nodeId",nodeId)).singleResult();
    }

}
