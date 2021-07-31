package com.iktpreobuka.ednevnik.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.utils.PostValidation;

@Entity
@Table(name = "predmet")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class SubjectEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView(Views.Admin.class)
	private Integer id;

	@NotBlank(message = "Naziv predmeta ne sme ostati prazan!", groups = PostValidation.class)
	@JsonView(Views.Public.class)
	private String nazivPredmeta;

	@NotNull(groups = PostValidation.class)
	@JsonView(Views.Public.class)
	private Integer nedeljniFondCasova;

	@ManyToMany(mappedBy = "predmeti", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonView(Views.Public.class)
	@JsonIgnore
	private Set<TeacherEntity> nastavnici = new HashSet<TeacherEntity>();

	@ManyToMany(mappedBy = "predmeti", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonView(Views.Public.class)
	@JsonIgnore
	private Set<ClassYearEntity> razredi = new HashSet<>();

	@OneToMany(mappedBy = "predmet", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonView(Views.Public.class)
	@JsonIgnore
	private List<SubjectClassEntity> predmetiUOdeljenju = new ArrayList<SubjectClassEntity>();

	public SubjectEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNazivPredmeta() {
		return nazivPredmeta;
	}

	public void setNazivPredmeta(String nazivPredmeta) {
		this.nazivPredmeta = nazivPredmeta;
	}

	public Integer getNedeljniFondCasova() {
		return nedeljniFondCasova;
	}

	public void setNedeljniFondCasova(Integer nedeljniFondCasova) {
		this.nedeljniFondCasova = nedeljniFondCasova;
	}

	@JsonIgnore
	public List<SubjectClassEntity> getPredmeti_odeljenja() {
		return predmetiUOdeljenju;
	}

	@JsonIgnore
	public void setPredmeti_odeljenja(List<SubjectClassEntity> predmeti_odeljenja) {
		this.predmetiUOdeljenju = predmeti_odeljenja;
	}

	@JsonIgnore
	public Set<TeacherEntity> getNastavnici() {
		return nastavnici;
	}

	@JsonIgnore
	public void setNastavnici(Set<TeacherEntity> nastavnici) {
		this.nastavnici = nastavnici;
	}

	@JsonIgnore
	public Set<ClassYearEntity> getRazredi() {
		return razredi;
	}

	@JsonIgnore
	public void setRazredi(Set<ClassYearEntity> razredi) {
		this.razredi = razredi;
	}
}
