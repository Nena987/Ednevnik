package com.iktpreobuka.ednevnik.services;

import com.iktpreobuka.ednevnik.entities.ClassEntity;
import com.iktpreobuka.ednevnik.entities.ClassYearEntity.ClassYear;

public interface ClassService {

	public ClassEntity dodajOdeljenje(ClassYear razred, Integer brojOdeljenja, Integer razredniId);

	public ClassEntity promenaRazrednogStaresine(Integer odeljenjeId, Integer nastavnikId);

	public ClassEntity brisanjeOdeljenja(Integer odeljenjeId);
}
