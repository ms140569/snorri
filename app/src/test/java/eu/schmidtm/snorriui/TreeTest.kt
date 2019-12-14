package eu.schmidtm.snorriui

import eu.schmidtm.snorriui.tree.buildTree
import eu.schmidtm.snorriui.tree.printTree
import eu.schmidtm.snorriui.tree.searchTree
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TreeTest {
    @Test
    fun search_in_tree() {
        val basePath = "src/test/resources/loki"
        val tree = buildTree(basePath)
        assertNotNull(tree)

        val searchResult = searchTree(basePath, tree, "test1234", "gonzo")

        assertEquals(2, searchResult.size)
    }

    @Test
    fun print_tree() {
        val basePath = "src/test/resources/loki"
        val tree = buildTree(basePath)
        assertNotNull(tree)

        // val searchResult = printTree(basePath, tree)
    }
}
