import java.net.*;
import java.io.*;

public class Joueur {

    String pseudo;
    final int id;
    static int id_tot = 0;
    int port_udp;
    boolean pret = false;
    Partie inscrit = null;
    String positionX;
    String positionY;
    String point = "0000";
    String nom_machine;

    public Joueur() {
        pseudo = "anonyme0";
        port_udp = -1;
        id = -1;
    }

    public Joueur(String pseudo, int port, String nom) {
        this.pseudo = pseudo;
        port_udp = port;
        nom_machine = nom;
        this.id = id_tot;
        id_tot++;
    }

    public synchronized void setPosX(int x) {
        positionX = posIntToString(x);
    }

    public synchronized void setPosY(int y) {
        positionY = posIntToString(y);
    }

    public synchronized void setPoint(String p) {
        point = p;
    }

    public String posIntToString(int p) {
        String res = String.valueOf(p);
        if (res.length() == 1) {
            res = "00" + res;
        } else if (res.length() == 2) {
            res = "0" + res;
        }
        return res;
    }

    public String scoreToString(int p) {
        String res = String.valueOf(p);
        if (res.length() == 1) {
            res = "000" + res;
        } else if (res.length() == 2) {
            res = "00" + res;
        } else if (res.length() == 3) {
            res = "0" + res;
        }
        return res;
    }

}
