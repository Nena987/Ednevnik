package com.iktpreobuka.ednevnik.utils;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iktpreobuka.ednevnik.entities.dto.ParentDTO;

@Component
public class ParentCustomValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return ParentDTO.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ParentDTO parent = (ParentDTO) target;
		if (parent.getLozinka() != null)
			if (!parent.getLozinka().equals(parent.getPonovljenaLozinka()))
				errors.reject("400", "Šifre moraju biti iste!");
	}

}
