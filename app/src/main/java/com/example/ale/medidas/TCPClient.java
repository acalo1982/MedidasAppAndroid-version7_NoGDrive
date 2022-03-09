package com.example.ale.medidas;

/**
 * Created by ale on 24/04/2017.
 */

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private String serverMessage = null;
    private String SERVERIP = "10.1.18.121";//valores por defecto
    private int SERVERPORT = 5025; //valor por defecto, en caso de que no se de este parametro en el constructor de esta clase
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    public String battery="-1";

    PrintWriter out;
    BufferedReader in;
    Socket socket;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener; //asociamos la interfaz con un objeto externo que la implemente (de esta forma , TCPCLient puede invocar a un método de ese obj externo)
    }

    public TCPClient(OnMessageReceived listener, String ip, String port) {
        mMessageListener = listener;//asociamos la interfaz con un objeto externo que la implemente (de esta forma , TCPCLient puede invocar a un método de ese obj externo)
        SERVERIP=ip;//dirección IP
        SERVERPORT=Integer.parseInt(port);//puerto
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */

    public void sendMessage(String message) {
        //Envía comandos sin espera de una respuesta: p.e comandos de configuración del VNA
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public String getMsg() {
        return serverMessage;
    }

    public void stopClient() {
        mRun = false;
    }

    public void run(String txt) {
        mRun = true;
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Log.e("TCP Client", "C: Connecting...");
            //create a socket to make the connection with the server
            //Socket socket = new Socket(serverAddr, SERVERPORT);
            this.socket = new Socket(serverAddr, SERVERPORT);
            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.e("TCPClient", "alej: Socket con el servidor Abierto para 1 comando!");


                //in this while the client listens for the messages sent by the server
                //while (mRun) {
//                    serverMessage = in.readLine(); //comprobamos si ha llegado algo al servidor
//
//                    if (serverMessage != null && mMessageListener != null) {
//                        //call the method messageReceived from MyActivity class
//                        mMessageListener.messageReceived(serverMessage);
//                    }
//                    serverMessage = null;
                //}

                sendMessage(txt);
                String str_end = txt.substring(txt.length() - 1);
                if (str_end.equals("?")) {
                    serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        //Log.e("TCPClient", "alej: Received Message: '" + serverMessage + "'");
                        if (txt.equals(":SYSTem:BATTery:ABSCharge?")) {
                            battery=serverMessage; //Se almacena el valor del porcentaje de batería del VNA
                            mMessageListener.messageReceived("Bateria");
                        }else{
                            mMessageListener.messageReceived(serverMessage);
                        }
                    }
                    serverMessage = null;
                }
                socket.close();

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
                mMessageListener.messageReceived("Error"); //servidor no responde
            }
            //finally {
            //the socket must be closed. It is not possible to reconnect to this socket
            // after it is closed, which means a new socket instance has to be created.
            //socket.close();
            //}

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
            mMessageListener.messageReceived("Error"); //cliente no conectado a la misma red que el servidor o sin conexión de red
            //Toast.makeText(getActivity(), "Lectura del Reference: Espere!", Toast.LENGTH_SHORT).show();
        }

    }

    public void run(String[] txt_list) {
        mRun = true;
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Log.e("TCP Client", "C: Connecting...");
            //create a socket to make the connection with the server
            //Socket socket = new Socket(serverAddr, SERVERPORT);
            this.socket = new Socket(serverAddr, SERVERPORT);

            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);//send the message to the server
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));//receive the message which the server sends back

                for (int idx = 0; idx < txt_list.length; idx++) {
                    String txt = txt_list[idx];//Procesamos cada comando
                    sendMessage(txt);
                    String str_end = txt.substring(txt.length() - 1);
                    if (str_end.equals("?")) { //Si el comando para el VNA acaba con "?", es que se espera una respuesta por parte del VNA  y hay que procesarla
                        serverMessage = in.readLine();
                        if (serverMessage != null && mMessageListener != null) {
                            mMessageListener.messageReceived(serverMessage);
                        }
                        serverMessage = null;
                    }
                    //Log.e("TCPClient", "alej: Comando "+idx+": "+txt);
                    Thread.sleep(1500);//delay de 1s en la ejecucion de cada comando
                }

                //Detecta secuencia de Inicializacion del VNA y envía fin del proceso a la Actividad principal
                if (txt_list[0].equals(":INST \"NA\"")) {
                    mMessageListener.messageReceived("FinInicializacion");//Comandos enviados correctamente
                }
                socket.close();
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
                mMessageListener.messageReceived("Error"); //servidor no responde
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
            mMessageListener.messageReceived("Error"); //servidor no responde
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}