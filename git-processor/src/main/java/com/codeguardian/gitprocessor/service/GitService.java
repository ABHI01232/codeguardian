package com.codeguardian.gitprocessor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GitService {
    @Value("${app.git.clone-base-path:/tmp/codeguardian/repos}")
    private String cloneBasePath;

    public String cloneRepository(String cloneUrl, String repositoryName) {
        try {
            Path repoPath = Paths.get(cloneBasePath, repositoryName);

            // Clean up existing directory if it exists
            if (Files.exists(repoPath)) {
                deleteDirectory(repoPath.toFile());
            }

            // Create parent directories
            Files.createDirectories(repoPath.getParent());

            // Clone the repository
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "git", "clone", cloneUrl, repoPath.toString()
            );

            Process process = processBuilder.start();
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Git clone timeout for repository: " + repositoryName);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String error = readStream(process.getErrorStream());
                throw new RuntimeException("Git clone failed: " + error);
            }

            log.info("Successfully cloned repository: {} to {}", repositoryName, repoPath);
            return repoPath.toString();

        } catch (Exception e) {
            log.error("Failed to clone repository: {}", repositoryName, e);
            throw new RuntimeException("Failed to clone repository: " + repositoryName, e);
        }
    }

    public void checkoutCommit(String repositoryPath, String commitId) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "git", "checkout", commitId
            );
            processBuilder.directory(new File(repositoryPath));

            Process process = processBuilder.start();
            boolean finished = process.waitFor(1, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Git checkout timeout for commit: " + commitId);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String error = readStream(process.getErrorStream());
                throw new RuntimeException("Git checkout failed: " + error);
            }

            log.info("Successfully checked out commit: {} in {}", commitId, repositoryPath);

        } catch (Exception e) {
            log.error("Failed to checkout commit: {} in {}", commitId, repositoryPath, e);
            throw new RuntimeException("Failed to checkout commit: " + commitId, e);
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
