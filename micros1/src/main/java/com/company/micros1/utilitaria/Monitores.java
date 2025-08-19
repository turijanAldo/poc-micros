package com.company.micros1.utilitaria;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.*;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Monitores {
     public static final Logger LOGGER = LoggerFactory.getLogger("GlobalLogger");

     private static final String LOG_FILE_PATH = "logs/MonitorGlobal.log";

     public ResponseEntity<ByteArrayResource> download(Integer lines) throws IOException {

        File file = new File(LOG_FILE_PATH);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] logContent;
        if (lines != null && lines > 0) {
            logContent = getLastNLines(file, lines).getBytes(StandardCharsets.UTF_8);
        } else {
            logContent = Files.readAllBytes(file.toPath());
        }

        // Comprimir en ZIP
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            zipOut.putNextEntry(new ZipEntry(file.getName()));
            zipOut.write(logContent);
            zipOut.closeEntry();
        }

        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }
        

     private String getLastNLines(File file, int n) throws IOException {
        LinkedList<String> linesList = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                linesList.add(line);
                if (linesList.size() > n) {
                    linesList.removeFirst();
                }
            }
        }
        return String.join(System.lineSeparator(), linesList);

    }

}
