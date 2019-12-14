package eu.schmidtm.snorriui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import eu.schmidtm.snorriui.tree.Node
import eu.schmidtm.snorriui.tree.NodeType
import eu.schmidtm.snorriui.tree.TreeEntry
import kotlinx.android.synthetic.main.tree_item.view.*

class TreeEntryAdapter(context: Context, items: ArrayList<Node<TreeEntry>>) : ArrayAdapter<Node<TreeEntry>>(context, R.layout.tree_item, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val entry = getItem(position)

        val newView = LayoutInflater.from(context).inflate(R.layout.tree_item, parent, false)

        newView.item_title.text = entry.data.title
        newView.item_subtitle.text = entry.data.subtitle

        if (entry.nodetype == NodeType.FOLDER) {
            newView.item_icon.setImageResource(R.drawable.ic_action_name)

            // This ImageView is turned INVISIBLE in the layout,
            // turn it only on if needed.
            if ((entry.children?.size ?: 0) > 0) {
                newView.item_clickhint.visibility = View.VISIBLE
            }
        } else {
            newView.item_icon.setImageResource(R.drawable.ic_key)
        }

        newView.item_icon.scaleType = ImageView.ScaleType.CENTER
        return newView
    }
}
