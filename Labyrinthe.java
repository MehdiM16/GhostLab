public class Labyrinthe {

    int long;
    int larg;
    char[][] lab;

    public Labyrinthe() {
        long = 0;
        larg = 0;
        lab = new char[0][0];
    }

    public Labyrinthe(int l1, int l2) {
        long = l1;
        larg = l2;
        lab = new char[l1][l2];
    }

}
