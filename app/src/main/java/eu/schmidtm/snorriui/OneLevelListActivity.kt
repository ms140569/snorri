package eu.schmidtm.snorriui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast
import eu.schmidtm.snorriui.Constants.Companion.BUNDLE_KEY_DISPLAY_LIST
import eu.schmidtm.snorriui.Constants.Companion.BUNDLE_KEY_HIGHLIGHT_SEARCHSTRING
import eu.schmidtm.snorriui.Constants.Companion.BUNDLE_KEY_KEY
import eu.schmidtm.snorriui.Constants.Companion.BUNDLE_KEY_RECORD
import eu.schmidtm.snorriui.Constants.Companion.BUNDLE_KEY_TOOLBAR_TITLE
import eu.schmidtm.snorriui.tree.Node
import eu.schmidtm.snorriui.tree.NodeType
import eu.schmidtm.snorriui.tree.TreeEntry
import eu.schmidtm.snorriui.tree.searchTree
import kotlinx.android.synthetic.main.activity_one_level_list.*

class OneLevelListActivity : AppCompatActivity(), PasswordDialogFragment.PasswordDialogListener {
    var item: Node<TreeEntry>? = null
    var searchString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = intent.extras
        val title = b.getString(BUNDLE_KEY_TOOLBAR_TITLE)
        val displaList = b.getParcelableArrayList<Node<TreeEntry>>(BUNDLE_KEY_DISPLAY_LIST)
        searchString = b.getString(BUNDLE_KEY_HIGHLIGHT_SEARCHSTRING)

        setContentView(R.layout.activity_one_level_list)
        setSupportActionBar(one_level_toolbar)

        supportActionBar!!.title = title

        accountList.adapter = TreeEntryAdapter(this, displaList)

        accountList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            @Suppress("UNCHECKED_CAST")
            item = accountList.getItemAtPosition(position) as Node<TreeEntry>

            item?.let { item ->
                if (item.nodetype == NodeType.FOLDER) {
                    val childSize = item.children?.size ?: 0

                    if (childSize > 0) {
                        intent.putExtra(BUNDLE_KEY_DISPLAY_LIST, prepareDisplayList(item))
                        intent.putExtra(BUNDLE_KEY_TOOLBAR_TITLE, item.data.title)
                        intent.putExtra(BUNDLE_KEY_HIGHLIGHT_SEARCHSTRING, item.data.title)
                        startActivity(intent)
                    }
                } else {
                    openRecord()
                }
            }
        }
    }

    private fun openRecord() {
        if (MainActivity.masterKey == null) {
            askPassword()
        } else {
            startRecordActivity(item!!, MainActivity.masterKey!!)
        }
    }

    private fun askPassword() {
        val alert = PasswordDialogFragment()
        alert.show(supportFragmentManager, "PasswordDialogFragment")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        startRecordActivity(this.item!!, MainActivity.masterKey!!)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }

    private fun startRecordActivity(item: Node<TreeEntry>, key: ByteArray) {
        val intent = Intent(this, RecordActivity::class.java)
        intent.putExtra(BUNDLE_KEY_RECORD, item)
        intent.putExtra(BUNDLE_KEY_KEY, key)

        intent.putExtra(BUNDLE_KEY_HIGHLIGHT_SEARCHSTRING, searchString)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_one_level, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val search = searchItem.actionView as android.support.v7.widget.SearchView

        search.setOnQueryTextListener(object : android.support.v7.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (MainActivity.masterKey != null && query!!.isNotEmpty()) {
                    Toast.makeText(applicationContext,
                            "Im searching for :$query", Toast.LENGTH_LONG)
                            .show()

                    val result = searchTree(MainActivity.localStorage, MainActivity.rootNode!!, MainActivity.masterKey!!, query)

                    if (result.isNotEmpty()) {
                        val intent = Intent(this@OneLevelListActivity, OneLevelListActivity::class.java)
                        intent.putExtra(BUNDLE_KEY_DISPLAY_LIST, prepareSearchList(result))
                        intent.putExtra(BUNDLE_KEY_TOOLBAR_TITLE, "Search: $query")

                        intent.putExtra(BUNDLE_KEY_HIGHLIGHT_SEARCHSTRING, query)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext,
                                "Sorry, found nothing...", Toast.LENGTH_LONG)
                                .show()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }
}
