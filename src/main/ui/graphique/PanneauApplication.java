package ui.graphique;

// Imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import algorithme.SolveurAffectation;
import modele.Chambre;
import modele.Groupe;
import modele.Personne;
import modele.Preferences;
import service.BilanEchange;
import service.CalculateurJalousie;
import service.SessionAffectation;
import ui.PresentationGroupe;

/**
 * Interface Swing construite et manipulée sur le thread des événements (EDT).
 * Elle délègue les règles métier aux mêmes services que la console.
 * Les données métier sont transitoires : la sérialisation de la fenêtre ne constitue
 * pas un mécanisme de sauvegarde. Le panneau peut être testé sans fenêtre et sans écran, avec de vrais événements de boutons.
 */

public final class PanneauApplication extends JPanel {

    // Attributs
    private static final long serialVersionUID = 1L; // identifiant de sérialisation requis par JPanel
    private final transient SolveurAffectation solveur; // stratégie de résolution injectée
    private final transient CalculateurJalousie calculateur; // calculateur injecté
    private transient Groupe groupe; // groupe courant, absent avant création
    private transient SessionAffectation session; // session courante, absente pendant la configuration
    private final TableauGroupe modele = new TableauGroupe(); // modèle du tableau récapitulatif
    private final JTable tableau = new JTable(modele); // tableau de consultation ; aucune modification directe des cellules
    private final JSpinner nombre = new JSpinner(new SpinnerNumberModel(4, 1, 26, 1)); // effectif souhaité, limité à 26
    private final JButton creer = new JButton("Créer le groupe"); // création ou remplacement explicite du groupe
    private final JComboBox<Personne> relationA = new JComboBox<>(); // première personne d'une relation
    private final JComboBox<Personne> relationB = new JComboBox<>(); // seconde personne d'une relation
    private final JButton ajouter = new JButton("Ajouter la relation"); // enregistrement de relation
    private final JComboBox<Personne> personnePreferences = new JComboBox<>(); // personne dont le classement est édité
    private final JTextField preferences = new JTextField(18); // classement saisi sous forme de numéros séparés par des espaces
    private final JButton enregistrer = new JButton("Enregistrer les préférences"); // validation explicite du classement
    private final JButton resoudre = new JButton("Proposer une affectation"); // déclenchement de l'affectation initiale
    private final JButton reconfigurer = new JButton("Revenir à la configuration"); // retour explicite à la configuration, qui abandonne la répartition
    private final JComboBox<Personne> echangeA = new JComboBox<>(); // première personne d'un échange
    private final JComboBox<Personne> echangeB = new JComboBox<>(); // seconde personne d'un échange
    private final JButton echanger = new JButton("Échanger"); // application de l'échange.
    private final JButton annuler = new JButton("Annuler le dernier échange"); // annulation d'un seul échange effectif
    private final JLabel indicateur = new JLabel("Créez un groupe pour commencer."); // indicateur de complétude ou coût courant
    private final JTextArea message = new JTextArea(2, 45); // dernier résultat ou erreur, conservé jusqu'à l'action suivante
    private final JTextArea detail = zoneTexte(); // explication pour la ligne sélectionnée
    private final JTextArea bilan = zoneTexte(); // explications de toutes les personnes jalouses
    private final JTextArea recapitulatif = zoneTexte(); // récapitulatif des relations et préférences

    // Constructeur
    /**
     * Construit le panneau avec ses services, sans créer de fenêtre.
     * @param solveur : stratégie de résolution non nulle
     * @param : calculateur service non nul
     * @throws IllegalArgumentException si une dépendance est nulle
     */
    public PanneauApplication(SolveurAffectation solveur, CalculateurJalousie calculateur) {
        super(new BorderLayout(16, 16));
        if (solveur == null || calculateur == null) {
            throw new IllegalArgumentException("Les services graphiques sont nécessaires.");
        }
        this.solveur = solveur;
        this.calculateur = calculateur;
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(construireEntete(), BorderLayout.NORTH);
        add(construireCommandes(), BorderLayout.WEST);
        add(construireResultats(), BorderLayout.CENTER);
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setOpaque(false);
        message.setName("message");
        add(message, BorderLayout.SOUTH);
        nommerComposants();
        brancherActions();
        actualiser();
    }

