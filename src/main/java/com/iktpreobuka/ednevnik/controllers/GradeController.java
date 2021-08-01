package com.iktpreobuka.ednevnik.controllers;

import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.controllers.util.RESTError;
import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.GradeEntity;
import com.iktpreobuka.ednevnik.entities.GradeEntity.Polugodiste;
import com.iktpreobuka.ednevnik.entities.ParentEntity;
import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.entities.dto.GradeDTO;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.ClassYearRepository;
import com.iktpreobuka.ednevnik.repositories.GradeRepository;
import com.iktpreobuka.ednevnik.repositories.ParentRepository;
import com.iktpreobuka.ednevnik.repositories.StudentRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectClassRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.services.EmailService;
import com.iktpreobuka.ednevnik.services.GradeService;
import com.iktpreobuka.ednevnik.services.StudentService;
import com.iktpreobuka.ednevnik.services.TeacherService;
import com.iktpreobuka.ednevnik.utils.GradePolugodisteValidator;
import com.iktpreobuka.ednevnik.utils.GradeVrstaOceneValidator;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

@RestController
@RequestMapping(path = "/ednevnik/ocene")
public class GradeController {

	@Autowired
	GradeRepository gradeRepository;

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	SubjectRepository subjectRepository;

	@Autowired
	StudentRepository studentRepository;

	@Autowired
	SubjectClassRepository subjectClassRepository;

	@Autowired
	ClassRepository classRepository;

	@Autowired
	ParentRepository parentRepository;

	@Autowired
	GradeService gradeService;

	@Autowired
	EmailService emailService;

	@Autowired
	TeacherService teacherService;

	@Autowired
	StudentService studentService;

	@Autowired
	ClassYearRepository classYearRepository;

	@Autowired
	GradeVrstaOceneValidator vrstaOceneValidator;

