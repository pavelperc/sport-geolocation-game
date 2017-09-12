package com.perc.pavel.sportgeolocationgame;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by pavel on 12.09.2017.
 * Пока ничего не работает
 * Всё частично взято отсюда:
 * https://stackoverflow.com/questions/38162775/really-simple-tcp-client
 */

class TcpClient {
    private static final String SERVER_IP = "62.109.23.138"; //server IP address
    private static final int SERVER_PORT = 9090;
    // message to send to the server
    private String serverMessage;

    // sends message received notifications
    private OnMessageReceived messageListener = null;
    
    // while this is true, the server will continue running
    private boolean run = false;
    // used to send messages
    private PrintWriter bufferOut;
    // used to read messages from the server
    private BufferedReader bufferIn;
    
    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    TcpClient(OnMessageReceived listener) {
        messageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    void sendMessage(String message) {
        if (bufferOut != null && !bufferOut.checkError()) {
            bufferOut.println(message);
            bufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    void stopClient() {
        
        run = false;
        Log.d("my_tag", "in stopClient: set run = false");

        if (bufferOut != null) {
            bufferOut.flush();
            bufferOut.close();
        }
        
        //messageListener = null;
        bufferIn = null;
        bufferOut = null;
        serverMessage = null;
    }

    private void run(OnMessageReceived internalMessageListener) {

        run = true;
        
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            //Log.e("TCP Client", "C: Connecting...");
            Log.d("my_tag", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            try {
                
                //sends the message to the server
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                
                //receives the message which the server sends back
                bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                int charsRead = 0;
                char[] buffer = new char[1024]; //choose your buffer size if you need other than 1024
                
                //in this while the client listens for the messages sent by the server
                while (run) {
                    charsRead = bufferIn.read(buffer);
                    serverMessage = new String(buffer).substring(0, charsRead);
                    
                    if (charsRead > 0 && serverMessage != null && internalMessageListener != null) {
                        Log.d("my_tag", "S: Received Message: '" + serverMessage + "'");
                        internalMessageListener.messageReceived(serverMessage);
                        
                    }
                    serverMessage = null;
                    
//                    serverMessage = bufferIn.readLine();
//                    if (serverMessage != null)
//                        Log.d("my_log", "message: " + serverMessage);
//                    
//                    if (serverMessage != null && internalMessageListener != null) {
//                        //call the method messageReceived from MyActivity class
//                        Log.d("my_tag", "S: Received Message: '" + serverMessage + "'");
//                        internalMessageListener.messageReceived(serverMessage);
//                    }

                }
                //Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
                

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);
                Log.d("my_tag", "error1:\t" + e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                Log.d("my_tag", "C: disconnected");
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);
            Log.d("my_tag", "error2:\t" + e);
        }

    }
    
    void runAsync() {
        if (!run)
            new ConnectTask(messageListener).execute("");
    }
    
    private class ConnectTask extends AsyncTask<Object, String, Object> {// <Params, Progress, Result>
        
        private final OnMessageReceived messageListener;
        
        ConnectTask(OnMessageReceived listener){
            this.messageListener = listener;
        }
        
        @Override
        protected Object doInBackground(Object... message) {
            
            run(new OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            //Log.d("test", "response " + values[0]);
            
            messageListener.messageReceived(values[0]);
            
            //process server response here....

        }
    }
    
    
    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    interface OnMessageReceived {
        void messageReceived(String message);
    }

}
