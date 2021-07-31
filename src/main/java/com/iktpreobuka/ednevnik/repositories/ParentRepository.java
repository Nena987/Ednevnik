package com.iktpreobuka.ednevnik.repositories;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.ParentEntity;

public interface ParentRepository extends CrudRepository<ParentEntity, Integer> {

	public ParentEntity findByKorisnickoIme(String korisnickoIme);

	public Set<ParentEntity> findByDecaOdeljenjeId(Integer odeljenjeId);

}
