import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    public static void lire_partie(BufferedReader br, String mess) {
        try {
            int nb_partie = lire_nombre_fin(br);
            System.out.println(mess + " " + nb_partie);
            for (int i = 0; i < nb_partie; i++) {
                try {
                    char[] partie = new char[5];
                    br.read(partie, 0, 5); // on lit 6 caractere pour lire l'espace avec le message
                    // br.read();
                    String part = String.valueOf(partie);// OGAME
                    int num_partie = lire_nombre_milieu(br);
                    int nb_inscrit = lire_nombre_fin(br);
                    System.out.println(part + " " + num_partie + " " + nb_inscrit);
                } catch (Exception e) {
                    System.out.println("erreur lecture partie");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void lire_joueur(BufferedReader br) {
        try {
            char[] jou = new char[5];
            br.read(jou, 0, 5);
            String rep = String.valueOf(jou);
            if (rep.equals("REGNO") || rep.equals("DUNNO")) {
                System.out.println(rep);
                br.read();
                br.read();
                br.read(); // on lit les *** pour lire entierement le message
            }
            int num_partie = lire_nombre_milieu(br);
            int nb_joueur = lire_nombre_fin(br);
            System.out.println(rep + " " + num_partie + " " + nb_joueur);
            for (int i = 0; i < nb_joueur; i++) {
                char[] joueur_i = new char[5];
                br.read(joueur_i, 0, 5);
                String joueur_s = String.valueOf(joueur_i);
                // br.read();// on lit l'espace
                String pseudo = lire_pseudo(br);
                System.out.println(joueur_s + " " + pseudo);
                br.read();
                br.read();
                br.read(); // on lit les *** pour lire entierement le message
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void lire_taille_partie(BufferedReader br) {
        try {
            char[] mess = new char[5];
            br.read(mess, 0, 5);
            String message = String.valueOf(mess);
            if (message.equals("DUNNO")) {
                System.out.println(message);
                br.read();
                br.read();
                br.read();// on lit les *** pour lire le message entierement
            } else {
                int num_partie = lire_nombre_milieu(br);
                int dim_h = lire_nombre_milieu(br);
                int dim_l = lire_nombre_fin(br);
                System.out.println(message + " " + num_partie + " " + dim_h + " " + dim_l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int lire_nombre_fin(BufferedReader br) {
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
            System.out.println(fin);
            return -1;
        }
    }

    public static int lire_nombre_milieu(BufferedReader br) {
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

    public static String lire_pseudo(BufferedReader br) { // on lit un string qui se situe au milieu d'un message
        String res = "";
        try {
            // br.read(); // on lit l'espace
            char lu = (char) br.read();
            while (lu != ' ' || res.length() == 0) {
                res += lu;
                lu = (char) br.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void main(String[] args) {
        try {
            Socket sock = new Socket("localhost", 9999); // ADAPTER POUR LULU
            BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            Scanner sc = new Scanner(System.in);

            char[] type_mess = new char[5];
            lire.read(type_mess, 0, 5);
            String mess_recu = String.valueOf(type_mess); // correspond au type de message envoyer par le serveur
            lire_partie(lire, mess_recu);
            String mess = ""; // correspond au message que le client enverra au serveur
            boolean est_inscrit = false;
            while (!mess.equals("START")) { // PROTOCOLE TCP
                System.out.println("vous pouvez entrez un message");
                mess = sc.nextLine();
                ecrit.print(mess + "***");
                ecrit.flush();
                System.out.println(mess);
                if (mess.contains("LIST?")) {
                    lire_joueur(lire);
                }

                else if (mess.contains("UNREG")) {
                    lire.read(type_mess, 0, 5);
                    mess_recu = String.valueOf(type_mess);
                    if (mess_recu.equals("DUNNO")) {
                        System.out.println(mess_recu);
                        lire.read();
                        lire.read();
                        lire.read(); // on lit les *** pour lire entierement le message
                    } else {
                        int nb_partie = lire_nombre_fin(lire);
                        System.out.println(mess_recu + " " + nb_partie);
                    }
                }

                else if (mess.contains("SIZE")) {
                    lire_taille_partie(lire);
                }

                else if (mess.contains("GAMES")) {
                    lire.read(type_mess, 0, 5);
                    mess_recu = String.valueOf(type_mess);
                    lire_partie(lire, mess_recu);
                }

                else if (mess.contains("REGIS") || mess.contains("NEWPL")) {
                    lire.read(type_mess, 0, 5);
                    mess_recu = String.valueOf(type_mess);
                    if (mess_recu.equals("REGNO")) {
                        System.out.println(mess_recu);
                        lire.read();
                        lire.read();
                        lire.read(); // on lit les *** pour lire entierement le message
                    } else {
                        int num_partie = lire_nombre_fin(lire);
                        System.out.println(mess_recu + " " + num_partie);
                        est_inscrit = true;
                    }
                }

                else if (mess.contains("START")) {
                    /*
                     * lire.read(type_mess, 0, 5);
                     * mess_recu = String.valueOf(type_mess);
                     * System.out.println(mess_recu);
                     * lire.read();
                     * lire.read();
                     * lire.read();// on lit les *** pour lire entierement le message
                     */

                }
            }

            if (est_inscrit) {
                System.out.println("je vais lire");
                lire.read(type_mess, 0, 5);
                mess_recu = String.valueOf(type_mess);
                System.out.println("j'ai lu");
            }

            MulticastSocket sock_multi;

            boolean partie_en_cours = false;

            if (mess_recu.equals("WELCO")) {
                int num_partie = lire_nombre_milieu(lire);
                int hauteur = lire_nombre_milieu(lire);
                int largeur = lire_nombre_milieu(lire);
                int nb_fantome = lire_nombre_milieu(lire);
                String ip_partie = lire_pseudo(lire);
                int port_dif = lire_nombre_fin(lire);
                System.out.println("WELCO " + num_partie + " " + hauteur + " " + largeur + " " + nb_fantome + " "
                        + ip_partie + " " + port_dif);
                sock_multi = new MulticastSocket(port_dif);
                sock_multi.joinGroup(InetAddress.getByName(ip_partie));
                partie_en_cours = true;
            }

            while (partie_en_cours) {

            }

            sc.close();
            lire.close();
            ecrit.close();
            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
