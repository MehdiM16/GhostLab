import java.util.Random;

public class Labyrinthe {

    byte[] larg;
    byte[] haut;
    byte nombre_fantome;
    char[][] lab;

    public Labyrinthe() { // exemple du sujet
        larg = shortToLittleEndian((short) 7);
        haut = shortToLittleEndian((short) 6);
        lab = new char[6][7];
        nombre_fantome = (byte) ((6 + 7) / 4);
        // lab[0][0], lab[1][0], lab[2][0], lab[4][0], lab[5][0] = '|';
        // lab[5][1] = '|';
        // lab[0][2], lab[1][2], lab[2][2], lab[3][2], lab[5][2] = '|';
        // je complète plus tard
    }

    public Labyrinthe(short l1, short l2) {
        haut = shortToLittleEndian(l1);
        larg = shortToLittleEndian(l2);
        lab = new char[l1][l2];
        nombre_fantome = (byte) ((l1 + l2) / 4);
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
        while (lab[a][b] == '|') {
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
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < h; j++) {
                if (lab[i][j] == '|') {
                    res += '|';
                } else {
                    res += ' ';
                }
            }
            res += '\n';
        }
        return res;
    }

}
