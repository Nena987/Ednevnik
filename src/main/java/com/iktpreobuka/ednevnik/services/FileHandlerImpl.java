package com.iktpreobuka.ednevnik.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iktpreobuka.ednevnik.repositories.SubjectRepository;

@Service
public class FileHandlerImpl implements FileHandler {

	@Autowired
	SubjectRepository subjectRepository;

	public void downloadLogFile(HttpServletResponse response) throws IOException {

		File file = new File(
				"C:\\Users\\Nena-Laptop\\Documents\\workspace-spring-tool-suite-4-4.10.0.RELEASE\\ednevnik\\logs\\spring-boot-logging.log");

		response.setContentType("application/octet-stream");
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=" + file.getName();

		response.setHeader(headerKey, headerValue);

		ServletOutputStream outputStream = response.getOutputStream();
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

		byte[] buffer = new byte[8192]; // 8KB buffer

		int bytesRead = -1;

		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		inputStream.close();
		outputStream.close();

	}

}
