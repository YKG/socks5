import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPFIN {

    public static void main(String[] args) {
        System.out.println("Hello World!");


        try {
            ServerSocket serverSocket = new ServerSocket(8888);

            Socket clientSocket = serverSocket.accept();
            PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;

            InputStream is = clientSocket.getInputStream();
            clientSocket.shutdownInput();
            System.out.println(is.read());

            while ((inputLine = in.readLine()) != null) {
                outputLine = inputLine;
                System.out.println("Got: " + inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
            clientSocket.shutdownOutput();
//            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
