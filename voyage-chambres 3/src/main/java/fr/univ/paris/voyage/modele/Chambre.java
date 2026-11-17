package fr.univ.paris.voyage.modele;

/**
 * Représente une chambre par un numéro strictement positif et immuable.
 * Deux chambres de même numéro sont égales. Le contrôle de l'appartenance
 * à un groupe, notamment la borne supérieure n, relève du futur Groupe.
 */
public final class Chambre {

    /** Numéro strictement positif identifiant la chambre dans un groupe. */
    private final int numero;

    /**
     * Construit une chambre à partir de son numéro.
     * @param numero numéro strictement positif
     * @throws IllegalArgumentException si le numéro est inférieur ou égal à zéro
     */
    public Chambre(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("Le numéro d'une chambre doit être strictement positif.");
        }
        this.numero = numero;
    }

    /**
     * Renvoie le numéro de la chambre.
     * @return numéro attribué à la construction
     */
    public int getNumero() {
        return numero;
    }

    /**
     * Compare les numéros de deux chambres.
     * @param autre objet à comparer, éventuellement nul
     * @return vrai si l'objet est une chambre portant le même numéro
     */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Chambre chambre)) {
            return false;
        }
        return numero == chambre.numero;
    }

    /**
     * Calcule un code de hachage cohérent avec l'égalité sur le numéro.
     * @return code de hachage du numéro
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(numero);
    }

    /**
     * Fournit le numéro sous forme de texte pour les futurs affichages.
     * @return représentation décimale du numéro
     */
    @Override
    public String toString() {
        return Integer.toString(numero);
    }
}
