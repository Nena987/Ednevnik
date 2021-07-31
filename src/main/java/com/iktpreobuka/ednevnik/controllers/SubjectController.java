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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.controllers.util.RESTError;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.entities.SubjectEntity;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.ClassYearRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectClassRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectRepository;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.services.SubjectService;
import com.iktpreobuka.ednevnik.services.TeacherService;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

@RestController
@RequestMapping(path = "/ednevnik/predmeti")
public class SubjectController {

	@Autowired
	SubjectRepository subjectRepository;

	@Autowired
	SubjectService subjectService;

	@Autowired
	ClassYearRepository classYearRepository;

	@Autowired
	ClassRepository classRepository;

	@Autowired
	TeacherRepository teacherRepository;

	@Autowired
	SubjectClassRepository subjectClassRepository;

	@Autowired
	TeacherService teacherService;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/* REST endpoint za prikazivanje svih predmeta */

	@GetMapping(path = "/SviPredmeti")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> prikaziSvePredmete() {
		if (subjectRepository.findAll() == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedan predmet nije pronađen!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(subjectRepository.findAll(), HttpStatus.OK);
	}

	/* REST endpoint za prikazivanje pojedinacnog predmeta */

	@GetMapping(path = "/{predmetId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> nadjiPredmetPoId(@PathVariable Integer predmetId) {
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen!"), HttpStatus.NOT_FOUND);
		return new ResponseEntity<SubjectEntity>(subjectRepository.findById(predmetId).get(), HttpStatus.OK);

	}

	/* REST endpoint za dodavanje novog predmeta */

	@PostMapping(path = "/")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajPredmet(@Validated(PostValidation.class) @RequestBody SubjectEntity newSubject,
			BindingResult result) {
		if (result.hasErrors())
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		if (subjectRepository.findByNazivPredmeta(newSubject.getNazivPredmeta()) != null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet je već dodat!"), HttpStatus.FORBIDDEN);
		SubjectEntity subject = subjectService.dodajPredmet(newSubject);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je dodao predmet " + newSubject.getNazivPredmeta() + ".");
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" \n"));
	}

	/* REST endpoint za izmenu postojeceg predmeta */

