import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");


        try (

                ServerSocket serverSocket = new ServerSocket(8888, 0, InetAddress.getByName("192.168.1.103"));

                Socket clientSocket = serverSocket.accept();
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String inputLine, outputLine;

            while ((inputLine = in.readLine()) != null) {
                outputLine = inputLine;
                System.out.println("Got: " + inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
