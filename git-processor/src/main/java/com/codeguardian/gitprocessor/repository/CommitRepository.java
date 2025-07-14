package com.codeguardian.gitprocessor.repository;

import com.codeguardian.gitprocessor.entity.CommitEntity;
import com.codeguardian.gitprocessor.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommitRepository extends JpaRepository<CommitEntity, Long> {
    Optional<CommitEntity> findByCommitId(String commitId);
    List<CommitEntity> findByRepository(RepositoryEntity repository);
}
