package com.example.ale.medidas;

import static com.example.ale.medidas.MathV.float2double;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

/*
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
*/

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.*;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

import fastandroid.neoncore.collection.FaCollection;

import static java.security.AccessController.getContext;

import org.apache.commons.math4.transform.FastFourierTransform;

/*
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
*/

public class MainActivity extends AppCompatActivity  {
    private TCPClient mTcpClient; //objeto que recivirá y enviará msg al servidor!
    private TCPClientv2 mTcpClientv2;
    private int conectado = 0;//monitoriza el estado de conexión al VNA
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private GraphView graph;
    private static double c = 0.3;//veloc luz lista para dividir por GHz y obtener metros: "distancia =c/fGHz" (m)
    private String S11;
    private MathDatos S11m = null;
    private MathDatos S11back = null;
    private MathDatos S11ref = null;
    private MathDatos S11std3 = null;
    private MathDatos Rcoef = null;//coef. S11 calibrado (con filtrado mediante FFT)
    private ArrayList<MathDatos> Rlist = new ArrayList<>();//array con las medidas realizadas hasta ahora
    //private ArrayList<String> Rlist_str=new ArrayList<>();//contiene el string literal devuelto por el analizador
    private MathDatos Rmedia;//valores complejos de la media, con presición de float (pq había un problema al hacer la media de las partes Re e Im, pero parece que haciendo la media de los módulos se corrige: sería pq la fase contenía errores¿?)
    private MathDatosD Rmedia2 = null;//guarda valores complejos con precisión de double en vez de float (sólo para calcular la media del módulo)
    private confFreq confF;//param para pintar las graf en freq
    private String[] NumAreas;//contiene la lista de las áreas a medir
    private SharedPreferences pref;//permite leer el fichero de conf por defecto
    private int NumAreaSelec = -1;//area actual de medida (por defecto, aparecemos en el area 0)
    //private int Nmed_prev = 0;//num de medidas ya realizadas en el área actual (se actualiza cuando se guarde el área por 1a vez!)
    private boolean actualizarArea = false; //sólo actualizamos las áreas cuando volvemos de la pantalla de conf.
    private double[] freq;
    private int criterio_ico = 10;//se pone el icono de criterio en modo transparente
    private double fo = 10;//nos centramos en 10GHz para probar los criterios con la plancha CAL1
    /*
    private GoogleApiClient apiClient;//cliente para manejar la conexión con Google Drive
    //private String MedidasAppFolderID = "DriveId:0BxBzpgvBYNJ1ZTJjeDlyNlVWUGs";//FolderID del directorio "MedidaApp" que hay en la cuenta "micromag@micromag.es"
    private String MedidasAppFolderID = "DriveId:CAASABjkOyDMyuu7iFcoAQ==";//FolderID del directorio "MedidaApp" que hay en la cuenta "micromag@micromag.es"
    */
    private ProgressDialog progress;//Barra de progreso para la conexión inicial al analizador
    private String bateria_vna = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);// a 180º

        // OPCIONES RELACIONADAS CON LA ACTIONBAR
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//evitamos que el salvapantallas aparezca en esta Actividad
        //int flg=View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; //en modo inmersivo: fullscreen + actionbar y softbar aparece/desaparece
        //flg=View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //flg=View.SYSTEM_UI_FLAG_FULLSCREEN;
        //getWindow().getDecorView().setSystemUiVisibility(flg);
        //getSupportActionBar().show();

//        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        getSupportActionBar().setTitle("Ale");
//        getSupportActionBar().setIcon(R.drawable.dani_icon);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        //getSupportActionBar().setLogo(R.drawable.dani_icon);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);

        //Inicializamos la lista de areas disponibles a mostrar en el desplegable de la Actionbar
//        String[] datos = new String[]{"A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10"};
//        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, datos);//Adaptador: contiene los datos a mostrar
//        Spinner cmbOpciones = (Spinner)findViewById(R.id.CmbToolbar);
//        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        cmbOpciones.setAdapter(adaptador);
//        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSupportActionBar().getThemedContext(),android.R.layout.appbar_filter_title,datos);
//        //adaptador.setDropDownViewResource(R.layout.appbar_filter_list);
//
//        cmbOpciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                //... Acciones al seleccionar una opción de la lista
//                Log.i("Toolbar 3", "Seleccionada opción " + i);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                //... Acciones al no existir ningún elemento seleccionado
//            }
//        });


        //CREAMOS UNOS EJES Y UNA CURVA DE EJEMPLO
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-35);
        graph.getViewport().setMaxY(2);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(2);
        graph.getViewport().setMaxX(18);
        // enable scaling and scrolling
        graph.getViewport().setScalable(false);
        graph.getViewport().setScalableY(false);


        //EVENTO: PULSAR SOBRE EL EJE (LO TRATAREMOS COMO UN OBJETO VIEW)
        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lecturaS11(null);//debug en Modo Offline
                if ((conectado == 1) || (conectado == 0)) { //Siempre entra en este "if": a veces cuando salimos de la app y volvemos "conectado=0" y realmente el VNA estaba conectado. Para evitar q nos deje medir, siempre medimos, aunque no hubiese VNA
                    new connectTask().execute(":CALC:DATA:SDAT?");//leer datos S11
                    new connectTask().execute(":SYSTem:BATTery:ABSCharge?");//leer nivel bateria VNA
                } else {
                    Toast.makeText(getApplicationContext(), "Analizador No Conectado!", Toast.LENGTH_SHORT).show();
                }
                //Debug: Conexión a GDrive y Crear carpeta en el directorio Raiz
