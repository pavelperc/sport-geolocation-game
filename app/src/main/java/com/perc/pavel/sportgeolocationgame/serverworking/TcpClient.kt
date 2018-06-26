package com.perc.pavel.sportgeolocationgame.serverworking

import com.perc.pavel.sportgeolocationgame.commonLog
import com.perc.pavel.sportgeolocationgame.serverLog
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread
import android.R.id.message


class TcpClient private constructor() : AbstractTcpClient() {
    companion object {
        /** [AbstractTcpClient] instance, which returns tcp connection result in ui thread.*/
        fun inUiThread(): AbstractTcpClient = TcpClient().inUiThread()
        
        /** [AbstractTcpClient] instance, which returns tcp connection result in client thread.*/
        fun inOtherThread(): AbstractTcpClient = TcpClient()
        
    }
    
    
    // used to send messages
    private var mBufferOut: PrintWriter? = null
    // used to read messages from the server
    private var mBufferIn: BufferedReader? = null
    
    private var backgroundTcpThread: Thread? = null
    
    
    @Volatile
    override var isTcpRunning = false
    
    private var isTryingToStop = false
    
    override fun startAsync(tcpConnectionListener: TcpConnectionListener) {
        
        serverLog("in startAsync: isTcpRunning = $isTcpRunning; isTryingToStop = $isTryingToStop")
//        if (isTcpRunning) {
////            tcpConnectionListener.onConnectionError("SERVER IS RUNNING");
//            Log.d(SERVER_LOG, "TRIED TO START AGAIN ALREADY RUNNING SERVER LISTENER");
//            return;
//        }
        
        isTcpRunning = true
        backgroundTcpThread = thread {
            serverLog("Start connecting: ")
            
            try {
                // try with resources
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(SERVER_IP, SERVER_PORT_TCP), 7000)
                    //sends the message to the server
                    mBufferOut = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true)
                    //receives the message which the server sends back
                    mBufferIn = BufferedReader(InputStreamReader(socket.getInputStream()))
                    
                    serverLog("Connected")// ??????
                    
                    
                    // вызываем onConnected
                    tcpConnectionListener.onConnected()
                    
                    // слушаем ответ от сервера. выходим из цикла только по исключениям
                    while (true) {
                        
                        val serverMessage = mBufferIn!!.readLine()
                        
                        if (serverMessage == null || serverMessage == "" || serverMessage == "\n")
                            continue
                        
                        
                        // пытаемся прочитать сообщение в этом же потоке.
                        try {
                            
                            commonLog("tcp received: $serverMessage")
                            
                            val jo = JSONObject(serverMessage)
                            
                            // сервер не понял наш json
                            if (jo.has("error")) {
                                tcpConnectionListener.onConnectionError(jo.getString("error"), "Server returned error")
                            } else {
                                // всё ОК, сервер понял json
                                
                                messageListeners.forEach { it.onTCPMessageReceived(jo) }
                            }
                        } catch (e: JSONException) {// нам прислали неверный json
                            tcpConnectionListener.onConnectionError(e.message!!, "JSONException: ")
                        }

//                        Log.d(SERVER_LOG, "loop iteration ended.");
                    }// end of loop
                }
            } catch (e: IOException) {
                // когда не достучались до сервера или закрыли выходной поток
                serverLog("IOException: ${e.message} in ---> $e.stackTrace)")
                
                // пишем, что сервер отключён до вывода alertDialog
                isTcpRunning = false
                // если мы попали сюда из за закрытия mBufferIn в stopClient - не отправляем onConnectionError.
                if (!isTryingToStop) {
                    tcpConnectionListener.onConnectionError(e.message!!, "IOException in server")
                }
            } catch (e: Exception) {
                // пишем, что сервер отключён до вывода alertDialog
                serverLog("Another Exception: " + e.message)
                isTcpRunning = false
                
                tcpConnectionListener.onConnectionError(e.message!!, "Another Server Exception")
            }
            // выход из цикла и закрытие сокета.
            
            isTcpRunning = false
            serverLog("Closed socket thread. isTryingToStop = $isTryingToStop")
        }
    }
    
    override fun reconnect(connectionListener: TcpConnectionListener) {
        serverLog("in reconnect")
        if (isTcpRunning) {
            serverLog("in reconnect: trying to close close socket")
            // метод сам ждёт пока поток завершится
            stopClient()
        }
        
        startAsync(connectionListener)
    }
    
    override fun sendMessage(message: JSONObject) {
        thread {
            if (mBufferOut?.checkError() == false) {
                //mBufferOut.write(message.toString());
                //mBufferOut.flush();
                
                val str = message.toString()
                val bufferSize = 1024
                var i = 0
                while (i < str.length) {
                    mBufferOut?.write(str.substring(i, Math.min(i + bufferSize, str.length)))
                            ?: return@thread
                    mBufferOut?.flush() ?: return@thread
                    i += bufferSize
                }
                mBufferOut?.write("\n") ?: return@thread
                mBufferOut?.flush() ?: return@thread
                
                commonLog("Sent message: $message")
            }
        }
    }
    
    override fun stopClient() {
        // ставим флаг, чтобы из исключения, выпавшего в потоке слушателя сервера не вызвалось onConnectionError
        isTryingToStop = true
        
        
        
        mBufferOut?.apply {
            flush()
            close()
        }
        
        mBufferIn?.apply {
            try {
                close()
            } catch (e: IOException) {
                serverLog("error in closing mBufferIn")
            }
        }
        
        try {// ждём пока завершится процесс, если он не завершён
            backgroundTcpThread?.join()
        } catch (e: InterruptedException) {
            serverLog("in stopClient: interrupted exception")
        }
        
        // убираем флаг
        isTryingToStop = false
        
        mBufferIn = null
        mBufferOut = null
    }
    
}