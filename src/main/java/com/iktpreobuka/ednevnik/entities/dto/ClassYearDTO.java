package com.iktpreobuka.ednevnik.entities.dto;

import javax.validation.constraints.NotNull;

import com.iktpreobuka.ednevnik.utils.PostValidation;

public class ClassYearDTO {

	@NotNull(groups = PostValidation.class)
	private String nazivRazreda;

	public ClassYearDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getNazivRazreda() {
		return nazivRazreda;
	}

	public void setNazivRazreda(String nazivRazreda) {
		this.nazivRazreda = nazivRazreda;
	}

}
