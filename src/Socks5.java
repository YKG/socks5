import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Socks5 {


    public static void main(String[] args) {
        System.out.println("Hello World!");

        class Handler implements Runnable {
            private final Socket socket;
            Handler(Socket socket) { this.socket = socket; }
            public void run() {
                System.out.println("================================= new socket");
                try {
                    Socket clientSocket = this.socket;
                    InputStream in = clientSocket.getInputStream();
                    OutputStream out = clientSocket.getOutputStream();
                    String inputLine, outputLine;

                    int c = in.read();
//                    System.out.println(c + " " + String.format("%x", c));
                    c = in.read();
//                    System.out.println(c);
                    c = in.read();
//                    System.out.println(c);

                    out.write("\u0005\u0000".getBytes());
                    out.flush();

                    in.read();
                    in.read();
                    in.read();

                    StringBuilder sb = new StringBuilder();
                    String host = "";
                    int atyp = in.read();
                    switch (atyp) {
                        case 0x01: // IPv4
                            for (int i = 0; i < 4; i++) {
                                int ch = in.read();
                                sb.append("." + ch);
                            }
                            host = sb.toString().substring(1);
                            break;
                        case 0x03: // domain name
                            c = in.read(); // len
//                            System.out.println(c);
                            for (int i = 0; i < c; i++) {
                                int ch = in.read();
//                                System.out.println(ch);
                                sb.append((char) ch);
                            }
                            host = sb.toString();
                            break;
                        default:
                            System.err.println("Unsupported atyp: " + atyp);
                            break;
                    }

                    int port = 0;
                    c = in.read();
//                    System.out.println("ph: " + c);
                    port += (c << 8);
//                    System.out.println("ph: " + port);
                    c = in.read();
//                    System.out.println("pl: " + c + String.format(" %x", c));
                    port += c;
//                    System.out.println("p: " + port);
//                    if (port < 0) {
//                        System.err.println("port error");
//                        System.exit(-1);
//                    }

                    out.write("\u0005\u0000\u0000\u0001".getBytes());
                    for (int i = 0; i < 6; i++) {
                        out.write("\u0000".getBytes());
                    }
                    out.flush();

                    System.out.println(String.format("Connect to %s:%d", host, port));
                    Socket toRemote = new Socket(host, port);

                    InputStream fromLocalInput = clientSocket.getInputStream();
                    OutputStream fromLocalOutput = clientSocket.getOutputStream();
                    InputStream toRemotelInput = toRemote.getInputStream();
                    OutputStream toRemotelOutput = toRemote.getOutputStream();

                    class Relay extends Thread {
                        private InputStream in;
                        private OutputStream out;

                        private Relay(InputStream in, OutputStream out) {
                            this.in = in;
                            this.out = out;
                        }

                        public void run() {
                            try {
                                for (int c; (c = in.read()) != -1; ) {
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
                // read and service request on socket
            }
        }

        class NetworkService implements Runnable {
            private final ServerSocket serverSocket;
            private final ExecutorService pool;

            public NetworkService(int port, int poolSize)
                    throws IOException {
                serverSocket = new ServerSocket(port);
                pool = Executors.newFixedThreadPool(poolSize);
            }

            public void run() { // run the service
                try {
                    for (;;) {
                        pool.execute(new Handler(serverSocket.accept()));
                    }
                } catch (IOException ex) {
                    pool.shutdown();
                }
            }
        }

        try {
            new NetworkService(28888, 1).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
