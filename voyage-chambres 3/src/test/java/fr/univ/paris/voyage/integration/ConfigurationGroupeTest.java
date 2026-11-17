package fr.univ.paris.voyage.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import fr.univ.paris.voyage.modele.Chambre;
import fr.univ.paris.voyage.modele.Groupe;
import fr.univ.paris.voyage.modele.Personne;
import fr.univ.paris.voyage.modele.Preferences;

/** Vérifie des configurations complètes en assemblant les quatre classes métier. */
class ConfigurationGroupeTest {

    /** Reconstitue le groupe du sujet, corrige une saisie et vérifie les comparaisons utiles au futur coût. */
    @Test
    void configureExempleDuSujetEtCorrigePreferences() {
        Groupe groupe = new Groupe(
                List.of(new Personne("A"), new Personne("B"), new Personne("C"), new Personne("D")),
                List.of(new Chambre(1), new Chambre(2), new Chambre(3), new Chambre(4)));
        Personne a = groupe.getPersonne("A");
        Personne b = groupe.getPersonne("B");
        Personne c = groupe.getPersonne("C");
        Personne d = groupe.getPersonne("D");
        groupe.ajouterRelation(b, a);
        groupe.ajouterRelation(b, c);
        groupe.ajouterRelation(b, d);
        groupe.definirPreferences(a, new Preferences(List.of(new Chambre(1), new Chambre(2), new Chambre(3), new Chambre(4))));
        groupe.definirPreferences(b, new Preferences(List.of(new Chambre(1), new Chambre(3), new Chambre(2), new Chambre(4))));
        groupe.definirPreferences(c, new Preferences(List.of(new Chambre(3), new Chambre(2), new Chambre(1), new Chambre(4))));
        assertFalse(groupe.estComplet());
        assertEquals(List.of(d), groupe.personnesSansPreferences());

        Preferences incompletes = new Preferences(List.of(new Chambre(1), new Chambre(4), new Chambre(2)));
        assertThrows(IllegalArgumentException.class, () -> groupe.definirPreferences(d, incompletes));
        assertEquals(List.of(d), groupe.personnesSansPreferences());
        groupe.definirPreferences(d, new Preferences(List.of(new Chambre(1), new Chambre(4), new Chambre(2), new Chambre(3))));

        assertTrue(groupe.estComplet());
        assertEquals(Set.of(a, c, d), groupe.getVoisins(b));
        for (Personne personne : List.of(a, c, d)) {
            assertEquals(Set.of(b), groupe.getVoisins(personne));
        }
        assertTrue(groupe.getPreferences(b).prefere(groupe.getChambre(3), groupe.getChambre(2)));
        assertTrue(groupe.getPreferences(c).prefere(groupe.getChambre(3), groupe.getChambre(2)));
        assertTrue(groupe.getPreferences(d).prefere(groupe.getChambre(4), groupe.getChambre(2)));
        assertFalse(groupe.getPreferences(a).prefere(groupe.getChambre(2), groupe.getChambre(1)));
    }

    /** Le groupe minimal est complet dès que son unique classement est défini. */
    @Test
    void configureGroupeMinimal() {
        Groupe groupe = new Groupe(List.of(new Personne("A")), List.of(new Chambre(1)));
        Personne a = groupe.getPersonne("A");
        groupe.definirPreferences(a, new Preferences(groupe.getChambres()));
        assertTrue(groupe.estComplet());
        assertTrue(groupe.getVoisins(a).isEmpty());
        assertEquals(0, groupe.getPreferences(a).rangDe(groupe.getChambre(1)));
    }
}