	@Autowired
	GradePolugodisteValidator polugodisteValidator;

	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		binder.addValidators(vrstaOceneValidator);
		binder.addValidators(polugodisteValidator);
	}

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/*
	 * REST endpoint za davanje ocene uceniku Pristup metodi ima administrator
	 */

	@PostMapping(path = "/dodajOcenuUcenikuAdmin/predmet/{predmetId}/ucenik/{ucenikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajOcenuUcenikuAdmin(@PathVariable Integer ucenikId, @PathVariable Integer predmetId,
			@Validated(PostValidation.class) @RequestBody GradeDTO novaOcena, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
			vrstaOceneValidator.validate(novaOcena, result);
			polugodisteValidator.validate(novaOcena, result);
		}
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen."), HttpStatus.NOT_FOUND);
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen."), HttpStatus.NOT_FOUND);
		if (!studentService.ucenikSlusaPredmet(ucenikId, predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik ne sluša ovaj predmet."),
					HttpStatus.NOT_FOUND);
		if (gradeService.zakljucenaOcena(subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId,
				classRepository.findByUceniciId(ucenikId).getId()), ucenikId, novaOcena.getPolugodiste()))
			return new ResponseEntity<RESTError>(
					new RESTError(2, "Ne možete dati novu ocenu nakon što je ocena zakjljučena."),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je učeniku "
				+ studentRepository.findById(ucenikId).get().getKorisnickoIme() + " dao ocenu iz predmeta "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + ".");
		emailService.posaljiMejlRoditeljuAdmin(gradeService.dodajOcenuUceniku(ucenikId, predmetId, novaOcena));
		return new ResponseEntity<GradeEntity>(gradeService.dodajOcenuUceniku(ucenikId, predmetId, novaOcena),
				HttpStatus.CREATED);
	}

	/* REST endpoint za davanje ocene uceniku */

	@PostMapping(path = "/dodajOcenuUceniku/predmet/{predmetId}/ucenik/{ucenikId}")
	@Secured({ "ROLE_TEACHER", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> dodajOcenuUceniku(@PathVariable Integer ucenikId, @PathVariable Integer predmetId,
			@Validated(PostValidation.class) @RequestBody GradeDTO novaOcena, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
			vrstaOceneValidator.validate(novaOcena, result);
			polugodisteValidator.validate(novaOcena, result);
		}
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen."), HttpStatus.NOT_FOUND);
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen."), HttpStatus.NOT_FOUND);
		if (!teacherService.nastavnikPredajePredmet(predmetId, nastavnik))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik ne predaje ovaj predmet."),
					HttpStatus.FORBIDDEN);
		if (!studentService.ucenikSlusaPredmet(ucenikId, predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik ne sluša ovaj predmet."),
					HttpStatus.NOT_FOUND);
		if (!teacherService.nastavnikPredajePredmetUceniku(predmetId, ucenikId, nastavnik))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik ne predaje ovaj predmet ovom odeljenju."),
					HttpStatus.FORBIDDEN);
		if (gradeService.zakljucenaOcena(subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId,
				classRepository.findByUceniciId(ucenikId).getId()), ucenikId, novaOcena.getPolugodiste()))
			return new ResponseEntity<RESTError>(
					new RESTError(2, "Ne možete dati novu ocenu nakon što je ocena zakjljučena."),
					HttpStatus.FORBIDDEN);
		logger.info("Nastavnik " + korisnickoIme + " je učeniku "
				+ studentRepository.findById(ucenikId).get().getKorisnickoIme() + " dao ocenu iz predmeta "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + ".");
		emailService.posaljiMejlRoditelju(gradeService.dodajOcenuUceniku(ucenikId, predmetId, novaOcena));
		return new ResponseEntity<GradeEntity>(gradeService.dodajOcenuUceniku(ucenikId, predmetId, novaOcena),
				HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" \n"));
	}

	/*
	 * REST endpoint za zakljucivanje ocene uceniku. Pristup metodi ima samo
	 * administrator
	 */

	@PostMapping(path = "/ZakljuciOcenuUcenikuAdmin/predmet/{predmetId}/ucenik/{ucenikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Private.class)
	public ResponseEntity<?> zakljuciOcenuUcenikuAdmin(@PathVariable Integer ucenikId, @PathVariable Integer predmetId,
			@Validated(PostValidation.class) @RequestBody GradeDTO novaOcena, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
			vrstaOceneValidator.validate(novaOcena, result);
			polugodisteValidator.validate(novaOcena, result);
		}
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen."), HttpStatus.NOT_FOUND);
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen."), HttpStatus.NOT_FOUND);
		if (!studentService.ucenikSlusaPredmet(ucenikId, predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik ne sluša ovaj predmet."),
					HttpStatus.NOT_FOUND);
		if (gradeService.zakljucenaOcena(subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId,
				classRepository.findByUceniciId(ucenikId).getId()), ucenikId, novaOcena.getPolugodiste()))
			return new ResponseEntity<RESTError>(new RESTError(2, "Ocena je već zakjljučena."), HttpStatus.FORBIDDEN);
		emailService.posaljiMejlRoditeljuAdmin(gradeService.zakljuciOcenuUceniku(ucenikId, predmetId, novaOcena));
		logger.info("Administrator " + korisnickoIme + " je učeniku "
				+ studentRepository.findById(ucenikId).get().getKorisnickoIme() + " zaključio ocenu iz predmeta "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + ".");
		return new ResponseEntity<GradeEntity>(gradeService.zakljuciOcenuUceniku(ucenikId, predmetId, novaOcena),
				HttpStatus.CREATED);

	}

	/*
	 * REST endpoint za zakljucivanje ocene. Pristup ima samo nastavnik koji predaje
	 * taj predmet odeljenju
	 */

	@PostMapping(path = "/ZakljuciOcenuUceniku/predmet/{predmetId}/ucenik/{ucenikId}")
	@Secured({ "ROLE_TEACHER", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> zakljuciOcenuUceniku(@PathVariable Integer ucenikId, @PathVariable Integer predmetId,
			@Validated(PostValidation.class) @RequestBody GradeDTO novaOcena, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
			vrstaOceneValidator.validate(novaOcena, result);
			polugodisteValidator.validate(novaOcena, result);
		}
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen."), HttpStatus.NOT_FOUND);
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen."), HttpStatus.NOT_FOUND);
		if (!teacherService.nastavnikPredajePredmet(predmetId, nastavnik))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik ne predaje ovaj predmet."),
					HttpStatus.FORBIDDEN);
		if (!studentService.ucenikSlusaPredmet(ucenikId, predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik ne sluša ovaj predmet."),
					HttpStatus.NOT_FOUND);
		if (!teacherService.nastavnikPredajePredmetUceniku(predmetId, ucenikId, nastavnik))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik ne predaje ovaj predmet ovom odeljenju."),
					HttpStatus.FORBIDDEN);
		if (gradeService.zakljucenaOcena(subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId,
				classRepository.findByUceniciId(ucenikId).getId()), ucenikId, novaOcena.getPolugodiste()))
			return new ResponseEntity<RESTError>(new RESTError(2, "Ocena je već zakjljučena."), HttpStatus.FORBIDDEN);

		logger.info("Nastavnik " + korisnickoIme + " je učeniku "
				+ studentRepository.findById(ucenikId).get().getKorisnickoIme() + " zaključio ocenu iz predmeta "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + ".");
		emailService.posaljiMejlRoditelju(gradeService.zakljuciOcenuUceniku(ucenikId, predmetId, novaOcena));
		return new ResponseEntity<GradeEntity>(gradeService.zakljuciOcenuUceniku(ucenikId, predmetId, novaOcena),
				HttpStatus.CREATED);

	}

	/*
	 * REST endpoint za izmenu ocene. Moze se promeniti samo sama ocena i vrsta
	 * ocene.
	 */

	@PutMapping(path = "/izmenaOcene/{ocenaId}")
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> izmenaOcene(@PathVariable Integer ocenaId,
			@Validated(PutValidation.class) @RequestBody GradeDTO novaOcena, BindingResult result) {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
			vrstaOceneValidator.validate(novaOcena, result);
		}
		if (!gradeRepository.existsById(ocenaId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Ocena nije pronađena!"), HttpStatus.FOUND);
		GradeEntity ocena = gradeRepository.findById(ocenaId).get();
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (teacherRepository.findByKorisnickoIme(korisnickoIme) != null) {
			TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
			if (!ocena.getPredmet_odeljenje().getNastavnici().contains(nastavnik))
				return new ResponseEntity<RESTError>(
						new RESTError(2, "Ne predejate ovaj predmet ovom odeljenju. Ne možete izmeniti ocenu!"),
						HttpStatus.FORBIDDEN);
		}
		logger.info("Korisnik " + korisnickoIme + " je učeniku " + ocena.getUcenik().getKorisnickoIme()
				+ " promenio ocenu iz predmeta " + ocena.getPredmet_odeljenje().getName() + ".");
		return new ResponseEntity<GradeEntity>(gradeService.izmenaOcene(ocenaId, novaOcena), HttpStatus.OK);
	}

	/*
	 * REST endpoint za brisanje ocene. Pristup metodi imaju administrator i
	 * nastavnik koji predaje predmet u tom odeljenju
	 */

	@DeleteMapping(path = "/brisanjeOcene/{ocenaId}")
	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> brisanjeOcene(@PathVariable Integer ocenaId) {
		if (!gradeRepository.existsById(ocenaId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Ocena nije pronađena!"), HttpStatus.FOUND);
		GradeEntity ocena = gradeRepository.findById(ocenaId).get();
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (teacherRepository.findByKorisnickoIme(korisnickoIme) != null) {
			TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
			if (!ocena.getPredmet_odeljenje().getNastavnici().contains(nastavnik))
				return new ResponseEntity<RESTError>(
						new RESTError(2, "Ne predejate ovaj predmet ovom odeljenju. Ne možete obrisati ocenu!"),
						HttpStatus.FORBIDDEN);
		}
		gradeRepository.delete(ocena);
		logger.info("Korisnik " + korisnickoIme + " je učeniku " + ocena.getUcenik().getKorisnickoIme()
				+ " obrisao ocenu iz predmeta " + ocena.getPredmet_odeljenje().getName() + ".");
		return new ResponseEntity<GradeEntity>(ocena, HttpStatus.OK);
	}

	/* REST endpoint za pretragu ocene za jednog studenta za jedan predmet */

	@GetMapping(path = "/pregledOceneZaStudentaIzPredmeta/predmet/{predmetId}/ucenik/{ucenikId}")
	@Secured({ "ROLE_ADMIN", "ROLE_CLASSTEACHER", "ROLE_TEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> pregledOcenaUcenikaZaPredmet(@PathVariable Integer predmetId,
			@PathVariable Integer ucenikId) {
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen."), HttpStatus.NOT_FOUND);
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen."), HttpStatus.NOT_FOUND);
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenje.getId());
		if (subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenje.getId()) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet se ne sluša u ovom odeljenju!"),
					HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		/*
		 * Nastavnik moze da vidi ocene ako predaje predmet tom odeljenju ili je
		 * razredni staresina tom odeljenju
		 */
		if (teacherRepository.findByKorisnickoIme(korisnickoIme) != null) {
			TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
			if (!teacherService.nastavnikPredajePredmetUceniku(predmetId, ucenikId, nastavnik)
					&& !teacherService.nastavnikJeRazredniOdeljenju(odeljenje.getId(), nastavnik))
				return new ResponseEntity<RESTError>(
						new RESTError(1, "Nastavnik ne predaje ovaj predmet ovom odeljenju."), HttpStatus.FORBIDDEN);
			logger.info("Nastavnik " + korisnickoIme + " je pregledao ocene učenika" + ucenik.getKorisnickoIme()
					+ " iz predmeta " + predmet.getName() + ".");
		} else
			logger.info("Administrator " + korisnickoIme + " je pregledao ocene učenika " + ucenik.getKorisnickoIme()
					+ " iz predmeta " + predmet.getName() + ".");
		return new ResponseEntity<>(gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmet, ucenikId),
				HttpStatus.OK);
	}

	/* REST endpoint za prikazivanje svih ocena ulogovanog ucenika */

	@GetMapping(path = "/prikaziSveOceneUcenika/polugodiste/{polugodiste}")
	@Secured("ROLE_STUDENT")
	@JsonView(Views.Public.class)
	public ResponseEntity<?> prikaziSveOceneUcenika(@PathVariable Polugodiste polugodiste) {
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		StudentEntity ucenik = studentRepository.findByKorisnickoIme(korisnickoIme);
		logger.info("Učenik " + korisnickoIme + " je pogledao svoje ocene iz svih predmeta.");
		return new ResponseEntity<>(gradeService.prikaziSveOceneUcenika(ucenik, polugodiste), HttpStatus.OK);

	}

	/* REST endpoint za prikazivanje svih ocena jednog ucenika */

	@GetMapping(path = "/prikaziSveOceneJednogUcenika/{ucenikId}/polugodiste/{polugodiste}")
	@Secured({ "ROLE_ADMIN", "ROLE_PARENT" })
	@JsonView(Views.Public.class)
	public ResponseEntity<?> prikaziSveOceneJednogUcenika(@PathVariable Integer ucenikId,
			@PathVariable Polugodiste polugodiste) {
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen!"), HttpStatus.NOT_FOUND);
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

		/* Roditelj moze da vidi ocene samo za svoju decu. */

		if (parentRepository.findByKorisnickoIme(korisnickoIme) != null) {
			ParentEntity roditelj = parentRepository.findByKorisnickoIme(korisnickoIme);
			if (!roditelj.getDeca().contains(ucenik))
				return new ResponseEntity<RESTError>(new RESTError(1, "Ne možete videti ocene ovog učenika!"),
						HttpStatus.NOT_FOUND);
			logger.info("Roditelj " + korisnickoIme + " je pregledao ocene učenika " + ucenik.getKorisnickoIme()
					+ " iz svih predmeta.");
		} else
			/* Admin moze da vidi ocene za svakog ucenika */
			logger.info("Administrator " + korisnickoIme + " je pregledao ocene učenika " + ucenik.getKorisnickoIme()
					+ " iz svih predmeta.");
		return new ResponseEntity<>(gradeService.prikaziSveOceneUcenika(ucenik, polugodiste), HttpStatus.OK);
	}

	/*
	 * REST endpoint za prikazivanje svih ocena iz jednog predmeta za celo odeljenje
	 */

	@GetMapping(path = "/pregledOcenaIzPredmetaZaOdeljenje/predmet/{predmetId}/odeljenje/{odeljenjeId}/polugodiste/{polugodiste}")
	@Secured({ "ROLE_TEACHER", "ROLE_CLASSTEACHER", "ROLE_ADMIN" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> pregledOcenaIzPredmetaZaOdeljenje(@PathVariable Integer predmetId,
			@PathVariable Integer odeljenjeId, @PathVariable Polugodiste polugodiste) {
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Traženo odeljenje ne postoji!"),
					HttpStatus.NOT_FOUND);
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Traženi predmet ne postoji!"), HttpStatus.NOT_FOUND);
		if (subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Traženi predmet se ne sluša u ovom odeljenju!"),
					HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		/*
		 * Nastavnik moze da vidi ocene ako predaje predmet tom odeljenju ili je
		 * razredni staresina tom odeljenju
		 */
		if (teacherRepository.findByKorisnickoIme(korisnickoIme) != null) {
			TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
			if (!teacherService.nastavnikPredajePredmet(predmetId, nastavnik)
					&& !teacherService.nastavnikJeRazredniOdeljenju(odeljenjeId, nastavnik))
				return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik ne predaje ovaj predmet!"),
						HttpStatus.BAD_REQUEST);

			if (!teacherService.nastavnikPredajePredmetOdeljenju(predmetId, odeljenjeId, nastavnik)
					&& !teacherService.nastavnikJeRazredniOdeljenju(odeljenjeId, nastavnik))
				return new ResponseEntity<RESTError>(
						new RESTError(1, "Nastavnik ne predaje ovaj predmet ovom odeljenju."), HttpStatus.FORBIDDEN);
			logger.info("Nastavnik " + korisnickoIme + " je pregledao ocene odeljenja "
					+ classYearRepository.findByOdeljenjaId(odeljenjeId).getNaziv() + "-"
					+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + " iz predmeta "
					+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + ".");

		} else
			logger.info("Administrator " + korisnickoIme + " je pregledao ocene odeljenja "
					+ classYearRepository.findByOdeljenjaId(odeljenjeId).getNaziv() + "-"
					+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + " iz predmeta "
					+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + ".");
		return new ResponseEntity<>(gradeService.pregledOcenaIzPredmetaZaOdeljenje(predmetId, odeljenjeId, polugodiste),
				HttpStatus.OK);
	}

	@GetMapping(path = "/oceneZaOdeljenjePDF/{odeljenjeId}")
	@Secured("ROLE_ADMIN")
	public ResponseEntity<?> oceneZaOdeljenjePDF(@PathVariable Integer odeljenjeId) {
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.NOT_FOUND);
		ByteArrayInputStream bis = gradeService.ocenePDF(odeljenjeId);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=ocenePDF.pdf");
		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(bis));
	}

}
