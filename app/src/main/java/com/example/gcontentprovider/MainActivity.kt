package com.example.gcontentprovider

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gcontentprovider.databinding.ActivityMainBinding
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        contactAdapter = ContactAdapter { contact ->
            // Handle item click here
            val intent = Intent(this, EditContactActivity::class.java).apply {
                putExtra("contact", contact)
            }
            startActivityForResult(intent, REQUEST_CODE_EDIT_CONTACT)
        }
        recyclerView.adapter = contactAdapter

        // Check for permissions and load contacts
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS,
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                ),
            )
        } else {
            loadContacts()
        }

        addContact()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_CONTACT || requestCode == REQUEST_CODE_EDIT_CONTACT) {
            if (resultCode == RESULT_OK) {
                loadContacts()
            }
        }
    }

    private fun addContact() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, EditContactActivity::class.java)
            intent.putExtra("contact", Contact(null, "", ""))
            startActivityForResult(intent, REQUEST_CODE_ADD_CONTACT)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_CONTACTS] == true && permissions[Manifest.permission.WRITE_CONTACTS] == true) {
                loadContacts()
            } else {
                // Handle the case where permission was not granted
            }
        }

    private fun loadContacts() {
        val contacts = mutableListOf<Contact>()
        val contentResolver = contentResolver
        val cursor =
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null,
            )

        cursor?.use {
            while (cursor.moveToNext()) {
                val name =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                    )
                val phoneNumber =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    )
                val id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(ContactsContract.Data._ID)
                )
                contacts.add(Contact(id, name, phoneNumber))
            }
        }

        contactAdapter.setContacts(contacts)
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
