package fr.univ.paris.voyage.modele;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Vérifie les règles du groupe et la conservation de son état après une erreur. */
class GroupeTest {

    /**
     * Construit un petit groupe neuf pour éviter tout état partagé entre tests.
     * @return groupe A, B, C et chambres 1, 2, 3
     */
    private Groupe creerGroupe() {
        return new Groupe(List.of(new Personne("A"), new Personne("B"), new Personne("C")),
                List.of(new Chambre(1), new Chambre(2), new Chambre(3)));
    }

    /** Vérifie que les recherches ne supposent pas les listes triées. */
    @Test
    void conserveOrdreEtRechercheParIdentifiant() {
        Personne b = new Personne("B");
        Chambre deux = new Chambre(2);
        Groupe groupe = new Groupe(List.of(b, new Personne("A")), List.of(deux, new Chambre(1)));
        assertEquals(List.of(b, new Personne("A")), groupe.getPersonnes());
        assertEquals(List.of(deux, new Chambre(1)), groupe.getChambres());
        assertSame(b, groupe.getPersonne("B"));
        assertSame(deux, groupe.getChambre(2));
    }

    /** Le modèle accepte une personne, la borne 26 et un groupe plus grand. */
    @ParameterizedTest
    @ValueSource(ints = {1, 26, 27})
    void accepteTaillesDuModele(int taille) {
        List<Personne> personnes = new ArrayList<>();
        List<Chambre> chambres = new ArrayList<>();
        for (int i = 1; i <= taille; i++) {
            personnes.add(new Personne("P" + i));
            chambres.add(new Chambre(i));
        }
        Groupe groupe = new Groupe(personnes, chambres);
        assertEquals(taille, groupe.getPersonnes().size());
        assertEquals(taille, groupe.getChambres().size());
        assertFalse(groupe.estComplet());
    }

