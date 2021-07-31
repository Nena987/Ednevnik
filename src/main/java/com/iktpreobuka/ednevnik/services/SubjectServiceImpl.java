package com.iktpreobuka.ednevnik.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity;
import com.iktpreobuka.ednevnik.entities.GradeEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.entities.SubjectEntity;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.ClassYearRepository;
import com.iktpreobuka.ednevnik.repositories.GradeRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectClassRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;

@Service
public class SubjectServiceImpl implements SubjectService {

	@Autowired
	SubjectRepository subjectRepository;

	@Autowired
	ClassYearRepository classYearRepository;

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	SubjectClassRepository subjectClassRepository;

	@Autowired
	ClassRepository classRepository;

	@Autowired
	GradeRepository gradeRepository;

	/* Metoda za dodavanje novog predmeta */

	public SubjectEntity dodajPredmet(SubjectEntity newSubject) {
		SubjectEntity subject = new SubjectEntity();
		subject.setNazivPredmeta(newSubject.getNazivPredmeta());
		subject.setNedeljniFondCasova(newSubject.getNedeljniFondCasova());
		subjectRepository.save(subject);
		return subject;
	}

	/* Metoda za dodavanje predmeta u razred */

	public SubjectEntity dodajPredmetURazred(Integer predmetId, Integer razredId) {
		SubjectEntity predmet = subjectRepository.findById(predmetId).get();
		ClassYearEntity razred = classYearRepository.findById(razredId).get();

		razred.getPredmeti().add(predmet);
		classYearRepository.save(razred);
		predmet.getRazredi().add(razred);
		subjectRepository.save(predmet);
		return predmet;
	}

	/* Metoda za dodavanje predmeta odeljenju */

	public SubjectClassEntity dodajPredmetOdeljenju(Integer predmetId, Integer odeljenjeId, Integer nastavnikId) {
		TeacherEntity nastavnik = teacherRepository.findById(nastavnikId).get();
		SubjectEntity predmet = subjectRepository.findById(predmetId).get();
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		SubjectClassEntity predmetOdeljenja = new SubjectClassEntity();
		predmetOdeljenja.setPredmet(predmet);
		predmetOdeljenja.setName(predmet.getNazivPredmeta());
		predmetOdeljenja.setOdeljenje(odeljenje);
		predmetOdeljenja.getNastavnici().add(nastavnik);
		subjectClassRepository.save(predmetOdeljenja);
		nastavnik.getPredmeti_odeljenja().add(predmetOdeljenja);
		teacherRepository.save(nastavnik);
		return predmetOdeljenja;
	}

	/* Metoda za izmenu pojedinacnog predmeta */

	public SubjectEntity izmenaPredmeta(SubjectEntity noviPredmet, Integer predmetId) {
		SubjectEntity predmet = subjectRepository.findById(predmetId).get();
		if (noviPredmet.getNazivPredmeta() != null)
			predmet.setNazivPredmeta(noviPredmet.getNazivPredmeta());
		if (noviPredmet.getNedeljniFondCasova() != null)
			predmet.setNedeljniFondCasova(noviPredmet.getNedeljniFondCasova());
		subjectRepository.save(predmet);
		return predmet;
	}

	/* Metoda za brisanje pojedinacnog predmeta */

	public SubjectEntity brisanjePredmeta(Integer predmetId) {
		SubjectEntity predmet = subjectRepository.findById(predmetId).get();
		for (TeacherEntity nastavnik : predmet.getNastavnici()) {
			nastavnik.getPredmeti().remove(predmet);
			teacherRepository.save(nastavnik);
		}
		for (ClassYearEntity razred : predmet.getRazredi()) {
			razred.getPredmeti().remove(predmet);
			classYearRepository.save(razred);
		}
		subjectRepository.delete(predmet);
		return predmet;
	}

	/* Metoda za brisanje predmeta iz odeljenja. */

	public SubjectClassEntity brisanjePredmetaIzOdeljenja(Integer predmetId, Integer odeljenjeId) {
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId);
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		for (TeacherEntity nastavnik : predmet.getNastavnici()) {
			nastavnik.getPredmeti_odeljenja().remove(predmet);
			teacherRepository.save(nastavnik);
		}
		for (GradeEntity ocena : predmet.getOcene()) {
			gradeRepository.delete(ocena);
		}
		odeljenje.getPredmetiUOdeljenju().remove(predmet);
		classRepository.save(odeljenje);
		subjectClassRepository.delete(predmet);
		return predmet;
	}

	/* Metoda za dodavanje nastavnika predmetu u odeljenju */

	public SubjectClassEntity dodajNastavnikaPredmetuUOdeljenju(Integer predmetId, Integer odeljenjeId,
			Integer nastavnikId) {
		TeacherEntity nastavnik = teacherRepository.findById(nastavnikId).get();
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId);
		predmet.getNastavnici().add(nastavnik);
		subjectClassRepository.save(predmet);
		nastavnik.getPredmeti_odeljenja().add(predmet);
		teacherRepository.save(nastavnik);
		return predmet;
	}

	/*
	 * Pomocna metoda za pronala]enje predmeta u odeljenju po nazivu iz liste
	 * predmeta u odeljenju
	 */

	public SubjectClassEntity nadjiPredmetUOdeljenjuPoImenuIzListe(String name, List<SubjectClassEntity> predmeti) {
		for (SubjectClassEntity predmet : predmeti) {
			if (predmet.getName().equals(name))
				return predmet;
		}
		return null;
	}

	/* Metoda koja brisa nastavnika predmetu u odeljenju */

	@Override
	public SubjectClassEntity brisanjeNastavnikPredmetuUOdeljenju(Integer predmetId, Integer odeljenjeId,
			Integer nastavnikId) {
		TeacherEntity nastavnik = teacherRepository.findById(nastavnikId).get();
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId);
		predmet.getNastavnici().remove(nastavnik);
		subjectClassRepository.save(predmet);
		nastavnik.getPredmeti_odeljenja().remove(predmet);
		teacherRepository.save(nastavnik);
		return predmet;
	}

	/*
	 * Pomocna metoda koja proverava da li se predmet slusa u nekom odeljenju
	 * razreda
	 */

	public Boolean predmetSeSlusaUOdeljenjuRazreda(Integer predmetId, Integer razredId) {
		ClassYearEntity razred = classYearRepository.findById(razredId).get();
		for (ClassEntity odeljenje : razred.getOdeljenja()) {
			if (odeljenje.getPredmetiUOdeljenju()
					.contains(subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenje.getId())))
				return true;
		}
		return false;
	}

	@Override
	public Boolean predmetSeSlusaURazredu(Integer predmetId, Integer razredId) {
		SubjectEntity predmet = subjectRepository.findById(predmetId).get();
		ClassYearEntity razred = classYearRepository.findById(razredId).get();
		if (razred.getPredmeti().contains(predmet))
			return true;
		return false;
	}

}
