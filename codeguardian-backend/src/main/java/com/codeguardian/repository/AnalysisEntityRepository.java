package com.codeguardian.repository;

import com.codeguardian.entity.AnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalysisEntityRepository extends JpaRepository<AnalysisEntity, String> {
    
    List<AnalysisEntity> findByRepositoryId(Long repositoryId);
    
    List<AnalysisEntity> findByStatus(String status);
    
    @Query("SELECT a FROM AnalysisEntity a WHERE a.repositoryId = :repositoryId ORDER BY a.timestamp DESC")
    List<AnalysisEntity> findByRepositoryIdOrderByTimestampDesc(@Param("repositoryId") Long repositoryId);
    
    @Query("SELECT a FROM AnalysisEntity a ORDER BY a.timestamp DESC")
    List<AnalysisEntity> findAllOrderByTimestampDesc();
    
    @Query("SELECT a FROM AnalysisEntity a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AnalysisEntity> findRecentAnalyses(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM AnalysisEntity a WHERE a.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(a.criticalFindings + a.highFindings + a.mediumFindings + a.lowFindings) FROM AnalysisEntity a WHERE a.status = 'COMPLETED'")
    Long getTotalFindings();
}