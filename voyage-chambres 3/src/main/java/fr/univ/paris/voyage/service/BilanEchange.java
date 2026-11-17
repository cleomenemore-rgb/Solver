package fr.univ.paris.voyage.service;

/**
 * Résultat immuable d'un échange ou de son annulation.
 * @param avant nombre de personnes jalouses avant l'opération
 * @param apres nombre de personnes jalouses après l'opération
 * @param modification vrai si deux personnes distinctes ont échangé leurs chambres
 */
public record BilanEchange(int avant, int apres, boolean modification) {

    /**
     * Vérifie la cohérence des coûts du bilan.
     * @param avant coût avant l'opération
     * @param apres coût après l'opération
     * @param modification présence d'un échange effectif
     * @throws IllegalArgumentException si un coût est négatif ou change sans modification
     */
    public BilanEchange {
        if (avant < 0 || apres < 0 || (!modification && avant != apres)) {
            throw new IllegalArgumentException("Bilan d'échange incohérent.");
        }
    }
}
