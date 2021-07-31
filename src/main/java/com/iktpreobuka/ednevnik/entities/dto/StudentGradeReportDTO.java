package com.iktpreobuka.ednevnik.entities.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

public class StudentGradeReportDTO {

	@JsonView(Views.Public.class)
	private String imeUcenika;

	@JsonView(Views.Public.class)
	private List<StudentGradeItem> izvestaji;

	@JsonView(Views.Public.class)
	private String uspeh;

	public StudentGradeReportDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getImeUcenika() {
		return imeUcenika;
	}

	public void setImeUcenika(String imeUcenika) {
		this.imeUcenika = imeUcenika;
	}

	public List<StudentGradeItem> getIzvestaji() {
		return izvestaji;
	}

	public void setIzvestaji(List<StudentGradeItem> izvestaji) {
		this.izvestaji = izvestaji;
	}

	public String getUspeh() {
		return uspeh;
	}

	public void setUspeh(String uspeh) {
		this.uspeh = uspeh;
	}

}
