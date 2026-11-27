package app;

// Imports Java
import java.awt.Dimension; // permet de définir les dimensions d'un composant graphique
import java.awt.GraphicsEnvironment; // permet de vérifier si un environnement graphique est disponible
import javax.swing.JFrame; // permet de créer une fenêtre principale de l'application.
import javax.swing.SwingUtilities; // permet de lancer les opérations graphiques sur le bon thread

// Imports internes
import algorithme.SolveurNaif;
import service.CalculateurJalousie;
import ui.graphique.PanneauApplication;

// Lance la fenêtre Swing sans dépendance graphique externe
public final class MainGraphique {

    private MainGraphique() { // empêche l'instanciation du point d'entrée
    }

    /**
     * Ouvre la fenêtre sur le thread des événements Swing.
     * @param args aucun argument attendu
     */
    public static void main(String[] args) {
        if (args.length != 0) {
            System.err.println("MainGraphique n'attend aucun argument.");
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Aucun écran disponible. Lancez Main pour utiliser la console.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame fenetre = new JFrame("Organisation des chambres — Partie 1");
            fenetre.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fenetre.setContentPane(new PanneauApplication(new SolveurNaif(), new CalculateurJalousie()));
            fenetre.setMinimumSize(new Dimension(1050, 760));
            fenetre.setSize(1200, 820);
            fenetre.setLocationRelativeTo(null);
            fenetre.setVisible(true);
        });
    }
}
