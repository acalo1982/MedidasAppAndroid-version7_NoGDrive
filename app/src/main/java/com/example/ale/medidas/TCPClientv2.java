package com.example.ale.medidas;

/**
 * Created by ale on 25/04/2017.
 */

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClientv2 {

    //private String serverMessage;
    //public static final String SERVERIP = "10.1.18.121";
    //public static final int SERVERPORT = 23;
    //public static final String SERVERIP = "192.168.173.1";
    public static final String SERVERIP = "10.1.18.111";
    public static final int SERVERPORT = 5023; //test con servidor echo
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;
    Socket socket;


    public void abrirSocket() {
        mRun = true;
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Log.e("TCP Client", "C: Connecting...");
            this.socket = new Socket(serverAddr, SERVERPORT);
            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.e("TCPClientv2", "alej: Socket con el servidor Abierto!");

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    public void sendMessage(String message) {
        //Envía comandos sin espera de una respuesta: p.e comandos de configuración del VNA
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public String leerMessage() {
        //Leemos datos del buffer de entrada
        if (in != null) {
            try {
                String msg = in.readLine(); //comprobamos si ha llegado algo al servidor
                return msg;
            }catch (Exception e) {
                Log.e("TCP", "S: Error", e);
                return null;
            }
        }else{
            return null;
                }
    }

    public void stopClient(){
        mRun = false;
    }

}