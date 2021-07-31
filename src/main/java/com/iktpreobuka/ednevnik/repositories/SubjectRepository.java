package com.iktpreobuka.ednevnik.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.SubjectEntity;

public interface SubjectRepository extends CrudRepository<SubjectEntity, Integer> {

	public List<SubjectEntity> findByNastavniciId(Integer nastavnikId);

	public SubjectEntity findByNazivPredmeta(String nazivPredmeta);

}
