import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ShutdownInput {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("kaige.org", 80);

            InputStream in = s.getInputStream();


            class Helper extends Thread {
                public void run() {
                    try {
                        System.out.println("In helper :::: begin");
                        Thread.sleep(3000);
                        System.out.println("In helper :::: 2222");
                        s.shutdownInput();
                        System.out.println("In helper :::: 3333");
                        System.out.println("In helper: " + in.read());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            new Helper().start();

            int c = in.read();
            System.out.println(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
