package com.perc.pavel.sportgeolocationgame;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by pavel on 12.09.2017.
 * <p>
 * Класс для работы с сервером через TCP.
 * <p>
 * В отдельном потоке ловит сообщения сервера и отсылает их всем подключённым слушателям.
 * <p>
 * Всё частично взято отсюда:
 * https://stackoverflow.com/questions/38162775/really-simple-tcp-client
 */

class TcpClient {
    
    static final String SERVER_IP = "92.63.105.60"; //server IP address
    //    private static final int SERVER_PORT_TCP = 7071;
    static final int SERVER_PORT_HTTP = 7070;
    private volatile boolean isTcpRunning = false;
    private boolean isTryingToStop = false;
    
    boolean isTcpRunning() {
        return isTcpRunning;
    }
    
    int getServerPortTcp() {
        return SERVER_PORT_HTTP + 1;
    }
    
    static final String SERVER_LOG = "server_log";
    
    
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    
    private Thread backgroundTcpThread;
    
    private List<TcpMessageListener> messageListeners = new ArrayList<>();
    
    static TcpClient instance;
    
    // HTTP:
    private OkHttpClient client;
    
    
    static TcpClient getInstance() {
        if (instance == null) {
            instance = new TcpClient();
        }
        return instance;
    }
    
    private TcpClient() {
        client = new OkHttpClient();
    }
    
    /**
     * Отправить сообщение на сервер.
     *
     * @param message JSON объект, отправляемый клиентом.
     */
    void sendMessage(final JSONObject message) {
        new Thread(new Runnable() {
            @Override
            synchronized public void run() {
                if (mBufferOut != null && !mBufferOut.checkError()) {
                    //mBufferOut.write(message.toString());
                    //mBufferOut.flush();
                    
                    String str = message.toString();
                    int bufferSize = 1024;
                    for (int i = 0; i < str.length(); i += bufferSize) {
                        mBufferOut.write(str.substring(i, Math.min(i + bufferSize, str.length())));
                        mBufferOut.flush();
                    }
                    mBufferOut.write("\n");
                    mBufferOut.flush();
                    Log.d("my_tag", "Sent message: " + message.toString());
                }
            }
        }).start();
    }
    
    /**
     * Отключает от сервера. Закрывает все in/out потоки,
     * ждёт пока поток слушания сообщения завершится, если он не завершён.
     */
    void stopClient() {
        // ставим флаг, чтобы из исключения, выпавшего в потоке слушателя сервера не вызвалось onConnectionError
        isTryingToStop = true;
        
        
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
//            Log.d(SERVER_LOG, "closed buffer out.");
            
        }
        if (mBufferIn != null) {
            try {
                mBufferIn.close();
//                Log.d(SERVER_LOG, "closed buffer in.");
            } catch (IOException e) {
                Log.d(SERVER_LOG, "error in closing mBufferIn");
            }
        }
        try {// ждём пока завершится процесс, если он не завершён
            backgroundTcpThread.join();
        } catch (InterruptedException e) {
            Log.d(SERVER_LOG, "in stopClient: interrupted exception");
        }
        
        // убираем флаг
        isTryingToStop = false;
        
        mBufferIn = null;
        mBufferOut = null;
    }
    
    /**
     * Отключиться от сервера, если подключён.
     *
     * @param connectionListener Слушатель подключения для повторного старта.
     */
    void reconnect(TcpConnectionListener connectionListener) {
        Log.d(SERVER_LOG, "in reconnect");
        if (isTcpRunning) {
            Log.d(SERVER_LOG, "in reconnect: trying to close close socket");
            stopClient();// метод сам ждёт пока поток завершится
        }
        
        startAsync(connectionListener);
    }
    
    /**
     * Подключиться к серверу.
     *
     * @param tcpConnectionListener Интерфейс для получения состояния о подключении
     */
    void startAsync(final TcpConnectionListener tcpConnectionListener) {
        Log.d(SERVER_LOG, "in startAsync: isTcpRunning = " + isTcpRunning + "; isTryingToStop = " + isTryingToStop);
//        if (isTcpRunning) {
////            tcpConnectionListener.onConnectionError("SERVER IS RUNNING");
//            Log.d(SERVER_LOG, "TRIED TO START AGAIN ALREADY RUNNING SERVER LISTENER");
//            return;
//        }
        
        final Handler handler = new Handler();
        
        isTcpRunning = true;
        backgroundTcpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(SERVER_LOG, "Start connecting: ");
                
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(SERVER_IP, getServerPortTcp()), 7000);
                    //sends the message to the server
                    mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
                    //receives the message which the server sends back
                    mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    Log.d(SERVER_LOG, "Connected");// ??????
                    
                    // вызываем onConnected
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tcpConnectionListener.onConnected();
                        }
                    });
