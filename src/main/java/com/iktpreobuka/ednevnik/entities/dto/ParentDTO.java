package com.iktpreobuka.ednevnik.entities.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

public class ParentDTO extends UserDTO {

	@NotBlank(message = "Ime roditelja ne sme biti prazno!", groups = PostValidation.class)
	private String imeRoditelja;

	@NotBlank(message = "Prezime roditelja ne sme biti prazno!", groups = PostValidation.class)
	private String prezimeRoditelja;

	@NotBlank(message = "Email adresa ne sme biti prazna!", groups = PostValidation.class)
	@Pattern(regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "Email adresa nije validna.", groups = {
					PostValidation.class, PutValidation.class })
	private String email;

	@NotBlank(message = "Broj telefona mora biti upisan!", groups = PostValidation.class)
	private String brojTelefona;

	public ParentDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getImeRoditelja() {
		return imeRoditelja;
	}

	public void setImeRoditelja(String imeRoditelja) {
		this.imeRoditelja = imeRoditelja;
	}

	public String getPrezimeRoditelja() {
		return prezimeRoditelja;
	}

	public void setPrezimeRoditelja(String prezimeRoditelja) {
		this.prezimeRoditelja = prezimeRoditelja;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBrojTelefona() {
		return brojTelefona;
	}

	public void setBrojTelefona(String brojTelefona) {
		this.brojTelefona = brojTelefona;
	}

}
