package com.example.ale.medidas;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ale on 19/04/2017.
 */

public class OpcionesFragment extends PreferenceFragment {
    public String S11back = null;
    public String S11ref = null;
    public String S113std = null;
    private TCPClient mTcpClient;
    private int flg = 0;//flag para indicar si la lectura de datos ha sido enviada por el botón "background" (flg=1), "reference" (flg=2) o "3er estandar" (flg=3)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.opciones);
        //Añadimos una respuesta al clic para los objetos Preferences de la pantalla de Conf
        //Boton Background
        Preference back = findPreference("background");
        back.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "Reading Background", Toast.LENGTH_SHORT).show();
                flg = 1;
                new connectTask().execute(":CALC:DATA:SDAT?");
                return true;
            }
        });

        //Boton Reference
        Preference ref = findPreference("reference");
        ref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "Reading Reference", Toast.LENGTH_SHORT).show();
                flg = 2;
                new connectTask().execute(":CALC:DATA:SDAT?");
                return true;
            }
        });

        //Boton 3er Estandar de calibración
        Preference cal3 = findPreference("plex2mm");
        cal3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "Reading 3rd Standard", Toast.LENGTH_SHORT).show();
                flg = 3;
                new connectTask().execute(":CALC:DATA:SDAT?");
                return true;
            }
        });
    }

    public void lecturaS11(String msg) {
        //Guardamos la lectura en el campo S11xx para que sea accesible desde el exterior de esta clase
        //Guardamos la medida como un String dentro del archivo de las Sharedpreference
        String prf_key = null;
        switch (flg) {
            case 1:
                Log.e("OpcionesFragment", "alej: S11back");
                S11back = msg;
                prf_key="background";
                break;
            case 2:
                Log.e("OpcionesFragment", "alej: S11ref");
                S11ref = msg;
                prf_key="reference";
                break;
            case 3:
                Log.e("OpcionesFragment", "alej: S113std");
                S113std = msg;
                prf_key="plex2mm";
                break;
            default:
                Toast.makeText(getActivity(), "Error: Repeat Calibration!", Toast.LENGTH_SHORT).show();
                break;
        }
        //Guardamos los valores de Back, reference o 3erStandar en el archivo de preferencia
        if (msg!=null) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = pref.edit();//para guardar datos en el archivo de preferencias (invocamos al editor que permite guardar datos)
            editor.putString(prf_key, msg);//guardamos el valor del estandar leido
            editor.commit();
            //Toast.makeText(getActivity(), "Preferences: Guardado Estandar " + flg + " =  " + pref.getString(prf_key, "") + ",", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "Ok", Toast.LENGTH_SHORT).show();
        }
    }

    //Cliente v1: Se abre/cierra un socket en el envío de cada comando (si el comando es de request, se espera a la respuesta del VNA)
    public class connectTask extends AsyncTask<String, String, TCPClient> {
        @Override
        protected TCPClient doInBackground(String... message) {
            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            if (message.length == 1) { //Enviamos 1 comando
                mTcpClient.run(message[0]); //abrimos el socket de comunicacion con el servidor
            } else { //Varios comandos a las vez
                mTcpClient.run(message);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg = values[0];
            lecturaS11(msg);//Tras recibir un msg, llamamos a la función que lo procesa
        }
    }


}