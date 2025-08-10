package com.codeguardian.repository;

import com.codeguardian.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, Long> {
    
    Optional<RepositoryEntity> findByUrl(String url);
    
    Optional<RepositoryEntity> findByNameAndPlatform(String name, String platform);
    
    List<RepositoryEntity> findByPlatform(String platform);
    
    @Query("SELECT r FROM RepositoryEntity r WHERE r.securityStatus = :status")
    List<RepositoryEntity> findBySecurityStatus(@Param("status") String status);
    
    @Query("SELECT r FROM RepositoryEntity r ORDER BY r.lastAnalysisDate DESC")
    List<RepositoryEntity> findAllOrderByLastAnalysisDesc();
    
    @Query("SELECT r FROM RepositoryEntity r ORDER BY r.createdAt DESC")
    List<RepositoryEntity> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(r) FROM RepositoryEntity r WHERE r.securityStatus = :status")
    long countBySecurityStatus(@Param("status") String status);
}