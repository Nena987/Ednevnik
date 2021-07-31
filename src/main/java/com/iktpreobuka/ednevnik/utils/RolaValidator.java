package com.iktpreobuka.ednevnik.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.iktpreobuka.ednevnik.entities.dto.RoleDTO;

@Component
public class RolaValidator implements Validator {

	public boolean supports(Class<?> clazz) {
		return RoleDTO.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		RoleDTO rola = (RoleDTO) target;
		Set<String> role = new HashSet<String>();
		role.add("ROLE_ADMIN");
		role.add("ROLE_STUDENT");
		role.add("ROLE_PARENT");
		role.add("ROLE_TEACHER");
		role.add("ROLE_CLASSTEACHER");
		if (rola.getNazivRole() != null)
			if (!role.contains(rola.getNazivRole()))
				errors.reject("400",
						"Dozvoljene vrednosti za rolu su 'ROLE_ADMIN', 'ROLE_STUDENT', 'ROLE_PARENT', 'ROLE_TEACHER' i 'ROLE_CLASSTEACHER'!");
	}

}
