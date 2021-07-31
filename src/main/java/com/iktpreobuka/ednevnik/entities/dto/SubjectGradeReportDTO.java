package com.iktpreobuka.ednevnik.entities.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

public class SubjectGradeReportDTO {

	@JsonView(Views.Private.class)
	private String nazivPredmeta;

	@JsonView(Views.Private.class)
	private List<SubjectGradeItem> izvestaji;

	@JsonView(Views.Public.class)
	private String prosekIzPredmeta;

	public SubjectGradeReportDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getNazivPredmeta() {
		return nazivPredmeta;
	}

	public void setNazivPredmeta(String nazivPredmeta) {
		this.nazivPredmeta = nazivPredmeta;
	}

	public List<SubjectGradeItem> getIzvestaji() {
		return izvestaji;
	}

	public void setIzvestaji(List<SubjectGradeItem> izvestaji) {
		this.izvestaji = izvestaji;
	}

	public String getProsekIzPredmeta() {
		return prosekIzPredmeta;
	}

	public void setProsekIzPredmeta(String prosekIzPredmeta) {
		this.prosekIzPredmeta = prosekIzPredmeta;
	}

}
