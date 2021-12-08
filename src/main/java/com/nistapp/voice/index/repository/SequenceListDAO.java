package com.nistapp.voice.index.repository;

import com.nistapp.voice.index.models.SequenceList;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.inject.Singleton;

@Singleton
public class SequenceListDAO implements PanacheRepository<SequenceList> {

}
