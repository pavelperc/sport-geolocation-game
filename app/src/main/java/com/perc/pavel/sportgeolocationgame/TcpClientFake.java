package com.perc.pavel.sportgeolocationgame;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pavel on 19.09.2017.
 */

/**
 * Эмулятор сервера в режимах TCP и HTTP.
 */
class TcpClientFake{
    
    private TcpListener tcpListener = null;

    /**
     * Запустить Сервер в tcp режиме.
     * @param messageListener Интерфейс общения с сервером. (Вызывается в основном потоке через Handler.post)
     */
    void startAsync(TcpListener messageListener) {
        tcpListener = messageListener;
        tcpListener.onTCPConnectionStatusChanged(true);
    }

    /**
     * Отправить сообщение серверу в режиме tcp. До этого нужно вызвать startAsync.
     * @param message Сообщение серверу.
     */
    void sendMessage(final JSONObject message) {
        final Handler handler = new Handler();
        if (tcpListener != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    
                    final JSONObject answer = simulateServerAnswer(message);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}

                    boolean result = handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tcpListener.onTCPMessageReceived(answer);
                        }
                    });
                    
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            tcpListener.onTCPMessageReceived(answer);
//                        }
//                    });
                }
            }).start();
        }
    }

    /**
     * Здесь симулируются ответы сервера в любом режиме (http / tcp)
     * @param message Сообщение серверу.
     * @return Ответ сервера без задержки.
     */
    private JSONObject simulateServerAnswer(final JSONObject message) {
        
        try {
            JSONObject answer = new JSONObject();
            
            switch (message.getString("type")) {
                case "register":
                    answer.put("response", 1);
                    break;
                case "authorization":
                    answer.put("response", 1);
                    break;
                default:
                    answer.put("response", 0);
                    answer.put("error", "unknown type");
                    break;
            }
            
            
            return answer;
        } catch (JSONException e){
            return new JSONObject();
        }
        
    }

    /**
     * Остановка tcp соединения.
     */
    void stopClient() {
        tcpListener.onTCPConnectionStatusChanged(false);
        tcpListener = null;
    }

    /**
     * Запрос сервера в режиме http.
     * @param message Сообщение серверу.
     * @param onResult Интерфейс, в который сервер отправляет ответ. (Вызывается в основном потоке через Handler.post)
     */
    void httpRequest(final JSONObject message, final HttpListener onResult){
        final Handler handler = new Handler();
        
        
        new Thread(new Runnable() {
            @Override
            public void run() {

                final JSONObject answer = simulateServerAnswer(message);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                
                
                boolean result = handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onResult.onMessageReceived(answer);
                    }
                });

//                Log.d("my_tag", "handler posted = " + result);
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        onResult.onMessageReceived(answer);
//                    }
//                });
            }
        }).start();
    }
}
