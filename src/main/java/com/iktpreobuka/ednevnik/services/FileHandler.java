package com.iktpreobuka.ednevnik.services;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public interface FileHandler {

	public void downloadLogFile(HttpServletResponse response) throws IOException;

}
