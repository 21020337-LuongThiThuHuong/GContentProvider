package com.example.gcontentprovider

import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val onItemClick: (Contact) -> Unit,
    private val onMultiSelectModeChanged: (Boolean) -> Unit // Callback to notify MainActivity about multi-select mode
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private val contacts = mutableListOf<Contact>()
    private val selectedContacts = mutableSetOf<Contact>()
    var isMultiSelectMode = false
        private set

    fun setContacts(newContacts: List<Contact>) {
        val sortedContacts = newContacts.sortedBy { it.name }
        contacts.clear()
        contacts.addAll(sortedContacts)
        notifyDataSetChanged()
    }

    fun enterMultiSelectMode() {
        isMultiSelectMode = true
        onMultiSelectModeChanged(true)
        notifyDataSetChanged()
    }

    fun exitMultiSelectMode() {
        isMultiSelectMode = false
        selectedContacts.clear()
        onMultiSelectModeChanged(false)
        notifyDataSetChanged()
    }

    fun getSelectedContacts(): List<Contact> = selectedContacts.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        holder.nameTextView.text = contact.name
        holder.phoneTextView.text = contact.phone
        holder.avaImage.setColorFilter(colors.random(), PorterDuff.Mode.SRC_IN)

        holder.itemView.setOnClickListener {
            if (isMultiSelectMode) {
                toggleSelection(contact)
            } else {
                onItemClick(contact)
            }
        }

        holder.itemView.setOnLongClickListener {
            enterMultiSelectMode()
            toggleSelection(contact)
            true
        }

        holder.checkbox.visibility = if (isMultiSelectMode) {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.checkbox.isChecked = selectedContacts.contains(contact)

        holder.checkbox.setOnClickListener {
            toggleSelection(contact)
        }
    }

    private fun toggleSelection(contact: Contact) {
        if (selectedContacts.contains(contact)) {
            selectedContacts.remove(contact)
        } else {
            selectedContacts.add(contact)
        }
        val index = contacts.indexOf(contact)
        notifyItemChanged(index)

        Log.e(
            "ContactAdapter",
            "Selected contacts: ${selectedContacts.joinToString { it.name }}"
        )
    }

    override fun getItemCount(): Int = contacts.size

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name)
        val phoneTextView: TextView = itemView.findViewById(R.id.phone)
        val avaImage: ImageView = itemView.findViewById(R.id.avatar)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    companion object {
        private val colors = arrayOf(
            Color.parseColor("#FF7F27"),
            Color.parseColor("#3580BB"),
            Color.parseColor("#7F82BB"),
            Color.parseColor("#377D22"),
            Color.parseColor("#F08784"),
            Color.parseColor("#8E403A"),
            Color.parseColor("#F5D547"),
            Color.parseColor("#F2A884")
        )
    }
}
