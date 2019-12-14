package eu.schmidtm.snorriui.crypto

import at.gadermaier.argon2.Argon2Factory
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

fun md5(s: ByteArray): ByteArray {
    try {
        // Create MD5 Hash
        val digest = java.security.MessageDigest.getInstance("MD5")
        digest.update(s)
        return digest.digest()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return ByteArray(0)
}

fun argon2hash(password: String): ByteArray {

    val salt: ByteArray = byteArrayOf(
            0x4F.toByte(), 0xEB.toByte(), 0x43.toByte(), 0xDB.toByte(), 0xBE.toByte(), 0xB0.toByte(), 0x43.toByte(), 0x5C.toByte(),
            0x86.toByte(), 0xC9.toByte(), 0x7F.toByte(), 0xA8.toByte(), 0x9B.toByte(), 0x4B.toByte(), 0xDB.toByte(), 0x0C.toByte())

    val argon2 = Argon2Factory.create()
            .setMemoryInKiB(32 * 1024)
            .setParallelism(4)

    argon2.hash(password.toByteArray(), salt)
    return argon2.output
}

fun decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val keySpec = SecretKeySpec(key, "AES")
    // We have to use the same nonce we used when encrypting the data
    // val gcmSpec = GCMParameterSpec(128, nonce)
    val gcmSpec = GCMParameterSpec(128, ciphertext.sliceArray(0..11))

    // Put the cipher in decrypt mode
    cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

    return cipher.doFinal(ciphertext.sliceArray(12..ciphertext.lastIndex))
}
