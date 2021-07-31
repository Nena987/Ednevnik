package com.iktpreobuka.ednevnik.services;

import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.dto.StudentDTO;

public interface StudentService {

	public StudentEntity dodajStudenta(StudentDTO newStudent);

	public StudentEntity dodajRoditeljaUceniku(Integer studentId, Integer parentId);

	public StudentEntity brisanjeUcenika(Integer ucenikId);

	public StudentEntity izmenaUcenika(Integer ucenikId, StudentDTO noviUcenik);

	public StudentEntity promenaOdeljenjaUceniku(Integer ucenikId, Integer odeljenjeId);

	public Boolean ucenikSlusaPredmet(Integer ucenikId, Integer predmetId);

}
