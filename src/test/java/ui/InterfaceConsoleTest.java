package ui;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import algorithme.SolveurAffectation;
import algorithme.SolveurNaif;
import modele.Affectation;
import modele.Groupe;
import service.CalculateurJalousie;

/** Vérifie les saisies, les erreurs, les fins d'entrée et les dépendances de la console. */
class InterfaceConsoleTest {

    /** Solveur de test : compte les appels et vérifie que le groupe est complet. */
    private static class SolveurCompteur implements SolveurAffectation {
        private int appels = 0;

        @Override
        public Affectation resoudre(Groupe groupe) {
            appels++;
            assertTrue(groupe.estComplet());
            return new SolveurNaif().resoudre(groupe);
        }
    }

    /**
     * Exécute une session avec les services réels et capture son affichage.
     * @param saisie lignes simulées au clavier
     * @return sortie avec fins de ligne normalisées
     * @throws IOException si la source échoue
     */
    private String session(String saisie) throws IOException {
        StringWriter sortie = new StringWriter();
        new InterfaceConsole(new StringReader(saisie), new PrintWriter(sortie),
                new SolveurNaif(), new CalculateurJalousie()).executer();
        return sortie.toString().replace("\r\n", "\n");
    }

    /**
     * Une taille incorrecte ne bloque pas la lecture du nombre suivant.
     * @param invalide taille invalide à essayer
     * @throws IOException si la lecture échoue
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "0", "27"})
    void redemandeEffectifApresErreur(String invalide) throws IOException {
        String texte = session(invalide + "\n1\n2\nA 1\n3\n3\n");
        assertTrue(texte.contains("Groupe créé : [A]"));
        assertTrue(texte.contains("A:1\n"));
        assertTrue(texte.contains("Au revoir !"));
    }

    /**
     * Les deux menus récupèrent après un choix inconnu ou mal typé.
     * @param invalide choix erroné
     * @throws IOException si la lecture échoue
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "bonjour", "9", "-2"})
    void recupereApresChoixInvalideDansLesDeuxMenus(String invalide) throws IOException {
        String texte = session("1\n" + invalide + "\n2\nA 1\n3\n" + invalide + "\n2\n3\n");
        assertTrue(texte.contains("Nombre de personnes jalouses : 0"));
        assertTrue(texte.contains("Au revoir !"));
        assertTrue(texte.contains("Choix inconnu") || texte.contains("Saisie incorrecte"));
    }

    /**
     * Un classement incorrect ne détruit pas celui précédemment enregistré.
     * @param invalide classement invalide
     * @throws IOException si la lecture échoue
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "A 1 1", "A 3 2", "Z 1 2"})
    void conservePreferencesApresCorrectionInvalide(String invalide) throws IOException {
        String texte = session("2\n2\nA 2 1\n2\n" + invalide + "\n2\nB 1 2\n3\n3\n");
        assertTrue(texte.contains("Préférences refusées"));
        assertTrue(texte.contains("A:2\nB:1\n"));
        assertTrue(texte.contains("Au revoir !"));
    }

    /**
     * Une relation invalide est refusée sans empêcher la suite de la configuration.
     * @param invalide paire de noms invalide
     * @throws IOException si la lecture échoue
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "A B C", "A Z", "A A"})
    void refuseRelationInvalide(String invalide) throws IOException {
        String texte = session("2\n1\n" + invalide + "\n2\nA 1 2\n2\nB 1 2\n3\n2\n3\n");
        assertTrue(texte.contains("Relation refusée"));
        assertTrue(texte.contains("Nombre de personnes jalouses : 0"));
    }

    /**
     * Un échange invalide laisse l'affectation inchangée.
     * @param invalide paire incorrecte
     * @throws IOException si la lecture échoue
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "A B C", "A Z"})
    void conserveAffectationApresEchangeInvalide(String invalide) throws IOException {
        String texte = session("2\n2\nA 1 2\n2\nB 1 2\n3\n1\n" + invalide + "\n3\n");
        assertTrue(texte.contains("Échange refusé"));
        assertEquals(3, texte.split("A:1\nB:2\n", -1).length - 1);
        assertFalse(texte.contains("A:2\n"));
    }

    /**
     * Chaque invite accepte une fin d'entrée sans boucle infinie ni exception.
     * @param saisie préfixe de session se terminant à une invite
     * @throws IOException si la lecture échoue
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "1\n", "1\n1\n", "1\n2\nA 1\n3\n"})
    void termineSurFinEntree(String saisie) throws IOException {
        String texte = session(saisie);
        assertTrue(texte.contains("Fin de l'entrée. Programme terminé."));
        assertFalse(texte.contains("Au revoir !"));
    }

    /** Une dernière ligne non terminée par un saut de ligne reste exploitable. */
    @Test
    void litDerniereLigneSansRetour() throws IOException {
        assertTrue(session("1\n2\nA 1\n3\n3").contains("Au revoir !"));
    }

