package modele;

/**
 * Représente une personne par son nom, utilisé comme identifiant dans un groupe.
 * Deux personnes de même nom sont égales. Le nom est sensible à la casse et
 * conservé tel quel ; la création des noms A à Z relève de la future console.
 * Cette classe immuable peut servir de clé dans une collection associative.
 */
public final class Personne {

    // Attribut
    private final String nom; // nom non nul et non blanc, fixé à la construction

    // Constructeur
    /**
     * Construit une personne sans normaliser son nom.
     *
     * @param nom :nom servant d'identifiant
     * @throws IllegalArgumentException si le nom est nul, vide ou uniquement blanc
     */
    public Personne(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom d'une personne ne peut pas être vide.");
        }
        this.nom = nom;
    }

    // Méthodes
    /**
     * Renvoie le nom de la personne.
     * @return nom conservé à la construction
     */
    public String getNom() {
        return nom;
    }

    /**
     * Compare les noms de deux personnes, en respectant la casse.
     * @param autre : objet à comparer, éventuellement nul
     * @return vrai si l'objet est une personne portant le même nom
     */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Personne personne)) {
            return false;
        }
        return nom.equals(personne.nom);
    }

    /**
     * Calcule un code de hachage cohérent avec l'égalité sur le nom.
     * @return code de hachage du nom
     */
    @Override
    public int hashCode() {
        return nom.hashCode();
    }

    /**
     * Fournit une représentation textuelle courte.
     * @return nom de la personne
     */
    @Override
    public String toString() {
        return nom;
    }
}
