package ui;

// Imports
import java.util.Set;
import modele.Groupe;
import modele.Personne;
import service.BilanEchange;
import service.SessionAffectation;

/** Présentation textuelle commune aux deux interfaces, sans modifier le modèle. */

public final class PresentationGroupe {

    private PresentationGroupe() { // empêche l'instanciation de cet ensemble de fonctions de présentation
    }

    // Méthodes
    /**
     * Décrit les relations et les préférences, y compris celles encore absentes.
     * @param groupe : groupe non nul
     * @return récapitulatif lisible
     */
    public static String configuration(Groupe groupe) {
        StringBuilder texte = new StringBuilder("Récapitulatif du groupe\n");
        for (Personne p : groupe.getPersonnes()) {
            texte.append(p).append(" — relations : ").append(groupe.getVoisins(p));
            texte.append(" ; préférences : ").append(groupe.personnesSansPreferences().contains(p)
                    ? "à renseigner" : groupe.getPreferences(p).getClassement()).append('\n');
        }
        return texte.toString();
    }

    /**
     * Explique les chambres préférées détenues par les voisins d'une personne.
     * @param personne : personne appartenant à la session
     * @param session : session non nulle
     * @return explication, y compris en l'absence de jalousie
     */
    public static String personne(Personne personne, SessionAffectation session) {
        int chambre = session.chambreDe(personne).getNumero();
        StringBuilder texte = new StringBuilder(personne + " possède la chambre " + chambre + ".\n");
        Set<Personne> voisins = session.voisinsJalouses(personne);
        if (voisins.isEmpty()) {
            texte.append("Aucune jalousie envers ses voisins.\n");
        } else {
            for (Personne voisin : voisins) {
                texte.append("→ ").append(personne).append(" jalouse ").append(voisin)
                        .append(" : préfère la chambre ").append(session.chambreDe(voisin).getNumero())
                        .append(" à la chambre ").append(chambre).append(".\n");
            }
        }
        return texte.toString();
    }

    /**
     * Présente le coût et les raisons de chaque jalousie du groupe.
     * @param groupe : groupe correspondant à la session
     * @param session : session non nulle
     * @return bilan détaillé
     */
    public static String jalousies(Groupe groupe, SessionAffectation session) {
        StringBuilder texte = new StringBuilder("Coût : " + session.cout()
                + " personne(s) jalouse(s) sur " + groupe.getPersonnes().size() + ".\n");
        for (Personne p : groupe.getPersonnes()) {
            if (!session.voisinsJalouses(p).isEmpty()) {
                texte.append('\n').append(personne(p, session));
            }
        }
        if (session.cout() == 0) {
            texte.append("Aucune personne jalouse.\n");
        }
        texte.append("Une personne compte une seule fois, quel que soit le nombre de voisins jalousés.\n");
        return texte.toString();
    }

    /**
     * Formate l'évolution du coût après échange ou annulation.
     * @param bilan : résultat non nul
     * @return phrase indiquant amélioration, dégradation ou stabilité
     */
    public static String bilan(BilanEchange bilan) {
        int difference = bilan.apres() - bilan.avant();
        String evolution = difference < 0 ? (-difference) + " personne(s) jalouse(s) de moins."
                : difference > 0 ? difference + " personne(s) jalouse(s) de plus." : "Coût inchangé.";
        return "Coût : " + bilan.avant() + " → " + bilan.apres() + ". " + evolution;
    }
}
