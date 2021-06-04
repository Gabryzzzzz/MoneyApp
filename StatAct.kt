package com.example.moneyapp_2

import android.content.Context
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.example.moneyapp_2.Global.dbUtente
import io.data2viz.charts.chart.Chart
import io.data2viz.charts.chart.chart
import io.data2viz.charts.chart.discrete
import io.data2viz.charts.chart.mark.area
import io.data2viz.charts.chart.quantitative
import io.data2viz.charts.config.ChartBasicConfig
import io.data2viz.charts.config.ChartConfig
import io.data2viz.charts.config.configs.greenConfig
import io.data2viz.charts.config.configs.lightConfig
import io.data2viz.geom.Size
import io.data2viz.viz.VizContainerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val vizSize = 500.0
data class SaldoVar(var date: String? = null, var value: Double? = null)
var saldoDates = ArrayList<SaldoVar>()

class CanadaChart(context: Context) : VizContainerView(context) { //questa classe definisce i valori che il grafico mostrerà
    private val chart: Chart<SaldoVar> = chart(saldoDates) {
        size = Size(vizSize, vizSize)
        title = "Andamento saldo giorno per giorno"
        setBackgroundColor(WHITE)
        val date = discrete({ domain.date })
        val value = quantitative({ domain.value }) {
            name = "Soldi"
        }
        area(date, value)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        chart.size = Size(vizSize, vizSize * h / w)
    }
}

class StatAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        //setContentView(R.layout.activity_stat)
        saldoDates = ArrayList()
        generateGraph()
        setContentView(CanadaChart(this))

        //iv_back_to_main_menu_from_stat_act.setOnClickListener { finish() }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun extDateFromCrono(): ArrayList<String> {
        val dateSingle = ArrayList<String>()
        for (i in 0 until dbUtente.crono!!.size) {
            dateSingle.add(dbUtente.crono!![i].data.toString())
        }
        Log.d("Stat", dateSingle.distinct().toString())
        return dateSingle.distinct() as ArrayList<String>
    }

    private fun generateGraph() {
        var saldoGiorno = SaldoVar()
        var saldo = dbUtente.saldo
        val singleDate = extDateFromCrono()
        for (i in 0 until singleDate.size) {
            saldoGiorno.date = singleDate[i]
            saldoGiorno.value = saldo
            saldoDates.add(saldoGiorno)
            saldoGiorno = SaldoVar()
            for (j in 0 until dbUtente.crono!!.size) {
                if (singleDate[i] == dbUtente.crono!![j].data) {
                    if (saldo != null) {
                        if (dbUtente.crono!![j].piuomeno == true) {
                            val temp = dbUtente.crono!![j].importo!!
                            saldo -= temp
                        } else {
                            val temp = dbUtente.crono!![j].importo!!
                            saldo += temp
                        }
                    }
                }
            }
        }
        while(true) {
            if (saldoDates.size > 10) {
                saldoDates.removeAt(saldoDates.size - 1)
            }
            else
            {
                break
            }
        }
        saldoDates.reverse()
        Log.d("Stat", saldoDates.toString())
    }
}