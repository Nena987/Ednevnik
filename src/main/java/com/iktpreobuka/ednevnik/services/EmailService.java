package com.iktpreobuka.ednevnik.services;

import com.iktpreobuka.ednevnik.entities.GradeEntity;

public interface EmailService {

	public void posaljiMejlRoditelju(GradeEntity ocena) throws Exception;

	public void posaljiMejlRoditeljuAdmin(GradeEntity ocena) throws Exception;

}
