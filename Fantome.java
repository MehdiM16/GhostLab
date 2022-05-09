import java.net.*;
import java.util.Random;

import javax.swing.text.LayoutQueue;

import java.io.*;
import java.lang.Runnable;

public class Fantome implements Runnable {

    int id;
    static int id_tot = 0;
    String positionX;
    String positionY;
    int vitesse; // plus la vitesse est grande, plus le fantome se deplace rapidement
    boolean attraper = false;
    byte[] addresse_diffusion;
    String port_diffusion;
    Labyrinthe labyrinthe;

    public Fantome() {
    }// constructeur pour les fantome temporaire

    public Fantome(String x, String y, byte[] addr, String port, Labyrinthe l) {
        id = id_tot;
        id_tot++;
        positionX = x;
        positionY = y;
        addresse_diffusion = addr;
        port_diffusion = port;
        vitesse = new Random().nextInt(3) + 1; // les vitesse possible sont 1, 2 et 3
        labyrinthe = l;
    }

    public String byteArrayToString(byte[] tab) {
        String tmp = new String(tab);
        String res = "";
        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) != '#') {
                res += tmp.charAt(i);
            }
        }
        return res;
    }

    public void deplacement(String move, int dist) {
        int posX = Integer.valueOf(positionX);
        int posY = Integer.valueOf(positionY);
        if (move.equals("UPMOV")) {
            // posX -= pas; // UPMOV
            while (posX > 0 && labyrinthe.getCase(posX - 1, posY) != '|' && dist > 0) {
                labyrinthe.setCase(posX, posY, 'V'); // on libere la case
                posX--;
                dist--;
                labyrinthe.setCase(posX, posY, 'F'); // on remplie la nouvelle case par un fantome
            }
            positionX = posIntToString(posX);
        } else if (move.equals("RIMOV")) {
            // posY += pas; // RIMOV
            while (posY < labyrinthe.littleEndianToInt(labyrinthe.larg) - 1 && labyrinthe.getCase(posX, posY + 1) != '|'
                    && dist > 0) {
                labyrinthe.setCase(posX, posY, 'V'); // on libere la case
                posY++;
                dist--;
                labyrinthe.setCase(posX, posY, 'F'); // on remplie la nouvelle case par un fantome
            }
            positionY = posIntToString(posY);
        } else if (move.equals("DOMOV")) {
            // posX += pas; // DOMOV
            while (posX < labyrinthe.littleEndianToInt(labyrinthe.haut) - 1 && labyrinthe.getCase(posX + 1, posY) != '|'
                    && dist > 0) {
                labyrinthe.setCase(posX, posY, 'V'); // on libere la case
                posX++;
                dist--;
                labyrinthe.setCase(posX, posY, 'F'); // on remplie la nouvelle case par un fantome
            }
            positionX = posIntToString(posX);
        } else if (move.equals("LEMOV")) {
            // posY -= pas; // LEMOV
            while (posY > 0 && labyrinthe.getCase(posX, posY - 1) != '|' && dist > 0) {
                labyrinthe.setCase(posX, posY, 'V'); // on libere la case
                posY--;
                dist--;
                labyrinthe.setCase(posX, posY, 'F'); // on remplie la nouvelle case par un fantome
            }
            positionY = posIntToString(posY);
        }
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

    public void run() {
        try {
            DatagramSocket sock_envoie = new DatagramSocket();
            byte[] data;
            InetSocketAddress dest = new InetSocketAddress(byteArrayToString(addresse_diffusion),
                    Integer.valueOf(port_diffusion));
            String[] move_possible = { "UPMOV", "RIMOV", "DOMOV", "LEMOV" };
            while (!attraper) {
                int choix = new Random().nextInt(4);
                deplacement(move_possible[choix], vitesse);
                String a_envoyer = "GHOST id : " + id + " " + positionX + " " + positionY + "+++";
                data = a_envoyer.getBytes();
                DatagramPacket message = new DatagramPacket(data, data.length, dest);
                sock_envoie.send(message);
                // Thread.sleep(vitesse * 2000);
                Thread.sleep(20000);
            }

            sock_envoie.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
