package fr.univ.paris.voyage.modele;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Vérifie le classement, les comparaisons et la protection des préférences. */
class PreferencesTest {

    /** Vérifie les rangs à partir de zéro, y compris avec des objets équivalents. */
    @Test
    void conserveOrdreEtCalculeRangs() {
        Preferences preferences = new Preferences(List.of(new Chambre(3), new Chambre(1), new Chambre(2)));
        assertAll(
            () -> assertEquals(List.of(new Chambre(3), new Chambre(1), new Chambre(2)), preferences.getClassement()),
            () -> assertEquals(0, preferences.rangDe(new Chambre(3))),
            () -> assertEquals(1, preferences.rangDe(new Chambre(1))),
            () -> assertEquals(2, preferences.rangDe(new Chambre(2)))
        );
    }

    /** Vérifie que le classement, et non le numéro, détermine la préférence. */
    @Test
    void compareStrictementDeuxChambres() {
        Preferences preferences = new Preferences(List.of(new Chambre(3), new Chambre(1), new Chambre(2)));
        assertTrue(preferences.prefere(new Chambre(3), new Chambre(1)));
        assertFalse(preferences.prefere(new Chambre(2), new Chambre(1)));
        assertFalse(preferences.prefere(new Chambre(1), new Chambre(1)));
    }

    /** Le cas minimal reste un ordre strict valide. */
    @Test
    void accepteUneSeuleChambre() {
        Preferences preferences = new Preferences(List.of(new Chambre(1)));
        assertEquals(0, preferences.rangDe(new Chambre(1)));
        assertFalse(preferences.prefere(new Chambre(1), new Chambre(1)));
    }

    /** Refuse une liste absente ou vide. */
    @Test
    void refuseClassementAbsent() {
        assertThrows(IllegalArgumentException.class, () -> new Preferences(null));
        assertThrows(IllegalArgumentException.class, () -> new Preferences(List.of()));
    }

    /** Refuse un élément nul au milieu du classement. */
    @Test
    void refuseChambreNulle() {
        assertThrows(IllegalArgumentException.class,
                () -> new Preferences(Arrays.asList(new Chambre(1), null)));
    }

    /** Détecte un doublon de numéro même porté par deux objets différents. */
    @Test
    void refuseDoublonLogique() {
        assertThrows(IllegalArgumentException.class,
                () -> new Preferences(List.of(new Chambre(1), new Chambre(2), new Chambre(1))));
    }

    /** Signale explicitement une chambre non classée ou nulle. */
    @Test
    void refuseRangInconnu() {
        Preferences preferences = new Preferences(List.of(new Chambre(1)));
        assertThrows(IllegalArgumentException.class, () -> preferences.rangDe(new Chambre(2)));
        assertThrows(IllegalArgumentException.class, () -> preferences.rangDe(null));
    }

    /** Vérifie les deux arguments de comparaison, même s'ils sont égaux. */
    @Test
    void refuseComparaisonInvalide() {
        Preferences preferences = new Preferences(List.of(new Chambre(1)));
        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                    () -> preferences.prefere(new Chambre(2), new Chambre(1))),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> preferences.prefere(new Chambre(1), new Chambre(2))),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> preferences.prefere(new Chambre(2), new Chambre(2))),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> preferences.prefere(null, new Chambre(1))),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> preferences.prefere(new Chambre(1), null))
        );
    }

    /** Une modification de la liste d'origine ne change ni les chambres ni les rangs. */
    @Test
    void copieListeRecue() {
        List<Chambre> source = new ArrayList<>(List.of(new Chambre(2), new Chambre(1)));
        Preferences preferences = new Preferences(source);
        source.set(0, new Chambre(3));
        source.clear();
        assertEquals(List.of(new Chambre(2), new Chambre(1)), preferences.getClassement());
        assertEquals(0, preferences.rangDe(new Chambre(2)));
    }

    /** L'accesseur ne permet pas de désynchroniser le classement et ses rangs. */
    @Test
    void protegeClassementRetourne() {
        Preferences preferences = new Preferences(List.of(new Chambre(1), new Chambre(2)));
        assertThrows(UnsupportedOperationException.class,
                () -> preferences.getClassement().set(0, new Chambre(3)));
        assertThrows(UnsupportedOperationException.class, () -> preferences.getClassement().clear());
        assertEquals(0, preferences.rangDe(new Chambre(1)));
    }
}
