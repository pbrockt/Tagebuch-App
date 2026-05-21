package com.pbrockt.tagebuch.data.local.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verwaltet kryptografische Operationen für die lokale Datenspeicherung.
 *
 * Wie funktioniert die Verschlüsselung?
 *
 * 1. Android Keystore: Ein sicherer Hardware-Speicher im Gerät.
 *    Schlüssel werden dort gespeichert und können das Gerät nie verlassen.
 *    Auch wenn jemand die App debuggt, kommt er nicht an den Schlüssel ran.
 *
 * 2. AES-GCM: Ein modernes Verschlüsselungsverfahren.
 *    - AES (Advanced Encryption Standard): Der eigentliche Algorithmus
 *    - GCM (Galois/Counter Mode): Sorgt dafür dass Manipulation erkannt wird
 *    - 256-bit Schlüssellänge: 2^256 mögliche Schlüssel — nicht knackbar
 *
 * 3. Datenbankpasswort: Wird einmal zufällig generiert, verschlüsselt in
 *    EncryptedSharedPreferences gespeichert und bei jedem App-Start abgerufen.
 */
@Singleton
class CryptoManager @Inject constructor(@ApplicationContext private val context: Context) {

    // Zugriff auf den Android Keystore — der Hardware-Schlüsselspeicher
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    // Bezeichner für unseren Schlüssel im Keystore
    private val keyAlias = "tagebuch_db_key"

    /**
     * Verschlüsselte Einstellungen für das Datenbankpasswort.
     * lazy bedeutet: wird erst beim ersten Zugriff erstellt.
     */
    private val securePrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, "crypto_manager_prefs", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Holt oder erstellt den AES-Schlüssel im Android Keystore.
     *
     * Beim ersten Aufruf wird ein neuer Schlüssel generiert und im Keystore gespeichert.
     * Bei allen weiteren Aufrufen wird der bestehende Schlüssel zurückgegeben.
     */
    private fun getOrCreateKey(): SecretKey {
        return if (keyStore.containsAlias(keyAlias)) {
            // Schlüssel existiert bereits — einfach abrufen
            (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            // Neuen Schlüssel im Keystore generieren
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                init(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setUserAuthenticationRequired(false) // Kein extra Fingerabdruck pro Zugriff
                        .build()
                )
            }.generateKey()
        }
    }

    /**
     * Verschlüsselt beliebige Daten mit dem Keystore-Schlüssel.
     *
     * Das Ergebnis enthält den IV (Initialisierungsvektor) + den verschlüsselten Text.
     * Der IV ist zufällig und muss für die Entschlüsselung mitgespeichert werden.
     * Format: [12 Byte IV][verschlüsselte Daten]
     */
    fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv          // Zufälliger 12-Byte Initialisierungsvektor
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext      // IV + verschlüsselter Text zusammenfügen
    }

    /**
     * Entschlüsselt Daten die mit encrypt() verschlüsselt wurden.
     *
     * @param data Muss im Format [12 Byte IV][verschlüsselte Daten] sein
     */
    fun decrypt(data: ByteArray): ByteArray {
        val iv = data.copyOfRange(0, 12)          // Ersten 12 Bytes = IV
        val ciphertext = data.copyOfRange(12, data.size)  // Rest = verschlüsselte Daten
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    /**
     * Gibt das Passwort für die SQLCipher-Datenbank zurück.
     *
     * Beim ersten Aufruf wird ein zufälliges 32-Byte-Passwort generiert und
     * sicher in EncryptedSharedPreferences gespeichert.
     * Bei allen weiteren Aufrufen wird das gespeicherte Passwort zurückgegeben.
     *
     * Warum nicht den Keystore-Schlüssel direkt verwenden?
     * → Keystore-Schlüssel sind nicht exportierbar (das ist Absicht!).
     *   Man kann damit nur verschlüsseln/entschlüsseln, den Schlüssel selbst
     *   aber nie auslesen. SQLCipher braucht aber das Passwort als Bytes.
     */
    fun generateDbPassphrase(): ByteArray {
        val stored = securePrefs.getString("db_passphrase", null)
        if (stored != null) {
            // Bereits generiertes Passwort zurückgeben
            return Base64.decode(stored, Base64.DEFAULT)
        }
        // Neues zufälliges Passwort generieren (SecureRandom = kryptografisch sicher)
        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        securePrefs.edit()
            .putString("db_passphrase", Base64.encodeToString(passphrase, Base64.DEFAULT))
            .apply()
        return passphrase
    }
}
