package com.iktpreobuka.ednevnik.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.GradeEntity;
import com.iktpreobuka.ednevnik.entities.GradeEntity.Polugodiste;
import com.iktpreobuka.ednevnik.entities.GradeEntity.VrstaOcene;
import com.iktpreobuka.ednevnik.entities.dto.StudentGradeReportDTO;
import com.iktpreobuka.ednevnik.entities.dto.SubjectGradeItem;
import com.iktpreobuka.ednevnik.entities.dto.SubjectGradeReportDTO;
import com.iktpreobuka.ednevnik.entities.dto.GradeDTO;
import com.iktpreobuka.ednevnik.entities.dto.StudentGradeItem;
import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.SubjectClassEntity;
import com.iktpreobuka.ednevnik.repositories.ClassRepository;
import com.iktpreobuka.ednevnik.repositories.GradeRepository;
import com.iktpreobuka.ednevnik.repositories.StudentRepository;
import com.iktpreobuka.ednevnik.repositories.SubjectClassRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class GradeServiceImpl implements GradeService {

	@Autowired
	GradeRepository gradeRepository;

	@Autowired
	StudentRepository studentRepository;

	@Autowired
	SubjectClassRepository subjectClassRepository;

	@Autowired
	ClassRepository classRepository;

	@Autowired
	EmailService emailService;

	private static final Logger logger = LoggerFactory.getLogger(GradeService.class);

	/* Metoda za proveru da li postoji zakljucena ocena */

	public Boolean zakljucenaOcena(SubjectClassEntity predmetUOdeljenju, Integer ucenikId, String polugodiste) {
		List<GradeEntity> ocene = gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmetUOdeljenju, ucenikId);
		for (GradeEntity ocena : ocene) {
			if (ocena.getPolugodiste().equals(Polugodiste.valueOf(polugodiste)) && ocena.getZakljucnaOcena())
				return true;
		}
		return false;
	}

	/*
	 * Metoda za davanje ocene uceniku.
	 */

	public GradeEntity dodajOcenuUceniku(Integer ucenikId, Integer predmetId, GradeDTO newGrade) throws Exception {
		GradeEntity ocena = new GradeEntity();
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		SubjectClassEntity predmetUOdeljenju = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId,
				odeljenje.getId());
		ocena.setOcena(newGrade.getOcena());
		ocena.setDatumOcenjivanja(LocalDate.now());
		ocena.setVrsta(VrstaOcene.valueOf(newGrade.getVrsta()));
		ocena.setPolugodiste(Polugodiste.valueOf(newGrade.getPolugodiste()));
		ocena.setZakljucnaOcena(false);
		ocena.setUcenik(ucenik);
		ocena.setPredmet_odeljenje(predmetUOdeljenju);
		gradeRepository.save(ocena);
		return ocena;
	}

	/* Metoda za izmenu ocene */

	@Override
	public GradeEntity izmenaOcene(Integer ocenaId, GradeDTO novaOcena) {
		GradeEntity ocena = gradeRepository.findById(ocenaId).get();
		if (novaOcena.getOcena() != null)
			ocena.setOcena(novaOcena.getOcena());
		if (novaOcena.getVrsta() != null)
			ocena.setVrsta(VrstaOcene.valueOf(novaOcena.getVrsta()));
		gradeRepository.save(ocena);
		return ocena;
	}

	/* Metoda za davanje zakljucne ocene uceniku iz predmeta */

	@Override
	public GradeEntity zakljuciOcenuUceniku(Integer ucenikId, Integer predmetId, GradeDTO zakljucnaOcena)
			throws Exception {
		GradeEntity ocena = new GradeEntity();
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		SubjectClassEntity predmetUOdeljenju = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId,
				odeljenje.getId());
		ocena.setOcena(zakljucnaOcena.getOcena());
		ocena.setDatumOcenjivanja(LocalDate.now());
		ocena.setVrsta(VrstaOcene.ZAKLJUCNA_OCENA);
		ocena.setPolugodiste(Polugodiste.valueOf(zakljucnaOcena.getPolugodiste()));
		ocena.setZakljucnaOcena(true);
		ocena.setUcenik(ucenik);
		ocena.setPredmet_odeljenje(predmetUOdeljenju);
		gradeRepository.save(ocena);
		return ocena;
	}

	/*
	 * Metoda za prikazivanje svih ocena jednog ucenika. Metoda generise izvestaj
	 * koji izlistava sve ocene ucenika iz jednog predmeta.
	 */

	public StudentGradeReportDTO prikaziSveOceneUcenika(StudentEntity ucenik, Polugodiste polugodiste) {
		StudentGradeReportDTO izvestaji = new StudentGradeReportDTO();
		izvestaji.setImeUcenika(ucenik.getImeUcenika() + " " + ucenik.getPrezimeUcenika());
		izvestaji.setIzvestaji(new ArrayList<StudentGradeItem>());
		List<SubjectClassEntity> predmeti = ucenik.getOdeljenje().getPredmetiUOdeljenju();
		for (SubjectClassEntity predmet : predmeti) {
			StudentGradeItem izvestaj = new StudentGradeItem();
			List<GradeEntity> ocene = gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmet, ucenik.getId());
			izvestaj.setNazivPredmeta(predmet.getName());
			izvestaj.setOcene(new ArrayList<>());
			for (GradeEntity ocena : ocene) {
				if (ocena.getOcena() != null && !ocena.getZakljucnaOcena()
						&& ocena.getPolugodiste().equals(polugodiste))
					izvestaj.getOcene().add(ocena.getOcena());
			}
			izvestaj.setProsecnaOcena(
					prosecnaOcenaIzPredmeta(predmet.getPredmet().getId(), ucenik.getId(), polugodiste));
			izvestaj.setZakljucnaOcena(
					pronadjiZakljucnuOcenu(predmet.getPredmet().getId(), ucenik.getId(), polugodiste));
			izvestaji.getIzvestaji().add(izvestaj);
			izvestaji.setUspeh(izracunajUspeh(ucenik.getId(), polugodiste));
		}
		return izvestaji;
	}

	/*
	 * Metoda za prizivanje svih ocena iz jednog predmeta u jednom odeljenju. Metoda
	 * generice izvestaj koji prikazuje ocene za predmet za sve ucenike u jednom
	 * odeljenju.
	 */

	public SubjectGradeReportDTO pregledOcenaIzPredmetaZaOdeljenje(Integer predmetId, Integer odeljenjeId,
			Polugodiste polugodiste) {
		SubjectGradeReportDTO izvestaji = new SubjectGradeReportDTO();
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId);
		izvestaji.setNazivPredmeta(predmet.getName());
		izvestaji.setIzvestaji(new ArrayList<>());
		List<StudentEntity> ucenici = odeljenje.getUcenici();
		for (StudentEntity ucenik : ucenici) {
			SubjectGradeItem izvestaj = new SubjectGradeItem();
			List<GradeEntity> ocene = gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmet, ucenik.getId());
			izvestaj.setImeUcenika(ucenik.getImeUcenika() + " " + ucenik.getPrezimeUcenika());
			izvestaj.setOcene(new ArrayList<>());
			for (GradeEntity ocena : ocene) {
				if (ocena.getOcena() != null && !ocena.getZakljucnaOcena())
					izvestaj.getOcene().add(ocena.getOcena());
			}
			izvestaj.setProsecnaOcena(
					prosecnaOcenaIzPredmeta(predmet.getPredmet().getId(), ucenik.getId(), polugodiste));
			izvestaj.setZakljucnaOcena(
					pronadjiZakljucnuOcenu(predmet.getPredmet().getId(), ucenik.getId(), polugodiste));
			izvestaji.getIzvestaji().add(izvestaj);

		}
		izvestaji.setProsekIzPredmeta(prosekIzPredmeta(predmetId, odeljenjeId, polugodiste));
		return izvestaji;
	}

	/* Pomocna metoda za racunanje prosecne ocene iz predmeta za ucenika */

	public Double prosecnaOcenaIzPredmeta(Integer predmetId, Integer ucenikId, Polugodiste polugodiste) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenje.getId());
		List<GradeEntity> ocene = gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmet, ucenikId);
		int K = 0;
		Double suma = 0.00;
		Double prosecnaOcena = 0.00;
		for (GradeEntity ocena : ocene) {
			if (!ocena.getZakljucnaOcena() && ocena.getOcena() != null && ocena.getPolugodiste().equals(polugodiste)) {
				suma += ocena.getOcena();
				K++;
			}
		}
		prosecnaOcena = suma / K;
		return prosecnaOcena;
	}

	/* Metoda za pronalazenje zaklucne ocene iz predmeta */

	@Override
	public String pronadjiZakljucnuOcenu(Integer predmetId, Integer ucenikId, Polugodiste polugodiste) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenje.getId());
		List<GradeEntity> ocene = gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmet, ucenikId);
		String zakljucnaOcena = "Ocena nije zaključena.";
		for (GradeEntity ocena : ocene) {
			if (ocena.getZakljucnaOcena() && ocena.getOcena() != null && ocena.getPolugodiste().equals(polugodiste)) {
				zakljucnaOcena = ocena.getOcena().toString();
				return zakljucnaOcena;
			}
		}
		return zakljucnaOcena;
	}

	/* Metoda za racunanje trenutnog uspeha ucenika */

	@Override
	public String izracunajUspeh(Integer ucenikId, Polugodiste polugodiste) {
		StudentEntity ucenik = studentRepository.findById(ucenikId).get();
		ClassEntity odeljenje = ucenik.getOdeljenje();
		String uspeh = "Trenutni uspeh je: ";
		List<SubjectClassEntity> predmeti = odeljenje.getPredmetiUOdeljenju();
		Double sumaZakljucnihOcena = 0.00;
		int K = 0;
		for (SubjectClassEntity predmet : predmeti) {
			List<GradeEntity> ocene = gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmet, ucenikId);
			for (GradeEntity ocena : ocene) {
				if (ocena.getZakljucnaOcena() && ocena.getOcena() != null
						&& ocena.getPolugodiste().equals(polugodiste)) {
					sumaZakljucnihOcena += ocena.getOcena();
					K++;
				}
			}
		}
		if (K > 0)
			return uspeh += Math.round(sumaZakljucnihOcena * 100.0 / K) / 100.0;
		return "Nema zakljucenih ocena!";
	}

	/* Metoda za racunanje proseka iz jednog predmeta za odeljenje */

	@Override
	public String prosekIzPredmeta(Integer predmetId, Integer odeljenjeId, Polugodiste polugodiste) {
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		SubjectClassEntity predmet = subjectClassRepository.findByPredmetIdAndOdeljenjeId(predmetId, odeljenjeId);
		List<StudentEntity> ucenici = odeljenje.getUcenici();
		String prosek = "Prosek iz predmeta je: ";
		Double sumaOcena = 0.00;
		int K = 0;
		for (StudentEntity ucenik : ucenici) {
			List<GradeEntity> ocene = gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmet, ucenik.getId());
			for (GradeEntity ocena : ocene) {
				if (!ocena.getZakljucnaOcena() && ocena.getOcena() != null
						&& ocena.getPolugodiste().equals(polugodiste)) {
					sumaOcena += ocena.getOcena();
					K++;
				}
			}
		}
		if (K > 0)
			return prosek += Math.round(sumaOcena * 100.0 / K) / 100.0;
		return "Još uvek nema ocena iz predmeta";
	}

	/*
	 * Metoda za genersisanje PDF fajla sa svim ocenama za sve predmete iz jednog
	 * odeljenja
	 */

	public ByteArrayInputStream ocenePDF(Integer odeljenjeId) {
		ClassEntity odeljenje = classRepository.findById(odeljenjeId).get();
		List<StudentEntity> ucenici = odeljenje.getUcenici();
		List<SubjectClassEntity> predmeti = odeljenje.getPredmetiUOdeljenju();

		Document dokument = new Document(PageSize.A3);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			PdfPTable tabela = new PdfPTable(predmeti.size() + 1);
			tabela.setWidthPercentage(100);

			Font fontZaglavlja = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
			fontZaglavlja.setSize(12f);

			PdfPCell poljeZaglavlja;
			poljeZaglavlja = new PdfPCell(new Phrase("Naziv predmeta / Ime učenika", fontZaglavlja));
			poljeZaglavlja.setHorizontalAlignment(Element.ALIGN_CENTER);
			poljeZaglavlja.setVerticalAlignment(Element.ALIGN_MIDDLE);
			tabela.addCell(poljeZaglavlja);

			for (SubjectClassEntity predmet : predmeti) {
				poljeZaglavlja = new PdfPCell(new Phrase(predmet.getName(), fontZaglavlja));
				poljeZaglavlja.setHorizontalAlignment(Element.ALIGN_CENTER);
				poljeZaglavlja.setVerticalAlignment(Element.ALIGN_MIDDLE);
				poljeZaglavlja.setFixedHeight(50f);
				tabela.addCell(poljeZaglavlja);
			}

			for (StudentEntity ucenik : ucenici) {
				PdfPCell polje;
				polje = new PdfPCell(new Phrase(ucenik.getImeUcenika() + " " + ucenik.getPrezimeUcenika()));
				polje.setHorizontalAlignment(Element.ALIGN_CENTER);
				polje.setVerticalAlignment(Element.ALIGN_MIDDLE);
				polje.setFixedHeight(36f);
				tabela.addCell(polje);

				for (SubjectClassEntity predmet : predmeti) {
					List<GradeEntity> ocene = gradeRepository.findByPredmetUOdeljenjuAndUcenikId(predmet,
							ucenik.getId());
					String ocenaPredmet = " ";
					for (GradeEntity ocena : ocene) {
						if (ocena.getOcena() != null && !ocena.getZakljucnaOcena()) {
							ocenaPredmet += ocena.getOcena().toString() + " ";

						}
					}
					polje = new PdfPCell(new Phrase(ocenaPredmet));
					polje.setHorizontalAlignment(Element.ALIGN_CENTER);
					polje.setVerticalAlignment(Element.ALIGN_MIDDLE);
					polje.setFixedHeight(36f);
					tabela.addCell(polje);
				}
			}

			PdfWriter.getInstance(dokument, out);
			dokument.open();
			dokument.add(tabela);
			dokument.close();
		} catch (DocumentException e) {
			logger.error("Došlo je do greške prilikom ispisa PDF datoteke.");
		}

		return new ByteArrayInputStream(out.toByteArray());
	}
}
