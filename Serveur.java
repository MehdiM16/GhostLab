import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.lang.Runnable;
import java.lang.Thread;

public class Serveur {

    static class Connexion implements Runnable {

        Socket socket;
        static ArrayList<Partie> liste = new ArrayList<Partie>();
        static ArrayList<Partie> partie_en_attente = new ArrayList<Partie>();

        public Connexion(Socket s) {
            socket = s;
        }

        public synchronized void enregistre_partie(ArrayList<Partie> part, Partie p) {
            part.add(p);
        }

        public synchronized void remove_partie(ArrayList<Partie> part, Partie p) {
            part.remove(p);
        }

        public synchronized boolean enregistre_joueur(Joueur j, int m) { // enregistre un joueur dans une partie
            for (Partie p : liste) {
                if (p.id == m) {
                    boolean ajout = p.enregistre_joueur(j);
                    return ajout;
                }
            }
            return false;
        }

        public synchronized int remove_joueur(Joueur j) {
            if (j.inscrit == null) {
                return -1;
            } else {
                Partie tmp = j.inscrit;
                int partie_id = tmp.remove_joueur(j);
                return partie_id;
            }
        }

        public void liste_partie(PrintWriter pw) {
            pw.print("GAMES " + String.valueOf(partie_en_attente.size()) + "***");
            for (Partie p : partie_en_attente) {
                pw.print("OGAME " + p.id + " " + p.nombre_inscrit() + "***");
            }
            pw.flush();
        }

        public void liste_joueur(PrintWriter pw, BufferedReader br) {
            int num_partie = lire_nombre_fin(br);
            if (num_partie == -1) {
                pw.print("REGNO***");
                pw.flush();
            } else {
                boolean a_ecrit = false;
                for (Partie p : liste) {
                    if (num_partie == p.id) {
                        pw.print("LIST! " + num_partie + " " + p.nombre_inscrit() + "***");
                        for (Joueur j : p.getList()) {
                            pw.print("PLAYR " + j.pseudo + "***");
                        }
                        a_ecrit = true;
                    }
                }
                if (!a_ecrit) {
                    pw.print("DUNNO***");
                }
                pw.flush();
            }
        }

        public void taille_labyrinthe(PrintWriter pw, BufferedReader br) {
            int num_partie = lire_nombre_fin(br);
            if (num_partie == -1) {
                pw.print("DUNNO***");
                pw.flush();
            } else {
                boolean a_ecrit = false;
                for (Partie p : liste) {
                    int larg = p.labyrinthe.littleEndianToInt(p.labyrinthe.larg);
                    int haut = p.labyrinthe.littleEndianToInt(p.labyrinthe.haut);
                    if (p.id == num_partie) { // il faut enlever les *** de instruction[1]
                        pw.print("SIZE! " + num_partie + " " + larg + " " + haut + "***");
                        a_ecrit = true;
                    }
                }
                if (!a_ecrit) {
                    pw.print("DUNNO***");
                }
                pw.flush();
            }
        }

