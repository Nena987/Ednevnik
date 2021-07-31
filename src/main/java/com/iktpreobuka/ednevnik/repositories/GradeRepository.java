package com.iktpreobuka.ednevnik.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.GradeEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;

public interface GradeRepository extends CrudRepository<GradeEntity, Integer> {

	public List<GradeEntity> findByPredmetUOdeljenjuAndUcenikId(SubjectClassEntity predmetUOdeljenju, Integer ucenikId);
	
	

}
