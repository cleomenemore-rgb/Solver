package fr.univ.paris.voyage.modele;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Réunit les personnes, les chambres, leurs relations et leurs préférences.
 * Les membres et les chambres sont fixés à la construction. Les relations et
 * les préférences sont renseignées progressivement par des opérations validées.
 * Le groupe conserve l'ordre des personnes fourni par l'appelant.
 */
public final class Groupe {

    /** Personnes indexées par nom, dans l'ordre de construction du groupe. */
    private final Map<String, Personne> personnesParNom;

    /** Chambres numérotées de 1 à n, dans l'ordre fourni à la construction. */
    private final List<Chambre> chambres;

    /** Voisins de chaque personne dans le graphe symétrique « ne s'aiment pas ». */
    private final Map<Personne, Set<Personne>> relations;

    /** Classements validés ; une personne peut ne pas encore en posséder. */
    private final Map<Personne, Preferences> preferencesParPersonne;

    /**
     * Construit un groupe non vide avec autant de chambres que de personnes.
     * Le modèle accepte plus de 26 personnes ; la console de partie 1
     * impose la limite de 26 et crée les noms A à Z.
     * @param personnes membres distincts, dans l'ordre de traitement souhaité
     * @param chambres chambres distinctes, numérotées de 1 au nombre de personnes
     * @throws IllegalArgumentException si une liste est nulle ou vide, si les
     *         tailles diffèrent, si un élément est nul ou dupliqué, ou si un
     *         numéro de chambre dépasse le nombre de personnes
     */
    public Groupe(List<Personne> personnes, List<Chambre> chambres) {
        if (personnes == null || chambres == null || personnes.isEmpty()) {
            throw new IllegalArgumentException("Le groupe doit contenir des personnes et des chambres.");
        }
        if (personnes.size() != chambres.size()) {
            throw new IllegalArgumentException("Il faut autant de chambres que de personnes.");
        }
        this.personnesParNom = new LinkedHashMap<>();
        this.relations = new LinkedHashMap<>();
        this.preferencesParPersonne = new LinkedHashMap<>();
        for (Personne personne : personnes) {
            if (personne == null) {
                throw new IllegalArgumentException("Le groupe contient une personne nulle.");
            }
            if (personnesParNom.putIfAbsent(personne.getNom(), personne) != null) {
                throw new IllegalArgumentException("Nom répété dans le groupe : " + personne);
            }
            relations.put(personne, new LinkedHashSet<>());
        }
        Set<Chambre> chambresVues = new HashSet<>();
        for (Chambre chambre : chambres) {
            if (chambre == null || chambre.getNumero() > personnes.size()) {
                throw new IllegalArgumentException("Les chambres doivent être numérotées de 1 à n.");
            }
            if (!chambresVues.add(chambre)) {
                throw new IllegalArgumentException("Chambre répétée dans le groupe : " + chambre);
            }
        }
        // n numéros distincts dans [1, n] couvrent nécessairement toutes les chambres.
        this.chambres = List.copyOf(chambres);
    }

    /**
     * Recherche une personne par son nom exact.
     * @param nom nom recherché, sensible à la casse
     * @return personne enregistrée dans le groupe
     * @throws IllegalArgumentException si le nom est nul ou inconnu
     */
    public Personne getPersonne(String nom) {
        Personne personne = personnesParNom.get(nom);
        if (personne == null) {
            throw new IllegalArgumentException("Personne inconnue : " + nom);
        }
        return personne;
    }

    /**
     * Renvoie une copie non modifiable des membres dans l'ordre de construction.
     * @return personnes du groupe
     */
    public List<Personne> getPersonnes() {
        return List.copyOf(personnesParNom.values());
    }

    /**
     * Renvoie les chambres sans permettre de modifier la liste.
     * @return liste immuable des chambres
     */
    public List<Chambre> getChambres() {
        return chambres;
    }

    /**
     * Recherche une chambre par son numéro.
     * @param numero numéro recherché, entre 1 et n
     * @return chambre enregistrée dans le groupe
     * @throws IllegalArgumentException si le numéro est inconnu
     */
    public Chambre getChambre(int numero) {
        for (Chambre chambre : chambres) {
            if (chambre.getNumero() == numero) {
                return chambre;
            }
        }
        throw new IllegalArgumentException("Chambre inconnue : " + numero);
    }

