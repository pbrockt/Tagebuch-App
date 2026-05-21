package com.pbrockt.tagebuch.data.local.crypto

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schlüsselableitung (Key Derivation) für WebDAV-Verschlüsselung.
 *
 * Problem: Der Nutzer gibt eine menschenlesbare Passphrase ein (z.B. "MeinGeheimesWort").
 * Für AES-Verschlüsselung brauchen wir aber exakt 256 Bit (32 Byte) Schlüsselmaterial
 * in gleichmäßiger Verteilung. Direkt die Passphrase zu verwenden wäre unsicher.
 *
 * Lösung: PBKDF2 (Password-Based Key Derivation Function 2)
 * → Wendet eine Hashfunktion (HMAC-SHA256) viele tausend Male auf die Passphrase an
 * → Das macht Brute-Force-Angriffe extrem langsam
 * → 100.000 Iterationen: Ein Angreifer braucht 100.000 mal so lange pro Versuch
 *
 * Die Daten werden clientseitig verschlüsselt, bevor sie auf den WebDAV-Server
 * hochgeladen werden. Der Server sieht nur unlesbaren Ciphertext.
 */
@Singleton
class PassphraseKdf @Inject constructor() {

    companion object {
        private const val ITERATIONS = 100_000  // Anzahl der Hash-Iterationen
        private const val KEY_LENGTH = 256       // Schlüssellänge in Bits
        // Statisches Salt: Verhindert dass gleiche Passphrasen gleiche Schlüssel erzeugen
        // (In einer echten Produktions-App sollte das Salt pro-Nutzer zufällig sein)
        private val SALT = "tagebuch_webdav_salt_v1".toByteArray()
    }

    /**
     * Leitet aus einer Passphrase einen 256-Bit Verschlüsselungsschlüssel ab.
     *
     * @param passphrase Die vom Nutzer eingegebene Passphrase
     * @return 32 Bytes Schlüsselmaterial für AES-256
     */
    fun deriveKey(passphrase: String): ByteArray {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), SALT, ITERATIONS, KEY_LENGTH)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Verschlüsselt Daten mit AES-GCM unter Verwendung des abgeleiteten Schlüssels.
     *
     * Format des Ergebnisses: [12 Byte IV][verschlüsselte Daten + GCM-Tag]
     *
     * @param data Die zu verschlüsselnden Daten (z.B. JSON eines Tagebucheintrags)
     * @param key  Der 32-Byte Schlüssel aus deriveKey()
     */
    fun encryptData(data: ByteArray, key: ByteArray): ByteArray {
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = javax.crypto.spec.SecretKeySpec(key, "AES")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data)
        return iv + ciphertext
    }

    /**
     * Entschlüsselt Daten die mit encryptData() verschlüsselt wurden.
     *
     * @param data Muss das Format [12 Byte IV][verschlüsselte Daten] haben
     * @param key  Derselbe Schlüssel der für die Verschlüsselung verwendet wurde
     */
    fun decryptData(data: ByteArray, key: ByteArray): ByteArray {
        val iv = data.copyOfRange(0, 12)
        val ciphertext = data.copyOfRange(12, data.size)
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = javax.crypto.spec.SecretKeySpec(key, "AES")
        cipher.init(
            javax.crypto.Cipher.DECRYPT_MODE,
            secretKey,
            javax.crypto.spec.GCMParameterSpec(128, iv)
        )
        return cipher.doFinal(ciphertext)
    }
}
