package com.iktpreobuka.ednevnik.entities.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

public class StudentGradeItem {

	@JsonView(Views.Public.class)
	private String nazivPredmeta;

	@JsonView(Views.Public.class)
	private List<Integer> ocene;

	@JsonView(Views.Public.class)
	private Double prosecnaOcena;
	
	@JsonView (Views.Public.class)
	private String zakljucnaOcena;

	public StudentGradeItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getNazivPredmeta() {
		return nazivPredmeta;
	}

	public void setNazivPredmeta(String nazivPredmeta) {
		this.nazivPredmeta = nazivPredmeta;
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
