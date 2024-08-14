package com.example.gcontentprovider

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gcontentprovider.databinding.ActivityMainBinding
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactAdapter = ContactAdapter(
            onItemClick = { contact ->
                if (!contactAdapter.isMultiSelectMode) {
                    val intent = Intent(this, EditContactActivity::class.java).apply {
                        putExtra("contact", contact)
                    }
                    startActivityForResult(intent, REQUEST_CODE_EDIT_CONTACT)
                }
            },
            onMultiSelectModeChanged = { isMultiSelectMode ->
                binding.deleteLayout.visibility = if (isMultiSelectMode) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }
        )

        binding.recyclerView.adapter = contactAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.deleteLayout.setOnClickListener {
            val selectedSize = contactAdapter.getSelectedContacts().size

            if (selectedSize > 0) {
                AlertDialog.Builder(this).apply {
                    setTitle("Xác nhận xóa")
                    setMessage("Bạn có chắc chắn muốn xóa $selectedSize liên hệ?")
                    setPositiveButton("Xóa") { _, _ ->
                        deleteSelectedContacts()
                    }
                    setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }
                    create().show()
                }
            } else {
                Toast.makeText(this, "Không có liên hệ nào được chọn", Toast.LENGTH_SHORT).show()
            }
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, EditContactActivity::class.java)
            intent.putExtra("contact", Contact(null, "", ""))
            startActivityForResult(intent, REQUEST_CODE_ADD_CONTACT)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        } else {
            loadContacts()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ADD_CONTACT, REQUEST_CODE_EDIT_CONTACT -> {
                    loadContacts()
                }
            }
        }
    }

    private fun deleteSelectedContacts() {
        val selectedContacts = contactAdapter.getSelectedContacts()
        if (selectedContacts.isNotEmpty()) {
            selectedContacts.forEach { contact ->
                val contactId = contact.id
                contactId?.let { id ->
                    val uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, id)
                    val rowsDeleted = contentResolver.delete(uri, null, null)

                    if (rowsDeleted > 0) {
                        setResult(RESULT_OK)
                    } else {
                    }
                } ?: run {
                    Toast.makeText(this, "No contact to delete", Toast.LENGTH_SHORT).show()
                }
            }
            contactAdapter.exitMultiSelectMode()
            loadContacts()
            Toast.makeText(this, "Contacts deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No contacts selected", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadContacts() {
        val contacts = mutableListOf<Contact>()
        val contentResolver = contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add(Contact(id, name, phone))
            }
        }

        contactAdapter.setContacts(contacts)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_CONTACTS] == true &&
                permissions[Manifest.permission.WRITE_CONTACTS] == true) {
                loadContacts()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    companion object {
        private const val REQUEST_CODE_ADD_CONTACT = 1
        private const val REQUEST_CODE_EDIT_CONTACT = 2
    }
}

data class Contact(
    val id: Long?,
    val name: String,
    val phone: String,
) : Serializable
