package org.altbeacon.beaconreference

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.altbeacon.apiUsers.User

class UserAdapter(private val context: Context, private val dataSource: List<User>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return if (position in 0 until dataSource.size) {
            dataSource[position]
        } else {
            // Return a default User object
            User(-1, "", "", false, false, "", "")
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.user_adapter, parent, false)

        val usernameTextView = rowView.findViewById(R.id.usernameTextView) as TextView
        val emailTextView = rowView.findViewById(R.id.emailTextView) as TextView

        val user = getItem(position) as User

        usernameTextView.text = user.username
        emailTextView.text = user.email

        return rowView
    }
}