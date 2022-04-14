import java.net.*;
import java.io.*;

public class Joueur implements Runnable, Serializable {

    String pseudo;
    final int id;
    static int id_tot = 0;
    int port_udp;
    boolean pret = false;
    Partie inscrit = null;
    Thread joueurThread;

    public Joueur(String pseudo, int port) {
        this.pseudo = pseudo;
        port_udp = port;
        this.id = id_tot;
        id_tot++;
    }

}
