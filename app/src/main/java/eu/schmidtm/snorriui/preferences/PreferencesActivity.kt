package eu.schmidtm.snorriui.preferences

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import eu.schmidtm.snorriui.R
import eu.schmidtm.snorriui.prefs
import kotlinx.android.synthetic.main.preferences.*

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.preferences)

        url.setText(prefs.url)
        user.setText(prefs.user)
        password.setText(prefs.password)

        btCancel.setOnClickListener {
            finish()
        }

        btSave.setOnClickListener {
            prefs.url = url.text.toString()
            prefs.user = user.text.toString()
            prefs.password = password.text.toString()

            finish()
        }
    }
}
