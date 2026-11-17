package fr.univ.paris.voyage.algorithme;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import fr.univ.paris.voyage.modele.*;

/** Vérifie les choix gloutons, les validations et l'absence d'état partagé. */
class SolveurNaifTest {

    /** Solveur réutilisé au sein de chaque scénario. */
    private final SolveurAffectation solveur = new SolveurNaif();

    /**
     * Construit un groupe dont chaque personne possède le classement fourni.
     * @param noms ordre de passage des personnes
     * @param classements préférences par personne
     * @return groupe complet
     */
    private Groupe groupe(List<String> noms, int[]... classements) {
        List<Personne> personnes = noms.stream().map(Personne::new).toList();
        List<Chambre> chambres = new ArrayList<>();
        for (int i = 1; i <= noms.size(); i++) {
            chambres.add(new Chambre(i));
        }
        Groupe groupe = new Groupe(personnes, chambres);
        for (int i = 0; i < noms.size(); i++) {
            List<Chambre> classement = new ArrayList<>();
            for (int numero : classements[i]) {
                classement.add(new Chambre(numero));
            }
            groupe.definirPreferences(personnes.get(i), new Preferences(classement));
        }
        return groupe;
    }

    /** Une personne seule reçoit l'unique chambre. */
    @Test
    void resoutGroupeMinimal() {
        Groupe g = groupe(List.of("A"), new int[]{1});
        Affectation a = solveur.resoudre(g);
        assertEquals(new Chambre(1), a.chambreDe(g.getPersonne("A")));
        assertTrue(a.estValidePour(g));
    }

    /** Des premiers choix distincts sont tous respectés, sans tri numérique. */
    @Test
    void respectePremiersChoixDistincts() {
        Groupe g = groupe(List.of("A", "B", "C"),
                new int[]{3, 1, 2}, new int[]{1, 2, 3}, new int[]{2, 3, 1});
        Affectation a = solveur.resoudre(g);
        assertEquals(new Chambre(3), a.chambreDe(g.getPersonne("A")));
        assertEquals(new Chambre(1), a.chambreDe(g.getPersonne("B")));
        assertEquals(new Chambre(2), a.chambreDe(g.getPersonne("C")));
        assertTrue(a.estValidePour(g));
    }

    /** Les chambres occupées sont ignorées jusqu'au troisième choix si nécessaire. */
    @Test
    void prendMeilleureChambreRestante() {
        Groupe g = groupe(List.of("A", "B", "C"),
                new int[]{2, 1, 3}, new int[]{2, 3, 1}, new int[]{3, 2, 1});
        Affectation a = solveur.resoudre(g);
        assertEquals(new Chambre(2), a.chambreDe(g.getPersonne("A")));
        assertEquals(new Chambre(3), a.chambreDe(g.getPersonne("B")));
        assertEquals(new Chambre(1), a.chambreDe(g.getPersonne("C")));
        assertTrue(a.estValidePour(g));
    }

    /** L'ordre du groupe est respecté même s'il n'est pas alphabétique. */
    @Test
    void utiliseOrdreDuGroupe() {
        Groupe g = groupe(List.of("B", "A"), new int[]{1, 2}, new int[]{1, 2});
        Affectation a = solveur.resoudre(g);
        assertEquals(new Chambre(1), a.chambreDe(g.getPersonne("B")));
        assertEquals(new Chambre(2), a.chambreDe(g.getPersonne("A")));
    }

    /** Le cas de classements identiques atteint la borne du parcours pour 26 personnes. */
    @Test
    void attribueToutesLesChambresSurVingtSixClassementsIdentiques() {
        List<String> noms = new ArrayList<>();
        int[][] classements = new int[26][26];
        for (int i = 0; i < 26; i++) {
            noms.add(String.valueOf((char) ('A' + i)));
            for (int j = 0; j < 26; j++) {
                classements[i][j] = j + 1;
            }
        }
        Groupe g = groupe(noms, classements);
        Affectation a = solveur.resoudre(g);
        for (int i = 0; i < 26; i++) {
            assertEquals(new Chambre(i + 1), a.chambreDe(g.getPersonne(noms.get(i))));
        }
        assertTrue(a.estValidePour(g));
    }

    /** Une résolution ne modifie ni préférences, ni relations, ni ordre. */
    @Test
    void conserveDonneesDuGroupe() {
        Groupe g = groupe(List.of("B", "A"), new int[]{2, 1}, new int[]{2, 1});
        Personne a = g.getPersonne("A");
        Personne b = g.getPersonne("B");
        g.ajouterRelation(a, b);
        List<Personne> personnes = g.getPersonnes();
        List<Chambre> chambres = g.getChambres();
        List<Chambre> classement = g.getPreferences(a).getClassement();
        var voisins = g.getVoisins(a);
        solveur.resoudre(g);
        assertEquals(personnes, g.getPersonnes());
        assertEquals(chambres, g.getChambres());
        assertEquals(classement, g.getPreferences(a).getClassement());
        assertEquals(List.of(new Chambre(2), new Chambre(1)), g.getPreferences(b).getClassement());
        assertEquals(voisins, g.getVoisins(a));
        assertTrue(g.estComplet());
    }

    /** Les appels sont déterministes et les échanges ne contaminent pas les autres résultats. */
    @Test
    void produitAffectationsIndependantes() {
        Groupe g = groupe(List.of("A", "B"), new int[]{1, 2}, new int[]{1, 2});
        Affectation premiere = solveur.resoudre(g);
        Affectation seconde = solveur.resoudre(g);
        assertNotSame(premiere, seconde);
        for (Personne personne : g.getPersonnes()) {
            assertEquals(premiere.chambreDe(personne), seconde.chambreDe(personne));
        }
        premiere.echanger(g.getPersonne("A"), g.getPersonne("B"));
        assertEquals(new Chambre(1), seconde.chambreDe(g.getPersonne("A")));
        assertEquals(new Chambre(1), solveur.resoudre(g).chambreDe(g.getPersonne("A")));
        Groupe autre = groupe(List.of("Z"), new int[]{1});
        assertTrue(solveur.resoudre(autre).estValidePour(autre));
    }

    /** Le groupe nul est refusé explicitement. */
    @Test
    void refuseGroupeNul() {
        assertThrows(IllegalArgumentException.class, () -> solveur.resoudre(null));
    }

    /** Un groupe incomplet est refusé, puis accepté une fois configuré. */
    @Test
    void refusePreferencesAbsentesPuisAccepteGroupeComplete() {
        Personne a = new Personne("A");
        Personne b = new Personne("B");
        Groupe g = new Groupe(List.of(a, b), List.of(new Chambre(1), new Chambre(2)));
        assertThrows(IllegalStateException.class, () -> solveur.resoudre(g));
        g.definirPreferences(a, new Preferences(g.getChambres()));
        assertThrows(IllegalStateException.class, () -> solveur.resoudre(g));
        assertEquals(List.of(b), g.personnesSansPreferences());
        g.definirPreferences(b, new Preferences(g.getChambres()));
        assertTrue(solveur.resoudre(g).estValidePour(g));
    }
}
