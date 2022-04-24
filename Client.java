import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static void lire_partie(BufferedReader br, String message) {
        System.out.println(message);
        message = message.substring(0, message.length() - 3);// on enleve les etoiles
        int nb_partie = Integer.valueOf(message.substring(6));
        for (int i = 0; i < nb_partie; i++) {
            try {
                String part = br.readLine();
                System.out.println(part);
            } catch (Exception e) {
                System.out.println("erreur lecture partie");
            }
        }
    }

    public static void lire_joueur(BufferedReader br, String message) {
        System.out.println(message);
        message = message.substring(0, message.length() - 3);// on enleve les etoiles
        int nb_joueur = Integer.valueOf(message.substring(8));
        for (int i = 0; i < nb_joueur; i++) {
            try {
                String part = br.readLine();
                System.out.println(part);
            } catch (Exception e) {
                System.out.println("erreur lecture joueur");
            }
        }
    }

    public static void main(String[] args) {
        try {
            Socket sock = new Socket("localhost", 9999); // ADAPTER POUR LULU
            BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            Scanner sc = new Scanner(System.in);

            String mess = lire.readLine();
            lire_partie(lire, mess);
            String mess_recu = "";
            while (!mess_recu.equals("GOBYE***")) { // PROTOCOLE TCP
                System.out.println("vous pouvez entrez un message");
                mess = sc.nextLine();
                ecrit.println(mess);
                ecrit.flush();
                System.out.println(mess);
                if (mess.contains("LIST")) {
                    lire_joueur(lire, mess);
                }

                else if (mess.contains("UNREG") || mess.contains("SIZE")) {
                    mess_recu = lire.readLine();
                    System.out.println(mess_recu);
                }

                else if (mess.contains("GAMES")) {
                    mess_recu = lire.readLine();
                    lire_partie(lire, mess_recu);
                } else {
                    lire.readLine();
                    System.out.println(mess_recu);
                    System.out.println("Nous venons de lire le message du serveur");
                }

            }

            sc.close();
            lire.close();
            ecrit.close();
            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
