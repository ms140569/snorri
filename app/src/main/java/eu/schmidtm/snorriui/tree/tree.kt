package eu.schmidtm.snorriui.tree

import android.os.Parcel
import android.os.Parcelable
import eu.schmidtm.snorriui.crypto.argon2hash
import eu.schmidtm.snorriui.readAndPrintLokiFile
import eu.schmidtm.snorriui.record.Locations
import eu.schmidtm.snorriui.record.loadRecord
import eu.schmidtm.snorriui.record.search
import java.io.File
import java.nio.file.Paths

typealias TreeMap = MutableMap<String, Node<TreeEntry>>
typealias TreeSearchResult = ArrayList<Pair<Node<TreeEntry>, Locations>>

fun printTree(basePath: String, tree: Node<TreeEntry>) {
    walkTree(basePath, tree, ::readAndPrintLokiFile)
}

fun walkTree(basePath: String, tree: Node<TreeEntry>, fn: (String, String) -> Unit) {
    println(tree.data.title)
    tree.children?.forEach { walkTree0(basePath, it, 1, fn) }
}

fun walkTree0(basePath: String, tree: Node<TreeEntry>, shift: Int, fn: (String, String) -> Unit) {
    println("  ".repeat(shift) + tree.data.title)

    if (tree.nodetype == NodeType.RECORD) {
        fn(basePath + tree.data.filepath, "matthias")
    } else if (tree.nodetype == NodeType.FOLDER) {
        tree.children?.forEach { walkTree0(basePath, it, shift + 1, fn) }
    }
}

fun searchTree(basePath: String, tree: Node<TreeEntry>, password: String, text: String): TreeSearchResult {
    return searchTree(basePath, tree, argon2hash(password), text)
}

fun searchTree(basePath: String, tree: Node<TreeEntry>, key: ByteArray, text: String): TreeSearchResult {
    val result: TreeSearchResult = ArrayList()
    tree.children?.forEach { searchTree0(basePath, it, key, text, result) }
    return result
}

fun searchTree0(basePath: String, node: Node<TreeEntry>, key: ByteArray, text: String, result: TreeSearchResult) {
    if (node.nodetype == NodeType.RECORD) {
        val rec = loadRecord(basePath + "/" + node.data.filepath, key)
        val record = rec?.second
        val searchResult = record?.search(text)

        if (searchResult != null && searchResult.isNotEmpty()) {
            result += Pair(node, searchResult)
        }
    } else if (node.nodetype == NodeType.FOLDER) {
        node.children?.forEach { searchTree0(basePath, it, key, text, result) }
    }
}

fun buildTree(localPath: String): Node<TreeEntry> {

    val tm: TreeMap = mutableMapOf()
    val tree = FilePathNode(NodeType.ROOT, "Root", mutableListOf<Node<TreeEntry>>())

    fun lookupParent(tm: TreeMap, path: String): Node<TreeEntry> {
        if (path == localPath) {
            return tree
            }

        val dir = Paths.get(path).parent ?: return tree

        val dirStr = dir.toString()

        for ((key, value) in tm) {
            println("Dir: $dirStr, Key: $key")
            if (key == dirStr) {
                return value
            }
        }

        return tree
    }

    // do the walk
    File(localPath).walkTopDown().forEach {
        val path = it.toString().removePrefix("$localPath/")

        if (!path.startsWith(".git")) {

            val parent = lookupParent(tm, path)

            if (path != localPath) {
                if (it.isDirectory) {
                    val node = FilePathNode(NodeType.FOLDER, path, mutableListOf())
                    parent.children?.add(node)
                    tm[path] = node
                } else {
                    if (path.toLowerCase().endsWith(".loki")) {
                        val node = FilePathNode(NodeType.RECORD, path, mutableListOf())
                        parent.children?.add(node)
                    }
                }
                println("E: $path, Dir: ${it.isDirectory}")
            }
        }
    }

    return tree
}

/*
Display Folder:

Internet (Title)
Gruppe - 97 Einträge (Subtitle)

Display Record:

Amazon.de (Title)
gonzo@example.com (Subtitle)
*/

enum class NodeType { ROOT, RECORD, FOLDER }

interface TreeEntry : Parcelable {
    val title: String
    val subtitle: String
    val filepath: String
}

data class FilePathTreeEntry(val nt: NodeType, val path: String, val numberOfChildren: Int) : TreeEntry {
    override val title: String
        get() {
            return when (nt) {
                NodeType.ROOT -> "Root"
                NodeType.RECORD -> File(path).nameWithoutExtension.removeSuffix(".loki")
                else -> File(path).nameWithoutExtension
            }
        }

    override val subtitle: String
        get() {
            if (nt == NodeType.RECORD) {
                return "" // load record here ...
            } else {
                return "$numberOfChildren Einträge"
            }
        }

    override val filepath: String
        get() = path

    constructor(parcel: Parcel) : this(
            NodeType.valueOf(parcel.readString()),
            parcel.readString(),
            parcel.readInt())

    override fun toString(): String {
        return "$nt : path='$path'"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nt.name)
        parcel.writeString(path)
        parcel.writeInt(numberOfChildren)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FilePathTreeEntry> {
        override fun createFromParcel(parcel: Parcel): FilePathTreeEntry {
            return FilePathTreeEntry(parcel)
        }

        override fun newArray(size: Int): Array<FilePathTreeEntry?> {
            return arrayOfNulls(size)
        }
    }
}

interface Node<T> : Parcelable {
    val nodetype: NodeType
    val children: MutableList<Node<T>>?
    val data: T
}

data class FilePathNode(val nt: NodeType, val path: String, val childs: MutableList<Node<TreeEntry>>) : Node<TreeEntry> {
    override val nodetype: NodeType
        get() = nt
    override val children: MutableList<Node<TreeEntry>>?
        get() = if (nt == NodeType.RECORD) null else childs
    override val data: TreeEntry
        get() = FilePathTreeEntry(nt, path, if (nt == NodeType.RECORD) 0 else childs.size)

    @Suppress("UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
            NodeType.valueOf(parcel.readString()),
            parcel.readString(),
            parcel.createTypedArrayList(FilePathNode.CREATOR) as MutableList<Node<TreeEntry>>)

    override fun toString(): String {
        return "$nt : '$path'" + if (nt == NodeType.RECORD) "" else ", childs=$childs)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nt.name)
        parcel.writeString(path)
        parcel.writeTypedList(childs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FilePathNode> {
        override fun createFromParcel(parcel: Parcel): FilePathNode {
            return FilePathNode(parcel)
        }

        override fun newArray(size: Int): Array<FilePathNode?> {
            return arrayOfNulls(size)
        }
    }
}
