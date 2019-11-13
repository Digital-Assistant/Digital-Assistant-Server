package com.nistapp.voice.index.repository;

import com.nistapp.voice.index.models.Userclicknodes;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserclicknodesRepository implements PanacheRepository<Userclicknodes> {

}
