import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Serveur {

    ArrayList<Partie> liste = new ArrayList<Partie>();

    public static void main(String[] args) {
        try {
            ServerSocket serv = new ServerSocket(9999);
            while (true) {
                BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));

                lire.close();
                ecrit.close();
                serv.close();
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

}