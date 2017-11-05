import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketOutputStream {
    public static void main(String[] args) {

        class Handle implements Runnable {
            Socket client;
            public Handle(Socket s) {
                client = s;
            }

            public void run() {
                try {
                    InputStream in = client.getInputStream();
                    OutputStream out = client.getOutputStream();

                    out.write("Hello".getBytes());
                    out.write(new byte[]{' '});
                    out.write(new byte[]{'W'});
                    out.write(new byte[]{'o'});
                    out.write(new byte[]{'r'});
                    out.write(new byte[]{'l'});
                    out.write(new byte[]{'d'});

                    int c;
                    while ((c = in.read()) != -1) {
                        out.write(c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        class BufferedHandle implements Runnable {
            Socket client;
            public BufferedHandle(Socket s) {
                client = s;
            }

            public void run() {
                try {
                    BufferedInputStream in = new BufferedInputStream(client.getInputStream());
                    BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream(), 16);

                    out.write("Hello".getBytes());
                    out.write(new byte[]{' '});
                    out.write(new byte[]{'W'});
                    out.write(new byte[]{'o'});
                    out.write(new byte[]{'r'});
                    out.write(new byte[]{'l'});
                    out.write(new byte[]{'d'});

                    int c;


                    while ((c = in.read()) != -1) {
                        out.write(c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
//                    pool.execute(new Handle(serverSocket.accept()));
                    pool.execute(new BufferedHandle(serverSocket.accept()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            int port = 51888;
            System.out.println("Port 51888");
            new NetworkService(port, 1).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
