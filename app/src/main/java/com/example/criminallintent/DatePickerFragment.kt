package com.example.criminallintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val DATE = "date"

class DatePickerFragment : DialogFragment() {

    interface Callbacks {
        fun onDateSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()
        val date = arguments?.getSerializable(DATE) as Date
        calendar.time = date
        val immutabledHours = calendar.get(Calendar.HOUR)
        val immutabledMinutes = calendar.get(Calendar.MINUTE)
        val immutabledSecondes = calendar.get(Calendar.SECOND)
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog.OnDateSetListener { _:DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val resultDate = GregorianCalendar(year, month, dayOfMonth, immutabledHours, immutabledMinutes, immutabledSecondes).time
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDayOfMonth
        )
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }


}