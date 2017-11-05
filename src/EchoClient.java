import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {

    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 9999);

            final BufferedInputStream in = new BufferedInputStream(s.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream());

            class ReadThread extends Thread {
                @Override
                public void run() {
                    byte buf[] = new byte[512];
                    int n;
                    try {
                        while ((n = in.read(buf, 0, 512)) != -1) {
                            System.out.println(new String(buf, 0, n));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            new ReadThread().start();

            Scanner stdin = new Scanner(System.in);
            while (stdin.hasNext()) {
                String cmd = stdin.next();
                out.write(cmd.getBytes());
                out.flush();
            }
            System.out.println("End");
            s.shutdownOutput();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
