package com.iktpreobuka.ednevnik.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

@Entity
@Table(name = "roditelj")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ParentEntity extends UserEntity {

	@Column(name = "ime_roditelja")
	@JsonView(Views.Public.class)
	private String imeRoditelja;

	@Column(name = "prezime_roditelja")
	@JsonView(Views.Public.class)
	private String prezimeRoditelja;

	@Column(name = "Email_adresa")
	@JsonView(Views.Private.class)
	private String email;

	@Column(name = "broj_telefona")
	@JsonView(Views.Private.class)
	private String brojTelefona;

	@OneToMany(mappedBy = "roditelj", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonIgnore
	@JsonView(Views.Private.class)
	private List<StudentEntity> deca;

	public ParentEntity() {
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

	@JsonIgnore
	public List<StudentEntity> getDeca() {
		return deca;
	}

	@JsonIgnore
	public void setDeca(List<StudentEntity> deca) {
		this.deca = deca;
	}

}
