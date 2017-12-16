import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class RTTServer {
    public static void main(String[] args) {
        class Handler implements Runnable {
            private final Socket socket;
            Handler(Socket socket) { this.socket = socket; }
            public void run() {
                try {
                    socket.setKeepAlive(true);
                    String addr = socket.getInetAddress().toString();
                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();
                    byte[] buf = new byte[4096];
                    while (in.read(buf) != -1) {
                        out.write("HTTP/1.1 200 OK\nContent-Length: 3\n\r\nHi\n".getBytes());
                        out.flush();
                        System.out.println(System.nanoTime() + " " + addr);
                    }
//                    socket.close();

                } catch (Exception e) {
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
            int port = 20180;
            System.out.println("Listen: " + port);
            new NetworkService(port, 10).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
