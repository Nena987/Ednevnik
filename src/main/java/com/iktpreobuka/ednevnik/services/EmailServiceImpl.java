package com.iktpreobuka.ednevnik.services;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.GradeEntity;
import com.iktpreobuka.ednevnik.entities.TeacherEntity;
import com.iktpreobuka.ednevnik.repositories.TeacherRepository;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	JavaMailSender emailSender;

	@Autowired
	TeacherRepository teacherRepository;

	/*
	 * Metoda za slanje mejla u kome pise koji ucenik je dobio koju ocenu iz kog
	 * predmeta i koji je nastavnik koji je ocenu dao, Mejl se salje roditelju
	 * ucenika
	 */

	public void posaljiMejlRoditelju(GradeEntity ocena) throws Exception {
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		TeacherEntity nastavnik = teacherRepository.findByKorisnickoIme(korisnickoIme);
		MimeMessage mail = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, true);
		helper.setTo(ocena.getUcenik().getRoditelj().getEmail());
		helper.setSubject("Nova ocena");
		String text = "<!DOCTYPE html><html><body>" + "<style>table {\r\n" + "  font-family: arial, sans-serif;\r\n"
				+ "  border-collapse: collapse;\r\n" + "  width: 100%;\r\n" + "}\r\n" + "\r\n" + "td, th {\r\n"
				+ "  border: 1px solid #dddddd;\r\n" + "  text-align: left;\r\n" + "  padding: 8px;\r\n" + "}\r\n"
				+ "\r\n" + "tr:nth-child(even) {\r\n" + "  background-color: #dddddd;\r\n" + "}\r\n" + "</style>\r\n"
				+ "</head>" + "<body>\r\n" + "\r\n" + "<h2>Nova ocena</h2>\r\n" + "\r\n" + "<table>\r\n" + "  <tr>"
				+ "<th>Učenik</th>" + "<th>Predmet</th>" + "<th>Ocena</th>" + "<th>Nastavnik</th>\r\n" + "  </tr>"
				+ "<tr><td>" + ocena.getUcenik().getImeUcenika() + " " + ocena.getUcenik().getPrezimeUcenika()
				+ "</td><td>" + ocena.getPredmet_odeljenje().getName() + "</td><td>" + ocena.getOcena() + "</td><td>"
				+ nastavnik.getIme() + " " + nastavnik.getPrezime() + "</td></tr>" + "</table></body></html>";
		helper.setText(text, true);
		emailSender.send(mail);
	}

	/*
	 * Metoda za slanje mejla u kome pise koji ucenik je dobio koju ocenu iz kog
	 * predmeta i koji je administrator koji je ocenu dao, Mejl se salje roditelju
	 * ucenika
	 */

	public void posaljiMejlRoditeljuAdmin(GradeEntity ocena) throws Exception {
		String korisnickoIme = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		MimeMessage mail = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, true);
		helper.setTo(ocena.getUcenik().getRoditelj().getEmail());
		helper.setSubject("Nova ocena");
		String text = "<!DOCTYPE html><html><body>" + "<style>table {\r\n" + "  font-family: arial, sans-serif;\r\n"
				+ "  border-collapse: collapse;\r\n" + "  width: 100%;\r\n" + "}\r\n" + "\r\n" + "td, th {\r\n"
				+ "  border: 1px solid #dddddd;\r\n" + "  text-align: left;\r\n" + "  padding: 8px;\r\n" + "}\r\n"
				+ "\r\n" + "tr:nth-child(even) {\r\n" + "  background-color: #dddddd;\r\n" + "}\r\n" + "</style>\r\n"
				+ "</head>" + "<body>\r\n" + "\r\n" + "<h2>Nova ocena</h2>\r\n" + "\r\n" + "<table>\r\n" + "  <tr>"
				+ "<th>Učenik</th>" + "<th>Predmet</th>" + "<th>Ocena</th>" + "<th>Nastavnik</th>\r\n" + "  </tr>"
				+ "<tr><td>" + ocena.getUcenik().getImeUcenika() + " " + ocena.getUcenik().getPrezimeUcenika()
				+ "</td><td>" + ocena.getPredmet_odeljenje().getName() + "</td><td>" + ocena.getOcena() + "</td><td>"
				+ "Administrator" + " " + korisnickoIme + "</td></tr>" + "</table></body></html>";
		helper.setText(text, true);
		emailSender.send(mail);
	}

}