    /** Refuse des listes nulles, vides ou de tailles différentes. */
    @Test
    void refuseListesInvalides() {
        List<Personne> personnes = List.of(new Personne("A"));
        List<Chambre> chambres = List.of(new Chambre(1));
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> new Groupe(null, chambres)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Groupe(personnes, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> new Groupe(List.of(), List.of())),
            () -> assertThrows(IllegalArgumentException.class, () -> new Groupe(personnes, List.of())),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> new Groupe(personnes, List.of(new Chambre(1), new Chambre(2))))
        );
    }

    /** Refuse les éléments nuls dans les listes. */
    @Test
    void refuseElementsNuls() {
        assertThrows(IllegalArgumentException.class,
                () -> new Groupe(Arrays.asList(new Personne("A"), null), List.of(new Chambre(1), new Chambre(2))));
        assertThrows(IllegalArgumentException.class,
                () -> new Groupe(List.of(new Personne("A")), Arrays.asList((Chambre) null)));
    }

    /** Refuse les doublons logiques de personnes et de chambres. */
    @Test
    void refuseIdentifiantsDupliques() {
        assertThrows(IllegalArgumentException.class,
                () -> new Groupe(List.of(new Personne("A"), new Personne("A")),
                        List.of(new Chambre(1), new Chambre(2))));
        assertThrows(IllegalArgumentException.class,
                () -> new Groupe(List.of(new Personne("A"), new Personne("B")),
                        List.of(new Chambre(1), new Chambre(1))));
    }

    /** Refuse un numéro hors du périmètre 1 à n. */
    @Test
    void refuseChambresHorsPerimetre() {
        assertThrows(IllegalArgumentException.class,
                () -> new Groupe(List.of(new Personne("A"), new Personne("B")),
                        List.of(new Chambre(1), new Chambre(3))));
    }

    /** Les listes fournies au constructeur ne permettent pas de modifier le groupe. */
    @Test
    void copieListesRecues() {
        List<Personne> personnes = new ArrayList<>(List.of(new Personne("A")));
        List<Chambre> chambres = new ArrayList<>(List.of(new Chambre(1)));
        Groupe groupe = new Groupe(personnes, chambres);
        personnes.clear();
        chambres.clear();
        assertEquals(List.of(new Personne("A")), groupe.getPersonnes());
        assertEquals(List.of(new Chambre(1)), groupe.getChambres());
        assertTrue(groupe.getVoisins(new Personne("A")).isEmpty());
    }

    /** Les listes retournées ne permettent pas d'altérer les membres ou les chambres. */
    @Test
    void protegeListesRetournees() {
        Groupe groupe = creerGroupe();
        assertThrows(UnsupportedOperationException.class, () -> groupe.getPersonnes().clear());
        assertThrows(UnsupportedOperationException.class, () -> groupe.getChambres().clear());
    }

    /** Les recherches invalides signalent une erreur compréhensible. */
    @Test
    void refuseRecherchesInconnues() {
        Groupe groupe = creerGroupe();
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.getPersonne("D")),
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.getPersonne("a")),
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.getPersonne(null)),
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.getChambre(0)),
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.getChambre(4))
        );
    }

    /** Une seule opération crée la relation dans les deux sens, sans doublon. */
    @Test
    void ajouteRelationSymetriqueSansDoublon() {
        Groupe groupe = creerGroupe();
        Personne a = new Personne("A");
        Personne b = new Personne("B");
        groupe.ajouterRelation(a, b);
        groupe.ajouterRelation(a, b);
        groupe.ajouterRelation(b, a);
        assertEquals(Set.of(b), groupe.getVoisins(a));
        assertEquals(Set.of(a), groupe.getVoisins(b));
        assertTrue(groupe.getVoisins(new Personne("C")).isEmpty());
    }

    /** Toute relation invalide est rejetée avant la première modification. */
    @Test
    void refuseRelationInvalideSansModification() {
        Groupe groupe = creerGroupe();
        Personne a = new Personne("A");
        Personne b = new Personne("B");
        Personne inconnue = new Personne("D");
        groupe.ajouterRelation(a, b);
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.ajouterRelation(a, inconnue)),
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.ajouterRelation(inconnue, a)),
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.ajouterRelation(a, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.ajouterRelation(null, a)),
            () -> assertThrows(IllegalArgumentException.class, () -> groupe.ajouterRelation(a, new Personne("A")))
        );
        assertEquals(Set.of(b), groupe.getVoisins(a));
        assertEquals(Set.of(a), groupe.getVoisins(b));
        assertTrue(groupe.getVoisins(new Personne("C")).isEmpty());
    }

    /** Les voisins exposés sont un instantané protégé, dans l'ordre d'ajout. */
    @Test
    void protegeInstantaneDesVoisins() {
        Groupe groupe = creerGroupe();
        Personne a = new Personne("A");
        groupe.ajouterRelation(a, new Personne("C"));
        Set<Personne> avant = groupe.getVoisins(a);
        assertThrows(UnsupportedOperationException.class, () -> avant.add(new Personne("B")));
        groupe.ajouterRelation(a, new Personne("B"));
        assertEquals(Set.of(new Personne("C")), avant);
        assertEquals(List.of(new Personne("C"), new Personne("B")), new ArrayList<>(groupe.getVoisins(a)));
    }

    /** Une personne absente n'a pas d'ensemble de voisins consultable. */
    @Test
    void refuseVoisinsPersonneInconnue() {
        Groupe groupe = creerGroupe();
        assertThrows(IllegalArgumentException.class, () -> groupe.getVoisins(new Personne("D")));
        assertThrows(IllegalArgumentException.class, () -> groupe.getVoisins(null));
    }

    /** L'absence de préférences est distincte d'une personne inconnue. */
    @Test
    void signalePreferencesAbsentes() {
        Groupe groupe = creerGroupe();
        assertThrows(IllegalStateException.class, () -> groupe.getPreferences(new Personne("A")));
        assertThrows(IllegalArgumentException.class, () -> groupe.getPreferences(new Personne("D")));
        assertThrows(IllegalArgumentException.class, () -> groupe.getPreferences(null));
        assertEquals(groupe.getPersonnes(), groupe.personnesSansPreferences());
    }

    /** Autorise le remplacement par un autre classement complet. */
    @Test
    void enregistreEtRemplacePreferences() {
        Groupe groupe = creerGroupe();
        Personne a = new Personne("A");
        Preferences initiales = new Preferences(groupe.getChambres());
        Preferences nouvelles = new Preferences(List.of(new Chambre(3), new Chambre(2), new Chambre(1)));
        groupe.definirPreferences(a, initiales);
        assertSame(initiales, groupe.getPreferences(a));
        groupe.definirPreferences(a, nouvelles);
        assertSame(nouvelles, groupe.getPreferences(a));
        assertEquals(List.of(new Personne("B"), new Personne("C")), groupe.personnesSansPreferences());
    }

    /** Refuse les classements incomplets, trop longs ou contenant une chambre étrangère. */
    @Test
    void conserveAnciennesPreferencesSiRemplacementInvalide() {
        Groupe groupe = creerGroupe();
        Personne a = new Personne("A");
        Preferences initiales = new Preferences(groupe.getChambres());
        groupe.definirPreferences(a, initiales);
        List<Preferences> invalides = List.of(
                new Preferences(List.of(new Chambre(1), new Chambre(2))),
                new Preferences(List.of(new Chambre(1), new Chambre(2), new Chambre(4))),
                new Preferences(List.of(new Chambre(1), new Chambre(2), new Chambre(3), new Chambre(4))));
        for (Preferences preferences : invalides) {
            assertThrows(IllegalArgumentException.class, () -> groupe.definirPreferences(a, preferences));
            assertSame(initiales, groupe.getPreferences(a));
        }
        assertThrows(IllegalArgumentException.class, () -> groupe.definirPreferences(a, null));
        assertSame(initiales, groupe.getPreferences(a));
    }

    /** Un échec de première définition laisse la personne dans la liste des absences. */
    @Test
    void neValidePasUneDefinitionInvalide() {
        Groupe groupe = creerGroupe();
        Preferences incompletes = new Preferences(List.of(new Chambre(1)));
        assertThrows(IllegalArgumentException.class,
                () -> groupe.definirPreferences(new Personne("A"), incompletes));
        assertFalse(groupe.estComplet());
        assertEquals(groupe.getPersonnes(), groupe.personnesSansPreferences());
    }

    /** Refuse l'attribution à une personne absente, sans compléter le groupe. */
    @Test
    void refusePreferencesPourPersonneInconnue() {
        Groupe groupe = creerGroupe();
        Preferences preferences = new Preferences(groupe.getChambres());
        assertThrows(IllegalArgumentException.class,
                () -> groupe.definirPreferences(new Personne("D"), preferences));
        assertThrows(IllegalArgumentException.class, () -> groupe.definirPreferences(null, preferences));
        assertEquals(groupe.getPersonnes(), groupe.personnesSansPreferences());
    }

    /** La liste des absences est une copie protégée et conserve l'ordre des personnes. */
    @Test
    void protegeListePersonnesSansPreferences() {
        Groupe groupe = creerGroupe();
        List<Personne> avant = groupe.personnesSansPreferences();
        assertThrows(UnsupportedOperationException.class, avant::clear);
        groupe.definirPreferences(new Personne("B"), new Preferences(groupe.getChambres()));
        assertEquals(groupe.getPersonnes(), avant);
        assertEquals(List.of(new Personne("A"), new Personne("C")), groupe.personnesSansPreferences());
    }

    /** Les relations ne sont pas obligatoires ; seules les préférences doivent être complètes. */
    @Test
    void devientCompletApresDernieresPreferences() {
        Groupe groupe = creerGroupe();
        Preferences preferences = new Preferences(groupe.getChambres());
        groupe.definirPreferences(new Personne("A"), preferences);
        groupe.definirPreferences(new Personne("B"), preferences);
        assertFalse(groupe.estComplet());
        groupe.definirPreferences(new Personne("C"), preferences);
        assertTrue(groupe.estComplet());
        assertTrue(groupe.personnesSansPreferences().isEmpty());
        assertTrue(groupe.getVoisins(new Personne("A")).isEmpty());
    }
}
