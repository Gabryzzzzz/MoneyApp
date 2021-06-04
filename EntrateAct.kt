package com.example.moneyapp_2

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import com.example.moneyapp_2.Global.dbUtente
import kotlinx.android.synthetic.main.activity_entrate.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class EntrateAct : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_entrate)




        val currentDate: String =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) //formato data
        val currentTime =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) //formato ora


        et_data_entrata.setText(currentDate)
        et_ora_entrata.setText(currentTime)

        cb_entrata.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
            {
                ll_dataora_entrata.visibility = View.VISIBLE
            }
            else{
                ll_dataora_entrata.visibility = View.GONE
            }
        }

        et_data_entrata.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    // Display Selected date in textbox
                    if (monthOfYear < 10) {
                        val newMonth = "0${monthOfYear + 1}"
                        et_data_entrata.setText("$dayOfMonth-${newMonth}-$year")
                        if (dayOfMonth < 10) {
                            val newDay = "0$dayOfMonth"
                            et_data_entrata.setText("$newDay-${newMonth}-$year")
                        }
                    } else {
                        et_data_entrata.setText("$dayOfMonth-${monthOfYear + 1}-$year")
                    }
                },
                year,
                month,
                day
            ).show()
        }

        et_ora_entrata.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                et_ora_entrata.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }

            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()

        }


        btn_inserisci_entrata.setOnClickListener {

            if (et_ammontare_entrata.text.toString()
                    .isNotEmpty() && checkIfDoubleWithTwoDecimals(
                    et_ammontare_entrata.text.toString().toCharArray()
                )
            ) {
                btn_inserisci_entrata.visibility = View.GONE
                pb_inserisci_entrata.visibility = View.VISIBLE



                dbUtente.crono!!.add(
                    Transazioni(
                        currentDate,
                        currentTime,
                        et_ammontare_entrata.text.toString().toDouble(),
                        et_causa_entrata.text.toString(),
                        true
                    )
                )
                dbUtente.saldo =
                    dbUtente.saldo?.plus(et_ammontare_entrata.text.toString().toDouble())

                updateDB()

                btn_inserisci_entrata.visibility = View.VISIBLE
                pb_inserisci_entrata.visibility = View.GONE
                Toast.makeText(this, "Spesa inserita", Toast.LENGTH_SHORT).show()
                finish()

            } else if (!et_ammontare_entrata.text.toString().isDigitsOnly()) {
                Toast.makeText(
                    this,
                    "Il numero inserito non è un numero accettabile",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                Toast.makeText(this, "Inserisci tutti i dati", Toast.LENGTH_SHORT).show()
            }
        }
        btn_back_to_main_menu_from_entrate_act.setOnClickListener {
            finish()
        }
    }
}