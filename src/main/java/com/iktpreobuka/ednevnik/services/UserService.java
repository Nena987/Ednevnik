package com.iktpreobuka.ednevnik.services;

import com.iktpreobuka.ednevnik.entities.UserEntity;
import com.iktpreobuka.ednevnik.entities.dto.UserDTO;

public interface UserService {

	public UserEntity createNewUser(UserDTO newAdmin);

	public UserEntity promenaRole(Integer korisnikId, Integer rolaId);
}
