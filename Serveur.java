import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.lang.Runnable;
import java.lang.Thread;

public class Serveur {

    static class Connexion implements Runnable {

        Socket socket;
        ArrayList<Partie> liste = new ArrayList<Partie>();
        ArrayList<Partie> partie_en_attente = new ArrayList<Partie>();

        public Connexion(Socket s) {
            socket = s;
        }

        public boolean enregistre_joueur(Joueur j, int m) { // enregistre un joueur dans une partie
            for (Partie p : liste) {
                if (p.id == m) {
                    p.liste.add(j);
                    j.inscrit = p;
                    return true;
                }
            }
            return false;
        }

        public void liste_partie(PrintWriter pw) {
            pw.print("GAMES " + String.valueOf(partie_en_attente.size()) + "***");
            for (Partie p : partie_en_attente) {
                pw.print("OGAME " + p.id + " " + p.liste.size() + "***");
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
                        pw.print("LIST! " + num_partie + " " + p.liste.size() + "***");
                        for (Joueur j : p.liste) {
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
                    if (p.id == num_partie) { // il faut enlever les *** de instruction[1]
                        pw.print(
                                "SIZE! " + num_partie + " " + p.labyrinthe.haut + " " + p.labyrinthe.larg + "***");
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
                if (j.inscrit == null || !(String.valueOf(fin_mess).equals("***"))) {
                    pw.print("DUNNO***");
                    pw.flush();
                } else {
                    Partie tmp = j.inscrit;
                    int partie_id = tmp.id;
                    tmp.liste.remove(j);
                    j.inscrit = null;
                    pw.print("UNROK " + partie_id + "***");
                    pw.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String lire_pseudo(BufferedReader br) {
            String res = "";
            try {
                br.read(); // on lit l'espace
                char lu = (char) br.read();
                while (lu != ' ') {
                    res += lu;
                    lu = (char) br.read();
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
                while (lu != ' ') {
                    if (Character.isDigit(lu)) {// on verifie si le caractere lu est bien un chiffre
                        recu += lu;
                    } else {
                        return -1;// si on lit autre chose qu'un chiffre ou une etoile on renvoie -1 pour
                                  // signifier une erreur
                    }
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
                Thread t_joueur;

                liste_partie(ecrit);

                boolean joueur_pret = false;

                while (!joueur_pret) {
                    char[] mess_type = new char[5];
                    lire.read(mess_type, 0, 5);
                    String mess = String.valueOf(mess_type);
                    System.out.println(mess);
                    if (mess.equals("NEWPL")) {
                        String pseudo = lire_pseudo(lire);
                        int joueur_port = lire_nombre_fin(lire);
                        System.out.println(pseudo + " " + joueur_port);
                        if (joueur_port != -1) {
                            moi = new Joueur(pseudo, joueur_port);
                            t_joueur = new Thread(moi, String.valueOf(moi.id));
                            t_joueur.start();
                            moi.joueurThread = t_joueur;
                            Partie pnew = new Partie();
                            pnew.liste.add(moi);
                            System.out.println("la partie d'id " + pnew.id + " viens d'etre cree"); // TEST
                            // Thread tpart = new Thread(pnew,pnew.id); pas sur que soit necessaire de créer
                            // un thread pour les partie
                            liste.add(pnew);
                            partie_en_attente.add(pnew);
                            moi.inscrit = pnew;

                            ecrit.print("REGOK " + pnew.id + "***");
                            ecrit.flush();
                        } else {
                            ecrit.print("REGNO***");
                            ecrit.flush();
                        }
                    }

                    else if (mess.equals("REGIS")) {
                        String pseudo = lire_pseudo(lire);
                        int joueur_port = lire_nombre_milieu(lire);
                        int num_partie = lire_nombre_fin(lire);
                        if (joueur_port != -1 && num_partie != -1) {
                            moi = new Joueur(pseudo, joueur_port);
                            t_joueur = new Thread(moi, String.valueOf(moi.id));
                            t_joueur.start();
                            moi.joueurThread = t_joueur;
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
                            ecrit.print("DUNNO***");
                            ecrit.flush();
                        } else {
                            joueur_pret = true;
                        }
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