package com.example.moneyapp_2

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.moneyapp_2.Global.dbUtente
import kotlinx.android.synthetic.main.activity_crono.*
import kotlinx.android.synthetic.main.popup_crono_change.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CronoAct : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("InflateParams", "SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_crono)

        val ctx = this
        pb_crono.visibility = View.VISIBLE

        val causa = ArrayList<String>()
        val importo = ArrayList<String>()
        val data = ArrayList<String>()
        val ora = ArrayList<String>()
        val pos = ArrayList<Int>()

        val list = findViewById<ListView>(R.id.lv_crono)


        for (i in 0 until dbUtente.crono!!.size) {
            causa.add(dbUtente.crono!![i].causa.toString())
            if (dbUtente.crono!![i].piuomeno.toString().toBoolean()) {
                importo.add("+${returnWithTwoDecimals(dbUtente.crono!![i].importo.toString().toCharArray())} €")
            } else {
                importo.add("-${returnWithTwoDecimals(dbUtente.crono!![i].importo.toString().toCharArray())} €")
            }
            data.add(dbUtente.crono!![i].data.toString())
            ora.add(dbUtente.crono!![i].ora.toString())
            pos.add(i)
        }

        val adapter = CustomAdapter(
            ctx,
            causa.toTypedArray(),
            importo.toTypedArray(),
            data.toTypedArray(),
            ora.toTypedArray()
        )

        pb_crono.visibility = View.GONE
        list.adapter = adapter

        val inflater: LayoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView = inflater.inflate(R.layout.popup_crono_change, null)

        var indice: Int = 0

        val popup = PopupWindow(
            newView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        lv_crono.setOnItemClickListener { parent, view, position, id ->
            popup.isFocusable = true
            popup.update()

            val etCausa = newView.findViewById<EditText>(R.id.et_popup_causa)
            val etImporto = newView.findViewById<EditText>(R.id.et_popup_importo)
            val etData = newView.findViewById<EditText>(R.id.et_popup_data)
            val etOra = newView.findViewById<EditText>(R.id.et_popup_ora)
            val rbEntrata = newView.findViewById<RadioButton>(R.id.rb_entrata)
            val rbSpesa = newView.findViewById<RadioButton>(R.id.rb_spesa)
            indice = position
            etCausa.setText(dbUtente.crono?.get(pos[position])?.causa.toString())
            etImporto.setText(dbUtente.crono?.get(pos[position])?.importo.toString())
            etData.setText(dbUtente.crono?.get(pos[position])?.data.toString())
            etOra.setText(dbUtente.crono?.get(pos[position])?.ora.toString())
            if (dbUtente.crono?.get(pos[position])?.piuomeno == true) {
                rbEntrata.isChecked = true
                rbSpesa.isChecked = false
            } else {
                rbEntrata.isChecked = false
                rbSpesa.isChecked = true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popup.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.BOTTOM
                popup.exitTransition = slideOut
            }

            if (!(this as Activity).isFinishing) {
                TransitionManager.beginDelayedTransition(tb_main_table)
                popup.showAtLocation(
                    tb_main_table, // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0
                ) // Y offset
            }
        }

        newView.btn_popup_annulla.setOnClickListener {
            popup.dismiss()
            /*Handler().postDelayed({
                finish()
                startActivity(intent);
            }, 100)*/
        }


        newView.btn_popup_salva.setOnClickListener {
            if (newView.et_popup_importo.text.toString()
                    .isNotEmpty() && checkIfDoubleWithTwoDecimals(
                    newView.et_popup_importo.text.toString().toCharArray()
                )
            ) {
                Log.d("Change", "Saldo finale ${dbUtente.saldo}")
                if (newView.et_popup_causa.text!!.isNotEmpty()) {
                    if (newView.et_popup_data.text!!.isNotEmpty()) {
                        if (newView.et_popup_ora.text!!.isNotEmpty()) {
                            if (newView.rb_entrata.isChecked && dbUtente.crono!![indice].piuomeno == false) {

                                dbUtente.saldo = dbUtente.saldo?.plus(dbUtente.crono!![indice].importo!!)
                                dbUtente.saldo = dbUtente.saldo?.plus(newView.et_popup_importo.text.toString().toDouble())
                            } else if(newView.rb_spesa.isChecked && dbUtente.crono!![indice].piuomeno == true) {

                                dbUtente.saldo = dbUtente.saldo?.minus(dbUtente.crono!![indice].importo!!)
                                dbUtente.saldo = dbUtente.saldo?.minus(newView.et_popup_importo.text.toString().toDouble())
                            } else if(newView.rb_spesa.isChecked && dbUtente.crono!![indice].piuomeno == false) {

                                dbUtente.saldo = dbUtente.saldo?.plus(dbUtente.crono!![indice].importo!!)
                                dbUtente.saldo = dbUtente.saldo?.minus(newView.et_popup_importo.text.toString().toDouble())
                            } else {
                                dbUtente.saldo = dbUtente.saldo?.minus(dbUtente.crono!![indice].importo!!)
                                dbUtente.saldo = dbUtente.saldo?.plus(newView.et_popup_importo.text.toString().toDouble())
                            }

                            val updateTransazione = Transazioni()
                            updateTransazione.importo = newView.et_popup_importo.text.toString().toDouble()
                            updateTransazione.causa = newView.et_popup_causa.text.toString()
                            updateTransazione.data = newView.et_popup_data.text.toString()
                            updateTransazione.ora = newView.et_popup_ora.text.toString()
                            updateTransazione.piuomeno = newView.rb_entrata.isChecked

                            dbUtente.crono!![pos[indice]] = updateTransazione

                            val causa = ArrayList<String>()
                            val importo = ArrayList<String>()
                            val data = ArrayList<String>()
                            val ora = ArrayList<String>()

                            for (i in 0 until dbUtente.crono!!.size) {
                                causa.add(dbUtente.crono!![i].causa.toString())
                                if (dbUtente.crono!![i].piuomeno.toString().toBoolean()) {
                                    importo.add("+${dbUtente.crono!![i].importo.toString()}")
                                } else {
                                    importo.add("-${dbUtente.crono!![i].importo.toString()}")
                                }
                                data.add(dbUtente.crono!![i].data.toString())
                                ora.add(dbUtente.crono!![i].ora.toString())
                            }

                            val adapter = CustomAdapter(
                                this,
                                causa.toTypedArray(),
                                importo.toTypedArray(),
                                data.toTypedArray(),
                                ora.toTypedArray()
                            )

                            val list = findViewById<ListView>(R.id.lv_crono)
                            list.adapter = adapter

                            Toast.makeText(this, "Cambiamenti effettuati", Toast.LENGTH_SHORT)
                                .show()

                            updateDB()
                            popup.dismiss()


                                Handler().postDelayed({
                                    finish();
                                    startActivity(intent)
                                }, 100)

                        } else {
                            Toast.makeText(this, "Inserisci un'ora", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this, "Inserisci una data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Inserisci una spesa", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "L'importo è vuoto o non accettabile", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        iv_back_to_main_menu_from_crono_act.setOnClickListener {
            finish()
        }


        newView.btn_popup_elimina.setOnClickListener {
            newView.ll_conferma_elimina.visibility = View.VISIBLE
            newView.btn_popup_elimina.visibility = View.GONE
        }

        newView.btn_popup_elimina_no.setOnClickListener {
            newView.ll_conferma_elimina.visibility = View.GONE
            newView.btn_popup_elimina.visibility = View.VISIBLE
        }

        newView.btn_popup_elimina_si.setOnClickListener {
            if (indice < dbUtente.crono!!.size-1) {
                if (dbUtente.crono?.get(indice)?.piuomeno == true) {
                    dbUtente.saldo = dbUtente.saldo?.minus(dbUtente.crono!![indice].importo!!)
                } else {
                    dbUtente.saldo = dbUtente.saldo?.plus(dbUtente.crono!![indice].importo!!)
                }
                Toast.makeText(this, "Il record è stato rimosso", Toast.LENGTH_SHORT).show()
                dbUtente.crono?.removeAt(indice)
                updateDB()
                popup.dismiss()
                Handler().postDelayed({
                    finish()
                    startActivity(intent)
                }, 100)
            } else {
                Toast.makeText(this, "Non puoi eliminare il primo record", Toast.LENGTH_SHORT)
                    .show()
            }
        }



        newView.et_popup_data.setOnClickListener {

            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                if(monthOfYear<10)
                {
                    val newMonth = "0${monthOfYear+1}"
                    newView.et_popup_data.setText("$dayOfMonth-${newMonth}-$year")
                    if(dayOfMonth<10)
                    {
                        val newDay = "0$dayOfMonth"
                        newView.et_popup_data.setText("$newDay-${newMonth}-$year")
                    }
                }
                else{
                    newView.et_popup_data.setText("$dayOfMonth-${monthOfYear+1}-$year")
                }
            }, year, month, day).show()

        }

        newView.et_popup_ora.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                newView.et_popup_ora.setText(SimpleDateFormat("HH:mm").format(cal.time))
            }

            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
    }
}




internal class CustomAdapter
    (
    private val context: Activity,
    private val causa: Array<String>,
    private val importo: Array<String>,
    private val data: Array<String>,
    private val ora: Array<String>
) :
    ArrayAdapter<String?>(context, R.layout.custom_list, causa) {
    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.custom_list, null, true)
        val causaCrono = rowView.findViewById<View>(R.id.tv_causa_crono) as TextView
        val importoCrono = rowView.findViewById<View>(R.id.tv_importo_crono) as TextView
        val dataCrono = rowView.findViewById<View>(R.id.tv_data_crono) as TextView
        val oraCrono = rowView.findViewById<View>(R.id.tv_ora_crono) as TextView
        causaCrono.text = causa[position]
        importoCrono.text = importo[position]
        dataCrono.text = data[position]
        oraCrono.text = ora[position]
        return rowView
    }
}