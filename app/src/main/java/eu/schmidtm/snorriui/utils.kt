package eu.schmidtm.snorriui

import eu.schmidtm.snorriui.record.Header
import eu.schmidtm.snorriui.record.loadRecord
import eu.schmidtm.snorriui.record.print
import eu.schmidtm.snorriui.record.verify
import eu.schmidtm.snorriui.tree.Node
import eu.schmidtm.snorriui.tree.TreeEntry
import eu.schmidtm.snorriui.tree.TreeSearchResult

fun readAndPrintLokiFile(path: String, password: String) {
    val result = loadRecord(path, password)

    if (result == null) {
        println("Lokifile corrupt.")
        return
    }

    val header = result.first
    val rec = result.second

    header.print()
    rec.print()

    if (!rec.verify()) {
        println("Calculated md5 checksum over fields do not match with given checksum.")
    }
}

fun Header.print() {
    println("Version        : ${this.version}")
    println("Counter        : ${this.counter}")
    println("Size           : ${this.size}")
    println("MD5            : ${hexdump(this.md5)}")
}

fun hexdump(bytes: ByteArray): String {

    var output = ""

    for (b in bytes) {
        output += String.format("%02X", b)
    }
    return output
}

fun prepareDisplayList(node: Node<TreeEntry>): ArrayList<Node<TreeEntry>> {
    val values = ArrayList<Node<TreeEntry>>()
    node.children?.forEach { child -> values.add(child) }
    return ArrayList(values.sortedWith(compareBy { it.data.title.toLowerCase() }))
}

fun prepareSearchList(treeSearchResult: TreeSearchResult): ArrayList<Node<TreeEntry>> {
    val values = ArrayList<Node<TreeEntry>>()

    for ((node, _) in treeSearchResult) {
        values.add(node)
    }
    return ArrayList(values.sortedWith(compareBy { it.data.title.toLowerCase() }))
}
