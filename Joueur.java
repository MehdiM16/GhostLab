import java.net.*;
import java.io.*;

public class Joueur implements Serializable {

    String id;
    int port_udp;
    boolean pret = false;
    Partie inscrit = null;

    public Joueur(String id, int port) {
        this.id = id;
        port_udp = port;
    }

}
