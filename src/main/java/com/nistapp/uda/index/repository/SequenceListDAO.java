package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.SequenceList;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SequenceListDAO implements PanacheRepositoryBase<SequenceList, Integer> {

}
