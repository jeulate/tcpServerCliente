import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class WebSocketServer {
    private static final int PORT = 8080;
    private static final List<WebSocketHandler> clients = new ArrayList<>();
    private static final List<WebSocketEvent.WebSocketEventListener> eventListeners = new ArrayList<>();

    private static WebSocketHandler handler;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Servidor WebSocket en ejecucion en el puerto "+ PORT);
            while (true){
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde "+ clienteSocket.getInetAddress().getHostAddress());

                handler = new WebSocketHandler(clienteSocket);
                clients.add(handler);

                //Registrar el nuevo cliente como oyente de eventos
                addWebSocketEventListener((WebSocketEvent.WebSocketEventListener) handler);

                handler.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(String message) {
        for (WebSocketHandler client: clients){
            client.sendMessage(message);
        }
    }

    public static synchronized void addWebSocketEventListener(WebSocketEvent.WebSocketEventListener listener){
        eventListeners.add(listener);
    }

    public static synchronized void removeWebSocketEventListener(WebSocketEvent.WebSocketEventListener listener){
        eventListeners.remove(listener);
    }

    //Metodo para notificar a los oyentes un evento WebSocket

    public static void fireEvent(WebSocketEvent event) {
        for (WebSocketEvent.WebSocketEventListener listener : eventListeners){
            listener.onWebSocketEvent(event);
        }
    }


    static class WebSocketHandler extends Thread{
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public WebSocketHandler(Socket socket){
            this.clientSocket = socket;
        }

        public void run(){
         try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine())!= null){
                    System.out.println("Mensaje recibido del cliente: " + inputLine);

                    // Notificar a los oyentes un evento de mensaje recibido
                    WebSocketEvent event = new WebSocketEvent(this,inputLine);
                    WebSocketServer.fireEvent(event);

                    WebSocketServer.broadcastMessage("Cliente dice: "+ inputLine);
                }
                in.close();
                out.close();
                clientSocket.close();

             }catch (IOException e){
                e.printStackTrace();
             }finally {
                 // Eliminar este manejador de la lista de clientes cuando la conexión se cierre
                 WebSocketServer.clients.remove(this);

                 // Remover este manejador como oyente de eventos
                 WebSocketServer.removeWebSocketEventListener((WebSocketEvent.WebSocketEventListener) this);
             }
        }

        // Método para enviar un mensaje al cliente
        public void sendMessage(String message) {
            out.println(message);
        }
    }



//    public static void main(String[] args) {
//        int port = 8080;
//
//        try (ServerSocket serverSocket = new ServerSocket(port)) {
//            System.out.println("Servidor WebSocket en ejecución en el puerto " + port);
//
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress());
//
//                WebSocketHandler handler = new WebSocketHandler(clientSocket);
//                handler.start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

}


//
//class WebSocketHandler extends Thread {
//    private final Socket clientSocket;
//    private PrintWriter out;
//    private BufferedReader in;
//
//    public WebSocketHandler(Socket socket) {
//        this.clientSocket = socket;
//    }
//
//    public void run() {
//        try {
//            out = new PrintWriter(clientSocket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                System.out.println("Mensaje recibido del cliente: " + inputLine);
//                // Puedes procesar los mensajes aquí y enviar respuestas si es necesario.
//
//            }
//
//
//            in.close();
//            out.close();
//            clientSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