    /** Espaces multiples, tabulations et fins de ligne Windows sont acceptés. */
    @Test
    void accepteEspacesEtFinsDeLigneWindows() throws IOException {
        String texte = session(" 2 \r\n1\r\n A\t B \r\n2\r\n A\t2   1 \r\n2\r\n B 1 2\r\n3\r\n2\r\n3\r\n");
        assertTrue(texte.contains("A:2\nB:1\n"));
        assertTrue(texte.contains("Nombre de personnes jalouses : 0"));
    }

    /** Un même nom dans un échange est une opération sans effet, explicitement signalée. */
    @Test
    void expliqueEchangeAvecSoiMeme() throws IOException {
        String texte = session("1\n2\nA 1\n3\n1\nA A\n3\n");
        assertTrue(texte.contains("Même personne : affectation inchangée."));
        assertTrue(texte.contains("A:1\n"));
    }

    /** Le modèle reçu par le solveur contient les noms A à Z et 26 chambres. */
    @Test
    void creeVingtSixPersonnesEtChambres() throws IOException {
        StringBuilder saisie = new StringBuilder("26\n");
        for (char nom = 'A'; nom <= 'Z'; nom++) {
            saisie.append("2\n").append(nom);
            for (int chambre = 1; chambre <= 26; chambre++) {
                saisie.append(' ').append(chambre);
            }
            saisie.append('\n');
        }
        saisie.append("3\n3\n");
        String texte = session(saisie.toString());
        assertTrue(texte.contains("A:1\nB:2\n"));
        assertTrue(texte.contains("Y:25\nZ:26\n"));
        assertTrue(texte.contains("Au revoir !"));
    }

    /** Le solveur n'est appelé qu'une seule fois, quand toutes les préférences sont renseignées. */
    @Test
    void bloqueResolutionEtIndiquePersonnesManquantes() throws IOException {
        SolveurCompteur solveur = new SolveurCompteur();
        StringWriter sortie = new StringWriter();
        new InterfaceConsole(new StringReader("2\n3\n2\nA 1 2\n3\n2\nB 2 1\n3\n3\n"),
                new PrintWriter(sortie), solveur, new CalculateurJalousie()).executer();
        assertEquals(1, solveur.appels);
        assertTrue(sortie.toString().contains("Préférences manquantes pour : [A, B]"));
        assertTrue(sortie.toString().contains("Préférences manquantes pour : [B]"));
    }

    /** Une entrée interrompue pendant la configuration ne déclenche pas le solveur. */
    @Test
    void neResoutPasConfigurationInterrompue() throws IOException {
        SolveurCompteur solveur = new SolveurCompteur();
        new InterfaceConsole(new StringReader("2\n2\nA 1 2\n3\n"),
                new PrintWriter(new StringWriter()), solveur, new CalculateurJalousie()).executer();
        assertEquals(0, solveur.appels);
    }

    /** Chaque dépendance obligatoire est contrôlée au constructeur. */
    @Test
    void refuseDependancesNulles() {
        StringReader source = new StringReader("");
        PrintWriter sortie = new PrintWriter(new StringWriter());
        SolveurNaif solveur = new SolveurNaif();
        CalculateurJalousie calculateur = new CalculateurJalousie();
        assertThrows(IllegalArgumentException.class, () -> new InterfaceConsole(null, sortie, solveur, calculateur));
        assertThrows(IllegalArgumentException.class, () -> new InterfaceConsole(source, null, solveur, calculateur));
        assertThrows(IllegalArgumentException.class, () -> new InterfaceConsole(source, sortie, null, calculateur));
        assertThrows(IllegalArgumentException.class, () -> new InterfaceConsole(source, sortie, solveur, null));
    }
}
