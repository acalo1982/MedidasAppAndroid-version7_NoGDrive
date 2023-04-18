package com.example.ale.medidas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math4.transform.FastFourierTransform;

import java.util.ArrayList;
import java.util.Arrays;

import fastandroid.neoncore.collection.FaCollection;

import static com.example.ale.medidas.MathV.contadorCondicion;
import static com.example.ale.medidas.MathV.min_max;
import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.indexOf;
import static org.apache.commons.lang3.math.NumberUtils.min;

/**
 * Created by ale on 11/05/2017.
 */


// Utility classes should always be final (no se pueden heredar y extender) and have a private constructor (no se pueden instanciar): conjunto de métodos estáticos (librería)
public final class MathV {

    private MathV() {
        // Utility classes should always be final and have a private constructor
    }


    //Producto de 2 vectores uno a uno
    public static double[] prodV(double[] v1, double[] v2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            v3 = new double[N];
            for (int i = 0; i < N; i += 1) {
                v3[i] = v1[i] * v2[i];
            }
            return v3;
        } else {
            return null;
        }
    }

    //Producto de 2 vectores uno a uno
    public static float[] prodV(float[] v1, float[] v2) {
        float[] v3;
        if (v1.length == v2.length) {
            int N = v1.length;
            v3 = new float[N];
            for (int i = 0; i < N; i += 1) {
                v3[i] = v1[i] * v2[i];
            }
            return v3;
        } else {
            return null;
        }
    }

    //Producto de las componentes de 2 vectores y una constante
    public static float[] prodV(float[] v1, float[] v2, float k) {
        float[] v3;
        if (v1.length == v2.length) {
            int N = v1.length;
            v3 = new float[N];
            for (int i = 0; i < N; i += 1) {
                v3[i] = k * v1[i] * v2[i];
            }
            return v3;
        } else {
            return null;
        }
    }

    //Suma de vectores componente a componente
    public static double[] sumV(double[] v1, double[] v2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            v3 = new double[N];
            for (int i = 0; i < N; i += 1) {
                v3[i] = v1[i] + v2[i];
            }
            return v3;
        } else {
            return null;
        }
    }

    //Suma ponderada de vectores componente a componente (puede tambien ser una resta: w1=1 y w2=-1 o al revés)
    public static double[] sum_wV(double[] v1, double[] v2, double w1, double w2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            v3 = new double[N];
            for (int i = 0; i < N; i += 1) {
                v3[i] = w1 * v1[i] + w2 * v2[i];
            }
            return v3;
        } else {
            return null;
        }
    }

    //Suma ponderada de vectores componente a componente (puede tambien ser una resta: w1=1 y w2=-1 o al revés)
    public static float[] sum_wV(float[] v1, float[] v2, double w1, double w2) {
        float[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            v3 = new float[N];
            for (int i = 0; i < N; i += 1) {
                v3[i] = (float) (w1 * v1[i] + w2 * v2[i]);
            }
            return v3;
        } else {
            return null;
        }
    }

    //Suma num. complejos ponderada de vectores componente a componente (puede tambien ser una resta: w1=1 y w2=-1 o al revés)
    public static MathDatos sum_wV(MathDatos v1, MathDatos v2, double w1, double w2) {
        MathDatos v3;
        float[] v3Re;
        float[] v3Im;
        float[] v1Re = v1.v1();
        float[] v1Im = v1.v2();
        float[] v2Re = v2.v1();
        float[] v2Im = v2.v2();
        if (v1Re.length == v2Re.length) {
            int N = v1Re.length;
            v3Re = new float[N];
            v3Im = new float[N];
            for (int i = 0; i < N; i += 1) {
                v3Re[i] = (float) (w1 * (double) v1Re[i] + w2 * (double) v2Re[i]);
                v3Im[i] = (float) (w1 * (double) v1Im[i] + w2 * (double) v2Im[i]);
                //Log.e("sum_wV","alej: [idx, Rre, Rim] = ["+i+", "+v3Re[i]+", "+v3Im[i]+"]");
            }
            v3 = new MathDatos(v3Re, v3Im);
            return v3;
        } else {
            return null;
        }
    }

    //Division num complejos ponderada de vectores componente a componente (puede tambien ser una resta: w1=1 y w2=-1 o al revés)
    public static MathDatos div_wV(MathDatos v1, MathDatos v2, double w1) {
        MathDatos C = null;
        float[] v3Re;
        float[] v3Im;
        float[] v1Re = v1.v1();
        float[] v1Im = v1.v2();
        float[] v2Re = v2.v1();
        float[] v2Im = v2.v2();
        double M1, M2, Mt, F1, F2, Ft;
        if (v1Re.length == v2Re.length) {
            int N = v1Re.length;
            v3Re = new float[N];
            v3Im = new float[N];
            for (int i = 0; i < N; i += 1) {
                M1 = Math.sqrt(Math.pow(v1Re[i], 2) + Math.pow(v1Im[i], 2));//modulo1
                F1 = Math.atan(v1Im[i] / v1Re[i]);//fase1
                M2 = Math.sqrt(Math.pow(v2Re[i], 2) + Math.pow(v2Im[i], 2));//modulo2
                //F2 = Math.atan2(v2Im[i], v2Re[i]);//fase2
                F2 = Math.atan(v2Im[i] / v2Re[i]);//fase2
                Mt = w1 * M1 / M2;//modulo de la division
                Ft = F1 - F2;//fase de la division de 2 num complejos
                v3Re[i] = (float) (Mt * Math.cos(Ft));
                v3Im[i] = (float) (Mt * Math.sin(Ft));
                //Log.e("div_wV", "alej: [idx, Rre, Rim] = [" + i + ", " + v3Re[i] + ", " + v3Im[i] + "]");
            }
            C = new MathDatos(v3Re, v3Im);
        }
        return C;
    }

    //Modulo de 2 vectores uno a uno
    public static double[] absdBV(double[] v1, double[] v2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            v3 = new double[N];
            for (int i = 0; i < N; i += 1) {
                double M = Math.sqrt(Math.pow(v1[i], 2) + Math.pow(v2[i], 2));//modulo
                v3[i] = 20 * Math.log10(M);//modulo en dB
            }
            return v3;
        } else {
            return null;
        }
    }

    //Modulo de 2 vectores uno a uno (float)
    public static float[] absdBV(float[] v1, float[] v2) {
        float[] v3;
        if (v1.length == v2.length) {
            int N = v1.length;
            v3 = new float[N];
            double M;
            for (int i = 0; i < N; i += 1) {
                M = Math.sqrt(Math.pow(v1[i], 2) + Math.pow(v2[i], 2));//modulo
                v3[i] = (float) (20 * Math.log10(M));//modulo en dB
            }
            return v3;
        } else {
            return null;
        }
    }

    //Modulo de cada una de las componentes de un vector complejo: MathDatos
    public static float[] absdBV(MathDatos a1) {
        float[] v1 = a1.v1();
        float[] v2 = a1.v2();
        return absdBV(v1, v2);
    }

    //Modulo de la combinación de 2 vectores
    public static float[] absdB_wV(MathDatos a1, MathDatos a2, double w1, double w2) {
        float[] v3 = null;
        float[] v1Re = a1.v1();
        float[] v1Im = a1.v2();
        float[] v2Re = a2.v1();
        float[] v2Im = a2.v2();
        double M1, M2;
        if (v1Re.length == v2Re.length) {
            int N = v1Re.length;
            v3 = new float[N];
            for (int i = 0; i < N; i += 1) {
                M1 = Math.sqrt(Math.pow(v1Re[i], 2) + Math.pow(v1Im[i], 2));//modulo1
                M2 = Math.sqrt(Math.pow(v2Re[i], 2) + Math.pow(v2Im[i], 2));//modulo2
                v3[i] = (float) Math.abs(M1 - M2);//diferencia entre los modulos para cada componente del vector
            }
        }
        return v3;
    }

    //Modulo de la combinación de 2 vectores
    public static double[] absdB_wV(MathDatos a1) {
        double[] v3 = null;
        float[] v1Re = a1.v1();
        float[] v1Im = a1.v2();
        double M1, M2;
        if (v1Re.length > 1) {
            int N = v1Re.length;
            v3 = new double[N];
            for (int i = 0; i < N; i += 1) {
                M1 = Math.sqrt(Math.pow(v1Re[i], 2) + Math.pow(v1Im[i], 2));//modulo1
                v3[i] = Math.abs(M1);//diferencia entre los modulos para cada componente del vector
            }
        }
        return v3;
    }

    //Modulo de la combinación de 2 vectores
    public static double[] absDB_wV(MathDatos a1) {
        double[] v3 = null;
        float[] v1Re = a1.v1();
        float[] v1Im = a1.v2();
        double M1, M2;
        if (v1Re.length > 1) {
            int N = v1Re.length;
            v3 = new double[N];
            for (int i = 0; i < N; i += 1) {
                M1 = Math.sqrt(Math.pow(v1Re[i], 2) + Math.pow(v1Im[i], 2));//modulo1
                v3[i] = 20 * Math.log10(M1);//diferencia entre los modulos para cada componente del vector
            }
        }
        return v3;
    }

    //Modulo de 2 vectores en unidades naturales
    public static double[] absV(double[] v1, double[] v2) {
        double[] v3 = null;
        if (v1.length == v2.length) {
            int N = v1.length;
            for (int i = 0; i < N; i += 1) {
                double M = Math.sqrt(Math.pow(v1[i], 2) + Math.pow(v2[i], 2));//modulo
                v3[i] = 20 * Math.log10(M);//modulo en dB
            }
            return v3;
        } else {
            return null;
        }
    }

    //Convertimos un string con los datos devueltos por el VNA a un vector 2D de double
    public static MathDatos vna2ReIm(String S) {
        String[] s11 = S.split(",");//se divide el string en arrays d string con ls digitos en el formato "2.3E-2"
        int N = s11.length;//long del doble de elementos: los partes reales y imaginarias
        float[] v1 = new float[N / 2];
        float[] v2 = new float[N / 2];
        int idx = 0;
        for (int i = 0; i < 1 * N; i += 2) {
            v1[idx] = Float.parseFloat(s11[i]);//parte real
            v2[idx] = Float.parseFloat(s11[i + 1]);//parte imag
            idx += 1;
        }
        MathDatos v3 = new MathDatos(v1, v2);
        return v3;
    }

    //Convertimos un string con los datos devueltos por el VNA a un vector 2D de double
    public static MathDatosD vna2ReImD(String S) {
        String[] s11 = S.split(",");//se divide el string en arrays d string con ls digitos en el formato "2.3E-2"
        int N = s11.length;//long del doble de elementos: los partes reales y imaginarias
        double[] v1 = new double[N / 2];
        double[] v2 = new double[N / 2];
        int idx = 0;
        for (int i = 0; i < 1 * N; i += 2) {
            v1[idx] = Double.parseDouble(s11[i]);//parte real
            v2[idx] = Double.parseDouble(s11[i + 1]);//parte imag
            idx += 1;
        }
        MathDatosD v3 = new MathDatosD(v1, v2);
        return v3;
    }

    public static float[] w_hamming(int N) {
        float[] v3 = new float[N];
        for (int i = 0; i < N; i += 1) {
            v3[i] = (float) (0.54 - 0.46 * Math.cos(2 * Math.PI / (double) (N - 1) * i));
        }
        return v3;
    }

    //Filtrado con una ventana Hamming de la CaL y la Med
    public static MathDatos[] filtrar(MathDatos[] Scal, double dR, double dx) {
        MathDatos R;
        //CaL y Medida
        MathDatos Sb_t = Scal[0];//back
        MathDatos Sr_t = Scal[1];//ref
        MathDatos Sm_t = Scal[2];//med
        int N = Sb_t.v1().length;
        int L = 2 * (int) (dR / (2 * dx));//ancho del filtro en num. de muestras: siempre es PAR!!
        float[] SbRe2 = new float[N];
        float[] SbIm2 = new float[N];
        float[] SrRe2 = new float[N];
        float[] SrIm2 = new float[N];
        float[] SmRe2 = new float[N];
        float[] SmIm2 = new float[N];
        float[] filt;

        //Posicion de filtrado: distancia a la que ocurre la reflex.
        float[] dist = absdB_wV(Sb_t, Sr_t, 1, -1);
        int pos_max = indexOf(dist, NumberUtils.max(dist));
        int pos1 = (int) pos_max - (L / 2);
        int pos2 = (int) pos_max + (L / 2);

        //Multiplicacion de la parte interés del vector por el filtro
        float[] SbRe_part = ArrayUtils.subarray(Sb_t.v1(), pos1, pos2);//la pos2 no está incluida en el array
        float[] SbIm_part = ArrayUtils.subarray(Sb_t.v2(), pos1, pos2);
        float[] SrRe_part = ArrayUtils.subarray(Sr_t.v1(), pos1, pos2);//la pos2 no está incluida en el array
        float[] SrIm_part = ArrayUtils.subarray(Sr_t.v2(), pos1, pos2);
        float[] SmRe_part = ArrayUtils.subarray(Sm_t.v1(), pos1, pos2);//la pos2 no está incluida en el array
        float[] SmIm_part = ArrayUtils.subarray(Sm_t.v2(), pos1, pos2);
        filt = w_hamming(L);//hamming window
        float k = (float) (1 / N);//(la división es convertida a un INT antes de hacerse: redondeada a cero; y luego ese 0, pasado a float: 0.0)cte para constrarrestar el efecto de la fft (que necesita ser multiplicada por el factor 1/Nfft)
        double k1 = 1 / (double) N;//así si ven los decimales
        Log.e("MathV.filtrar", "alej: [N, L, k, pos_max, pos1, pos2] = [" + N + ", " + L + ", " + k1 + ", " + pos_max + ", " + pos1 + ", " + pos2 + "]");
        //Background
        SbRe_part = prodV(SbRe_part, filt);
        SbIm_part = prodV(SbIm_part, filt);
        System.arraycopy(SbRe_part, 0, SbRe2, pos1, L);
        System.arraycopy(SbIm_part, 0, SbIm2, pos1, L);
        Sb_t = new MathDatos(SbRe2, SbIm2);
        //Reference
        SrRe_part = prodV(SrRe_part, filt);
        SrIm_part = prodV(SrIm_part, filt);
        System.arraycopy(SrRe_part, 0, SrRe2, pos1, L);
        System.arraycopy(SrIm_part, 0, SrIm2, pos1, L);
        Sr_t = new MathDatos(SrRe2, SrIm2);
        //Medida
        SmRe_part = prodV(SmRe_part, filt);
        SmIm_part = prodV(SmIm_part, filt);
        System.arraycopy(SmRe_part, 0, SmRe2, pos1, L);
        System.arraycopy(SmIm_part, 0, SmIm2, pos1, L);
        Sm_t = new MathDatos(SmRe2, SmIm2);

        //Log.e("MathV.filtrar", "alej: [Sb_t, Sr_t, Sm_t] = [" + Sb_t.v1()[pos_max-L/2-1] + ", " + Sr_t.v1()[pos_max-L/2-1] + ", " + Sm_t.v1()[pos_max-L/2-1] + "]");

        //S-Matrix filtrada: lista para la CaL y la Med
        Scal[0] = Sb_t;
        Scal[1] = Sr_t;
        Scal[2] = Sm_t;

        //Test: filtro
        //float[] cero=new float[N];
        //Sm_t2 = new MathDatos(filt, cero);

        return Scal;
    }

    //Filtrado con una ventana Hamming de la CaL y la Med (Scal_t: valores en el dominio del espacio, no freq!)
    public static MathDatos[] calBackRef(MathDatos[] Scal_t, int Norig) {
        MathDatos[] R = new MathDatos[3];

        //Matriz de CaL filtrada
        MathDatos Sb_t = Scal_t[0];
        MathDatos Sr_t = Scal_t[1];
        MathDatos Sm_t = Scal_t[2];

        //----- FFT: con la librería "neoncore" ------------------
        float[] Sb2Re = Sb_t.v1();
        float[] Sb2Im = Sb_t.v2();
        float[] Sr2Re = Sr_t.v1();
        float[] Sr2Im = Sr_t.v2();
        float[] Sm2Re = Sm_t.v1();
        float[] Sm2Im = Sm_t.v2();
        int N = Sm2Im.length;
        double k = (1 / (double) N);//factor q necesita corregirse al hacer "y=FFT(IFFT(y))*1/N"

        FaCollection.fft_float32(Sb2Re, Sb2Im);
        FaCollection.fft_float32(Sr2Re, Sr2Im);
        FaCollection.fft_float32(Sm2Re, Sm2Im);


        //Estandares de CaL en la freq (ya filtrados en espacio): reusamos los objetos, para instanciar más
        MathDatos Sb2 = new MathDatos(Sb2Re, Sb2Im);//freq
        MathDatos Sr2 = new MathDatos(Sr2Re, Sr2Im);
        MathDatos Sm2 = new MathDatos(Sm2Re, Sm2Im);
        //------------------------------------------------------------

        //-----FFT: Se hace con la librería Apache Commons Math v4---
        //Valores en el espacio del Back, Ref y Med filtrados: multiplicados por la ventana
        double[][] Sb_tt = new double[2][];
        double[][] Sr_tt = new double[2][];
        double[][] Sm_tt = new double[2][];
        Sb_tt[0] = float2double(Sb_t.v1());
        Sb_tt[1] = float2double(Sb_t.v2());
        Sr_tt[0] = float2double(Sr_t.v1());
        Sr_tt[1] = float2double(Sr_t.v2());
        Sm_tt[0] = float2double(Sm_t.v1());
        Sm_tt[1] = float2double(Sm_t.v2());
        //Los valores filtrados son pasados al dominio de la freq (FFT): el resultado se almacena en la misma variable de entrada: paso de param. por referencia
        FastFourierTransform fft = new FastFourierTransform(FastFourierTransform.Norm.STD, false);
        fft.transformInPlace(Sb_tt);
        fft.transformInPlace(Sr_tt);
        fft.transformInPlace(Sm_tt);

        //Se pasa del formato de Matriz de 2 vectores Double con [Re Im] a MathDatos que contiene Re Im como vectores Float
        MathDatos Sb3 = new MathDatos(MathV.double2float(Sb_tt)[0], MathV.double2float(Sb_tt)[1]);
        MathDatos Sr3 = new MathDatos(MathV.double2float(Sr_tt)[0], MathV.double2float(Sr_tt)[1]);
        MathDatos Sm3 = new MathDatos(MathV.double2float(Sm_tt)[0], MathV.double2float(Sm_tt)[1]);

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("¿Estás seguro?");
//        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                // Código que se ejecutará al hacer clic en el botón "Sí"
//            }
//        });
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                // Código que se ejecutará al hacer clic en el botón "No"
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();


        //Se pisan los valores de la anterior transformada
        Sb2 = Sb3;
        Sr2 = Sr3;
        Sm2 = Sm3;
        //------------------------------------------------------------



        //-----FFT: Se hace con la librería Apache Commons Math v4---
//        double[][] Sb_tt = new double[2][];
//        double[][] Sr_tt = new double[2][];
//        double[][] Sm_tt = new double[2][];
//        Sb_tt[0] = float2double(Sb2Re);
//        Sb_tt[1] = float2double(Sb2Im);
//        Sr_tt[0] = float2double(Sr2Re);
//        Sr_tt[1] = float2double(Sr2Im);
//        Sm_tt[0] = float2double(Sm2Re);
//        Sm_tt[1] = float2double(Sm2Im);
//
//        FastFourierTransform fft = new FastFourierTransform(FastFourierTransform.Norm.STD, false);
//        fft.transformInPlace(Sb_tt);
//        fft.transformInPlace(Sr_tt);
//        fft.transformInPlace(Sm_tt);
//
//        //Estandares de CaL en la freq (ya filtrados en espacio): reusamos los objetos, para instanciar más
//        MathDatos Sb2 = new MathDatos(MathV.double2float(Sb_tt)[0], MathV.double2float(Sb_tt)[1]);//freq
//        MathDatos Sr2 = new MathDatos(MathV.double2float(Sr_tt)[0], MathV.double2float(Sr_tt)[1]);
//        MathDatos Sm2 = new MathDatos(MathV.double2float(Sm_tt)[0], MathV.double2float(Sm_tt)[1]);
        //-----------------------

        //CaL: calibración sencilla: R=Rpec*(Sm-Sb)/(Sr-Sb)=-1*(Sm-Sb)/(Sr-Sb)!!
        MathDatos s1 = sum_wV(Sm2, Sb2, 1, -1);
        MathDatos s2 = sum_wV(Sr2, Sb2, 1, -1);
        MathDatos r = div_wV(s1, s2, -1);
        r = new MathDatos(Arrays.copyOf(r.v1(), Norig), Arrays.copyOf(r.v2(), Norig));//nos quedamos solo con los N (201) primeros puntos, quitando el zero-padding
        R[0] = r;
        //R[0] = s1;
        //R[1] = s2;
        //R[2] = r;

        //Log.e("MathV.filtrar", "alej: [N, k, R, Sb2, Sr2, Sm2] = [" + N + ", " + k + ", " + r.v1()[0] + ", " + Sb_t.v1()[0] + ", " + Sr_t.v1()[0] + ", " + Sm_t.v1()[0] + "]");

        return R;
    }


    //Metodo para pintar una gráfica en unos ejes dando la curva y los ejes
    public static void pintarSerie(LineGraphSeries<DataPoint> mSerie, GraphView graph, int cl, MathDatos Sm, double dx, double[] xlim, double[] ylim) {
        graph.addSeries(mSerie);//añadimos la serie a los ejes
        mSerie.setColor(cl);
        float[] Sm_Re_t = Sm.v1();
        float[] Sm_Im_t = Sm.v2();
        int Nfft = Sm_Re_t.length;
        DataPoint[] points3 = new DataPoint[Nfft];
        //Pintamos la IFFT
        for (int i = 0; i < Nfft; i += 1) {
            double M = Math.sqrt(Math.pow(Sm_Re_t[i], 2) + Math.pow(Sm_Im_t[i], 2));//modulo
            M = 20 * Math.log10(M);//modulo en dB
            double x = (double) i * dx / 2;//retardo de ida y vuelta
            points3[i] = new DataPoint(x, M);
            mSerie.appendData(points3[i], true, Nfft);
        }
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(ylim[0]);
        graph.getViewport().setMaxY(ylim[1]);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(xlim[0]);
        graph.getViewport().setMaxX(xlim[1]);
    }

    //Metodo para pintar una gráfica en unos ejes, dando los ejes y creando un objeto curva en cada llamada
    public static void pintarSerie(GraphView graph, int cl, MathDatos Sm, double dx, double[] xlim, double[] ylim) {
        LineGraphSeries<DataPoint> mSerie = new LineGraphSeries<>();
        graph.addSeries(mSerie);//añadimos la serie a los ejes
        mSerie.setColor(cl);
        float[] Sm_Re_t = Sm.v1();
        float[] Sm_Im_t = Sm.v2();
        int Nfft = Sm_Re_t.length;
        DataPoint[] points3 = new DataPoint[Nfft];
        //Pintamos la IFFT
        for (int i = 0; i < Nfft; i += 1) {
            double M = Math.sqrt(Math.pow(Sm_Re_t[i], 2) + Math.pow(Sm_Im_t[i], 2));//modulo
            M = 20 * Math.log10(M);//modulo en dB
            double x = (double) i * dx;//retardo de ida y vuelta
            points3[i] = new DataPoint(x, M);
            mSerie.appendData(points3[i], true, Nfft);
        }
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(ylim[0]);
        graph.getViewport().setMaxY(ylim[1]);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(xlim[0]);
        graph.getViewport().setMaxX(xlim[1]);
    }

    //(para "float")Metodo para pintar una gráfica en unos ejes, dando los ejes y creando un objeto curva en cada llamada
    public static void pintarSerie(GraphView graph, int cl, MathDatos Sm, double dx, double[] xlim, double[] ylim, double fini, int Nmax) {
        LineGraphSeries<DataPoint> mSerie = new LineGraphSeries<>();
        graph.addSeries(mSerie);//añadimos la serie a los ejes
        mSerie.setColor(cl);
        float[] Sm_Re_t = Sm.v1();
        float[] Sm_Im_t = Sm.v2();
        //int Nfft = Sm_Re_t.length
        int Nfft = Nmax;
        DataPoint[] points3 = new DataPoint[Nfft];
        //Pintamos la IFFT
        double[] Mlist = new double[Nmax];
        double M;
        for (int i = 0; i < Nfft; i += 1) {
            M = Math.sqrt(Math.pow(Sm_Re_t[i], 2) + Math.pow(Sm_Im_t[i], 2));//modulo
            M = 20 * Math.log10(M);//modulo en dB
            Mlist[i] = M;
            double x = fini + i * dx;//retardo de ida y vuelta
            points3[i] = new DataPoint(x, M);
            mSerie.appendData(points3[i], true, Nfft);
        }

        //Pintar los puntos donde se alcanzan los mínimos
        positionValor minLocales = min_max(Mlist, "min");
        if (!(minLocales == null)) { //Si existen mínimos, se pintan!
            PointsGraphSeries<DataPoint> serie_point = new PointsGraphSeries<>();
            graph.addSeries(serie_point);
            serie_point.setShape(PointsGraphSeries.Shape.POINT);
            for (int i = 0; i < minLocales.idx.length; i += 1) {
                double x = fini + minLocales.idx[i] * dx;//freq del minimo
                points3[i] = new DataPoint(x, minLocales.valor[i]);
                serie_point.appendData(points3[i], true, minLocales.idx.length);
            }
        }

        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(ylim[0]);
        graph.getViewport().setMaxY(ylim[1]);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(xlim[0]);
        graph.getViewport().setMaxX(xlim[1]);
    }

    //(para "double")Metodo para pintar una gráfica en unos ejes, dando los ejes y creando un objeto curva en cada llamada
    public static void pintarSerie(GraphView graph, int cl, MathDatosD Sm, double dx, double[] xlim, double[] ylim, double fini, int Nmax) {
        LineGraphSeries<DataPoint> mSerie = new LineGraphSeries<>();
        graph.addSeries(mSerie);//añadimos la serie a los ejes
        mSerie.setColor(cl);
        double[] Sm_Re_t = Sm.v1();
        double[] Sm_Im_t = Sm.v2();
        //int Nfft = Sm_Re_t.length
        int Nfft = Nmax;
        DataPoint[] points3 = new DataPoint[Nfft];
        //Pintamos la IFFT
        for (int i = 0; i < Nfft; i += 1) {
            double M = Math.sqrt(Math.pow(Sm_Re_t[i], 2) + Math.pow(Sm_Im_t[i], 2));//modulo
            M = 20 * Math.log10(M);//modulo en dB
            double x = fini + i * dx;//retardo de ida y vuelta
            points3[i] = new DataPoint(x, M);
            mSerie.appendData(points3[i], true, Nfft);
        }
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(ylim[0]);
        graph.getViewport().setMaxY(ylim[1]);
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(xlim[0]);
        graph.getViewport().setMaxX(xlim[1]);
    }

    //Calcula la media de las partes Re y Im del array conteniendo las medidas
    public static MathDatos media(ArrayList<MathDatos> S11list) {
        MathDatos m = null;
        int Nmeas = S11list.size();//num de medidas en el array
        int Nfreq;
        float[] v3Re, v3Im;

        if (Nmeas > 1) { //Si hay elementos en la lista, realizamos la media
            Nfreq = S11list.get(0).v1().length;//num de puntos de freq
            //Log.e("MathV.Media", "alej: [Nmeas, Nfreq] = [" + Nmeas + ", " + Nfreq + "]");
            double k = (1 / (double) Nmeas);
            MathDatos aux = new MathDatos(new float[Nfreq], new float[Nfreq]);//vector 0;
            for (int j = 0; j < Nmeas; j += 1) {
                aux = sum_wV(aux, S11list.get(j), 1, k);
            }
            m = aux;
        }
        if (Nmeas == 1) { //Sólo hay 1 solo elemento: la media es él mismo
            m = S11list.get(0);
        }
        return m;
    }

    //Calcula la media de una nueva medida (Add)de las partes Re y Im del array: a partir de la media anterior
    public static MathDatosD mediaAdd(MathDatos Rmed, MathDatosD Rmean, int Nmeas) {
        MathDatosD m = null;
        double Nmed = (double) Nmeas;
        if (!(Rmean == null)) { //Si hay elementos en la lista, realizamos la media
            int Nfreq = Rmed.v1().length;//num de puntos de freq
            double M;
            double[] vRe = new double[Nfreq];
            double[] vIm = new double[Nfreq];
            for (int i = 0; i < Nfreq; i += 1) {
                M = Math.sqrt(Math.pow(Rmed.v1()[i], 2) + Math.pow(Rmed.v2()[i], 2));//medias de los módulos en unidades naturales
                vRe[i] = (Rmean.v1()[i] * (Nmed - 1) + M) / Nmed;
            }
            m = new MathDatosD(vRe, vIm);
        }
        if (Nmeas == 1) { //Sólo hay 1 solo elemento: la media es él mismo
            m = new MathDatosD(absdB_wV(Rmed), new double[Rmed.v1().length]);//modulo de la 1a medida (parte Im es cero y la Re = modulo)
        }
        return m;
    }

    //Calcula la media anterior a la medida actual (Del)de las partes Re y Im del array: a partir de la media anterior
    public static MathDatosD mediaDel(MathDatos Rmed, MathDatosD Rmean, int Nmeas) {
        MathDatosD m = null;
        double Nmed = (double) Nmeas;
        //if (Nmeas > 1) { //Si hay elementos en la lista, realizamos la media
        if (!(Rmean == null)) { //Si hay elementos en la lista, realizamos la media
            int Nfreq = Rmed.v1().length;//num de puntos de freq
            double M;
            double[] vRe = new double[Nfreq];
            double[] vIm = new double[Nfreq];
            for (int i = 0; i < Nfreq; i += 1) {
                M = Math.sqrt(Math.pow(Rmed.v1()[i], 2) + Math.pow(Rmed.v2()[i], 2));//medias de los módulos en unidades naturales
                vRe[i] = (Rmean.v1()[i] * Nmed - M) / (Nmed - 1);
            }
            m = new MathDatosD(vRe, vIm);
        }
        if (Nmeas == 1) { //Sólo hay 1 solo elemento: la media es él mismo
            m = new MathDatosD(absdB_wV(Rmed), new double[Rmed.v1().length]);//modulo de la 1a medida (parte Im es cero y la Re = modulo)
        }
        return m;
    }

    //Devuelve el menor de los minimos(maximo) locales del vector y
    public static positionValor min_max(double[] y, String tipo) {
        positionValor info = null;
        double[] minLoc = null;
        double[] pos = null;

        double minmax = 0;
        int N = y.length;
        double[] ysg = new double[N - 1];
        double[] ymin = new double[N - 2];

        //calcula el incremento en cada punto: dy=y'(x)*dx
        for (int i = 0; i < N - 1; i += 1) {
            ysg[i] = y[i + 1] - y[i];
            //obtenemos el signo del incremento o derivada
            if (ysg[i] >= 0) {
                ysg[i] = 1;
            } else {
                ysg[i] = -1;
            }
        }

        //Vemos cuando existe un cambio de signo al pasar de un punto a otro: 0 (no hay cambio de signo), 2 (minimo local) y -2 (maximo loca)
        for (int i = 0; i < N - 2; i += 1) {
            ymin[i] = ysg[i + 1] - ysg[i];
            if (tipo.equals("min")) { //Devuelve la pos de los minimo locales
                if (ymin[i] == 2) {
                    minLoc = add(minLoc, y[i + 1]); //incrementamos en 1 la pos del min, para contrarrestar el efecto de tener "ysg[]" N-1 elementos en vez de N
                    pos = add(pos, i + 1);
                    //Log.e("MathV.min_max", "alej: Minimo local [idx, value] = [" + (i + 1) + ", " + y[i + 1] + "]");
                }
            } else { //Devuelve la posición de los max
                if (ymin[i] == -2) {
                    minLoc = add(minLoc, y[i + 1]);
                    pos = add(pos, i + 1);
                }
            }
        }

        //Devolvemos el menor de los minimos obtenidos y su posición dentro del array (en caso de que no existan minimos, se devuelve "null")
        if (!(minLoc == null)) {
            info = new positionValor(pos, minLoc);
        }
        return info;
    }

    public static int contadorCondicion(double[] v, double L) {
        int cnt = 0;
        int N = v.length;
        for (int i = 0; i < N; i += 1) {
            if (v[i] <= L) {
                cnt += 1;
            }
        }
        return cnt;
    }

    //Convierte array de Float a array de Double
    public static double[] float2double(float[] dat) {
        double[] datD = new double[dat.length];
        for (int i = 0; i < dat.length; i++) {
            Float float_obj1 = new Float(dat[i]); //crea objeto Float
            double double_obj = float_obj1.doubleValue(); //invoca al método de esta claes que lo convierte a double
            datD[i] = double_obj; //rellena un vector de doubles
        }
        return datD;
    }

    //Convierte array de Double a array de Float
    public static float[] double2float(double[] dat) {
        float[] datF = new float[dat.length];
        for (int i = 0; i < dat.length; i++) {
            Double float_obj1 = new Double(dat[i]); //crea objeto Double
            float float_obj = float_obj1.floatValue(); //invoca al método de esta claes que lo convierte a Float
            datF[i] = float_obj; //rellena un vector de doubles
        }
        return datF;
    }

    //Convierte doble array de Doube a array doble de Float
    public static float[][] double2float(double[][] dat) {
        //
        float[][] datF = new float[dat.length][];
        for (int i = 0; i < dat.length; i++) {
            datF[i] = double2float(dat[i]);
        }
        return datF;
    }
}


