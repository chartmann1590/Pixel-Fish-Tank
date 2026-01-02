package com.charles.virtualpet.fishtank.backup

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object BackupValidator {
    private const val SUPPORTED_SCHEMA_VERSION = 1
    private const val EXPECTED_APP_ID = "pixel-fish-tank"
    
    fun validate(envelope: BackupEnvelope): ValidationResult {
        // Check appId matches
        if (envelope.appId != EXPECTED_APP_ID) {
            return ValidationResult.Error(
                "Invalid backup file. This backup is for a different app (expected: $EXPECTED_APP_ID, found: ${envelope.appId})"
            )
        }
        
        // Check schemaVersion is supported
        if (envelope.schemaVersion > SUPPORTED_SCHEMA_VERSION) {
            return ValidationResult.Error(
                "Backup file version (${envelope.schemaVersion}) is newer than supported version ($SUPPORTED_SCHEMA_VERSION). Please update the app."
            )
        }
        
        // Additional validation can be added here (e.g., check data integrity)
        
        return ValidationResult.Success
    }
}

