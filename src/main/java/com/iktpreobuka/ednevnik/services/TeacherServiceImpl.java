package com.iktpreobuka.ednevnik.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.ParentEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity.Rola;
import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.entities.SubjectEntity;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.entities.dto.TeacherDTO;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.ParentRepository;
import com.iktpreobuka.ednevnik.repositories.RoleRepository;
import com.iktpreobuka.ednevnik.repositories.StudentRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectClassRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;
import com.iktpreobuka.ednevnik.utils.Encryption;

@Service
public class TeacherServiceImpl implements TeacherService {

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	SubjectRepository subjectRepository;

	@Autowired
	SubjectClassRepository subjectClassRepository;

	@Autowired
	StudentRepository studentRepository;

	@Autowired
	ClassRepository classRepository;

	@Autowired
	ParentRepository parentRepository;

	/* MEtoda za dodavanje novog nastavnika */

	public TeacherEntity dodajNastavnika(TeacherDTO newTeacher) {
		TeacherEntity teacher = new TeacherEntity();
		RoleEntity role = roleRepository.findByName(Rola.ROLE_TEACHER);
		teacher.setKorisnickoIme(newTeacher.getKorisnickoIme());
		teacher.setLozinka(Encryption.getPassEncoded(newTeacher.getLozinka()));
		teacher.setIme(newTeacher.getIme());
		teacher.setPrezime(newTeacher.getPrezime());
		teacher.setRole(role);
		teacherRepository.save(teacher);
		return teacher;
	}

	/* Metoda za dodavanje predmeta nastavniku */

	public TeacherEntity dodajPredmetNastavniku(Integer nastavnikId, Integer predmetId) {
		TeacherEntity teacher = teacherRepository.findById(nastavnikId).get();
		SubjectEntity subject = subjectRepository.findById(predmetId).get();

		teacher.getPredmeti().add(subject);
		teacherRepository.save(teacher);
		subject.getNastavnici().add(teacher);
		subjectRepository.save(subject);
		return teacher;
	}

	/* Metoda za izmenu podataka o nastavniku */

	public TeacherEntity izmenaNastavnika(Integer nastavnikId, TeacherDTO noviNastavnik) {
		TeacherEntity nastavnik = teacherRepository.findById(nastavnikId).get();
		if (noviNastavnik.getIme() != null)
			nastavnik.setIme(noviNastavnik.getIme());
		if (noviNastavnik.getPrezime() != null)
			nastavnik.setPrezime(noviNastavnik.getPrezime());
		teacherRepository.save(nastavnik);
		return nastavnik;
	}

	/* Metoda za brisanje nastavnika */

	public TeacherEntity brisanjeNastavnika(Integer nastavnikId) {
		TeacherEntity nastavnik = teacherRepository.findById(nastavnikId).get();
		if (nastavnik.getOdeljenjeRazrednog() != null) {
			ClassEntity odeljenje = nastavnik.getOdeljenjeRazrednog();
			odeljenje.setRazredniStaresina(null);
		}
		for (SubjectClassEntity predmet : nastavnik.getPredmeti_odeljenja())
			predmet.getNastavnici().remove(nastavnik);
		for (SubjectEntity predmet : nastavnik.getPredmeti())
			predmet.getNastavnici().remove(nastavnik);
		teacherRepository.delete(nastavnik);
		return nastavnik;
	}

	/*
	 * Metoda koja vraca set nastavnika koji predaju bilo koji predmet jednom
	 * konkretnom uceniku
	 */

	public Set<TeacherEntity> nadjiNastavnikaKojiPredajeUceniku(StudentEntity ucenik) {
		ClassEntity odeljenje = ucenik.getOdeljenje();
		List<SubjectClassEntity> predmeti = odeljenje.getPredmetiUOdeljenju();
		Set<TeacherEntity> nastavnici = new HashSet<TeacherEntity>();
		for (SubjectClassEntity predmet : predmeti) {
			for (TeacherEntity nastavnik : predmet.getNastavnici()) {
				nastavnici.add(nastavnik);
			}
		}
		return nastavnici;
	}

	/* Pomocna metoda za proveru da li nastavnik predaje odredjeni predmet */

	@Override
	public Boolean nastavnikPredajePredmet(Integer predmetId, TeacherEntity nastavnik) {
		SubjectEntity predmet = subjectRepository.findById(predmetId).get();
		if (predmet.getNastavnici().contains(nastavnik))
			return true;
		return false;
	}

	/* Pomocna metoda za proveru da li nastavnik predaje predmet odeljenju */

	@Override
	public Boolean nastavnikPredajePredmetOdeljenju(Integer predmetId, Integer odeljenjeId, TeacherEntity nastavnik) {
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId);
		if (!nastavnikPredajePredmet(predmetId, nastavnik))
			return false;
		if (predmet == null)
			return false;
		if (predmet.getNastavnici().contains(nastavnik))
			return true;
		return false;
	}

	/* Pomocna metoda za proveru da li nastavnik predaje predmet uceniku */

	@Override
	public Boolean nastavnikPredajePredmetUceniku(Integer predmetId, Integer ucenikId, TeacherEntity nastavnik) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		if (nastavnikPredajePredmetOdeljenju(predmetId, odeljenje.getId(), nastavnik))
			return true;
		return false;
	}

	/* Pomocna metoda za proveru da li je nastavnik razredni staresina odeljenju */

	@Override
	public Boolean nastavnikJeRazredniOdeljenju(Integer odeljenjeId, TeacherEntity nastavnik) {
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		if (nastavnik.getOdeljenjeRazrednog() != null)
			if (nastavnik.getOdeljenjeRazrednog().equals(odeljenje))
				return true;
		return false;
	}

	/*
	 * Pomocna metoda za proveru da li je neki nastavnik razredni staresina uceniku
	 */

	@Override
	public Boolean nastavnikJeRazredniUceniku(Integer ucenikId, TeacherEntity nastavnik) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		if (nastavnikJeRazredniOdeljenju(odeljenje.getId(), nastavnik))
			return true;
		return false;
	}

	/*
	 * Pomocna metoda koja proverava da li je nastavnik razredni staresina nekod od
	 * deteta roditelja
	 */

	public Boolean nastavnikJeRazredniRoditeljevomDetetu(Integer roditeljId, TeacherEntity nastavnik) {
		ParentEntity roditelj = parentRepository.findById(roditeljId).get();
		List<StudentEntity> deca = roditelj.getDeca();
		if (deca != null) {
			for (StudentEntity dete : deca) {
				if (nastavnikJeRazredniUceniku(dete.getId(), nastavnik))
					return true;
			}
		}
		return false;
	}

}
