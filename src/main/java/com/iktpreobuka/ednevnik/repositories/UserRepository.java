package com.iktpreobuka.ednevnik.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {

	UserEntity findByKorisnickoIme(String korisnickoIme);

	@Query("SELECT u FROM UserEntity u WHERE u.role.name = 'ROLE_ADMIN'")
	public List<UserEntity> findByRoleAdmin();

}
