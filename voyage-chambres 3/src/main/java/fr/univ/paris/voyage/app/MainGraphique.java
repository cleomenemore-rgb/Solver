package fr.univ.paris.voyage.app;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import fr.univ.paris.voyage.algorithme.SolveurNaif;
import fr.univ.paris.voyage.service.CalculateurJalousie;
import fr.univ.paris.voyage.ui.graphique.PanneauApplication;

/** Lance la fenêtre Swing sans dépendance graphique externe. */
public final class MainGraphique {

    /** Empêche l'instanciation du point d'entrée. */
    private MainGraphique() {
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
