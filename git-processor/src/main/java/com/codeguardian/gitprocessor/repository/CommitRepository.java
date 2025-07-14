package com.codeguardian.gitprocessor.repository;

import com.codeguardian.gitprocessor.entity.CommitEntity;
import com.codeguardian.gitprocessor.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommitRepository extends JpaRepository<CommitEntity, Long> {
    Optional<CommitEntity> findByCommitId(String commitId);
    List<CommitEntity> findByRepository(RepositoryEntity repository);
    // Additional methods
    List<CommitEntity> findByRepositoryId(Long repositoryId);
    List<CommitEntity> findByAnalysisStatus(String analysisStatus);
    List<CommitEntity> findByRepositoryAndAnalysisStatus(RepositoryEntity repository, String analysisStatus);

    @Query("SELECT c FROM CommitEntity c WHERE c.repository.id = :repositoryId AND c.createdAt >= :since ORDER BY c.timestamp DESC")
    List<CommitEntity> findByRepositoryIdAndCreatedAtAfter(
            @Param("repositoryId") Long repositoryId,
            @Param("since") LocalDateTime since);

    @Query("SELECT c FROM CommitEntity c WHERE c.authorEmail = :email ORDER BY c.timestamp DESC")
    List<CommitEntity> findByAuthorEmailOrderByTimestampDesc(@Param("email") String email);

    Long countByRepository(RepositoryEntity repository);
    Long countByRepositoryAndAnalysisStatus(RepositoryEntity repository, String analysisStatus);

    @Query("SELECT c FROM CommitEntity c WHERE c.repository = :repository AND " +
            "(c.filesAdded LIKE %:filename% OR c.filesModified LIKE %:filename% OR c.filesRemoved LIKE %:filename%)")
    List<CommitEntity> findByRepositoryAndFileChanges(@Param("repository") RepositoryEntity repository,
                                                      @Param("filename") String filename);

}
