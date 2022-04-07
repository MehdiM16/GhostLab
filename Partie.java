import java.io.*;
import java.net.*;
import java.lang.Runnable;

public class Partie implements Runnable, Serializable {
    
    ArrayList<Joueur> liste = new Arraylist<Joueur>();
    final int id;

    Socket sock = new Socket(9999);
    DatagramSocket dgsock = new DatagramSocket();

}