    /**
     * Construit l’en-tête et son indicateur de progression.
     * @return en-tête et indicateur de progression
     */
    private JPanel construireEntete() {
        JPanel entete = new JPanel(new GridLayout(0, 1, 0, 6));
        JLabel titre = new JLabel("Organisation des chambres");
        titre.setFont(titre.getFont().deriveFont(Font.BOLD, 25f));
        entete.add(titre);
        entete.add(new JLabel("Configurez le groupe, puis explorez les échanges et leurs effets."));
        indicateur.setFont(indicateur.getFont().deriveFont(Font.BOLD, 15f));
        entete.add(indicateur);
        return entete;
    }

    /**
     * Construit la colonne des commandes.
     * @return colonne de commandes séparées par étape
     */
    private JPanel construireCommandes() {
        JPanel commandes = new JPanel(new BorderLayout());
        commandes.setPreferredSize(new Dimension(310, 0));
        JPanel sections = new JPanel();
        sections.setLayout(new javax.swing.BoxLayout(sections, javax.swing.BoxLayout.Y_AXIS));
        JPanel creation = section("1. Groupe", new JLabel("Nombre de personnes (1 à 26)"), nombre, creer);
        JPanel relations = section("2. Relations « ne s'aiment pas »", ligne(relationA, relationB), ajouter);
        JPanel classement = section("3. Préférences", personnePreferences,
                new JLabel("De la plus appréciée à la moins appréciée"), preferences, enregistrer);
        JPanel resolution = section("4. Affectation", resoudre, reconfigurer);
        reconfigurer.setToolTipText("Conserve le groupe, mais abandonne l'affectation et l'annulation.");
        sections.add(creation);
        sections.add(javax.swing.Box.createVerticalStrut(12));
        sections.add(relations);
        sections.add(javax.swing.Box.createVerticalStrut(12));
        sections.add(classement);
        sections.add(javax.swing.Box.createVerticalStrut(12));
        sections.add(resolution);
        commandes.add(sections, BorderLayout.NORTH);
        return commandes;
    }

