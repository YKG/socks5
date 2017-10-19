import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Socks5 {
    public static void main(String[] args) {
        System.out.println("Hello World!");


        try (

//                ServerSocket serverSocket = new ServerSocket(8888, 0, InetAddress.getByName("192.168.1.103"));
                ServerSocket serverSocket = new ServerSocket(8888);


        ) {
            Socket clientSocket = serverSocket.accept();
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();
            String inputLine, outputLine;

            int c = in.read();
            System.out.println(c + " " + String.format("%x", c));
            c = in.read();
            System.out.println(c);
            c = in.read();
            System.out.println(c);

            out.write("\u0005\u0000".getBytes());
            out.flush();

            c = in.read();
            c = in.read();
            c = in.read();
            c = in.read();
            c = in.read(); // len
            System.out.println(c);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < c; i++) {
                int ch = in.read();
                System.out.println(ch);
                sb.append((char)ch);
            }
            String host = sb.toString();
            int port = 0;
            c = in.read();
            System.out.println("ph: " + c);
            port += (c << 8);
            System.out.println("ph: " + port);
            c = in.read();
            System.out.println("pl: " + c + String.format("%x", c));
            port += c;
            System.out.println("p: " + port);

            out.write("\u0005\u0000\u0000\u0001".getBytes());
            for (int i = 0; i < 6; i++) {
                out.write("\u0000".getBytes());
            }
            out.flush();

            System.out.println("Host: " + host);
            System.out.println("Port: " + port);
            Socket toRemote = new Socket(host, port);

            InputStream fromLocalInput = clientSocket.getInputStream();
            OutputStream fromLocalOutput = clientSocket.getOutputStream();
            InputStream toRemotelInput = toRemote.getInputStream();
            OutputStream toRemotelOutput = toRemote.getOutputStream();
//            PrintWriter toRemoteOut =
//                    new PrintWriter(toRemote.getOutputStream(), true);
//            BufferedReader toRemoteIn = new BufferedReader(
//                    new InputStreamReader(toRemote.getInputStream()));




            class Relay extends Thread {
                InputStream in;
                OutputStream out;
                byte[] buf;
                int cnt;
                static final int BUF_SIZE = 4096;

                public Relay(InputStream in, OutputStream out) {
                    this.in = in;
                    this.out = out;
                    buf = new byte[BUF_SIZE];
                    cnt = 0;
                }

                public void run() {
                    String inputLine, outputLine;
                    try {

                        for (int c = 0; (c = in.read()) != -1;) {
                            out.write(c);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            new Relay(fromLocalInput, toRemotelOutput).start();
            new Relay(toRemotelInput, fromLocalOutput).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
