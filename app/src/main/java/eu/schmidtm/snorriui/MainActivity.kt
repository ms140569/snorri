package eu.schmidtm.snorriui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import eu.schmidtm.snorriui.git.GitRepo
import eu.schmidtm.snorriui.git.fetchOrClone
import eu.schmidtm.snorriui.preferences.Preferences
import eu.schmidtm.snorriui.preferences.PreferencesActivity
import eu.schmidtm.snorriui.tree.Node
import eu.schmidtm.snorriui.tree.TreeEntry
import eu.schmidtm.snorriui.tree.buildTree
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber

val prefs: Preferences by lazy {
    MainActivity.prefs!!
}

class MainActivity : AppCompatActivity(), StatusTarget {

    companion object {
        var prefs: Preferences? = null
        var masterKey: ByteArray? = null
        var localStorage: String = ""
        var rootNode: Node<TreeEntry>? = null
    }

    override fun informUser(msg: String) {
        doAsync {
            uiThread {
                textView.text = msg
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Preferences(applicationContext)
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        localStorage = filesDir.absolutePath

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        kiwiPicture.setOnClickListener {
            val ctx = this
            val repo = GitRepo(prefs!!.url, prefs!!.user, prefs!!.password)

            textView.text = "Fetching: ${repo.url}"

            doAsync {
                val msg = fetchOrClone(repo, localStorage, ctx)
                rootNode = buildTree(localStorage)

                uiThread {
                    Toast.makeText(applicationContext,
                            msg, Toast.LENGTH_LONG)
                            .show()

                            val intent = Intent(ctx, OneLevelListActivity::class.java)
                            intent.putExtra(Constants.BUNDLE_KEY_DISPLAY_LIST, prepareDisplayList(rootNode!!))
                            intent.putExtra(Constants.BUNDLE_KEY_TOOLBAR_TITLE, rootNode!!.data.title)
                            intent.putExtra(Constants.BUNDLE_KEY_HIGHLIGHT_SEARCHSTRING, "")
                            startActivity(intent)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, PreferencesActivity::class.java))
                true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
