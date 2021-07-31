package com.iktpreobuka.ednevnik.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.TeacherEntity;

public interface TeacherRepository extends CrudRepository<TeacherEntity, Integer> {

	public TeacherEntity findByKorisnickoIme(String korisnickoIme);
	
	public List<TeacherEntity> findByPredmeti_NazivPredmeta (String nazivPredmeta);

}
