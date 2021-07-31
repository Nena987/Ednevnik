package com.iktpreobuka.ednevnik.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.ClassYearEntity;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity.ClassYear;
import com.iktpreobuka.ednevnik.entities.SubjectEntity;
import com.iktpreobuka.ednevnik.entities.dto.ClassYearDTO;
import com.iktpreobuka.ednevnik.repositories.ClassYearRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectRepository;

@Service
public class ClassYearServiceImpl implements ClassYearService {

	@Autowired
	ClassYearRepository classYearRepository;

	@Autowired
	SubjectRepository subjectRepository;

	/* Medtoda za kreiranje novog razreda */

	public ClassYearEntity dodajRazred(ClassYearDTO newClassYear) {
		ClassYearEntity classYear = new ClassYearEntity();
		classYear.setNaziv(ClassYear.valueOf(newClassYear.getNazivRazreda()));
		classYearRepository.save(classYear);
		return classYear;
	}

	/*
	 * Metoda za brisanje pojedinacnog razreda. Iz razreda se ujedno brise i svaki
	 * predmet koji se slusao u tom razredu.
	 */

	public ClassYearEntity brisanjeRazreda(Integer razredId) {
		ClassYearEntity razred = classYearRepository.findById(razredId).get();
		for (SubjectEntity predmet : razred.getPredmeti()) {
			predmet.getRazredi().remove(razred);
			subjectRepository.save(predmet);
		}
		classYearRepository.delete(razred);
		return razred;
	}

}
