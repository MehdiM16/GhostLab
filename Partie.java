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
    String address_diffusion;
    String port_diffusion;
    Socket sock;
    DatagramSocket dgsock;

    public Partie(Socket s) {
        sock = s;
        id = id_tot;
        id_tot++;
        address_diffusion = "225.1.1." + String.valueOf(id);
        port_diffusion = String.valueOf(8000 + id);
        labyrinthe = new Labyrinthe(); // valeur random pour test si fonctionne bien
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
        try {
            BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            System.out.println("Je suis une partie");
            while (!peut_commencer()) {
                // System.out.println("tout les joueur ne sont pas pret");
                start = false;
                Thread.sleep(1000);
            }
            start = true;
            System.out.println("la partie commence");
            /*
             * ecrit.print("WELCO " + id + " " + labyrinthe.haut + " " + labyrinthe.larg +
             * " " + labyrinthe.nombre_fantome
             * + " " + address_diffusion + " " + port_diffusion + "***");
             * ecrit.flush();
             */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
