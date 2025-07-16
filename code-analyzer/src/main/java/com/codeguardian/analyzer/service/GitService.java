package com.codeguardian.analyzer.service;

import com.codeguardian.analyzer.model.AnalysisRequest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GitService {

    @Value("${app.git.clone-base-path:/tmp/codeguardian/repos}")
    private String cloneBasePath;

    public AnalysisRequest createAnalysisRequest(Map<String, Object> commitData) {
        String commitId = (String) commitData.get("commitId");
        String repositoryName = (String) commitData.get("repositoryName");
        String repositoryUrl = (String) commitData.get("repositoryUrl");
        String repositoryCloneUrl = (String) commitData.get("repositoryCloneUrl");
        String platform = (String) commitData.get("platform");
        
        log.info("Creating analysis request for commit: {} in repository: {}", commitId, repositoryName);
        
        List<AnalysisRequest.FileChange> fileChanges = new ArrayList<>();
        
        try {
            // Get repository directory
            Path repoPath = getOrCloneRepository(repositoryCloneUrl, repositoryName);
            
            if (repoPath != null) {
                // Get file changes from commit
                fileChanges = getFileChangesFromCommit(repoPath, commitId);
            } else {
                log.warn("Could not access repository, creating request with file list only");
                fileChanges = createFileChangesFromCommitData(commitData);
            }
            
        } catch (Exception e) {
            log.error("Error accessing repository for commit analysis: {}", e.getMessage(), e);
            // Fallback to basic file change information
            fileChanges = createFileChangesFromCommitData(commitData);
        }
        
        return new AnalysisRequest(
            commitId,
            repositoryUrl,
            "main", // branch
            commitId,
            (String) commitData.get("message"),
            (String) commitData.get("authorName"),
            java.time.LocalDateTime.now(),
            fileChanges,
            java.util.Map.of()
        );
    }

    private Path getOrCloneRepository(String cloneUrl, String repositoryName) {
        try {
            // Create repository directory
            Path repoPath = Paths.get(cloneBasePath, sanitizeRepositoryName(repositoryName));
            
            if (Files.exists(repoPath.resolve(".git"))) {
                // Repository exists, pull latest changes
                log.info("Repository exists, pulling latest changes: {}", repoPath);
                try (Git git = Git.open(repoPath.toFile())) {
                    git.pull().call();
                    log.info("Successfully pulled latest changes for: {}", repositoryName);
                }
                return repoPath;
            } else {
                // Clone repository
                log.info("Cloning repository: {} to {}", cloneUrl, repoPath);
                Files.createDirectories(repoPath.getParent());
                
                try (Git git = Git.cloneRepository()
                        .setURI(cloneUrl)
                        .setDirectory(repoPath.toFile())
                        .setCloneSubmodules(false)
                        .call()) {
                    log.info("Successfully cloned repository: {}", repositoryName);
                    return repoPath;
                }
            }
        } catch (Exception e) {
            log.error("Error cloning/accessing repository: {}", e.getMessage(), e);
            return null;
        }
    }

    private List<AnalysisRequest.FileChange> getFileChangesFromCommit(Path repoPath, String commitId) {
        List<AnalysisRequest.FileChange> fileChanges = new ArrayList<>();
        
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoPath.resolve(".git").toFile())
                .build()) {
            
            try (RevWalk revWalk = new RevWalk(repository)) {
                ObjectId commitObjectId = repository.resolve(commitId);
                if (commitObjectId == null) {
                    log.warn("Commit not found: {}", commitId);
                    return fileChanges;
                }
                
                RevCommit commit = revWalk.parseCommit(commitObjectId);
                RevCommit parentCommit = null;
                
                if (commit.getParentCount() > 0) {
                    parentCommit = revWalk.parseCommit(commit.getParent(0).getId());
                }
                
                // Get diff between commit and parent
                try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                    diffFormatter.setRepository(repository);
                    
                    List<DiffEntry> diffs = diffFormatter.scan(
                        parentCommit != null ? parentCommit.getTree() : null,
                        commit.getTree()
                    );
                    
                    for (DiffEntry diff : diffs) {
                        String filePath = diff.getNewPath();
                        if (filePath.equals("/dev/null")) {
                            filePath = diff.getOldPath();
                        }
                        
                        // Only analyze certain file types
                        if (shouldAnalyzeFile(filePath)) {
                            String content = getFileContent(repository, commit.getTree(), filePath);
                            if (content != null) {
                                fileChanges.add(new AnalysisRequest.FileChange(
                                    filePath,
                                    diff.getChangeType().name(),
                                    content,
                                    null, // previousContent
                                    0,    // additions
                                    0,    // deletions
                                    null  // patch
                                ));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting file changes from commit: {}", e.getMessage(), e);
        }
        
        return fileChanges;
    }

    private String getFileContent(Repository repository, RevTree tree, String filePath) {
        try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, tree)) {
            if (treeWalk != null) {
                byte[] data = repository.open(treeWalk.getObjectId(0)).getBytes();
                return new String(data);
            }
        } catch (Exception e) {
            log.error("Error reading file content: {}", e.getMessage(), e);
        }
        return null;
    }

    private boolean shouldAnalyzeFile(String filePath) {
        String[] extensionsToAnalyze = {
            ".java", ".js", ".jsx", ".ts", ".tsx", ".py", ".php", ".rb", ".go", ".cs", ".cpp", ".c", ".h",
            ".sql", ".xml", ".html", ".css", ".json", ".yaml", ".yml", ".properties", ".sh", ".bat"
        };
        
        for (String ext : extensionsToAnalyze) {
            if (filePath.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private List<AnalysisRequest.FileChange> createFileChangesFromCommitData(Map<String, Object> commitData) {
        List<AnalysisRequest.FileChange> fileChanges = new ArrayList<>();
        
        // Create sample file changes for demonstration
        String filesAdded = (String) commitData.get("filesAdded");
        String filesModified = (String) commitData.get("filesModified");
        String filesRemoved = (String) commitData.get("filesRemoved");
        
        if (filesAdded != null && !filesAdded.isEmpty()) {
            for (String file : filesAdded.split(",")) {
                fileChanges.add(new AnalysisRequest.FileChange(
                    file.trim(),
                    "ADDED",
                    generateSampleContent(file.trim()),
                    null, // previousContent
                    0,    // additions
                    0,    // deletions
                    null  // patch
                ));
            }
        }
        
        if (filesModified != null && !filesModified.isEmpty()) {
            for (String file : filesModified.split(",")) {
                fileChanges.add(new AnalysisRequest.FileChange(
                    file.trim(),
                    "MODIFIED",
                    generateSampleContent(file.trim()),
                    null, // previousContent
                    0,    // additions
                    0,    // deletions
                    null  // patch
                ));
            }
        }
        
        if (filesRemoved != null && !filesRemoved.isEmpty()) {
            for (String file : filesRemoved.split(",")) {
                fileChanges.add(new AnalysisRequest.FileChange(
                    file.trim(),
                    "REMOVED",
                    null,
                    null, // previousContent
                    0,    // additions
                    0,    // deletions
                    null  // patch
                ));
            }
        }
        
        return fileChanges;
    }

    private String generateSampleContent(String filename) {
        // Generate realistic sample content based on file type for demonstration
        if (filename.endsWith(".java")) {
            return """
                public class SecurityExample {
                    private String password = "hardcoded123"; // Security issue!
                    
                    public void authenticateUser(String username, String pwd) {
                        String query = "SELECT * FROM users WHERE username = '" + username + "'"; // SQL injection risk!
                        // Database query logic here
                        System.out.println("Debug: User login attempt for " + username); // Debug info exposure
                    }
                    
                    public String hashPassword(String password) {
                        return MD5.hash(password); // Weak crypto algorithm
                    }
                }
                """;
        } else if (filename.endsWith(".js") || filename.endsWith(".jsx")) {
            return """
                const API_KEY = "sk-1234567890abcdef"; // Hardcoded secret
                
                function displayUserContent(userInput) {
                    document.getElementById('content').innerHTML = userInput; // XSS vulnerability
                    console.log('Debug: Displaying content for user:', userInput); // Debug exposure
                }
                
                function generateToken() {
                    return Math.random().toString(36); // Insecure random
                }
                
                function searchUsers(searchTerm) {
                    const query = `SELECT * FROM users WHERE name LIKE '%${searchTerm}%'`; // SQL injection
                    return database.query(query);
                }
                """;
        } else if (filename.endsWith(".py")) {
            return """
                import hashlib
                import random
                
                SECRET_KEY = "my-secret-key-123"  # Hardcoded secret
                
                def authenticate_user(username, password):
                    query = f"SELECT * FROM users WHERE username = '{username}'"  # SQL injection
                    print(f"Debug: Authenticating user {username}")  # Debug info
                    
                def hash_password(password):
                    return hashlib.md5(password.encode()).hexdigest()  # Weak crypto
                    
                def generate_session_id():
                    return str(random.random())  # Insecure random
                """;
        } else {
            return "// Sample code content for analysis\n" +
                   "const secret = 'hardcoded-secret-123';\n" +
                   "console.log('Debug information');\n";
        }
    }

    private String sanitizeRepositoryName(String repositoryName) {
        return repositoryName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}