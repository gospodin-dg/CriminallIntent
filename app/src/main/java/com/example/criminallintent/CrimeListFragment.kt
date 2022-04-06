package com.example.criminallintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.time.DayOfWeek
import java.time.Month
import java.time.MonthDay
import java.time.Year

private const val TAG: String = "CrimeListFragment"

class CrimeListFragment: Fragment() {

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Log.d(TAG, "Create ${crimeListViewModel.crimesList.size} crimes")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        recyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        updateUI()
        return view
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    private inner class CrimeHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime
        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val crimeSolvedImageView: ImageView = itemView.findViewById(R.id.is_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime){
            val dayOfWeek = DateFormat.DAY_OF_WEEK_FIELD.toString()
            val month: Month
            val dayOfMonthDay: MonthDay
            val year: Year
            this.crime = crime
            titleTextView.text = this.crime.title
            //dateTextView.text = this.crime.date.toString()
            dateTextView.text = dayOfWeek


            crimeSolvedImageView.visibility = if (crime.isSolved) {
                ImageView.VISIBLE
            } else {
                ImageView.GONE
            }
        }

        override fun onClick(v: View?) {
            Toast.makeText(context, "Преступление - ${crime.title}!!!", Toast.LENGTH_SHORT).show()
        }


    }

    private inner class CrimeAdapter(val crimes: List<Crime>): RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemCount(): Int {
            return crimes.size
        }

    }

    private fun updateUI() {
        val crimes = crimeListViewModel.crimes
        val adapter = CrimeAdapter(crimes)
        recyclerView.adapter = adapter
    }

}