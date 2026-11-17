package fr.univ.paris.voyage.integration;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import fr.univ.paris.voyage.algorithme.SolveurAffectation;
import fr.univ.paris.voyage.algorithme.SolveurNaif;
import fr.univ.paris.voyage.modele.*;
import fr.univ.paris.voyage.service.CalculateurJalousie;

/** Relie configuration, résolution automatique, calcul et échange manuel. */
class ResolutionNaiveTest {

    /**
     * Construit les préférences d'un exemple à partir de numéros.
     * @param numeros chambres classées
     * @return préférences correspondantes
     */
    private Preferences preferences(int... numeros) {
        return new Preferences(Arrays.stream(numeros).mapToObj(Chambre::new).toList());
    }

    /** L'exemple du sujet passe du coût 2 au coût 1 après échange de B et C. */
    @Test
    void resoutPuisAmelioreManuellementExempleDuSujet() {
        Personne a = new Personne("A");
        Personne b = new Personne("B");
        Personne c = new Personne("C");
        Personne d = new Personne("D");
        Groupe g = new Groupe(List.of(a, b, c, d),
                List.of(new Chambre(1), new Chambre(2), new Chambre(3), new Chambre(4)));
        for (Personne voisin : List.of(a, c, d)) {
            g.ajouterRelation(b, voisin);
        }
        g.definirPreferences(a, preferences(1, 2, 3, 4));
        g.definirPreferences(b, preferences(1, 3, 2, 4));
        g.definirPreferences(c, preferences(3, 2, 1, 4));
        g.definirPreferences(d, preferences(1, 4, 2, 3));
        SolveurAffectation solveur = new SolveurNaif();
        Affectation resultat = solveur.resoudre(g);
        CalculateurJalousie calculateur = new CalculateurJalousie();
        assertEquals(new Chambre(1), resultat.chambreDe(a));
        assertEquals(new Chambre(3), resultat.chambreDe(b));
        assertEquals(new Chambre(2), resultat.chambreDe(c));
        assertEquals(new Chambre(4), resultat.chambreDe(d));
        assertEquals(Set.of(b, c), calculateur.personnesJalouses(g, resultat));
        assertEquals(2, calculateur.calculerCout(g, resultat));
        resultat.echanger(b, c);
        assertEquals(Set.of(b), calculateur.personnesJalouses(g, resultat));
        assertEquals(1, calculateur.calculerCout(g, resultat));
        assertTrue(resultat.estValidePour(g));
        assertEquals(2, calculateur.calculerCout(g, solveur.resoudre(g)));
    }

    /** Sans relation, même des premiers choix identiques ne créent aucune jalousie. */
    @Test
    void coutNulSansRelationApresResolution() {
        Personne a = new Personne("A");
        Personne b = new Personne("B");
        Groupe g = new Groupe(List.of(a, b), List.of(new Chambre(1), new Chambre(2)));
        g.definirPreferences(a, preferences(1, 2));
        g.definirPreferences(b, preferences(1, 2));
        Affectation resultat = new SolveurNaif().resoudre(g);
        assertTrue(resultat.estValidePour(g));
        assertEquals(0, new CalculateurJalousie().calculerCout(g, resultat));
    }
}
