package com.iktpreobuka.ednevnik.services;

import java.io.ByteArrayInputStream;

import com.iktpreobuka.ednevnik.entities.GradeEntity;
import com.iktpreobuka.ednevnik.entities.GradeEntity.Polugodiste;
import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.entities.dto.GradeDTO;
import com.iktpreobuka.ednevnik.entities.dto.StudentGradeReportDTO;
import com.iktpreobuka.ednevnik.entities.dto.SubjectGradeReportDTO;

public interface GradeService {

	public Boolean zakljucenaOcena(SubjectClassEntity predmetUOdeljenju, Integer ucenikId, String polugodiste);

	public GradeEntity dodajOcenuUceniku(Integer ucenikId, Integer predmetId, GradeDTO newGrade) throws Exception;

	public GradeEntity izmenaOcene(Integer ocenaId, GradeDTO novaOcena);

	public StudentGradeReportDTO prikaziSveOceneUcenika(StudentEntity ucenik, Polugodiste polugodiste);

	public SubjectGradeReportDTO pregledOcenaIzPredmetaZaOdeljenje(Integer predmetId, Integer odeljenjeId,
			Polugodiste polugodiste);

	public Double prosecnaOcenaIzPredmeta(Integer predmetId, Integer ucenikId, Polugodiste polugodiste);

	public GradeEntity zakljuciOcenuUceniku(Integer ucenikId, Integer predmetId, GradeDTO zakljucnaOcena)
			throws Exception;

	public String pronadjiZakljucnuOcenu(Integer predmetId, Integer ucenikId, Polugodiste polugodiste);

	public String izracunajUspeh(Integer ucenikId, Polugodiste polugodiste);

	public String prosekIzPredmeta(Integer predmetId, Integer odeljenjeId, Polugodiste polugodiste);

	public ByteArrayInputStream ocenePDF(Integer odeljenjeId);
}
