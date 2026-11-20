package algorithme;

import java.util.LinkedHashMap;
import java.util.Map;
import modele.Affectation;
import modele.Chambre;
import modele.Groupe;
import modele.Personne;

/**
 * Affectation gloutonne dans l'ordre des personnes conservé par le groupe.
 * Chaque personne reçoit sa chambre préférée parmi celles encore libres.
 * Les relations ne participent pas au choix : le coût de jalousie n'est pas minimisé.
 * Pour n personnes, au plus n(n+1)/2 chambres sont examinées. Le temps est en
 * O(n²) dans le pire cas, sous les hypothèses usuelles de hachage, et la mémoire supplémentaire
 * en O(n).
 */
public final class SolveurNaif implements SolveurAffectation {

    /** Construit un solveur sans état, réutilisable sur plusieurs groupes. */
    public SolveurNaif() {
    }

    /**
     * Attribue à chaque personne la première chambre libre de son classement.
     * Le groupe et ses préférences restent inchangés.
     * @param groupe : groupe dont toutes les préférences sont renseignées
     * @return affectation complète dans l'ordre de passage du groupe
     * @throws IllegalArgumentException si le groupe est nul
     * @throws IllegalStateException si des préférences sont absentes
     */
    @Override
    public Affectation resoudre(Groupe groupe) {
        if (groupe == null) {
            throw new IllegalArgumentException("Le groupe est nécessaire à la résolution.");
        }
        if (!groupe.estComplet()) {
            throw new IllegalStateException("Toutes les préférences doivent être renseignées avant la résolution.");
        }
        // Le groupe garantit des chambres numérotées de 1 à n ; la case 0 est inutilisée.
        boolean[] occupees = new boolean[groupe.getChambres().size() + 1];
        Map<Personne, Chambre> attribution = new LinkedHashMap<>();
        for (Personne personne : groupe.getPersonnes()) {
            for (Chambre chambre : groupe.getPreferences(personne).getClassement()) {
                int numero = chambre.getNumero();
                if (!occupees[numero]) {
                    attribution.put(personne, chambre);
                    occupees[numero] = true;
                    break;
                }
            }
        }
        return new Affectation(groupe, attribution);
    }
}
