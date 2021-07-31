package com.iktpreobuka.ednevnik.repositories;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.ednevnik.entities.RoleEntity;
import com.iktpreobuka.ednevnik.entities.RoleEntity.Rola;

public interface RoleRepository extends CrudRepository<RoleEntity, Integer> {

	public RoleEntity findByName(Rola name);

}
