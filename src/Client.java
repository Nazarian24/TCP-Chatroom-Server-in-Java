import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            this.client = new Socket("127.0.0.1", 9999); // Connect to the server
            out = new PrintWriter(client.getOutputStream(), true); // true = autoflush
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            System.out.println("Connected to the server!");

            // Start input handler in a separate thread
            InputHandler inHandler = new InputHandler();
            Thread t1 = new Thread(inHandler);
            t1.start();

            // Listen for incoming messages from the server
            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println("Server says: " + inMessage); // Show server message
            }

        } catch (IOException e) {
            e.printStackTrace();
            shutdown(); // In case of error, shutdown the client
        }
    }

    public void shutdown() {
        done = true;
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
           //null
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit")) {
                        inReader.close();
                        shutdown();
                    } else {
                        out.println(message); // Send message to the server
                    }
                }

            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
