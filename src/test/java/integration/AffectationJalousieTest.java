package integration;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import modele.Affectation;
import modele.Chambre;
import modele.Groupe;
import modele.Personne;
import modele.Preferences;
import service.CalculateurJalousie;

/** Assemble le modèle complet et le calcul de jalousie sur des scénarios du sujet. */
class AffectationJalousieTest {

    /** @return groupe A/B/C/D avec les relations et préférences exactes du sujet */
    private Groupe groupeDuSujet() {
        Groupe groupe = new Groupe(
                List.of(new Personne("A"), new Personne("B"), new Personne("C"), new Personne("D")),
                List.of(new Chambre(1), new Chambre(2), new Chambre(3), new Chambre(4)));
        Personne b = groupe.getPersonne("B");
        for (String nom : List.of("A", "C", "D")) {
            groupe.ajouterRelation(b, groupe.getPersonne(nom));
        }
        groupe.definirPreferences(groupe.getPersonne("A"), preferences(1, 2, 3, 4));
        groupe.definirPreferences(b, preferences(1, 3, 2, 4));
        groupe.definirPreferences(groupe.getPersonne("C"), preferences(3, 2, 1, 4));
        groupe.definirPreferences(groupe.getPersonne("D"), preferences(1, 4, 2, 3));
        return groupe;
    }

    /**
     * Traduit un classement numérique en objets métier pour les exemples.
     * @param numeros numéros dans l'ordre des préférences
     * @return classement correspondant
     */
    private Preferences preferences(int... numeros) {
        List<Chambre> chambres = new ArrayList<>();
        for (int numero : numeros) {
            chambres.add(new Chambre(numero));
        }
        return new Preferences(chambres);
    }

    /** Les deux figures du sujet sont reliées par l'échange des chambres de A et B. */
    @Test
    void reproduitCoutsUnEtDeuxDuSujet() {
        Groupe groupe = groupeDuSujet();
        Personne a = groupe.getPersonne("A");
        Personne b = groupe.getPersonne("B");
        Personne c = groupe.getPersonne("C");
        Personne d = groupe.getPersonne("D");
        Affectation affectation = new Affectation(groupe,
                Map.of(a, new Chambre(1), b, new Chambre(2), c, new Chambre(3), d, new Chambre(4)));
        CalculateurJalousie calculateur = new CalculateurJalousie();
        assertEquals(Set.of(b), calculateur.personnesJalouses(groupe, affectation));
        assertEquals(1, calculateur.calculerCout(groupe, affectation));

        affectation.echanger(a, b);
        assertEquals(Set.of(a, d), calculateur.personnesJalouses(groupe, affectation));
        assertEquals(List.of(a, d), new ArrayList<>(calculateur.personnesJalouses(groupe, affectation)));
        assertEquals(2, calculateur.calculerCout(groupe, affectation));
        assertEquals(new Chambre(2), affectation.chambreDe(a));
        assertEquals(new Chambre(1), affectation.chambreDe(b));
        assertEquals(new Chambre(3), affectation.chambreDe(c));
        assertEquals(new Chambre(4), affectation.chambreDe(d));
        assertTrue(affectation.estValidePour(groupe));

        affectation.echanger(a, b);
        assertEquals(Set.of(b), calculateur.personnesJalouses(groupe, affectation));
        assertEquals(1, calculateur.calculerCout(groupe, affectation));
    }

    /** Une erreur d'échange laisse la solution et son coût inchangés. */
    @Test
    void conserveSolutionApresEchangeRefuse() {
        Groupe groupe = groupeDuSujet();
        Affectation affectation = new Affectation(groupe, Map.of(
                new Personne("A"), new Chambre(1), new Personne("B"), new Chambre(2),
                new Personne("C"), new Chambre(3), new Personne("D"), new Chambre(4)));
        assertThrows(IllegalArgumentException.class,
                () -> affectation.echanger(groupe.getPersonne("A"), new Personne("Z")));
        CalculateurJalousie calculateur = new CalculateurJalousie();
        assertEquals(1, calculateur.calculerCout(groupe, affectation));
        assertEquals(new Chambre(1), affectation.chambreDe(groupe.getPersonne("A")));
        assertTrue(affectation.estValidePour(groupe));
    }

    /** Une personne seule n'a aucun voisin et ne peut être jalouse. */
    @Test
    void coutNulPourGroupeMinimal() {
        Personne a = new Personne("A");
        Chambre chambre = new Chambre(1);
        Groupe groupe = new Groupe(List.of(a), List.of(chambre));
        groupe.definirPreferences(a, new Preferences(groupe.getChambres()));
        Affectation affectation = new Affectation(groupe, Map.of(a, chambre));
        CalculateurJalousie calculateur = new CalculateurJalousie();
        assertFalse(calculateur.estJalouse(a, groupe, affectation));
        assertTrue(calculateur.personnesJalouses(groupe, affectation).isEmpty());
        assertEquals(0, calculateur.calculerCout(groupe, affectation));
    }
}
