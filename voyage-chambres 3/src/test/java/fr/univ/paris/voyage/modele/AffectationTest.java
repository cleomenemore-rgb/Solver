package fr.univ.paris.voyage.modele;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Vérifie les affectations complètes, les échanges et la protection des données. */
class AffectationTest {

    /** @return groupe A, B et chambres 1, 2, sans préférences */
    private Groupe groupe() {
        return new Groupe(List.of(new Personne("A"), new Personne("B")),
                List.of(new Chambre(1), new Chambre(2)));
    }

    /** @return attribution A vers 1 et B vers 2 */
    private Map<Personne, Chambre> attribution() {
        return Map.of(new Personne("A"), new Chambre(1), new Personne("B"), new Chambre(2));
    }

    /** Une affectation dépend des membres et chambres, pas encore des préférences. */
    @Test
    void creeAffectationCompleteSansPreferences() {
        Groupe groupe = groupe();
        Affectation affectation = new Affectation(groupe, attribution());
        assertFalse(groupe.estComplet());
        assertTrue(affectation.estValidePour(groupe));
        assertEquals(new Chambre(1), affectation.chambreDe(new Personne("A")));
        assertEquals(new Chambre(2), affectation.chambreDe(new Personne("B")));
    }

    /** Le cas à une seule personne est accepté. */
    @Test
    void accepteGroupeMinimal() {
        Groupe groupe = new Groupe(List.of(new Personne("A")), List.of(new Chambre(1)));
        Affectation affectation = new Affectation(groupe, Map.of(new Personne("A"), new Chambre(1)));
        affectation.echanger(new Personne("A"), new Personne("A"));
        assertTrue(affectation.estValidePour(groupe));
        assertEquals(new Chambre(1), affectation.chambreDe(new Personne("A")));
    }

    /** Les arguments indispensables sont contrôlés. */
    @Test
    void refuseArgumentsNuls() {
        assertThrows(IllegalArgumentException.class, () -> new Affectation(null, attribution()));
        assertThrows(IllegalArgumentException.class, () -> new Affectation(groupe(), null));
    }

