package com.iktpreobuka.ednevnik.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.StudentEntity;

public interface StudentRepository extends CrudRepository<StudentEntity, Integer> {

	public StudentEntity findByKorisnickoIme(String korisnickoIme);

	public List<StudentEntity> findByOdeljenjeRazredId(Integer razredId);

}
