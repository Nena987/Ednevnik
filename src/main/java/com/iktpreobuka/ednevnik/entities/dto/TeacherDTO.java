package com.iktpreobuka.ednevnik.entities.dto;

import javax.validation.constraints.NotBlank;

import com.iktpreobuka.ednevnik.utils.PostValidation;

public class TeacherDTO extends UserDTO {

	@NotBlank(message = "Ime nastavnika ne sme biti prazno!", groups = PostValidation.class)
	private String ime;

	@NotBlank(message = "Prezime nastavnika ne sme biti prazno!", groups = PostValidation.class)
	private String prezime;

	public TeacherDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getIme() {
		return ime;
	}

	public void setIme(String ime) {
		this.ime = ime;
	}

	public String getPrezime() {
		return prezime;
	}

	public void setPrezime(String prezime) {
		this.prezime = prezime;
	}

}