	@PutMapping(path = "izmenaPredmeta/{predmetId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> izmenaPredmeta(@Validated(PutValidation.class) @RequestBody SubjectEntity noviPredmet,
			@PathVariable Integer predmetId) {
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen!"), HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je izmenio predmet " + noviPredmet.getNazivPredmeta() + ".");
		return new ResponseEntity<>(subjectService.izmenaPredmeta(noviPredmet, predmetId), HttpStatus.OK);
	}

	/* REST endpoint za brisanje pojedinacnog predmeta */

	@DeleteMapping(path = "/{predmetId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjePredmeta(@PathVariable Integer predmetId) {
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen!"), HttpStatus.NOT_FOUND);
		if (subjectRepository.findById(predmetId).get().getPredmeti_odeljenja().size() > 0)
			return new ResponseEntity<RESTError>(
					new RESTError(2, "Predmet koji se sluša u nekom odeljenju ne može biti obrisan!"),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je izbrisao predmet "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + ".");
		return new ResponseEntity<SubjectEntity>(subjectService.brisanjePredmeta(predmetId), HttpStatus.OK);

	}

	/* REST endpoint za dodavanje predmeta u razred */

	@PutMapping(path = "dodajPredmetURazred/{predmetId}/razred/{razredId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajPredmetURazred(@PathVariable Integer predmetId, @PathVariable Integer razredId) {
		if (!subjectRepository.existsById(predmetId)) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen."), HttpStatus.NOT_FOUND);
		}
		if (!classYearRepository.existsById(razredId)) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Razred nije pronađen."), HttpStatus.NOT_FOUND);
		}
		if (subjectService.predmetSeSlusaURazredu(predmetId, razredId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet je već dodat u razred!"),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je dodao predmet "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + " u razred "
				+ classYearRepository.findById(razredId).get().getNaziv() + ".");
		return new ResponseEntity<SubjectEntity>(subjectService.dodajPredmetURazred(predmetId, razredId),
				HttpStatus.OK);
	}

	/* REST endpoint za brisanje predmeta iz razreda */

	@PutMapping(path = "brisanjePredmetaIzRazreda/{predmetId}/razred/{razredId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjePredmetaIzRazreda(@PathVariable Integer predmetId,
			@PathVariable Integer razredId) {
		if (!subjectRepository.existsById(predmetId)) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen."), HttpStatus.NOT_FOUND);
		}
		if (!classYearRepository.existsById(razredId)) {
			return new ResponseEntity<RESTError>(new RESTError(1, "Razred nije pronađen."), HttpStatus.NOT_FOUND);
		}
		ClassYearEntity razred = classYearRepository.findById(razredId).get();
		SubjectEntity predmet = subjectRepository.findById(predmetId).get();
		if (!razred.getPredmeti().contains(predmet))
			return new ResponseEntity<RESTError>(new RESTError(1, "Razred ne sadrži ovaj predmet!"),
					HttpStatus.NOT_FOUND);
		{
			if (subjectService.predmetSeSlusaUOdeljenjuRazreda(predmetId, razredId))
				return new ResponseEntity<RESTError>(
						new RESTError(2, "Predmet koji se sluša u nekom odeljenju ne može biti izbrisan iz razreda!"),
						HttpStatus.FORBIDDEN);
		}
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je izbrisao predmet "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + " iz razreda "
				+ classYearRepository.findById(razredId).get().getNaziv() + ".");
		return new ResponseEntity<SubjectEntity>(predmet, HttpStatus.OK);
	}

	/* REST endpoint za dodavanje predmeta i nastavnika odeljenju */

	@PostMapping(path = "/dodajPredmetOdeljenju/predmet/{predmetId}/odeljenje/{odeljenjeId}/nastavnik/{nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajPredmetOdeljenju(@PathVariable Integer predmetId, @PathVariable Integer odeljenjeId,
			@PathVariable Integer nastavnikId) {
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen!"), HttpStatus.NOT_FOUND);
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.NOT_FOUND);
		if (subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId) != null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet je već dodat ovom odeljenju!"),
					HttpStatus.FORBIDDEN);
		if (!teacherRepository.existsById(nastavnikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik nije pronađen!"), HttpStatus.NOT_FOUND);
		TeacherEntity razredni = teacherRepository.findById(nastavnikId).get();
		if (!teacherService.nastavnikPredajePredmet(predmetId, razredni))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik ne predaje ovaj predmet."),
					HttpStatus.FORBIDDEN);
		if (!subjectService.predmetSeSlusaURazredu(predmetId,
				classYearRepository.findByOdeljenjaId(odeljenjeId).getId()))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet se ne sluša u ovom razredu!"),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je dodao predmet "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + " i nastavnika "
				+ razredni.getKorisnickoIme() + " u odeljenje "
				+ classRepository.findById(odeljenjeId).get().getRazred().getNaziv() + "-"
				+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + ".");
		return new ResponseEntity<>(subjectService.dodajPredmetOdeljenju(predmetId, odeljenjeId, nastavnikId),
				HttpStatus.CREATED);
	}

	@PutMapping(path = "/dodajNastavnikaPredmetuUOdeljenju/predmet/{predmetId}/odeljenje/{odeljenjeId}/nastavnik/{nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajNastavnikaPredmetuUOdeljenju(@PathVariable Integer predmetId,
			@PathVariable Integer odeljenjeId, @PathVariable Integer nastavnikId) {
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen!"), HttpStatus.NOT_FOUND);
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.NOT_FOUND);
		if (subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet ne postoji u odeljenju!"),
					HttpStatus.FORBIDDEN);
		if (!teacherRepository.existsById(nastavnikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik nije pronađen!"), HttpStatus.NOT_FOUND);
		TeacherEntity nastavnik = teacherRepository.findById(nastavnikId).get();
		if (!teacherService.nastavnikPredajePredmet(predmetId, nastavnik))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik ne predaje ovaj predmet."),
					HttpStatus.FORBIDDEN);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je predmetu "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + " iz odeljenja "
				+ classRepository.findById(odeljenjeId).get().getRazred() + "-"
				+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + " dodao nastavnika"
				+ nastavnik.getKorisnickoIme() + ".");
		return new ResponseEntity<>(
				subjectService.dodajNastavnikaPredmetuUOdeljenju(predmetId, odeljenjeId, nastavnikId),
				HttpStatus.CREATED);
	}

	@PutMapping(path = "/brisanjeNastavnikaPredmetuUOdeljenju/predmet/{predmetId}/odeljenje/{odeljenjeId}/nastavnik/{nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjeNastavnikPredmetuUOdeljenju(@PathVariable Integer predmetId,
			@PathVariable Integer odeljenjeId, @PathVariable Integer nastavnikId) {
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen!"), HttpStatus.NOT_FOUND);
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.NOT_FOUND);
		if (subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet se ne sluša u ovom odeljenju!"),
					HttpStatus.FORBIDDEN);
		TeacherEntity nastavnik = teacherRepository.findById(nastavnikId).get();
		if (!teacherService.nastavnikPredajePredmetOdeljenju(predmetId, odeljenjeId, nastavnik))
			return new ResponseEntity<RESTError>(new RESTError(1, "Nastavnik ne predaje taj predmet odeljenju!"),
					HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je predmetu "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + " iz odeljenja "
				+ classYearRepository.findByOdeljenjaId(odeljenjeId).getNaziv() + "-"
				+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + " obrisao nastavnika"
				+ nastavnik.getKorisnickoIme() + ".");
		return new ResponseEntity<SubjectClassEntity>(
				subjectService.brisanjeNastavnikPredmetuUOdeljenju(predmetId, odeljenjeId, nastavnikId), HttpStatus.OK);

	}

	/* REST endpoint za brisanje predmeta iz odeljenja */

	@DeleteMapping(path = "brisanjePredmetaIzOdeljenja/predmet/{predmetId}/odeljenje/{odeljenjeId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjePredmetaIzOdeljenja(@PathVariable Integer predmetId,
			@PathVariable Integer odeljenjeId) {
		if (!subjectRepository.existsById(predmetId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet nije pronađen!"), HttpStatus.FOUND);
		if (!classRepository.existsById(odeljenjeId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Odeljenje nije pronađeno!"), HttpStatus.FOUND);
		if (subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId) == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Predmet se ne sluša u ovom odeljenju!"),
					HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administator " + korisnickoIme + " je predmet "
				+ subjectRepository.findById(predmetId).get().getNazivPredmeta() + " obrisao iz odeljenja "
				+ classRepository.findById(odeljenjeId).get().getRazred() + "-"
				+ classRepository.findById(odeljenjeId).get().getBrojOdeljenja() + ".");
		return new ResponseEntity<>(subjectService.brisanjePredmetaIzOdeljenja(predmetId, odeljenjeId), HttpStatus.OK);
	}

	/* REST metoda za prikazivanje svih predmeta koje predaje jedan nastavnik */

	@GetMapping(path = "/predmetiNastavnika/{nastavnikId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> predmetiKojePredajeNastavnik(@PathVariable Integer nastavnikId) {
		return new ResponseEntity<>(subjectRepository.findByNastavniciId(nastavnikId), HttpStatus.OK);
	}

}
