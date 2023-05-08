package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.SequenceList;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SequenceListDAO implements PanacheRepository<SequenceList> {

}
