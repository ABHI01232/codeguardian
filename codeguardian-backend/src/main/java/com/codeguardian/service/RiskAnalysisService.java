package com.codeguardian.service;

import com.codeguardian.entity.AnalysisEntity;
import com.codeguardian.entity.RepositoryEntity;
import com.codeguardian.repository.AnalysisEntityRepository;
import com.codeguardian.repository.RepositoryEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RiskAnalysisService {

    @Autowired
    private AnalysisEntityRepository analysisEntityRepository;

    @Autowired
    private RepositoryEntityRepository repositoryEntityRepository;

    // OWASP Top 10 vulnerability mappings
    private static final Map<String, String> OWASP_MAPPINGS = Map.of(
        "SQL_INJECTION", "A03:2021 - Injection",
        "XSS_VULNERABILITY", "A03:2021 - Injection", 
        "HARDCODED_SECRET", "A07:2021 - Identification and Authentication Failures",
        "WEAK_CRYPTO", "A02:2021 - Cryptographic Failures",
        "INSECURE_RANDOM", "A02:2021 - Cryptographic Failures",
        "PATH_TRAVERSAL", "A01:2021 - Broken Access Control",
        "CSRF_VULNERABILITY", "A01:2021 - Broken Access Control",
        "DEBUG_CODE", "A09:2021 - Security Logging and Monitoring Failures"
    );

    // Banking security risk weights
    private static final Map<String, Integer> BANKING_RISK_WEIGHTS = Map.of(
        "HARDCODED_SECRET", 10, // Critical for banking
        "SQL_INJECTION", 9,     // Data breach risk
        "WEAK_CRYPTO", 8,       // Regulatory requirement
        "XSS_VULNERABILITY", 7,  // Customer data risk
        "PATH_TRAVERSAL", 6,    // Data access risk
        "INSECURE_RANDOM", 5,   // Security weakness
        "CSRF_VULNERABILITY", 4, // Session security
        "DEBUG_CODE", 2         // Information disclosure
    );

    public Map<String, Object> calculateGlobalRiskOverview() {
        List<AnalysisEntity> allAnalyses = analysisEntityRepository.findAll();
        List<RepositoryEntity> allRepos = repositoryEntityRepository.findAll();

        Map<String, Object> overview = new HashMap<>();
        
        // Basic metrics
        overview.put("totalRepositories", allRepos.size());
        overview.put("totalAnalyses", allAnalyses.size());
        overview.put("lastAnalysisDate", allAnalyses.stream()
            .map(AnalysisEntity::getTimestamp)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null));

        // Risk calculations
        int totalCritical = allAnalyses.stream().mapToInt(a -> a.getCriticalFindings() != null ? a.getCriticalFindings() : 0).sum();
        int totalHigh = allAnalyses.stream().mapToInt(a -> a.getHighFindings() != null ? a.getHighFindings() : 0).sum();
        int totalMedium = allAnalyses.stream().mapToInt(a -> a.getMediumFindings() != null ? a.getMediumFindings() : 0).sum();
        int totalLow = allAnalyses.stream().mapToInt(a -> a.getLowFindings() != null ? a.getLowFindings() : 0).sum();

        overview.put("totalFindings", totalCritical + totalHigh + totalMedium + totalLow);
        overview.put("criticalFindings", totalCritical);
        overview.put("highFindings", totalHigh);
        overview.put("mediumFindings", totalMedium);
        overview.put("lowFindings", totalLow);

        // Global risk score (0-100)
        int globalRiskScore = calculateRiskScore(totalCritical, totalHigh, totalMedium, totalLow);
        overview.put("globalRiskScore", globalRiskScore);
        overview.put("riskLevel", getRiskLevel(globalRiskScore));

        // Banking compliance status
        overview.put("bankingComplianceScore", calculateBankingComplianceScore(allAnalyses));
        
        // Repository risk distribution
        Map<String, Integer> riskDistribution = new HashMap<>();
        for (RepositoryEntity repo : allRepos) {
            String riskLevel = getRepositoryRiskLevel(repo.getId());
            riskDistribution.put(riskLevel, riskDistribution.getOrDefault(riskLevel, 0) + 1);
        }
        overview.put("repositoryRiskDistribution", riskDistribution);

        return overview;
    }

    public Map<String, Object> calculateRepositoryRisk(Long repositoryId) {
        Optional<RepositoryEntity> repoOpt = repositoryEntityRepository.findById(repositoryId);
        if (repoOpt.isEmpty()) {
            return Map.of("error", "Repository not found");
        }

        RepositoryEntity repo = repoOpt.get();
        List<AnalysisEntity> analyses = analysisEntityRepository.findByRepositoryId(repositoryId);
        
        Map<String, Object> riskData = new HashMap<>();
        riskData.put("repositoryId", repositoryId);
        riskData.put("repositoryName", repo.getName());
        
        if (analyses.isEmpty()) {
            riskData.put("riskScore", 0);
            riskData.put("riskLevel", "UNKNOWN");
            riskData.put("message", "No analysis data available");
            return riskData;
        }

        // Get latest analysis
        AnalysisEntity latestAnalysis = analyses.stream()
            .max(Comparator.comparing(AnalysisEntity::getTimestamp))
            .orElse(analyses.get(0));

        int critical = latestAnalysis.getCriticalFindings() != null ? latestAnalysis.getCriticalFindings() : 0;
        int high = latestAnalysis.getHighFindings() != null ? latestAnalysis.getHighFindings() : 0;
        int medium = latestAnalysis.getMediumFindings() != null ? latestAnalysis.getMediumFindings() : 0;
        int low = latestAnalysis.getLowFindings() != null ? latestAnalysis.getLowFindings() : 0;

        int riskScore = calculateRiskScore(critical, high, medium, low);
        
        riskData.put("riskScore", riskScore);
        riskData.put("riskLevel", getRiskLevel(riskScore));
        riskData.put("totalFindings", critical + high + medium + low);
        riskData.put("criticalFindings", critical);
        riskData.put("highFindings", high);
        riskData.put("mediumFindings", medium);
        riskData.put("lowFindings", low);
        riskData.put("lastAnalysisDate", latestAnalysis.getTimestamp());
        
        // Banking specific risk assessment
        riskData.put("bankingRiskScore", calculateBankingRiskScore(latestAnalysis));
        riskData.put("regulatoryCompliance", calculateRegulatoryCompliance(latestAnalysis));
        
        return riskData;
    }

    public Map<String, Object> generateOwaspTop10Report() {
        List<AnalysisEntity> allAnalyses = analysisEntityRepository.findAll();
        Map<String, Object> report = new HashMap<>();
        
        Map<String, Integer> owaspCounts = new HashMap<>();
        Map<String, List<String>> owaspDetails = new HashMap<>();
        
        // Initialize OWASP categories
        for (String category : OWASP_MAPPINGS.values()) {
            owaspCounts.put(category, 0);
            owaspDetails.put(category, new ArrayList<>());
        }
        
        // Count vulnerabilities by OWASP category
        for (AnalysisEntity analysis : allAnalyses) {
            if (analysis.getFindingsDetails() != null) {
                String findings = analysis.getFindingsDetails();
                for (Map.Entry<String, String> mapping : OWASP_MAPPINGS.entrySet()) {
                    if (findings.contains("type=" + mapping.getKey())) {
                        String category = mapping.getValue();
                        owaspCounts.put(category, owaspCounts.get(category) + 1);
                        owaspDetails.get(category).add(analysis.getRepositoryName() + " - " + mapping.getKey());
                    }
                }
            }
        }
        
        report.put("owaspTop10Compliance", owaspCounts);
        report.put("vulnerabilityDetails", owaspDetails);
        report.put("totalRepositories", repositoryEntityRepository.count());
        report.put("complianceScore", calculateOwaspComplianceScore(owaspCounts));
        
        return report;
    }

    public Map<String, Object> generateBankingComplianceReport() {
        List<AnalysisEntity> allAnalyses = analysisEntityRepository.findAll();
        Map<String, Object> report = new HashMap<>();
        
        // Banking-specific compliance metrics
        int highRiskFindings = 0;
        int pciDssIssues = 0;
        int dataProtectionIssues = 0;
        int cryptographicIssues = 0;
        
        for (AnalysisEntity analysis : allAnalyses) {
            if (analysis.getFindingsDetails() != null) {
                String findings = analysis.getFindingsDetails();
                
                // Count high-risk banking vulnerabilities
                if (findings.contains("HARDCODED_SECRET") || findings.contains("SQL_INJECTION")) {
                    highRiskFindings++;
                }
                if (findings.contains("SQL_INJECTION") || findings.contains("XSS_VULNERABILITY")) {
                    pciDssIssues++;
                }
                if (findings.contains("HARDCODED_SECRET") || findings.contains("DEBUG_CODE")) {
                    dataProtectionIssues++;
                }
                if (findings.contains("WEAK_CRYPTO") || findings.contains("INSECURE_RANDOM")) {
                    cryptographicIssues++;
                }
            }
        }
        
        report.put("highRiskFindings", highRiskFindings);
        report.put("pciDssIssues", pciDssIssues);
        report.put("dataProtectionIssues", dataProtectionIssues);
        report.put("cryptographicIssues", cryptographicIssues);
        
        // Compliance scores (0-100)
        report.put("pciDssCompliance", Math.max(0, 100 - (pciDssIssues * 10)));
        report.put("dataProtectionCompliance", Math.max(0, 100 - (dataProtectionIssues * 15)));
        report.put("cryptographicCompliance", Math.max(0, 100 - (cryptographicIssues * 20)));
        
        int overallCompliance = (int) ((
            (int) report.get("pciDssCompliance") +
            (int) report.get("dataProtectionCompliance") +
            (int) report.get("cryptographicCompliance")
        ) / 3.0);
        
        report.put("overallBankingCompliance", overallCompliance);
        report.put("complianceStatus", overallCompliance >= 80 ? "COMPLIANT" : 
                                      overallCompliance >= 60 ? "NEEDS_IMPROVEMENT" : "NON_COMPLIANT");
        
        return report;
    }

    public Map<String, Object> calculateSecurityTrends(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<AnalysisEntity> recentAnalyses = analysisEntityRepository.findAll()
            .stream()
            .filter(a -> a.getTimestamp() != null && a.getTimestamp().isAfter(startDate))
            .sorted(Comparator.comparing(AnalysisEntity::getTimestamp))
            .collect(Collectors.toList());
        
        Map<String, Object> trends = new HashMap<>();
        trends.put("analysisCount", recentAnalyses.size());
        trends.put("timeRange", days + " days");
        
        if (recentAnalyses.isEmpty()) {
            trends.put("message", "No analysis data in the specified time range");
            return trends;
        }
        
        // Calculate trends over time
        Map<String, List<Integer>> dailyFindings = new HashMap<>();
        dailyFindings.put("critical", new ArrayList<>());
        dailyFindings.put("high", new ArrayList<>());
        dailyFindings.put("medium", new ArrayList<>());
        dailyFindings.put("low", new ArrayList<>());
        
        // Group by day and sum findings
        Map<String, List<AnalysisEntity>> dailyAnalyses = recentAnalyses.stream()
            .collect(Collectors.groupingBy(a -> a.getTimestamp().toLocalDate().toString()));
        
        for (Map.Entry<String, List<AnalysisEntity>> entry : dailyAnalyses.entrySet()) {
            List<AnalysisEntity> dayAnalyses = entry.getValue();
            int dayCritical = dayAnalyses.stream().mapToInt(a -> a.getCriticalFindings() != null ? a.getCriticalFindings() : 0).sum();
            int dayHigh = dayAnalyses.stream().mapToInt(a -> a.getHighFindings() != null ? a.getHighFindings() : 0).sum();
            int dayMedium = dayAnalyses.stream().mapToInt(a -> a.getMediumFindings() != null ? a.getMediumFindings() : 0).sum();
            int dayLow = dayAnalyses.stream().mapToInt(a -> a.getLowFindings() != null ? a.getLowFindings() : 0).sum();
            
            dailyFindings.get("critical").add(dayCritical);
            dailyFindings.get("high").add(dayHigh);
            dailyFindings.get("medium").add(dayMedium);
            dailyFindings.get("low").add(dayLow);
        }
        
        trends.put("dailyFindings", dailyFindings);
        trends.put("totalFindings", recentAnalyses.stream().mapToInt(a -> 
            (a.getCriticalFindings() != null ? a.getCriticalFindings() : 0) +
            (a.getHighFindings() != null ? a.getHighFindings() : 0) +
            (a.getMediumFindings() != null ? a.getMediumFindings() : 0) +
            (a.getLowFindings() != null ? a.getLowFindings() : 0)
        ).sum());
        
        return trends;
    }

    public Map<String, Object> generateExecutiveSummary() {
        Map<String, Object> globalOverview = calculateGlobalRiskOverview();
        Map<String, Object> bankingCompliance = generateBankingComplianceReport();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("organizationRiskLevel", globalOverview.get("riskLevel"));
        summary.put("totalRepositories", globalOverview.get("totalRepositories"));
        summary.put("criticalIssues", globalOverview.get("criticalFindings"));
        summary.put("bankingCompliance", bankingCompliance.get("overallBankingCompliance"));
        summary.put("complianceStatus", bankingCompliance.get("complianceStatus"));
        
        // Executive recommendations
        List<String> recommendations = generateExecutiveRecommendations(globalOverview, bankingCompliance);
        summary.put("keyRecommendations", recommendations);
        
        summary.put("generatedAt", LocalDateTime.now());
        return summary;
    }

    public Map<String, Object> calculateRemediationPriorities(Long repositoryId) {
        List<AnalysisEntity> analyses = analysisEntityRepository.findByRepositoryId(repositoryId);
        
        if (analyses.isEmpty()) {
            return Map.of("message", "No analysis data available for prioritization");
        }
        
        AnalysisEntity latestAnalysis = analyses.stream()
            .max(Comparator.comparing(AnalysisEntity::getTimestamp))
            .orElse(analyses.get(0));
        
        Map<String, Object> priorities = new HashMap<>();
        List<Map<String, Object>> priorityList = new ArrayList<>();
        
        // Parse findings and assign priorities
        if (latestAnalysis.getFindingsDetails() != null) {
            String findings = latestAnalysis.getFindingsDetails();
            for (Map.Entry<String, Integer> entry : BANKING_RISK_WEIGHTS.entrySet()) {
                if (findings.contains("type=" + entry.getKey())) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("vulnerabilityType", entry.getKey());
                    item.put("riskWeight", entry.getValue());
                    item.put("priority", entry.getValue() >= 8 ? "HIGH" : 
                                       entry.getValue() >= 5 ? "MEDIUM" : "LOW");
                    item.put("bankingImpact", getBankingImpact(entry.getKey()));
                    priorityList.add(item);
                }
            }
        }
        
        // Sort by risk weight (highest first)
        priorityList.sort((a, b) -> Integer.compare((Integer) b.get("riskWeight"), (Integer) a.get("riskWeight")));
        
        priorities.put("remediationPriorities", priorityList);
        priorities.put("totalIssues", priorityList.size());
        priorities.put("highPriorityCount", priorityList.stream().mapToInt(p -> 
            "HIGH".equals(p.get("priority")) ? 1 : 0).sum());
        
        return priorities;
    }

    public Map<String, Object> generateRiskMatrix() {
        List<AnalysisEntity> allAnalyses = analysisEntityRepository.findAll();
        Map<String, Object> matrix = new HashMap<>();
        
        // Risk matrix: Impact vs Likelihood
        Map<String, Map<String, Integer>> riskMatrix = new HashMap<>();
        riskMatrix.put("HIGH_IMPACT", Map.of("HIGH_LIKELIHOOD", 0, "MEDIUM_LIKELIHOOD", 0, "LOW_LIKELIHOOD", 0));
        riskMatrix.put("MEDIUM_IMPACT", Map.of("HIGH_LIKELIHOOD", 0, "MEDIUM_LIKELIHOOD", 0, "LOW_LIKELIHOOD", 0));
        riskMatrix.put("LOW_IMPACT", Map.of("HIGH_LIKELIHOOD", 0, "MEDIUM_LIKELIHOOD", 0, "LOW_LIKELIHOOD", 0));
        
        for (AnalysisEntity analysis : allAnalyses) {
            if (analysis.getFindingsDetails() != null) {
                // Simplified risk categorization
                int totalFindings = (analysis.getCriticalFindings() != null ? analysis.getCriticalFindings() : 0) +
                                  (analysis.getHighFindings() != null ? analysis.getHighFindings() : 0) +
                                  (analysis.getMediumFindings() != null ? analysis.getMediumFindings() : 0) +
                                  (analysis.getLowFindings() != null ? analysis.getLowFindings() : 0);
                
                String impact = totalFindings >= 5 ? "HIGH_IMPACT" : totalFindings >= 3 ? "MEDIUM_IMPACT" : "LOW_IMPACT";
                String likelihood = analysis.getCriticalFindings() != null && analysis.getCriticalFindings() > 0 ? "HIGH_LIKELIHOOD" :
                                   analysis.getHighFindings() != null && analysis.getHighFindings() > 0 ? "MEDIUM_LIKELIHOOD" : "LOW_LIKELIHOOD";
                
                Map<String, Integer> impactRow = riskMatrix.get(impact);
                impactRow.put(likelihood, impactRow.get(likelihood) + 1);
            }
        }
        
        matrix.put("riskMatrix", riskMatrix);
        matrix.put("totalAnalyses", allAnalyses.size());
        
        return matrix;
    }

    // Helper methods
    private int calculateRiskScore(int critical, int high, int medium, int low) {
        // Enhanced risk scoring algorithm with exponential weighting for critical issues
        double criticalWeight = critical > 0 ? Math.min(50, critical * 20 + Math.pow(critical, 1.5) * 5) : 0;
        double highWeight = high > 0 ? Math.min(30, high * 12 + Math.sqrt(high) * 3) : 0;
        double mediumWeight = medium > 0 ? Math.min(15, medium * 6 + Math.log(medium + 1) * 2) : 0;
        double lowWeight = low > 0 ? Math.min(8, low * 2 + Math.log(low + 1)) : 0;
        
        int totalScore = (int) (criticalWeight + highWeight + mediumWeight + lowWeight);
        
        // Apply banking industry multiplier for high-risk vulnerabilities
        if (critical > 0) {
            totalScore = Math.min(100, (int) (totalScore * 1.2)); // 20% penalty for any critical issues
        }
        
        return Math.min(100, totalScore); // Cap at 100
    }

    private String getRiskLevel(int score) {
        if (score >= 80) return "CRITICAL";
        if (score >= 60) return "HIGH";
        if (score >= 30) return "MEDIUM";
        return "LOW";
    }

    private String getRepositoryRiskLevel(Long repositoryId) {
        Map<String, Object> riskData = calculateRepositoryRisk(repositoryId);
        return (String) riskData.getOrDefault("riskLevel", "UNKNOWN");
    }

    private int calculateBankingComplianceScore(List<AnalysisEntity> analyses) {
        if (analyses.isEmpty()) return 100;
        
        int totalIssues = 0;
        for (AnalysisEntity analysis : analyses) {
            if (analysis.getFindingsDetails() != null) {
                String findings = analysis.getFindingsDetails();
                for (String riskType : BANKING_RISK_WEIGHTS.keySet()) {
                    if (findings.contains(riskType)) {
                        totalIssues += BANKING_RISK_WEIGHTS.get(riskType);
                    }
                }
            }
        }
        
        return Math.max(0, 100 - (totalIssues / analyses.size()));
    }

    private int calculateBankingRiskScore(AnalysisEntity analysis) {
        if (analysis.getFindingsDetails() == null) return 0;
        
        int bankingRisk = 0;
        String findings = analysis.getFindingsDetails();
        
        for (Map.Entry<String, Integer> entry : BANKING_RISK_WEIGHTS.entrySet()) {
            if (findings.contains("type=" + entry.getKey())) {
                bankingRisk += entry.getValue();
            }
        }
        
        return Math.min(100, bankingRisk);
    }

    private Map<String, String> calculateRegulatoryCompliance(AnalysisEntity analysis) {
        Map<String, String> compliance = new HashMap<>();
        
        if (analysis.getFindingsDetails() != null) {
            String findings = analysis.getFindingsDetails();
            
            compliance.put("PCI_DSS", findings.contains("SQL_INJECTION") || findings.contains("XSS_VULNERABILITY") ? 
                          "NON_COMPLIANT" : "COMPLIANT");
            compliance.put("SOX", findings.contains("HARDCODED_SECRET") || findings.contains("DEBUG_CODE") ? 
                          "NON_COMPLIANT" : "COMPLIANT");
            compliance.put("GDPR", findings.contains("HARDCODED_SECRET") || findings.contains("WEAK_CRYPTO") ? 
                          "NON_COMPLIANT" : "COMPLIANT");
        }
        
        return compliance;
    }

    private int calculateOwaspComplianceScore(Map<String, Integer> owaspCounts) {
        int totalIssues = owaspCounts.values().stream().mapToInt(Integer::intValue).sum();
        return Math.max(0, 100 - (totalIssues * 5)); // 5 points deducted per issue
    }

    private List<String> generateExecutiveRecommendations(Map<String, Object> overview, Map<String, Object> compliance) {
        List<String> recommendations = new ArrayList<>();
        
        int criticalFindings = (Integer) overview.get("criticalFindings");
        int bankingCompliance = (Integer) compliance.get("overallBankingCompliance");
        
        if (criticalFindings > 0) {
            recommendations.add("Address " + criticalFindings + " critical security vulnerabilities immediately");
        }
        
        if (bankingCompliance < 80) {
            recommendations.add("Improve banking regulatory compliance - currently at " + bankingCompliance + "%");
        }
        
        if ((Integer) overview.get("totalFindings") > 50) {
            recommendations.add("Implement automated security scanning in CI/CD pipeline");
        }
        
        recommendations.add("Schedule quarterly security reviews with development teams");
        recommendations.add("Consider implementing security training program");
        
        return recommendations;
    }

    private String getBankingImpact(String vulnerabilityType) {
        return switch (vulnerabilityType) {
            case "HARDCODED_SECRET" -> "Data breach, regulatory fines, customer trust loss";
            case "SQL_INJECTION" -> "Database compromise, financial data exposure";
            case "WEAK_CRYPTO" -> "Data decryption, compliance violations";
            case "XSS_VULNERABILITY" -> "Customer data theft, session hijacking";
            case "PATH_TRAVERSAL" -> "Unauthorized data access, system compromise";
            case "INSECURE_RANDOM" -> "Predictable tokens, authentication bypass";
            case "CSRF_VULNERABILITY" -> "Unauthorized transactions, account takeover";
            case "DEBUG_CODE" -> "Information disclosure, system intelligence gathering";
            default -> "Potential security risk requiring assessment";
        };
    }
}