package com.iktpreobuka.ednevnik.services;

import com.iktpreobuka.ednevnik.entities.ParentEntity;
import com.iktpreobuka.ednevnik.entities.dto.ParentDTO;

public interface ParentService {

	public ParentEntity dodajRoditelja(ParentDTO newParent);

	public ParentEntity izmenaRoditelja(Integer roditeljId, ParentDTO noviRoditelj);

	public ParentEntity brisanjeRoditelja(Integer roditeljId);

}
