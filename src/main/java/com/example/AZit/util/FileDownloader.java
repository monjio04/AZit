package com.example.AZit.util;

import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileDownloader {
    public Path downloadFile(String urlStr) throws Exception {
        Path tempPath = Files.createTempFile("musicgen_", ".wav");
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.write(tempPath, in.readAllBytes());
        }
        return tempPath;
    }

}
