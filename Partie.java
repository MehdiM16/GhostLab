import java.io.*;
import java.net.*;
import java.lang.Runnable;
import java.util.ArrayList;

public class Partie implements Runnable, Serializable {

    ArrayList<Joueur> liste = new ArrayList<Joueur>();
    final int id;
    static int id_tot = 0;
    boolean start = false;
    Labyrinthe labyrinthe;

    Socket sock;
    DatagramSocket dgsock;

    public Partie() {
        id = id_tot;
        id_tot++;
        try {
            Socket sock = new Socket("localhost", 9999);
            DatagramSocket dgsock = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean peut_commencer() {
        for (Joueur j : liste) {
            if (!j.pret)
                return false;
        }
        return true;
    }

    public void run() {
    }

}
