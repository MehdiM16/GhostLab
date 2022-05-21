import java.net.*;
import java.util.Random;
import java.io.*;
import java.lang.Runnable;

public class Fantome implements Runnable {

    int id;
    static int id_tot = 0;
    String positionX;
    String positionY;
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

    public synchronized String getPosX() {
        return positionX;
    }

    public synchronized String getPosY() {
        return positionY;
    }

    public synchronized void setPosX(int x) {
        positionX = posIntToString(x);
    }

    public synchronized void setPosY(int y) {
        positionY = posIntToString(y);
    }

    public void setAttraper(boolean b) {
        attraper = b;
    }

    public void deplacement() {
        String[] move_possible = { "UPMOV", "RIMOV", "DOMOV", "LEMOV" };
        boolean a_bouger = false;
        int posX = Integer.valueOf(positionX);
        int posY = Integer.valueOf(positionY);
        while (!a_bouger) {
            String move = move_possible[new Random().nextInt(move_possible.length)];
            if (move.equals("UPMOV")) {
                if (posX > 0 && labyrinthe.getCase(posX - 1, posY) != '|') {
                    labyrinthe.setCase(posX, posY, 'V'); // on libere la case
                    posX--;
                    labyrinthe.setCase(posX, posY, 'F'); // on remplie la nouvelle case par un fantome
                    setPosX(posX);
                    a_bouger = true;
                }
            } else if (move.equals("RIMOV")) {
                if (posY < labyrinthe.littleEndianToInt(labyrinthe.larg) - 1
                        && labyrinthe.getCase(posX, posY + 1) != '|') {
                    labyrinthe.setCase(posX, posY, 'V'); // on libere la case
                    posY++;
                    labyrinthe.setCase(posX, posY, 'F'); // on remplie la nouvelle case par un fantome
                    setPosY(posY);
                    a_bouger = true;
                }
            } else if (move.equals("DOMOV")) {
                if (posX < labyrinthe.littleEndianToInt(labyrinthe.haut) - 1
                        && labyrinthe.getCase(posX + 1, posY) != '|') {
                    labyrinthe.setCase(posX, posY, 'V'); // on libere la case
                    posX++;
                    labyrinthe.setCase(posX, posY, 'F'); // on remplie la nouvelle case par un fantome
                    setPosX(posX);
                    a_bouger = true;
                }
            } else if (move.equals("LEMOV")) {
                if (posY > 0 && labyrinthe.getCase(posX, posY - 1) != '|') {
                    labyrinthe.setCase(posX, posY, 'V'); // on libere la case
                    posY--;
                    labyrinthe.setCase(posX, posY, 'F'); // on remplie la nouvelle case par un fantome
                    setPosY(posY);
                    a_bouger = true;
                }
            }
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
            while (!attraper) {
                deplacement();
                String a_envoyer = "GHOST id : " + id + " " + positionX + " " + positionY + "+++";
                // ENLEVER ID FANTOME DU MESSAGE A ENVOYER QUAND TEST FINIS
                data = a_envoyer.getBytes();
                DatagramPacket message = new DatagramPacket(data, data.length, dest);
                sock_envoie.send(message);
                // Thread.sleep(new Random().nextInt(10000) + 10000); // entre 10000 et 20000
                Thread.sleep(20000);
            }
            System.out.println("je suis le fantome " + id + " et j'ai finis");

            sock_envoie.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
