package modele;

// Imports
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Affectation complète d'une chambre distincte à chaque personne d'un groupe.
 * La map fournie est copiée. Seuls les échanges peuvent ensuite modifier
 * l'affectation, en conservant une chambre par personne et une personne par chambre.
 * Plusieurs affectations indépendantes peuvent concerner le même groupe.
 */
public final class Affectation {

    // Attributs
    private final Map<Personne, Chambre> chambresParPersonne; // chambres attribuées, indexées par les personnes auxquelles elles reviennent

    // Constructeur
    /**
     * Construit une affectation et vérifie sa correspondance avec le groupe.
     * Les préférences du groupe ne sont pas nécessaires pour cette vérification.
     * @param groupe : groupe auquel l'affectation doit correspondre
     * @param attribution : association de toutes les personnes à toutes les chambres
     * @throws IllegalArgumentException si un argument est nul ou si l'association
     *         est incomplète, contient une personne ou une chambre étrangère,
     *         un élément nul ou une chambre attribuée plusieurs fois
     */
    public Affectation(Groupe groupe, Map<Personne, Chambre> attribution) {
        if (groupe == null || attribution == null) {
            throw new IllegalArgumentException("Le groupe et l'attribution sont nécessaires.");
        }
        this.chambresParPersonne = new LinkedHashMap<>(attribution);
        if (!estValidePour(groupe)) {
            throw new IllegalArgumentException("L'affectation doit attribuer chaque chambre du groupe une seule fois.");
        }
    }

    // Méthodes
    /**
     * Renvoie la chambre actuellement attribuée à la personne donnée.
     * @param personne : personne recherchée
     * @return chambre actuellement attribuée
     * @throws IllegalArgumentException si la personne est nulle ou absente
     */
    public Chambre chambreDe(Personne personne) {
        Chambre chambre = chambresParPersonne.get(personne);
        if (chambre == null) {
            throw new IllegalArgumentException("Personne absente de l'affectation : " + personne);
        }
        return chambre;
    }

    /**
     * Échange les chambres de deux personnes après validation des deux arguments.
     * Échanger une personne avec elle-même laisse l'affectation inchangée.
     * @param a : première personne
     * @param b : seconde personne
     * @throws IllegalArgumentException si une personne est nulle ou absente ;
     *         dans ce cas, aucune chambre n'est modifiée
     */
    public void echanger(Personne a, Personne b) {
        Chambre chambreA = chambreDe(a);
        Chambre chambreB = chambreDe(b);
        chambresParPersonne.put(a, chambreB);
        chambresParPersonne.put(b, chambreA);
    }

    /**
     * Vérifie que l'affectation couvre exactement les membres et chambres du groupe.
     * Deux groupes possédant les mêmes identifiants sont compatibles, même si
     * leurs relations ou préférences diffèrent.
     * @param groupe : groupe à vérifier, éventuellement nul
     * @return vrai si toutes les personnes et chambres correspondent sans doublon ;
     *         faux si le groupe est nul
     */
    public boolean estValidePour(Groupe groupe) {
        if (groupe == null) {
            return false;
        }
        Set<Personne> personnes = new HashSet<>(groupe.getPersonnes());
        Set<Chambre> chambres = new HashSet<>(chambresParPersonne.values());
        return chambresParPersonne.keySet().equals(personnes)
                && chambres.size() == chambresParPersonne.size()
                && chambres.equals(new HashSet<>(groupe.getChambres()));
    }
}
