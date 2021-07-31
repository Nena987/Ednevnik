package com.iktpreobuka.ednevnik.entities.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

public class SubjectGradeItem {

	@JsonView(Views.Private.class)
	private String imeUcenika;

	@JsonView(Views.Private.class)
	private List<Integer> ocene;

	@JsonView(Views.Public.class)
	private Double prosecnaOcena;

	@JsonView(Views.Public.class)
	private String zakljucnaOcena;

	public SubjectGradeItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getImeUcenika() {
		return imeUcenika;
	}

	public void setImeUcenika(String imeUcenika) {
		this.imeUcenika = imeUcenika;
	}

	public List<Integer> getOcene() {
		return ocene;
	}

	public void setOcene(List<Integer> ocene) {
		this.ocene = ocene;
	}

	public Double getProsecnaOcena() {
		return prosecnaOcena;
	}

	public void setProsecnaOcena(Double prosecnaOcena) {
		this.prosecnaOcena = prosecnaOcena;
	}

	public String getZakljucnaOcena() {
		return zakljucnaOcena;
	}

	public void setZakljucnaOcena(String zakljucnaOcena) {
		this.zakljucnaOcena = zakljucnaOcena;
	}

}
