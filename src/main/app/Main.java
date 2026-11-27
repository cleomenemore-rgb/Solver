package app;

// Imports Java
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter; // permet d'écrire du texte dans un flux de sortie
import java.nio.charset.Charset; // permet de gérer l'encodage des caractères utilisés pour lire ou écrire du texte.

// Imports internes
import algorithme.SolveurNaif;
import service.CalculateurJalousie;
import ui.InterfaceConsole;

// Point d'entrée de la partie 1 : assemble les services et lance la console

public final class Main {

    private Main() { // empêche l'instanciation de cette classe de lancement
    }

    /**
     * Lance une session interactive, sans fermer les flux standards.
     * @param args : aucun argument pour la console, ou --gui pour la fenêtre
     */
    public static void main(String[] args) {
        if (args.length == 1 && "--gui".equals(args[0])) {
            MainGraphique.main(new String[0]);
            return;
        }
        if (args.length != 0) {
            System.err.println("Usage : java -jar voyage-chambres-0.6.0.jar [--gui]");
            return;
        }
        // Le lecteur natif de la console respecte son encodage ; les tests et
        // les entrées redirigées utilisent le charset par défaut de Java.
        Readable entree = System.console() == null
                ? new InputStreamReader(System.in, Charset.defaultCharset()) : System.console().reader();
        InterfaceConsole console = new InterfaceConsole(entree, new PrintWriter(System.out, true),
                new SolveurNaif(), new CalculateurJalousie());
        try {
            console.executer();
        } catch (IOException e) {
            System.err.println("Erreur de lecture. Programme arrêté : " + e.getMessage());
        }
    }
}
