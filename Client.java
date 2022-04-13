import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static void main (String[]args) {
        try {
            Socket sock = new Socket("localhost",9999); // ADAPTER POUR LULU
            BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            Scanner sc = new Scanner(System.in);
            
            while(true) { // PROTOCOLE TCP
                String mess = br.readLine();
                System.out.println(mess);
                mess = sc.nextLine();
                ecrit.print(mess);
                ecrit.flush();
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
