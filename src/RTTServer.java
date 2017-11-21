import java.io.IOException;
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
                    String addr = socket.getInetAddress().toString();
                    socket.close();

                    System.out.println(System.nanoTime() + " " + addr);
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
            int port = 20199;
            System.out.println("Listen: " + port);
            new NetworkService(port, 10).run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
