package eu.schmidtm.snorriui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import eu.schmidtm.snorriui.Constants.Companion.BUNDLE_KEY_HIGHLIGHT_SEARCHSTRING
import eu.schmidtm.snorriui.Constants.Companion.BUNDLE_KEY_KEY
import eu.schmidtm.snorriui.Constants.Companion.BUNDLE_KEY_RECORD
import eu.schmidtm.snorriui.MainActivity.Companion.localStorage
import eu.schmidtm.snorriui.record.Locations
import eu.schmidtm.snorriui.record.loadRecord
import eu.schmidtm.snorriui.record.search
import eu.schmidtm.snorriui.storage.Storage
import eu.schmidtm.snorriui.tree.Node
import eu.schmidtm.snorriui.tree.TreeEntry
import java.io.File
import kotlinx.android.synthetic.main.one_record.*
import timber.log.Timber

class RecordActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = intent.extras
        val filepath = b.getParcelable<Node<TreeEntry>>(BUNDLE_KEY_RECORD).data.filepath
        val key = b.getByteArray(BUNDLE_KEY_KEY)
        val searchString = b.getString(BUNDLE_KEY_HIGHLIGHT_SEARCHSTRING)

        setContentView(R.layout.one_record)

        setSupportActionBar(findViewById(R.id.record_toolbar))

        rec_password.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

        rec_ib_password.setOnClickListener {
            Timber.d("onClickListener of password button ...")
            if (rec_password.visibility == View.VISIBLE) {
                rec_password.visibility = View.INVISIBLE
            } else {
                rec_password.visibility = View.VISIBLE
            }
        }

        val file = File("$localStorage/$filepath")

        val data = file.readBytes()

        if (key.isEmpty()) {
            return
        }

        val rec: Storage.Record?
        try {
            rec = readRecord(data, key)

            if (rec != null) {

                supportActionBar?.title = rec.title
                supportActionBar?.setDisplayShowHomeEnabled(true)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                rec_account.text = rec.account
                rec_password.text = rec.password

                rec_password.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

                rec_tags.text = rec.tagsList.joinToString(separator = ", ")
                rec_url.text = rec.url
                rec_notes.text = rec.notes

                if (searchString.isNotEmpty()) {
                    // TODO: i am too dump, gotta search twice.
                    val highlights = rec.search(searchString)
                    markSearchResult(highlights, searchString.length)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(applicationContext,
                    "Wrong Password", Toast.LENGTH_LONG)
                    .show()
        }
    }

    private fun markSearchResult(locations: Locations, length: Int) {
        markTextView(R.id.record_toolbar, locations.title, length)
        markTextView(R.id.rec_account, locations.account, length)
        markTextView(R.id.rec_password, locations.password, length)
        markTextView(R.id.rec_tags, locations.tags, length)
        markTextView(R.id.rec_url, locations.url, length)
        markTextView(R.id.rec_notes, locations.notes, length)
    }

    private fun markTextView(id: Int, position: Int, length: Int) {
        if (position == -1) {
            return
        }

        val textView: TextView = findViewById(id)

        // from: https://stackoverflow.com/questions/4897349/android-coloring-part-of-a-string-using-textview-settext/4897412#4897412

        val sb = SpannableStringBuilder(textView.text)
        val fcs = ForegroundColorSpan(Color.rgb(255, 0, 0))
        val bss = StyleSpan(Typeface.BOLD)

        sb.setSpan(fcs, position, position + length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        sb.setSpan(bss, position, position + length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        textView.text = sb
    }

    private fun readRecord(data: ByteArray, key: ByteArray): Storage.Record? {
        val result = loadRecord(data, key)
        return result?.second
    }
}
