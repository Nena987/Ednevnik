package com.iktpreobuka.ednevnik.repositories;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.ClassYearEntity;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity.ClassYear;

public interface ClassYearRepository extends CrudRepository<ClassYearEntity, Integer> {

	ClassYearEntity findByNaziv(ClassYear classYear);

	public Set<ClassYearEntity> findByOdeljenjaPredmetiUOdeljenjuNastavniciId(Integer nastavnikId);
	
	public ClassYearEntity findByOdeljenjaId (Integer odeljenjeId);

}
