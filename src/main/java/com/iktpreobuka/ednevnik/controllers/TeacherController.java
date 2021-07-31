package com.iktpreobuka.ednevnik.controllers;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.controllers.util.RESTError;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.entities.dto.TeacherDTO;
import com.iktpreobuka.ednevnik.repositories.SubjectRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;
import com.iktpreobuka.ednevnik.repositories.UserRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.services.TeacherService;
import com.iktpreobuka.ednevnik.utils.Encryption;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;
import com.iktpreobuka.ednevnik.utils.TeacherCustomValidator;

@RestController
@RequestMapping(path = "/ednevnik/nastavnici")
public class TeacherController {

	@Autowired
	TeacherCustomValidator teacherCustomValidator;

	@Autowired
	TeacherService teacherService;

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	SubjectRepository subjectRepository;

	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		binder.addValidators(teacherCustomValidator);
	}

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/* REST endpoint za prikazivanje svih nastavnika */

	@GetMapping(path = "/SviNastavnici")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> prikaziSveNastavnike() {
		if (teacherRepository.findAll() == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedan nastavnik nije pronađen!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(teacherRepository.findAll(), HttpStatus.OK);
	}

	/* REST endpoint za pronalazenje nastavnika po ID-u */

	@GetMapping(path = "/{nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> nadjiJednogNastavnika(@PathVariable Integer nastavnikId) {
		if (!teacherRepository.existsById(nastavnikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik nije pronađen!"), HttpStatus.FOUND);
		return new ResponseEntity<TeacherEntity>(teacherRepository.findById(nastavnikId).get(), HttpStatus.OK);
	}

	/* REST endpoint za dodavanje novog nastavnika */

	@PostMapping(path = "/")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajNastavnika(@Validated(PostValidation.class) @RequestBody TeacherDTO newTeacher,
			BindingResult result) {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
			teacherCustomValidator.validate(newTeacher, result);
		}
		if (userRepository.findByKorisnickoIme(newTeacher.getKorisnickoIme()) != null) {
			return new ResponseEntity<RESTError>(
					new RESTError(1, "Korisničko ime već postoji! Unesite novo korisničko ime."), HttpStatus.FORBIDDEN);
		}
		TeacherEntity teacher = teacherService.dodajNastavnika(newTeacher);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info(
				"Administrator " + korisnickoIme + " je dodao novog nastavnika " + newTeacher.getKorisnickoIme() + ".");
		return new ResponseEntity<TeacherEntity>(teacher, HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" \n"));
	}

	/* REST endpoint za izmenu podataka o nastavniku */

	@PutMapping(path = "/izmenaNastavnika/{nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> izmenaNastavnika(@PathVariable Integer nastavnikId,
			@Validated(PutValidation.class) @RequestBody TeacherDTO noviNastavnik, BindingResult result) {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		if (!teacherRepository.existsById(nastavnikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik nije pronađen!"), HttpStatus.FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je izmenio podatke o nastavniku "
				+ noviNastavnik.getKorisnickoIme() + ".");
		return new ResponseEntity<TeacherEntity>(teacherService.izmenaNastavnika(nastavnikId, noviNastavnik),
				HttpStatus.OK);
	}

	/* REST endpoint za izmenu sifre nastavnika */

	@PutMapping(path = "/izmenaSifre")
	@Secured({ "ROLE_TEACHER", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> izmeniSifru(@RequestParam String staraSifra, @RequestParam String novaSifra,
			@RequestParam String novaSifraPonovo) {
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
		if (!Encryption.validatePassword(staraSifra, nastavnik.getLozinka()))
			return new ResponseEntity<RESTError>(new RESTError(1, "Uneli ste pogrešnu staru šifru!"),
					HttpStatus.FORBIDDEN);
		if (!novaSifra.equals(novaSifraPonovo))
			return new ResponseEntity<RESTError>(new RESTError(1, "Šifre moraju biti iste!"), HttpStatus.FORBIDDEN);
		nastavnik.setLozinka(Encryption.getPassEncoded(novaSifra));
		teacherRepository.save(nastavnik);
		return new ResponseEntity<TeacherEntity>(nastavnik, HttpStatus.OK);
	}

	/* REST endpoint za brisanje nastavnika */

	@DeleteMapping(path = "/{nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjeNastavnika(@PathVariable Integer nastavnikId) {
		if (!teacherRepository.existsById(nastavnikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik nije pronađen!"), HttpStatus.FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je obrisao nastavnika "
				+ teacherRepository.findById(nastavnikId).get().getKorisnickoIme() + ".");
		return new ResponseEntity<>(teacherService.brisanjeNastavnika(nastavnikId), HttpStatus.OK);

	}

	/* REST endpoint za dodavanje predmeta nastavniku */

	@PutMapping(path = "/dodajPredmetNastavniku/{nastavnikId}/predmet/{predmetId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajNastavnikaPredmetu(@PathVariable Integer nastavnikId,
			@PathVariable Integer predmetId) {
		if (!teacherRepository.existsById(nastavnikId)) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik nije pronađen!"), HttpStatus.FORBIDDEN);
		}
		if (!subjectRepository.existsById(predmetId)) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen!"), HttpStatus.FORBIDDEN);
		}
		if (teacherService.nastavnikPredajePredmet(predmetId, teacherRepository.findById(nastavnikId).get()))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik već predaje ovaj predmet!"),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je dodao nastavnika "
				+ teacherRepository.findById(nastavnikId).get().getKorisnickoIme() + " predmetu "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + ".");
		return new ResponseEntity<TeacherEntity>(teacherService.dodajPredmetNastavniku(nastavnikId, predmetId),
				HttpStatus.CREATED);
	}

	/* REST endpoint koji vraca listu nastavnika koji predaju neki predmet */

	@GetMapping(path = "/nastavniciKojiPredajuPredmet/{nazivPredmeta}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> nastavniciKojiPredajuPredmet(@PathVariable String nazivPredmeta) {
		if (teacherRepository.findByPredmeti_NazivPredmeta(nazivPredmeta) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedan nastavnik nije pronađen!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(teacherRepository.findByPredmeti_NazivPredmeta(nazivPredmeta), HttpStatus.OK);
	}

}
