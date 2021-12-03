import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Thread that is listening for incoming messages from the chat client
 *
 * @author Jennifer McCarthy, jemc7787, 930124-0983
 */
public class ClientThread extends Thread {

    private final Server server;
    private final Socket socket;
    private PrintWriter printWriter;
    private boolean alive = false;
    private String username;

    /**
     * Constructor that sets the client threads server and socket
     *
     * @param server the server that will broadcast the message
     * @param socket the socket that holds the connection between server and client
     */
    public ClientThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    /**
     * Listens for a message from the client via the server using stream sockets
     * If message is to 'All' it will be broadcasted, otherwise it will be sent to reciever client
     * When a client is disconnected it is removed from the server and the thread are killed
     */
    @Override
    public void run() {

        /* Sends a list of online clients to a connected client and receives the clients username */
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            username = bufferedReader.readLine();

            printWriter = new PrintWriter(socket.getOutputStream(), true);
            String list = server.getOnlineClients(username);
            printWriter.println(list);

            String broadcastMessage = "CONNECTED CLIENT: [" + username + "]";
            server.broadcast(broadcastMessage);
            server.displayMessage(broadcastMessage, this);

        } catch (Exception ex){
            ex.printStackTrace();
        }

        /* Listens for messages */
        alive = true;
        while (alive) {
            String msg;
            try {
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((msg = bufferedReader.readLine()) != null) {
                    String sender = msg.substring(1, msg.indexOf(">"));
                    String message = msg.substring(msg.indexOf(":"));
                    String receiver = msg.substring(msg.indexOf(">") + 1, msg.indexOf("]"));
                    if (receiver.equals("All")){
                        String completeMsg = sender + message;
                        server.broadcast(completeMsg);
                        server.displayMessage("public: " + msg, this);
                    } else {
                        String completeMsg = sender + " to " + receiver + message;
                        server.sendPrivate(completeMsg, this, receiver);
                        server.displayMessage("private: " + msg, this);
                    }
                }
                printWriter.close();
                bufferedReader.close();
                socket.close();

            } catch (Exception e) {
                server.removeClient(this);
            }
            server.killThread(this);
        }
    }

    /**
     * Gets the thread socket
     *
     * @return socket
     */
    public Socket getSocket(){
        return socket;
    }

    /**
     * Sends a message to the client using socket output stream
     *
     * @param msg The text message
     */
    void sendMessage(String msg) {
        printWriter.println(msg);
    }

    /**
     * Sets the client thread to be running or not
     *
     * @param bool
     */
    public void setAlive(boolean bool){
        alive = bool;
    }

    /**
     * Gets a clients username
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }
}