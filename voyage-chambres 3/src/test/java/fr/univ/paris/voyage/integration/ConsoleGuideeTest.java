package fr.univ.paris.voyage.integration;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import fr.univ.paris.voyage.algorithme.SolveurNaif;
import fr.univ.paris.voyage.service.CalculateurJalousie;
import fr.univ.paris.voyage.ui.InterfaceConsole;

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

    /**
     * Une erreur indique sa cause et laisse le classement initial intact.
     * @param saisie correction invalide @param explication aide attendue
     * @throws IOException si le lecteur échoue
     */
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "A 1 1|apparaît plusieurs fois", "A 0 2|numéros de 1 à 2",
        "A 3 2|numéros de 1 à 2", "A x 2|numéro entier valide",
        "a 1 2|en majuscules parmi [A, B]", "Z 1 2|Personne inconnue",
        "A 1|un nom suivi de 2 numéros"
    })
    void expliqueCorrectionSansPerte(String saisie, String explication) throws IOException {
        String texte = jouer("2\n2\nA 2 1\n2\n" + saisie + "\n2\nB 1 2\n3\n3\n");
        assertTrue(texte.contains(explication));
        assertTrue(texte.contains("Aucun classement modifié"));
        assertTrue(texte.contains("Choisissez 2 pour réessayer"));
        assertTrue(texte.contains("A:2\nB:1\n"));
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
