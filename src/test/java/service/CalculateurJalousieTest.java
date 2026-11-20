package service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import modele.Affectation;
import modele.Chambre;
import modele.Groupe;
import modele.Personne;
import modele.Preferences;

/** Vérifie la définition de la jalousie et les préconditions des calculs. */
class CalculateurJalousieTest {

    /** Calculateur sans état, propre à chaque instance de test JUnit. */
    private final CalculateurJalousie calculateur = new CalculateurJalousie();

    /** @return groupe A et B dont chacun préfère la chambre 1 à la chambre 2 */
    private Groupe groupeComplet() {
        Groupe groupe = new Groupe(List.of(new Personne("A"), new Personne("B")),
                List.of(new Chambre(1), new Chambre(2)));
        for (Personne personne : groupe.getPersonnes()) {
            groupe.definirPreferences(personne, new Preferences(groupe.getChambres()));
        }
        return groupe;
    }

    /**
     * Construit l'affectation A vers 1, B vers 2.
     * @param groupe groupe compatible
     * @return affectation complète
     */
    private Affectation affectation(Groupe groupe) {
        return new Affectation(groupe, Map.of(new Personne("A"), new Chambre(1), new Personne("B"), new Chambre(2)));
    }

    /** Préférer la chambre d'une personne que l'on apprécie n'est pas de la jalousie au sens du sujet. */
    @Test
    void ignorePersonnesNonVoisines() {
        Groupe groupe = groupeComplet();
        Affectation affectation = affectation(groupe);
        assertFalse(calculateur.estJalouse(new Personne("B"), groupe, affectation));
        assertEquals(0, calculateur.calculerCout(groupe, affectation));
        groupe.ajouterRelation(new Personne("A"), new Personne("B"));
        assertEquals(1, calculateur.calculerCout(groupe, affectation));
    }

    /** La relation est symétrique, mais la jalousie peut ne concerner qu'un seul membre. */
    @Test
    void jalousieNonNecessairementReciproque() {
        Groupe groupe = groupeComplet();
        groupe.ajouterRelation(new Personne("A"), new Personne("B"));
        Affectation affectation = affectation(groupe);
        assertFalse(calculateur.estJalouse(new Personne("A"), groupe, affectation));
        assertTrue(calculateur.estJalouse(new Personne("B"), groupe, affectation));
        assertEquals(Set.of(new Personne("B")), calculateur.personnesJalouses(groupe, affectation));
    }

    /** Avec des préférences opposées, les deux personnes peuvent être jalouses puis satisfaites. */
    @Test
    void utilisePreferencesDeChaquePersonneEtRecalculeApresEchange() {
        Groupe groupe = groupeComplet();
        Personne a = new Personne("A");
        Personne b = new Personne("B");
        groupe.definirPreferences(a, new Preferences(List.of(new Chambre(2), new Chambre(1))));
        groupe.ajouterRelation(a, b);
        Affectation affectation = affectation(groupe);
        assertEquals(2, calculateur.calculerCout(groupe, affectation));
        affectation.echanger(a, b);
        assertEquals(0, calculateur.calculerCout(groupe, affectation));
    }

    /** Une personne jalousant deux voisins n'est comptée qu'une fois. */
    @Test
    void compteUneSeuleFoisMalgrePlusieursVoisins() {
        Groupe groupe = new Groupe(List.of(new Personne("A"), new Personne("B"), new Personne("C")),
                List.of(new Chambre(1), new Chambre(2), new Chambre(3)));
        Personne a = new Personne("A");
        Personne b = new Personne("B");
        Personne c = new Personne("C");
        for (Personne personne : groupe.getPersonnes()) {
            groupe.definirPreferences(personne, new Preferences(groupe.getChambres()));
        }
        groupe.ajouterRelation(a, b);
        groupe.ajouterRelation(a, c);
        Affectation affectation = new Affectation(groupe, Map.of(a, new Chambre(3), b, new Chambre(1), c, new Chambre(2)));
        assertEquals(Set.of(a), calculateur.personnesJalouses(groupe, affectation));
        assertEquals(1, calculateur.calculerCout(groupe, affectation));
    }

