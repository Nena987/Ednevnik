package com.iktpreobuka.ednevnik.entities.dto;

import javax.validation.constraints.NotNull;

import com.iktpreobuka.ednevnik.utils.PostValidation;

public class RoleDTO {

	@NotNull(groups = PostValidation.class)
	public String nazivRole;

	public RoleDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getNazivRole() {
		return nazivRole;
	}

	public void setNazivRole(String nazivRole) {
		this.nazivRole = nazivRole;
	}

}
