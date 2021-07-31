package com.iktpreobuka.ednevnik.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

@Entity
@Table(name = "razred")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ClassYearEntity {

	public enum ClassYear {
		I, II, III, IV, V, VI, VII, VIII
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView(Views.Admin.class)
	private Integer id;

	@Column(name = "naziv_razreda")
	@JsonView(Views.Public.class)
	private ClassYear naziv;

	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(name = "predmetiURazredu", joinColumns = @JoinColumn(name = "razred_id"), inverseJoinColumns = @JoinColumn(name = "predmet_id"))
	@JsonView(Views.Public.class)
	@JsonIgnore
	private Set<SubjectEntity> predmeti = new HashSet<>();

	@OneToMany(mappedBy = "razred", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonIgnore
	@JsonView(Views.Public.class)
	private List<ClassEntity> odeljenja = new ArrayList<>();

	public ClassYearEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ClassYear getNaziv() {
		return naziv;
	}

	public void setNaziv(ClassYear naziv) {
		this.naziv = naziv;
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
	public List<ClassEntity> getOdeljenja() {
		return odeljenja;
	}

	@JsonIgnore
	public void setOdeljenja(List<ClassEntity> odeljenja) {
		this.odeljenja = odeljenja;
	}
}