class MathDatos {
    private float[] Re = null;
    private float[] Im = null;

    //Constructor
    public MathDatos(float[] Re, float[] Im) {
        setReIm(Re, Im);
    }

    //Devuelve parte real
    public float[] v1() {
        return Re;
    }

    //Devuelve parte imaginaria
    public float[] v2() {
        return Im;
    }

    //Cambiamos el valor de la parte real e imaginaria del vector de num  complejos
    public void setReIm(float[] re, float[] im) {
        Re = re;
        Im = im;
    }

    //Pasa el vector a un string como una lista separadas por comas (la parte real e im de cada componente del vector tb van separadas por coma)
    public String toString() {
        String str = "";
        int N = Re.length;
        for (int i = 0; i < N - 1; i += 1) {
            str = str + Re[i] + "," + Im[i] + ",";
        }
        str = str + Re[N - 1] + "," + Im[N - 1];
        return str;
    }
}

class MathDatosD {
    private double[] Re = null;
    private double[] Im = null;

    //Constructor
    public MathDatosD(double[] Re, double[] Im) {
        setReIm(Re, Im);
    }

    //Constructor: se convierte a double el float
    public MathDatosD(MathDatos C) {
        int N = C.v1().length;
        double[] re = new double[N];
        double[] im = new double[N];
        for (int i = 0; i < N; i += 1) {
            re[i] = (double) C.v1()[i];
            im[i] = (double) C.v2()[i];
        }
        setReIm(re, im);
    }

