package ru.testing.testSistem.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

    @Service
    public class FileStorageService {
        private final Path rootLocation; // Путь к директории для хранения файлов

        public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
            this.rootLocation = Paths.get("").toAbsolutePath().resolve("src/main/resources/static/" + uploadDir);
            init();// Инициализируем директорию при создании сервиса
        }

        private void init() {
            try {
                Files.createDirectories(rootLocation); // Создает все несуществующие директории
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize storage", e);
            }
        }
        // сохранить файл
        public String store(MultipartFile file, String customFilename) {
            try {
                if (file.isEmpty()) {
                    throw new RuntimeException("Failed to store empty file");
                }

                // Очищаем имя файла от опасных символов ../
                String filename = StringUtils.cleanPath(customFilename);

                // Проверяем, что файл не пытается выйти за пределы папки
                Path destinationFile = rootLocation.resolve(filename).
                        normalize().
                        toAbsolutePath();
                // Защита от path traversal атак
                if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                    throw new RuntimeException("Cannot store file outside current directory");
                }

                // Сохраняем файл
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING); // Перезаписывает существующий файл
                    System.out.println("Trying to save to: " + destinationFile);
                    System.out.println("Root location: " + rootLocation.toAbsolutePath());
                    return filename;
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }
        /*        public void delete(String filename) {
            try {
                Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
                Files.deleteIfExists(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file", e);
            }
        }*/
    }