package com.codeguardian.gitprocessor.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Component
@Slf4j
public class WebhookSecurityValidator {

    @Value("${webhook.github.secret:}")
    private String githubSecret;

    @Value("${webhook.gitlab.token:}")
    private String gitlabToken;

    /**
     * Validates GitHub webhook signature using HMAC-SHA256
     *
     * @param signature The signature from X-Hub-Signature-256 header
     * @param payload The raw request body
     * @return true if signature is valid, false otherwise
     */
    public boolean validateGitHubSignature(String signature, String payload) {
        // If no secret is configured, skip validation (for development)
        if (!StringUtils.hasText(githubSecret)) {
            log.warn("GitHub webhook secret not configured - skipping signature validation");
            return true;
        }

        if (!StringUtils.hasText(signature)) {
            log.warn("GitHub signature is missing");
            return false;
        }

        if (!signature.startsWith("sha256=")) {
            log.warn("Invalid GitHub signature format - should start with 'sha256='");
            return false;
        }

        try {
            String expectedSignature = calculateHmacSha256(payload, githubSecret);
            String receivedSignature = signature.substring(7); // Remove "sha256=" prefix

            boolean isValid = secureEquals(expectedSignature, receivedSignature);

            if (isValid) {
                log.debug("GitHub webhook signature validation successful");
            } else {
                log.warn("GitHub webhook signature validation failed");
                log.debug("Expected: {}, Received: {}", expectedSignature, receivedSignature);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating GitHub webhook signature", e);
            return false;
        }
    }

    /**
     * Validates GitLab webhook token
     *
     * @param token The token from X-Gitlab-Token header
     * @return true if token is valid, false otherwise
     */
    public boolean validateGitLabToken(String token) {
        // If no token is configured, skip validation (for development)
        if (!StringUtils.hasText(gitlabToken)) {
            log.warn("GitLab webhook token not configured - skipping token validation");
            return true;
        }

        if (!StringUtils.hasText(token)) {
            log.warn("GitLab token is missing");
            return false;
        }

        boolean isValid = secureEquals(gitlabToken, token);

        if (isValid) {
            log.debug("GitLab webhook token validation successful");
        } else {
            log.warn("GitLab webhook token validation failed");
        }

        return isValid;
    }

    /**
     * Validates BitBucket webhook signature (for future use)
     *
     * @param signature The signature from X-Hub-Signature header
     * @param payload The raw request body
     * @param secret The webhook secret
     * @return true if signature is valid, false otherwise
     */
    public boolean validateBitBucketSignature(String signature, String payload, String secret) {
        if (!StringUtils.hasText(secret)) {
            log.warn("BitBucket webhook secret not provided - skipping signature validation");
            return true;
        }

        if (!StringUtils.hasText(signature)) {
            log.warn("BitBucket signature is missing");
            return false;
        }

        if (!signature.startsWith("sha256=")) {
            log.warn("Invalid BitBucket signature format - should start with 'sha256='");
            return false;
        }

        try {
            String expectedSignature = calculateHmacSha256(payload, secret);
            String receivedSignature = signature.substring(7); // Remove "sha256=" prefix

            boolean isValid = secureEquals(expectedSignature, receivedSignature);

            if (isValid) {
                log.debug("BitBucket webhook signature validation successful");
            } else {
                log.warn("BitBucket webhook signature validation failed");
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating BitBucket webhook signature", e);
            return false;
        }
    }

    /**
     * Calculates HMAC-SHA256 signature for the given data and secret
     *
     * @param data The data to sign
     * @param secret The secret key
     * @return The HMAC-SHA256 signature as hex string
     * @throws NoSuchAlgorithmException if HMAC-SHA256 is not available
     * @throws InvalidKeyException if the secret key is invalid
     */
    private String calculateHmacSha256(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {

        if (data == null || secret == null) {
            throw new IllegalArgumentException("Data and secret cannot be null");
        }

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(hash);
    }

    /**
     * Converts byte array to hexadecimal string
     *
     * @param bytes The byte array
     * @return Hexadecimal string representation
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Secure string comparison to prevent timing attacks
     *
     * @param a First string
     * @param b Second string
     * @return true if strings are equal, false otherwise
     */
    private boolean secureEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }

        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }

        return result == 0;
    }

    /**
     * Validates webhook signature based on platform
     *
     * @param platform The git platform (GITHUB, GITLAB, BITBUCKET)
     * @param signature The signature from header
     * @param payload The raw request body
     * @param token Alternative token for platforms that use tokens
     * @return true if validation passes, false otherwise
     */
    public boolean validateWebhookSecurity(String platform, String signature, String payload, String token) {
        if (!StringUtils.hasText(platform)) {
            log.warn("Platform not specified for webhook validation");
            return false;
        }

        switch (platform.toUpperCase()) {
            case "GITHUB":
                return validateGitHubSignature(signature, payload);
            case "GITLAB":
                return validateGitLabToken(token);
            case "BITBUCKET":
                return validateBitBucketSignature(signature, payload, token);
            default:
                log.warn("Unsupported platform for webhook validation: {}", platform);
                return false;
        }
    }

    /**
     * Checks if webhook security is properly configured
     *
     * @return true if at least one platform security is configured
     */
    public boolean isSecurityConfigured() {
        boolean githubConfigured = StringUtils.hasText(githubSecret);
        boolean gitlabConfigured = StringUtils.hasText(gitlabToken);

        if (!githubConfigured && !gitlabConfigured) {
            log.warn("No webhook security configured - this is not recommended for production");
            return false;
        }

        log.info("Webhook security configuration: GitHub={}, GitLab={}",
                githubConfigured ? "configured" : "not configured",
                gitlabConfigured ? "configured" : "not configured");

        return true;
    }

    /**
     * Generates a secure random token for webhook configuration
     *
     * @param length The desired token length
     * @return A secure random token
     */
    public String generateSecureToken(int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] randomBytes = new byte[length];
            new java.security.SecureRandom().nextBytes(randomBytes);
            byte[] hash = digest.digest(randomBytes);

            return bytesToHex(hash).substring(0, Math.min(length, hash.length * 2));
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating secure token", e);
            throw new RuntimeException("Failed to generate secure token", e);
        }
    }
}