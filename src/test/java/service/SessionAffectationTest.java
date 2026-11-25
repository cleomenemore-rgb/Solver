package service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import algorithme.SolveurNaif;
import modele.*;
import ui.PresentationGroupe;

/** Vérifie les explications, les bilans et l'historique partagé des échanges. */
class SessionAffectationTest {

    /** @return groupe complet de trois personnes partageant le classement 1,2,3 */
    private Groupe groupe() {
        Groupe g = new Groupe(List.of(new Personne("A"), new Personne("B"), new Personne("C")),
                List.of(new Chambre(1), new Chambre(2), new Chambre(3)));
        for (Personne p : g.getPersonnes()) {
            g.definirPreferences(p, new Preferences(g.getChambres()));
        }
        g.ajouterRelation(g.getPersonne("A"), g.getPersonne("B"));
        g.ajouterRelation(g.getPersonne("A"), g.getPersonne("C"));
        return g;
    }

    /** @param g groupe complet @return session naïve correspondante */
    private SessionAffectation session(Groupe g) {
        return new SessionAffectation(g, new SolveurNaif(), new CalculateurJalousie());
    }

    /** Tous les voisins jalousés sont expliqués, sans compter une personne deux fois. */
    @Test
    void expliquePlusieursVoisinsMaisCompteUnePersonne() {
        Groupe g = groupe();
        Personne a = g.getPersonne("A");
        Personne b = g.getPersonne("B");
        Personne c = g.getPersonne("C");
        Affectation affectation = new Affectation(g, Map.of(a, new Chambre(3), b, new Chambre(1), c, new Chambre(2)));
        CalculateurJalousie calculateur = new CalculateurJalousie();
        assertEquals(Set.of(b, c), calculateur.voisinsJalouses(a, g, affectation));
        assertEquals(1, calculateur.calculerCout(g, affectation));
        assertTrue(calculateur.voisinsJalouses(b, g, affectation).isEmpty());
    }

    /** L'explication est un instantané protégé et change après un échange effectif. */
    @Test
    void protegeInstantaneDesVoisins() {
        Groupe g = groupe();
        SessionAffectation s = session(g);
        Personne a = g.getPersonne("A");
        Personne c = g.getPersonne("C");
        Set<Personne> avant = s.voisinsJalouses(a);
        assertTrue(avant.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> avant.add(c));
        s.echanger(a, c);
        assertTrue(avant.isEmpty());
        assertEquals(2, s.voisinsJalouses(a).size());
    }

    /** Les coûts sont recalculés et l'annulation restaure toutes les chambres. */
    @Test
    void echangeEtAnnuleAvecBilan() {
        Groupe g = groupe();
        SessionAffectation s = session(g);
        Personne a = g.getPersonne("A");
        Personne c = g.getPersonne("C");
        assertFalse(s.peutAnnuler());
        assertEquals(new BilanEchange(2, 1, true), s.echanger(a, c));
        assertTrue(s.peutAnnuler());
        assertEquals(new Chambre(3), s.chambreDe(a));
        assertEquals(new BilanEchange(1, 2, true), s.annuler());
        assertEquals(new Chambre(1), s.chambreDe(a));
        assertEquals(new Chambre(3), s.chambreDe(c));
        assertFalse(s.peutAnnuler());
        assertThrows(IllegalStateException.class, s::annuler);
    }

    /** Un refus ou une opération neutre ne détruit pas l'annulation précédente. */
    @Test
    void erreursEtEchangeNeutrePreserventHistorique() {
        Groupe g = groupe();
        SessionAffectation s = session(g);
        Personne a = g.getPersonne("A");
        Personne c = g.getPersonne("C");
        s.echanger(a, c);
        assertThrows(IllegalArgumentException.class, () -> s.echanger(a, new Personne("Z")));
        assertThrows(IllegalArgumentException.class, () -> s.echanger(null, a));
        assertEquals(new BilanEchange(1, 1, false), s.echanger(a, a));
        assertTrue(s.peutAnnuler());
        s.annuler();
        assertEquals(new Chambre(1), s.chambreDe(a));
    }

    /** L'annulation porte sur un seul échange, le plus récent. */
    @Test
    void annuleSeulementDernierEchange() {
        Groupe g = groupe();
        SessionAffectation s = session(g);
        Personne a = g.getPersonne("A");
        Personne b = g.getPersonne("B");
        Personne c = g.getPersonne("C");
        s.echanger(a, b);
        s.echanger(b, c);
        s.annuler();
        assertEquals(new Chambre(2), s.chambreDe(a));
        assertEquals(new Chambre(1), s.chambreDe(b));
        assertEquals(new Chambre(3), s.chambreDe(c));
        assertThrows(IllegalStateException.class, s::annuler);
    }

