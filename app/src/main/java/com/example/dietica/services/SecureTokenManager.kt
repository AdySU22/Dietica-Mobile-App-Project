import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureTokenManager(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("com.example.myapp.SecurePreferences", Context.MODE_PRIVATE)

    private val encryptionKey: SecretKey = generateEncryptionKey()

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_LENGTH = 128
        private const val IV_LENGTH = 12
    }

    // Save encrypted token into SharedPreferences
    fun saveToken(token: String) {
        val encryptedToken = encryptToken(token)
        with(sharedPref.edit()) {
            putString("secure_token", encryptedToken)
            apply()
        }
    }

    // Retrieve the decrypted token from SharedPreferences
    fun getToken(): String? {
        val encryptedToken = sharedPref.getString("secure_token", null) ?: return null
        return decryptToken(encryptedToken)
    }

    // Encrypt token using AES/GCM/NoPadding
    private fun encryptToken(token: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_LENGTH)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, GCMParameterSpec(TAG_LENGTH, iv))

        val encryptedData = cipher.doFinal(token.toByteArray())
        val ivAndEncryptedData = iv + encryptedData
        return Base64.encodeToString(ivAndEncryptedData, Base64.DEFAULT)
    }

    // Decrypt token using AES/GCM/NoPadding
    private fun decryptToken(encryptedToken: String): String? {
        val ivAndEncryptedData = Base64.decode(encryptedToken, Base64.DEFAULT)
        val iv = ivAndEncryptedData.copyOfRange(0, IV_LENGTH)
        val encryptedData = ivAndEncryptedData.copyOfRange(IV_LENGTH, ivAndEncryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, GCMParameterSpec(TAG_LENGTH, iv))

        return try {
            val decryptedData = cipher.doFinal(encryptedData)
            String(decryptedData)
        } catch (e: Exception) {
            null
        }
    }

    // Generate a secure AES encryption key
    private fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256) // 256-bit key
        return keyGenerator.generateKey()
    }
}
