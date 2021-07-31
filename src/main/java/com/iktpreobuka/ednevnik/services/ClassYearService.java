package com.iktpreobuka.ednevnik.services;

import com.iktpreobuka.ednevnik.entities.ClassYearEntity;
import com.iktpreobuka.ednevnik.entities.dto.ClassYearDTO;

public interface ClassYearService {

	public ClassYearEntity dodajRazred(ClassYearDTO newClassYear);

	public ClassYearEntity brisanjeRazreda(Integer razredId);
}
