package fr.univ.paris.voyage.service;

import java.util.Set;
import fr.univ.paris.voyage.algorithme.SolveurAffectation;
import fr.univ.paris.voyage.modele.Affectation;
import fr.univ.paris.voyage.modele.Chambre;
import fr.univ.paris.voyage.modele.Groupe;
import fr.univ.paris.voyage.modele.Personne;

/**
 * Partage entre interfaces la répartition courante, les bilans et une annulation.
 * L'affectation n'est pas exposée afin de protéger l'historique. Le groupe fourni
 * reste la source des préférences et relations, lues à chaque calcul.
 */
public final class SessionAffectation {

    /** Données métier utilisées pour évaluer les chambres. */
    private final Groupe groupe;
    /** Répartition détenue par la session. */
    private final Affectation affectation;
    /** Service de calcul partagé. */
    private final CalculateurJalousie calculateur;
    /** Première personne du dernier échange effectif, ou null. */
    private Personne dernierA;
    /** Seconde personne du dernier échange effectif, ou null. */
    private Personne dernierB;

    /**
     * Résout un groupe complet et prépare un historique vide.
     * @param groupe groupe à répartir
     * @param solveur stratégie de résolution
     * @param calculateur service d'évaluation
     * @throws IllegalArgumentException si une dépendance est nulle
     * @throws IllegalStateException si les préférences sont incomplètes ou le résultat invalide
     */
    public SessionAffectation(Groupe groupe, SolveurAffectation solveur, CalculateurJalousie calculateur) {
        if (groupe == null || solveur == null || calculateur == null) {
            throw new IllegalArgumentException("Les dépendances de la session sont nécessaires.");
        }
        if (!groupe.estComplet()) {
            throw new IllegalStateException("Toutes les préférences doivent être renseignées.");
        }
        Affectation resultat = solveur.resoudre(groupe);
        if (resultat == null || !resultat.estValidePour(groupe)) {
            throw new IllegalStateException("Le solveur doit produire une affectation valide.");
        }
        // Copie indépendante, même si une stratégie conserve sa propre référence au résultat.
        java.util.Map<Personne, Chambre> chambres = new java.util.LinkedHashMap<>();
        for (Personne personne : groupe.getPersonnes()) {
            chambres.put(personne, resultat.chambreDe(personne));
        }
        this.groupe = groupe;
        this.affectation = new Affectation(groupe, chambres);
        this.calculateur = calculateur;
    }

    /**
     * Consulte la chambre d'une personne.
     * @param personne personne recherchée
     * @return chambre attribuée
     * @throws IllegalArgumentException si la personne est nulle ou inconnue
     */
    public Chambre chambreDe(Personne personne) {
        return affectation.chambreDe(personne);
    }

    /**
     * Recalcule le coût de la répartition courante.
     * @return nombre courant de personnes jalouses
     */
    public int cout() {
        return calculateur.calculerCout(groupe, affectation);
    }

    /**
     * Explique la jalousie d'une personne sans exposer la répartition interne.
     * @param personne personne examinée
     * @return instantané non modifiable des voisins jalousés
     * @throws IllegalArgumentException si la personne est nulle ou inconnue
     */
    public Set<Personne> voisinsJalouses(Personne personne) {
        return calculateur.voisinsJalouses(personne, groupe, affectation);
    }

    /**
     * Échange deux chambres et mémorise la dernière opération effective.
     * Une erreur ou un échange avec soi-même conserve l'annulation précédente.
     * @param a première personne
     * @param b seconde personne
     * @return bilan avant/après
     * @throws IllegalArgumentException si une personne est nulle ou inconnue
     */
    public BilanEchange echanger(Personne a, Personne b) {
        int avant = cout();
        affectation.echanger(a, b);
        boolean modification = !a.equals(b);
        if (modification) {
            dernierA = a;
            dernierB = b;
        }
        return new BilanEchange(avant, cout(), modification);
    }

    /**
     * Indique si une annulation est disponible.
     * @return vrai si le dernier échange effectif peut encore être annulé
     */
    public boolean peutAnnuler() {
        return dernierA != null;
    }

    /**
     * Inverse le dernier échange, puis vide l'historique d'un seul niveau.
     * @return bilan de l'annulation
     * @throws IllegalStateException s'il n'y a aucun échange à annuler
     */
    public BilanEchange annuler() {
        if (!peutAnnuler()) {
            throw new IllegalStateException("Aucun échange à annuler.");
        }
        int avant = cout();
        affectation.echanger(dernierA, dernierB);
        dernierA = null;
        dernierB = null;
        return new BilanEchange(avant, cout(), true);
    }
}
