import java.io.*;
import java.net.*;
import java.lang.Runnable;

public class Partie implements Runnable, Serializable {

    ArrayList<Joueur> liste = new Arraylist<Joueur>();
    final int id;
    static int id_tot = 0;
    boolean start = false;
    Labyrinthe labyrinthe;

    Socket sock = new Socket(9999);
    DatagramSocket dgsock = new DatagramSocket();

    public Partie() {
        id = id_tot;
        id_tot++;
    }

    public boolean peut_commencer() {
        for (Joueur j : liste) {
            if (!j.pret)
                return false;
        }
        return true;
    }

}
