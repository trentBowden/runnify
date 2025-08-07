package dev.trentbowden.runnify.service.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.security.crypto.keygen.KeyGenerators
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct

@Service
class EncryptionService {

    @Value("\${app.encryption.password}")
    private lateinit var encryptionPassword: String

    private lateinit var textEncryptor: TextEncryptor

    @PostConstruct
    fun init() {
        if (encryptionPassword.isBlank()) {
            throw IllegalStateException("Encryption password must be configured")
        }

        // Use a fixed salt for deterministic encryption (needed for database queries)
        val salt = KeyGenerators.string().generateKey()
        textEncryptor = Encryptors.text(encryptionPassword, salt)
    }

    fun encrypt(plaintext: String?): String? {
        return plaintext?.let { textEncryptor.encrypt(it) }
    }

    fun decrypt(ciphertext: String?): String? {
        return ciphertext?.let { textEncryptor.decrypt(it) }
    }
}
