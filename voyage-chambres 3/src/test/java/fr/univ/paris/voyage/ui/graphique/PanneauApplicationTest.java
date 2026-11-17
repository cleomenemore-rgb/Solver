package fr.univ.paris.voyage.ui.graphique;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import fr.univ.paris.voyage.algorithme.SolveurNaif;
import fr.univ.paris.voyage.service.CalculateurJalousie;

/** Joue les vrais événements Swing sur l'EDT, sans nécessiter d'écran. */
class PanneauApplicationTest {

    /**
     * Exécute des assertions sur l'EDT et propage leur échec à JUnit.
     * @param test scénario à jouer
     * @throws Exception si une opération échoue
     */
    private void surEdt(Runnable test) throws Exception {
        try { SwingUtilities.invokeAndWait(test); }
        catch (InvocationTargetException e) {
            if (e.getCause() instanceof AssertionError erreur) throw erreur;
            throw e;
        }
    }

    /** @return nouveau panneau avec les services réels */
    private PanneauApplication panneau() {
        return new PanneauApplication(new SolveurNaif(),new CalculateurJalousie());
    }

    /**
     * Recherche récursivement un composant nommé.
     * @param racine conteneur à parcourir
     * @param nom nom stable du composant
     * @return composant correspondant ou null
     */
    private Component chercher(Container racine,String nom) {
        for(Component composant:racine.getComponents()) {
            if(nom.equals(composant.getName())) return composant;
            if(composant instanceof Container conteneur) {
                Component trouve=chercher(conteneur,nom);
                if(trouve!=null) return trouve;
            }
        }
        return null;
    }

    /** @param p panneau @param nom composant @return bouton nommé */
    private JButton bouton(PanneauApplication p,String nom) { return (JButton)chercher(p,nom); }
    /** @param p panneau @param nom composant @return texte affiché */
    private String texte(PanneauApplication p,String nom) { return ((JTextArea)chercher(p,nom)).getText(); }
    /** @param p panneau @param nom liste @param indice personne à choisir */
    private void choisir(PanneauApplication p,String nom,int indice) { ((JComboBox<?>)chercher(p,nom)).setSelectedIndex(indice); }
    /** @param p panneau @param personne indice de personne @param classement numéros à saisir */
    private void preferences(PanneauApplication p,int personne,String classement) {
        choisir(p,"personnePreferences",personne);
        ((JTextField)chercher(p,"preferences")).setText(classement);
        bouton(p,"enregistrer").doClick();
    }
    /** @param p panneau @param n nombre de personnes à créer */
    private void creer(PanneauApplication p,int n) {
        ((JSpinner)chercher(p,"nombre")).setValue(n);bouton(p,"creer").doClick();
    }
    /** @param p panneau @return table du groupe */
    private JTable table(PanneauApplication p) { return (JTable)chercher(p,"tableau"); }

    /** Les actions sont désactivées tant que leurs données ne sont pas prêtes. */
    @Test
    void bloqueActionsPrematurees() throws Exception {
        surEdt(()->{
            PanneauApplication p=panneau();
            assertFalse(bouton(p,"resoudre").isEnabled());assertFalse(bouton(p,"echanger").isEnabled());
            creer(p,2);assertEquals(2,table(p).getRowCount());
            preferences(p,0,"1 2");assertFalse(bouton(p,"resoudre").isEnabled());
            preferences(p,1,"2 1");assertTrue(bouton(p,"resoudre").isEnabled());
            assertFalse(table(p).isCellEditable(0,1));
        });
    }

    /** L'exemple du sujet passe de coût 2 à 1 puis revient à 2 après annulation. */
    @Test
    void parcoursCompletSujetEtAnnulation() throws Exception {
        surEdt(()->{
            PanneauApplication p=panneau();creer(p,4);
            choisir(p,"relationA",1);
            for(int voisin:new int[]{0,2,3}) { choisir(p,"relationB",voisin);bouton(p,"ajouter").doClick(); }
            preferences(p,0,"1 2 3 4");preferences(p,1,"1 3 2 4");
            preferences(p,2,"3 2 1 4");preferences(p,3,"1 4 2 3");
            assertTrue(texte(p,"recapitulatif").contains("A"));
            bouton(p,"resoudre").doClick();
            assertEquals("3",table(p).getValueAt(1,3));
            assertEquals("Jalouse",table(p).getValueAt(1,4));
            assertTrue(texte(p,"bilan").contains("2 personne(s) jalouse(s)"));
            table(p).setRowSelectionInterval(2,2);
            assertTrue(texte(p,"detail").contains("C jalouse B"));
            assertFalse(bouton(p,"enregistrer").isEnabled());
            choisir(p,"echangeA",1);choisir(p,"echangeB",2);bouton(p,"echanger").doClick();
            assertTrue(texte(p,"message").contains("2 → 1"));
            assertEquals("Sans jalousie",table(p).getValueAt(2,4));
            assertTrue(bouton(p,"annuler").isEnabled());bouton(p,"annuler").doClick();
            assertEquals("3",table(p).getValueAt(1,3));
            assertTrue(texte(p,"message").contains("1 → 2"));
            assertFalse(bouton(p,"annuler").isEnabled());
        });
    }

