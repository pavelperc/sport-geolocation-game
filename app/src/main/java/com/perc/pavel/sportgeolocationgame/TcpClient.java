package com.perc.pavel.sportgeolocationgame;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by pavel on 12.09.2017.
 * 
 * Класс для работы с сервером через TCP
 * Пока ничего не работает. (сообщения отправляются, но не принимаются.
 * Ещё не работает отключение от сервера)
 * 
 * В будущем возможно добавление сюда общения через http
 * 
 * Всё частично взято отсюда:
 * https://stackoverflow.com/questions/38162775/really-simple-tcp-client
 */

class TcpClient {
    private static final String SERVER_IP = "62.109.23.138"; //server IP address
    private static final int SERVER_PORT = 9090;

    private ConnectTask connectTask;
    private Socket socket;
    
    // used to send messages
    private PrintWriter bufferOut;
    // used to read messages from the server
    //private BufferedReader bufferIn;
    

    /**
     * Отправить сообщение на сервер.
     *
     * @param message JSON объект, отправляемый клиентом.
     */
    void sendMessage(final JSONObject message) {
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bufferOut != null && !bufferOut.checkError()) {
                    bufferOut.println(message.toString());
                    bufferOut.flush();
                    Log.d("serv_tag", "C: Message sent.");
                }
            }
        }).start();
    }

    /**
     * Отключиться от сервера
     */
    void stopClient() {
        Log.d("serv_tag", "Before stopping Client");
        if (connectTask != null){
            if (connectTask.cancel(true))
                connectTask = null;
            
            
        }
    }


    /**
     * Подключиться к серверу.
     * @param messageListener Интерфейс для получения ответов от сервера и состояния о подключении
     */
    void startAsync(TcpListener messageListener) {
        if (connectTask == null) {
            connectTask = new ConnectTask(messageListener);
            connectTask.execute();
        }
    }

    private class ConnectTask extends AsyncTask<Void, Object, Void> {// <Params, Progress, Result>

        private final TcpListener messageListener;

        ConnectTask(TcpListener listener){
            this.messageListener = listener;
        }

        @Override
        protected Void doInBackground(Void... message) {
            
            try {
                //here you must put your computer's IP address.
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                
                //Log.e("TCP Client", "C: Connecting...");
                Log.d("serv_tag", "C: Connecting...");
                
                //create a socket to make the connection with the server
                socket = new Socket(serverAddr, SERVER_PORT);

                try {
                    //sends the message to the server
                    bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    //receives the message which the server sends back
                    BufferedReader bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    publishProgress(null, true);
                    
                    Thread.sleep(5000);
                    
                    String serverMessage = null;
                    //in this while the client listens for the messages sent by the server
                    while (!isCancelled()) {
                        Log.d("serv_tag", "Before reading from server...");
                        
                        
                        try {
                            serverMessage = bufferIn.readLine();
                            Log.d("serv_tag", "Reading from server...");
                        } catch (Exception e){
                            Log.d("serv_tag", "error in readLine: " + e);
                        }
                        
                        
                        
                        if (serverMessage != null) {
                            Log.d("serv_tag", "message: " + serverMessage);
                            
                            //call the method messageReceived from MyActivity class
                            Log.d("serv_tag", "S: Received Message: '" + serverMessage + "'");
                            publishProgress(serverMessage, null);
                        }
                        Thread.sleep(500);
                    }

                    //Log.d("serv_tag", "After cycle");


                } catch (Exception e) {

                    Log.d("serv_tag", "error1:\t" + e);

                } finally {
                    //the socket must be closed. It is not possible to reconnect to this socket
                    // after it is closed, which means a new socket instance has to be created.
                    socket.close();
                    Log.d("serv_tag", "C: disconnected (socket closed in background task)");
                }

            } catch (Exception e) {
                
                Log.d("serv_tag", "error2:\t" + e);
            }
            
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) { // String message, Boolean connectionStatus
            super.onProgressUpdate(values);
            
            if (values[0] != null)
                try {
                    messageListener.onTCPMessageReceived(new JSONObject(values[0].toString()));
                } catch (JSONException ignored) {}


            if (values.length > 1 && values[1] != null){
                messageListener.onTCPConnectionStatusChanged((boolean) values[1]);
            }
        }


        @Override
        protected void onCancelled() {
            if (bufferOut != null) {
                bufferOut.flush();
                bufferOut.close();
                Log.d("serv_tag", "bufferOut closed");
            }
            try {
                socket.close();
                Log.d("serv_tag", "socket closed in onCancelled");
            } catch (Exception ignored) {}
            
            
            //bufferIn = null;
            bufferOut = null;
            
            messageListener.onTCPConnectionStatusChanged(false);
            
            super.onCancelled();
        }
    }
}