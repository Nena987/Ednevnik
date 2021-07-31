package com.iktpreobuka.ednevnik.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iktpreobuka.ednevnik.entities.dto.GradeDTO;

@Component
public class GradeVrstaOceneValidator implements Validator {

	public boolean supports(Class<?> clazz) {
		return GradeDTO.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		GradeDTO ocena = (GradeDTO) target;
		Set<String> vrstaOcene = new HashSet<String>();
		vrstaOcene.add("PISMENI_ZADATAK");
		vrstaOcene.add("KONTROLNI_ZADATAK");
		vrstaOcene.add("USMENO_ODGOVARANJE");
		vrstaOcene.add("ZAKLJUCNA_OCENA");
		vrstaOcene.add("OSTALO");
		if (ocena.getVrsta() != null)
			if (!vrstaOcene.contains(ocena.getVrsta()))
				errors.reject("400",
						"Dozvoljene vrednosti za vrstu ocene su 'PISMENI_ZADATAK', 'KONTROLNI_ZADATAK', 'USMENO_ODGOVARANJE', 'ZAKLJUCNA_OCENA' i 'OSTALO'!");
	}

}
