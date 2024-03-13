//package com.example.ale.medidas;
//// ale
//import android.util.Log;
//
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.drive.Drive;
//import com.google.android.gms.drive.DriveApi;
//import com.google.android.gms.drive.DriveContents;
//import com.google.android.gms.drive.DriveFile;
//import com.google.android.gms.drive.DriveFolder;
//import com.google.android.gms.drive.DriveId;
//import com.google.android.gms.drive.MetadataChangeSet;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.io.Writer;
//
///**
// * Created by ale on 02/06/2017.
// */
//
//public class GDriveClient {
//
//    private GoogleApiClient apiClient;//cliente para manejar la conexión con Google Drive
//    private String strFolderId;//folder "MedidasApp" la cuenta de Drive de "micromag@micromag.es" creado a mano
//    private String strFileId;//Si estamos re-grabando un archivo de medida, usamos este FileID para borrar el anterior
//    private OnMessageReceived mMessageListener = null; //interfaz para devolver la comunicación a la clase que llame a ésta
//    private String operacionOk = "Error";
//    private String strFileIdnew = "";
//    private String src_file;
//
//    public GDriveClient(GoogleApiClient client, String src_file, String strFolderId, String strFileId, OnMessageReceived listener) {
//        apiClient = client;
//        mMessageListener = listener; //servirá para una vez acabada la tarea de esta clase, notificar a la clase llamante
//        this.strFileId = strFileId;//ID del archivo de sobre el que queremos operar (copiar o borrar a/de GDrive): si ya existe de antes en GDrive
//        this.strFolderId = strFolderId;//Directorio donde se encuentran esos archivos
//        this.src_file = src_file;//Path al archivo XML guardado en "share-prefs" que se copiará en GDrive
//    }
//
//
//    //GDrive: Crear Archivo dendro de directorio conocido
//    public void createFile(final String src) {
//        final String LOGTAG = "GDClient.createFile";
//        String[] tk = src.split("/");//Separa el nombre del path del fichero
//        final String filename = tk[tk.length - 1];
//        Drive.DriveApi.newDriveContents(apiClient)
//                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
//                    @Override
//                    public void onResult(DriveApi.DriveContentsResult result) {
//                        if (result.getStatus().isSuccess()) {
//                            //writeSampleText(result.getDriveContents());//Test: Escribe Contenido del fichero
//                            saveSharedPreferencesToFile(src, result.getDriveContents());//Escribe Contenido del fichero
//                            MetadataChangeSet changeSet =              //Título fichero y Tipo (txt)
//                                    new MetadataChangeSet.Builder()
//                                            .setTitle(filename)
//                                            .setMimeType("text/plain")
//                                            .build();
//                            //Opción 1: Directorio raíz
//                            //DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);
//                            //Opción 2: Otra carpeta distinta al directorio raiz
//                            DriveFolder folder = DriveId.decodeFromString(strFolderId).asDriveFolder();
//                            folder.createFile(apiClient, changeSet, result.getDriveContents()) //Escritura del fichero a GDrive
//                                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
//                                        @Override
//                                        public void onResult(DriveFolder.DriveFileResult result) {
//                                            if (result.getStatus().isSuccess()) {
//                                                strFileIdnew = result.getDriveFile().getDriveId().toString();
//                                                operacionOk = "Ok";
//                                                Log.e(LOGTAG, "Operacion: " + operacionOk + " Fichero creado con ID = " + strFileIdnew);
//                                                //Borramos el archivo antiguo, si existiese
//                                                deleteFile(strFileId);
//                                            } else {
//                                                operacionOk = "Error";
//                                                Log.e(LOGTAG, "Error al crear el fichero");
//                                            }
//                                            //Devolver el control al programa ppal con el estado de la operación y el nuevo FileID (si se ha creado un archivo)
//                                            if (mMessageListener != null) {
//                                                Log.e(LOGTAG, "alej: Operacion: " + operacionOk);
//                                                String[] msg = new String[]{operacionOk, strFileIdnew};
//                                                mMessageListener.messageReceived(msg);
//                                            }
//                                        }
//                                    });
//                        } else {
//                            operacionOk = "Error";
//                            strFileIdnew = "";
//                            Log.e(LOGTAG, "Error al crear DriveContents");
//                            //Devolver el control al programa ppal con el estado de la operación y el nuevo FileID (si se ha creado un archivo)
//                            if (mMessageListener != null) {
//                                Log.e(LOGTAG, "alej: Operacion: " + operacionOk);
//                                String[] msg = new String[]{operacionOk, strFileIdnew};
//                                mMessageListener.messageReceived(msg);
//                            }
//                        }
//                    }
//                });
//    }
//
//    //GDrive: Escribe el contenido del fichero al objeto de su contenido
//    private void writeSampleText(DriveContents driveContents) {
//        OutputStream outputStream = driveContents.getOutputStream();
//        Writer writer = new OutputStreamWriter(outputStream);
//        try {
//            writer.write("Esto es un texto de prueba!");
//            writer.close();
//        } catch (IOException e) {
//            Log.e("writeSampleText", "Error al escribir en el fichero: " + e.getMessage());
//        }
//    }
//
//    //Rellena el contenido del fichero, escribiendo en el metadato "DriveContent" del fichero
//    private void saveSharedPreferencesToFile(String src, DriveContents dst) {
//        try {
//            InputStream in = new FileInputStream(src);//stream de entrada que apunta a la ubicación del fichero XML a copiar
//            OutputStream out = dst.getOutputStream();//stream de salida que apunta al contenido del fichero en GDrive donde se va a copiar la información
//
//            // Copy the bits from instream to outstream
//            byte[] buf = new byte[1024];
//            int len;
//            while ((len = in.read(buf)) > 0) {
//                out.write(buf, 0, len);
//            }
//            in.close();
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            Log.e("GDriveClient.Write", "alej: Archivo origen no encontrado: " + src + " Traza: " + e.getMessage());
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e("GDriveClient.Write", "alej: Error al escribir en el fichero: " + e.getMessage());
//        }
//    }
//
//    //GDrive: Lo usamos para borrar los archivos de medida: Tendríamos que guardar el FolderID dentro del XML junto a las medidas, para poder borrarlo
//    public void deleteFile(String strFileId) {
//        final String LOGTAG = "GDClient.delete";
//        Log.e(LOGTAG, "alej: Fichero a Borrar: "+strFileId);
//        if (!(strFileId.equals(""))) { //Si el FileID no está vacio, se borra XML antiguo
//            DriveFile file = DriveId.decodeFromString(strFileId).asDriveFile();//recuperamos el ID a partir del String
//
//            //Opción 1: Enviar a la papelera
////            file.trash(apiClient).setResultCallback(new ResultCallback<Status>() {
////                @Override
////                public void onResult(Status status) {
////                    if (status.isSuccess()) {
////                        Log.e(LOGTAG, "alej: Fichero eliminado correctamente.");
////                        operacionOk = "Ok";
////                    } else {
////                        Log.e(LOGTAG, "alej: Error al eliminar el fichero");
////                        operacionOk = "Error";
////                    }
////                }
////            });
//            //Opción 2: Eliminar
//            file.delete(apiClient).setResultCallback(new ResultCallback<Status>() {
//                @Override
//                public void onResult(Status status) {
//                    if (status.isSuccess()) {
//                        Log.e(LOGTAG, "alej: Fichero eliminado correctamente.");
//                        operacionOk = "Ok";
//                    } else {
//                        Log.e(LOGTAG, "alej: Error al eliminar el fichero");
//                        operacionOk = "Error";
//                    }
//                }
//            });
//        }
//    }
//
//    //Decidir según el valor de "operacion", qué tiene que hacer el cliente
//    public void run(String operacion) {
//        String LOGTAG = "GDClient.run";
//
//        //Operación: Copiar archivo
//        if (operacion.equals("Copiar")) {
//            Log.e("GDriveClient.run", "alej: Createfile: " + src_file);
//            createFile(src_file);
//        }
//        //Operación: Borrar archivo
//        if (operacion.equals("Borrar")) {
//            //Aún por implementar
//        }
//    }
//
//    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
//    //class at on asynckTask doInBackground
//    public interface OnMessageReceived {
//        public void messageReceived(String[] message);
//    }
//}
