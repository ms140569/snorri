package eu.schmidtm.snorriui

import eu.schmidtm.snorriui.crypto.argon2hash
import org.junit.Assert.assertTrue
import org.junit.Test

/*
From Go version:

Key: a21d7e76f4f75b3ef848a317d34327b12189eb8a35a19415f5db651710ebdab5, password: x
Key: ebdc65d61884851549cfb2fb07ac0dc40807d35765b7d20b4e429e3b83629171, password: m
Key: 67e4373326946d16da8f60cd2e219749065ab1a250e7be891b781e23143d34a0, password: mm
Key: 57251d83dfb062118ca2b25e9a6529898f20dd867cf7fa95dc0cedfa07e3de45, password: Matthias

 */

fun String.hexStringToByteArray() = ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

class ArgonTest {
    @Test
    fun foreignImplementationTest() {
        val map = mapOf(
                /* ktlint-disable no-multi-spaces */
                "x"        to "a21d7e76f4f75b3ef848a317d34327b12189eb8a35a19415f5db651710ebdab5",
                "m"        to "ebdc65d61884851549cfb2fb07ac0dc40807d35765b7d20b4e429e3b83629171",
                "mm"       to "67e4373326946d16da8f60cd2e219749065ab1a250e7be891b781e23143d34a0",
                "Matthias" to "57251d83dfb062118ca2b25e9a6529898f20dd867cf7fa95dc0cedfa07e3de45")
                /* ktlint-enable no-multi-spaces */

        for ((key, value) in map) {
            println("$key = $value")
            assertTrue(argon2hash(key) contentEquals value.hexStringToByteArray())
            }
    }
}
