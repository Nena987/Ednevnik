package com.iktpreobuka.ednevnik.services;

import java.util.List;

import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.entities.SubjectEntity;

public interface SubjectService {

	public SubjectEntity dodajPredmet(SubjectEntity newSubject);

	public SubjectEntity dodajPredmetURazred(Integer predmetId, Integer razredId);

	public SubjectClassEntity dodajPredmetOdeljenju(Integer predmetId, Integer odeljenjeId, Integer nastavnikId);

	public SubjectEntity izmenaPredmeta(SubjectEntity noviPredmet, Integer predmetId);

	public SubjectEntity brisanjePredmeta(Integer predmetId);

	public SubjectClassEntity brisanjePredmetaIzOdeljenja(Integer predmetId, Integer odeljenjeId);

	public SubjectClassEntity nadjiPredmetUOdeljenjuPoImenuIzListe(String name, List<SubjectClassEntity> predmeti);

	public SubjectClassEntity dodajNastavnikaPredmetuUOdeljenju(Integer predmetId, Integer odeljenjeId,
			Integer nastavnikId);

	public SubjectClassEntity brisanjeNastavnikPredmetuUOdeljenju(Integer predmetId, Integer odeljenjeId,
			Integer nastavnikId);

	public Boolean predmetSeSlusaUOdeljenjuRazreda(Integer predmetId, Integer razredId);
	
	public Boolean predmetSeSlusaURazredu (Integer predmetId, Integer razredId);
}
