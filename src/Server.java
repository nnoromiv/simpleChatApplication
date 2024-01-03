import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Server class is a multithreaded server that handles client connections, allows clients to send
 * messages, change nicknames, and gracefully shut down.
 */
public class Server implements Runnable {

    // These are instance variables of the Server class.
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket _server;
    private boolean _done;
    private ExecutorService _pool;

    // The `public Server()` constructor initializes the `connections` ArrayList and sets the `_done`
    // boolean variable to `false`. This constructor is called when a new instance of the `Server`
    // class is created.
    public Server() {
        connections = new ArrayList<>();
        _done = false;
    }

    @Override
  // The `run()` method is the entry point for the thread that the `Server` class implements. It is
  // responsible for accepting client connections, creating a new `ConnectionHandler` for each client,
  // and adding it to the `connections` ArrayList. It also starts a thread pool to handle each client
  // connection concurrently. The method continues to accept new client connections until the `_done`
  // boolean variable is set to `true`. If an exception occurs, the `shutDown()` method is called to
  // gracefully shut down the server.
    public void run() {
        try {
            _server = new ServerSocket(9999);
            _pool = Executors.newCachedThreadPool();
            while (!_done){
                Socket _client = _server.accept();
                ConnectionHandler _handler = new ConnectionHandler(_client);
                connections.add(_handler);
                _pool.execute(_handler);
            }
        } catch (Exception e) {
            try {
                shutDown();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * The function broadcasts a message to all connected channels.
     * 
     * @param _message The _message parameter is a string that represents the message that you want to
     * broadcast to all the connected channels.
     */
    public void broadCastMessage(String _message){
        for(ConnectionHandler _channel : connections) {
            if(_channel != null){
                _channel.sendMessage(_message);
            }
        }
    }

    /**
     * The shutDown() function shuts down the server and closes all connections.
     */
    public void shutDown() {
        try {
            _done = true;
            if(_pool != null || _server != null){
                _pool.shutdown();
                if(!_server.isClosed()){
                    _server.close();
                }
                for (ConnectionHandler _channel : connections) {
                    _channel.shutDown();
                }
            }
        } catch (IOException e) {
            System.out.println("Error Handled");
        }
    }
/**
 * The ConnectionHandler class handles the communication between a client and a server in a chat
 * application.
 */
    class ConnectionHandler implements Runnable {

        // These are instance variables of the `ConnectionHandler` class.
        private Socket _client;
        private BufferedReader _in;
        private PrintWriter _out;
        private String _nickName;

       // The `public ConnectionHandler(Socket _client)` constructor is initializing the `_client`
       // instance variable of the `ConnectionHandler` class with the value passed as a parameter. This
       // constructor is called when a new instance of the `ConnectionHandler` class is created, and it
       // sets the `_client` variable to the provided `Socket` object.
        public ConnectionHandler(Socket _client) {
            this._client = _client;
        }

        @Override
        // The `run()` method in the `ConnectionHandler` class is responsible for handling the
        // communication between a client and the server.
        public void run(){
            try {
                _out = new PrintWriter(_client.getOutputStream(), true);
                _in = new BufferedReader(new InputStreamReader(_client.getInputStream()));
                _out.println("Hello, Please enter a nickname: ");
                _nickName = _in.readLine();
                System.out.println(_nickName + " connected!");
                broadCastMessage(_nickName + " joined the chat!");
                String _message;
                while((_message = _in.readLine()) != null){
                    // This code block is checking if the received message from the client starts with
                    // "/nick". If it does, it splits the message into two parts using the space
                    // character as the delimiter. The first part will be "/nick" and the second part
                    // will be the new nickname.
                    if(_message.startsWith("/nick")){
                        String[] _messageSplit = _message.split(" ", 2);
                        if(_messageSplit.length == 2){
                            broadCastMessage(_nickName + " " + "renamed themselves to " + _messageSplit[1]);
                            System.out.println(_nickName + " " + "renamed themselves to " + _messageSplit[1]);
                            _nickName = _messageSplit[1];
                            _out.println("Successfully changed nickname to " + _nickName);
                        } else {
                            _out.println("No proper nickname provided");
                        }
                    }
                    // The `else if (_message.startsWith("/quit"))` block is checking if the received
                    // message from the client starts with "/quit". If it does, it means that the
                    // client wants to quit the chat.
                     else if (_message.startsWith("/quit")){
                        broadCastMessage(_nickName + " left the chat.");
                        shutDown();
                    } 
                    // The `else` block is executed when the received message from the client does
                    // not start with "/nick" or "/quit". In this case, it means that the client
                    // wants to send a regular message to the chat.
                    else {
                        broadCastMessage(_nickName + ": " + _message);
                    }
                }
            } catch (IOException e) {
                try {
                    shutDown();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

       /**
        * The function sends a message by printing it to the output stream.
        * 
        * @param _message The _message parameter is a string that represents the message to be sent.
        */
        public void sendMessage(String _message){
            _out.println(_message);
        }

        /**
         * The shutDown() function closes the input and output streams and the client socket if it is
         * not already closed.
         */
        public void shutDown() throws IOException{
            _in.close();
            _out.close();
            if(!_client.isClosed()){
                _client.close();
            }
        }
    }
    
}
