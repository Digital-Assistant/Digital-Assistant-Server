package com.nistapp.uda.index.repository;

import com.nistapp.uda.index.models.Userclicknodes;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserclicknodesRepository implements PanacheRepository<Userclicknodes> {
}
