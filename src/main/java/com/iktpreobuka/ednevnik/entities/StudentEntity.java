package com.iktpreobuka.ednevnik.entities;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

@Entity
@Table(name = "ucenik")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class StudentEntity extends UserEntity {

	@Column(name = "ime_učenika")
	@JsonView(Views.Public.class)
	private String imeUcenika;

	@Column(name = "prezime_učenika")
	@JsonView(Views.Public.class)
	private String prezimeUcenika;

	@JsonFormat(pattern = "dd-MM-yyyy")
	@Column(name = "datum_rođenja_učenika")
	@JsonView(Views.Private.class)
	private LocalDate datumRodjenja;

	@Column(name = "matični_broj_učenika")
	@JsonView(Views.Private.class)
	private String jmbg;

	@Column(name = "ulica_i_broj_adrese_učenika")
	@JsonView(Views.Private.class)
	private String ulicaIBroj;

	@Column(name = "grad_adrese_učenika")
	@JsonView(Views.Private.class)
	private String grad;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "roditelj")
	@JsonView(Views.Private.class)
	private ParentEntity roditelj;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "odeljenje")
	@JsonView(Views.Public.class)
	private ClassEntity odeljenje;

	@OneToMany(mappedBy = "ucenik", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonIgnore
	@JsonView(Views.Private.class)
	private List<GradeEntity> ocene;

	public StudentEntity() {
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

	public ParentEntity getRoditelj() {
		return roditelj;
	}

	public void setRoditelj(ParentEntity roditelj) {
		this.roditelj = roditelj;
	}

	public ClassEntity getOdeljenje() {
		return odeljenje;
	}

	public void setOdeljenje(ClassEntity odeljenje) {
		this.odeljenje = odeljenje;
	}

	@JsonIgnore
	public List<GradeEntity> getOcene() {
		return ocene;
	}

	@JsonIgnore
	public void setOcene(List<GradeEntity> ocene) {
		this.ocene = ocene;
	}

}
