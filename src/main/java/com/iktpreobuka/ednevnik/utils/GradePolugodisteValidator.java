package com.iktpreobuka.ednevnik.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iktpreobuka.ednevnik.entities.dto.GradeDTO;

@Component
public class GradePolugodisteValidator implements Validator {

	public boolean supports(Class<?> clazz) {
		return GradeDTO.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		GradeDTO ocena = (GradeDTO) target;
		Set<String> polugodista = new HashSet<String>();
		polugodista.add("PRVO_POLUGODISTE");
		polugodista.add("DRUGO_POLUGODISTE");
		if (ocena.getPolugodiste() != null)
			if (!polugodista.contains(ocena.getPolugodiste()))
				errors.reject("400",
						"Dozvoljene vrednosti za polugodište su 'PRVO_POLUGODISTE' i 'DRUGO_POLUGODISTE'!");
	}

}
