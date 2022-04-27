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
        labyrinthe = new Labyrinthe(6, 8); // valeur random pour test si fonctionne bien
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

    public boolean canPlayTour(int posX, int posY, int dir, int pas) {
        char[][] grille = labyrinthe.lab;
        int largeur = labyrinthe.larg;
        int hauteur = labyrinthe.haut;

        if (dir == 1) { // UPMOV
            if (posX - pas < 0)
                return false;
            for (int i = posX - 1; i >= posX - 1 - pas; i--) {
                if (grille[i][posY] == '|')
                    return false;
            }
            return true;
        } else if (dir == 2) { // RIMOV
            if (posY + pas >= largeur)
                return false;
            for (int i = posY + 1; i < posY + 1 + pas; i++) {
                if (grille[posX][i] == '|')
                    return false;
            }
            return true;
        } else if (dir == 3) { // DOMOV
            if (posX + pas >= hauteur)
                return false;
            for (int i = posX + 1; i < posX + 1 + pas; i++) {
                if (grille[i][posY] == '|')
                    return false;
            }
            return true;
        } else if (dir == 4) { // LEMOV
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

    public void joueTour(Joueur j, int dir, int pas) {
        int posX = j.positionX;
        int posY = j.positionY;
        if (canPlayTour(posX, posY, dir, pas)) {
            if (dir == 1)
                j.positionX -= pas; // UPMOV
            else if (dir == 2)
                j.positionY += pas; // RIMOV
            else if (dir == 3)
                j.positionX += pas; // DOMOV
            else
                j.positionY -= pas; // LEMOV
        }
    }

    public void run() {
    }

}
