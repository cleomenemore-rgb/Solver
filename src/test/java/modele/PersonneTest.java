package modele;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

/** Vérifie l'identité et la validation de Personne. */
class PersonneTest {

    /** Vérifie que le constructeur conserve l'identifiant. */
    @Test
    void conserveIdentifiant() {
        assertEquals("A", new Personne("A").getNom());
    }

    /** Vérifie la réflexivité, la symétrie et la transitivité de l'égalité. */
    @Test
    void respecteContratEgalite() {
        Personne premier = new Personne("A");
        Personne deuxieme = new Personne("A");
        Personne troisieme = new Personne("A");
        assertAll(
            () -> assertEquals(premier, premier),
            () -> assertEquals(premier, deuxieme),
            () -> assertEquals(deuxieme, premier),
            () -> assertEquals(deuxieme, troisieme),
            () -> assertEquals(premier, troisieme),
            () -> assertEquals(premier.hashCode(), deuxieme.hashCode())
        );
    }

    /** Refuse l'égalité avec null, un autre type ou un autre identifiant. */
    @Test
    void distingueObjetsDifferents() {
        Personne objet = new Personne("A");
        assertAll(
            () -> assertNotEquals(objet, new Personne("B")),
            () -> assertFalse(objet.equals(null)),
            () -> assertFalse(objet.equals(new Object()))
        );
    }

    /** Vérifie la représentation textuelle utilisée dans les affichages. */
    @Test
    void fournitRepresentationLisible() {
        assertEquals(String.valueOf("A"), new Personne("A").toString());
    }

    /** Refuse les noms absents ou uniquement composés de blancs. */
    @ParameterizedTest // permet d'exécuter un même test avec plusieurs valeurs
    @NullAndEmptySource // fournit automatiquement les valeurs null et chaîne vide
    @ValueSource(strings = {" ", "   ", "\t", "\n", "\u2003"}) // permet de définir une liste de valeurs à tester
    void refuseNomInvalide(String nom) {
        assertThrows(IllegalArgumentException.class, () -> new Personne(nom));
    }

    /** Ne limite pas le modèle aux lettres A à Z de la première console. */
    @ParameterizedTest
    @ValueSource(strings = {"Z", "Alice", "Élodie", "Jean Paul", " A "})
    void conserveNomValideSansTransformation(String nom) {
        assertEquals(nom, new Personne(nom).getNom());
    }

    /** La casse fait partie de l'identifiant. */
    @Test
    void distingueMajusculesEtMinuscules() {
        assertNotEquals(new Personne("A"), new Personne("a"));
    }
}