    //Devuelve parte real
    public double[] v1() {
        return Re;
    }

    //Devuelve parte imaginaria
    public double[] v2() {
        return Im;
    }

    //Cambiamos el valor de la parte real e imaginaria del vector de num  complejos
    public void setReIm(double[] re, double[] im) {
        Re = re;
        Im = im;
    }

    //Pasa el vector a un string como una lista separadas por comas (la parte real e im de cada componente del vector tb van separadas por coma)
    public String toString() {
        String str = "";
        int N = Re.length;
        for (int i = 0; i < N - 1; i += 1) {
            str = str + Re[i] + "," + Im[i] + ",";
        }
        str = str + Re[N - 1] + "," + Im[N - 1];
        return str;
    }
}

//Clase con los valores de configuración para pintar las medidas en freq!
class confFreq {
    public double df;
    public double[] xlimF;
    public double[] ylimF;
    public double fini;
    public double fstop;
    public int N;

    public confFreq(double df, double[] xlimF, double[] ylimF, double fini, double fstop, int N) {
        this.xlimF = xlimF;
        this.ylimF = ylimF;
        this.N = N;
        this.df = df;
        this.fini = fini;
        this.fstop = fstop;
    }

}

class criterioCurva {
    private double[] ymod; //modulo de la medida, sobre la que aplicaremos cierto criterio
    private double[] freq;

