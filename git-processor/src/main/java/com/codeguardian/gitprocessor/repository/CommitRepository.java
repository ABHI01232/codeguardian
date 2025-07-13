package com.codeguardian.gitprocessor.repository;

import com.codeguardian.gitprocessor.entity.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommitRepository extends JpaRepository<CommitEntity, Long> {
    Optional<CommitEntity> findByCommitId(String commitId);
}
