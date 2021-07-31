package com.iktpreobuka.ednevnik.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

@Entity
@Table(name = "odeljenje")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ClassEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView(Views.Admin.class)
	private Integer id;

	@JsonView(Views.Public.class)
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "razred")
	private ClassYearEntity razred;

	@NotNull(groups = PostValidation.class)
	@Min(value = 1, groups = { PostValidation.class, PutValidation.class })
	@JsonView(Views.Public.class)
	private Integer brojOdeljenja;

	@OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "razredniStaresina")
	@JsonView(Views.Public.class)
	private TeacherEntity razredniStaresina;

	@OneToMany(mappedBy = "odeljenje", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonIgnore
	@JsonView(Views.Public.class)
	private List<StudentEntity> ucenici;

	@OneToMany(mappedBy = "odeljenje", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonIgnore
	@JsonView(Views.Public.class)
	private List<SubjectClassEntity> predmetiUOdeljenju;

	public ClassEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBrojOdeljenja() {
		return brojOdeljenja;
	}

	public void setBrojOdeljenja(Integer brojOdeljenja) {
		this.brojOdeljenja = brojOdeljenja;
	}

	public ClassYearEntity getRazred() {
		return razred;
	}

	public void setRazred(ClassYearEntity razred) {
		this.razred = razred;
	}

	public TeacherEntity getRazredniStaresina() {
		return razredniStaresina;
	}

	public void setRazredniStaresina(TeacherEntity razredniStaresina) {
		this.razredniStaresina = razredniStaresina;
	}

	@JsonIgnore
	public List<StudentEntity> getUcenici() {
		return ucenici;
	}

	@JsonIgnore
	public void setUcenici(List<StudentEntity> ucenici) {
		this.ucenici = ucenici;
	}

	@JsonIgnore
	public List<SubjectClassEntity> getPredmetiUOdeljenju() {
		return predmetiUOdeljenju;
	}

	@JsonIgnore
	public void setPredmetiUOdeljenju(List<SubjectClassEntity> predmetiUOdeljenju) {
		this.predmetiUOdeljenju = predmetiUOdeljenju;
	}

}
