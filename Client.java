import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

    static class Lire_multicast implements Runnable {

        static class Lire_UDP implements Runnable {

            DatagramSocket sock_udp;
            boolean partie_finis = false;

            public Lire_UDP(DatagramSocket s) {
                sock_udp = s;
            }

            public void run() {
                try {
                    byte[] data = new byte[230];
                    DatagramPacket paquet = new DatagramPacket(data, data.length);
                    while (!partie_finis) {
                        sock_udp.receive(paquet);
                        String st = new String(paquet.getData(), 0, paquet.getLength());
                        System.out.println(st.substring(0, st.length() - 3));
                    }
                    System.out.println("la lecture udp est finis");
                } catch (Exception e) {
                    System.out.println("la lecture udp est finis dans le catch");
                }
            }

        }

        MulticastSocket sock;
        boolean en_cours = true;
        Lire_UDP lit_udp;
        DatagramSocket data_sock;

        public Lire_multicast(MulticastSocket ms, DatagramSocket ds, String addr) {
            try {
                sock = ms;
                sock.joinGroup(InetAddress.getByName(addr));
                data_sock = ds;
                lit_udp = new Lire_UDP(data_sock);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                Thread udp_t = new Thread(lit_udp);
                udp_t.start();
                byte[] data = new byte[230];
                DatagramPacket paquet = new DatagramPacket(data, data.length);
                while (en_cours) {
                    sock.receive(paquet);
                    String recu = new String(paquet.getData(), 0, paquet.getLength());
                    System.out.println(recu.substring(0, recu.length() - 3));
                    if (recu.substring(0, 5).equals("ENDGA")) {
                        en_cours = false;
                        data_sock.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("la lecture multicast est finis dans le catch");
            }
        }

    }

    public static void lire_partie(BufferedReader br, String mess) {
        try {
            if (mess.equals("DUNNO")) {
                br.read();
                br.read();
                br.read();
                System.out.println(mess);
            } else {
                int nb_partie = lire_nombre_fin(br);
                System.out.println(mess + " " + nb_partie);
                for (int i = 0; i < nb_partie; i++) {
                    try {
                        char[] partie = new char[5];
                        br.read(partie, 0, 5);
                        String part = String.valueOf(partie);// OGAME
                        int num_partie = lire_nombre_milieu(br);
                        int nb_inscrit = lire_nombre_fin(br);
                        System.out.println(part + " " + num_partie + " " + nb_inscrit);
                    } catch (Exception e) {
                        System.out.println("erreur lecture partie");
                    }
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
            if (rep.equals("DUNNO")) {
                System.out.println(rep);
                br.read();
                br.read();
                br.read(); // on lit les *** pour lire entierement le message
            } else {
                int num_partie = lire_nombre_milieu(br);
                int nb_joueur = lire_nombre_fin(br);
                System.out.println(rep + " " + num_partie + " " + nb_joueur);
                for (int i = 0; i < nb_joueur; i++) {
                    char[] joueur_i = new char[5];
                    br.read(joueur_i, 0, 5);
                    String joueur_s = String.valueOf(joueur_i);
                    String pseudo = lire_pseudo_fin(br);
                    System.out.println(joueur_s + " " + pseudo);
                }
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

    public static void lire_mouvement(BufferedReader br, String message) {
        try {
            if (message.equals("MOVE!")) {
                String posX = lire_pseudo_milieu(br);
                String posY = lire_pseudo_fin(br);
                System.out.println(message + " " + posX + " " + posY);
            } else if (message.equals("MOVEF")) {
                String posX = lire_pseudo_milieu(br);
                String posY = lire_pseudo_milieu(br);
                String score = lire_pseudo_fin(br);
                System.out.println(message + " " + posX + " " + posY + " " + score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void lire_joueur_partie(BufferedReader br) {
        try {
            int nb_joueur = lire_nombre_fin(br);
            System.out.println(" " + nb_joueur);
            for (int i = 0; i < nb_joueur; i++) {
                char[] joueur_i = new char[5];
                br.read(joueur_i, 0, 5);
                String joueur_s = String.valueOf(joueur_i);
                String pseudo = lire_pseudo_milieu(br);
                String posX = lire_pseudo_milieu(br);
                String posY = lire_pseudo_milieu(br);
                String score = lire_pseudo_fin(br);
                System.out.println(joueur_s + " " + pseudo + " " + posX + " " + posY + " " + score);
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

    public static String lire_pseudo_milieu(BufferedReader br) { // on lit un string qui se situe au milieu d'un
                                                                 // message
        String res = "";
        try {
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

    public static String lire_pseudo_fin(BufferedReader br) { // on lit un string qui se situe au milieu d'un
                                                              // message
        String res = "";
        String fin = "";
        boolean prec_etoile = false;
        try {
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

    public static String lire_addresse_milieu(BufferedReader br) {
        String res = "";
        try {
            char lu = (char) br.read();
            while (lu != ' ' || res.length() == 0) {
                if (lu != '#') {
                    res += lu;
                }
                lu = (char) br.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static int recup_port(String message) {
        String port = "";
        boolean espace_lu = false;
        for (int i = 6; i < message.length(); i++) {
            char tmp = message.charAt(i);
            if (Character.isDigit(tmp) && espace_lu) {
                port += tmp;
            } else if (tmp == ' ') {
                if (espace_lu) {
                    break;
                    // si on arrive ici c'est que on lit un espace une deuxieme fois et que donc on
                    // a finis de lire le port
                } else {
                    espace_lu = true;
                }
            }
        }
        return Integer.valueOf(port);
    }

    public static String lire_message_total(BufferedReader br) { // lit un message sans tenir compte de la presence
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

    public static String supprime_espace(String mess) { // supprime des espace inutile au debut et a la fin d'un message
                                                        // s'il y en a
        int taille = mess.length();
        for (int i = taille - 1; i >= 0; i--) {
            if (mess.charAt(i) == ' ') {
                mess = mess.substring(0, mess.length() - 1);
            } else {
                break;
            }
        }
        taille = mess.length();
        while (mess.charAt(0) == ' ') {
            mess = mess.substring(1);
        }
        return mess;
    }

    public static void main(String[] args) {
        try {
            Socket sock = new Socket("localhost", 9123); // ADAPTER POUR LULU
            BufferedReader lire = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter ecrit = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            Scanner sc = new Scanner(System.in);

            int port_udp = -1;

            char[] type_mess = new char[5];
            lire.read(type_mess, 0, 5);
            String mess_recu = String.valueOf(type_mess); // correspond au type de message envoyer par le serveur
            lire_partie(lire, mess_recu);
            String mess = ""; // correspond au message que le client enverra au serveur
            boolean est_inscrit = false;
            while (!mess.equals("START")) { // PROTOCOLE TCP
                System.out.println("vous pouvez entrer un message");
                String mess_tmp = sc.nextLine();
                mess = supprime_espace(mess_tmp);
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

                else if (mess.contains("GAME?")) {
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
                        port_udp = recup_port(mess);
                        int num_partie = lire_nombre_fin(lire);
                        System.out.println(mess_recu + " " + num_partie);
                        est_inscrit = true;
                    }
                }

                else if (mess.contains("START")) {
                    // si on envoie START le serveur ne repond rien
                }

                else {
                    System.out.println(lire_message_total(lire));
                }
            }

            if (est_inscrit) {
                lire.read(type_mess, 0, 5);
                mess_recu = String.valueOf(type_mess);
            }

            boolean partie_en_cours = false;

            if (mess_recu.equals("WELCO")) {
                int num_partie = lire_nombre_milieu(lire);
                int hauteur = lire_nombre_milieu(lire);
                int largeur = lire_nombre_milieu(lire);
                int nb_fantome = lire_nombre_milieu(lire);
                String ip_partie = lire_addresse_milieu(lire);
                int port_dif = lire_nombre_fin(lire);
                System.out.println("WELCO " + num_partie + " " + hauteur + " " + largeur + " " + nb_fantome + " "
                        + ip_partie + " " + port_dif);
                partie_en_cours = true;

                MulticastSocket multi_sock = new MulticastSocket(port_dif);
                DatagramSocket sock_mess = new DatagramSocket(port_udp);

                Lire_multicast lecture = new Lire_multicast(multi_sock, sock_mess, ip_partie);
                Thread t_lecture = new Thread(lecture);
                t_lecture.start();

                lire.read(type_mess, 0, 5); // on lit POSIT ...
                mess_recu = String.valueOf(type_mess);
                String nom_joueur = lire_pseudo_milieu(lire);
                String pos_x = lire_pseudo_milieu(lire);
                String pos_y = lire_pseudo_fin(lire);
                System.out.println(mess_recu + " " + nom_joueur + " " + pos_x + " " + pos_y);

                while (partie_en_cours) {
                    System.out.println("vous pouvez entrer un message");
                    String mess_tmp = sc.nextLine();
                    mess = supprime_espace(mess_tmp);
                    ecrit.print(mess + "***");
                    ecrit.flush();
                    System.out.println(mess);

                    lire.read(type_mess, 0, 5);
                    mess_recu = String.valueOf(type_mess);

                    if (mess_recu.equals("GOBYE")) {
                        System.out.println(mess_recu);
                        partie_en_cours = false;
                        lire.read();
                        lire.read();
                        lire.read();// on lit les ***
                    } else {

                        if (mess.contains("MOV") && (mess_recu.equals("MOVE!") || mess_recu.equals("MOVEF"))) {
                            lire_mouvement(lire, mess_recu);
                        }

                        else if (mess.contains("GLIS?") && (mess_recu.equals("GLIS!"))) {
                            System.out.print(mess_recu);
                            lire_joueur_partie(lire);
                        }

                        else if (mess.contains("MALL?")) {
                            System.out.println(mess_recu);
                            lire.read();
                            lire.read();
                            lire.read(); // on lit les ***
                        }

                        else if (mess.contains("SEND?")) {
                            System.out.println(mess_recu);
                            lire.read();
                            lire.read();
                            lire.read(); // on lit les ***
                        }

                        else if (mess_recu.equals("DUNNO")) {
                            System.out.println(mess_recu);
                            lire.read();
                            lire.read();
                            lire.read();
                        }

                        else {
                            System.out.println(lire_message_total(lire));
                        }

                    }
                }

                multi_sock.close();
                sock_mess.close();
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
