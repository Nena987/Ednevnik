package com.iktpreobuka.ednevnik.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.entities.RoleEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity.Rola;
import com.iktpreobuka.ednevnik.entities.UserEntity;
import com.iktpreobuka.ednevnik.entities.dto.UserDTO;
import com.iktpreobuka.ednevnik.repositories.RoleRepository;
import com.iktpreobuka.ednevnik.repositories.UserRepository;
import com.iktpreobuka.ednevnik.utils.Encryption;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	UserRepository userRepository;

	/* Metoda za dodavanje novog admina */

	public UserEntity createNewUser(UserDTO newAdmin) {
		UserEntity admin = new UserEntity();
		RoleEntity role = roleRepository.findByName(Rola.ROLE_ADMIN);
		admin.setKorisnickoIme(newAdmin.getKorisnickoIme());
		admin.setLozinka(Encryption.getPassEncoded(newAdmin.getLozinka()));
		admin.setRole(role);
		userRepository.save(admin);
		return admin;
	}

	/* Metoda za dodavanje i promenu role korisnika */

	@Override
	public UserEntity promenaRole(Integer korisnikId, Integer rolaId) {
		UserEntity korisnik = userRepository.findById(korisnikId).get();
		RoleEntity rola = roleRepository.findById(rolaId).get();
		korisnik.setRole(rola);
		userRepository.save(korisnik);
		return korisnik;
	}

	
}
