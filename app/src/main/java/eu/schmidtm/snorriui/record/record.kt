package eu.schmidtm.snorriui.record

import android.os.Parcel
import android.os.Parcelable
import eu.schmidtm.snorriui.crypto.argon2hash
import eu.schmidtm.snorriui.crypto.decrypt
import eu.schmidtm.snorriui.crypto.md5
import eu.schmidtm.snorriui.hexdump
import eu.schmidtm.snorriui.storage.Storage
import java.io.File

/*

Magic    : 4c 4f 4b 49     :  4 : "LOKI" Magic Header
Version  : 00 00 00 01     :  4 : v1 - Protocol/Format version
Counter  : 00 00 00 17     :  4 : Version number of Masterpassword
Size     : 00 00 00 00     :  4 : Size of encrypted payload
MD5 Hash : 16 Bytes        : 16 : md5sum of encrypted payload


Data     : .........       : Variable-sized encrypted payload

*/

enum class Field { TITLE, ACCOUNT, PASSWORD, TAGS, URL, NOTES }

data class Locations(
    val title: Int,
    val account: Int,
    val password: Int,
    val tags: Int,
    val url: Int,
    val notes: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(title)
        parcel.writeInt(account)
        parcel.writeInt(password)
        parcel.writeInt(tags)
        parcel.writeInt(url)
        parcel.writeInt(notes)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun isNotEmpty(): Boolean {
        if (title != -1) { return true }
        if (account != -1) { return true }
        if (password != -1) { return true }
        if (tags != -1) { return true }
        if (url != -1) { return true }
        if (notes != -1) { return true }

        return false
    }

    fun getSize(): Int {
        var retVal: Int = 0

        if (title != -1) { retVal++ }
        if (account != -1) { retVal++ }
        if (password != -1) { retVal++ }
        if (tags != -1) { retVal++ }
        if (url != -1) { retVal++ }
        if (notes != -1) { retVal++ }

        return retVal
    }

    companion object CREATOR : Parcelable.Creator<Locations> {
        override fun createFromParcel(parcel: Parcel): Locations {
            return Locations(parcel)
        }

        override fun newArray(size: Int): Array<Locations?> {
            return arrayOfNulls(size)
        }
    }
}

data class Header(val version: Int, val counter: Int, val size: Int, val md5: ByteArray)

fun Storage.Record.calculateFieldMd5(): ByteArray {
    return md5(
            this.title.toByteArray() +
                    this.account.toByteArray() +
                    this.password.toByteArray() +
                    this.tagsList.joinToString(separator = ", ").toByteArray() +
                    this.url.toByteArray() +
                    this.notes.toByteArray()
    )
}

fun Storage.Record.search(text: String): Locations {
    return searchSensitive(text, true)
}

fun Storage.Record.searchSensitive(text: String, ignoreCase: Boolean): Locations {
    return Locations(
            this.title.indexOf(text, 0, ignoreCase),
            this.account.indexOf(text, 0, ignoreCase),
            this.password.indexOf(text, 0, ignoreCase),
            this.tagsList.joinToString(separator = ", ").indexOf(text, 0, ignoreCase),
            this.url.indexOf(text, 0, ignoreCase),
            this.notes.indexOf(text, 0, ignoreCase))
}

fun Storage.Record.verify(): Boolean {
    val fieldMd5 = this.calculateFieldMd5()

    println("Field MD5    : ${hexdump(fieldMd5)}")

    if (this.md5.toUpperCase() != hexdump(fieldMd5)) {
        return false
    }
    return true
}

fun Storage.Record.print() {
    println("Magic          : ${this.magic}")
    println("Rec MD5        : ${this.md5}")

    println("\n")
    println("Title          : ${this.title}")
    println("Account        : ${this.account}")
    println("Password       : ${this.password}")

//    for (tag in this.tagsList) {
//        println("Tag            : ${tag}")
//    }

    val tagsString = this.tagsList.joinToString(separator = ", ")

    println("Tags           : $tagsString")

    println("Url            : ${this.url}")
    println("Notes          : ${this.notes}")
}

fun loadRecord(bytes: ByteArray, password: String): Pair<Header, Storage.Record>? {
    val key = argon2hash(password)
    return loadRecord(bytes, key)
}

fun loadRecord(path: String, password: String): Pair<Header, Storage.Record>? {
    val bytes = File(path).readBytes()
    val key = argon2hash(password)

    return loadRecord(bytes, key)
}

fun loadRecord(path: String, key: ByteArray): Pair<Header, Storage.Record>? {
    val bytes = File(path).readBytes()
    return loadRecord(bytes, key)
}

fun loadRecord(bytes: ByteArray, key: ByteArray): Pair<Header, Storage.Record>? {

    val result = loadRecord(bytes)

    if (result == null) {
        println("Lokifile corrupt.")
        return null
    }

    val header = result.first
    val payload = result.second

    val decrypted = decrypt(payload, key)
    val record = Storage.Record.parseFrom(decrypted)

    println("Payload        : ${hexdump(payload)}")
    println("Key            : ${hexdump(key)}")
    println("Decrypted data : ${hexdump(decrypted)}")
    println("Decrypted str  : ${String(decrypted)}")

    return Pair(header, record)
}

fun loadRecord(bytes: ByteArray): Pair<Header, ByteArray>? {
    val magic = bytes.sliceArray(0..3)
    val version = java.nio.ByteBuffer.wrap(bytes.sliceArray(4..7)).int
    val counter = java.nio.ByteBuffer.wrap(bytes.sliceArray(8..11)).int
    val size = java.nio.ByteBuffer.wrap(bytes.sliceArray(12..15)).int
    val md5 = bytes.sliceArray(16..31)
    val payload = bytes.sliceArray(32..bytes.lastIndex)

    val calculatedMD5 = md5(payload)

    if (!(md5 contentEquals calculatedMD5)) {
        println("MD5 sum does not match. Given: ${hexdump(md5)}, calculated MD5 : ${hexdump(calculatedMD5)}")
        return null
    }

    if (!verifyMagic(magic)) {
        println("Magic value is wrong: ${String(magic)}")
        return null
    }

    val header = Header(version, counter, size, md5)
    return Pair(header, payload)
}

fun verifyMagic(magic: ByteArray): Boolean {
    if (magic.size != 4) {
        return false
    }

    if (magic[0] != 0x4c.toByte() ||
            magic[1] != 0x4f.toByte() ||
            magic[2] != 0x4b.toByte() ||
            magic[3] != 0x49.toByte()) {
        return false
    }
    return true
}
