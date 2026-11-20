package service;

// Imports
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import modele.Affectation;
import modele.Chambre;
import modele.Groupe;
import modele.Personne;
import modele.Preferences;

/**
 * Évalue la jalousie sans modifier le groupe ni l'affectation.
 * Une personne est jalouse si elle préfère la chambre d'au moins un de ses
 * voisins à la sienne. Les préférences examinées sont celles de cette personne.
 * Chaque personne jalouse ne compte qu'une fois dans le coût.
 */
public final class CalculateurJalousie {

    // Constructeur
    public CalculateurJalousie() { // construit un calculateur sans état, réutilisable pour plusieurs affectations
    }

    // Méthodes
    /**
     * Détermine si une personne est jalouse dans l'affectation courante.
     * @param personne : personne à examiner
     * @param groupe : groupe contenant les relations et les préférences
     * @param affectation : affectation à évaluer
     * @return vrai si au moins un voisin possède une chambre préférée à la sienne
     * @throws IllegalArgumentException si un argument est nul, si la personne est
     *         inconnue ou si l'affectation ne correspond pas au groupe
     * @throws IllegalStateException si le groupe n'a pas toutes ses préférences
     */
    public boolean estJalouse(Personne personne, Groupe groupe, Affectation affectation) {
        verifierContexte(groupe, affectation);
        return estJalouseDansGroupeValide(personne, groupe, affectation);
    }

    /**
     * Renvoie un instantané non modifiable des personnes jalouses.
     * @param groupe : groupe contenant les relations et les préférences
     * @param affectation : affectation à évaluer
     * @return personnes jalouses dans l'ordre des membres du groupe
     * @throws IllegalArgumentException si un argument est nul ou si l'affectation
     *         ne correspond pas au groupe
     * @throws IllegalStateException si le groupe n'a pas toutes ses préférences
     */
    public Set<Personne> personnesJalouses(Groupe groupe, Affectation affectation) {
        verifierContexte(groupe, affectation);
        Set<Personne> jalouses = new LinkedHashSet<>();
        for (Personne personne : groupe.getPersonnes()) {
            if (estJalouseDansGroupeValide(personne, groupe, affectation)) {
                jalouses.add(personne);
            }
        }
        return Collections.unmodifiableSet(jalouses);
    }

    /**
     * Compte les personnes jalouses, indépendamment du nombre de voisins jalousés.
     * @param groupe : groupe contenant les relations et les préférences
     * @param affectation : affectation à évaluer
     * @return nombre de personnes jalouses, compris entre zéro et la taille du groupe
     * @throws IllegalArgumentException si un argument est nul ou si l'affectation
     *         ne correspond pas au groupe
     * @throws IllegalStateException si le groupe n'a pas toutes ses préférences
     */
    public int calculerCout(Groupe groupe, Affectation affectation) {
        return personnesJalouses(groupe, affectation).size();
    }

    /**
     * Liste tous les voisins dont une personne préfère la chambre à la sienne.
     * Le résultat est un instantané non modifiable, dans l'ordre des relations.
     * Cette méthode explique la jalousie ; elle ne change pas son comptage unique.
     * @param personne : personne dont on utilise le classement
     * @param groupe : groupe complet
     * @param affectation : répartition courante
     * @return voisins jalousés, éventuellement aucun
     * @throws IllegalArgumentException si un argument est nul, inconnu ou incompatible
     * @throws IllegalStateException si le groupe est incomplet
     */
    public Set<Personne> voisinsJalouses(Personne personne, Groupe groupe, Affectation affectation) {
        verifierContexte(groupe, affectation);
        Preferences preferences = groupe.getPreferences(personne);
        Chambre chambre = affectation.chambreDe(personne);
        Set<Personne> voisins = new LinkedHashSet<>();
        for (Personne voisin : groupe.getVoisins(personne)) {
            if (preferences.prefere(affectation.chambreDe(voisin), chambre)) {
                voisins.add(voisin);
            }
        }
        return Collections.unmodifiableSet(voisins);
    }

    /**
     * Examine une personne après validation du contexte commun.
     * Ce découpage évite de revalider tout le groupe pour chacun de ses membres.
     * @param personne : personne à examiner
     * @param groupe : groupe complet et compatible avec l'affectation
     * @param affectation : affectation déjà validée
     * @return vrai dès qu'une chambre préférée est trouvée chez un voisin
     * @throws IllegalArgumentException si la personne est nulle ou inconnue
     */
    private boolean estJalouseDansGroupeValide(Personne personne, Groupe groupe, Affectation affectation) {
        Preferences preferences = groupe.getPreferences(personne);
        Chambre chambreActuelle = affectation.chambreDe(personne);
        for (Personne voisin : groupe.getVoisins(personne)) {
            if (preferences.prefere(affectation.chambreDe(voisin), chambreActuelle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie les données communes à toutes les opérations de calcul.
     * @param groupe : groupe à examiner
     * @param affectation : affectation à examiner
     * @throws IllegalArgumentException si un argument est nul ou incompatible
     * @throws IllegalStateException si des préférences sont absentes
     */
    private void verifierContexte(Groupe groupe, Affectation affectation) {
        if (groupe == null || affectation == null) {
            throw new IllegalArgumentException("Le groupe et l'affectation sont nécessaires au calcul.");
        }
        if (!affectation.estValidePour(groupe)) {
            throw new IllegalArgumentException("L'affectation ne correspond pas au groupe.");
        }
        if (!groupe.estComplet()) {
            throw new IllegalStateException("Toutes les préférences doivent être renseignées avant le calcul.");
        }
    }
}
