import java.lang.Runnable;

public class Fantome implements Runnable {

    int id;
    static int id_tot = 0;
    String positionX;
    String positionY;
    int vitesse; // plus la vitesse est grande, plus le fantome se deplace rapidement

    public Fantome(String x, String y) {
        id = id_tot;
        id_tot++;
        positionX = x;
        positionY = y;
    }

    public void run() {
    }

}
