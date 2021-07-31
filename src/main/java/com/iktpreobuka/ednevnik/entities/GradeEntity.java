package com.iktpreobuka.ednevnik.entities;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.ednevnik.security.Views;

@Entity
@Table(name = "ocena")
public class GradeEntity {

	public enum VrstaOcene {
		PISMENI_ZADATAK, KONTROLNI_ZADATAK, USMENO_ODGOVARANJE, ZAKLJUCNA_OCENA, OSTALO,
	}

	public enum Polugodiste {
		PRVO_POLUGODISTE, DRUGO_POLUGODISTE
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView(Views.Private.class)
	private Integer id;

	@Column(name = "Ocena")
	@JsonView(Views.Public.class)
	private Integer ocena;

	@Column(name = "datum_ocenjivanja")
	@JsonFormat(pattern = "dd-MM-yyyy")
	@JsonView(Views.Public.class)
	private LocalDate datumOcenjivanja;

	@Column(name = "vrsta_ocene")
	@JsonView(Views.Public.class)
	private VrstaOcene vrsta;

	@Column(name = "polugodiste")
	@JsonView(Views.Public.class)
	private Polugodiste polugodiste;

	@Column(name = "zakljucnaOcena")
	@JsonView(Views.Admin.class)
	private Boolean zakljucnaOcena;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "predmetUOdeljenju")
	@JsonView(Views.Public.class)
	private SubjectClassEntity predmetUOdeljenju;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "ucenik")
	@JsonView(Views.Public.class)
	private StudentEntity ucenik;

	public GradeEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public VrstaOcene getVrsta() {
		return vrsta;
	}

	public void setVrsta(VrstaOcene vrsta) {
		this.vrsta = vrsta;
	}

	public Polugodiste getPolugodiste() {
		return polugodiste;
	}

	public SubjectClassEntity getPredmet_odeljenje() {
		return predmetUOdeljenju;
	}

	public void setPredmet_odeljenje(SubjectClassEntity predmet_odeljenje) {
		this.predmetUOdeljenju = predmet_odeljenje;
	}

	public void setPolugodiste(Polugodiste polugodiste) {
		this.polugodiste = polugodiste;
	}

	public StudentEntity getUcenik() {
		return ucenik;
	}

	public void setUcenik(StudentEntity ucenik) {
		this.ucenik = ucenik;
	}

	public Boolean getZakljucnaOcena() {
		return zakljucnaOcena;
	}

	public void setZakljucnaOcena(Boolean zakljucnaOcena) {
		this.zakljucnaOcena = zakljucnaOcena;
	}

}
