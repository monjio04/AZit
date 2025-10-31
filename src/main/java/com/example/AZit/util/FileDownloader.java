package com.example.AZit.util;

import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileDownloader {
    public void downloadFile(String urlStr, String outputPath) throws Exception {
        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent()); // 폴더 없으면 생성
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.write(path, in.readAllBytes());
        }
    }
}