    /**
     * Ajoute la relation « ne s'aiment pas » dans les deux sens.
     * Une relation déjà présente ne change pas le groupe.
     * @param a première personne, identifiée par son nom
     * @param b seconde personne, identifiée par son nom
     * @throws IllegalArgumentException si une personne est nulle ou inconnue,
     *         ou si les deux personnes sont égales
     */
    public void ajouterRelation(Personne a, Personne b) {
        verifierPersonne(a);
        verifierPersonne(b);
        if (a.equals(b)) {
            throw new IllegalArgumentException("Une personne ne peut pas être en relation avec elle-même.");
        }
        // Valider les deux personnes avant de modifier l'un des deux ensembles.
        relations.get(a).add(getPersonne(b.getNom()));
        relations.get(b).add(getPersonne(a.getNom()));
    }

    /**
     * Renvoie un instantané non modifiable des voisins d'une personne.
     * Les ajouts ultérieurs au groupe ne modifient pas cet instantané.
     * @param personne personne dont on cherche les voisins
     * @return voisins dans leur ordre d'ajout, éventuellement aucun
     * @throws IllegalArgumentException si la personne est nulle ou inconnue
     */
    public Set<Personne> getVoisins(Personne personne) {
        verifierPersonne(personne);
        return Collections.unmodifiableSet(new LinkedHashSet<>(relations.get(personne)));
    }

    /**
     * Enregistre ou remplace les préférences d'une personne après validation.
     * En cas d'erreur, les anciennes préférences restent inchangées.
     * @param personne personne concernée
     * @param preferences classement contenant exactement les chambres du groupe
     * @throws IllegalArgumentException si la personne est nulle ou inconnue,
     *         si les préférences sont nulles ou si le classement ne correspond
     *         pas aux chambres du groupe
     */
    public void definirPreferences(Personne personne, Preferences preferences) {
        verifierPersonne(personne);
        if (preferences == null) {
            throw new IllegalArgumentException("Les préférences ne peuvent pas être nulles.");
        }
        List<Chambre> classement = preferences.getClassement();
        if (classement.size() != chambres.size() || !classement.containsAll(chambres)) {
            throw new IllegalArgumentException("Les préférences doivent contenir toutes les chambres du groupe.");
        }
        preferencesParPersonne.put(getPersonne(personne.getNom()), preferences);
    }

    /**
     * Renvoie les préférences déjà enregistrées d'une personne.
     * @param personne personne concernée
     * @return classement immuable
     * @throws IllegalArgumentException si la personne est nulle ou inconnue
     * @throws IllegalStateException si ses préférences ne sont pas encore définies
     */
    public Preferences getPreferences(Personne personne) {
        verifierPersonne(personne);
        Preferences preferences = preferencesParPersonne.get(personne);
        if (preferences == null) {
            throw new IllegalStateException("Préférences absentes pour " + personne);
        }
        return preferences;
    }

    /**
     * Liste les personnes dont les préférences n'ont pas encore été définies.
     * @return copie non modifiable, dans l'ordre de construction du groupe
     */
    public List<Personne> personnesSansPreferences() {
        List<Personne> personnes = new ArrayList<>();
        for (Personne personne : personnesParNom.values()) {
            if (!preferencesParPersonne.containsKey(personne)) {
                personnes.add(personne);
            }
        }
        return List.copyOf(personnes);
    }

    /**
     * Indique si chaque personne possède un classement validé.
     * Un groupe sans relation peut être complet.
     * @return vrai si toutes les préférences sont renseignées
     */
    public boolean estComplet() {
        return preferencesParPersonne.size() == personnesParNom.size();
    }

    /**
     * Vérifie l'appartenance d'une personne au groupe par son nom.
     * @param personne personne à vérifier
     * @throws IllegalArgumentException si elle est nulle ou inconnue
     */
    private void verifierPersonne(Personne personne) {
        if (personne == null || !personnesParNom.containsKey(personne.getNom())) {
            throw new IllegalArgumentException("Personne inconnue : " + personne);
        }
    }
}
