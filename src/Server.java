
import java.io.DataInputStream;
import java.io.DataOutputStream;
/**
 *
 * @author NgocLong
 */

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws Exception {
        Dictionary dictionary = new Dictionary();
        ServerSocket server = new ServerSocket(1337);
        System.out.println("Server initialized. Waiting for connection...");
        Socket socket = server.accept();
        System.out.println("Connection accepted!");
        DataInputStream dataIn = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

        String stringIn;
        String stringOut;

        while (true) {
            stringIn = dataIn.readUTF();
            if (stringIn.equals("bye")) {
                System.out.println("Received stop signal");
                break;
            }
            System.out.println("Received from client:" + stringIn);

                String result = dictionary.HandleInput(stringIn);
                stringOut = String.format("%s", result);

            dataOut.writeUTF(stringOut);
            dataOut.flush();
            System.out.println("Sent to client:" + stringOut);
        }
        System.out.println("Bye bye! Server shutting down...");
        dataIn.close();
        socket.close();
        server.close();
    }
}