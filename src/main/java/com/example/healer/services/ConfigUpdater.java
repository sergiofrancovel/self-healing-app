package com.example.healer.services;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;

@Service
public class ConfigUpdater {
    public void updateConfig(String configChanges) {
        try {
            Path propertiesFilePath = Path.of("src/main/resources/application.properties").toAbsolutePath();
            System.out.println("Attempting to write to: " + propertiesFilePath);

            Files.write(
                    propertiesFilePath,
                    (configChanges + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND, // Append new content
                    StandardOpenOption.CREATE  // Create file if it doesn't exist
            );

            System.out.println("Successfully wrote: " + configChanges);
        } catch (IOException e) {
            System.out.println("Failed to update configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}