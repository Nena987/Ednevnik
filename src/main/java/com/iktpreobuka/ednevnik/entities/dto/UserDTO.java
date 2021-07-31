package com.iktpreobuka.ednevnik.entities.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

public class UserDTO {

	@NotBlank(message = "Korisničko ime ne sme ostati prazno!", groups = PostValidation.class)
	@Size(min = 5, max = 10, message = "Korisničko ime mora imati između 5 i 10 karaktera.", groups = {
			PostValidation.class, PutValidation.class })
	private String korisnickoIme;

	@NotBlank(message = "Šifra ne sme ostati prazna!", groups = PostValidation.class)
	@Size(min = 5, max = 100, message = "Šifra mora imati između 5 i 100 karaktera.", groups = { PostValidation.class,
			PutValidation.class })
	@Pattern(regexp = "[a-zA-Z0-9]+", message = "Lozinka nije validna.", groups = { PostValidation.class,
			PutValidation.class })
	private String lozinka;

	@NotBlank(message = "Šifra ne sme ostati prazna!", groups = PostValidation.class)
	@Size(min = 5, max = 100, message = "Šifra mora imati između 5 i 100 karaktera.", groups = { PostValidation.class,
			PutValidation.class })
	@Pattern(regexp = "[a-zA-Z0-9]+", message = "Lozinka nije validna.", groups = { PostValidation.class,
			PutValidation.class })
	private String ponovljenaLozinka;

	public UserDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getKorisnickoIme() {
		return korisnickoIme;
	}

	public void setKorisnickoIme(String korisnickoIme) {
		this.korisnickoIme = korisnickoIme;
	}

	public String getLozinka() {
		return lozinka;
	}

	public void setLozinka(String lozinka) {
		this.lozinka = lozinka;
	}

	public String getPonovljenaLozinka() {
		return ponovljenaLozinka;
	}

	public void setPonovljenaLozinka(String ponovljenaLozinka) {
		this.ponovljenaLozinka = ponovljenaLozinka;
	}

}
