import java.net.*;
import java.io.*;

public class Client {

    public static void main (String[]args) {
        try {
            Socket sock = new Socket("localhost",9999); // ADAPTER POUR LULU
            BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            while(true) {
                String mess = br.readLine();
                System.out.println(mess);
            }
            lire.close();
            ecrit.close();
            sock.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
