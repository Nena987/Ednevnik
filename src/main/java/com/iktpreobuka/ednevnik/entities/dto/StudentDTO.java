package com.iktpreobuka.ednevnik.entities.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

public class StudentDTO extends UserDTO {

	@NotBlank(message = "Ime učenika ne sme biti prazno.", groups = PostValidation.class)
	private String imeUcenika;

	@NotBlank(message = "Prezime učenika ne sme biti prazno.", groups = PostValidation.class)
	private String prezimeUcenika;

	@JsonFormat(pattern = "dd-MM-yyyy")
	@NotNull(message = "Datum rođenja ne sme ostati prazan!", groups = PostValidation.class)
	private LocalDate datumRodjenja;

	@Size(min = 13, max = 13, message = "Matični broj mora imati tačno 13 cifara!", groups = { PostValidation.class,
			PutValidation.class })
	@Pattern(regexp = "[0-9]+", message = "Moraju biti brojevi", groups = { PostValidation.class, PutValidation.class })
	@NotBlank(message = "Matični broj učenika ne sme biti prazan!", groups = PostValidation.class)
	private String jmbg;

	@NotBlank(message = "Ulica i broj uČenika ne smeju ostati prazni!", groups = PostValidation.class)
	private String ulicaIBroj;

	@NotBlank(message = "Grad u kome učenik živi ne smee biti prazan!", groups = PostValidation.class)
	private String grad;

	public StudentDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getImeUcenika() {
		return imeUcenika;
	}

	public void setImeUcenika(String imeUcenika) {
		this.imeUcenika = imeUcenika;
	}

	public String getPrezimeUcenika() {
		return prezimeUcenika;
	}

	public void setPrezimeUcenika(String prezimeUcenika) {
		this.prezimeUcenika = prezimeUcenika;
	}

	public LocalDate getDatumRodjenja() {
		return datumRodjenja;
	}

	public void setDatumRodjenja(LocalDate datumRodjenja) {
		this.datumRodjenja = datumRodjenja;
	}

	public String getJMBG() {
		return jmbg;
	}

	public void setJMBG(String jMBG) {
		jmbg = jMBG;
	}

	public String getUlicaIBroj() {
		return ulicaIBroj;
	}

	public void setUlicaIBroj(String ulicaIBroj) {
		this.ulicaIBroj = ulicaIBroj;
	}

	public String getGrad() {
		return grad;
	}

	public void setGrad(String grad) {
		this.grad = grad;
	}

}
