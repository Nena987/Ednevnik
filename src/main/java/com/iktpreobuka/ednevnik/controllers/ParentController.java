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
import com.iktpreobuka.ednevnik.entities.ParentEntity;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.entities.dto.ParentDTO;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.ParentRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;
import com.iktpreobuka.ednevnik.repositories.UserRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.services.ParentService;
import com.iktpreobuka.ednevnik.services.TeacherService;
import com.iktpreobuka.ednevnik.utils.Encryption;
import com.iktpreobuka.ednevnik.utils.ParentCustomValidator;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

@RestController
@RequestMapping(path = "/ednevnik/roditelji")
public class ParentController {

	@Autowired
	ParentCustomValidator parentCustomValidator;

	@Autowired
	ParentRepository parentRepository;

	@Autowired
	ParentService parentService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	TeacherService teacherService;

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	ClassRepository classRepository;

	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		binder.addValidators(parentCustomValidator);
	}

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/* REST endpoint za prikazivanje svih roditelja */

	@GetMapping(path = "/SviRoditelji")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> prikaziSveRoditelje() {
		if (parentRepository.findAll() == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedan roditelj nije pronađen!"), HttpStatus.OK);
		return new ResponseEntity<>(parentRepository.findAll(), HttpStatus.OK);
	}

	/* REST endpoint za pronalazenje roditelja po ID-u */

	@GetMapping(path = "/{roditeljId}")
	@Secured({ "ROLE_ADMIN", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Private.class)
	public ResponseEntity<?> nadjiJednojRoditelja(@PathVariable Integer roditeljId) {
		if (!parentRepository.existsById(roditeljId)) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Roditelj nije pronađen!"), HttpStatus.NOT_FOUND);
		}
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (teacherRepository.findByKorisnickoIme(korisnickoIme) != null) {
			TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
			if (!teacherService.nastavnikJeRazredniRoditeljevomDetetu(roditeljId, nastavnik))
				return new ResponseEntity<RESTError>(
						new RESTError(2, "Ne možete imati uvid u roditelja čijoj deci niste razredni starešina!"),
						HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<ParentEntity>(parentRepository.findById(roditeljId).get(), HttpStatus.OK);
	}

	/*
	 * REST endpoint za dodavanje novog roditelja Pristup metodi ima samo admin
	 */

	@PostMapping(path = "/")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajRoditelja(@Validated(PostValidation.class) @RequestBody ParentDTO noviRoditelj,
			BindingResult result) {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else
			parentCustomValidator.validate(noviRoditelj, result);
		if (userRepository.findByKorisnickoIme(noviRoditelj.getKorisnickoIme()) != null) {
			return new ResponseEntity<RESTError>(
					new RESTError(1, "Korisničko ime već postoji! Unesite novo korisničko ime."), HttpStatus.FORBIDDEN);
		}
		ParentEntity parent = parentService.dodajRoditelja(noviRoditelj);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je dodao novog roditelja " + noviRoditelj.getKorisnickoIme()
				+ ".");
		return new ResponseEntity<ParentEntity>(parent, HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" \n"));
	}

	/* REST endpoint za izmenu podataka o roditelju */

	@PutMapping(path = "/izmenaRoditelja/{roditeljId}")
	@Secured({ "ROLE_ADMIN", "ROLE_CLASSTEACHER" })
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> izmenaRoditelja(@PathVariable Integer roditeljId,
			@Validated(PutValidation.class) @RequestBody ParentDTO noviRoditelj, BindingResult result) {
		if (result.hasErrors())
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		if (!parentRepository.existsById(roditeljId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Roditelj nije pronađen!"), HttpStatus.FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (teacherRepository.findByKorisnickoIme(korisnickoIme) != null) {
			TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
			if (!teacherService.nastavnikJeRazredniRoditeljevomDetetu(roditeljId, nastavnik))
				return new ResponseEntity<RESTError>(
						new RESTError(2, "Ne možete izmeniti roditelja čijoj deci niste razredni starešina!"),
						HttpStatus.FORBIDDEN);
		}
		logger.info("Korisnik " + korisnickoIme + " je izmenio podatke o roditelju sa korisničkim imenom "
				+ noviRoditelj.getKorisnickoIme() + ".");
		return new ResponseEntity<ParentEntity>(parentService.izmenaRoditelja(roditeljId, noviRoditelj), HttpStatus.OK);
	}

	/* REST endpoint za izmenu sifre roditelja */

	@PutMapping(path = "/izmenaSifre")
	@Secured("ROLE_PARENT")
	@JsonView(Views.Private.class)
	public ResponseEntity<?> izmeniSifru(@RequestParam String staraSifra, @RequestParam String novaSifra,
			@RequestParam String novaSifraPonovo) {
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		ParentEntity roditelj = parentRepository.findByKorisnickoIme(korisnickoIme);
		if (!Encryption.validatePassword(staraSifra, roditelj.getLozinka()))
			return new ResponseEntity<RESTError>(new RESTError(1, "Uneli ste pogrešnu staru šifru!"),
					HttpStatus.FORBIDDEN);
		if (!novaSifra.equals(novaSifraPonovo))
			return new ResponseEntity<RESTError>(new RESTError(1, "Šifre moraju biti iste!"), HttpStatus.FORBIDDEN);
		roditelj.setLozinka(Encryption.getPassEncoded(novaSifra));
		parentRepository.save(roditelj);
		return new ResponseEntity<ParentEntity>(roditelj, HttpStatus.OK);
	}

	/* REST endpoint za brisanje roditelja */

	@DeleteMapping(path = "/brisanjeRoditelja/{roditeljId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjeRoditelja(@PathVariable Integer roditeljId) {
		if (!parentRepository.existsById(roditeljId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Roditelj nije pronađen!"), HttpStatus.FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je izbrisao roditelja "
				+ parentRepository.findById(roditeljId).get().getKorisnickoIme() + ".");
		return new ResponseEntity<ParentEntity>(parentService.brisanjeRoditelja(roditeljId), HttpStatus.OK);
	}

	/* REST endpoint za pronalazenje svih roditelja dece iz jednoj odeljenja */

	@GetMapping(path = "/roditeljiDeceUOdeljenju/{odeljenjeId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> roditeljiDeceUOdeljenju(@PathVariable Integer odeljenjeId) {
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.NOT_FOUND);
		if (parentRepository.findByDecaOdeljenjeId(odeljenjeId) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedan roditelj nije pronađen!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(parentRepository.findByDecaOdeljenjeId(odeljenjeId), HttpStatus.OK);
	}

}
