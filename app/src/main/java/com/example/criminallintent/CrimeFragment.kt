package com.example.criminallintent



import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
import androidx.lifecycle.Observer
import android.text.format.DateFormat
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat



private const val ARGS_BUNDLE_KEY = "crime_Id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 100
private const val REQUEST_TIME = 200
private const val REQUEST_SUSPECT = 300
private const val DATE_FORMAT = "EEE, MMM dd, yyyy"
private const val TIME_FORMAT = "hh:mm"
private const val REQUEST_CODE_READ_CONTACTS = 500
private var READ_CONTACTS_GRANTED = false

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var sendReportButton: Button
    private lateinit var chooseSuspectButton: Button
    private lateinit var callSuspectButton: Button
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARGS_BUNDLE_KEY) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.btn_crime_date) as Button
        timeButton = view.findViewById(R.id.btn_crime_time) as Button
        sendReportButton = view.findViewById(R.id.send_crime_report_btn) as Button
        chooseSuspectButton = view.findViewById(R.id.choose_suspect_btn) as Button
        callSuspectButton = view.findViewById(R.id.call_suspect_btn) as Button
        solvedCheckBox = view.findViewById(R.id.solved_label_checkbox) as CheckBox

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateFormatButton(crime.date)
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            chooseSuspectButton.text = crime.suspect
        }
    }


    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {  }
        }

        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }

        sendReportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooser = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooser)
            }
        }

        chooseSuspectButton.apply {
            val pickSuspectIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickSuspectIntent, REQUEST_SUSPECT)
            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickSuspectIntent, PackageManager.MATCH_DEFAULT_ONLY)
            isEnabled = resolvedActivity != null
        }

        callSuspectButton.apply {
            setOnClickListener {
                permissionsCheck()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARGS_BUNDLE_KEY, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun dateFormatButton(date: Date) {
        val dateFormatInstance = DateFormat.format(DATE_FORMAT, date)
        val timeFormatInstance = DateFormat.format(TIME_FORMAT, date)
        dateButton.text = dateFormatInstance
        timeButton.text = timeFormatInstance
    }

    private fun getCrimeReport(): String {
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_SUSPECT && data != null -> {
                val contactUri: Uri? = data.data
                val contactFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = contactUri?.let {
                    requireActivity().contentResolver.query(
                        it,
                        contactFields,
                        null,
                        null,
                        null
                    )
                }
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    chooseSuspectButton.text = suspect
                }
                cursor?.close()
            }
        }

    }


    private fun permissionsCheck() {
        if (crime.suspect.isNotEmpty()) {
            val hasReadContactPermission =
                context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.READ_CONTACTS
                    )
                }
            if (hasReadContactPermission == PackageManager.PERMISSION_GRANTED) {
                READ_CONTACTS_GRANTED = true
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    REQUEST_CODE_READ_CONTACTS
                )
            }
            if (READ_CONTACTS_GRANTED) {
                callSuspect()
            }
        } else {
            Toast.makeText(context, "Нужно выбрать подозреваемого!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                READ_CONTACTS_GRANTED = true
            }
        }
        if(READ_CONTACTS_GRANTED){
            callSuspect()
        }
        else{
            Toast.makeText(context, "Требуется установить разрешения", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("Range")
    private fun callSuspect() {
        var idSuspectContact: String? = " "
        val idCursor: Cursor? = requireActivity().contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
            null,
            "DISPLAY_NAME = '" + crime.suspect + "'",
            null,
            null
        )
        if (idCursor != null && idCursor.count != 0) {
            idCursor.moveToFirst()
            idSuspectContact = idCursor?.getString(idCursor.getColumnIndex(ContactsContract.Contacts._ID))
            val phoneCursor: Cursor? = requireActivity().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + idSuspectContact,
                null,
                null)
            if (phoneCursor != null && phoneCursor.count != 0) {
                phoneCursor.moveToFirst()
                val phoneNumber: String? = phoneCursor?.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val numberUri: Uri = Uri.parse("tel:$phoneNumber")
                val callSuspectIntent = Intent(Intent.ACTION_DIAL, numberUri)
                val packageManager: PackageManager = requireActivity().packageManager
                val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(callSuspectIntent, PackageManager.MATCH_DEFAULT_ONLY)
                if (resolvedActivity == null) {
                    callSuspectButton.isEnabled = false
                    Toast.makeText(context, "Устройство не поддерживает функцию звонка!!!", Toast.LENGTH_LONG).show()
                    return
                }
                startActivity(callSuspectIntent)
            } else {
                Toast.makeText(context, "Номер не известен!!!", Toast.LENGTH_LONG).show()
            }
            phoneCursor?.close()
        } else {
            Toast.makeText(context, "Подозреваемый отсутствует в базе контактов!!!", Toast.LENGTH_LONG).show()
        }
        idCursor?.close()
    }

}
