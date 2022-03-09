package com.example.ale.medidas;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;


public class PantallaConf extends AppCompatActivity {
    private OpcionesFragment opcfgr; //hacemos visible el handle desde dentro de los métodos de esta clase!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pantalla_conf); // Para asociar a esta actividad su layout
        opcfgr=new OpcionesFragment();
        //Sustituye el layout de la actividad por el del fragment que hemos definido
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new OpcionesFragment()).commit();
        getFragmentManager().beginTransaction().replace(android.R.id.content, opcfgr).commit();
        getSupportActionBar().hide();//escondemos la action bar en esta actividad (usaremos el botón hacia atrás del móvil)
        //Activa el botón en la ActionBar para volver hacia atrás
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //Sobreescribe el metodo q se ejecuta al pulsar un icono de la ActionBar mostrada en esta actividad
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //clic en el boton de ir para atrás!
                // API 5+ solution
                Toast.makeText(getApplicationContext(), "alej: S11back = "+ opcfgr.S11back, Toast.LENGTH_SHORT).show();
                //onBackPressed(); //volvemos a la Actividad principal
                //finish(); //aún sin probar

                //volvemos a la Actividad principal mediante un intent
//                Intent i=new Intent(this, MainActivity.class);
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//            if (hasFocus) {
//                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
//            }
//        }
//    }
}
