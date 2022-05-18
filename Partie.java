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
        try {
            dgsock = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public synchronized boolean partie_finis() {
        for (Fantome f : labyrinthe.liste) {
            if (!f.attraper) {
                return false;
            }
        }
        return true;
    }

    public synchronized Joueur getJoueur(String nom) {
        for (Joueur tmp : liste) {
            if (tmp.pseudo.equals(nom)) {
                return tmp;
            }
        }
        return null;
    }

    public synchronized boolean peut_commencer() {
        for (Joueur j : liste) {
            if (!j.pret)
                return false;
        }
        return true;
    }

    public synchronized int joueTour(Joueur j, String dir, String pas_s) {
        int pas = Integer.valueOf(pas_s);
        int posX = Integer.valueOf(j.positionX);
        int posY = Integer.valueOf(j.positionY);
        int fantome_rencontre = 0;
        if (dir.equals("UPMOV")) {
            // posX -= pas; // UPMOV
            while (posX > 0 && labyrinthe.getCase(posX - 1, posY) != '|' && pas > 0) {
                posX--;
                pas--;
                if (labyrinthe.getCase(posX, posY) == 'F') {
                    fantome_rencontre++;
                    labyrinthe.setCase(posX, posY, 'V');
                    labyrinthe.remove_fantome(j, labyrinthe.posIntToString(posX), labyrinthe.posIntToString(posY));
                    joueur_prend_fantome(j);
                }
            }
            j.positionX = j.posIntToString(posX);
        } else if (dir.equals("RIMOV")) {
            // posY += pas; // RIMOV
            while (posY < labyrinthe.littleEndianToInt(labyrinthe.larg) - 1 && labyrinthe.getCase(posX, posY + 1) != '|'
                    && pas > 0) {
                posY++;
                pas--;
                if (labyrinthe.getCase(posX, posY) == 'F') {
                    fantome_rencontre++;
                    labyrinthe.setCase(posX, posY, 'V');
                    labyrinthe.remove_fantome(j, labyrinthe.posIntToString(posX), labyrinthe.posIntToString(posY));
                    joueur_prend_fantome(j);
                }
            }
            j.positionY = j.posIntToString(posY);
        } else if (dir.equals("DOMOV")) {
            // posX += pas; // DOMOV
            while (posX < labyrinthe.littleEndianToInt(labyrinthe.haut) - 1 && labyrinthe.getCase(posX + 1, posY) != '|'
                    && pas > 0) {
                posX++;
                pas--;
                if (labyrinthe.getCase(posX, posY) == 'F') {
                    fantome_rencontre++;
                    labyrinthe.setCase(posX, posY, 'V');
                    labyrinthe.remove_fantome(j, labyrinthe.posIntToString(posX), labyrinthe.posIntToString(posY));
                    joueur_prend_fantome(j);
                }
            }
            j.positionX = j.posIntToString(posX);
        } else if (dir.equals("LEMOV")) {
            // posY -= pas; // LEMOV
            while (posY > 0 && labyrinthe.getCase(posX, posY - 1) != '|' && pas > 0) {
                posY--;
                pas--;
                if (labyrinthe.getCase(posX, posY) == 'F') {
                    fantome_rencontre++;
                    labyrinthe.setCase(posX, posY, 'V');
                    labyrinthe.remove_fantome(j, labyrinthe.posIntToString(posX), labyrinthe.posIntToString(posY));
                    joueur_prend_fantome(j);
                }
            }
            j.positionY = j.posIntToString(posY);
        }
        return fantome_rencontre;
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

    public void joueur_prend_fantome(Joueur j) {
        try {
            byte[] data;
            InetSocketAddress dest = new InetSocketAddress(byteArrayToString(address_diffusion),
                    Integer.valueOf(port_diffusion));
            String a_envoyer = "SCORE " + j.pseudo + " " + j.point + " " + j.positionX + " " + j.positionY + "+++";
            data = a_envoyer.getBytes();
            DatagramPacket message = new DatagramPacket(data, data.length, dest);
            dgsock.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Joueur gagnant() {
        Joueur res = new Joueur();
        int score = Integer.valueOf(res.point);
        for (Joueur j : liste) {
            if (Integer.valueOf(j.point) > score) {
                res = j;
                score = Integer.valueOf(res.point);
            }
        }
        return res;
    }

    public void multidiffuse_message(Joueur j, String message) {
        try {
            byte[] data;
            InetSocketAddress dest = new InetSocketAddress(byteArrayToString(address_diffusion),
                    Integer.valueOf(port_diffusion));
            String a_envoyer = "MESSA " + j.pseudo + " " + message + "+++";
            data = a_envoyer.getBytes();
            DatagramPacket paquet = new DatagramPacket(data, data.length, dest);
            dgsock.send(paquet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean envoie_message_joueur(Joueur envoyeur, String destinataire, String message) {
        boolean res = false;
        try {
            Joueur objectif = getJoueur(destinataire);
            if (objectif == null) {
                res = false;
            } else {
                DatagramSocket dso = new DatagramSocket();
                byte[] data;
                String a_envoyer = "MESSP " + envoyeur.pseudo + " " + message + "+++";
                data = a_envoyer.getBytes();
                InetSocketAddress ia = new InetSocketAddress(objectif.nom_machine, objectif.port_udp);
                DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
                dso.send(paquet);
                res = true;
                dso.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
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

            for (Fantome f : labyrinthe.liste) {
                Thread t = new Thread(f);
                t.start();
            }

            while (start && !partie_finis()) {
            }

            System.out.println("la partie est terminé");
            Joueur a_gagner = gagnant();
            byte[] data;
            InetSocketAddress dest = new InetSocketAddress(byteArrayToString(address_diffusion),
                    Integer.valueOf(port_diffusion));
            String a_envoyer = "ENDGA " + a_gagner.pseudo + " " + a_gagner.point + "+++";
            data = a_envoyer.getBytes();
            DatagramPacket message = new DatagramPacket(data, data.length, dest);
            dgsock.send(message);

            // !!!! Voir en cas de 2 joueur qui ont le meme score

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
