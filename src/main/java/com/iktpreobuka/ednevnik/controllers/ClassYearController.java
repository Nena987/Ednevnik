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
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.controllers.util.RESTError;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity.ClassYear;
import com.iktpreobuka.ednevnik.entities.dto.ClassYearDTO;
import com.iktpreobuka.ednevnik.repositories.ClassYearRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.services.ClassYearService;
import com.iktpreobuka.ednevnik.utils.ClassYearValidator;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

@RestController
@RequestMapping(path = "/ednevnik/razredi")
public class ClassYearController {

	@Autowired
	ClassYearService classYearService;

	@Autowired
	ClassYearRepository classYearRepository;

	@Autowired
	ClassYearValidator classYearValidator;

	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		binder.addValidators(classYearValidator);
	}

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/* REST endpoint za prikazivanje svih razreda */

	@GetMapping(path = "/SviRazredi")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> prikaziSveRazrede() {
		if (classYearRepository.findAll() == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Lista razreda nije pronađena!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(classYearRepository.findAll(), HttpStatus.OK);
	}

	/* REST endpoing za pronalazenje pojedinacnog razreda */

	@GetMapping(path = "/{razredId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> pronadjiRazredPoId(@PathVariable Integer razredId) {
		if (!classYearRepository.existsById(razredId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Traženi razred nije pronađen!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<ClassYearEntity>(classYearRepository.findById(razredId).get(), HttpStatus.OK);
	}

	/* REST endpoint za dodavanje novog razreda */

	@PostMapping(path = "/")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> docajRazred(@Validated(PostValidation.class) @RequestBody ClassYearDTO newClassYear,
			BindingResult result) {
		if (result.hasErrors())
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);

		if (classYearRepository.findByNaziv(ClassYear.valueOf(newClassYear.getNazivRazreda())) != null) {
			return new ResponseEntity<RESTError>(new RESTError(2, "Razred je već dodat!"), HttpStatus.FORBIDDEN);
		}

		ClassYearEntity classYear = classYearService.dodajRazred(newClassYear);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je dodao razred " + newClassYear.getNazivRazreda() + ".");
		return new ResponseEntity<ClassYearEntity>(classYear, HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" \n"));
	}

	/* REST endpoint za izmenu pojedinacnog razreda */

	@PutMapping(path = "/izmenaRazreda/{razredId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> izmenaRazreda(@PathVariable Integer razredId,
			@Validated(PutValidation.class) @RequestBody ClassYearDTO noviRazred, BindingResult result) {
		if (result.hasErrors())
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		if (!classYearRepository.existsById(razredId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Razred nije pronađen!"), HttpStatus.NOT_FOUND);
		ClassYearEntity razred = classYearRepository.findById(razredId).get();
		if (ClassYear.valueOf(noviRazred.getNazivRazreda()) != null)
			razred.setNaziv(ClassYear.valueOf(noviRazred.getNazivRazreda()));
		classYearRepository.save(razred);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info(
				"Administrator " + korisnickoIme + " je promenio naziv razreda " + noviRazred.getNazivRazreda() + ".");
		return new ResponseEntity<>(razred, HttpStatus.OK);
	}

	/* REST endpoing za brisanje pojedinacnog razreda */

	@DeleteMapping(path = "/{razredId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjeRazreda(@PathVariable Integer razredId) {
		if (!classYearRepository.existsById(razredId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Traženi razred nije pronađen!"),
					HttpStatus.NOT_FOUND);
		if (classYearRepository.findById(razredId).get().getOdeljenja().size() != 0)
			return new ResponseEntity<RESTError>(new RESTError(2, "Razred koji ima odeljenja ne može biti obrisan!"),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je obrisao razred "
				+ classYearRepository.findById(razredId).get().getNaziv() + ".");
		return new ResponseEntity<ClassYearEntity>(classYearService.brisanjeRazreda(razredId), HttpStatus.OK);
	}

	/* REST endpoint za pronalazenje razreda kojima predaje neki nastavnik */

	@GetMapping(path = "/razrediKojimaPredajeNastavnik/nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> razrediKojimaPredajeNastavnik(@PathVariable Integer nastavnikId) {
		if (classYearRepository.findByOdeljenjaPredmetiUOdeljenjuNastavniciId(nastavnikId) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedan razred nije pronađen!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(classYearRepository.findByOdeljenjaPredmetiUOdeljenjuNastavniciId(nastavnikId),
				HttpStatus.OK);
	}

}
