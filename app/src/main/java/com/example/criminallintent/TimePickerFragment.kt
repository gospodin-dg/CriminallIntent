package com.example.criminallintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.sql.Time
import java.util.*

private const val TIME = "time"

class TimePickerFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()
        val time = arguments?.getSerializable(TIME) as Date
        calendar.time = time
        val immutabledYear = calendar.get(Calendar.YEAR)
        val immutabledMonth = calendar.get(Calendar.MONTH)
        val immutabledDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val initialHours = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinutes = calendar.get(Calendar.MINUTE)
        val isPM = true

        val timeListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute ->
            val resultTime = GregorianCalendar(immutabledYear, immutabledMonth, immutabledDayOfMonth, hourOfDay, minute).time
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onTimeSelected(resultTime)
            }
        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHours,
            initialMinutes,
            isPM)
    }

    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(TIME, date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }

    interface Callbacks {
       fun onTimeSelected (date: Date)
    }
}