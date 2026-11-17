package fr.univ.paris.voyage.integration;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import org.junit.jupiter.api.Test;
import fr.univ.paris.voyage.algorithme.SolveurNaif;
import fr.univ.paris.voyage.service.CalculateurJalousie;
import fr.univ.paris.voyage.ui.InterfaceConsole;

/** Vérifie l'intégration du bilan détaillé et de l'annulation dans la console. */
class ConsoleEnrichieTest {

    /** L'historique fonctionne et une seconde annulation est refusée sans bloquer. */
    @Test
    void detailEtAnnulation() throws IOException {
        StringWriter texte=new StringWriter();
        new InterfaceConsole(new StringReader("2\n1\nA B\n2\nA 1 2\n2\nB 2 1\n3\n4\n1\nA B\n4\n4\n3\n"),
                new PrintWriter(texte),new SolveurNaif(),new CalculateurJalousie()).executer();
        String sortie=texte.toString();
        assertTrue(sortie.contains("Récapitulatif du groupe"));
        assertTrue(sortie.contains("Coût : 0 → 2"));
        assertTrue(sortie.contains("Échange annulé. Coût : 2 → 0"));
        assertTrue(sortie.contains("Aucun échange à annuler"));
        assertTrue(sortie.contains("Au revoir !"));
    }
}
