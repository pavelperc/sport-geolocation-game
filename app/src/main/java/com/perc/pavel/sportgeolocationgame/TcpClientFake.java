package com.perc.pavel.sportgeolocationgame;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by pavel on 19.09.2017.
 */

/**
 * Эмулятор сервера в режимах TCP и HTTP.
 */
class TcpClientFake {
    
    static class Flag {
        double lat;
        double lng;
        
        public Flag(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }
    
    private static TcpClientFake instance;
    
    static TcpClientFake getInstance() {
        if (instance == null)
            instance = new TcpClientFake();
        return instance;
    }
    
    ArrayList<Player> players = new ArrayList<>();
    //ArrayList<Flag> flags;
    
    
    
    final int OTHER_PLAYERS_COUNT = 4;
    Random rnd = new Random();
    
    
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
            // 1 градус = 111km 
            // 10 метров = 0.00009 градусов
            double llDelta = 0.00018;
            
            switch (message.getString("type")) {
                case "register":
                    answer.put("response", 1);
                    break;
                case "authorization":
                    answer.put("response", 1);
                    break;
                case "startGame":
                    double lat = message.getDouble("lat");
                    double lng = message.getDouble("lng");
                    
                    
                    players.clear();
                    for (int i = 0; i < OTHER_PLAYERS_COUNT; i++) {
                        Player player = new Player(
                                "player_" + i,
                                lat + (rnd.nextDouble() - 0.5) * llDelta,
                                lng + (rnd.nextDouble() - 0.5) * llDelta,
                                (i % 2 == 0 ? Color.RED : Color.GREEN));
                                
                        players.add(player);
                    }
                    answer.put("response", 1);
                    break;
                case "getPlayerLocations":
                    
                    JSONArray jPlayers = new JSONArray();
                    for (int i = 0; i < OTHER_PLAYERS_COUNT; i++) {
                        players.get(i).lat += (rnd.nextDouble() - (double)i/OTHER_PLAYERS_COUNT) * llDelta;
                        players.get(i).lng += (rnd.nextDouble() - (double)i/OTHER_PLAYERS_COUNT) * llDelta;
                        
                        jPlayers.put(players.get(i).getJson());
                    }
                    
                    answer.put("response", 1);
                    answer.put("players", jPlayers);
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
                    Thread.sleep(500);
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
