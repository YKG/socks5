import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Handler implements Runnable {
    private final Socket fromClient;
    private Socket toServer;
    private ExecutorService pool;
    private final int BUFSIZE = 20 * 1024;

    Handler(Socket socket, ExecutorService pool) {
        this.fromClient = socket;
        this.pool = pool;
    }

    public void run() {
        try {
//            fromClient.setSoTimeout(3000);
            InputStream in = fromClient.getInputStream();
            OutputStream out = fromClient.getOutputStream();
            String inputLine, outputLine;

            int c = in.read();
            if (c != 0x05) {
                closeFromClientSocket();
                return;
            }
            int methodLength = in.read();
            for (int i = 0; i < methodLength; i++) {
                c = in.read(); // discard
            }
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
                    System.err.println("Unsupported  atyp: " + atyp);
                    fromClient.close();
                    return;
            }
            int port = 0;
            c = in.read();
            port += (c << 8);
            c = in.read();
            port += c;

            if (port == 0) {
                fromClient.close();
                System.out.println(String.format("Cancel connect to %s:%d", host, port));
                return;
            }

            boolean buggy = false;
            if (buggy) {
//                out.write("\u0005\u0000\u0000\u0001".getBytes());
//                for (int i = 0; i < 6; i++) {
//                    out.write("\u0000".getBytes());
//                }
            } else {
                StringBuilder reply = new StringBuilder("\u0005\u0000\u0000\u0001");
                for (int i = 0; i < 6; i++) {
                    reply.append("\u0000");
                }
                out.write(reply.toString().getBytes());
            }
            out.flush();

            System.out.println(String.format("Connect to %s:%d", host, port));

            if (connectToServer(host, port)) {
                relay();
            } else {
                closeFromClientSocket();
            }
        } catch (SocketException e) {
            e.printStackTrace();
            closeFromClientSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeFromClientSocket() {
        try {
            System.err.println("Socket closing...");
            fromClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean connectToServer(String host, int port) {
        try {
//            toServer = new Socket(host, port);
            toServer = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("128.199.111.96", 51888)));

            InetSocketAddress dest = InetSocketAddress.createUnresolved(host, port);

            toServer.connect(dest);
//            toServer.setSoTimeout(5000);
            return true;
        } catch (ConnectException ex) {
            if (ex.getMessage().contains("Connection timed out")) {
                System.err.println("Connection timed out: " + host + ":" + port);
            } else {
                ex.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void encode(byte[] buf, int size) {
        for (int i = 0; i < size; i++) {
            buf[i] = (byte) (buf[i] ^ (byte)('S'));
        }
    }

    private void decode(byte[] buf, int size) {
        encode(buf, size);
    }

    class SocketReader extends Thread {
        public SocketReader() {

        }

        @Override
        public void run() {
//            System.out.println("C --> S start");
            try {
                BufferedInputStream in = new BufferedInputStream(fromClient.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(toServer.getOutputStream());

                int n;
                byte buf[] = new byte[BUFSIZE];

                while ((n = in.read(buf)) != -1) {
//                            System.out.println("C --> R  n: " + n);
                    encode(buf, n);
                    out.write(buf, 0, n);
                    out.flush();
                }
            } catch (SocketTimeoutException ex) {
                System.err.println("C --> R Socket timeout..."  + ex.getMessage());
            } catch (SocketException ex) {
                if (ex.getMessage().contains("Socket closed")){
                    System.err.println("    XXXX R XXX S ..."  + ex.getMessage() + " // ");
//                    System.err.println("    XXXX R XXX S ..."  + ex.getMessage() + " // "+ toServer.getInetAddress().toString() + ":" + toServer.getPort());
                } else if (ex.getMessage().contains("reset")) {
                    ex.printStackTrace();
                    System.err.println("C --> R       RST..."  + ex.getMessage() + " // "+ toServer.getInetAddress().toString() + ":" + toServer.getPort());
                } else if (ex.getMessage().contains("Software caused connection abort: socket write error")){
                    System.err.println("C --> R       RST abort..."  + ex.getMessage() + " // "+ toServer.getInetAddress().toString() + ":" + toServer.getPort());
                } else if (ex.getMessage().contains("Broken pipe")){
                    System.err.println(" C --> R      ..."  + ex.getMessage() + " // "+ toServer.getInetAddress().toString() + ":" + toServer.getPort());
                } else {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (!toServer.isClosed()) {
                        toServer.close();
                    }
                    if (!fromClient.isClosed()) {
                        fromClient.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    class SocketWriter extends Thread {
        public SocketWriter() {

        }

        @Override
        public void run() {
//            System.out.println("C <-- S start");

            try {
                BufferedInputStream in = new BufferedInputStream(toServer.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(fromClient.getOutputStream());

                int n;
                byte buf[] = new byte[BUFSIZE];
                while ((n = in.read(buf)) != -1) {
//                            System.out.println("      R <-- S  n: " + n);
                    decode(buf, n);
                    out.write(buf, 0, n);
                    out.flush();
                }
            } catch (SocketTimeoutException ex) {
                System.err.println("         R <-- S Socket timeout..."  + ex.getMessage());
            } catch (SocketException ex) {
                if (ex.getMessage().contains("Socket closed")){
                    System.err.println("    XXXX R XXX S ..."  + ex.getMessage() + " // ");
//                    System.err.println("    XXXX R XXX S ..."  + ex.getMessage() + " // "+ toServer.getInetAddress().toString() + ":" + toServer.getPort());
                } else if (ex.getMessage().contains("reset")) {
                    System.err.println("         R <-- S RST..."  + ex.getMessage() + " // "+ toServer.getInetAddress().toString() + ":" + toServer.getPort());
                } else if (ex.getMessage().contains("Software caused connection abort: socket write error")){
                    System.err.println("         R <-- S RST abort..."  + ex.getMessage() + " // "+ toServer.getInetAddress().toString() + ":" + toServer.getPort());
                } else if (ex.getMessage().contains("Broken pipe")){
                    System.err.println("    XXXX R <-- S ..."  + ex.getMessage() + " // "+ toServer.getInetAddress().toString() + ":" + toServer.getPort());
                } else {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                try {
                    if (!fromClient.isClosed()) {
                        fromClient.close();
                    }
                    if (!toServer.isClosed()) {
                        toServer.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    private void relay() {
        new SocketWriter().start();
        new SocketReader().start();
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

    public void run() {
        try {
            for (;;) {
                pool.execute(new Handler(serverSocket.accept(), pool));
            }
        } catch (IOException ex) {
            pool.shutdown();
        }
    }
}

public class Socks5Rewrite {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        try {
            int port = 51888;
            int poolSize = 32;
            System.out.println("Listen: " + port + " PoolSize: " + poolSize);
            new NetworkService(port, poolSize).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
