package com.iktpreobuka.ednevnik.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.controllers.util.RESTError;
import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity.ClassYear;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.ClassYearRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.services.ClassService;

@RestController
@RequestMapping(path = "/ednevnik/odeljenja")
public class ClassController {

	@Autowired
	ClassRepository classRepository;

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	ClassYearRepository classYearRepository;

	@Autowired
	ClassService classService;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/* REST endpoint za prikazivanje svih odeljenja */

	@GetMapping(path = "/SvaOdeljenja")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> prikaziSvaOdeljenja() {
		if (classRepository.findAll() == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedno odeljenje nije pronađeno!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(classRepository.findAll(), HttpStatus.OK);
	}

	/* REST endpoint za pronalazenje odeljenja po ID-u */

	@GetMapping(path = "/{odeljenjeId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> pronadjiOdeljenje(@PathVariable Integer odeljenjeId) {
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.NOT_FOUND);
		return new ResponseEntity<ClassEntity>(classRepository.findById(odeljenjeId).get(), HttpStatus.OK);
	}

	/* REST endpoint za dodavanje novog odeljenja */

	@PostMapping(path = "/")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajOdeljenje(@RequestParam ClassYear razred, @RequestParam Integer brojOdeljenja,
			@RequestParam Integer razredniId) {
		if (classRepository.findByBrojOdeljenjaAndRazredNaziv(brojOdeljenja, razred) != null)
			return new ResponseEntity<RESTError>(new RESTError(2, "Odeljenje je već dodato."), HttpStatus.FORBIDDEN);
		if (!teacherRepository.existsById(razredniId))
			return new ResponseEntity<RESTError>(new RESTError(2, "Nastavnik nije pronađen."), HttpStatus.NOT_FOUND);
		if (teacherRepository.findById(razredniId).get().getOdeljenjeRazrednog() != null)
			return new ResponseEntity<RESTError>(
					new RESTError(2, "Nastavnik je već razredni starešina nekom drugom odeljenju."),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je dodao odeljenje " + razred + "-" + brojOdeljenja
				+ " sa razrednim starešinom " + teacherRepository.findById(razredniId).get().getKorisnickoIme() + ".");
		return new ResponseEntity<ClassEntity>(classService.dodajOdeljenje(razred, brojOdeljenja, razredniId),
				HttpStatus.CREATED);

	}

	/* REST endpoint za dodavanje ili izmenu razredog staresine u odeljenju */

	@PutMapping(path = "/promenaStaresine/{odeljenjeId}/razredni/{nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> promenaRazrednogStaresine(@PathVariable Integer odeljenjeId,
			@PathVariable Integer nastavnikId) {
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.NOT_FOUND);
		if (!teacherRepository.existsById(nastavnikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik nije pronađen!"), HttpStatus.NOT_FOUND);
		if (teacherRepository.findById(nastavnikId).get().getOdeljenjeRazrednog() != null)
			return new ResponseEntity<RESTError>(
					new RESTError(2, "Nastavnik je već razredni starešina nekom drugom odeljenju."),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je odeljenju "
				+ classYearRepository.findByOdeljenjaId(odeljenjeId).getNaziv() + "-"
				+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + " dodao razrednog starešinu "
				+ teacherRepository.findById(nastavnikId).get().getKorisnickoIme() + ".");
		return new ResponseEntity<ClassEntity>(classService.promenaRazrednogStaresine(odeljenjeId, nastavnikId),
				HttpStatus.OK);
	}

	/*
	 * REST endpoint za brisanje odeljenja. Moguce je izbrisati samo ona odeljenja
	 * koja nemaju ucenike
	 */

	@DeleteMapping(path = "/brisanjeOdeljenja/{odeljenjeId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjeOdeljenja(@PathVariable Integer odeljenjeId) {
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.NOT_FOUND);
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		if (odeljenje.getUcenici().size() != 0)
			return new ResponseEntity<RESTError>(
					new RESTError(1,
							"Ne možete obrisati odeljenje koje ima đake. Premestite prvo đake u druga odeljenja!"),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je obrisao odeljenje "
				+ classYearRepository.findByOdeljenjaId(odeljenjeId).getNaziv() + "-"
				+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + ".");
		return new ResponseEntity<ClassEntity>(classService.brisanjeOdeljenja(odeljenjeId), HttpStatus.OK);
	}

}
