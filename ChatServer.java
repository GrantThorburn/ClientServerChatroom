package bigJavaChatTwo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;

/**
 * Used working baseline example from Retro Gamer on: https://stackoverflow.com/users/4148092/retro-gamer
 * https://stackoverflow.com/questions/46185206/java-send-message-to-all-clients 
 * 
 * From his description: 
 * "A multithreaded chat room server.  When a client connects the
 * server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received.  After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED".  Then
 * all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name.  The
 * broadcast messages are prefixed with "MESSAGE ".
 *
 * There are a few features that have been left out.
 * Two are very useful and belong in production code:
 *
 *     1. The protocol should be enhanced so that the client can
 *        send clean disconnect messages to the server.
 *
 *     2. The server should do some logging."
 *     
 *     
 *  I added protocol for disconnect messages and a server timeout for all clients. 
 *  A bit harsh on the client side throwing an exception for both
 *  timing out, as well as personally choosing to disconnect. 
 *  Need to add logging and some sort of cryptology measures.
 *  In addition, make disconnect cleaner instead of copying sequence three times. 
 *  Perhaps integrating AES encryption C/S. 
 */
public class ChatServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * Server timeout, in miliseconds. 60000 is 10 minutes. 
     */
    private static final int SERVER_SOCKET_TIMEOUT_IN_MILLISECONDS = 600000;
    
    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    
    private static final String SERVER = "SERVER";
    private static final String DISCONNECTME = "DISCONNECTME";
    
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {new Handler(listener.accept()).start();}
        } finally {listener.close();}
    }//end main

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {this.socket = socket;}

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {
            	//Create a 10 minute timeout of inactive users. 
            	socket.setSoTimeout(SERVER_SOCKET_TIMEOUT_IN_MILLISECONDS);
            	
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    name = name.trim();
                    if (name == null) {return;}
                    synchronized (names) {
                        if (!names.contains(name)) {names.add(name);break;}}
                }//end while

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                writers.add(out);
                
                //add that client has been added into the chatter, intro message. 
                for (PrintWriter writer : writers) {writer.println("MESSAGE " + SERVER + ": " + name + " has joined the chatter.");}
                
                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    input = input.trim();
                    if (input == null) {return;}
                    
                    //If client desires to disconnect, clear data
                    System.out.println(input);
                    
                    if(input.equals(DISCONNECTME)) {
                    	System.out.println(name + " is disconnecting");
                    	for (PrintWriter writer : writers) {writer.println("MESSAGE " + name + " will be disconnected.");}
                    	if (name != null) {names.remove(name);}
                        if (out != null) {writers.remove(out);}
                        try {socket.close();
                        } catch (IOException e) {}
                    }//end if DISCONNECTME
                    
                    //print all regular messages to the chatroom. 
                    for (PrintWriter writer : writers) {writer.println("MESSAGE " + name + ": " + input);}
                }//end while 
                
            } catch(SocketTimeoutException e) {
            	//tell the server that user is off. User is down. 
            	System.out.println("Removing " + name + " due to SocketTimeoutException, closing socket.");
            	
            	//tell user in textfield that he got kicked off. 
            	String input = name + " user kicked off for inactivity.";
            	for (PrintWriter writer : writers) {writer.println("MESSAGE " + SERVER + ": " + input);}
            	
            	//Disconnect sequence
            	if (name != null) {names.remove(name);}
                if (out != null) {writers.remove(out);}
                try {socket.close();
                } catch (IOException p) {}
                //end SocketTimeoutException
                
            } catch (IOException e) {
            	System.out.println(e);
            } finally {
                // Disconnect Sequence. Remove name and print
                // writer from the set, and close its socket.
                if (name != null) {names.remove(name);}
                if (out != null) {writers.remove(out);}
                try {socket.close();
                } catch (IOException e) {}
            }//end finally 
        }//end run()
    }//end Handler
}//end ChatServer