//                new Thread() {
//                    @Override
//                    public void run() {
//                        GDFolderRoot();
//                    }
//                }.start();
            }
        });

        //EVENTO: PULSACIÓN LARGA SOBRE EL EJE
        graph.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                borrarRlist();
                Toast.makeText(getApplicationContext(), "Borrar Medida!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //CONF PARAMETROS: nos aseguramos que la conf tiene al menos los valores por defecto
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        if (pref.getString("freq1", "").equals("")) {
            editor.putString("freq1", "2");
            editor.commit();
        }
        if (pref.getString("freqEnd", "").equals("")) {
            editor.putString("freqEnd", "18");
            editor.commit();
        }
        if (pref.getString("filtro", "").equals("")) {
            editor.putString("filtro", "0.18");
            editor.commit();
        }
        if (pref.getString("npoint", "").equals("")) {
            editor.putString("npoint", "201");
            editor.commit();
        }
        if (pref.getString("Narea", "").equals("")) {
            editor.putString("Narea", "10");
            editor.commit();
        }
        if (pref.getString("nameProyecto", "").equals("")) {
            editor.putString("nameProyecto", "MicromagTest");
            editor.commit();
        }
        if (pref.getString("ip", "").equals("")) {
            editor.putString("ip", "10.1.18.121");
            editor.commit();
        }
        if (pref.getString("puerto", "").equals("")) {
            editor.putString("puerto", "5025");
            editor.commit();
        }
        if (pref.getString("esquema", "").equals("")) {
            editor.putString("esquema", "BandaXMetal");
            editor.commit();
        }
        double f1 = Double.parseDouble(pref.getString("freq1", "2"));
        double f2 = Double.parseDouble(pref.getString("freqEnd", "18"));
        double Npoint = Double.parseDouble(pref.getString("npoint", "201"));
        double df = (f2 - f1) / (Npoint - 1);
        double[] xlimF = new double[]{f1, f2};
        double[] ylimF = new double[]{-35, 5};
        confF = new confFreq(df, xlimF, ylimF, f1, f2, (int) Npoint);//guardamos la conf de medida por defecto
        freq = new double[(int) Npoint];
        for (int i = 0; i < Npoint; i += 1) {
            freq[i] = f1 + df * i;
            //Log.e("MainAct.OnCreate","alej: freq["+i+"] = "+freq[i]);
        }
/*
        //Cliente que maneja la conexión a Google Drive (asumimos que habrá una carpeta llamada "MedidasAPP")
        apiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Drive.API).addScope(Drive.SCOPE_FILE).build();
    */
    }



    @Override
    public void onStart() {
        super.onStart();
        /*
        apiClient.connect();
*/
    }

    @Override
    public void onStop() {
        super.onStop();
        /*
        apiClient.disconnect();
        */
    }

    /*
    //GDrive: (Debug) Test Crear Folder en Directorio Raiz
    public void GDFolderRoot() {
        DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);
        Log.i("GDFolderRoot", "alej: DriveFolder = " + folder);
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle("0AaMedidasApp").build();
        folder.createFolder(apiClient, changeSet).setResultCallback(
                new ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFolderResult result) {
                        if (result.getStatus().isSuccess())
                            Log.i("GDFolderRoot", "alej: Carpeta creada con ID = " + result.getDriveFolder().getDriveId());
                        else
                            Log.e("GDFolderRoot", "alej: Error al crear carpeta");
                    }
                });
    }

    //GDrive: Interface que maneja un error en la conexión al servicio de GDrive
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Error de conexion!", Toast.LENGTH_SHORT).show();
        Log.e("GDrive API", "alej: OnConnectionFailed: " + connectionResult);
    }

*/
    //Este método es llamado cuando damos al botón HW atrás, desde la actividad de configuración (objetivo: actualizar el spinner con las áreas disponibles de la Actionbar!)
    @Override
    protected void onResume() {
        super.onResume();  // Always call the superclass method first
        //graph.removeAllSeries();//borramos los ejes
        NumAreaSelec = -1; //evita que cuando se modifique el nombre del proyecto, el último área que se estuviera visualizando del proyecto anterior se copie en el nuevo proyecto
        actualizarArea = true;
        invalidateOptionsMenu();//esto hace una llamada a la función "onPrepareOptionsMenu()" q tiene acceso a los items de la actionbar
        actualizarArea = false;
        graph.removeAllSeries();//borramos los ejes
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    //Actualiza el titulo del item "Nuevo" con el num de medidas actuales:a modo de contador
    public void setContMedItem() {
        invalidateOptionsMenu();//esto hace una llamada a la función "onPrepareOptionsMenu()" q tiene acceso a los items de la actionbar

    }

    //Borramos la última medida almacenada
    public void borrarRlist() {
        int Nmeas = Rlist.size();
        if (Nmeas > 1) {
            Rmedia2 = MathV.mediaDel(Rcoef, Rmedia2, Rlist.size());//actualizamos la media actual (tras el borrado)
            Rlist.remove(Nmeas - 1);//eliminamos el último elemento (1er elemento, indice i=0!)
            Rcoef = Rlist.get(Rlist.size() - 1);//actualizamos la última medida como la actual
            Log.e("borrarList", "alej: Nmeas = " + Rlist.size());
            //Grafica: medida filtrada
            graph.removeAllSeries();//borramos las series de los ejes
            MathV.pintarSerie(graph, Color.BLUE, Rcoef, confF.df, confF.xlimF, confF.ylimF, confF.fini, confF.N);
            MathV.pintarSerie(graph, Color.BLACK, Rmedia2, confF.df, confF.xlimF, confF.ylimF, confF.fini, confF.N);

            //Criterio aplicado a la curva medida
            double[] Rmod = MathV.absDB_wV(Rcoef);
            criterioCurva criterio = new criterioCurva(freq, Rmod);
            //criterio_ico = Math.abs(criterio.BandaXmetal(fo));//criterio aplicado a 10GHz
            criterio_ico = (criterio.BandaXmetal(fo));//criterio aplicado a 10GHz

            setContMedItem();//set contador de medidas en la actionbar
        }
        if (Nmeas == 1) {
            Rlist.remove(Nmeas - 1);//eliminamos el único elemento
            Rcoef = null;
            Rmedia = null;
            criterio_ico = 10;//icono de criterio transparente
            graph.removeAllSeries();//borramos los ejes
            setContMedItem();//set contador de medidas en la actionbar
        }
    }

    //Cada petición de lectura: Leer S11, Calibrarlo con Filtrado con FFT y Pintar Media actual y Medida
    public void lecturaS11(String msg) {
        //LECTURA de los datos del VNA
        //Modo Offline para Testeo: Cogemos siempre un valor de medida almacenados en fichero de conf. /data/data/com.example.ale.medidas/shared_prefs/*.xml
        //msg = PreferenceManager.getDefaultSharedPreferences(this).getString("medida", ""); //Modo Offline
        S11 = msg;

        //Recuperamos la configuración
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        //Guardamos la medida en la preferencia (fase de testeo: para ir probando cosas con datos correctos)
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putString("medida", msg);
//        editor.commit();
        //Toast.makeText(getApplicationContext(), "Freq range: [" + pref.getString("freq1", "") + "," + pref.getString("freqEnd", "") + "] Filtro: " + pref.getString("filtro", ""), Toast.LENGTH_SHORT).show();


        //VAlores de configuración
        float fini = 2;
        float fstop = 18;
        double dR = 0.2;//así se declara un número como float (32 bits)
        int N = 201;
        int Nfft = (int) Math.pow(2, 10);//1024 = 2^10 -- long del vector S11 (201 puntos) + zero padding (ceros hasta los 1024 puntos: 10 bits)
        if (pref.getString("freq1", "") != null) {
            fini = Float.parseFloat(pref.getString("freq1", ""));
            fstop = Float.parseFloat(pref.getString("freqEnd", ""));
            dR = Float.parseFloat(pref.getString("filtro", ""));
            N = (int) Float.parseFloat(pref.getString("npoint", ""));
            //Toast.makeText(getApplicationContext(), "(Num de puntos, filtro) = (" + N + "," + dR + ")", Toast.LENGTH_SHORT).show();
        }
        double df = (double) (fstop - fini) / (double) (N - 1);
        double[] xlimF = new double[]{fini, fstop};
        double[] ylimF = new double[]{-35, 5};
        double dx = 1 / (df * Nfft) * c;//retardo de ida y vuelta (dividimos entre 2 la distancia!)
        double dmax = 1 / df * c;//retardo de ida y vuelta (dividimos entre 2 la distancia!)
        double[] xlim = new double[]{0, dmax / 2};
        double[] ylim = new double[]{-20, 40};
        confF = new confFreq(df, xlimF, ylimF, fini, fstop, N);//guardamos la conf de medida

        //Toast.makeText(getApplicationContext(), "(Num de puntos,df) = (" + N + "," + df + ")", Toast.LENGTH_SHORT).show();

        //Medida y Recuperar CaL: Back, ref y 3erStd
        String back = pref.getString("background", "");
        String ref = pref.getString("reference", "");
        String std3 = pref.getString("plex2mm", "");
        //String med = pref.getString("medida", "");
        String txt = "";
        if (back.equals("")) {
            txt += "Back";
        }
        if (ref.equals("")) {
            txt += ".Ref";
        }
        if (!txt.equals("")) {
            Toast.makeText(getApplicationContext(), "CaL Incompleta! Falta: " + txt, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!std3.equals("")) {
            S11std3 = MathV.vna2ReIm(std3);
        }
        //String to Vectors
        S11m = MathV.vna2ReIm(S11);
        S11back = MathV.vna2ReIm(back);
        S11ref = MathV.vna2ReIm(ref);

        //Testeo
//        Toast.makeText(getApplicationContext(), "CaL S11back N = "+ Nfft +" S11 = " + back, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), "CaL S11ref N = "+ Nfft +" S11 = " + ref, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getApplicationContext(), "CaL S11m N = "+ Nfft +" S11 = " + S11, Toast.LENGTH_SHORT).show();


        //Zero padding para la IFFT
        float[] Sb_Re = Arrays.copyOf(S11back.v1(), Nfft);//Background
        float[] Sb_Im = Arrays.copyOf(S11back.v2(), Nfft);
        float[] Sr_Re = Arrays.copyOf(S11ref.v1(), Nfft); //Reference
        float[] Sr_Im = Arrays.copyOf(S11ref.v2(), Nfft);
        float[] Sm_Re = Arrays.copyOf(S11m.v1(), Nfft);   //Medida
        float[] Sm_Im = Arrays.copyOf(S11m.v2(), Nfft);


        // ----- IFFT: Se hace con la librería "neoncore" --------
        float[] Sb_Re_t = Sb_Re;//copia de los valores en freq y tras la fft será rellenado cn los valores en el tiempo
        float[] Sb_Im_t = Sb_Im;
        float[] Sr_Re_t = Sr_Re;
        float[] Sr_Im_t = Sr_Im;
        float[] Sm_Re_t = Sm_Re;
        float[] Sm_Im_t = Sm_Im;

        FaCollection.ifft_float32(Sb_Re_t, Sb_Im_t);//esta función modifica el contenido de las variables "Sr_t" y "Si_t" (como si fueran punteros!)
        FaCollection.ifft_float32(Sr_Re_t, Sr_Im_t);
        FaCollection.ifft_float32(Sm_Re_t, Sm_Im_t);

        MathDatos Sb_t = new MathDatos(Sb_Re_t, Sb_Im_t);
        MathDatos Sr_t = new MathDatos(Sr_Re_t, Sr_Im_t);
        MathDatos Sm_t = new MathDatos(Sm_Re_t, Sm_Im_t);
        MathDatos[] Sparam = new MathDatos[]{Sb_t, Sr_t, Sm_t};//array de objetos MathDatos (CaL y Medida)


        //Pintamos la IFFT
        //graph.removeAllSeries();//borramos las series de los ejes
        //MathV.pintarSerie(graph, Color.RED, Sb_t, dx/2, xlim, ylim);//retardo de ida y vuelta
        //MathV.pintarSerie(graph, Color.BLUE, Sr_t, dx/2, xlim, ylim);
        //MathV.pintarSerie(graph, Color.GREEN, Sm_t, dx/2, xlim, ylim);

        //Filtrado y CaL
        MathDatos[] Scal_t = MathV.filtrar(Sparam, dR, dx);

        //Realizamos "IFFT/Nfft"
        MathDatos[] S2 = MathV.calBackRef(Scal_t, N);
        Rcoef = S2[0];//S11 de la medida calibrado!
        //---------------------------------------------------------

        //-----IFFT: Se hace con la librería Apache Commons Math v4---
        double[][] Sb_tt = new double[2][];
        double[][] Sr_tt = new double[2][];
        double[][] Sm_tt = new double[2][];
        Sb_tt[0] = float2double(Sb_Re);
        Sb_tt[1] = float2double(Sb_Im);
        Sr_tt[0] = float2double(Sr_Re);
        Sr_tt[1] = float2double(Sr_Im);
        Sm_tt[0] = float2double(Sm_Re);
        Sm_tt[1] = float2double(Sm_Im);
        FastFourierTransform ifft = new FastFourierTransform(FastFourierTransform.Norm.STD, true);
        ifft.transformInPlace(Sb_tt);
        ifft.transformInPlace(Sr_tt);
        ifft.transformInPlace(Sm_tt);

        //float[][] Sb_t2=MathV.double2float(Sb_tt);// se convierte a un array-2D de float en vez de double
        MathDatos Sb_tf = new MathDatos(MathV.double2float(Sb_tt)[0], MathV.double2float(Sb_tt)[1]);
        MathDatos Sr_tf = new MathDatos(MathV.double2float(Sr_tt)[0], MathV.double2float(Sr_tt)[1]);
        MathDatos Sm_tf = new MathDatos(MathV.double2float(Sm_tt)[0], MathV.double2float(Sm_tt)[1]);
        MathDatos[] Sparam2 = new MathDatos[]{Sb_tf, Sr_tf, Sm_tf};//array de objetos MathDatos (CaL y Medida)

        //Filtrado y CaL
        MathDatos[] Scal_tt = MathV.filtrar(Sparam2, dR, dx);

        //Realizamos "FFT/Nfft"
        MathDatos[] SS2 = MathV.calBackRef(Scal_tt, N);
        Rcoef = SS2[0];//S11 de la medida calibrado!
        //------------------------------------------------------------


        //Añadimos la nueva medida a la lista de medidas
        Rlist.add(Rcoef);
        Log.e("lecturaS11", "alej: Nmeas = " + Rlist.size());
        MathV.pintarSerie(graph, Color.BLUE, Rcoef, df, xlimF, ylimF, fini, N);
        Rmedia2 = MathV.mediaAdd(Rcoef, Rmedia2, Rlist.size());//última media

        //Criterio aplicado a la curva medida
        double[] Rmod = MathV.absDB_wV(Rcoef);
        criterioCurva criterio = new criterioCurva(freq, Rmod);
        //criterio_ico = Math.abs(criterio.BandaXmetal(fo));//criterio aplicado a 10GHz
        if (pref.getString("esquema", "").equals("BandaXMetal")) {
            criterio_ico = (criterio.BandaXmetal(fo));//criterio aplicado a 10GHz
        } else {
            Toast.makeText(getApplicationContext(), "¡No se ha seleccionado el Esquema de Pintado!", Toast.LENGTH_SHORT).show();
        }

        //Grafica: medida filtrada
        graph.removeAllSeries();//borramos las series de los ejes
        MathV.pintarSerie(graph, Color.BLUE, Rcoef, df, xlimF, ylimF, fini, N);
        MathV.pintarSerie(graph, Color.BLACK, Rmedia2, df, xlimF, ylimF, fini, N);

        //Actualizamos el num de medidas e icono de criterio en el menu
        setContMedItem();

        //Pintamos la operación inversa para ver si obtenemos la curva original "FFT(IFFT)": Para que sea correcto es necesario hacer "1/Nfft*FFT(IFFT)"
        //(sólo pintamos los N primeros valores, pq los restantes hasta llegar a Nfft serán 0: zero padding!!)
//        FaCollection.fft_float32(Sr_t, Si_t);

    }

    //Sobreescribir el comportamiento pulsar el botón "hacia atrás" en la actividad principal: evitar que se cierre la app y perder los datos no guardados!
    //Además, parece que se puede capturar el evento "right click" de un ratón conectado por micro-usb/bluetooth

    @Override
    public void onDestroy() {
        super.onDestroy();
        onBackPressed();
    }

    //Copia el archivo XML de preferencias al almacenamiento  externo (extraible o no) público y accesible a todos
    private boolean saveSharedPreferencesToFile(String src, String dst) {
        boolean res = false;
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }


    //Comprobar si está accesible (montado) el almacenamiento externo
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //Guardar Medidas en archivo XML y en el almacenamiento público externo
    @Override
    public void onBackPressed() {
        guardarArea();
    }

    //Guardar Area en: archivo XML (Preferences), en almacenamiento publico (interno o externo) y en GDrive
    public void guardarArea() {
        String LOGTAG = "MainAct.guardarArea";
        // Guardaremos la medidas del area seleccionada en un archivo XML de nombre: "proyecto.Area.XX.xml"
        int Nmed = Rlist.size();
        String Nmed_update = "1";
        if ((Nmed > 0) && (NumAreaSelec > -1)) { //guardamos archivo: lo creamos o actualizamos uno existente
            //Nombre archivo XML
            String nmfile = pref.getString("nameProyecto", "") + ".Area." + NumAreas[NumAreaSelec];
            Log.e(LOGTAG, "alej nmfile: " + nmfile);
            SharedPreferences prefArea = getSharedPreferences(nmfile, Context.MODE_PRIVATE);
            String Nmedidas = prefArea.getString("Nmedidas", "");//num de medidas en el archivo

            //Escritura de las medidas,  media y otros campos de control
            SharedPreferences.Editor editor = prefArea.edit();
            String fileID = prefArea.getString("GoogleDriveFileID", "");//antes de borrar se recupera el valor d este campo, q se habrá añadido posteriormente al fichero
            String folderID = prefArea.getString("GoogleDriveFolderID", "");
            editor.clear();//Borramos los valores anteriores: pq estamos sobreescribiendo un area ya guardada!
            //editor.commit();//aplicamos el borrado
            editor.apply();//aplicamos el borrado Inmediato
            //editor.putString("Nmedidas", Nmed_update);//num total de medidas en el archivo
            editor.putString("Nmedidas", String.valueOf(Nmed));//num total de medidas en el archivo
            editor.putString("NombreArea", NumAreas[NumAreaSelec]);//nombre del area al que pertenecen esas medidas
            editor.putString("Media", Rmedia2.toString());//actualizamos la media de las medidas
            editor.putString("GoogleDriveFileID", fileID);
            editor.putString("GoogleDriveFolderID", folderID);
            //Otros campos de control importantes para saber la lista de freq de cada medida
            pref = PreferenceManager.getDefaultSharedPreferences(this);
            editor.putString("freq1", pref.getString("freq1", ""));
            editor.putString("freqEnd", pref.getString("freqEnd", ""));
            editor.putString("npoint", pref.getString("npoint", ""));
            String med_txt;
            for (int i = 0; i < Nmed; i += 1) { //Bucle para guardar las medidas actuales guardadas
                //med_txt = "Medida" + (i + 1 + Integer.parseInt(Nmedidas));//medida actual, teniendo en cuenta las medidas que se hicieron la vez anterior
                med_txt = "Medida" + (i + 1);
                editor.putString(med_txt, Rlist.get(i).toString());
            }
            //editor.commit();//aplicamos el borrado
            editor.apply();//cambios inmediato
            //Toast.makeText(getApplicationContext(), "Área Guardada: " + NumAreas[NumAreaSelec] + "!", Toast.LENGTH_SHORT).show();
            //conectado = 1;//no hace falta que activemos el boton de conex con el VNA

            //Copiamos el archivo XML al almacenamiento publico (interno) tb para que sea accesible para pasarlos al ordenador
            File fdExt = new File(Environment.getExternalStorageDirectory(), "0AaMedidas");//creamos o accedemos al directorio medidas
            //File fdInt = getApplicationContext().getCacheDir();//directorio interno donde se guarda los xml de las preferencias
            File fdInt = new File(getApplicationInfo().dataDir, "shared_prefs");
            if (fdExt.exists() == false) { //creamos el directorio, si no existe!
                fdExt.mkdir();
            }
            String dst = fdExt.getAbsolutePath() + File.separator + nmfile + ".xml";
            String src = fdInt.getAbsolutePath() + File.separator + nmfile + ".xml";
            if (isExternalStorageWritable()) {
                if (saveSharedPreferencesToFile(src, dst) == false) {
                    Toast.makeText(getApplicationContext(), "Error de Escritura", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Área Guardada: " + NumAreas[NumAreaSelec] + "!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Almacenamiento Externo No Disponible", Toast.LENGTH_SHORT).show();
            }
/*
            //Copiar Archivo a GDrive
            //GDFolderRoot();//Testeo: Crea una carpeta en el Raiz y obtiene el FolderID
            String[] datos = new String[]{src, "Copiar", MedidasAppFolderID, fileID};//filename del XML a copiar!
            new createGDriveFileTask().execute(datos);//debug en Modo Online
            */
        }
        if ((Nmed == 0) && (NumAreaSelec > -1)) {
            // Borrar este archivo de preferencias q contiene las medidas del área seleccionada, ya q se han borrado todas!
            String nmfile = pref.getString("nameProyecto", "") + ".Area." + NumAreas[NumAreaSelec];
            Log.e(LOGTAG, "alej nmfile: " + nmfile);
            SharedPreferences prefArea = getSharedPreferences(nmfile, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefArea.edit();
            editor.clear();
            editor.apply();//cambios inmediato

            //Borrar archivo del almacenamiento externo
            File fdExt = new File(Environment.getExternalStorageDirectory(), "0AaMedidas");//creamos o accedemos al directorio medidas
            if (fdExt.exists() == true) { //Comprobar que existe el directorio y que existe el archivo
                String dst = fdExt.getAbsolutePath() + File.separator + nmfile + ".xml";
                File file = new File(dst);
                file.delete();
                Log.e(LOGTAG, "alej: Archivo borrado SD: " + nmfile);
                //Toast.makeText(getApplicationContext(), "Área Borrada: " + NumAreas[NumAreaSelec] + "!", Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(), "Área Borrada: " + NumAreas[NumAreaSelec] + "!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    //Actualiza los nombres de las areas o el num de areas a medir
    public void actulizaSpinnerArea(@NonNull Menu menu) {
        //Spinner: Asociamos el Adaptador con el contenido
        MenuItem item = menu.findItem(R.id.cmbToolbar);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        //Leemos el num de areas que haya en las preferencias (por defecto, serán 10)
        int Nareas;
        String Na = pref.getString("Narea", "");
        if (Na.equals("")) {
            Nareas = 10;
            NumAreas = new String[Nareas];
            for (int i = 0; i < Nareas; i += 1) {
                NumAreas[i] = "A" + (i + 1);
            }
        } else {
            try {
                Nareas = Integer.parseInt(Na);
                NumAreas = new String[Nareas];
                for (int i = 0; i < Nareas; i += 1) {
                    NumAreas[i] = "A" + (i + 1);
                }
            } catch (NumberFormatException e) {
                NumAreas = Na.split(",");//El string contiene el nombre de las areas, en vez del numero de areas
            }
        }


        //Asociamos Spinner con su adaptador
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(getSupportActionBar().getThemedContext(), android.R.layout.simple_spinner_item, android.R.id.text1, NumAreas);//Adaptador: contiene los datos a mostrar
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptador);
    }

    //Actualiza el icono del criterio para la última medida
    public void actualizaCriterio(Menu menu) {
        MenuItem item2 = menu.findItem(R.id.criterio);
        int Rsound = 0;
        int icono = 0;
        String txt = "";
        switch (criterio_ico) {
            case 1: //No necesita ser pintado más
                item2.setIcon(android.R.drawable.presence_busy);
                icono = android.R.drawable.presence_busy;
                txt = "STOP";
                Rsound = R.raw.stop;
                break;
            case -1: //No necesita ser pintado más
                item2.setIcon(android.R.drawable.presence_busy);
                icono = android.R.drawable.presence_busy;
                txt = "STOP";
                Toast.makeText(getApplicationContext(), "Baja Atenuación!", Toast.LENGTH_SHORT).show();
                Rsound = R.raw.stop;
                break;
            case 2: //Necesita menor espesor
                item2.setIcon(android.R.drawable.button_onoff_indicator_on);
                icono = android.R.drawable.button_onoff_indicator_on;
                txt = "MENOS";
                Rsound = R.raw.minus;
                break;
            case -2: //Necesita menor espesor
                item2.setIcon(android.R.drawable.button_onoff_indicator_on);
                icono = android.R.drawable.button_onoff_indicator_on;
                txt = "MENOS";
                Toast.makeText(getApplicationContext(), "Baja Atenuación!", Toast.LENGTH_SHORT).show();
                Rsound = R.raw.minus;
                break;
            case 3: //Espesor adecuado
                item2.setIcon(android.R.drawable.btn_star_big_on);
                icono = android.R.drawable.btn_star_big_on;
                txt = "OK";
                Rsound = R.raw.perfect;
                break;
            case -3: //Espesor adecuado
                item2.setIcon(android.R.drawable.btn_star_big_on);
                icono = android.R.drawable.btn_star_big_on;
                txt = "OK";
                Toast.makeText(getApplicationContext(), "Baja Atenuación!", Toast.LENGTH_SHORT).show();
                Log.e("actualizarCriterio", "alej: -3 Baja Att.");
                Rsound = R.raw.perfect;
                break;
            case 4: //Necesita mayor espesor
                item2.setIcon(android.R.drawable.ic_input_add);
                icono = android.R.drawable.ic_input_add;
                txt = "MAS";
                Rsound = R.raw.plus;
                break;
            case -4: //Necesita mayor espesor
                item2.setIcon(android.R.drawable.ic_input_add);
                icono = android.R.drawable.ic_input_add;
                txt = "MAS";
                Toast.makeText(getApplicationContext(), "Baja Atenuación!", Toast.LENGTH_SHORT).show();
                Rsound = R.raw.plus;
                break;
            case 5: //Curva con forma erronea: p.e medir el background o metal o que aparezcan multiples picos de absorción <-10dB (para el monobanda)
                item2.setIcon(android.R.drawable.presence_offline);
                icono = android.R.drawable.presence_offline;
                txt = "ERROR";
                Toast.makeText(getApplicationContext(), "Curva Errónea!", Toast.LENGTH_SHORT).show();
                //Rsound = R.raw.dcat;
                break;
            default: //La curva S11 no es correcta (no tiene mínimos o tiene un formato que no es el esperado)
                item2.setIcon(android.R.drawable.screen_background_light_transparent);
                Rsound = 0;
                break;
        }


        /*//Emite sonido acorde al tipo de medida realizada
        if (Rsound != 0) {
            MediaPlayer player = new MediaPlayer();
            String RESOURCE_PATH = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";
            String path = RESOURCE_PATH + getPackageName() + File.separator + Rsound;//android.resource://[package]/[resource_id]
            try {
                player.setDataSource(getApplicationContext(), Uri.parse(path));
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
*/

        //Notificacion en la barra de tareas
//        int notificationID = 1;
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
//        mBuilder.setContentTitle(txt);
//        mBuilder.setContentText(txt);
//        mBuilder.setContentInfo("M" + Rlist.size());
//        mBuilder.setSmallIcon(icono);
//        mBuilder.setTicker(txt);
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(notificationID, mBuilder.build());
    }

    //Actualiza la actionbar tras una llamada a "invalidateOptionsMenu()" (aprovecharemos para cambiar el título del item de la actionbar q usaremos para mostrar el num. medidas actual
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Actualizamos el num de medidas realizadas: Tras realizar cada medida, se actualiza
        MenuItem item = menu.findItem(R.id.action_nuevo);
        //int Nmed = Rlist.size() + Nmed_prev;
        int Nmed = Rlist.size();
        String txt = "M: " + Nmed;
        item.setTitle(txt);
        //Log.i("ActionBar.Nuevo", "alej: Nmed = "+Nmed);

        //Actualizamos el num de areas a medir: en teoría debe hacerse cuando pulsamos el botón "atrás" desde la pantalla de conf
        if (actualizarArea == true) {
            actulizaSpinnerArea(menu);
            graph.removeAllSeries();//borramos los ejes
        }


        //Actualizamos el icono del criterio de la medida
        actualizaCriterio(menu);


        //Pintamos el botón en rojo (estamos conectados)
        item = menu.findItem(R.id.action_settings);
        if (conectado == 1) {
            item.setIcon(R.drawable.ic_lock_power_off_verde);
        } else {
            //item.setIcon(android.R.drawable.presence_invisible);
            item.setIcon(android.R.drawable.ic_lock_power_off);
        }

        //Actuliza nivel de la bateria
        item = menu.findItem(R.id.bateria);
        if ((conectado == 1) || (conectado == 0)) { //no comprobamos que esté conectado: a veces tras estar desconectado  y salir de la pantalla principal, la var. se pone en desconectado
            int drawableResourceId;
            if (bateria_vna.equals("-1")) {
                drawableResourceId = R.drawable.battery_charge_background_00;
            } else {
                String icon_name = "";
                int bat = Integer.parseInt(bateria_vna);
                bat += 7;//la lectura del vna no coincide con el valor
                bateria_vna = String.valueOf(bat);
                if (bat < 5) {
                    drawableResourceId = R.drawable.battery_charge_background_01;
                } else if (bat >= 5 && bat < 10) {
                    drawableResourceId = R.drawable.battery_charge_background_05;
                } else {
                    int bat2digit = Integer.parseInt(bateria_vna.substring(1));
                    String digit2 = "";
                    if (bat2digit <= 3) {
                        digit2 = "0";
                    } else if (bat2digit > 3 && bat2digit <= 9) {
                        digit2 = "5";
                    }
                    icon_name = "battery_charge_background_" + bateria_vna.substring(0, 1) + digit2;
                    Log.e("onPrepareOptionsMenu", "alej: Archivo Bateria: " + icon_name);
                    drawableResourceId = this.getResources().getIdentifier(icon_name, "drawable", this.getPackageName());
                }

            }
            item.setIcon(drawableResourceId);
        } else { //No muestra dato sobre la bateria
            item.setIcon(R.drawable.battery_charge_background_00);
        }

        return true;
    }

    //Asociamos el menu a la ActionBar por defecto de la App
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Creamos la clase que manejará el evento resultante de seleccionar un área
        MenuItem item = menu.findItem(R.id.cmbToolbar);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            Menu m = menu;//referencia al menu de la ActionBar, para poder acceder a los items

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Procesamos el evento cuando el área actual no coincide con el nuevo area seleccionado: si no, no hay nada nuevo que mostrar
                if (!(i == NumAreaSelec)) {
                    //--Si existe fichero del area, ya guardado anteriormente, recuperamos las medidas (pintamos por pantalla la media y última medida y actualizamos el)
                    //Borramos datos anteriores
                    guardarArea();//forzamos a guardar nuevamente el area, para evitar perder datos
                    Rlist.clear();//borramos la lista de curvas anteriores!
                    graph.removeAllSeries();//borramos las series de los ejes
                    //Abrimos o creamos (si no existe) el fichero de conf. para ese área
                    String nmfile = pref.getString("nameProyecto", "") + ".Area." + NumAreas[i];
                    SharedPreferences prefArea = getSharedPreferences(nmfile, Context.MODE_PRIVATE);
                    String Nmedidas = prefArea.getString("Nmedidas", "0");//num de medidas en el archivo
                    int Nmed_prev = Integer.parseInt(Nmedidas);//cargamos area y actualizamos el num de medidas a las que ya se hicieron previamente en ese área
                    if (!(Nmedidas.equals("0"))) { //Si existen medidas guardadas, las carga
                        String med_str, media_str;
                        MathDatos med;
                        MathDatosD media;
                        med_str = prefArea.getString("Medida" + Nmedidas, "");
                        media_str = prefArea.getString("Media", "");
                        med = MathV.vna2ReIm(med_str);
                        media = MathV.vna2ReImD(media_str);

                        //Actualizamos los valores de la última medida y la media para el área que estamos cargando
                        Rcoef = med;
                        Rmedia2 = media;

                        //Cargamos el Array con las medidas anteriores
                        for (int idx = 0; idx < Nmed_prev; idx += 1) {
                            String str = prefArea.getString("Medida" + (idx + 1), "");
                            Rlist.add(MathV.vna2ReIm(str));
                        }

                        //Grafica: medida filtrada
                        MathV.pintarSerie(graph, Color.BLUE, Rcoef, confF.df, confF.xlimF, confF.ylimF, confF.fini, confF.N);
                        MathV.pintarSerie(graph, Color.BLACK, Rmedia2, confF.df, confF.xlimF, confF.ylimF, confF.fini, confF.N);

                        //Criterio aplicado a la curva medida
                        double[] Rmod = MathV.absDB_wV(Rcoef);
                        criterioCurva criterio = new criterioCurva(freq, Rmod);
                        //criterio_ico = Math.abs(criterio.BandaXmetal(fo));//criterio aplicado a fo
                        criterio_ico = (criterio.BandaXmetal(fo));//criterio aplicado a fo
                        actualizaCriterio(m);
                        //Toast.makeText(getApplicationContext(), "Area cargada:  " + NumAreas[i] + "!", Toast.LENGTH_SHORT).show();
                    } else { //Si no existen medidas, es q ese área es nueva y no se ha guardado nada previamente
                        Toast.makeText(getApplicationContext(), "Area nueva!", Toast.LENGTH_SHORT).show();
                        criterio_ico = 10;
                        actualizaCriterio(m);
                    }


                    //Actualizamos el num de medidas mostrados en la ActionBar: ya sea a "0" (si es nueva área) o Nmed_anterior (si el área ya fue guardada)
                    MenuItem item = m.findItem(R.id.action_nuevo);
                    //int Nmed = Rlist.size() + Nmed_prev;
                    int Nmed = Rlist.size();
                    String txt = "M: " + Nmed;
                    item.setTitle(txt);
                }
                NumAreaSelec = i;//actualizamos el área actual en la que se está midiendo
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //... Acciones al no existir ningún elemento seleccionado
            }
        });

        //Actualizamos el num de areas a medir
        actulizaSpinnerArea(menu);

        return true;
    }

    //Detecta qué botón de la ActionBar se ha pulsado
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_nuevo:
                return true;
            case R.id.action_buscar: //Pulsado sobre el icono de configuracion
                Log.i("ActionBar", "Settings!");
                //Intent intent = new Intent(this, Configuracion.class); // Abrimos la pantalla de configuracion: Implementada como una secundaria Activity
                Intent intent = new Intent(this, PantallaConf.class); // Abrimos Activity secundaria que hace uso del fragment XML como su propia UI
                startActivity(intent);
                return true;
            case R.id.action_settings: //Icono que muestra si estamos online (conectados al VNA)
                Log.i("ActionBar", "Settings!");
                if (conectado == 0) {
                    conectado = 1;
                    Log.e("ActionBar", "alej: Conectado: boton en rojo");
                    //Configuración del VNA
                    String[] cmd_conf = {":INST \"NA\"", ":SOUR:POW:ALC HIGH", ":INIT:CONT 1", ":FREQ:STAR 2e9", ":FREQ:STOP 18e9", ":SWE:POIN 201", ":BWID 1000", ":AVER:COUN 1"};

                    //Mostramos Barra de Progreso
                    progress = new ProgressDialog(this);
                    progress.setMessage("Conectando al VNA");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.setMax(1);
                    progress.setProgress(0);
                    progress.show();
                    new connectTask().execute(cmd_conf);//La conexión con el VNA se maneja en otro hilo independiente
                    //item.setIcon(android.R.drawable.presence_online);
                    //item.setIcon(android.R.drawable.ic_notification_overlay);
                    item.setIcon(R.drawable.ic_lock_power_off_verde);//No marcamos en rojo hasta que no se haya conectado (icono coloreado de rojo indicando q se está conectado)
                } else {
                    conectado = 0;
                    bateria_vna = "-1";
                    //item.setIcon(android.R.drawable.presence_invisible);
                    item.setIcon(android.R.drawable.ic_lock_power_off);
                    Log.e("ActionBar", "alej: Desconectado: boton en gris");
                }
                return true;
            case R.id.bateria: //Se pulsa sobre el icono de la bateria
                if (conectado == 1) {
                    new connectTask().execute(":SYSTem:BATTery:ABSCharge?");//leer nivel bateria VNA
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
/*

    //Guarda FileID de GDrive en el archivos XML de area guardado previamente
    public void writeDriveFileID2Preferences(String newFileID, String src_file) {
        String[] tk = src_file.split("/");//Separa el nombre del path del fichero
        String nmfile = tk[tk.length - 1];
        nmfile = nmfile.substring(0, nmfile.length() - 4);//eliminamos la extension ".xml"
        SharedPreferences prefArea = getSharedPreferences(nmfile, Context.MODE_PRIVATE);
        //Escritura de las medidas,  media y otros campos de control
        SharedPreferences.Editor editor = prefArea.edit();
        editor.putString("GoogleDriveFileID", newFileID);//ID del fichero creado
        editor.putString("GoogleDriveFolderID", MedidasAppFolderID);//ID del directorio donde se ha creado el fichero
        editor.commit();
    }
*/

    //Cliente v1: Se abre/cierra un socket en el envío de cada comando (si el comando es de request, se espera a la respuesta del VNA)
    public class connectTask extends AsyncTask<String, String, TCPClient> {
        @Override
        protected TCPClient doInBackground(String... message) {
            //Recuperamos del archivo de conf la IP y Port del VNA ("pref" es una varible global (var de la Main activity), accesible por ConnectTask pq es una clase anidada dentro de la MainActivity)
            String IPvna = pref.getString("ip", "");
            String Portvna = pref.getString("port", "");
            if (IPvna.equals("")) {
                IPvna = "10.1.18.121";
            }
            if (Portvna.equals("")) {
                Portvna = "5025";
            }

            //we create a TCPClient object, implmentamos la interfaz de TCPcliente con una clase anónima (permite comunicación de TCPCLient con el exterior) y damos los valores de IP y port del servidor
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            }, IPvna, Portvna);

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
            if (msg.equals("Bateria")) {
                conectado = 1;
                bateria_vna = mTcpClient.battery;
                Log.e("connectTask", "alej: Bateria VNA: " + bateria_vna + "%");
                invalidateOptionsMenu();//actualiza la actionBar con el porcentaje de bateria q le queda al VNA
            } else if (msg.equals("Error")) {
                Toast.makeText(getApplicationContext(), "Error de red o No conectado a la Wfi del VNA", Toast.LENGTH_SHORT).show();
                conectado = 0; //boton en gris pq ha habido un fallo
                bateria_vna = "-1";//bateria VNA: no se dispone de datos
                invalidateOptionsMenu();//actualiza la actionBar para dibujar el boton en gris
            } else if (msg.equals("FinInicializacion")) { //Se han enviado todos los comandos de inicializacion al VNA: cerrar Barra de Progreso!
//                progress.setProgress(progress.getMax());
                progress.dismiss();
            } else { //Los datos devueltos son la lectura del param S11
                lecturaS11(msg);//Tras recibir un msg, llamamos a la función que lo procesa
                conectado = 1;
            }
        }
    }

/*

    //GDrive: Maneja la conexión a Google Drive para guardar las medidas, debe hacerse en otro hilo (thread)
    public class createGDriveFileTask extends AsyncTask<String, String, GDriveClient> {
        private String resultado;
        private String newFileID;
        private String fn;//nombre del archivo XML sobre el que realizamos la operación

        @Override
        protected GDriveClient doInBackground(String... listaString) {
            fn = listaString[0];//archivo XML que queremos copiar/borrar a GDrive
            String operacion = listaString[1];//Tipo de operación: Crear y Copiar archivo a GDrive o Borrar archivo de GDrive
            String strFolderID = listaString[2];//directorio q contiene a los archivos XML de medida
            String strFileID = listaString[3];//ID del archivo de medida concreto que queremos borrar

            //Cliente GDrive para conexión a la cuenta
            GDriveClient gdrive = new GDriveClient(apiClient, fn, strFolderID, strFileID, new GDriveClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String[] message) { //Devolverá el FileID del nuevo archivo creado
                    //this method calls the onProgressUpdate
                    resultado = message[0];//comproar operación correcta o no
                    newFileID = message[1];//ID del nuevo archivo creado (sólo para operación Copiar)
                    Log.e("messageReceived", "alej: [resultado, newFileID] = [" + resultado + ", " + newFileID + "]");
                    publishProgress(resultado);
                }
            });

            //LLamamos al método "run()" del objeto "gdrive" que sabrá que hacer en cada operación
            //Log.e("WrteID2Pref", "alej: [XMLfile, Operacion, GDFolderID] = [" + listaString[0] + ", " + listaString[1] + ", " + listaString[2] + "]");
            gdrive.run(operacion);

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String msg = values[0];
            String LOGTAG = "onProgressUp";
            if (!(msg.equals("Error"))) {
                Log.e(LOGTAG, "alej: FileID: " + newFileID);
                writeDriveFileID2Preferences(newFileID, fn);
            } else {
                Toast.makeText(getApplicationContext(), "Error de comunicación con GDrive!", Toast.LENGTH_SHORT).show();
            }
        }
    }
*/


//    //Cliente v2: Comparte un socket para enviar varios comandos y escuchar las respuestas enviadas por el servidor
//    public class connectTaskv2 extends AsyncTask<Void, String, Boolean> {
//        @Override
//        protected Boolean doInBackground(Void... values) {
//            mTcpClientv2.abrirSocket();
//            return null;
//        }
//    }
//    public class connectTaskv3 extends AsyncTask<Void, String, Boolean> {
//        @Override
//        protected Boolean doInBackground(Void... values) {
//            String msg = mTcpClientv2.leerMessage();
//            publishProgress(msg);
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//            String msg = values[0]; //recuperamos el texto como parámetro de entrada
//            Toast.makeText(getApplicationContext(), "Num de puntos: " + msg.length() + " Rxdo msg=" + msg, Toast.LENGTH_SHORT).show();
//        }
//    }


}


