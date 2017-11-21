import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class RTT {

    public static void main(String[] args) {
//        String host = "52.191.134.183";
        String host = "127.0.0.1";
//        host = "kaige.org";
//        host = "139.199.20.173";
//        host = "52.191.134.183";
        host = "sgp2";
        int port = 22;

        int count = 5;
        printRTT(host, port, count);
    }

    private static void printRTT(String host, int port, int count) {
        double avgRTT = 0;
        for (int i = 0; i < count; i++) {
            double rtt = (double) getRTT(host, port) / 1000000;
            avgRTT = avgRTT/(i + 1) * i + rtt/(i + 1);
            System.out.printf("%11.6fms %7.2fms\n", rtt, avgRTT);
        }
    }

    private static long getRTT(String host, int port) {
        SocketAddress serverAddr = new InetSocketAddress(host, port);

        try (Socket s = new Socket()){
            long startTime = System.nanoTime();
            // ... the code being measured ...
            s.connect(serverAddr);
            long estimatedTime = System.nanoTime() - startTime;
            return estimatedTime;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
