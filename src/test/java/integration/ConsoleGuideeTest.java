package integration;

import static org.junit.jupiter.api.Assertions.*; // permet d'effectuer des vérifications dans les tests (égalité, nullité, conditions, etc.)
import java.io.*;
import org.junit.jupiter.api.Test; // permet de définir et d'exécuter des tests unitaires avec JUnit
import algorithme.SolveurNaif;
import service.CalculateurJalousie;
import ui.InterfaceConsole;

/** Sessions complètes : aide contextuelle, correction et conservation des données. */
class ConsoleGuideeTest {
    /** @param saisie réponses simulées @return texte affiché @throws IOException en cas de panne */
    private String jouer(String saisie) throws IOException {
        StringWriter texte = new StringWriter();
        new InterfaceConsole(new StringReader(saisie), new PrintWriter(texte),
                new SolveurNaif(), new CalculateurJalousie()).executer();
        return texte.toString().replace("\r\n", "\n");
    }

    /** Le récapitulatif est consultable avant résolution, sans changer la configuration. */
    @Test
    void recapitulatifPartielPuisComplet() throws IOException {
        String texte = jouer("2\n4\n1\nA B\n2\nA 2 1\n4\n2\nB 1 2\n4\n3\n3\n");
        assertTrue(texte.contains("à renseigner"));
        assertTrue(texte.contains("A — relations : [B] ; préférences : [2, 1]"));
        assertTrue(texte.contains("Classements renseignés : 0/2"));
        assertTrue(texte.contains("Classements renseignés : 1/2"));
        assertTrue(texte.contains("Classements renseignés : 2/2"));
        assertTrue(texte.contains("A:2\nB:1\n"));
    }

    /** Le format de l'exemple suit la taille réelle, même pour une personne seule. */
    @Test
    void exempleAdapteAuGroupeMinimal() throws IOException {
        String texte = jouer("1\n2\nA 1\n3\n3\n");
        assertTrue(texte.contains("exemple : A 1)"));
        assertFalse(texte.contains("exemple : A 1 2 3"));
        assertTrue(texte.contains("de la plus appréciée à la moins appréciée"));
        assertTrue(texte.contains("ne garantit pas le minimum de jalousies"));
    }

    /** Un refus après un échange conserve aussi la possibilité de l'annuler. */
    @Test
    void erreurPuisAnnulationRetrouveRepartition() throws IOException {
        String texte = jouer("2\n1\nA B\n2\nA 1 2\n2\nB 2 1\n3\n1\nA B\n1\nA Z\n4\n2\n3\n");
        assertTrue(texte.contains("A reçoit la chambre 2 ; B reçoit la chambre 1"));
        assertTrue(texte.contains("Échange refusé"));
        assertTrue(texte.contains("Répartition inchangée. Choisissez 1 pour réessayer"));
        assertTrue(texte.contains("Échange annulé. Coût : 2 → 0"));
        assertTrue(texte.contains("Nombre de personnes jalouses : 0"));
        assertTrue(texte.endsWith("A:1\nB:2\nFin de session : aucune sauvegarde sur fichier n’est effectuée.\nAu revoir !\n"));
    }

    /** Des noms inconnus n'ajoutent aucune arête ; une reprise valide fonctionne. */
    @Test
    void relationRefuseePuisCorrigee() throws IOException {
        String texte = jouer("2\n1\nA Z\n4\n1\nA B\n4\n");
        assertTrue(texte.contains("Relations inchangées"));
        assertTrue(texte.contains("A — relations : []"));
        assertTrue(texte.contains("A — relations : [B]"));
        assertTrue(texte.contains("Fin de l'entrée. Programme terminé."));
    }
}
