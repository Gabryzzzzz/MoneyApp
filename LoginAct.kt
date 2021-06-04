package com.example.moneyapp_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

lateinit var auth: FirebaseAuth

class LoginAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        back_to_log_reg.setOnClickListener {
            startActivity(Intent(this, LogOrRegAct::class.java))
            finish()
        }

        btn_loggato.setOnClickListener { //Esegue codice quando si clicca sul bottone di login
            if (tv_email_login.text.toString().isNotEmpty() && tv_password_login.text.toString().isNotEmpty())
            { //Controlla che il campo Email e il campo password non siano vuoti
                pb_login.visibility = View.VISIBLE //Mostro una barra di caricamento
                btn_loggato.visibility = View.GONE //Mentre eseguo il codice disattivo il bottone per impedirne il riuso
                auth.signInWithEmailAndPassword(
                    tv_email_login.text.toString(),
                    tv_password_login.text.toString()
                ) //Attraverso la funzione signInWithEmailAndPassword(email,password) eseguo l'accesso
                    .addOnCompleteListener(this) { task -> //Aggiunge un listner che controlla se riesce signIn....)
                        if (task.isSuccessful) { //Se la funzione riesce esegue il codice sottostante
                            startActivity(Intent(this, MainMenuAct::class.java))
                            finish() //Avvio l'attività principale e termine la seguente
                        } else {
                            Toast.makeText(
                                baseContext, "Autenticazione fallita.",
                                Toast.LENGTH_SHORT
                            ).show() //Se il login non riesce mostra un Toast di negazione
                            pb_login.visibility = View.GONE //Mostro di nuovo il botto
                            btn_loggato.visibility = View.VISIBLE //Nascondo la barra di caricamento
                        }
                    }
            } else { //Se i dati inseriti non sono accettabili mostra un Toast
                Toast.makeText(
                    baseContext, "Dati errati",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}