import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    public static void main(String[] args) {
//        server_send_RST_at_the_end();
        server_send_RST_at_the_end();
    }

    private static void server_send_RST_at_the_end() {
        try {
            ServerSocket server = new ServerSocket(9999);

            Socket client = server.accept();
            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());

            byte[] buf = new byte[512];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void orderly_release() {
        try (
            ServerSocket server = new ServerSocket(9999);

            Socket client = server.accept();
            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
        ){

            byte[] buf = new byte[512];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void orderly_release_can_be_done_by_close_the_socket() {
        try {
            ServerSocket server = new ServerSocket(9999);

            Socket client = server.accept();
            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());

            byte[] buf = new byte[512];
            int n;
            while ((n = in.read(buf)) != -1) {
                System.out.println("n: " + n);
                out.write(buf, 0, n);
                out.flush();
            }

            System.out.println("Closing...");
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void orderly_release_can_be_done_by_shutdown_the_socket_outputstream() {
        try {
            ServerSocket server = new ServerSocket(9999);

            Socket client = server.accept();
            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());

            byte[] buf = new byte[512];
            int n;
            while ((n = in.read(buf)) != -1) {
                System.out.println("n: " + n);
                out.write(buf, 0, n);
                out.flush();
            }

           client.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
