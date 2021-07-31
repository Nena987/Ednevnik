package com.iktpreobuka.ednevnik.services;

import java.util.Set;

import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.entities.dto.TeacherDTO;

public interface TeacherService {

	public TeacherEntity dodajNastavnika(TeacherDTO newTeacher);

	public TeacherEntity dodajPredmetNastavniku(Integer nastavnikId, Integer predmetId);

	public Set<TeacherEntity> nadjiNastavnikaKojiPredajeUceniku(StudentEntity ucenik);

	public TeacherEntity izmenaNastavnika(Integer nastavnikId, TeacherDTO noviNastavnik);

	public TeacherEntity brisanjeNastavnika(Integer nastavnikId);

	public Boolean nastavnikPredajePredmet(Integer predmetId, TeacherEntity nastavnik);

	public Boolean nastavnikPredajePredmetOdeljenju(Integer predmetId, Integer odeljenjeId, TeacherEntity nastavnik);

	public Boolean nastavnikPredajePredmetUceniku(Integer predmetId, Integer ucenikId, TeacherEntity nastavnik);

	public Boolean nastavnikJeRazredniOdeljenju(Integer odeljenjeId, TeacherEntity nastavnik);

	public Boolean nastavnikJeRazredniUceniku(Integer ucenikId, TeacherEntity nastavnik);

	public Boolean nastavnikJeRazredniRoditeljevomDetetu(Integer roditeljId, TeacherEntity nastavnik);
}
