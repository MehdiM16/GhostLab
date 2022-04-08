import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Serveur {

    ArrayList<Partie> liste = new ArrayList<Partie>(); 
    
    public boolean enregistre_joueur (String joueur_id, int port, int m) {
        for (Partie p : liste) {
            if (p.id == m) {
                p.liste.add(new Joueur(joueur_id, port));
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serv = new ServerSocket(9999);
            MulticastSocket mso = new MulticastSocket(12500);
            while (true) {
                Socket sock = serv.accept();
                BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                
                ecrit.println("GAMES " + String.valueOf(liste.size()) + "***");
                ecrit.flush();
                for (Partie p : liste) {
                    ecrit.println("OGAME " + String.valueOf(p.id) + String.valueOf(p.liste.size()) + "***");
                    ecrit.flush();
                }
                String mess = lire.readLine();
                if (mess.contains("NEWPL")) {
                    String[] instruction = mess.split(' ');
                    if (instruction.length != 3) {
                        ecrit.println("REGNO***");
                        ecrit.flush();
                    }
                    else {
                        pnew = new Partie();
                        liste.add(pnew);
                        pnew.liste.add(new Joueur(instruction[1], instruction[2]));
                        ecrit.println("REGOK " + pnew.id + "***");
                        ecrit.flush();
                    }
                }
                
                else if (mess.contains("REGIS")) {
                    String[] instruction = mess.split(' ');
                    if (instruction.length != 4) {
                        ecrit.println("REGNO***");
                        ecrit.flush();
                    }
                    boolean enregistre = enregistre_joueur(instruction[1], instruction[2], instruction[3]);
                    if (boolean) {
                        ecrit.println("REGOK " + pnew.id + "***");
                        ecrit.flush();
                    }
                    else {
                        ecrit.println("REGNO***");
                        ecrit.flush();
                    }
                }
                

                lire.close();
                ecrit.close();
                sock.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}