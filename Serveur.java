import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;

import java.lang.Thread;

public class Serveur {

    private ArrayList<Partie> liste = new ArrayList<Partie>();
    private ArrayList<Partie> partie_prete = new ArrayList<Partie>();

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

    public static void main(String[] args) {
        try {
            ServerSocket serv = new ServerSocket(9999);
            MulticastSocket mso = new MulticastSocket(12500);
            while (true) {
                Socket sock = serv.accept();
                BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));

                ecrit.println("GAMES " + String.valueOf(partie_prete.size()) + "***");
                ecrit.flush();
                for (Partie p : partie_prete) {
                    ecrit.println("OGAME " + String.valueOf(p.id) + String.valueOf(p.liste.size()) + "***");
                    ecrit.flush();
                }
                String mess = lire.readLine();
                if (mess.contains("NEWPL")) {
                    String[] instruction = mess.split(' ');
                    if (instruction.length != 3) {
                        ecrit.println("REGNO***");
                        ecrit.flush();
                    }
                    else {
                        Partie pnew = new Partie();
                        //Thread tpart = new Thread(pnew,pnew.id); pas sur que soit necessaire de créer un thread pour les partie
                        liste.add(pnew);
                        Joueur moi = new Joueur(instruction[1], instruction[2]);
                        Thread t_joueur = new Thread(moi,String.valueOf(moi.id));
                        moi.joueurThread = t_joueur;
                        pnew.liste.add(moi);
                        moi.inscrit = pnew;
                        ecrit.println("REGOK " + pnew.id + "***");
                        ecrit.flush();
                    }
                }
                
                else if (mess.contains("REGIS")) {
                    String[] instruction = mess.split(' ');
                    if (instruction.length != 4) {
                        ecrit.println("REGNO***");
                        ecrit.flush();
                    }
                    Joueur moi = new Joueur(instruction[1], instruction[2]);
                    Thread t_joueur = new Thread(moi,String.valueOf(moi.id));
                    moi.joueurThread = t_joueur;
                    boolean enregistre = enregistre_joueur(moi, instruction[3]);
                    if (enregistre)) {
                        ecrit.println("REGOK " + pnew.id + "***");
                        ecrit.flush();
                    }
                    else {
                        ecrit.println("REGNO***");
                        ecrit.flush();
                    }
                }

                else if(mess.contains("LIST?")) {
                    String[] instruction = mess.split(' ');
                    if(instruction.length != 2) {
                        ecrit.println("REGNO***");
                        ecrit.flush();
                    } else {
                        boolean a_ecrit = false;
                        for(Partie p : liste) {
                            if(Integer.valueOf(instruction[1]) == p.id) {
                                ecrit.println("LIST! " + instruction[1] + " " + String.valueOf(p.liste.size()) + "***");
                                for(Joueur j : p.liste) {
                                    ecrit.println("PLAYR " + j.id + "***");
                                }
                                a_ecrit = true;
                            }
                        }
                        if(!a_ecrit) {
                            ecrit.println("DUNNO***");
                        }
                        ecrit.flush();
                    }
                }

                else if(mess.equals("GAMES***")) {
                    ecrit.println("GAMES " + String.valueOf(partie_prete.size()) + "***");
                    for (Partie p : partie_prete) {
                        ecrit.println("OGAME " + String.valueOf(p.id) + String.valueOf(p.liste.size()) + "***");
                    }
                    ecrit.flush();
                }

                else if(mess.containts("SIZE?")) {
                    String[] instruction = mess.split(' ');
                    if(instruction.length != 2) {
                        ecrit.println("DUNNO***");
                        ecrit.flush();
                    } else {
                        boolean a_ecrit = false;
                        for(Partie p  : liste) {
                            if(p.id == Integer.valueOf(instruction[1])) { // il faut enlever les *** de instruction[1]
                                ecrit.println("SIZE! " + instruction[1] + " " + p.labyrinthe.long + " " + p.labyrinthe.larg + "***");
                                a_ecrit = true;
                            }
                        }
                        if(!a_ecrit) {
                            ecrit.println("DUNNO***");
                        }
                        ecrit.flush();
                    }
                }
 
                else if(mess.equals("UNREG***")) {
                    if(moi.inscrit == null) {
                        ecrit.println("DUNNO***");
                    } else {
                        Partie tmp = moi.inscrit;
                        int partie_id = tmp.id;
                        tmp.remove(moi);
                        moi.inscrit = null;
                        ecrit.println("UNROK " + partie_id + "***");
                        ecrit.flush();
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