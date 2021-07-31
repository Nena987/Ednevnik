package com.iktpreobuka.ednevnik.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iktpreobuka.ednevnik.entities.dto.ClassYearDTO;

@Component
public class ClassYearValidator implements Validator {

	public boolean supports(Class<?> clazz) {
		return ClassYearDTO.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ClassYearDTO razred = (ClassYearDTO) target;
		Set<String> razredi = new HashSet<String>();
		razredi.add("I");
		razredi.add("II");
		razredi.add("III");
		razredi.add("IV");
		razredi.add("V");
		razredi.add("VI");
		razredi.add("VII");
		razredi.add("VIII");
		if (razred.getNazivRazreda() != null)
			if (!razredi.contains(razred.getNazivRazreda()))
				errors.reject("400",
						"Dozvoljene vrednosti za polugodište su 'I', 'II', 'III', 'IV', 'V', 'VI', 'VII' i 'VIII'!");
	}

}
