package com.iktpreobuka.ednevnik.utils;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iktpreobuka.ednevnik.entities.dto.StudentDTO;

@Component
public class StudentCustomValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return StudentDTO.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		StudentDTO student = (StudentDTO) target;
		if (student.getLozinka() != null)
			if (!student.getLozinka().equals(student.getPonovljenaLozinka()))
				errors.reject("400", "Šifre moraju biti iste!");
	}

}
