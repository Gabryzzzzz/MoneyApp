package com.example.moneyapp_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_log_reg.*

class LogOrRegAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_log_reg)

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            val intent = Intent(this, MainMenuAct::class.java);
            startActivity(intent);
            finish();
        }




        btn_registrati_act.setOnClickListener{
            startActivity(Intent(this, RegistrazioneAct::class.java))
            finish()
        }

        btn_login_act.setOnClickListener{
            startActivity(Intent(this, LoginAct::class.java))
            finish()
        }
    }
}