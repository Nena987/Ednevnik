package com.iktpreobuka.ednevnik.utils;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iktpreobuka.ednevnik.entities.dto.UserDTO;

@Component
public class UserCustomValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UserDTO.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserDTO user = (UserDTO) target;
		if (!user.getLozinka().equals(user.getPonovljenaLozinka()))
			errors.reject("400", "Šifre moraju biti iste!");
	}

}
