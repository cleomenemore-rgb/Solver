package algorithme;

import modele.Affectation;
import modele.Groupe;

// Contrat des algorithmes produisant une affectation complète sans modifier le groupe.

public interface SolveurAffectation {

    /**
     * Construit une nouvelle affectation pour un groupe dont les préférences sont complètes.
     * Le résultat est indépendant des affectations produites par les appels précédents.
     * Aucune garantie d'optimalité du coût n'est imposée par ce contrat.
     * @param groupe : groupe à répartir
     * @return nouvelle affectation valide pour le groupe
     * @throws IllegalArgumentException si le groupe est nul
     * @throws IllegalStateException si des préférences sont absentes
     */
    Affectation resoudre(Groupe groupe);
}
