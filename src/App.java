public class App {
   /**
    * The main function creates a server object, runs the server, catches any exceptions, and then runs
    * a client.
    */
    public static void main(String[] args) throws Exception {
        Server _server = new Server();
        Client _client = new Client();
        try {
            _server.run();
        } catch (Exception e) {
            System.out.println("Error is: " + e);
        } finally {
            _client.run();
        }
    }
}
