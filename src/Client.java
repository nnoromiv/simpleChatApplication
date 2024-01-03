import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * The Client class is a Java implementation of a client that connects to a server and allows for
 * sending and receiving messages.
 */
public class Client implements Runnable {
    
    // These are instance variables of the Client class.
    private Socket _client;
    private BufferedReader _in;
    private PrintWriter _out;
    private boolean _done;

    @Override
    // The `run()` method is the main method that is executed when the `Client` class is run as a
    // thread.
    public void run() {
        try {
            _client = new Socket("127.0.0.1", 9999);    
            _out = new PrintWriter(_client.getOutputStream(), true);
            _in = new BufferedReader(new InputStreamReader(_client.getInputStream())); 
            
            InputHandler _inputHandler = new InputHandler();
            Thread _thread = new Thread(_inputHandler);
            _thread.start();

            String _inMessage;
            while ((_inMessage = _in.readLine()) != null) {
                System.out.println(_inMessage);
            }
        } catch (IOException e) {
            // Nothing
        }
    }

    /**
     * The shutDown() function closes the input and output streams, and if the client is not closed, it
     * closes the client connection.
     */
    public void shutDown() throws IOException{
        _done = true;
        _in.close();
        _out.close();
        if(!_client.isClosed()){
            _client.close();
        }
    }

    /**
     * The InputHandler class reads input from the user and sends it to an output stream, with the
     * ability to quit the program if the user enters "/quit".
     */
    class InputHandler implements Runnable {

        @Override
        /**
         * Reads user input from the console and sends messages to the server.
         * The method continuously reads input until the client is marked as done.
         * If the user enters "/quit," the client sends the quit command to the server,
         * closes the input stream, and initiates a graceful shutdown.
         * If an IOException occurs during the input reading process, the client attempts
         * to perform a graceful shutdown by calling the shutDown method.
         *
         * @throws IOException If an I/O error occurs while reading user input or
         *                     performing a shutdown operation.
         */
        public void run(){
            try {
                BufferedReader _inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!_done){
                    String _message = _inReader.readLine();
                    if(_message.equals("/quit")){
                        _out.println(_message);
                        _inReader.close();
                        shutDown();
                    } else {
                        _out.println(_message);
                    }
                }
            } catch(IOException e) {
                try {
                    shutDown();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }    
}
