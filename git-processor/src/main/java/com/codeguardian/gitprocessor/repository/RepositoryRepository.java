package com.codeguardian.gitprocessor.repository;

import com.codeguardian.gitprocessor.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long> {
    Optional<RepositoryEntity> findByExternalId(String externalId);
    Optional<RepositoryEntity> findByFullName(String fullName);
    List<RepositoryEntity> findByPlatform(String platform);
    List<RepositoryEntity> findByNameContainingIgnoreCase(String name);

    @Query("SELECT r FROM RepositoryEntity r WHERE r.updatedAt >= :since")
    List<RepositoryEntity> findRecentlyUpdated(@Param("since") LocalDateTime since);

}