        public void desinscription(PrintWriter pw, BufferedReader br, Joueur j) {
            try {
                char[] fin_mess = new char[3];
                br.read(fin_mess, 0, 3);
                int j_supp = remove_joueur(j);
                if (j_supp == -1 || !(String.valueOf(fin_mess).equals("***"))) {
                    pw.print("DUNNO***");
                    pw.flush();
                } else {
                    pw.print("UNROK " + j_supp + "***");
                    pw.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String lire_pseudo_milieu(BufferedReader br) { // on lit un string qui se situe au milieu d'un message
            String res = "";
            try {
                // br.read(); // on lit l'espace
                char lu = (char) br.read();
                while (lu != ' ' || res.length() == 0) {
                    if (lu != ' ') { // pour eviter de lire le premier espace
                        res += lu;
                    }
                    lu = (char) br.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        public String lire_pseudo_fin(BufferedReader br) { // on lit un string qui se situe au milieu d'un
                                                           // message
            String res = "";
            String fin = "";
            boolean prec_etoile = false;
            try {
                // br.read(); // on lit l'espace
                char lu = (char) br.read();
                while (!fin.equals("***")) {
                    if (lu == '*') {
                        fin += lu;
                        prec_etoile = true;
                    } else if (lu != ' ') {
                        if (prec_etoile) { // on arrive dans ce cas si on lit seulement 1 ou 2 etoiles au milieu d'un
                                           // string
                            res += fin;
                            fin = "";
                        }
                        res += lu;
                        prec_etoile = false;
                    }
                    if (!fin.equals("***")) {
                        lu = (char) br.read();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        public int lire_nombre_fin(BufferedReader br) {
            String fin = "";
            String recu = "";
            try {
                char lu = (char) br.read();
                while (lu != '*') {
                    if (Character.isDigit(lu)) {// on verifie si le caractere lu est bien un chiffre
                        recu += lu;
                    } else {
                        if (lu != ' ') {
                            return -1;// si on lit autre chose qu'un chiffre ou une etoile on renvoie -1 pour
                                      // signifier une erreur
                        }
                    }
                    lu = (char) br.read();
                }
                fin += lu;
                fin += (char) br.read();
                fin += (char) br.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (fin.equals("***") && recu.length() > 0) {// si on recoit bien les etoile pour signifié la fin du
                                                         // message on retourne la valeur
                return Integer.valueOf(recu);
            } else { // sinon cela signifie que le message est erroné donc on retourne -1
                return -1;
            }
        }

        public int lire_nombre_milieu(BufferedReader br) {
            String recu = "";
            try {
                char lu = (char) br.read();
                boolean premier_lu = true;
                while (lu != ' ' || premier_lu) {
                    if (Character.isDigit(lu)) {// on verifie si le caractere lu est bien un chiffre
                        recu += lu;
                    } else {
                        if (!premier_lu) {
                            return -1;// si on lit autre chose qu'un chiffre ou une etoile on renvoie -1 pour
                                      // signifier une erreur
                        }
                    }
                    lu = (char) br.read();
                    premier_lu = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (recu.length() > 0) {
                return Integer.valueOf(recu);
            } else {
                return -1;
            }
        }

        public void run() {
            try {
                BufferedReader lire = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                Joueur moi = new Joueur();
                // Thread t_joueur;

                liste_partie(ecrit);

                boolean joueur_pret = false;

                while (!joueur_pret) {
                    char[] mess_type = new char[5];
                    lire.read(mess_type, 0, 5);
                    String mess = String.valueOf(mess_type);
                    System.out.println(mess);
                    if (mess.equals("NEWPL")) {
                        String pseudo = lire_pseudo_milieu(lire);
                        int joueur_port = lire_nombre_fin(lire);
                        System.out.println(pseudo + " " + joueur_port);
                        if (joueur_port != -1) {
                            moi = new Joueur(pseudo, joueur_port);
                            // t_joueur = new Thread(moi, String.valueOf(moi.id));
                            // t_joueur.start();
                            // moi.joueurThread = t_joueur;
                            Partie pnew = new Partie();
                            System.out.println("la partie d'id " + pnew.id + " viens d'etre cree"); // TEST
                            Thread tpart = new Thread(pnew, String.valueOf(pnew.id)); // pas sur que soit necessaire de
                                                                                      // créer
                            tpart.start();
                            pnew.partThread = tpart;
                            // un thread pour les partie
                            // liste.add(pnew);
                            enregistre_partie(liste, pnew);
                            // partie_en_attente.add(pnew);
                            enregistre_partie(partie_en_attente, pnew);
                            boolean enregistre = enregistre_joueur(moi, pnew.id);
                            // moi.inscrit = pnew;
                            if (enregistre) {
                                ecrit.print("REGOK " + pnew.id + "***");
                                ecrit.flush();
                            } else {
                                ecrit.print("REGNO***");
                                ecrit.flush();
                            }
                            if (moi.inscrit == null) {
                                System.out.println("frerot tes pas inscrit");
                            } else {
                                System.out.println("tes inscrit frerot");
                            }
                        } else {
                            ecrit.print("REGNO***");
                            ecrit.flush();
                        }
                    }

                    else if (mess.equals("REGIS")) {
                        String pseudo = lire_pseudo_milieu(lire);
                        int joueur_port = lire_nombre_milieu(lire);
                        int num_partie = lire_nombre_fin(lire);
                        System.out.println(pseudo + " " + joueur_port + " " + num_partie);
                        if (joueur_port != -1 && num_partie != -1) {
                            moi = new Joueur(pseudo, joueur_port);
                            // t_joueur = new Thread(moi, String.valueOf(moi.id));
                            // t_joueur.start();
                            // moi.joueurThread = t_joueur;
                            boolean enregistre = enregistre_joueur(moi, num_partie);
                            if (enregistre) {
                                ecrit.print("REGOK " + num_partie + "***");
                                ecrit.flush();
                            } else {
                                ecrit.print("REGNO***");
                                ecrit.flush();
                            }
                            if (moi.inscrit == null) {
                                System.out.println("frerot tes pas inscrit");
                            } else {
                                System.out.println("tes inscrit frerot");
                            }
                        } else {
                            ecrit.print("REGNO***");
                            ecrit.flush();
                        }
                    }

                    else if (mess.equals("LIST?")) {
                        liste_joueur(ecrit, lire);
                    }

                    else if (mess.equals("GAMES")) {
                        char[] fin_mess = new char[3];
                        lire.read(fin_mess, 0, 3);
                        if (!(String.valueOf(fin_mess).equals("***"))) {
                            ecrit.print("DUNNO***");
                            ecrit.flush();
                        } else {
                            liste_partie(ecrit);
                        }
                    }

                    else if (mess.equals("SIZE?")) {
                        taille_labyrinthe(ecrit, lire);
                    }

                    else if (mess.equals("UNREG")) {
                        desinscription(ecrit, lire, moi);
                    }

                    else if (mess.equals("START")) {
                        char[] fin_mess = new char[3];
                        lire.read(fin_mess, 0, 3);
                        if (moi.inscrit == null || !(String.valueOf(fin_mess).equals("***"))) {
                            // ecrit.print("DUNNO***");
                            // ecrit.flush();
                            joueur_pret = false;
                        } else {
                            // ecrit.print("ATTEN***");
                            // ecrit.flush();
                            joueur_pret = true;
                            moi.pret = true;
                        }
                    }
                }

                System.out.println("nous sommes sortie du while ahah");
                boolean partie_en_cours = false;

                while (!moi.inscrit.peut_commencer()) {
                    partie_en_cours = false;
                    Thread.sleep(1000);
                }

                if (moi.inscrit.peut_commencer()) {
                    partie_en_cours = true;
                    int larg = moi.inscrit.labyrinthe.littleEndianToInt(moi.inscrit.labyrinthe.larg);
                    int haut = moi.inscrit.labyrinthe.littleEndianToInt(moi.inscrit.labyrinthe.haut);
                    String addr_tmp = new String(moi.inscrit.address_diffusion);
                    System.out.println("la partie peut commencer");
                    ecrit.print("WELCO " + moi.inscrit.id + " " + haut + " "
                            + larg + " " + moi.inscrit.labyrinthe.nombre_fantome
                            + " " + addr_tmp + " " + moi.inscrit.port_diffusion + "***");
                    ecrit.flush();
                }

                String pos = moi.inscrit.labyrinthe.positionAleatoire();
                System.out.println(pos);
                String pos_x = pos.substring(0, 3);
                String pos_y = pos.substring(3);
                moi.positionX = pos_x;
                moi.positionY = pos_y;
                ecrit.print("POSIT " + moi.pseudo + " " + pos_x + " " + pos_y + "***");
                ecrit.flush();

                while (partie_en_cours) {
                    char[] mess_type = new char[5];
                    lire.read(mess_type, 0, 5);
                    String mess = String.valueOf(mess_type);
                    System.out.println(mess);
                    if (mess.equals("UPMOV") || mess.equals("LEMOV") || mess.equals("RIMOV") || mess.equals("DOMOV")) {
                        String dist = lire_pseudo_fin(lire);
                        int rencontre = moi.inscrit.joueTour(moi, mess, dist);
                        if (rencontre == 0) {
                            ecrit.print("MOVE! " + moi.positionX + " " + moi.positionY + "***");
                        } else {
                            int mon_score = Integer.valueOf(moi.point);
                            int new_score = mon_score + (rencontre * 50);
                            moi.point = String.valueOf(moi.scoreToString(new_score));
                            ecrit.print("MOVEF " + moi.positionX + " " + moi.positionY + " " + moi.point + "***");
                            // rencontre * 50 = score, a modifier en fonction des resultat lors des test ou
                            // non
                        }
                        ecrit.flush();
                    }

                    if (!moi.inscrit.start) {
                        partie_en_cours = false;
                    }
                }

                lire.close();
                ecrit.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serv = new ServerSocket(9999);
            MulticastSocket mso = new MulticastSocket(12500);
            while (true) {
                Socket sock = serv.accept();
                Connexion connex = new Connexion(sock);
                Thread t = new Thread(connex);
                t.start();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}