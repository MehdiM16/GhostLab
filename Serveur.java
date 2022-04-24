import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.lang.Runnable;
import java.lang.Thread;

public class Serveur {

    static class Connexion implements Runnable {

        Socket socket;
        ArrayList<Partie> liste = new ArrayList<Partie>();
        ArrayList<Partie> partie_prete = new ArrayList<Partie>();

        public Connexion(Socket s) {
            socket = s;
        }

        public boolean enregistre_joueur(Joueur j, int m) {
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
            pw.println("GAMES " + String.valueOf(partie_prete.size()) + "***");
            pw.println("bonjour frerot");
            for (Partie p : partie_prete) {
                pw.println("OGAME " + String.valueOf(p.id) + String.valueOf(p.liste.size()) + "***");
            }
            pw.flush();
        }

        public void liste_joueur(PrintWriter pw, String message) {
            String[] instruction = message.split(" ");
            if (instruction.length != 2) {
                pw.println("REGNO***");
                pw.flush();
            } else {
                boolean a_ecrit = false;
                for (Partie p : liste) {
                    if (Integer.valueOf(instruction[1]) == p.id) {
                        pw.println("LIST! " + instruction[1] + " " + String.valueOf(p.liste.size()) + "***");
                        for (Joueur j : p.liste) {
                            pw.println("PLAYR " + j.id + "***");
                        }
                        a_ecrit = true;
                    }
                }
                if (!a_ecrit) {
                    pw.println("DUNNO***");
                }
                pw.flush();
            }
        }

        public void taille_labyrinthe(PrintWriter pw, String message) {
            String[] instruction = message.split(" ");
            if (instruction.length != 2) {
                pw.println("DUNNO***");
                pw.flush();
            } else {
                boolean a_ecrit = false;
                for (Partie p : liste) {
                    if (p.id == Integer.valueOf(instruction[1])) { // il faut enlever les *** de instruction[1]
                        pw.println(
                                "SIZE! " + instruction[1] + " " + p.labyrinthe.haut + " " + p.labyrinthe.larg + "***");
                        a_ecrit = true;
                    }
                }
                if (!a_ecrit) {
                    pw.println("DUNNO***");
                }
                pw.flush();
            }
        }

        public void desinscription(PrintWriter pw, Joueur j) {
            if (j.inscrit == null) {
                pw.println("DUNNO***");
                pw.flush();
            } else {
                Partie tmp = j.inscrit;
                int partie_id = tmp.id;
                tmp.liste.remove(j);
                j.inscrit = null;
                pw.println("UNROK " + partie_id + "***");
                pw.flush();
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
                    String mess = lire.readLine();
                    System.out.println(mess);
                    mess = mess.substring(0, mess.length() - 3);// on enleve les *** du message pour pourvoir le
                                                                // manipuler plus facilement
                    if (mess.contains("NEWPL")) {
                        String[] instruction = mess.split(" ");
                        if (instruction.length != 3) {
                            ecrit.println("REGNO***");
                            ecrit.flush();
                        } else {
                            Partie pnew = new Partie();
                            System.out.println("la partie d'id " + pnew.id + " viens d'etre cree");
                            // Thread tpart = new Thread(pnew,pnew.id); pas sur que soit necessaire de créer
                            // un thread pour les partie
                            liste.add(pnew);
                            moi = new Joueur(instruction[1], Integer.valueOf(instruction[2]));
                            t_joueur = new Thread(moi, String.valueOf(moi.id));
                            t_joueur.start();
                            moi.joueurThread = t_joueur;
                            pnew.liste.add(moi);
                            moi.inscrit = pnew;
                            ecrit.println("REGOK " + pnew.id + "***");
                            ecrit.flush();
                        }
                    }

                    else if (mess.contains("REGIS")) {
                        String[] instruction = mess.split(" ");
                        if (instruction.length != 4) {
                            ecrit.println("REGNO***");
                            ecrit.flush();
                        }
                        moi = new Joueur(instruction[1], Integer.valueOf(instruction[2]));
                        t_joueur = new Thread(moi, String.valueOf(moi.id));
                        t_joueur.start();
                        moi.joueurThread = t_joueur;
                        boolean enregistre = enregistre_joueur(moi, Integer.valueOf(instruction[3]));
                        if (enregistre) {
                            ecrit.println("REGOK " + instruction[3] + "***");
                            ecrit.flush();
                        } else {
                            ecrit.println("REGNO***");
                            ecrit.flush();
                        }
                    }

                    else if (mess.contains("LIST?")) {
                        liste_joueur(ecrit, mess);
                    }

                    else if (mess.contains("GAMES")) {
                        liste_partie(ecrit);
                    }

                    else if (mess.contains("SIZE?")) {
                        taille_labyrinthe(ecrit, mess);
                    }

                    else if (mess.contains("UNREG")) {
                        desinscription(ecrit, moi);
                    }

                    else if (mess.contains("START")) {
                        if (moi.inscrit == null) {
                            ecrit.println("DUNNO***");
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