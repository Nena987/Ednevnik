package com.iktpreobuka.ednevnik.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

@Entity
@Table(name = "rola")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class RoleEntity {

	public enum Rola {
		ROLE_ADMIN, ROLE_STUDENT, ROLE_PARENT, ROLE_TEACHER, ROLE_CLASSTEACHER
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "role_id")
	@JsonView(Views.Admin.class)
	private Integer id;

	@Column(name = "role_name")
	@JsonView(Views.Admin.class)
	private Rola name;

	@JsonIgnore
	@OneToMany(mappedBy = "role", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JsonView(Views.Admin.class)
	private List<UserEntity> korisnici = new ArrayList<>();

	public RoleEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Rola getName() {
		return name;
	}

	public void setName(Rola name) {
		this.name = name;
	}

	@JsonIgnore
	public List<UserEntity> getKorisnici() {
		return korisnici;
	}

	@JsonIgnore
	public void setKorisnici(List<UserEntity> korisnici) {
		this.korisnici = korisnici;
	}

}
