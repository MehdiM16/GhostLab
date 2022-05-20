import java.util.ArrayList;
import java.util.Random;

public class Labyrinthe {

    byte[] larg;
    byte[] haut;
    byte nombre_fantome;
    char[][] lab;
    ArrayList<Fantome> liste = new ArrayList<Fantome>();
    byte[] addresse_diffusion;
    String port_diffusion;

    public Labyrinthe(byte[] addr, String port) {
        larg = shortToLittleEndian((short) 7);
        haut = shortToLittleEndian((short) 6);
        lab = new char[6][7];
        generateLab();
        nombre_fantome = (byte) ((6 + 7) / 4);
        addresse_diffusion = addr;
        port_diffusion = port;
        for (int i = 0; i < nombre_fantome; i++) {
            String pos = positionAleatoire();
            Fantome f = new Fantome(pos.substring(0, 3), pos.substring(3), addresse_diffusion, port_diffusion, this);
            // liste.add(f);
            add_fantome(f);
        }
    }

    // On represente les mur par "|" et les fantome par "F"
    // et les cases vide par un V
    // On pourra definir la position de base des fantome avec la fonction
    // positionAleatoire()

    public synchronized void setCase(int x, int y, char c) {
        try {
            lab[x][y] = c;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized char getCase(int x, int y) {
        try {
            return lab[x][y];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ' ';
    }

    public synchronized void add_fantome(Fantome f) {
        liste.add(f);
    }

    public synchronized void remove_fantome(Joueur j, String x, String y) {
        try {
            Fantome a_supprimer = new Fantome();
            for (Fantome f : liste) {
                if (f.getPosX().equals(x) && f.getPosY().equals(y)) {
                    f.setAttraper(true);
                    int mon_score = Integer.valueOf(j.point);
                    int new_score = mon_score + 50;
                    j.setPoint(String.valueOf(j.scoreToString(new_score)));
                    a_supprimer = f;
                }
            }
            liste.remove(a_supprimer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] shortToLittleEndian(short numero) {
        byte[] b = new byte[4];
        b[0] = (byte) (numero & 0xFF);
        b[1] = (byte) ((numero >> 8) & 0xFF);
        b[2] = (byte) ((numero >> 16) & 0xFF);
        b[3] = (byte) ((numero >> 24) & 0xFF);
        return b;
    }

    public int littleEndianToInt(byte[] tab) {
        int res = java.nio.ByteBuffer.wrap(tab).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        return res;
    }

    public String positionAleatoire() { // retourne la position sous la forme "XXXXXX" les 3 premier X etant pour la
                                        // ligne et les 3 suivant pour la colonne
        int a = new Random().nextInt(littleEndianToInt(haut));
        int b = new Random().nextInt(littleEndianToInt(larg));
        while (lab[a][b] == '|' || lab[a][b] == 'F') {
            a = new Random().nextInt(littleEndianToInt(haut));
            b = new Random().nextInt(littleEndianToInt(larg));
        }
        return posIntToString(a) + posIntToString(b);
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

    public String toString() {
        String res = "";
        int l = littleEndianToInt(larg);
        int h = littleEndianToInt(haut);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < l; j++) {
                res += lab[i][j] + " ";
            }
            res += '\n';
        }
        return res;
    }

    public void generateLab() { // genere le labyrinthe de d'exemple du sujet
        lab[0][0] = '|';
        lab[0][1] = 'V';
        for (int i = 2; i < lab[0].length; i++) {
            lab[0][i] = '|';
        }
        lab[1][0] = '|';
        lab[1][1] = 'V';
        lab[1][2] = '|';
        for (int i = 3; i < lab[1].length; i++) {
            lab[1][i] = 'V';
        }
        lab[2][0] = '|';
        lab[2][1] = 'V';
        lab[2][2] = '|';
        lab[2][3] = '|';
        lab[2][4] = 'V';
        lab[2][5] = '|';
        lab[2][6] = '|';
        for (int i = 0; i < 5; i++) {
            lab[3][i] = 'V';
        }
        lab[3][2] = '|';
        lab[3][5] = '|';
        lab[3][6] = '|';

        lab[4][0] = '|';
        lab[4][1] = 'V';
        lab[4][2] = 'V';
        lab[4][3] = 'V';
        lab[4][4] = 'V';
        lab[4][5] = 'V';
        lab[4][6] = '|';

        for (int i = 0; i < lab[5].length; i++) {
            lab[5][i] = '|';
        }
        lab[5][5] = 'V';

        System.out.println(toString());
    }

}