    /**
     * Assemble les résultats et leurs explications.
     * @return table, échanges et explications redimensionnables
     */
    private JPanel construireResultats() {
        JPanel resultats = new JPanel(new BorderLayout(0, 10));
        tableau.setRowHeight(30);
        tableau.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableau.getTableHeader().setReorderingAllowed(false);
        tableau.getColumnModel().getColumn(0).setPreferredWidth(70);
        tableau.getColumnModel().getColumn(1).setPreferredWidth(230);
        tableau.getColumnModel().getColumn(2).setPreferredWidth(115);
        tableau.getColumnModel().getColumn(3).setPreferredWidth(65);
        tableau.getColumnModel().getColumn(4).setPreferredWidth(110);
        JScrollPane tableDefilante = new JScrollPane(tableau);
        tableDefilante.setPreferredSize(new Dimension(620, 235));
        JTabbedPane onglets = new JTabbedPane();
        onglets.addTab("Personne sélectionnée", new JScrollPane(detail));
        onglets.addTab("Toutes les jalousies", new JScrollPane(bilan));
        onglets.addTab("Récapitulatif du groupe", new JScrollPane(recapitulatif));
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableDefilante, onglets);
        division.setResizeWeight(0.5);
        division.setDividerLocation(260);
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        barre.add(new JLabel("Échanger"));
        barre.add(echangeA);
        barre.add(new JLabel("avec"));
        barre.add(echangeB);
        barre.add(echanger);
        barre.add(annuler);
        resultats.add(barre, BorderLayout.NORTH);
        resultats.add(division, BorderLayout.CENTER);
        return resultats;
    }

    /** Associe des noms stables aux composants pour les tests et l'accessibilité. */
    private void nommerComposants() {
        JComponent[] composants = {nombre, creer, relationA, relationB, ajouter, personnePreferences,
                preferences, enregistrer, resoudre, reconfigurer, echangeA, echangeB,
                echanger, annuler, tableau, indicateur, detail, bilan, recapitulatif};
        String[] noms = {"nombre", "creer", "relationA", "relationB", "ajouter", "personnePreferences",
                "preferences", "enregistrer", "resoudre", "reconfigurer", "echangeA", "echangeB",
                "echanger", "annuler", "tableau", "indicateur", "detail", "bilan", "recapitulatif"};
        for (int i = 0; i < composants.length; i++) {
            composants[i].setName(noms[i]);
            composants[i].getAccessibleContext().setAccessibleName(noms[i]);
        }
        preferences.setToolTipText("Exemple pour 4 personnes : 1 3 2 4. Validez avec Enregistrer.");
    }

    /** Relie les événements aux actions ; les erreurs attendues restent dans la fenêtre. */
    private void brancherActions() {
        creer.addActionListener(e -> creerGroupe());
        ajouter.addActionListener(e -> tenter(() -> {
            Personne a = selection(relationA);
            Personne b = selection(relationB);
            boolean existe = groupe.getVoisins(a).contains(b);
            groupe.ajouterRelation(a, b);
            informer(existe ? "Cette relation existe déjà." : "Relation enregistrée dans les deux sens.", false);
        }));
        personnePreferences.addActionListener(e -> chargerPreferences());
        enregistrer.addActionListener(e -> tenter(this::enregistrerPreferences));
        preferences.addActionListener(e -> { if (enregistrer.isEnabled()) enregistrer.doClick(); });
        resoudre.addActionListener(e -> tenter(() -> {
            session = new SessionAffectation(groupe, solveur, calculateur);
            informer("Affectation initiale créée. Sélectionnez une personne pour comprendre sa situation.", false);
        }));
        reconfigurer.addActionListener(e -> {
            session = null;
            informer("Configuration réouverte. L'affectation et l'annulation ont été réinitialisées.", false);
            actualiser();
        });
        echanger.addActionListener(e -> tenter(() -> {
            BilanEchange resultat = session.echanger(selection(echangeA), selection(echangeB));
            informer((resultat.modification() ? "Chambres échangées. " : "Même personne : aucune modification. ")
                    + PresentationGroupe.bilan(resultat), false);
        }));
        annuler.addActionListener(e -> tenter(() ->
                informer("Échange annulé. " + PresentationGroupe.bilan(session.annuler()), false)));
        tableau.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualiserDetail();
        });
    }

    /** Valide l'effectif et demande confirmation avant de remplacer un groupe existant. */
    private void creerGroupe() {
        int effectif;
        try {
            String texte = ((JSpinner.DefaultEditor) nombre.getEditor()).getTextField().getText();
            effectif = Integer.parseInt(texte.strip());
            if (effectif < 1 || effectif > 26) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            informer("Entrez un nombre entier entre 1 et 26.", true);
            return;
        }
        nombre.setValue(effectif);
        if (groupe != null && JOptionPane.showConfirmDialog(this,
                "Remplacer le groupe ? Les préférences, relations et affectations seront perdues.",
                "Nouveau groupe", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        tenter(() -> {
            int n = ((Number) nombre.getValue()).intValue();
            if (n < 1 || n > 26) throw new IllegalArgumentException("L'effectif doit être compris entre 1 et 26.");
            List<Personne> personnes = new ArrayList<>();
            List<Chambre> chambres = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                personnes.add(new Personne(String.valueOf((char) ('A' + i))));
                chambres.add(new Chambre(i + 1));
            }
            groupe = new Groupe(personnes, chambres);
            preferences.setToolTipText("Classez chaque chambre de 1 à " + n
                    + " une seule fois, de la plus appréciée à la moins appréciée.");
            session = null;
            remplir(relationA); remplir(relationB); remplir(personnePreferences);
            remplir(echangeA); remplir(echangeB);
            if (n > 1) { relationB.setSelectedIndex(1); echangeB.setSelectedIndex(1); }
            informer("Groupe créé. Renseignez un classement complet pour chaque personne.", false);
        });
    }

    /** Valide toute la saisie avant de remplacer un classement. */
    private void enregistrerPreferences() {
        String saisie = preferences.getText().strip();
        String[] numeros = saisie.isEmpty() ? new String[0] : saisie.split("\\s+");
        if (numeros.length != groupe.getChambres().size()) {
            throw new IllegalArgumentException("Indiquez exactement " + groupe.getChambres().size() + " chambres, séparées par des espaces. L’ancien classement est conservé.");
        }
        List<Chambre> classement = new ArrayList<>();
        for (String numero : numeros) {
            int valeur = Integer.parseInt(numero);
            if (valeur < 1 || valeur > groupe.getChambres().size()) {
                throw new IllegalArgumentException("Chambre inconnue : " + valeur + ". Utilisez les numéros de 1 à "
                        + groupe.getChambres().size() + ". L’ancien classement est conservé.");
            }
            Chambre chambre = groupe.getChambre(valeur);
            if (classement.contains(chambre)) {
                throw new IllegalArgumentException("La chambre " + valeur
                        + " apparaît plusieurs fois. Chaque chambre doit apparaître une seule fois. L’ancien classement est conservé.");
            }
            classement.add(chambre);
        }
        Personne personne = selection(personnePreferences);
        groupe.definirPreferences(personne, new Preferences(classement));
        informer("Préférences enregistrées pour " + personne + ".", false);
    }

    /** Recharge le classement enregistré lors du changement de personne. */
    private void chargerPreferences() {
        Personne personne = (Personne) personnePreferences.getSelectedItem();
        if (groupe == null || personne == null || groupe.personnesSansPreferences().contains(personne)) {
            preferences.setText("");
        } else {
            preferences.setText(groupe.getPreferences(personne).getClassement().stream()
                    .map(c -> String.valueOf(c.getNumero())).collect(java.util.stream.Collectors.joining(" ")));
        }
    }

    /** Met à jour les commandes, la table et les explications après une opération. */
    private void actualiser() {
        boolean configuration = groupe != null && session == null;
        relationA.setEnabled(configuration); relationB.setEnabled(configuration); ajouter.setEnabled(configuration);
        personnePreferences.setEnabled(configuration); preferences.setEnabled(configuration); enregistrer.setEnabled(configuration);
        resoudre.setEnabled(configuration && groupe.estComplet());
        reconfigurer.setEnabled(session != null);
        echangeA.setEnabled(session != null); echangeB.setEnabled(session != null); echanger.setEnabled(session != null);
        annuler.setEnabled(session != null && session.peutAnnuler());
        int ligne = tableau.getSelectedRow();
        modele.actualiser(groupe, session);
        if (modele.getRowCount() > 0) {
            int cible = Math.max(0, Math.min(ligne, modele.getRowCount() - 1));
            tableau.setRowSelectionInterval(cible, cible);
        }
        if (groupe == null) return;
        indicateur.setText(session == null ? "Préférences : " + (groupe.getPersonnes().size()
                - groupe.personnesSansPreferences().size()) + " / " + groupe.getPersonnes().size()
                + " — manquantes : " + groupe.personnesSansPreferences()
                : "Coût : " + session.cout() + " personne(s) jalouse(s) sur " + groupe.getPersonnes().size());
        recapitulatif.setText(PresentationGroupe.configuration(groupe));
        recapitulatif.setCaretPosition(0);
        bilan.setText(session == null ? "La jalousie sera évaluée après l'affectation des chambres."
                : PresentationGroupe.jalousies(groupe, session));
        bilan.setCaretPosition(0);
        actualiserDetail();
    }

    /** Met à jour le détail de la personne sélectionnée sans recalculer la table. */
    private void actualiserDetail() {
        int index = tableau.getSelectedRow();
        if (groupe == null || index < 0 || index >= groupe.getPersonnes().size()) {
            detail.setText("Sélectionnez une personne dans le tableau.");
        } else if (session == null) {
            detail.setText("La chambre et la jalousie seront affichées après la résolution.\n"
                    + "Vérifiez les préférences et les relations dans le récapitulatif.");
        } else {
            detail.setText(PresentationGroupe.personne(groupe.getPersonnes().get(index), session));
        }
        detail.setCaretPosition(0);
    }

    /**
     * Exécute une action et affiche les erreurs de saisie ou d'état attendues.
     * @param action : opération à exécuter sur l'EDT
     */
    private void tenter(Runnable action) {
        try {
            action.run();
            actualiser();
        } catch (NumberFormatException e) {
            informer("Chaque chambre doit être un numéro entier valide. Séparez les numéros par des espaces. L’ancien classement est conservé.", true);
        } catch (IllegalArgumentException | IllegalStateException e) {
            informer(e.getMessage(), true);
        }
    }

    /**
     * Affiche un message avec un libellé explicite, sans dépendre seulement de sa couleur.
     * @param texte : message à montrer
     * @param erreur : vrai pour une saisie refusée
     */
    private void informer(String texte, boolean erreur) {
        message.setText((erreur ? "Erreur : " : "") + texte);
        message.setForeground(erreur ? new Color(160, 35, 35) : new Color(30, 70, 90));
    }

    /**
     * Remplit une liste de sélection avec les personnes du groupe courant.
     * @param liste : composant à remplir
     */
    private void remplir(JComboBox<Personne> liste) {
        liste.removeAllItems();
        for (Personne personne : groupe.getPersonnes()) liste.addItem(personne);
    }

    /**
     * Lit une personne choisie dans une liste active.
     * @param liste : liste de sélection
     * @return personne choisie
     * @throws IllegalStateException si aucune personne n'est sélectionnée
     */
    private Personne selection(JComboBox<Personne> liste) {
        Personne personne = (Personne) liste.getSelectedItem();
        if (personne == null) throw new IllegalStateException("Sélectionnez une personne.");
        return personne;
    }

    /**
     * Assemble une section de formulaire.
     * @param titre : nom de l'étape
     * @param composants : éléments à empiler
     * @return panneau de formulaire
     */
    private static JPanel section(String titre, JComponent... composants) {
        JPanel panneau = new JPanel(new GridLayout(0, 1, 0, 5));
        panneau.setBorder(BorderFactory.createTitledBorder(titre));
        for (JComponent composant : composants) panneau.add(composant);
        return panneau;
    }

    /**
     * Aligne deux listes de sélection.
     * @param a : première liste
     * @param b : seconde liste
     * @return ligne de formulaire
     */
    private static JPanel ligne(JComponent a, JComponent b) {
        JPanel panneau = new JPanel(new GridLayout(1, 2, 8, 0));
        panneau.add(a); panneau.add(b);
        return panneau;
    }

    /**
     * Crée une zone de texte pour les explications.
     * @return zone de texte non éditable, avec retour à la ligne et marge
     */
    private static JTextArea zoneTexte() {
        JTextArea zone = new JTextArea();
        zone.setEditable(false);
        zone.setLineWrap(true);
        zone.setWrapStyleWord(true);
        zone.setMargin(new java.awt.Insets(12, 12, 12, 12));
        zone.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        return zone;
    }
}