    /** Les personnes manquantes et les affectations vides sont refusées. */
    @Test
    void refuseAffectationIncomplete() {
        assertThrows(IllegalArgumentException.class, () -> new Affectation(groupe(), Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Affectation(groupe(), Map.of(new Personne("A"), new Chambre(1))));
    }

    /** Une personne étrangère ne peut ni remplacer un membre ni s'y ajouter. */
    @Test
    void refusePersonneEtrangere() {
        Map<Personne, Chambre> valeurs = new HashMap<>(attribution());
        valeurs.remove(new Personne("B"));
        valeurs.put(new Personne("C"), new Chambre(2));
        assertThrows(IllegalArgumentException.class, () -> new Affectation(groupe(), valeurs));
        valeurs.put(new Personne("B"), new Chambre(2));
        assertThrows(IllegalArgumentException.class, () -> new Affectation(groupe(), valeurs));
    }

    /** Une chambre ne peut être donnée à deux personnes, même avec deux instances. */
    @Test
    void refuseChambreDupliquee() {
        assertThrows(IllegalArgumentException.class, () -> new Affectation(groupe(),
                Map.of(new Personne("A"), new Chambre(1), new Personne("B"), new Chambre(1))));
    }

    /** Une chambre absente du groupe ne peut être attribuée. */
    @Test
    void refuseChambreEtrangere() {
        assertThrows(IllegalArgumentException.class, () -> new Affectation(groupe(),
                Map.of(new Personne("A"), new Chambre(1), new Personne("B"), new Chambre(3))));
    }

    /** Les maps ordinaires autorisent null, mais l'affectation le refuse. */
    @Test
    void refuseElementsNuls() {
        Map<Personne, Chambre> valeurs = new HashMap<>(attribution());
        valeurs.put(new Personne("A"), null);
        assertThrows(IllegalArgumentException.class, () -> new Affectation(groupe(), valeurs));
        valeurs.clear();
        valeurs.put(null, new Chambre(1));
        valeurs.put(new Personne("B"), new Chambre(2));
        assertThrows(IllegalArgumentException.class, () -> new Affectation(groupe(), valeurs));
    }

    /** Ni les échanges ni les modifications de la map source ne se propagent entre les deux. */
    @Test
    void copieAttributionRecue() {
        Map<Personne, Chambre> source = new HashMap<>(attribution());
        Affectation affectation = new Affectation(groupe(), source);
        affectation.echanger(new Personne("A"), new Personne("B"));
        assertEquals(attribution(), source);
        source.clear();
        assertEquals(new Chambre(2), affectation.chambreDe(new Personne("A")));
        assertTrue(affectation.estValidePour(groupe()));
    }

    /** Deux échanges successifs rétablissent l'affectation initiale. */
    @Test
    void echangePuisRetablitChambres() {
        Groupe groupe = groupe();
        Affectation affectation = new Affectation(groupe, attribution());
        affectation.echanger(new Personne("A"), new Personne("B"));
        assertEquals(new Chambre(2), affectation.chambreDe(new Personne("A")));
        assertEquals(new Chambre(1), affectation.chambreDe(new Personne("B")));
        assertTrue(affectation.estValidePour(groupe));
        affectation.echanger(new Personne("A"), new Personne("B"));
        assertEquals(new Chambre(1), affectation.chambreDe(new Personne("A")));
        assertEquals(new Chambre(2), affectation.chambreDe(new Personne("B")));
    }

    /** Un échange avec soi-même est sans effet pour une personne connue. */
    @Test
    void echangeAvecSoiMemeSansEffet() {
        Affectation affectation = new Affectation(groupe(), attribution());
        affectation.echanger(new Personne("A"), new Personne("A"));
        assertEquals(new Chambre(1), affectation.chambreDe(new Personne("A")));
        assertEquals(new Chambre(2), affectation.chambreDe(new Personne("B")));
    }

    /** Les deux paramètres sont validés avant tout changement de chambre. */
    @Test
    void refuseEchangeInvalideSansModification() {
        Affectation affectation = new Affectation(groupe(), attribution());
        Personne a = new Personne("A");
        Personne inconnue = new Personne("C");
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> affectation.echanger(a, inconnue)),
            () -> assertThrows(IllegalArgumentException.class, () -> affectation.echanger(inconnue, a)),
            () -> assertThrows(IllegalArgumentException.class, () -> affectation.echanger(a, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> affectation.echanger(null, a)),
            () -> assertThrows(IllegalArgumentException.class, () -> affectation.echanger(inconnue, inconnue))
        );
        assertEquals(new Chambre(1), affectation.chambreDe(a));
        assertEquals(new Chambre(2), affectation.chambreDe(new Personne("B")));
    }

    /** Une recherche invalide ne renvoie pas silencieusement null. */
    @Test
    void refuseRechercheInconnue() {
        Affectation affectation = new Affectation(groupe(), attribution());
        assertThrows(IllegalArgumentException.class, () -> affectation.chambreDe(new Personne("C")));
        assertThrows(IllegalArgumentException.class, () -> affectation.chambreDe(null));
    }

    /** La compatibilité utilise les identifiants, pas l'identité de l'objet Groupe. */
    @Test
    void verifieCompatibiliteDesGroupes() {
        Affectation affectation = new Affectation(groupe(), attribution());
        assertTrue(affectation.estValidePour(groupe()));
        assertFalse(affectation.estValidePour(null));
        assertFalse(affectation.estValidePour(new Groupe(
                List.of(new Personne("A"), new Personne("C")), List.of(new Chambre(1), new Chambre(2)))));
        assertFalse(affectation.estValidePour(new Groupe(List.of(new Personne("A")), List.of(new Chambre(1)))));
    }

    /** Deux solutions du même groupe sont indépendantes. */
    @Test
    void conserveIndependanceDesSolutions() {
        Groupe groupe = groupe();
        Affectation premiere = new Affectation(groupe, attribution());
        Affectation seconde = new Affectation(groupe, attribution());
        premiere.echanger(new Personne("A"), new Personne("B"));
        assertEquals(new Chambre(1), seconde.chambreDe(new Personne("A")));
        assertEquals(new Chambre(2), seconde.chambreDe(new Personne("B")));
        assertTrue(groupe.getVoisins(new Personne("A")).isEmpty());
    }
}
