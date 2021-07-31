package com.iktpreobuka.ednevnik.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

@Entity
@Table(name = "nastavnik")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TeacherEntity extends UserEntity {

	@JsonView(Views.Public.class)
	private String ime;

	@JsonView(Views.Public.class)
	private String prezime;

	@OneToOne(mappedBy = "razredniStaresina", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonIgnore
	@JsonView(Views.Public.class)
	private ClassEntity odeljenjeRazrednog;

	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(name = "nastavnikPredmeta", joinColumns = @JoinColumn(name = "nastavnikId"), inverseJoinColumns = @JoinColumn(name = "predmetId"))
	@JsonIgnore
	@JsonView(Views.Public.class)
	private Set<SubjectEntity> predmeti = new HashSet<SubjectEntity>();

	@ManyToMany(mappedBy = "nastavnici", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonView(Views.Public.class)
	@JsonIgnore
	private Set<SubjectClassEntity> predmetiUOdeljenju = new HashSet<SubjectClassEntity>();

	public TeacherEntity() {
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

	@JsonIgnore
	public ClassEntity getOdeljenjeRazrednog() {
		return odeljenjeRazrednog;
	}

	@JsonIgnore
	public void setOdeljenjeRazrednog(ClassEntity odeljenjeRazrednog) {
		this.odeljenjeRazrednog = odeljenjeRazrednog;
	}

	@JsonIgnore
	public Set<SubjectEntity> getPredmeti() {
		return predmeti;
	}

	@JsonIgnore
	public void setPredmeti(Set<SubjectEntity> predmeti) {
		this.predmeti = predmeti;
	}

	@JsonIgnore
	public Set<SubjectClassEntity> getPredmeti_odeljenja() {
		return predmetiUOdeljenju;
	}

	@JsonIgnore
	public void setPredmeti_odeljenja(Set<SubjectClassEntity> predmeti_odeljenja) {
		this.predmetiUOdeljenju = predmeti_odeljenja;
	}
}
