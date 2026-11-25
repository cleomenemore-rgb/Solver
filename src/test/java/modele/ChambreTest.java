package modele;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Vérifie l'identité et la validation de Chambre. */
class ChambreTest {

    /** Vérifie que le constructeur conserve l'identifiant. */
    @Test
    void conserveIdentifiant() {
        assertEquals(1, new Chambre(1).getNumero());
    }

    /** Vérifie la réflexivité, la symétrie et la transitivité de l'égalité. */
    @Test
    void respecteContratEgalite() {
        Chambre premier = new Chambre(1);
        Chambre deuxieme = new Chambre(1);
        Chambre troisieme = new Chambre(1);
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
        Chambre objet = new Chambre(1);
        assertAll(
            () -> assertNotEquals(objet, new Chambre(2)),
            () -> assertFalse(objet.equals(null)),
            () -> assertFalse(objet.equals(new Object()))
        );
    }

    /** Vérifie la représentation textuelle utilisée dans les affichages. */
    @Test
    void fournitRepresentationLisible() {
        assertEquals(String.valueOf(1), new Chambre(1).toString());
    }

    /** Refuse zéro et les valeurs négatives, y compris la borne de int. */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -26, Integer.MIN_VALUE})
    void refuseNumeroInvalide(int numero) {
        assertThrows(IllegalArgumentException.class, () -> new Chambre(numero));
    }

    /** Le modèle n'impose pas la limite de 26 de l'interface de partie 1. */
    @ParameterizedTest
    @ValueSource(ints = {1, 26, 27, Integer.MAX_VALUE})
    void accepteNumeroPositif(int numero) {
        assertEquals(numero, new Chambre(numero).getNumero());
    }
}
