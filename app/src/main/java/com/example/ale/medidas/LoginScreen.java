package com.example.ale.medidas;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginScreen extends AppCompatActivity {
    private int cnt = 1;//max num de login antes de cerrar la app
    EditText user, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String LOGTAG = "onCreate";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        //Botones de la pantalla de login
        user = (EditText) findViewById(R.id.usuario);
        pass = (EditText) findViewById(R.id.contrasena);
        Button loginbtn = (Button) findViewById(R.id.btn_login);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int maxcnt=3;
                if (cnt < maxcnt) {//Tres intentos de login erroneo antes de cerrarse
                    boolean ok = login();
                    if (ok == false) {
                        Toast.makeText(getApplicationContext(), "Quedan "+(maxcnt-cnt)+" intentos!", Toast.LENGTH_SHORT).show();
                        cnt += 1;
                    }
                    Log.e(LOGTAG, "alej: cnt = " + cnt);
                } else {
                    finish();//cerramos la app
                }

            }
        });
    }

    //Funcion que comprueba el login
    public boolean login() {
        String LOGTAG = "login";
        boolean ok = false;
        String usuario = user.getText().toString();
        String password = pass.getText().toString();
        Log.e(LOGTAG, "alej: [usuario, pass] = [" + usuario + ", " + password + "]");
        if (usuario.equals("admin") && password.equals("admin")) {
            ok = true;
            Intent intent = new Intent(this, MainActivity.class); // Abrimos Activity secundaria que hace uso del fragment XML como su propia UI
            startActivity(intent);
        }
        return ok;
    }
}