    /** Une correction invalide conserve le classement déjà validé. */
    @Test
    void refusePreferencesInvalidesSansLesEcraser() throws Exception {
        surEdt(()->{
            PanneauApplication p=panneau();creer(p,2);preferences(p,0,"2 1");
            for(String invalide:new String[]{"", "1", "1 1", "0 2", "3 2", "x 2", "2147483648 2"}) {
                preferences(p,0,invalide);assertTrue(texte(p,"message").startsWith("Erreur :"));
                assertEquals("[2, 1]",table(p).getValueAt(0,1));
            }
            preferences(p,1,"1 2");bouton(p,"resoudre").doClick();
            assertEquals("2",table(p).getValueAt(0,3));
        });
    }

    /** Les doublons et auto-relations sont traités sans corrompre le graphe. */
    @Test
    void relationsValidees() throws Exception {
        surEdt(()->{
            PanneauApplication p=panneau();creer(p,2);bouton(p,"ajouter").doClick();
            bouton(p,"ajouter").doClick();assertTrue(texte(p,"message").contains("existe déjà"));
            choisir(p,"relationB",0);bouton(p,"ajouter").doClick();
            assertTrue(texte(p,"message").startsWith("Erreur :"));
            assertEquals("[B]",table(p).getValueAt(0,2));assertEquals("[A]",table(p).getValueAt(1,2));
        });
    }

    /** Reconfigurer abandonne l'historique mais conserve relations et préférences. */
    @Test
    void reconfigurationReinitialiseLaSession() throws Exception {
        surEdt(()->{
            PanneauApplication p=panneau();creer(p,2);preferences(p,0,"1 2");preferences(p,1,"1 2");
            bouton(p,"resoudre").doClick();bouton(p,"echanger").doClick();
            assertTrue(bouton(p,"annuler").isEnabled());bouton(p,"reconfigurer").doClick();
            assertFalse(bouton(p,"annuler").isEnabled());assertEquals("—",table(p).getValueAt(0,3));
            assertTrue(bouton(p,"enregistrer").isEnabled());assertEquals("[1, 2]",table(p).getValueAt(0,1));
            bouton(p,"resoudre").doClick();assertEquals("1",table(p).getValueAt(0,3));
            assertFalse(bouton(p,"annuler").isEnabled());
        });
    }

    /** L'échange avec soi-même ne crée pas d'annulation. */
    @Test
    void groupeMinimalEtEchangeNeutre() throws Exception {
        surEdt(()->{
            PanneauApplication p=panneau();creer(p,1);preferences(p,0,"1");bouton(p,"resoudre").doClick();
            bouton(p,"echanger").doClick();assertFalse(bouton(p,"annuler").isEnabled());
            assertTrue(texte(p,"message").contains("aucune modification"));
            assertTrue(texte(p,"bilan").contains("Aucune personne jalouse"));
        });
    }

    /** Les deux bornes graphiques sont respectées lors de la création. */
    @Test
    void creeVingtSixPersonnes() throws Exception {
        surEdt(()->{
            PanneauApplication p=panneau();creer(p,26);
            assertEquals(26,table(p).getRowCount());assertEquals("Z",table(p).getValueAt(25,0));
            assertFalse(bouton(p,"resoudre").isEnabled());
        });
    }

    /** Une saisie libre d'effectif ne doit pas être arrondie ou tronquée par Swing. */
    @Test
    void refuseEffectifMalForme() throws Exception {
        surEdt(()->{
            PanneauApplication p=panneau();
            JSpinner n=(JSpinner)chercher(p,"nombre");
            for(String invalide:new String[]{"0","27","-1","1.5","1,5","abc","999999999999"}) {
                ((JSpinner.DefaultEditor)n.getEditor()).getTextField().setText(invalide);
                bouton(p,"creer").doClick();
                assertEquals(0,table(p).getRowCount());
                assertTrue(texte(p,"message").startsWith("Erreur :"));
            }
        });
    }

    /** Les dépendances obligatoires sont validées. */
    @Test
    void refuseServicesNuls() throws Exception {
        surEdt(()->{
            assertThrows(IllegalArgumentException.class,()->new PanneauApplication(null,new CalculateurJalousie()));
            assertThrows(IllegalArgumentException.class,()->new PanneauApplication(new SolveurNaif(),null));
        });
    }
}
