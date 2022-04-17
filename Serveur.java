import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;

import java.lang.Thread;

public class Serveur {

    private static ArrayList<Partie> liste = new ArrayList<Partie>();
    private static ArrayList<Partie> partie_prete = new ArrayList<Partie>();

    public static boolean enregistre_joueur(Joueur j, int m) {
        for (Partie p : liste) {
            if (p.id == m) {
                p.liste.add(j);
                j.inscrit = p;
                return true;
            }
        }
        return false;
    }

    public static void liste_partie(PrintWriter pw) {
        pw.println("GAMES " + String.valueOf(partie_prete.size()) + "***");
        for (Partie p : partie_prete) {
            pw.println("OGAME " + String.valueOf(p.id) + String.valueOf(p.liste.size()) + "***");
        }
        pw.flush();
    }

    public static void liste_joueur(PrintWriter pw, String message) {
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

    public static void taille_labyrinthe(PrintWriter pw, String message) {
        String[] instruction = message.split(" ");
        if (instruction.length != 2) {
            pw.println("DUNNO***");
            pw.flush();
        } else {
            boolean a_ecrit = false;
            for (Partie p : liste) {
                if (p.id == Integer.valueOf(instruction[1])) { // il faut enlever les *** de instruction[1]
                    pw.println("SIZE! " + instruction[1] + " " + p.labyrinthe.haut + " " + p.labyrinthe.larg + "***");
                    a_ecrit = true;
                }
            }
            if (!a_ecrit) {
                pw.println("DUNNO***");
            }
            pw.flush();
        }
    }

    public static void desinscription(PrintWriter pw, Joueur j) {
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

    public static void main(String[] args) {
        try {
            ServerSocket serv = new ServerSocket(9999);
            MulticastSocket mso = new MulticastSocket(12500);
            while (true) {
                Socket sock = serv.accept();
                BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));

                ecrit.println("bonjour");
                ecrit.flush();

                Joueur moi = new Joueur();
                Thread t_joueur;

                liste_partie(ecrit);

                boolean joueur_pret = false;
                while (!joueur_pret) {
                    String mess = lire.readLine();
                    if (mess.contains("NEWPL")) {
                        String[] instruction = mess.split(" ");
                        if (instruction.length != 3) {
                            ecrit.println("REGNO***");
                            ecrit.flush();
                        } else {
                            Partie pnew = new Partie();
                            // Thread tpart = new Thread(pnew,pnew.id); pas sur que soit necessaire de créer
                            // un thread pour les partie
                            liste.add(pnew);
                            moi = new Joueur(instruction[1], Integer.valueOf(instruction[2]));
                            t_joueur = new Thread(moi, String.valueOf(moi.id));
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

                    else if (mess.equals("GAMES***")) {
                        liste_partie(ecrit);
                    }

                    else if (mess.contains("SIZE?")) {
                        taille_labyrinthe(ecrit, mess);
                    }

                    else if (mess.equals("UNREG***")) {
                        desinscription(ecrit, moi);
                    }

                    else if (mess.equals("START***")) {
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
                sock.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}