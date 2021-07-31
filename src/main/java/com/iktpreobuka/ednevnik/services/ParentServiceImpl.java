package com.iktpreobuka.ednevnik.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.ParentEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity.Rola;
import com.iktpreobuka.ednevnik.entities.StudentEntity;
import com.iktpreobuka.ednevnik.entities.dto.ParentDTO;
import com.iktpreobuka.ednevnik.repositories.ParentRepository;
import com.iktpreobuka.ednevnik.repositories.RoleRepository;
import com.iktpreobuka.ednevnik.utils.Encryption;

@Service
public class ParentServiceImpl implements ParentService {

	@Autowired
	ParentRepository parentRepository;

	@Autowired
	RoleRepository roleRepository;

	/*
	 * Metoda za dodavanje novog roditelja. Osim setovanja osnovnih podataka,
	 * roditelju je rola automatski setuje na "ROLE_PARENT".
	 */

	public ParentEntity dodajRoditelja(ParentDTO newParent) {
		ParentEntity parent = new ParentEntity();
		RoleEntity role = roleRepository.findByName(Rola.ROLE_PARENT);
		parent.setKorisnickoIme(newParent.getKorisnickoIme());
		parent.setLozinka(Encryption.getPassEncoded(newParent.getLozinka()));
		parent.setImeRoditelja(newParent.getImeRoditelja());
		parent.setPrezimeRoditelja(newParent.getPrezimeRoditelja());
		parent.setEmail(newParent.getEmail());
		parent.setBrojTelefona(newParent.getBrojTelefona());
		parent.setRole(role);
		parentRepository.save(parent);
		return parent;
	}

	/* Metoda za izmenu podataka o roditelju */

	public ParentEntity izmenaRoditelja(Integer roditeljId, ParentDTO noviRoditelj) {
		ParentEntity roditelj = parentRepository.findById(roditeljId).get();
		if (noviRoditelj.getImeRoditelja() != null)
			roditelj.setImeRoditelja(noviRoditelj.getImeRoditelja());
		if (noviRoditelj.getPrezimeRoditelja() != null)
			roditelj.setPrezimeRoditelja(noviRoditelj.getPrezimeRoditelja());
		if (noviRoditelj.getEmail() != null)
			roditelj.setEmail(noviRoditelj.getEmail());
		if (noviRoditelj.getBrojTelefona() != null)
			roditelj.setBrojTelefona(noviRoditelj.getBrojTelefona());
		parentRepository.save(roditelj);
		return roditelj;
	}

	/* Metoda za brisanje roditelja. */

	public ParentEntity brisanjeRoditelja(Integer roditeljId) {
		ParentEntity roditelj = parentRepository.findById(roditeljId).get();
		for (StudentEntity dete : roditelj.getDeca())
			dete.setRoditelj(null);
		parentRepository.delete(roditelj);
		return roditelj;
	}

}
