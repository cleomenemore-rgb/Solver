package integration;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import algorithme.SolveurNaif;
import service.CalculateurJalousie;
import ui.InterfaceConsole;

/** Scénarios utilisateur complets utilisant tous les composants réels de la partie 1. */
class SessionConsoleTest {

    /**
     * Joue les commandes d'un utilisateur et récupère tous les messages.
     * @param saisie session à rejouer
     * @return sortie normalisée
     * @throws IOException si la lecture échoue
     */
    private String session(String saisie) throws IOException {
        StringWriter sortie = new StringWriter();
        new InterfaceConsole(new StringReader(saisie), new PrintWriter(sortie),
                new SolveurNaif(), new CalculateurJalousie()).executer();
        return sortie.toString().replace("\r\n", "\n");
    }

    /** La démonstration du sujet configure le graphe, calcule le coût et échange B et C. */
    @Test
    void joueExempleDuSujetAvecEchangeEtAnnulation() throws IOException {
        String texte = session("""
                4
                1
                A B
                1
                B C
                1
                B D
                2
                A 1 2 3 4
                2
                B 1 3 2 4
                2
                C 3 2 1 4
                2
                D 1 4 2 3
                3
                2
                1
                B C
                2
                1
                B C
                2
                3
                """);
        assertTrue(texte.contains("A:1\nB:3\nC:2\nD:4\n"));
        assertTrue(texte.contains("A:1\nB:2\nC:3\nD:4\n"));
        int premier = texte.indexOf("Nombre de personnes jalouses : 2");
        int ameliore = texte.indexOf("Nombre de personnes jalouses : 1", premier);
        int restaure = texte.indexOf("Nombre de personnes jalouses : 2", ameliore);
        assertTrue(premier >= 0 && ameliore > premier && restaure > ameliore);
        assertTrue(texte.endsWith("Au revoir !\n"));
    }

    /** Une correction valide remplace le classement et une relation répétée reste unique. */
    @Test
    void corrigePreferencesEtRepeteRelation() throws IOException {
        String texte = session("2\n1\nA B\n1\nB A\n2\nA 1 2\n2\nA 2 1\n2\nB 1 2\n3\n2\n1\nA B\n2\n3\n");
        assertTrue(texte.contains("Cette relation existe déjà."));
        assertTrue(texte.contains("A:2\nB:1\n"));
        assertTrue(texte.contains("Nombre de personnes jalouses : 0"));
        assertTrue(texte.contains("A:1\nB:2\n"));
        assertTrue(texte.contains("Nombre de personnes jalouses : 2"));
    }
}
