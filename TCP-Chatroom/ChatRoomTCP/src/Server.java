import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Runnable {

    
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done; //Done variable responsible for checking whether or not server is running or not
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try  {
            server = new ServerSocket(9999); 
            //Pool thread to store new connections, and make sure when a user quits only they quit
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
            shutdown();
        }
    }

        //Close server socket 
        public void shutdown() {

            try {
                done = true;
                if(!server.isClosed()) {
                    server.close();
                }

                for (ConnectionHandler ch: connections) {
                    ch.shutdown();
                }
            } catch (Exception e) {
                //Can't handle
            }

           
        }


    //Presents message to all current threads
    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.SendMessage(message);
            }

        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname; 

        // Constructor to allow multiple instances to be run, for multiple users
        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true); // autoFlush is true here
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                //Handling Nicknames
                out.println("Please enter a nickname:");
                nickname = in.readLine(); //Whatever client sends after prompt will be stored as nickname
                while (nickname.isEmpty()) {
                    out.println("Please enter a nickname:");
                nickname = in.readLine(); 
                }
                System.out.println(nickname + " connected!");
                broadcast(nickname + " joined the chat!");
                

                //Handling Messages & Commands
                String message;
                while ((message = in.readLine()) != null ) {
                    if ((message.startsWith("/nick"))) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " has changed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " has changed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickname);
                        } else {
                            out.println("No user nickname provided!");
                        }

                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " has left the chat");
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
                
            
            } catch (Exception e) {
                shutdown();
            } 
        
        }
            //Functions to display message
        public void SendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {

            try {
                in.close();
                out.close();
    
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //ignore
            }

           
        }
    }

    public static void main(String[] args) {
        Server server = new Server(); 
        System.out.println("Server is running!");
        server.run();
    }
}
