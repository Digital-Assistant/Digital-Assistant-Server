package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.SequenceList;
import com.nistapp.uda.index.models.Userclicknodes;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SequenceListDAO implements PanacheRepositoryBase<SequenceList, Long> {

    public SequenceList findById(Long nodeId) {
        return find("id=:nodeId", Parameters.with("nodeId", nodeId)).firstResult();
    }

}
