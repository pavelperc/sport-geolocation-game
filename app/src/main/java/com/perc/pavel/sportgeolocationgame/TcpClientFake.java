package com.perc.pavel.sportgeolocationgame;

import android.os.Handler;

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
    
    private static TcpClientFake instance;
    static TcpClientFake getInstance() {
        if (instance == null)
            instance = new TcpClientFake();
        return instance;
    }
    
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Flag> flags = new ArrayList<>();
    private ArrayList<Integer> teamColors = new ArrayList<>();
    
    private final int OTHER_PLAYERS_COUNT = 4;
    private Random rnd = new Random();
    
    private TcpMessageListener tcpMessageListener = null;
    
    /**
     * Запустить Сервер в tcp режиме.
     * @param messageListener Интерфейс общения с сервером. (Вызывается в основном потоке через Handler.post)
     */
    void startAsync(TcpMessageListener messageListener, TcpConnectionListener connectionListener) {
//        tcpConnectionListener.onTCPConnectionStatusChanged(true);
        connectionListener.onConnected();
        tcpMessageListener = messageListener;
    }

    /**
     * Отправить сообщение серверу в режиме tcp. До этого нужно вызвать startAsync.
     * @param message Сообщение серверу.
     */
    void sendMessage(final JSONObject message) {
        final Handler handler = new Handler();
        if (tcpMessageListener != null){
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
                            tcpMessageListener.onTCPMessageReceived(answer);
                        }
                    });
                    
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            tcpConnectionListener.onTCPMessageReceived(answer);
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
                    
                    
                    // для того, кто создаёт игру
                    if (message.has("setupGameInfo")) {
                        JSONObject setupInfo = message.getJSONObject("setupGameInfo");
    
                        // Сохраняем цвета команд
                        JSONArray jColors = setupInfo.getJSONArray("teamColors");
                        teamColors.clear();
                        for (int i = 0; i < jColors.length(); i++) {
                            teamColors.add(jColors.getInt(i));
                        }
    
                        // Сохраняем флажки
                        JSONArray jFlags = setupInfo.getJSONArray("flags");
                        flags.clear();
                        for (int i = 0; i < jFlags.length(); i++) {
                            flags.add(new Flag(jFlags.getJSONObject(i)));
                        }
                        
                        // создаём игроков
                        players.clear();
                        for (int i = 0; i < OTHER_PLAYERS_COUNT; i++) {
                            Player player = new Player(
                                    "player_" + i,
                                    lat + (rnd.nextDouble() - 0.5) * llDelta,
                                    lng + (rnd.nextDouble() - 0.5) * llDelta,
                                    // i + 1 так как нулевой цвет у игрока, приславшего настройку
                                    teamColors.get((i + 1) % teamColors.size()));
                            
                            players.add(player);
                        }
                    }
                    
                    
                    answer.put("response", 1);
                    // возвращаем всем (пока у нас только создатель) нулевой цвет
                    answer.put("teamColor", teamColors.get(0));
                    break;
                case "getPlayerLocations":
                    // заполняем позиции игроков
                    JSONArray jPlayers = new JSONArray();
                    for (int i = 0; i < OTHER_PLAYERS_COUNT; i++) {
                        players.get(i).lat += (rnd.nextDouble() - (double)i/OTHER_PLAYERS_COUNT) * llDelta;
                        players.get(i).lng += (rnd.nextDouble() - (double)i/OTHER_PLAYERS_COUNT) * llDelta;
                        
                        jPlayers.put(players.get(i).getJson());
                    }
//                    // заполняем позиции флажков
//                    JSONArray jFlags = new JSONArray();
//                    for (int i = 0; i < OTHER_PLAYERS_COUNT; i++) {
//                        jFlags.put(flags.get(i).getJson());
//                    }
                    
                    // запаковываем отправку
                    answer.put("response", 1);
                    answer.put("players", jPlayers);
//                    answer.put("flags", jFlags);
                    break;
                case "activateFlag":
                    
                    answer.put("response", 1);
                    int index = message.getJSONObject("activateFlag").getInt("index");
                    int color = message.getJSONObject("activateFlag").getInt("color");
                    flags.get(index).teamColor = color;
                    
                    
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
//        tcpConnectionListener.onTCPConnectionStatusChanged(false);
        tcpMessageListener = null;
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
                        onResult.onResponse(answer.toString());
                    }
                });

//                Log.d("my_tag", "handler posted = " + result);
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        onResult.onResponse(answer);
//                    }
//                });
            }
        }).start();
    }
}
