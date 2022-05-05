import java.io.*;
import java.net.*;
import java.lang.Runnable;
import java.util.ArrayList;
import java.lang.Thread;

public class Partie implements Runnable, Serializable {

    ArrayList<Joueur> liste = new ArrayList<Joueur>();
    final byte id;
    static byte id_tot = 0;
    byte nombre_joueur = 0;
    boolean start = false;
    Labyrinthe labyrinthe;
    Thread partThread;
    byte[] address_diffusion;
    String port_diffusion;
    Socket sock;
    DatagramSocket dgsock;

    public Partie() {
        id = id_tot;
        id_tot++;
        String addr_tmp = "225.1.1." + String.valueOf(id);
        int taille = addr_tmp.length();
        for (int i = taille; i < 15; i++) {
            addr_tmp += '#';
        }
        address_diffusion = addr_tmp.getBytes();
        port_diffusion = String.valueOf(8000 + id);
        labyrinthe = new Labyrinthe(address_diffusion, port_diffusion); // valeur random pour test si fonctionne bien
        /*
         * try {
         * Socket sock = new Socket("localhost", 9999);
         * DatagramSocket dgsock = new DatagramSocket();
         * } catch (Exception e) {
         * e.printStackTrace();
         * }
         */
    }

    public synchronized boolean enregistre_joueur(Joueur j) { // enregistre un joueur dans une partie
        boolean ajout = liste.add(j);
        j.inscrit = this;
        nombre_joueur++;
        return ajout;

    }

    public synchronized int remove_joueur(Joueur j) {
        boolean retire = liste.remove(j);
        if (retire) {
            j.inscrit = null;
            nombre_joueur--;
            return this.id;
        } else {
            return -1;
        }
    }

    public synchronized byte nombre_inscrit() {
        return nombre_joueur;
    }

    public synchronized ArrayList<Joueur> getList() {
        return liste;
    }

    public boolean peut_commencer() {
        for (Joueur j : liste) {
            if (!j.pret)
                return false;
        }
        return true;
    }

    public boolean canPlayTour(int posX, int posY, String dir, int pas) {
        char[][] grille = labyrinthe.lab;
        int largeur = labyrinthe.littleEndianToInt(labyrinthe.larg);
        int hauteur = labyrinthe.littleEndianToInt(labyrinthe.haut);

        if (dir.equals("UPMOV")) { // UPMOV
            if (posX - pas < 0)
                return false;
            for (int i = posX - 1; i >= posX - 1 - pas; i--) {
                if (grille[i][posY] == '|')
                    return false;
            }
            return true;
        } else if (dir.equals("RIMOV")) { // RIMOV
            if (posY + pas >= largeur)
                return false;
            for (int i = posY + 1; i < posY + 1 + pas; i++) {
                if (grille[posX][i] == '|')
                    return false;
            }
            return true;
        } else if (dir.equals("DOMOV")) { // DOMOV
            if (posX + pas >= hauteur)
                return false;
            for (int i = posX + 1; i < posX + 1 + pas; i++) {
                if (grille[i][posY] == '|')
                    return false;
            }
            return true;
        } else if (dir.equals("LEMOV")) { // LEMOV
            if (posY - pas < 0)
                return false;
            for (int i = posY - 1; i >= posY - pas; i--) {
                if (grille[posX][i] == '|')
                    return false;
            }
            return true;
        } else
            return false;
    }

    public void joueTourbis(Joueur j, String dir, int pas) {
        int posX = Integer.valueOf(j.positionX);
        int posY = Integer.valueOf(j.positionY);
        if (canPlayTour(posX, posY, dir, pas)) {
            if (dir.equals("UPMOV")) {
                posX -= pas; // UPMOV
                j.positionX = j.posIntToString(posX);
            } else if (dir.equals("RIMOV")) {
                posY += pas; // RIMOV
                j.positionY = j.posIntToString(posY);
            } else if (dir.equals("DOMOV")) {
                posX += pas; // DOMOV
                j.positionX = j.posIntToString(posX);
            } else if (dir.equals("LEMOV")) {
                posY -= pas; // LEMOV
                j.positionY = j.posIntToString(posY);
            }
        }
    }

    public void joueTour(Joueur j, String dir, String pas_s) {
        int pas = Integer.valueOf(pas_s);
        int posX = Integer.valueOf(j.positionX);
        int posY = Integer.valueOf(j.positionY);
        if (dir.equals("UPMOV")) {
            // posX -= pas; // UPMOV
            while (labyrinthe.lab[posX][posY] != '|' && posX > 0 && pas > 0) {
                posX--;
                pas--;
            }
            j.positionX = j.posIntToString(posX);
        } else if (dir.equals("RIMOV")) {
            // posY += pas; // RIMOV
            while (posY < labyrinthe.littleEndianToInt(labyrinthe.larg) - 1 && labyrinthe.lab[posX][posY] != '|'
                    && pas > 0) {
                posY++;
                pas--;
                System.out.println("y : " + posY + " , larg : " + labyrinthe.littleEndianToInt(labyrinthe.larg));
            }
            j.positionY = j.posIntToString(posY);
        } else if (dir.equals("DOMOV")) {
            // posX += pas; // DOMOV
            while (posX < labyrinthe.littleEndianToInt(labyrinthe.haut) - 1 && labyrinthe.lab[posX][posY] != '|'
                    && pas > 0) {
                posX++;
                pas--;
            }
            j.positionX = j.posIntToString(posX);
        } else if (dir.equals("LEMOV")) {
            // posY -= pas; // LEMOV
            while (labyrinthe.lab[posX][posY] != '|' && posY > 0 && pas > 0) {
                posY--;
                pas--;
            }
            j.positionY = j.posIntToString(posY);
        }
    }

    public void run() {
        try {
            System.out.println("Je suis une partie");
            while (!peut_commencer()) {
                // System.out.println("tout les joueur ne sont pas pret");
                start = false;
                Thread.sleep(1000);
            }
            start = true;
            System.out.println("la partie commence");
            // cette fonction servira principalement pour faire deplacer les fantomes dans
            // le labyrinthe
            // et peut etre pour les message udp et le multicast, je ne sais pas encore
            // s'il vaut mieux le faire ici ou dans la classe serveur

            while (start) {
                for (int i = 0; i < labyrinthe.nombre_fantome; i++) {

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
