import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Chat server that receives and sends text messages to online clients using stream sockets
 *
 * @author Jennifer McCarthy, jemc7787, 930124-0983
 */
public class Server {

    private final int port;
    private JFrame jFrame;
    private JTextArea jTextArea;
    private String host;
    private final ArrayList<ClientThread> clientThreads = new ArrayList<>();

    /**
     * Constructor that sets the port number and calls a method that creates a GUI
     */
    public Server(int port){
        this.port = port;
        createGUI();
    }

    /**
     * Creates a GUI for the chat server
     */
    public void createGUI(){
        jFrame = new JFrame("Chat server");

        jTextArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        jTextArea.setEditable(false);

        jFrame.setSize(700, 400);
        jFrame.setLocationRelativeTo(null);
        jFrame.add(jScrollPane);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }

    /**
     * Listens for chat client requests and puts accepted new clients into threads and starts them
     */
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            host = serverSocket.getInetAddress().getLocalHost().getHostName();
            jFrame.setTitle("SERVER ON: " + host + " - PORT: " + port + " - N CLIENTS: " + clientThreads.size());

            while (true){
                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(this, socket);
                clientThreads.add(clientThread);
                jFrame.setTitle("SERVER ON: " + host + " - PORT: " + port + " - N CLIENTS: " + clientThreads.size());
                clientThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets the list of all online clients
     *
     * @return string of online clients
     */
    public String getOnlineClients(String exclude){
        StringBuilder stringBuilder = new StringBuilder();
        for (ClientThread clientThread : clientThreads) {
            if (!clientThread.getUsername().equals(exclude)) {
                stringBuilder.append(clientThread.getUsername() + ",");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Broadcasts an incoming message to all clients
     *
     * @param msg The incoming message from a chat client
     */
    synchronized void broadcast(String msg) {
        for (ClientThread clientThread : clientThreads) {
            clientThread.sendMessage(msg);
        }
    }

    /**
     * Sends a private message to a client
     *
     * @param msg the message to be sent
     * @param sender the sending client
     * @param receiver the receiver client
     */
    synchronized void sendPrivate(String msg, ClientThread sender, String receiver){
        for (ClientThread clientThread : clientThreads) {
            if (clientThread.getUsername().equals(receiver)){
                clientThread.sendMessage(msg);
            }
        }
        sender.sendMessage(msg);
    }

    /**
     * Displays an incoming message from a chat client
     *
     * @param msg The incoming message from a chat client
     * @param sender The client that sent the message
     */
    public void displayMessage(String msg, ClientThread sender){
        jTextArea.append("CLIENT: " + sender.getSocket().getInetAddress().getHostName() + " BROADCAST: " + msg + "\n");
    }

    /**
     * Removes a client that is disconnected and broadcasts it to the remaining clients
     *
     * @param clientThread Client to be removed
     */
    synchronized void removeClient(ClientThread clientThread) {
        String broadcastMessage = "DISCONNECTED CLIENT: [" + clientThread.getUsername() + "]";
        broadcast(broadcastMessage);
        displayMessage(broadcastMessage, clientThread);
        clientThreads.remove(clientThread);
        jFrame.setTitle("SERVER ON: " + host + " - PORT: " + port + " - N CLIENTS: " + clientThreads.size());
    }

    /**
     * Kills a thread by setting running variable to false
     *
     * @param clientThread The thread to be killed
     */
    void killThread(ClientThread clientThread){
        clientThread.setAlive(false);
    }

    /**
     * Main method that starts a new server on a user inputted port if present or a standard port
     *
     * @param args eventual port number
     */
    public static void main(String[] args) {
        int port = 2000;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);
        server.run();
    }
}