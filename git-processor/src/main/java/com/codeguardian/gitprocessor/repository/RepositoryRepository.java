package com.codeguardian.gitprocessor.repository;

import com.codeguardian.gitprocessor.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long> {
    Optional<RepositoryEntity> findByExternalId(String externalId);
    Optional<RepositoryEntity> findByFullName(String fullName);
}
