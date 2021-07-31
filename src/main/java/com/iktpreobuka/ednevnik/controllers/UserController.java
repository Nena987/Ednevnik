package com.iktpreobuka.ednevnik.controllers;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
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
import com.iktpreobuka.ednevnik.entities.UserEntity;
import com.iktpreobuka.ednevnik.entities.dto.UserDTO;
import com.iktpreobuka.ednevnik.entities.dto.UserTokenDTO;
import com.iktpreobuka.ednevnik.repositories.RoleRepository;
import com.iktpreobuka.ednevnik.repositories.UserRepository;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.services.FileHandler;
import com.iktpreobuka.ednevnik.services.UserService;
import com.iktpreobuka.ednevnik.utils.Encryption;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.UserCustomValidator;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping(path = "/")
public class UserController {

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserCustomValidator userCustomValidator;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	UserService userService;

	@Autowired
	FileHandler fileHandler;

	@InitBinder
	protected void initBinder(final WebDataBinder binder) {
		binder.addValidators(userCustomValidator);
	}

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	@Value("${spring.security.secret-key}")
	private String secretKey;

	@Value("${spring.security.token-duration}")
	private Integer duration;

	/* REST endpoint za logovanje korisnika */

	@PostMapping(path = "/login")
	public ResponseEntity<?> login(@RequestParam String korisnickoIme, @RequestParam String lozinka) {
		UserEntity user = userRepository.findByKorisnickoIme(korisnickoIme);
		if (user != null && Encryption.validatePassword(lozinka, user.getLozinka())) {
			String token = getJWTToken(user);
			UserTokenDTO userDTO = new UserTokenDTO(user.getKorisnickoIme(), token);
			logger.info("Korisnik " + user.getKorisnickoIme() + " se ulogovao.");
			return new ResponseEntity<>(userDTO, HttpStatus.OK);
		}
		return new ResponseEntity<>("Pogrešno korisničko ime/lozinka!", HttpStatus.UNAUTHORIZED);

	}

	private String getJWTToken(UserEntity user) {
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList(user.getRole().getName().toString());
		String token = Jwts.builder().setId("softtekJWT").setSubject(user.getKorisnickoIme())
				.claim("authorities",
						grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + duration))
				.signWith(SignatureAlgorithm.HS512, secretKey).compact();
		return "Bearer " + token;
	}

	/*
	 * REST endpoint za kreiranje novog admina koja poziva metodu iz servisa.
	 * Pristup endpointu imaju samo korisnici koji su administratori
	 */

	@Secured("ROLE_ADMIN")
	@PostMapping(path = "/dodajAdmina")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> dodajNovogADmina(@Validated(PostValidation.class) @RequestBody UserDTO noviAdmin,
			BindingResult result) {
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else
			userCustomValidator.validate(noviAdmin, result);
		if (userRepository.findByKorisnickoIme(noviAdmin.getKorisnickoIme()) != null) {
			return new ResponseEntity<RESTError>(
					new RESTError(1, "Korisničko ime već postoji! Unesite novo korisničko ime."), HttpStatus.FORBIDDEN);
		}
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je dodao novog administratora sa korisničkim imenom "
				+ noviAdmin.getKorisnickoIme() + ".");
		return new ResponseEntity<UserEntity>(userService.createNewUser(noviAdmin), HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(" \n"));
	}

	/* REST endpoint za prikazivanje svih korisnika */

	@GetMapping(path = "/ednevnik/korisnici")
	public ResponseEntity<?> pronadjiSveKorisnike() {
		if (userRepository.findAll() == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nije pronađen nijedan korisnik!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
	}

	/* REST endpoint za prikazivanje svih admina */

	@GetMapping(path = "/ednevnik/SviAdmini")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> pronadjiSveAdmine() {
		if (userRepository.findByRoleAdmin() == null)
			return new ResponseEntity<RESTError>(new RESTError(1, "Nije pronađen nijedan administratror!"),
					HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(userRepository.findByRoleAdmin(), HttpStatus.OK);
	}

	/* REST endpoint za izmenu sifre admina */

	@PutMapping(path = "/ednevnik/izmenaSifre")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> izmeniSifru(@RequestParam String staraSifra, @RequestParam String novaSifra,
			@RequestParam String novaSifraPonovo) {
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		UserEntity admin = userRepository.findByKorisnickoIme(korisnickoIme);
		if (!Encryption.validatePassword(staraSifra, admin.getLozinka()))
			return new ResponseEntity<RESTError>(new RESTError(1, "Uneli ste pogrešnu staru šifru!"),
					HttpStatus.FORBIDDEN);
		if (!novaSifra.equals(novaSifraPonovo))
			return new ResponseEntity<RESTError>(new RESTError(1, "Šifre moraju biti iste!"), HttpStatus.FORBIDDEN);
		admin.setLozinka(Encryption.getPassEncoded(novaSifra));
		userRepository.save(admin);
		return new ResponseEntity<UserEntity>(admin, HttpStatus.OK);
	}

	/* REST endpoint za dodavanje i promenu role korisnika */

	@PutMapping(path = "/promenaRole/{korisnikId}/rola/{rolaId}")
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	public ResponseEntity<?> promenaRole(@PathVariable Integer korisnikId, @PathVariable Integer rolaId) {
		if (!userRepository.existsById(korisnikId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Korisnik nije pronađen!"), HttpStatus.NOT_FOUND);
		if (!roleRepository.existsById(rolaId))
			return new ResponseEntity<RESTError>(new RESTError(1, "Rola nije pronađena!"), HttpStatus.NOT_FOUND);
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		logger.info("Administrator " + korisnickoIme + " je promenio rolu korisniku "
				+ userRepository.findById(korisnikId).get().getKorisnickoIme() + ".");
		return new ResponseEntity<UserEntity>(userService.promenaRole(korisnikId, rolaId), HttpStatus.OK);
	}

	/* REST endpoint za download log datoteke */

	@GetMapping(path = "/downloadLogFile")
	@Secured("ROLE_ADMIN")
	public void downloadLogFile(HttpServletResponse response) throws Exception {
		fileHandler.downloadLogFile(response);
	}
}