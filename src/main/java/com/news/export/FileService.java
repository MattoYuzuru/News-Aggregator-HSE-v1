package com.news.export;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileService {
    public File createDirectoryIfNotExists(String dirPath) throws IOException {
        File outputDir = new File(dirPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Could not create output directory: " + dirPath);
        }
        return outputDir;
    }

    public void writeToFile(File file, String content) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
