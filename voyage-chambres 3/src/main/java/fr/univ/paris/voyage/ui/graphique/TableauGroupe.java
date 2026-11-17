package fr.univ.paris.voyage.ui.graphique;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import fr.univ.paris.voyage.modele.Groupe;
import fr.univ.paris.voyage.modele.Personne;
import fr.univ.paris.voyage.service.SessionAffectation;

/** Instantané non éditable du groupe pour la table Swing. */
public final class TableauGroupe extends AbstractTableModel {

    /** Identifiant de sérialisation requis par le parent Swing. */
    private static final long serialVersionUID = 1L;
    /** Libellés des colonnes. */
    private static final String[] COLONNES = {"Personne", "Préférences", "Relations", "Chambre", "État"};
    /** Cellules calculées lors d'une actualisation, pas lors du dessin de chaque cellule. */
    private String[][] lignes = new String[0][COLONNES.length];

    /** Crée une table initialement vide. */
    public TableauGroupe() {
    }

    /**
     * Recalcule les cellules après une action validée.
     * @param groupe groupe à afficher, ou null pour vider la table
     * @param session session correspondant au groupe, ou null pendant la configuration
     */
    public void actualiser(Groupe groupe, SessionAffectation session) {
        List<Personne> personnes = groupe == null ? List.of() : groupe.getPersonnes();
        lignes = new String[personnes.size()][COLONNES.length];
        List<Personne> manquantes = groupe == null ? List.of() : groupe.personnesSansPreferences();
        for (int i = 0; i < personnes.size(); i++) {
            Personne p = personnes.get(i);
            lignes[i][0] = p.getNom();
            lignes[i][1] = manquantes.contains(p) ? "À renseigner" : groupe.getPreferences(p).getClassement().toString();
            lignes[i][2] = groupe.getVoisins(p).toString();
            lignes[i][3] = session == null ? "—" : String.valueOf(session.chambreDe(p).getNumero());
            lignes[i][4] = session == null ? (manquantes.contains(p) ? "Incomplet" : "Prêt")
                    : session.voisinsJalouses(p).isEmpty() ? "Sans jalousie" : "Jalouse";
        }
        fireTableDataChanged();
    }

    /**
     * Fournit nombre de personnes affichées.
     * @return nombre de personnes affichées
     */
    @Override
    public int getRowCount() {
        return lignes.length;
    }

    /**
     * Fournit nombre de colonnes affichées.
     * @return nombre de colonnes affichées
     */
    @Override
    public int getColumnCount() {
        return COLONNES.length;
    }

    /**
     * Retourne un en-tête.
     * @param colonne indice de colonne
     * @return nom de colonne
     */
    @Override
    public String getColumnName(int colonne) {
        return COLONNES[colonne];
    }

    /**
     * Consulte une cellule, sans déclencher de calcul métier.
     * @param ligne indice de personne
     * @param colonne indice de colonne
     * @return texte affiché
     */
    @Override
    public Object getValueAt(int ligne, int colonne) {
        return lignes[ligne][colonne];
    }
}
