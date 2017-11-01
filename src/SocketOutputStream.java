import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
                    pool.execute(new Handle(serverSocket.accept()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            int port = 51888;
            System.out.println("Port 58888");
            new NetworkService(port, 1).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
