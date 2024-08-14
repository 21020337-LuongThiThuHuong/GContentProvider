package com.example.gcontentprovider

import android.content.ContentValues
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditContactActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var saveButton: Button
    private var contactId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contact)

        // Enable edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameEditText = findViewById(R.id.name)
        phoneEditText = findViewById(R.id.phone)
        saveButton = findViewById(R.id.save_button)

        val contact = intent.getSerializableExtra("contact") as? Contact
        if (contact != null) {
            nameEditText.setText(contact.name)
            phoneEditText.setText(contact.phone)
            contactId = contact.id // Get ID from contact
        }

        saveButton.setOnClickListener {
            saveContact()
        }
    }

    private fun saveContact() {
        val name = nameEditText.text.toString()
        val phone = phoneEditText.text.toString()

        val contentResolver = contentResolver

        if (name.isEmpty() && phone.isEmpty()) {
            Toast.makeText(this, "Name and phone cannot both be empty", Toast.LENGTH_SHORT).show()
            return // Do not proceed further
        }

        if (contactId != null) {
            // Get RAW_CONTACT_ID from DATA_ID
            val rawContactId = getRawContactId(contactId!!)

            if (rawContactId != null) {
                // Update name
                val nameValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId) // Sử dụng RAW_CONTACT_ID
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                }
                val nameUpdated = contentResolver.update(
                    ContactsContract.Data.CONTENT_URI,
                    nameValues,
                    "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(rawContactId.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                )
                // Update phone number
                val phoneValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId) // Sử dụng RAW_CONTACT_ID
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                }
                val phoneUpdated = contentResolver.update(
                    ContactsContract.Data.CONTENT_URI,
                    phoneValues,
                    "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(rawContactId.toString(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                )

                if (nameUpdated == 0 && phoneUpdated == 0) {
                    Toast.makeText(this, "Failed to update contact", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle the case where RAW_CONTACT_ID is not found
                Toast.makeText(this, "Failed to find RAW_CONTACT_ID", Toast.LENGTH_SHORT).show()
            }

        } else {
            // Create new contact
            val newValues = ContentValues().apply {
                put(ContactsContract.RawContacts.ACCOUNT_TYPE, null as String?)
                put(ContactsContract.RawContacts.ACCOUNT_NAME, null as String?)
            }
            val rawContactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, newValues)
            val newRawContactId = rawContactUri?.lastPathSegment?.toLongOrNull() ?: return

            // Add name
            val nameValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, newRawContactId)
                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            }
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

            // Add phone number
            val phoneValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, newRawContactId)
                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            }
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)
        }

        setResult(RESULT_OK)
        Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun getRawContactId(dataId: Long): Long? {
        val projection = arrayOf(ContactsContract.Data.RAW_CONTACT_ID)
        val selection = "${ContactsContract.Data._ID} =?"
        val selectionArgs = arrayOf(dataId.toString())
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow(ContactsContract.Data.RAW_CONTACT_ID))
            }
        }
        return null
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}