//                    Log.d(SERVER_LOG, "Before loop");
                    
                    // слушаем ответ от сервера. выходим из цикла только по исключениям
                    while (true) {
//                        Log.d(SERVER_LOG, "loop begin");
                        
                        
                        
                        final String serverMessage = mBufferIn.readLine();
//                        Log.d("my_tag", "reseived from server raw: \"" + (serverMessage == null ? "" : serverMessage) + "\"");
                        if (serverMessage == null || serverMessage.equals("") || serverMessage.equals("\n"))
                            continue;
                        
//                        Log.d(SERVER_LOG, "read server msg");
                        
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // пытаемся прочитать сообщение в этом же потоке.
                                try {
                                    Log.d("my_tag", "tcp received: " + serverMessage);
                                    JSONObject jo = new JSONObject(serverMessage);
                                    if (jo.has("error")) {// сервер не понял наш json
                                        tcpConnectionListener.onConnectionError("server returned error: " + jo.getString("error"));
                                    } else {// всё ОК, сервер понял json
                                        for (TcpMessageListener listener : messageListeners) {
                                            listener.onTCPMessageReceived(jo);
                                        }
                                    }
                                } catch (final JSONException e) {// нам прислали неверный json
                                    tcpConnectionListener.onConnectionError("JSONException: " + e.getMessage());
                                }
                            }
                        });
                        
//                        Log.d(SERVER_LOG, "loop iteration ended.");
                    }// end of loop
                } catch (final IOException e) {// когда не достучались до сервера или закрыли выходной поток
                    Log.d(SERVER_LOG, "IOException: " + e.getMessage() + " in---> " + Arrays.toString(e.getStackTrace()));
                    isTcpRunning = false;// пишем, что сервер отключён до вывода alertDialog
                    // если мы попали сюда из за закрытия mBufferIn в stopClient - не отправляем onConnectionError.
                    if (!isTryingToStop) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tcpConnectionListener.onConnectionError(e.getMessage());
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.d(SERVER_LOG, "Another Exception: " + e.getMessage());
                    isTcpRunning = false;// пишем, что сервер отключён до вывода alertDialog
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tcpConnectionListener.onConnectionError(e.getMessage());
                        }
                    });
                }
                // выход из цикла и закрытие сокета.
                
                isTcpRunning = false;
                Log.d(SERVER_LOG, "Closed socket thread. isTryingToStop = " + isTryingToStop);
                
            }
        });
        backgroundTcpThread.start();
    }
    
    /**
     * Добавить ещё один messageListener
     *
     * @param messageListener интерфейс для обратного вызова.
     */
    void addMessageListener(TcpMessageListener messageListener) {
        messageListeners.add(messageListener);
    }
    
    /**
     * Удалить все messageListener
     */
    void clearAllMessageListeners() {
        messageListeners.clear();
    }
    
    
    /**
     * Возвращает url builder, заполненый http, server_ip, server_port
     */
    static HttpUrl.Builder getUrlBuilder() {
        return new HttpUrl.Builder()
                .scheme("http")
                .host(SERVER_IP)
                .port(SERVER_PORT_HTTP);
    }
    
    
    void httpGetRequest(HttpUrl url, final HttpListener httpListener) {
        httpPostRequest(url, null, httpListener);
    }
    
    
    /**
     * Перегрузка метода post запроса, для случая когда нужен только один параметр http - pathSegment
     * @param action параметр pathSegment для url
     */
    void httpPostRequest(String action, JSONObject json, HttpListener httpListener) {
        HttpUrl url = getUrlBuilder().addPathSegment(action).build();
        httpPostRequest(url, json, httpListener);
    }
    
    void httpPostRequest(HttpUrl url, JSONObject json, final HttpListener httpListener) {
        final Handler handler = new Handler();
        
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        
        
        final Request request = json == null ?
                new Request.Builder()
                        .url(url)
                        .build()
                :
                new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(JSON, json.toString()))
                        .build();
        
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (httpListener == null)
                    return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        httpListener.onFailure(e.getMessage());
                    }
                });
            }
            
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (httpListener == null)
                    return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String str = response.body().string();
                            Log.d("my_tag", "Http received: " + str);
                            JSONObject jo = new JSONObject(str);
                            httpListener.onResponse(jo);
                        } catch (IOException e) {
                            httpListener.onFailure("Unexpected code: " + response);
                        } catch (JSONException e) {
                            httpListener.onFailure("JSONException: " + e.getMessage());
                        } catch (Exception e) {
                            httpListener.onFailure("Another http exception: " + e.getMessage());
                        }
                    }
                });
            }
        });
    }
    
    public void sendMessage(JSONObject jo, ProgressBar pbLoading) {
        
        pbLoading.setVisibility(View.VISIBLE);
        sendMessage(jo);
    }
}