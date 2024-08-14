package com.example.gcontentprovider

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gcontentprovider.R

class ContactAdapter(private val onItemClick: (Contact) -> Unit) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private val contacts = mutableListOf<Contact>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        // Handle contact name
        val displayName = if (contact.name.isNotEmpty()) contact.name else "(Chưa đặt tên)"
        holder.nameTextView.text = displayName

        // Handle phone number
        val displayPhone = if (contact.phone.isNotEmpty()) contact.phone else "null"
        holder.phoneTextView.text = displayPhone

        val colors = arrayOf(
            Color.parseColor("#FF7F27"),
            Color.parseColor("#3580BB"),
            Color.parseColor("#7F82BB"),
            Color.parseColor("#377D22"),
            Color.parseColor("#F08784"),
            Color.parseColor("#8E403A"),
            Color.parseColor("#F5D547"),
            Color.parseColor("#F2A884")
        )

        // Pick a random color from the array
        val randomColor = colors.random()

        // Set the random color tint to the avatar
        holder.avaImage.setColorFilter(randomColor, PorterDuff.Mode.SRC_IN)

        holder.itemView.setOnClickListener {
            onItemClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun setContacts(contacts: List<Contact>) {
        val sortedContacts = contacts.sortedBy { it.name }

        this.contacts.clear()
        this.contacts.addAll(sortedContacts)
        notifyDataSetChanged()
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name)
        val phoneTextView: TextView = itemView.findViewById(R.id.phone)
        val avaImage: ImageView = itemView.findViewById(R.id.avatar)
    }
}
