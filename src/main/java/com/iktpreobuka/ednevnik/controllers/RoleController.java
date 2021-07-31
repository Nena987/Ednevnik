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
import com.iktpreobuka.ednevnik.entities.RoleEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity.Rola;
import com.iktpreobuka.ednevnik.entities.UserEntity;
import com.iktpreobuka.ednevnik.entities.dto.RoleDTO;
import com.iktpreobuka.ednevnik.repositories.RoleRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;
import com.iktpreobuka.ednevnik.utils.RolaValidator;

@RestController
@RequestMapping(path = "/ednevnik/role")
public class RoleController {

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	RolaValidator rolaValidator;

	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		binder.addValidators(rolaValidator);
	}

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	/* REST endpoint za prikazivanje svih rola */

	@GetMapping(path = "/sveRole")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> sveRole() {
		if (roleRepository.findAll() == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nijedna rola nije pronađena!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(roleRepository.findAll(), HttpStatus.OK);
	}

	/* REST endpoint za pronazenje role po ID-u */

	@GetMapping(path = "/{rolaId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> pronadjiRolu(@PathVariable Integer rolaId) {
		if (!roleRepository.existsById(rolaId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Rola nije pronađena!"), HttpStatus.NOT_FOUND);
		return new ResponseEntity<RoleEntity>(roleRepository.findById(rolaId).get(), HttpStatus.OK);
	}

	/* REST endpoint za kreiranje nove role */

	@PostMapping(path = "/")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajRolu(@Validated(PostValidation.class) @RequestBody RoleDTO novaRola,
			BindingResult result) {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		if (roleRepository.findByName(Rola.valueOf(novaRola.getNazivRole())) != null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Rola je već napravljena!"), HttpStatus.FORBIDDEN);
		RoleEntity rola = new RoleEntity();
		rola.setName(Rola.valueOf(novaRola.getNazivRole()));
		roleRepository.save(rola);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je dodao rolu " + rola.getName() + ".");
		return new ResponseEntity<RoleEntity>(rola, HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" \n"));
	}

	/* REST endpoint za izmenu role */

	@PutMapping(path = "/izmenaRole/{rolaId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> izmeniRolu(@PathVariable Integer rolaId,
			@Validated(PutValidation.class) @RequestBody RoleDTO novaRola, BindingResult result) {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		if (!roleRepository.existsById(rolaId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Rola nije pronađena!"), HttpStatus.NOT_FOUND);
		RoleEntity rola = roleRepository.findById(rolaId).get();
		if (novaRola.getNazivRole() != null)
			rola.setName(Rola.valueOf(novaRola.getNazivRole()));
		roleRepository.save(rola);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je promenio rolu " + rola.getName() + ".");
		return new ResponseEntity<RoleEntity>(rola, HttpStatus.OK);
	}

	/* REST endpoint za brisanje role */

	@DeleteMapping(path = "/brisanjeRole/{rolaId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> brisanjeRole(@PathVariable Integer rolaId) {
		if (!roleRepository.existsById(rolaId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Rola nije pronađena!"), HttpStatus.NOT_FOUND);
		RoleEntity rola = roleRepository.findById(rolaId).get();
		for (UserEntity korisnik : rola.getKorisnici())
			korisnik.setRole(null);
		roleRepository.delete(rola);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je obrisao rolu " + rola.getName() + ".");
		return new ResponseEntity<RoleEntity>(rola, HttpStatus.OK);
	}

}
