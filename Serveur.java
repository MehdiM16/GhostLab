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
            String[] valeur = recup_valeur(br);
            print_tab(valeur);
            if (valeur.length == 1) {
                int num_partie;
                try {
                    num_partie = Integer.valueOf(valeur[0]);
                } catch (Exception e) {
                    num_partie = -1;
                }
                if (num_partie == -1) {
                    pw.print("DUNNO***");
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
            } else {
                pw.print("DUNNO***");
                pw.flush();
            }
        }

        public void liste_joueur_partie(BufferedReader br, PrintWriter pw, Joueur j) {
            try {
                String fin_mess = lire_message_total(br);
                System.out.println(fin_mess);
                if (fin_mess.equals("")) {
                    if (j.inscrit != null && j.inscrit.start) {
                        pw.print("GLIS! " + j.inscrit.nombre_inscrit() + "***");
                        for (Joueur tmp : j.inscrit.getList()) {
                            pw.print("GPLYR " + tmp.pseudo + " " + tmp.positionX + " " + tmp.positionY + " " + tmp.point
                                    + "***");
                        }
                        pw.flush();
                    }
                } else {
                    pw.print("DUNNO***");
                    pw.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void taille_labyrinthe(PrintWriter pw, BufferedReader br) {
            String[] valeur = recup_valeur(br);
            print_tab(valeur);
            if (valeur.length == 1) {
                int num_partie;
                try {
                    num_partie = Integer.valueOf(valeur[0]);
                } catch (Exception e) {
                    num_partie = -1;
                }
                if (num_partie == -1) {
                    pw.print("DUNNO***");
                    pw.flush();
                } else {
                    boolean a_ecrit = false;
                    for (Partie p : liste) {
                        int larg = p.labyrinthe.littleEndianToInt(p.labyrinthe.larg);
                        int haut = p.labyrinthe.littleEndianToInt(p.labyrinthe.haut);
                        if (p.id == num_partie) {
                            pw.print("SIZE! " + num_partie + " " + larg + " " + haut + "***");
                            a_ecrit = true;
                        }
                    }
                    if (!a_ecrit) {
                        pw.print("DUNNO***");
                    }
                    pw.flush();
                }
            } else {
                pw.print("DUNNO***");
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

        public void quitte_partie(PrintWriter pw, BufferedReader br, Joueur j) {
            try {
                String fin_mess = lire_message_total(br);
                System.out.println(fin_mess);
                if (fin_mess.equals("")) {
                    remove_joueur(j);
                    pw.print("GOBYE***");
                } else {
                    pw.print("DUNNO***");
                }
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String lire_message_total(BufferedReader br) { // lit un message sans tenir compte de la presence
                                                              // d'espace, s'arrete quand la fonction lit ***
            String res = "";
            String fin = "";
            boolean prec_etoile = false;
            try {
                char lu = (char) br.read();
                while (lu == ' ') {
                    lu = (char) br.read(); // on lit tout les espaces qu'il pourrais y avoir avant de lire ce qu'on veut
                                           // lire
                }
                while (!fin.equals("***")) {
                    if (lu == '*') {
                        fin += lu;
                        prec_etoile = true;
                    } else {
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

        public String lire_pseudo(BufferedReader br) { // on lit un string correspondant a un seul argument d'un message
            String res = "";
            String fin = "";
            boolean prec_etoile = false;
            try {
                char lu = (char) br.read();
                while (lu == ' ') {
                    lu = (char) br.read(); // on lit tout les espaces qu'il pourrais y avoir avant de lire ce qu'on veut
                                           // lire
                }
                while (!fin.equals("***") && lu != ' ') {
                    if (lu == '*') {
                        fin += lu;
                        prec_etoile = true;
                    } else {
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

        public String[] recup_valeur(BufferedReader br) { // lit un message sans tenir compte de la presence
            // d'espace, s'arrete quand la fonction lit ***
            String res = lire_message_total(br);
            return res.split(" ");
        }

        public void print_tab(String[] tab) {
            for (int i = 0; i < tab.length; i++) {
                System.out.print(tab[i] + " ");
            }
            System.out.println("");
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
                    System.out.print(mess + " ");
                    if (mess.equals("NEWPL") && moi.inscrit == null) {
                        String[] valeur = recup_valeur(lire);
                        print_tab(valeur);
                        if (valeur.length == 2) {
                            String pseudo = valeur[0];
                            int joueur_port;
                            try {
                                joueur_port = Integer.valueOf(valeur[1]);
                            } catch (Exception e) {
                                joueur_port = -1;
                            }
                            if (!pseudo.equals("") && joueur_port > 0) {
                                moi = new Joueur(pseudo, joueur_port, socket.getInetAddress().getHostName());

                                Partie pnew = new Partie();
                                Thread tpart = new Thread(pnew);
                                tpart.start();
                                pnew.partThread = tpart;
                                enregistre_partie(liste, pnew);
                                enregistre_partie(partie_en_attente, pnew);
                                boolean enregistre = enregistre_joueur(moi, pnew.id);
                                if (enregistre) {
                                    ecrit.print("REGOK " + pnew.id + "***");
                                    ecrit.flush();
                                } else {
                                    ecrit.print("REGNO***");
                                    ecrit.flush();
                                }
                            } else {
                                ecrit.print("REGNO***");
                                ecrit.flush();
                            }
                        } else {
                            ecrit.print("REGNO***");
                            ecrit.flush();
                        }
                    }

                    else if (mess.equals("REGIS") && moi.inscrit == null) {
                        String[] valeur = recup_valeur(lire);
                        print_tab(valeur);
                        if (valeur.length == 3) {
                            String pseudo = valeur[0];
                            int joueur_port;
                            int num_partie;
                            try {
                                joueur_port = Integer.valueOf(valeur[1]);
                                num_partie = Integer.valueOf(valeur[2]);
                            } catch (Exception e) {
                                joueur_port = -1;
                                num_partie = -1;
                            }
                            if (!pseudo.equals("") && joueur_port > 0 && num_partie >= 0) {
                                moi = new Joueur(pseudo, joueur_port, socket.getInetAddress().getHostName());
                                boolean enregistre = enregistre_joueur(moi, num_partie);
                                if (enregistre) {
                                    ecrit.print("REGOK " + num_partie + "***");
                                    ecrit.flush();
                                } else {
                                    ecrit.print("REGNO***");
                                    ecrit.flush();
                                }
                            } else {
                                ecrit.print("REGNO***");
                                ecrit.flush();
                            }
                        } else {
                            ecrit.print("REGNO***");
                            ecrit.flush();
                        }
                    }

                    else if (mess.equals("LIST?")) {
                        liste_joueur(ecrit, lire);
                    }

                    else if (mess.equals("GAME?")) {
                        String fin_mess = lire_message_total(lire);
                        System.out.println(fin_mess);
                        if (!fin_mess.equals("")) {
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
                        String fin_mess = lire_message_total(lire);
                        if (moi.inscrit == null || !fin_mess.equals("")) {
                            joueur_pret = false;
                        } else {
                            joueur_pret = true;
                            moi.pret = true;
                        }
                    }

                    else {
                        String inconnu = lire_message_total(lire);
                        System.out.println(inconnu);
                        ecrit.print("DUNNO***");
                        ecrit.flush();
                    }
                }

                boolean partie_en_cours = false;

                while (!moi.inscrit.peut_commencer()) {
                    partie_en_cours = false;
                    Thread.sleep(1000);
                }

                if (moi.inscrit.peut_commencer()) {
                    partie_en_cours = true;
                    remove_partie(partie_en_attente, moi.inscrit);
                    // on supprime la partie qui viens de commencer de la liste des partie en
                    // attente de commencer
                    int larg = moi.inscrit.labyrinthe.littleEndianToInt(moi.inscrit.labyrinthe.larg);
                    int haut = moi.inscrit.labyrinthe.littleEndianToInt(moi.inscrit.labyrinthe.haut);
                    String addr_tmp = new String(moi.inscrit.address_diffusion);
                    ecrit.print("WELCO " + moi.inscrit.id + " " + haut + " "
                            + larg + " " + moi.inscrit.labyrinthe.nombre_fantome
                            + " " + addr_tmp + " " + moi.inscrit.port_diffusion + "***");
                    ecrit.flush();
                }

                String pos = moi.inscrit.labyrinthe.positionAleatoire();
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
                    System.out.print(mess + " ");
                    if (!moi.inscrit.partie_finis()) {
                        if (mess.equals("UPMOV") || mess.equals("LEMOV") || mess.equals("RIMOV")
                                || mess.equals("DOMOV")) {
                            String[] valeur = recup_valeur(lire);
                            print_tab(valeur);
                            if (valeur.length == 1) {
                                int rencontre = moi.inscrit.joueTour(moi, mess, valeur[0]);
                                if (rencontre == 0) {
                                    ecrit.print("MOVE! " + moi.positionX + " " + moi.positionY + "***");
                                } else if (rencontre > 0) {
                                    ecrit.print(
                                            "MOVEF " + moi.positionX + " " + moi.positionY + " " + moi.point + "***");
                                } else {
                                    ecrit.print("DUNNO***");
                                }
                            } else {
                                ecrit.print("DUNNO***");
                            }
                            ecrit.flush();
                        }

                        else if (mess.equals("GLIS?")) {
                            liste_joueur_partie(lire, ecrit, moi);
                        }

                        else if (mess.equals("MALL?")) {
                            String envoie_message = lire_message_total(lire);
                            System.out.println(envoie_message);
                            ecrit.print("MALL!***");
                            ecrit.flush();
                            moi.inscrit.multidiffuse_message(moi, envoie_message);
                        }

                        else if (mess.equals("SEND?")) {
                            String destinataire = lire_pseudo(lire);
                            String envoie_message = lire_message_total(lire);
                            System.out.println(destinataire + " " + envoie_message);
                            boolean message_envoyer = moi.inscrit.envoie_message_joueur(moi, destinataire,
                                    envoie_message);
                            if (message_envoyer) {
                                ecrit.print("SEND!***");
                            } else {
                                ecrit.print("NSEND***");
                            }
                            ecrit.flush();
                        }

                        else if (mess.equals("IQUIT")) {
                            quitte_partie(ecrit, lire, moi);
                            partie_en_cours = false;
                        }

                        else {
                            String inconnu = lire_message_total(lire);
                            System.out.println(inconnu);
                            ecrit.print("DUNNO***");
                            ecrit.flush();
                        }

                    } else {
                        partie_en_cours = false;
                        ecrit.print("GOBYE***");
                        ecrit.flush();
                        remove_partie(liste, moi.inscrit);
                        // on supprime la partie qui viens de finir de la liste de toutes les partie
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
            ServerSocket serv = new ServerSocket(9123);
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
