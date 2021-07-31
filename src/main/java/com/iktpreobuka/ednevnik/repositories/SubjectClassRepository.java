package com.iktpreobuka.ednevnik.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;

public interface SubjectClassRepository extends CrudRepository<SubjectClassEntity, Integer> {

	public SubjectClassEntity findByPredmetIdAndOdeljenjeId(Integer predmetId, Integer odeljenjeId);

	public List<SubjectClassEntity> findByPredmetIdAndOdeljenje(Integer predmetId, ClassEntity odeljenje);

	public SubjectClassEntity findByName(String name);

}
