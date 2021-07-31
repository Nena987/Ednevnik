package com.iktpreobuka.ednevnik.entities.dto;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iktpreobuka.ednevnik.utils.PostValidation;
import com.iktpreobuka.ednevnik.utils.PutValidation;

public class GradeDTO {

	@NotNull(groups = PostValidation.class)
	@Min(value = 1, message = "Ocena ne sme biti manja od 1!", groups = { PostValidation.class, PutValidation.class })
	@Max(value = 5, message = "Ocena ne sme biti veća od 5!", groups = { PostValidation.class, PutValidation.class })
	private Integer ocena;

	@Column(name = "datum_ocenjivanja")
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate datumOcenjivanja;

	@NotNull(message = "Morate uneti vrstu ocene!", groups = PostValidation.class)
	@Column(name = "vrsta_ocene")
	private String vrsta;

	@NotNull(message = "Morate uneti polugodište!", groups = PostValidation.class)
	@Column(name = "polugodiste")
	private String polugodiste;

	private Boolean zakljucnaOcena;

	public GradeDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getOcena() {
		return ocena;
	}

	public void setOcena(Integer ocena) {
		this.ocena = ocena;
	}

	public LocalDate getDatumOcenjivanja() {
		return datumOcenjivanja;
	}

	public void setDatumOcenjivanja(LocalDate datumOcenjivanja) {
		this.datumOcenjivanja = datumOcenjivanja;
	}

	public String getVrsta() {
		return vrsta;
	}

	public void setVrsta(String vrsta) {
		this.vrsta = vrsta;
	}

	public String getPolugodiste() {
		return polugodiste;
	}

	public void setPolugodiste(String polugodiste) {
		this.polugodiste = polugodiste;
	}

	public Boolean getZakljucnaOcena() {
		return zakljucnaOcena;
	}

	public void setZakljucnaOcena(Boolean zakljucnaOcena) {
		this.zakljucnaOcena = zakljucnaOcena;
	}

	
	
}
