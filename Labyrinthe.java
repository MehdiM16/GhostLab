public class Labyrinthe {

    short larg;
    short haut;
    int nombre_fantome;
    char[][] lab;

    public Labyrinthe() { // exemple du sujet
        larg = 7;
        haut = 6;
        lab = new char[6][7];
        nombre_fantome = (larg + haut) / 4;
        // lab[0][0], lab[1][0], lab[2][0], lab[4][0], lab[5][0] = '|';
        // lab[5][1] = '|';
        // lab[0][2], lab[1][2], lab[2][2], lab[3][2], lab[5][2] = '|';
        // je complète plus tard
    }

    public Labyrinthe(short l1, short l2) {
        larg = l1;
        haut = l2;
        lab = new char[l1][l2];
        nombre_fantome = (larg + haut) / 4;
    }

    public String toString() {
        String res = "";
        for (int i = 0; i < haut; i++) {
            for (int j = 0; j < larg; j++) {
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
