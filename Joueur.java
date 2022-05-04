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
    String positionX;
    String positionY;

    public Joueur() {
        pseudo = "anonyme0";
        port_udp = -1;
        id = -1;
    }

    public Joueur(String pseudo, int port) {
        this.pseudo = pseudo;
        port_udp = port;
        this.id = id_tot;
        id_tot++;
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

    public void sendMessage(String mess) {
        try {
            DatagramSocket ds = new DatagramSocket(); // pour envoyer
            byte[] data = new byte[100];
            InetSocketAddress ia = new InetSocketAddress("225.1.2.4", 11000);
            data = mess.getBytes();
            DatagramPacket dp = new DatagramPacket(data, data.length, ia);
            ds.send(dp);
            ds.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String recvMessageMLT(MulticastSocket mso) { // on suppose qu'on est deja abonné
        try {
            byte[] data = new byte[100];
            DatagramPacket paquet = new DatagramPacket(data, data.length);
            mso.receive(paquet);
            String res = new String(paquet.getData(), 0, paquet.getLength());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "erreur reception message";
    }

    public String recvMessageNRM(DatagramSocket ds) {
        try {
            byte[] data = new byte[100];
            DatagramPacket paquet = new DatagramPacket(data, data.length);
            ds.receive(paquet);
            String res = new String(paquet.getData(), 0, paquet.getLength());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "erreur reception message";
    }

    public void run() {
    }

}
