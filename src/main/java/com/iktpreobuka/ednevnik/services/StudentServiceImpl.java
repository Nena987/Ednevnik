package com.iktpreobuka.ednevnik.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.GradeEntity;
import com.iktpreobuka.ednevnik.entities.ParentEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity.Rola;
import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.entities.dto.StudentDTO;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.GradeRepository;
import com.iktpreobuka.ednevnik.repositories.ParentRepository;
import com.iktpreobuka.ednevnik.repositories.RoleRepository;
import com.iktpreobuka.ednevnik.repositories.StudentRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectClassRepository;
import com.iktpreobuka.ednevnik.utils.Encryption;

@Service
public class StudentServiceImpl implements StudentService {

	@Autowired
	StudentRepository studentRepository;

	@Autowired
	ParentRepository parentRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	GradeRepository gradeRepository;

	@Autowired
	SubjectClassRepository subjectClassRepository;

	@Autowired
	ClassRepository classRepository;

	@Autowired
	SubjectService subjectService;

	/*
	 * Metoda za dodavanje novog ucenika. Prilikom kreiranja novog ucenika, rola se
	 * automatski setuje na "ROLE_STUDENT".
	 */

	@Override
	public StudentEntity dodajStudenta(StudentDTO newStudent) {
		StudentEntity student = new StudentEntity();
		RoleEntity role = roleRepository.findByName(Rola.ROLE_STUDENT);
		student.setKorisnickoIme(newStudent.getKorisnickoIme());
		student.setLozinka(Encryption.getPassEncoded(newStudent.getLozinka()));
		student.setImeUcenika(newStudent.getImeUcenika());
		student.setPrezimeUcenika(newStudent.getPrezimeUcenika());
		student.setDatumRodjenja(newStudent.getDatumRodjenja());
		student.setJMBG(newStudent.getJMBG());
		student.setUlicaIBroj(newStudent.getUlicaIBroj());
		student.setGrad(newStudent.getGrad());
		student.setRole(role);
		studentRepository.save(student);
		return student;
	}

	/* Metoda za izmenu podataka o uceniku */

	public StudentEntity izmenaUcenika(Integer ucenikId, StudentDTO noviUcenik) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		if (noviUcenik.getImeUcenika() != null)
			ucenik.setImeUcenika(noviUcenik.getImeUcenika());
		if (noviUcenik.getPrezimeUcenika() != null)
			ucenik.setPrezimeUcenika(noviUcenik.getPrezimeUcenika());
		if (noviUcenik.getDatumRodjenja() != null)
			ucenik.setDatumRodjenja(noviUcenik.getDatumRodjenja());
		if (noviUcenik.getJMBG() != null)
			ucenik.setJMBG(noviUcenik.getJMBG());
		if (noviUcenik.getUlicaIBroj() != null)
			ucenik.setUlicaIBroj(noviUcenik.getUlicaIBroj());
		if (noviUcenik.getGrad() != null)
			ucenik.setGrad(noviUcenik.getGrad());
		studentRepository.save(ucenik);
		return ucenik;
	}

	/* Metoda za dodavanje roditelja uceniku */

	@Override
	public StudentEntity dodajRoditeljaUceniku(Integer studentId, Integer parentId) {
		StudentEntity student = studentRepository.findById(studentId).get();
		ParentEntity parent = parentRepository.findById(parentId).get();
		student.setRoditelj(parent);
		studentRepository.save(student);
		return student;
	}

	/*
	 * Metoda za brisanje ucenika, njegovih ocena i roditelja ukoliko nemaju druge
	 * dece
	 */

	public StudentEntity brisanjeUcenika(Integer ucenikId) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		List<GradeEntity> ocene = ucenik.getOcene();
		for (GradeEntity ocena : ocene)
			gradeRepository.delete(ocena);
		ParentEntity roditelj = ucenik.getRoditelj();
		studentRepository.delete(ucenik);
		if (roditelj.getDeca().size() == 0)
			parentRepository.delete(roditelj);
		return ucenik;
	}

	/*
	 * Metoda za promenu odeljenja ucenika uz prenos ocena iz predmeta koji se
	 * poklapaju. Moguce je prebaciti ucenika samo u odeljenje iz istog razreda
	 */

	@Override
	public StudentEntity promenaOdeljenjaUceniku(Integer ucenikId, Integer odeljenjeId) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity staroOdeljenje = ucenik.getOdeljenje();
		ClassEntity novoOdeljenje = classRepository.findById(odeljenjeId).get();
		List<SubjectClassEntity> stariPredmeti = staroOdeljenje.getPredmetiUOdeljenju();
		List<SubjectClassEntity> noviPredmeti = novoOdeljenje.getPredmetiUOdeljenju();
		for (SubjectClassEntity predmet : stariPredmeti) {
			if (subjectService.nadjiPredmetUOdeljenjuPoImenuIzListe(predmet.getName(), noviPredmeti) != null) {
				SubjectClassEntity noviPredmet = subjectService.nadjiPredmetUOdeljenjuPoImenuIzListe(predmet.getName(),
						noviPredmeti);
				for (GradeEntity ocena : predmet.getOcene())
					ocena.setPredmet_odeljenje(noviPredmet);
			} else
				for (GradeEntity ocena : predmet.getOcene())
					gradeRepository.delete(ocena);
		}
		ucenik.setOdeljenje(novoOdeljenje);
		studentRepository.save(ucenik);
		return ucenik;
	}

	/* Pomocna metoda za proveru da li ucenik slusa neki predmet */

	@Override
	public Boolean ucenikSlusaPredmet(Integer ucenikId, Integer predmetId) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		if (subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenje.getId()) != null)
			return true;
		return false;
	}

}