    /** Le résultat est protégé et reste un instantané après un échange. */
    @Test
    void protegeResultatEtNeModifiePasAffectation() {
        Groupe groupe = groupeComplet();
        Personne a = new Personne("A");
        Personne b = new Personne("B");
        groupe.ajouterRelation(a, b);
        Affectation affectation = affectation(groupe);
        Set<Personne> avant = calculateur.personnesJalouses(groupe, affectation);
        assertThrows(UnsupportedOperationException.class, avant::clear);
        assertEquals(new Chambre(1), affectation.chambreDe(a));
        assertEquals(new Chambre(2), affectation.chambreDe(b));
        assertEquals(Set.of(b), groupe.getVoisins(a));
        affectation.echanger(a, b);
        assertEquals(Set.of(b), avant);
        assertEquals(Set.of(a), calculateur.personnesJalouses(groupe, affectation));
    }

    /** Une modification du classement est prise en compte au calcul suivant. */
    @Test
    void recalculeApresModificationPreferences() {
        Groupe groupe = groupeComplet();
        groupe.ajouterRelation(new Personne("A"), new Personne("B"));
        Affectation affectation = affectation(groupe);
        assertEquals(1, calculateur.calculerCout(groupe, affectation));
        groupe.definirPreferences(new Personne("B"), new Preferences(List.of(new Chambre(2), new Chambre(1))));
        assertEquals(0, calculateur.calculerCout(groupe, affectation));
    }

    /** Les trois entrées publiques refusent un groupe incomplet, même sans relations. */
    @Test
    void refuseGroupeIncomplet() {
        Groupe groupe = new Groupe(List.of(new Personne("A"), new Personne("B")),
                List.of(new Chambre(1), new Chambre(2)));
        groupe.definirPreferences(new Personne("A"), new Preferences(groupe.getChambres()));
        Affectation affectation = affectation(groupe);
        assertAll(
            () -> assertThrows(IllegalStateException.class, () -> calculateur.estJalouse(new Personne("A"), groupe, affectation)),
            () -> assertThrows(IllegalStateException.class, () -> calculateur.personnesJalouses(groupe, affectation)),
            () -> assertThrows(IllegalStateException.class, () -> calculateur.calculerCout(groupe, affectation))
        );
    }

    /** Des arguments nuls sont rejetés avec l'exception annoncée. */
    @Test
    void refuseArgumentsNuls() {
        Groupe groupe = groupeComplet();
        Affectation affectation = affectation(groupe);
        Personne a = new Personne("A");
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.estJalouse(a, null, affectation)),
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.estJalouse(a, groupe, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.estJalouse(null, groupe, affectation)),
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.personnesJalouses(null, affectation)),
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.personnesJalouses(groupe, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.calculerCout(null, affectation)),
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.calculerCout(groupe, null))
        );
    }

    /** Une personne inconnue ne peut être évaluée, même si elle n'a aucun voisin. */
    @Test
    void refusePersonneInconnue() {
        Groupe groupe = groupeComplet();
        assertThrows(IllegalArgumentException.class,
                () -> calculateur.estJalouse(new Personne("C"), groupe, affectation(groupe)));
    }

    /** Un même nombre de personnes ne suffit pas à rendre une affectation compatible. */
    @Test
    void refuseAffectationIncompatible() {
        Groupe groupe = groupeComplet();
        Groupe autre = new Groupe(List.of(new Personne("A"), new Personne("C")), groupe.getChambres());
        Affectation affectation = new Affectation(autre,
                Map.of(new Personne("A"), new Chambre(1), new Personne("C"), new Chambre(2)));
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.estJalouse(new Personne("A"), groupe, affectation)),
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.personnesJalouses(groupe, affectation)),
            () -> assertThrows(IllegalArgumentException.class, () -> calculateur.calculerCout(groupe, affectation))
        );
    }
}
