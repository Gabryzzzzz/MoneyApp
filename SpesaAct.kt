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
import com.example.moneyapp_2.Global.dataPrimoSaldoMillisecondi
import com.example.moneyapp_2.Global.dbUtente
import kotlinx.android.synthetic.main.activity_spesa.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SpesaAct : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_spesa)

        val currentDate: String =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) //formato data
        val currentTime =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) //formato ora

        et_data_spesa.setText(currentDate)
        et_ora_spesa.setText(currentTime)

        cb_spesa.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
            {
                ll_dataora_spesa.visibility = View.VISIBLE
            }
            else{
                ll_dataora_spesa.visibility = View.GONE
            }
        }


        et_data_spesa.setOnClickListener {
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
                        et_data_spesa.setText("$dayOfMonth-${newMonth}-$year")
                        if (dayOfMonth < 10) {
                            val newDay = "0$dayOfMonth"
                            et_data_spesa.setText("$newDay-${newMonth}-$year")
                        }
                    } else {
                        et_data_spesa.setText("$dayOfMonth-${monthOfYear + 1}-$year")
                    }
                },
                year,
                month,
                day
            )
            dpd.datePicker.minDate = dataPrimoSaldoMillisecondi!!
            dpd.show()
        }

        et_ora_spesa.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                et_ora_spesa.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }

            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btn_inserisci_spesa.setOnClickListener {
            if (et_ammontare_spesa.text.toString().isNotEmpty() && checkIfDoubleWithTwoDecimals(
                    et_ammontare_spesa.text.toString().toCharArray()
                )
            ) {
                btn_inserisci_spesa.visibility = View.GONE
                pb_inserisci_spesa.visibility = View.VISIBLE

                var possible = false
                if (et_ammontare_spesa.text.toString().toDouble() < dbUtente.saldo.toString()
                        .toDouble()
                ) {
                    possible = true
                }
                if (possible) {

                    dbUtente.crono!!.add(
                        Transazioni(
                            et_data_spesa.text.toString(),
                            et_ora_spesa.text.toString(),
                            et_ammontare_spesa.text.toString().toDouble(),
                            et_causa_spesa.text.toString(),
                            false
                        )
                    )
                    dbUtente.saldo =
                        dbUtente.saldo?.minus(et_ammontare_spesa.text.toString().toDouble())

                    updateDB()

                    btn_inserisci_spesa.visibility = View.VISIBLE
                    pb_inserisci_spesa.visibility = View.GONE
                    Toast.makeText(this, "Spesa inserita", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Spesa troppo alta, non puoi spendere ciò che non hai...",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else if (!et_ammontare_spesa.text.toString().isDigitsOnly()) {
                Toast.makeText(
                    this,
                    "Il numero inserito non è un numero accettabile",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Inserisci tutti i dati", Toast.LENGTH_SHORT).show()
            }
        }

        btn_back_to_main_menu_from_spesa_act.setOnClickListener {
            finish()
        }
    }
}