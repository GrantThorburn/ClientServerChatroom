package bigJavaChatTwo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Used working baseline example from Retro Gamer on: https://stackoverflow.com/users/4148092/retro-gamer
 * https://stackoverflow.com/questions/46185206/java-send-message-to-all-clients
 * 
 * From his description:
 * "A simple Swing-based client for the chat server.  Graphically
 * it is a frame with a text field for entering messages and a
 * textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows.
 * When the server sends "SUBMITNAME" the client replies with the
 * desired screen name.  The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are
 * already in use.  When the server sends a line beginning
 * with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all
 * chatters connected to the server.  When the server sends a
 * line beginning with "MESSAGE " then all characters following
 * this string should be displayed in its message area."
 * 
 * I've added timeout and a simple, but not yet detailed, way of disconnecting 
 * inactive users and users who choose to disconnect. The server has a timeout of 
 * 10 minutes (600000 milliseconds) on its socket. I hope to be able to add logging in the near future,
 * as well as Cryptology measures. 
 */
public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
	private Socket socket;

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */
    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();

        // Add Listeners
        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server. Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }//end actionPerformed. 
        });
    }//end ChatClient

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }//end getServerAddress()

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }//end getName()

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

    	try {
    		// Make connection and initialize streams
    		String serverAddress = getServerAddress();
    		System.out.println(serverAddress);
        
    		final String HOST = "localhost"; //"127.0.0.1"
    		//String serverAddress = HOST;
    		socket = new Socket(serverAddress, 9001);
    		in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
    		out = new PrintWriter(socket.getOutputStream(), true);

    		// Process all messages from server, according to the protocol.
    		while (true) {
    			String line = in.readLine();
    			if (line.startsWith("SUBMITNAME")) {
    				out.println(getName());
    			} else if (line.startsWith("NAMEACCEPTED")) {
    				textField.setEditable(true);
    			} else if (line.startsWith("MESSAGE")) {
    				messageArea.append(line.substring(8) + "\n");}
    		}//end while(true)
        
    	//catch a Timeout of Client. 
    	}catch(NullPointerException e) {
    		//Exception to the run most likely from timeout, GUI has sent message
    		//Need to fix this and make it separate from inactivity and actually disconnected. 
    		String message = "Disconnected, please close Chatter Box \n and Establish New Connection.";
    		JOptionPane.showMessageDialog(new JFrame(), message, "Dialog",JOptionPane.ERROR_MESSAGE);
    	}//end NullPointerException e
    }//end run()

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }//end main
}//end ChatClient