    /** Le solveur ne peut modifier le résultat détenu par la session via sa référence. */
    @Test
    void copieResultatDuSolveur() {
        Groupe g = groupe();
        Affectation initial = new SolveurNaif().resoudre(g);
        SessionAffectation s = new SessionAffectation(g, ignore -> initial, new CalculateurJalousie());
        initial.echanger(g.getPersonne("A"), g.getPersonne("B"));
        assertEquals(new Chambre(1), s.chambreDe(g.getPersonne("A")));
        assertFalse(s.peutAnnuler());
    }

    /** Deux sessions restent indépendantes, y compris leur annulation. */
    @Test
    void sessionsIndependantes() {
        Groupe g = groupe();
        SessionAffectation s = session(g);
        SessionAffectation autre = session(g);
        s.echanger(g.getPersonne("A"), g.getPersonne("C"));
        assertEquals(2, autre.cout());
        assertFalse(autre.peutAnnuler());
    }

    /** Un groupe incomplet et les dépendances nulles sont refusés. */
    @Test
    void refuseConstructionInvalide() {
        Groupe g = groupe();
        SolveurNaif solveur = new SolveurNaif();
        CalculateurJalousie calculateur = new CalculateurJalousie();
        assertThrows(IllegalArgumentException.class, () -> new SessionAffectation(null, solveur, calculateur));
        assertThrows(IllegalArgumentException.class, () -> new SessionAffectation(g, null, calculateur));
        assertThrows(IllegalArgumentException.class, () -> new SessionAffectation(g, solveur, null));
        assertThrows(IllegalStateException.class, () -> new SessionAffectation(g, ignore -> null, calculateur));
        Groupe incomplet = new Groupe(List.of(new Personne("A")), List.of(new Chambre(1)));
        assertThrows(IllegalStateException.class, () -> session(incomplet));
        Affectation etrangere = new Affectation(incomplet, Map.of(new Personne("A"), new Chambre(1)));
        assertThrows(IllegalStateException.class, () -> new SessionAffectation(g, ignore -> etrangere, calculateur));
    }

    /** Les détails de jalousie refusent les mêmes contextes invalides que les coûts. */
    @Test
    void valideContexteDesExplications() {
        Groupe g = groupe();
        Affectation a = new SolveurNaif().resoudre(g);
        CalculateurJalousie c = new CalculateurJalousie();
        assertThrows(IllegalArgumentException.class, () -> c.voisinsJalouses(null, g, a));
        assertThrows(IllegalArgumentException.class, () -> c.voisinsJalouses(new Personne("Z"), g, a));
        assertThrows(IllegalArgumentException.class, () -> c.voisinsJalouses(g.getPersonne("A"), null, a));
        assertThrows(IllegalArgumentException.class, () -> c.voisinsJalouses(g.getPersonne("A"), g, null));
    }

    /** Les messages expliquent le classement propre à la personne et le coût global. */
    @Test
    void presenteExplicationsEtEvolution() {
        Groupe g = groupe();
        SessionAffectation s = session(g);
        assertTrue(PresentationGroupe.personne(g.getPersonne("B"), s).contains("B jalouse A : préfère la chambre 1 à la chambre 2"));
        assertTrue(PresentationGroupe.personne(g.getPersonne("A"), s).contains("Aucune jalousie"));
        assertTrue(PresentationGroupe.jalousies(g, s).contains("2 personne(s) jalouse(s) sur 3"));
        assertTrue(PresentationGroupe.configuration(g).contains("préférences"));
        assertTrue(PresentationGroupe.bilan(new BilanEchange(2, 1, true)).contains("de moins"));
        assertTrue(PresentationGroupe.bilan(new BilanEchange(1, 2, true)).contains("de plus"));
        assertTrue(PresentationGroupe.bilan(new BilanEchange(1, 1, false)).contains("inchangé"));
    }

    /** Les bilans ne peuvent annoncer un coût négatif ou un changement sans opération. */
    @Test
    void refuseBilanIncoherent() {
        assertThrows(IllegalArgumentException.class, () -> new BilanEchange(-1, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new BilanEchange(1, -1, true));
        assertThrows(IllegalArgumentException.class, () -> new BilanEchange(1, 2, false));
    }
}
