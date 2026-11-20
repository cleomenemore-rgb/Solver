package modele;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classement immuable des chambres, de la plus appréciée à la moins appréciée.
 * Chaque chambre apparaît une seule fois. Le groupe vérifie ensuite que cette
 * liste contient exactement ses chambres.
 */
public final class Preferences {

    // Attributs
    private final List<Chambre> classement; // copie non modifiable du classement fourni à la construction
    private final Map<Chambre, Integer> rangsParChambre; // rang de chaque chambre, à partir de zéro pour la chambre préférée

    // Constructeur
    /**
     * Construit un classement sans chambre nulle ni doublon.
     * @param classement : chambres de la meilleure à la moins bonne
     * @throws IllegalArgumentException si la liste est nulle, vide, contient une chambre nulle ou deux chambres de même numéro
     */
    public Preferences(List<Chambre> classement) {
        if (classement == null || classement.isEmpty()) {
            throw new IllegalArgumentException("Le classement ne peut pas être vide.");
        }
        Map<Chambre, Integer> rangs = new HashMap<>();
        for (int i = 0; i < classement.size(); i++) {
            Chambre chambre = classement.get(i);
            if (chambre == null) {
                throw new IllegalArgumentException("Le classement contient une chambre nulle.");
            }
            if (rangs.put(chambre, i) != null) {
                throw new IllegalArgumentException("Chambre répétée dans le classement : " + chambre);
            }
        }
        this.classement = List.copyOf(classement);
        this.rangsParChambre = Map.copyOf(rangs);
    }

    // Méthodes
    /**
     * Renvoie le classement, sans permettre sa modification.
     * @return liste immuable, de la meilleure chambre à la moins bonne
     */
    public List<Chambre> getClassement() {
        return classement;
    }

    /**
     * Renvoie la position d'une chambre dans le classement.
     * @param chambre : chambre à rechercher, comparée par son numéro
     * @return rang à partir de zéro ; un rang plus petit est meilleur
     * @throws IllegalArgumentException si la chambre est nulle ou absente
     */
    public int rangDe(Chambre chambre) {
        if (chambre == null || !rangsParChambre.containsKey(chambre)) {
            throw new IllegalArgumentException("Chambre absente du classement : " + chambre);
        }
        return rangsParChambre.get(chambre);
    }

    /**
     * Indique si la première chambre est strictement préférée à la seconde.
     * Une chambre n'est jamais strictement préférée à elle-même.
     * @param a : première chambre
     * @param b : seconde chambre
     * @return vrai si le rang de a est inférieur au rang de b
     * @throws IllegalArgumentException si l'une des chambres est nulle ou absente
     */
    public boolean prefere(Chambre a, Chambre b) {
        return rangDe(a) < rangDe(b);
    }
}
