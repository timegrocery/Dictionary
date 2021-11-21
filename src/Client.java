/**
 *
 * @author NgocLong
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 1337);
        DataInputStream dataIn = new DataInputStream((socket.getInputStream()));
        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String stringOut;
        String stringIn;

        while (true) {
            System.out.print("Look up word:");
            stringOut = reader.readLine();
            dataOut.writeUTF(stringOut);
            dataOut.flush();
            if (stringOut.equals("bye")) {
                System.out.println("Connection closed.");
                break;
            }
            stringIn = dataIn.readUTF();
            System.out.println("Received from server:\n---\n" + stringIn + "\n---");
        }

        dataOut.close();
        dataIn.close();
        socket.close();
    }
}