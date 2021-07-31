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
import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.entities.dto.StudentDTO;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.ClassYearRepository;
import com.iktpreobuka.ednevnik.repositories.ParentRepository;
import com.iktpreobuka.ednevnik.repositories.StudentRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;
import com.iktpreobuka.ednevnik.repositories.UserRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.services.StudentService;
import com.iktpreobuka.ednevnik.services.TeacherService;
import com.iktpreobuka.ednevnik.utils.Encryption;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;
import com.iktpreobuka.ednevnik.utils.StudentCustomValidator;

@RestController
@RequestMapping(path = "/ednevnik/ucenici")
public class StudentController {

	@Autowired
	StudentCustomValidator studentCustomValidator;

	@Autowired
	StudentService studentService;

	@Autowired
	StudentRepository studentRepository;

	@Autowired
	ParentRepository parentRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ClassRepository classRepository;

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	TeacherService teacherService;

	@Autowired
	ClassYearRepository classYearRepository;

	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		binder.addValidators(studentCustomValidator);
	}

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/* REST endpoint za prikazivanje svih ucenika */

	@GetMapping(path = "/SviUcenici")
	@Secured("ROLE_ADMIN")
	public ResponseEntity<?> prikaziSveUcenike() {
		if (studentRepository.findAll() == null) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedan učenik nije pronađen!"),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(studentRepository.findAll(), HttpStatus.OK);
	}

	/*
	 * REST endpoint za prikazivanje ucenika po ID-u Pristup ima samo administrator
	 */

	@GetMapping(path = "/jedanUcenik/Admin/{ucenikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> pronadjiUcenikaPoId(@PathVariable Integer ucenikId) {
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen!"), HttpStatus.NOT_FOUND);
		return new ResponseEntity<StudentEntity>(studentRepository.findById(ucenikId).get(), HttpStatus.OK);
	}

	/*
	 * REST endpoint za prikazivanje ucenika po ID-u Prisput metoti imaju razredni
	 * staresina i nastavnici koji predaju odeljenju u kome je ucenik
	 */

	@GetMapping(path = "/jedanUcenik/Nastavnik/{ucenikId}")
	@Secured({ "ROLE_TEACHER", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> pronadjiUcenikaPoIdNastavnik(@PathVariable Integer ucenikId) {
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen!"), HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		if (!teacherService.nadjiNastavnikaKojiPredajeUceniku(ucenik).contains(nastavnik)
				&& !teacherService.nastavnikJeRazredniUceniku(ucenikId, nastavnik))
			return new ResponseEntity<RESTError>(
					new RESTError(1, "Ne možete videti podatke o učeniku kome ne predajete!"), HttpStatus.FORBIDDEN);
		return new ResponseEntity<StudentEntity>(studentRepository.findById(ucenikId).get(), HttpStatus.OK);
	}

	/* REST endpoint za dodavanje novog ucenika */

	@PostMapping(path = "/")
	@Secured("ROLE_ADMIN")
	public ResponseEntity<?> dodajNovogUcenika(@Validated(PostValidation.class) @RequestBody StudentDTO newStudent,
			BindingResult result) {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else
			studentCustomValidator.validate(newStudent, result);
		if (userRepository.findByKorisnickoIme(newStudent.getKorisnickoIme()) != null) {
			return new ResponseEntity<RESTError>(
					new RESTError(1, "Korisničko ime već postoji! Unesite novo korisničko ime."), HttpStatus.FORBIDDEN);
		}
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info(
				"Administrator " + korisnickoIme + " je dodao novog učenika " + newStudent.getKorisnickoIme() + ".");
		return new ResponseEntity<StudentEntity>(studentService.dodajStudenta(newStudent), HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" \n"));
	}

	@PutMapping(path = "/izmenaUcenika/{ucenikId}")
	@Secured({ "ROLE_ADMIN", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> izmenaUcenika(@Validated(PutValidation.class) @RequestBody StudentDTO noviUcenik,
			@PathVariable Integer ucenikId, BindingResult result) {
		if (result.hasErrors())
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen!"), HttpStatus.FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (teacherRepository.findByKorisnickoIme(korisnickoIme) != null) {
			TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
			if (!teacherService.nastavnikJeRazredniUceniku(ucenikId, nastavnik))
				return new ResponseEntity<RESTError>(
						new RESTError(2, "Ne možete izmeniti učenika kome niste razredni starešina!"),
						HttpStatus.FORBIDDEN);
		}
		logger.info("Korisnik " + korisnickoIme + " je izmenio podatke o učeniku sa korisničkim imenom "
				+ studentRepository.findById(ucenikId).get().getKorisnickoIme() + ".");
		return new ResponseEntity<StudentEntity>(studentService.izmenaUcenika(ucenikId, noviUcenik), HttpStatus.OK);
	}

	/* REST endpoint za promenu sifre ucenika */

	@PutMapping(path = "/izmenaSifre")
	@Secured("ROLE_STUDENT")
	@JsonView(Views.Public.class)
	public ResponseEntity<?> izmeniSifru(@RequestParam String staraSifra, @RequestParam String novaSifra,
			@RequestParam String novaSifraPonovo) {
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		StudentEntity ucenik = studentRepository.findByKorisnickoIme(korisnickoIme);
		if (!Encryption.validatePassword(staraSifra, ucenik.getLozinka()))
			return new ResponseEntity<RESTError>(new RESTError(1, "Uneli ste pogrešnu staru šifru!"),
					HttpStatus.FORBIDDEN);
		if (!novaSifra.equals(novaSifraPonovo))
			return new ResponseEntity<RESTError>(new RESTError(1, "Šifre moraju biti iste!"), HttpStatus.FORBIDDEN);
		ucenik.setLozinka(Encryption.getPassEncoded(novaSifra));
		studentRepository.save(ucenik);
		return new ResponseEntity<StudentEntity>(ucenik, HttpStatus.OK);
	}

	/*
	 * REST endpoint za brisanje ucenika, njegovih ocena i roditelja ukoliko nema
	 * druge dece
	 */

	@DeleteMapping(path = "/brisanjeUcenika/{ucenikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjeUcenika(@PathVariable Integer ucenikId) {
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen!"), HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je obrisao učenika "
				+ studentRepository.findById(ucenikId).get().getKorisnickoIme() + ".");
		return new ResponseEntity<StudentEntity>(studentService.brisanjeUcenika(ucenikId), HttpStatus.OK);
	}

	/* REST endpoint za dodavanje i izmenu roditelja uceniku */

	@PutMapping(path = "/{ucenikId}/{roditeljId}")
	@Secured({ "ROLE_ADMIN", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> addParentToStudent(@PathVariable Integer ucenikId, @PathVariable Integer roditeljId) {
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen"), HttpStatus.NOT_FOUND);
		if (!parentRepository.existsById(roditeljId))
			return new ResponseEntity<RESTError>(new RESTError(2, "Roditelj nije pronađen."), HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (teacherRepository.findByKorisnickoIme(korisnickoIme) != null) {
			TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
			if (!teacherService.nastavnikJeRazredniUceniku(ucenikId, nastavnik))
				return new ResponseEntity<RESTError>(
						new RESTError(2, "Ne možete izmeniti učenika kome niste razredni starešina!"),
						HttpStatus.FORBIDDEN);
		}
		logger.info("Korisnik " + korisnickoIme + " je dodao roditelja "
				+ parentRepository.findById(roditeljId).get().getKorisnickoIme() + " učeniku "
				+ studentRepository.findById(ucenikId).get().getKorisnickoIme() + ".");
		return new ResponseEntity<StudentEntity>(studentService.dodajRoditeljaUceniku(ucenikId, roditeljId),
				HttpStatus.OK);
	}

	/* REST endpoint za dodavanje ucenika u odeljenje */

	@PutMapping(path = "/dodajUcenikaUOdeljenje/odeljenje/{odeljenjeId}/ucenik/{ucenikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajUcenikaUOdeljenje(@PathVariable Integer odeljenjeId, @PathVariable Integer ucenikId) {
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno."), HttpStatus.NOT_FOUND);
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen."), HttpStatus.NOT_FOUND);
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		if (ucenik.getOdeljenje() != null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik se već nalazi u drugom odeljenju."),
					HttpStatus.NOT_FOUND);
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		ucenik.setOdeljenje(odeljenje);
		studentRepository.save(ucenik);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je dodao učenika " + ucenik.getKorisnickoIme()
				+ " u odeljenje " + classYearRepository.findByOdeljenjaId(odeljenjeId).getNaziv() + "-"
				+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + ".");
		return new ResponseEntity<StudentEntity>(ucenik, HttpStatus.OK);
	}

	/*
	 * REST endpoint za promenu odeljenja uceniku i prenosenje ocena iz svih
	 * predmeta koji se poklapaju
	 */

	@PutMapping(path = "/promenaOdeljenjaUceniku/{ucenikId}/novoOdeljenje/{odeljenjeId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> promenaOdeljenjaUceniku(@PathVariable Integer ucenikId,
			@PathVariable Integer odeljenjeId) {
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno."), HttpStatus.NOT_FOUND);
		if (!studentRepository.existsById(ucenikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Učenik nije pronađen."), HttpStatus.NOT_FOUND);
		if (!studentRepository.findById(ucenikId).get().getOdeljenje().getRazred()
				.equals(classRepository.findById(odeljenjeId).get().getRazred()))
			return new ResponseEntity<RESTError>(
					new RESTError(1, "Učenika možete prebaciti samo u odeljenje iz istog razreda!"),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je prebacio učenika "
				+ studentRepository.findById(ucenikId).get().getKorisnickoIme() + " u odeljenje "
				+ classYearRepository.findByOdeljenjaId(odeljenjeId).getNaziv() + "-"
				+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + ".");
		return new ResponseEntity<StudentEntity>(studentService.promenaOdeljenjaUceniku(ucenikId, odeljenjeId),
				HttpStatus.OK);
	}

	/* REST endpoint za pronalazenje svih ucenika iz jednog razreda */

	@GetMapping(path = "/listaSvihUcenikaIzRazreda/{razredId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> sviUceniciURazredu(@PathVariable Integer razredId) {
		if (!classYearRepository.existsById(razredId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Razred nije pronađen!"), HttpStatus.NOT_FOUND);
		if (studentRepository.findByOdeljenjeRazredId(razredId) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedan učenik nije pronađen!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(studentRepository.findByOdeljenjeRazredId(razredId), HttpStatus.OK);
	}

}