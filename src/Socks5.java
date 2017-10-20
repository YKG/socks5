import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Socks5 {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        class Handler implements Runnable {
            private final Socket socket;
            Handler(Socket socket) { this.socket = socket; }
            public void run() {
                try {
                    Socket clientSocket = this.socket;
                    clientSocket.setSoTimeout(3000);
                    InputStream in = clientSocket.getInputStream();
                    OutputStream out = clientSocket.getOutputStream();
                    String inputLine, outputLine;

                    int c = in.read();
                    c = in.read();
                    c = in.read();
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
                            for (int i = 0; i < c; i++) {
                                int ch = in.read();
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
                    port += (c << 8);
                    c = in.read();
                    port += c;

                    boolean buggy = false;
                    if (buggy) {
                        out.write("\u0005\u0000\u0000\u0001".getBytes());
                        for (int i = 0; i < 6; i++) {
                            out.write("\u0000".getBytes());
                        }
                    } else {
                        StringBuilder reply = new StringBuilder("\u0005\u0000\u0000\u0001");
                        for (int i = 0; i < 6; i++) {
                            reply.append("\u0000");
                        }
                        out.write(reply.toString().getBytes());
                    }
                    out.flush();

//                    if (port == 443 || host.contains("apple")) {
//                        System.err.println(String.format("Cancel Connect to %s:%d", host, port));
//                        throw new SocketException("443");
//                    } else {
//                        System.out.println(String.format("Connect to %s:%d", host, port));
//                    }
                    System.out.println(String.format("Connect to %s:%d", host, port));
                    Socket toRemote = new Socket(host, port);

                    InputStream fromLocalInput = clientSocket.getInputStream();
                    OutputStream fromLocalOutput = clientSocket.getOutputStream();
                    InputStream toRemotelInput = toRemote.getInputStream();
                    OutputStream toRemotelOutput = toRemote.getOutputStream();

                    class SocketCloseDaemon extends Thread {
                        private InputStream in1, in2;
                        private SocketCloseDaemon(InputStream in1, InputStream in2) {
                            this.in1 = in1;
                            this.in2 = in2;
                        }

                        public void run() {
                            while (true) {
                                try {
                                    if (in1.available() == 0 && in2.available() == 0) {
//                                        socket.close();

                                        toRemotelOutput.close();
//                                        fromLocalOutput.close();
                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    class Relay extends Thread {
                        private InputStream in;
                        private OutputStream out;
                        private Relay(InputStream in, OutputStream out) {
                            this.in = in;
                            this.out = out;
                        }
                        public void run() {
                            while (true) {
                                try {
//                                    System.out.println("Local ======>>  Relay");
                                    for (int c; ((c = in.read()) != -1); ) {
                                        out.write(c);
                                    }
                                    toRemote.shutdownOutput();
//                                    System.out.println("<<<<<<<<<<<<<<<<< exit loop");
                                    break;
                                } catch (SocketTimeoutException ste) {
//                                    System.out.println("Local ==TOUT=>>  Relay");
                                } catch (SocketException e) {
//                                    e.printStackTrace();
                                    System.err.println("E:" + e.getCause() + " " + e.getMessage());
                                    break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }
                    }

                    class Relay2 extends Thread {
                        private InputStream in;
                        private OutputStream out;
                        private Relay2(InputStream in, OutputStream out) {
                            this.in = in;
                            this.out = out;
                        }
                        public void run() {
                            try {
//                                System.out.println("                Relay <<======= Remote");
                                for (int c; ((c = in.read()) != -1); ) {
                                    out.write(c);
                                }
//                                System.out.println("<<<<<<<<<<<<<<<<< exit loop");
//                                System.out.println("                Relay <<==FIN== Remote");
                                socket.shutdownOutput();
                                socket.shutdownInput();
                            } catch (SocketException e) {
//                                e.printStackTrace();
                                System.err.println("E:" + e.getMessage());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    SocketCloseDaemon d = new SocketCloseDaemon(fromLocalInput, toRemotelInput);
                    new Relay(fromLocalInput, toRemotelOutput).start();
                    new Relay2(toRemotelInput, fromLocalOutput).start();
                } catch (SocketException e) {
                    e.printStackTrace();
                    try {
                        System.err.println("Socket closing..."  + e.getMessage());
                        socket.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
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
//                    int cnt = 0;
//                    new Handler(serverSocket.accept()).run();
                    for (;;) {
//                        System.out.print("socket " + (cnt++) + ": ");
                        pool.execute(new Handler(serverSocket.accept()));
                    }
                } catch (IOException ex) {
                    pool.shutdown();
                }
            }
        }
        try {
            int port = 58888;
            System.out.println("Listen: " + port);
            new NetworkService(port, 9).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
