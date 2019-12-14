package eu.schmidtm.snorriui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import eu.schmidtm.snorriui.crypto.argon2hash

class PasswordDialogFragment : DialogFragment() {
    internal lateinit var listener: PasswordDialogListener

    interface PasswordDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val alert = AlertDialog.Builder(it)

            val edittext = EditText(it)

            edittext.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            edittext.transformationMethod = PasswordTransformationMethod.getInstance()

            alert.setMessage("Password")
            alert.setTitle("Enter your password")

            alert.setView(edittext)

            alert.setPositiveButton("Login") { _, _ ->
                val password = edittext.text.toString()

                if (password.isNotEmpty()) {
                    MainActivity.masterKey = argon2hash(password)
                    listener.onDialogPositiveClick(this)
                }
            }

            alert.setNegativeButton("Cancel") { _, _ ->
                listener.onDialogNegativeClick(this)
            }

            alert.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as PasswordDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement PasswordDialogListener"))
        }
    }
}
