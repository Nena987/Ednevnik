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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

@Entity
@Table(name = "predmet_odeljenje")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class SubjectClassEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView(Views.Admin.class)
	private Integer id;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "predmet")
	@JsonView(Views.Private.class)
	private SubjectEntity predmet;

	@NotBlank
	@JsonView(Views.Public.class)
	private String name;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "odeljenje")
	@JsonView(Views.Private.class)
	@JsonIgnore
	private ClassEntity odeljenje;

	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(name = "nastavnikPredmetaUOdeljenju", joinColumns = @JoinColumn(name = "predmetUOdeljenjuId"), inverseJoinColumns = @JoinColumn(name = "nastavnikId"))
	@JsonView(Views.Public.class)
	@JsonIgnore
	private Set<TeacherEntity> nastavnici = new HashSet<TeacherEntity>();

	@OneToMany(mappedBy = "predmetUOdeljenju", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonIgnore
	@JsonView(Views.Private.class)
	private List<GradeEntity> ocene = new ArrayList<GradeEntity>();

	public SubjectClassEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SubjectEntity getPredmet() {
		return predmet;
	}

	public void setPredmet(SubjectEntity predmet) {
		this.predmet = predmet;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public ClassEntity getOdeljenje() {
		return odeljenje;
	}

	@JsonIgnore
	public void setOdeljenje(ClassEntity odeljenje) {
		this.odeljenje = odeljenje;
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
	public List<GradeEntity> getOcene() {
		return ocene;
	}

	@JsonIgnore
	public void setOcene(List<GradeEntity> ocene) {
		this.ocene = ocene;
	}

}
