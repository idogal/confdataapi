package com.idog.confdata.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DiskStorageService implements DiskStorage {

    private static final Logger LOGGER = LogManager.getLogger(DiskStorageService.class);

    Charset charset = Charset.forName(StandardCharsets.UTF_8.name());
    private String ext = "json";
    private Path root;

    public DiskStorageService(String root) throws IOException {
        this(Paths.get(root));
    }

    public DiskStorageService(Path root) throws IOException {
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        } else if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Root should be a directory.");
        }

        this.root = root;
    }

    @Override
    public void persist(String key, String value) {
        String fileName = String.format("%s.%s", key, ext);
        Path fullPath = root.resolve(fileName);
        if (Files.exists(fullPath)) {
            return;
        }

        try (PrintStream out = new PrintStream(new FileOutputStream(fullPath.toFile()), true, charset.name())) {
            out.print(value);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            LOGGER.error(e);
        }
    }

    @Override
    public String get(String key) {
        String fileName = String.format("%s.%s", key, ext);
        Path fullPath = root.resolve(fileName);
        if (!Files.exists(fullPath)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(fullPath, charset);

            String all = "";
            for (String line : lines) {
                all = all + line + System.lineSeparator();
            }

            return all;

        } catch (IOException e) {
            LOGGER.error(e);
            return null;
        }
    }
}
