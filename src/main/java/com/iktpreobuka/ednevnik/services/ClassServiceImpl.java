package com.iktpreobuka.ednevnik.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity.Rola;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.ClassYearRepository;
import com.iktpreobuka.ednevnik.repositories.RoleRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectClassRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity.ClassYear;

@Service
public class ClassServiceImpl implements ClassService {

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	ClassYearRepository classYearRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	ClassRepository classRepository;

	@Autowired
	SubjectClassRepository subjectClassRepository;

	/*
	 * Metoda za dodavanje odeljenja. Osim podataka o odeljenju dodeljuje se i
	 * razredni staresina. Nastavniku koji se dodeljuje kao razredni staresina menja
	 * se rola na "ROLE_CLASSTEACHER"
	 */

	public ClassEntity dodajOdeljenje(ClassYear razred, Integer brojOdeljenja, Integer razredniId) {

		ClassEntity odeljenje = new ClassEntity();
		TeacherEntity razredni = teacherRepository.findById(razredniId).get();

		odeljenje.setRazred(classYearRepository.findByNaziv(razred));
		odeljenje.setBrojOdeljenja(brojOdeljenja);
		odeljenje.setRazredniStaresina(razredni);

		RoleEntity rola = roleRepository.findByName(Rola.ROLE_CLASSTEACHER);
		razredni.setRole(rola);
		teacherRepository.save(razredni);
		classRepository.save(odeljenje);
		return odeljenje;
	}

	/*
	 * Metoda za dodavanje ili promenu razrednog staresine odeljenju. Metoda starom
	 * razrednom staresini setuje rolu na ROLE_TEACHER a novom razrednom staresini
	 * na ROLE_CLASSTEACHER.
	 */

	@Override
	public ClassEntity promenaRazrednogStaresine(Integer odeljenjeId, Integer nastavnikId) {
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		if (odeljenje.getRazredniStaresina() != null) {
			RoleEntity rola2 = roleRepository.findByName(Rola.ROLE_TEACHER);
			TeacherEntity stariRazredni = odeljenje.getRazredniStaresina();
			stariRazredni.setRole(rola2);
			teacherRepository.save(stariRazredni);
		}
		RoleEntity rola1 = roleRepository.findByName(Rola.ROLE_CLASSTEACHER);
		TeacherEntity noviRazredni = teacherRepository.findById(nastavnikId).get();
		odeljenje.setRazredniStaresina(noviRazredni);
		noviRazredni.setRole(rola1);
		teacherRepository.save(noviRazredni);
		classRepository.save(odeljenje);
		return odeljenje;
	}

	/*
	 * Metoda za brisanje odeljenja. Moguce je izbrisati samo ona odeljenja koja
	 * nemaju ucenike
	 */

	@Override
	public ClassEntity brisanjeOdeljenja(Integer odeljenjeId) {
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		if (odeljenje.getRazredniStaresina() != null) {
			RoleEntity rola = roleRepository.findByName(Rola.ROLE_TEACHER);
			TeacherEntity razredni = odeljenje.getRazredniStaresina();
			razredni.setRole(rola);
			teacherRepository.save(razredni);
		}
		for (SubjectClassEntity predmet : odeljenje.getPredmetiUOdeljenju()) {
			for (TeacherEntity nastavnik : predmet.getNastavnici()) {
				nastavnik.getPredmeti_odeljenja().remove(predmet);
				teacherRepository.save(nastavnik);
				predmet.getNastavnici().remove(nastavnik);
				subjectClassRepository.save(predmet);
			}
			subjectClassRepository.delete(predmet);
		}
		classRepository.delete(odeljenje);
		return odeljenje;
	}

}
