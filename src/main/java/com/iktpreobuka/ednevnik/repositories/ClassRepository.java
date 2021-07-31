package com.iktpreobuka.ednevnik.repositories;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity.ClassYear;

public interface ClassRepository extends CrudRepository<ClassEntity, Integer> {

	public ClassEntity findByBrojOdeljenjaAndRazredNaziv(Integer brojOdeljenja, ClassYear razred);

	public ClassEntity findByUceniciId(Integer ucenikId);

}
