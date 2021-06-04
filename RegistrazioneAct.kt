package com.example.moneyapp_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_registrazione.*
import java.text.SimpleDateFormat
import java.util.*

class RegistrazioneAct : AppCompatActivity(), TextWatcher {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registrazione)

        //Sezione di creazione variabili ----------------------------------------------------------------------
        val email = findViewById<EditText>(R.id.tv_email_registrazione) //Campo email
        val password = findViewById<EditText>(R.id.tv_password_registrazione) //Campo password
        password.addTextChangedListener(this) //Listener che serve per indicare la sicurezza della password
        val passwordR = findViewById<EditText>(R.id.tv_password_registrazione_ripeti) //Campo ripeti password
        val currentDate: String =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) //formato data
        val currentTime =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) //formato ora
        val utente = findViewById<EditText>(R.id.tv_utente_inserisci) //Campo nome utente
        val saldo = findViewById<EditText>(R.id.tv_primo_saldo) //Campo saldo
        auth = FirebaseAuth.getInstance() //inizializzo l'istanza FireBaseAuth
        //Sezione di creazione variabili ----------------------------------------------------------------------

        back_to_login_img.setOnClickListener {
            startActivity(Intent(this, LogOrRegAct::class.java))
            finish()
        }


        btn_registrato.setOnClickListener {
            //Inizio controllo dati, Email deve essere una email, le password devono corrispondere e si devon inserire tutti i dati
            if (isEmailValid(email.text.toString())) { //Controllo se la email inserita è  una email valida
                if (password.text.toString().isNotEmpty() && passwordR.text.toString()
                        .isNotEmpty()
                ) { //Controllo se i campi passowrd contengono qualcosa
                    if ((password.text.toString() == passwordR.text.toString())) {
                        if (utente.text.toString() != "" && saldo.text.toString() != "") {
                            //Controllo se i campi Password e Ripeti password siano uguali
                            //In questa sezione viene eseguito il codice una volta verificati tutti i campi
                            pb_registrazione.visibility = View.VISIBLE //Mostro una barra di caricamento
                            btn_registrato.visibility = View.GONE //Mentre eseguo il codice disattivo il bottone per impedirne il riuso
                            auth.createUserWithEmailAndPassword(
                                email.text.toString(),
                                password.text.toString() //Attraverso la funzione di Firebase effettuo la registrazione del nuovo utente
                            ).addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) { //Se la registrazione è riuscita eseguo il codice sottostante
                                    Toast.makeText(this, "Utente creato", Toast.LENGTH_LONG).show()
                                    val transazione = Transazioni( //Creo un primo elemento per la tabella crono con il saldo iniziale
                                        currentDate,
                                        currentTime,
                                        saldo.text.toString().toDouble(),
                                        "Primo Saldo",
                                        true
                                    ) // Tutti i dati sono inseriti pensando al primo saldo come una prima transazione in entrata
                                    val crono: ArrayList<Transazioni> = ArrayList()
                                    crono.add(transazione) //creo la tabella crono ed inserisco il primo elemento
                                    val user = User( //creo la tabella user ed inserisco i datiu iniziali utente, email, saldo e crono
                                        crono,
                                        email.text.toString(),
                                        saldo.text.toString().toDouble(),
                                        utente.text.toString()
                                    )
                                    database = Firebase.database.reference //mi connetto al mio database
                                    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser //ottengo una instanza dell'utente corrente per ottenere l'UID
                                    writeNewUser(currentFirebaseUser.uid, user) //Creo l'utente chiamando la funzione WriteNewUser
                                    val intent = Intent(
                                        this,
                                        MainMenuAct::class.java)
                                    startActivity(intent) //Avvio l'attività di benvenuto e primo saldo
                                    finish()
                                } else { //Se qualcosa non riesce con la creazione mostra un Toast con l'errore
                                    Toast.makeText(
                                        this,
                                        "Utente non creato -> + ${task.exception}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Riempi i campi saldo e utente", Toast.LENGTH_LONG)
                                .show()
                        }
                    } else {
                        Toast.makeText(this, "Le password non corrispondono", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Uno dei due campi non è stato riempito",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else if (email.text.toString() == "") {
                Toast.makeText(this, "Inserire una email", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "L'email inserita non è una email", Toast.LENGTH_LONG).show()
            }
            //Fine controllo dati, Email deve essere una email, le password devono corrispondere e si devon inserire tutti i dati
            pb_registrazione.visibility = View.GONE //Nascondo il caricamento
            btn_registrato.visibility = View.VISIBLE //Mostro il bottone
        }

    }

    private fun isEmailValid(email: String): Boolean { //Questa funzione controlla l'autenticità di una email
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    private fun updatePasswordStrengthView(password: String) {

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar_pass_strenght)
        val strengthView = findViewById<ProgressBar>(R.id.password_strongness) as TextView
        if (TextView.VISIBLE != strengthView.visibility)
            return

        if (TextUtils.isEmpty(password)) {
            strengthView.text = ""
            progressBar.progress = 0
            return
        }

        val str = PasswordStrength.calculateStrength(password)
        strengthView.text = str.getText(this)
        strengthView.setTextColor(str.color)

        progressBar.progressDrawable.setColorFilter(
            str.color,
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        if (str.getText(this) == "Weak") {
            progressBar.progress = 25
        } else if (str.getText(this) == "Medium") {
            progressBar.progress = 50
        } else if (str.getText(this) == "Strong") {
            progressBar.progress = 75
        } else {
            progressBar.progress = 100
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        updatePasswordStrengthView(s.toString())
    }

    override fun afterTextChanged(s: Editable?) {}

    private fun writeNewUser(userId: String, dati: User) { //Questa funzione crea il database (che in realta è una struttura json)
        val user = User(dati.crono, dati.email, dati.saldo, dati.username)
        database.child("users").child(userId).setValue(user)
    }
}