    public criterioCurva(double[] f, double[] y) {
        ymod = y;
        freq = f;
    }

    //Criterio para mono banda sobre metal en banda X
    public int BandaXmetal(double fo) {
        //Los valores devueltos podrán ser: 0 (curva erronea), 1 (No pintar), 2 (Pintar un poco), 3 (Correcto), 4(Pintar más)
        String LOGATG = "BandaXmetal";
        int resultado = 0;
        double S11maxdB = -10;//umbral: max valor del S11 permitido
        double df = freq[2] - freq[1];
        double bw = 0.5;//0.2GHz entorno a "fo"
        int dpos = (int) (bw / df);
        ;
        if (dpos == 0) {
            dpos = 1;
        }
        int posfo = indexOf(freq, fo, 0, df);


        //Comprobar si la curva es errónea: (código 5), Si tiene 2 picos <-10dB o la media es <-10dB (medida del background o antena mal apoyada o superficie curva) o media es > -3dB (midiendo metal)
        positionValor minLocAll = min_max(ymod, "min");
        int N = ymod.length;
        double ymeandB = 0;
        for (int i = 0; i < ymod.length; i += 1) {
            ymeandB += ymod[i];
        }
        ymeandB = ymeandB / N;
        Log.e(LOGATG, "alej: Media: " + ymeandB);
        //Media con valor muy bajo (medida al aire) o muy alto (medida metal)
        if (ymeandB >= -2 || (ymeandB < S11maxdB)) {
            Log.e("BandaXmetal", "alej: Curva Erronea ymeas = " + ymeandB);
            return 5;
        }

        //Multiples picos de absorción con baja atenuación: estaríamos midiendo un esquema de doble banda o cualquier otra curva errónea
        int cnt = 0;//contador d picos con |S11|<-5dB (picos entre -5 y -10 son considerados válidos, pero de poca atenuación)
        for (int i = 0; i < minLocAll.valor.length; i += 1) {
            if (minLocAll.valor[i] <= S11maxdB / 2) { //-5dB
                cnt += 1;
            }
            if (cnt > 1) {
                Log.e("BandaXmetal", "alej: Curva Erronea Npicos <-10dB = " + cnt);
                return 5;
            }

        }

        //Cumple la especificación en la freq "fo" (+-200MHz)
        int dposC = (int) (0.1 / df);//
        double[] yfo_bw = ArrayUtils.subarray(ymod, posfo - dposC, posfo + dposC + 1);//(ymod entorno al punto "fo")
        double yminfo = min(yfo_bw);
        if (yminfo <= S11maxdB) { //Comprobamos si la curva de S11 en fo (+-100MHz) alcanza los -10dB (Att. suficiente, aunque el pico pueda estar en otra freq)
            Log.e("BandaXmetal", "alej: Resultado 3: Cumple Spec [~fo, S11] = [" + freq[posfo] + ", " + yminfo + "]");
            return 3;
        } else { //Miramos si existe pico alrededor de fo +-500MHz
            yfo_bw = ArrayUtils.subarray(ymod, posfo - dpos, posfo + dpos + 1);//(ymod entorno al punto "fo")
            double[] freq_bw = ArrayUtils.subarray(freq, posfo - dpos, posfo + dpos + 1);//(ymod entorno al punto "fo")
            positionValor minLocyofo = min_max(yfo_bw, "min");
            if (!(minLocyofo == null)) {
                double kk = min(minLocyofo.valor);
                int kk_pos = indexOf(minLocyofo.valor, kk, 0);
                Log.e("BandaXmetal", "alej: Resultado -3: Cumple Spec Baja Atenuación  [fmin, S11] = [" + freq_bw[(int) minLocyofo.idx[kk_pos]] + ", " + yfo_bw[kk_pos] + "]");
                return -3; //existe un pico en fo, pero con poca atenuación
            }
        }

        //Comprobar si existe minimo de 2 a fo GHz
        double[] y1 = ArrayUtils.subarray(ymod, 0, posfo - dpos);
        double[] f1 = ArrayUtils.subarray(freq, 0, posfo - dpos);
        int flag1 = 0;
        double freq1 = 0;
        positionValor minLoc1 = min_max(y1, "min");
        if (!(minLoc1 == null)) {
            int cnt10dB = contadorCondicion(minLoc1.valor, S11maxdB);
            int cnt5dB = contadorCondicion(minLoc1.valor, S11maxdB / 2);
            int cnt3dB = contadorCondicion(minLoc1.valor, -3);
            //if (cnt3dB > 0 && cnt5dB == 0) { //Picos entre -3 y -5dB son considerados como picos de baja atenuación (si son >-3dB no se consideran picos)
            if (cnt5dB > 0 && cnt10dB == 0) { //Picos entre -5 y -10dB son considerados como picos de baja atenuación (si son >-3dB no se consideran picos)
                flag1 = -1;
                double y1min = min(minLoc1.valor);
                int ymin1_pos = indexOf(minLoc1.valor, y1min, 0);
                freq1 = f1[(int) minLoc1.idx[ymin1_pos]];//freq del minimo1
            }
            if (cnt10dB > 0) {
                double y1min = min(minLoc1.valor);
                int ymin1_pos = indexOf(minLoc1.valor, y1min, 0);
                freq1 = f1[(int) minLoc1.idx[ymin1_pos]];//freq del minimo1
                flag1 = 1;
            }
            Log.e("BandaXmetal", "alej: [freq1, cnt5dB] = [" + freq1 + ", " + cnt5dB + "]");
        }

        //Comprobar si existe minimo entre fo y 18GHz
        double[] y2 = ArrayUtils.subarray(ymod, posfo + dpos, ymod.length);
        double[] f2 = ArrayUtils.subarray(freq, posfo + dpos, ymod.length);
        int flag2 = 0;
        double freq2 = 0;
        positionValor minLoc2 = min_max(y2, "min");
        if (!(minLoc2 == null)) {
            int cnt10dB = contadorCondicion(minLoc2.valor, S11maxdB);
            int cnt5dB = contadorCondicion(minLoc2.valor, S11maxdB / 2);
            int cnt3dB = contadorCondicion(minLoc2.valor, -3);
            //if (cnt3dB > 0 && cnt5dB == 0) { //Picos entre -3 y -5dB son considerados como picos de baja atenuación (si son >-3dB no se consideran picos)
            if (cnt5dB > 0 && cnt10dB == 0) { //Picos entre -5 y -10dB son considerados como picos de baja atenuación (si son >-3dB no se consideran picos)
                double y2min = min(minLoc2.valor);
                int ymin2_pos = indexOf(minLoc2.valor, y2min, 0);
                freq2 = f2[(int) minLoc2.idx[ymin2_pos]];//freq del minimo1
                flag2 = -1;
            }
            if (cnt5dB > 0) { //si existe algún pico cuya atenuación sea <-5dB, se considera que existe pico
                double y2min = min(minLoc2.valor);
                int ymin2_pos = indexOf(minLoc2.valor, y2min, 0);
                freq2 = f2[(int) minLoc2.idx[ymin2_pos]];//freq del minimo1
                flag2 = 1;
            }
            Log.e("BandaXmetal", "alej: [freq2, cnt5dB] = [" + freq2 + ", " + cnt5dB + "]");
        }


        //Pico entre 2 y fo GHz:
        if (Math.abs(flag1) == 1) { //Pico con al menos atenuación <-5dB
            if (freq1 < 8) {
                Log.e("BandaXmetal", "alej: Resultado 1: Parar de Pintar: fmin = " + freq1);
                resultado = 1;
            } else {
                Log.e("BandaXmetal", "alej: Resultado 2: No pintar Mucho: fmin = " + freq1);
                resultado = 2;
            }
            return resultado * flag1;//el signo distingue si tenemos atenuación >-10dB
        }

        //Pico entre fo y 18 GHz: No seguir pintando!
        if (Math.abs(flag2) == 1) { //Pico con al menos atenuación <-5dB
            Log.e("BandaXmetal", "alej: Resultado 4: Pintar flag2 = " + flag2);
            return 4 * flag2;//el signo distingue si tenemos atenuación >-10dB
        }

        //Si no existen ningún minimo
        Log.e("BandaXmetal", "alej: Resultado 0: No existen mínimos o tienen atenuación <-6dB! Media: " + ymeandB);
        //return 0;//no muestra icono del tipo de curva
        return 5;//icono de curva erronea
    }
}

//Util cuando queremos devolver 2 parámetros desde una función
class positionValor {
    public double[] idx;
    public double[] valor;

    public positionValor(double[] idx, double[] valor) {
        this.idx = idx;
        this.valor = valor;
    }
}


//class MediaV{
//    private MathDatos v; //vector complejo conteniendo la media actual
//    private double cont; //contador del numero de curvas promediadas hasta el momento
//
//}