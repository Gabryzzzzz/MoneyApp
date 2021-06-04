package com.example.moneyapp_2

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.moneyapp_2.Global.dataPrimoSaldoMillisecondi
import com.example.moneyapp_2.Global.database
import com.example.moneyapp_2.Global.dbUtente
import com.example.moneyapp_2.Global.logOut
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.math.MathUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_crono.*
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.nav_head.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

lateinit var utente: FirebaseUser
lateinit var toggle: ActionBarDrawerToggle

object Global {
    var dbUtente = User()
    lateinit var database: DatabaseReference //mi connetto al mio database
    var dataPrimoSaldoMillisecondi: Long? = null
    var logOut: Boolean = false
}

class MainMenuAct : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        utente = FirebaseAuth.getInstance().currentUser
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main_menu)
        ll_main_menu.visibility = View.GONE

        database = Firebase.database.reference

        pb_saldo_main.visibility = View.VISIBLE
        tv_saldo_main_menu.visibility = View.GONE


        val bottomSheetBehavior = BottomSheetBehavior.from(navigationView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomAppBar.setNavigationOnClickListener {
            if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                scrim.visibility = View.GONE
            }
            else{
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                scrim.visibility = View.VISIBLE
            }
        }


        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.dm_log_out -> {
                    val intent = Intent(applicationContext, SplashScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra("EXIT", true)
                    logOut = false
                    FirebaseAuth.getInstance().signOut()
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

        scrim.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            scrim.visibility = View.GONE
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val baseColor = Color.BLACK
                // 60% opacity
                val baseAlpha = ResourcesCompat.getFloat(resources, R.dimen.material_emphasis_medium)
                // Map slideOffset from [-1.0, 1.0] to [0.0, 1.0]
                val offset = (slideOffset - (-1f)) / (1f - (-1f)) * (1f - 0f) + 0f
                val alpha = MathUtils.lerp(0f, 255f, offset * baseAlpha).toInt()
                val color = Color.argb(alpha, baseColor.red, baseColor.green, baseColor.blue)
                scrim.setBackgroundColor(color)
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
        })


        val uid = utente.uid
        val rootRef = FirebaseDatabase.getInstance().reference
        val uidRef = rootRef.child("users").child(uid)
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        val currentDate: String =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) //formato data

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataListener = object : ValueEventListener {
                    @SuppressLint("SetTextI18n", "SimpleDateFormat")
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        dbUtente = dataSnapshot.getValue<User>()!!

                        if (dbUtente.crono?.size!! > 1) {
                            dbUtente.crono = dbUtente.crono!!.sortedWith(compareBy<Transazioni> {
                                LocalDate.parse(
                                    it.data,
                                    dateTimeFormatter
                                )
                            }.thenBy { it.ora }).reversed() as ArrayList<Transazioni>
                        }

                        var profitto = 0.00
                        for (i in 0 until dbUtente.crono!!.size) {
                            val dataPro = dbUtente.crono!![i].data
                            if (dataPro == currentDate) {
                                if (dbUtente.crono!![i].piuomeno == true) {
                                    profitto += dbUtente.crono!![i].importo!!
                                } else {
                                    profitto -= dbUtente.crono!![i].importo!!
                                }
                            }
                        }

                        when {
                            profitto > 0 -> {
                                tv_profitto_main.visibility = View.VISIBLE
                                tv_profitto_main.text =
                                    "+${returnWithTwoDecimals(profitto.toString().toCharArray())} €"
                                tv_profitto_main.setTextColor(Color.parseColor("#05eb18"))
                            }
                            profitto < 0 -> {
                                tv_profitto_main.visibility = View.VISIBLE
                                tv_profitto_main.text =
                                    "${returnWithTwoDecimals(profitto.toString().toCharArray())} €"
                                tv_profitto_main.setTextColor(Color.parseColor("#eb0505"))
                            }
                            else -> tv_profitto_main.visibility = View.GONE
                        }

                        tv_nome_utente_main.text = dbUtente.username

                        dbUtente.saldo =
                            returnWithTwoDecimals(
                                dbUtente.saldo.toString().toCharArray()
                            ).toDouble()

                        val dataPrimoSaldo: String? =
                            dbUtente.crono!![dbUtente.crono!!.size - 1].data

                        val sdf = SimpleDateFormat("dd-MM-yyyy")
                        try {
                            val mDate = sdf.parse(dataPrimoSaldo)
                            dataPrimoSaldoMillisecondi = mDate.time - 31556952000
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }

                        if (!checkIfDoubleWithTwoDecimals(
                                dbUtente.saldo.toString().toCharArray()
                            )
                        ) {
                            dbUtente.saldo =
                                returnWithTwoDecimals(
                                    dbUtente.saldo.toString().toCharArray()
                                ).toDouble()
                        }

                        tv_saldo_main_menu.visibility = View.VISIBLE
                        tv_saldo_main_menu.text = "${dbUtente.saldo.toString()} €"
                        val causa = ArrayList<String>()
                        val importo = ArrayList<String>()
                        val data = ArrayList<String>()
                        val ora = ArrayList<String>()
                        for (i in 0 until dbUtente.crono!!.size) {
                            causa.add(dbUtente.crono!![i].causa.toString())
                            if (dbUtente.crono!![i].piuomeno.toString().toBoolean()) {
                                importo.add(
                                    "+${
                                        returnWithTwoDecimals(
                                            dbUtente.crono!![i].importo.toString().toCharArray()
                                        )
                                    } €"
                                )
                            } else {
                                importo.add(
                                    "-${
                                        returnWithTwoDecimals(
                                            dbUtente.crono!![i].importo.toString().toCharArray()
                                        )
                                    } €"
                                )
                            }
                            data.add(dbUtente.crono!![i].data.toString())
                            ora.add(dbUtente.crono!![i].ora.toString())
                        }
                        val adapter = CustomAdapter(
                            this@MainMenuAct,
                            causa.toTypedArray(),
                            importo.toTypedArray(),
                            data.toTypedArray(),
                            ora.toTypedArray()
                        )
                        val list = findViewById<ListView>(R.id.lv_main_menu)
                        list.adapter = adapter
                        Log.d("Prova", "Rilevato cambio di dati")

                        dm_user.text = dbUtente.username
                        dm_email.text = dbUtente.email

                        pb_saldo_main.visibility = View.GONE
                        tv_saldo_main_menu.visibility = View.VISIBLE
                        ll_main_menu.visibility = View.VISIBLE
                        bottomAppBar.visibility = View.VISIBLE
                    }
                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                }
                uidRef.addListenerForSingleValueEvent(dataListener)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        }
        database.addValueEventListener(listener)

        btn_entrate_act.setOnClickListener {
            startActivity(Intent(this, EntrateAct::class.java))
        }

        btn_spese_act.setOnClickListener {
            startActivity(Intent(this, SpesaAct::class.java))
        }

        btn_crono_act.setOnClickListener {
            startActivity(Intent(this, CronoAct::class.java))
        }

        btn_stat_act.setOnClickListener {
            if(dbUtente.crono?.size!! > 1){
                startActivity(Intent(this, StatAct::class.java))
            }
            else{
                Toast.makeText(this, "Hai bisogno di almeno 2 transazioni", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

fun checkIfDoubleWithTwoDecimals(numberToCheck: CharArray): Boolean {
    var isDouble = true
    var haveDot = false
    var twoAfterDot = 0
    var finalnumber: CharArray = charArrayOf()

    for (i in numberToCheck.indices) {
        if (numberToCheck[0] == '.' || numberToCheck[0] == ',') {
            isDouble = false
        }
        if (twoAfterDot < 3) {
            if (numberToCheck[i] == '.' || numberToCheck[i] == ',') {
                haveDot = true
            }
            if (haveDot) {
                twoAfterDot++
            }
            finalnumber += numberToCheck[i]
        } else {
            isDouble = false
        }
        if (!isDouble) {
            break
        }
    }
    return isDouble
}

fun returnWithTwoDecimals(numberToCheck: CharArray): String {
    var isDouble = true
    var haveDot = false
    var twoAfterDot = 0
    var finalnumber: CharArray = charArrayOf()
    for (i in numberToCheck.indices) {
        if (twoAfterDot < 3) {
            if (numberToCheck[i] == '.' || numberToCheck[i] == ',') {
                haveDot = true
            }
            if (haveDot) {
                twoAfterDot++
            }
            finalnumber += numberToCheck[i]
        } else {
            isDouble = false
        }
        if (!isDouble) {
            break
        }
    }
    if (twoAfterDot < 3) {
        finalnumber += '0'
    }
    var returned = String()
    for (i in finalnumber.indices) {
        returned += finalnumber[i]
    }
    return returned
}


fun writeNewUser(userId: String, dati: User) {
    val user = User(dati.crono, dati.email, dati.saldo, dati.username)
    database.child("users").child(userId).setValue(user)
}


@RequiresApi(Build.VERSION_CODES.O)
fun updateDB() {
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    if(dbUtente.crono?.size!! > 1) {
        dbUtente.crono = dbUtente.crono!!.sortedWith(compareBy<Transazioni> {
            LocalDate.parse(
                it.data,
                dateTimeFormatter
            )
        }.thenBy { it.ora }).reversed() as java.util.ArrayList<Transazioni>
    }
    writeNewUser(utente!!.uid, dbUtente)
}

