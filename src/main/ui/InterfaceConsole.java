package ui;

// Imports
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import algorithme.SolveurAffectation;
import service.SessionAffectation;
import service.BilanEchange;
import modele.Chambre;
import modele.Groupe;
import modele.Personne;
import modele.Preferences;
import service.CalculateurJalousie;

/**
 * Pilote une session de configuration puis de manipulation d'une affectation.
 * Les règles métier restent dans le modèle et les services. Les flux sont fournis
 * par l'appelant et ne sont jamais fermés ici, notamment pour préserver System.in.
 */
public final class InterfaceConsole {

    // Attributs
    private final Scanner entree; // lecteur de lignes, afin qu'une saisie incorrecte soit consommée entièrement
    private final PrintWriter sortie; // destination des messages et des invites
    private final SolveurAffectation solveur; // stratégie de construction de l'affectation initiale
    private final CalculateurJalousie calculateur; // service chargé d'évaluer la jalousie

    // Constructeur
    /**
     * Prépare une console dont les entrées et sorties peuvent être simulées en test.
     * @param entree : source de caractères, conservée ouverte
     * @param sortie : destination des messages, conservée ouverte
     * @param solveur : algorithme utilisé après validation de la configuration
     * @param calculateur : service de calcul du coût
     * @throws IllegalArgumentException si une dépendance est nulle
     */
    public InterfaceConsole(Readable entree, PrintWriter sortie,
            SolveurAffectation solveur, CalculateurJalousie calculateur) {
        if (entree == null || sortie == null || solveur == null || calculateur == null) {
            throw new IllegalArgumentException("Les dépendances de la console sont nécessaires.");
        }
        this.entree = new Scanner(entree);
        this.sortie = sortie;
        this.solveur = solveur;
        this.calculateur = calculateur;
    }

    // Méthodes
    /**
     * Exécute une session jusqu'à Quitter ou la fin de l'entrée.
     * Une fin d'entrée termine proprement la session, même au milieu d'une saisie.
     * @throws IOException si la source signale une erreur de lecture
     */
    public void executer() throws IOException {
        sortie.println("=== Organisation des chambres ===");
        sortie.println("Étape 1 : créez le groupe.");
        sortie.println("Étape 2 : renseignez ses préférences et relations.");
        sortie.println("Étape 3 : consultez la répartition proposée et essayez des échanges.");
        sortie.println("Une relation signifie que deux personnes ne s’aiment pas ; elle est réciproque.");
        sortie.println("Une personne est jalouse si elle préfère la chambre d’une personne avec laquelle elle a une relation à la sienne.");
        sortie.println("Les données restent en mémoire pendant cette session uniquement.");
        Groupe groupe = creerGroupe();
        if (groupe == null || !configurer(groupe)) {
            terminerSurFinEntree();
            return;
        }
        sortie.println(PresentationGroupe.configuration(groupe));
        SessionAffectation affectation = new SessionAffectation(groupe, solveur, calculateur);
        sortie.println("Chaque personne, dans l’ordre du groupe, reçoit sa chambre préférée encore libre.");
        sortie.println("Cette méthode ne garantit pas le minimum de jalousies.");
        sortie.println("Affectation initiale proposée :");
        afficherAffectation(groupe, affectation);
        sortie.println(PresentationGroupe.jalousies(groupe, affectation));
        if (!gererAffectation(groupe, affectation)) {
            terminerSurFinEntree();
        }
        sortie.flush();
    }

    /**
     * Demande un effectif valide et crée les personnes A à Z et les chambres 1 à n.
     * @return groupe créé, ou null si l'entrée se termine
     * @throws IOException si la lecture échoue
     */
    private Groupe creerGroupe() throws IOException {
        while (true) {
            String ligne = lireLigne("Nombre de personnes (1 à 26) : ");
            if (ligne == null) {
                return null;
            }
            try {
                int nombre = convertirEntier(ligne);
                if (nombre < 1 || nombre > 26) {
                    sortie.println("Le nombre de personnes doit être compris entre 1 et 26.");
                    continue;
                }
                List<Personne> personnes = new ArrayList<>();
                List<Chambre> chambres = new ArrayList<>();
                for (int i = 0; i < nombre; i++) {
                    personnes.add(new Personne(String.valueOf((char) ('A' + i))));
                    chambres.add(new Chambre(i + 1));
                }
                sortie.println("Groupe créé : " + personnes);
                sortie.println("Chambres disponibles : " + chambres + ". Une chambre par personne.");
                sortie.println("Les relations sont facultatives ; le classement de chaque personne est obligatoire.");
                return new Groupe(personnes, chambres);
            } catch (InputMismatchException e) {
                sortie.println("Saisie incorrecte : entrez un seul nombre entier.");
            }
        }
    }

    /**
     * Affiche le premier menu jusqu'à obtention d'une configuration complète.
     * @param groupe : groupe à configurer
     * @return vrai si la configuration est terminée, faux en fin d'entrée
     * @throws IOException si la lecture échoue
     */
    private boolean configurer(Groupe groupe) throws IOException {
        while (true) {
            sortie.println("\n--- Configuration ---");
            sortie.println("1. Ajouter une relation entre deux personnes");
            sortie.println("2. Ajouter ou modifier les préférences d'une personne");
            sortie.println("3. Terminer la configuration et attribuer les chambres");
            sortie.println("4. Consulter le récapitulatif du groupe");
            sortie.println("Classements renseignés : " + (groupe.getPersonnes().size()
                    - groupe.personnesSansPreferences().size()) + "/" + groupe.getPersonnes().size());
            String ligne = lireLigne("Votre choix : ");
            if (ligne == null) {
                return false;
            }
            try {
                switch (convertirEntier(ligne)) {
                    case 1 -> {
                        if (!ajouterRelation(groupe)) {
                            return false;
                        }
                    }
                    case 2 -> {
                        if (!saisirPreferences(groupe)) {
                            return false;
                        }
                    }
                    case 3 -> {
                        if (groupe.estComplet()) {
                            return true;
                        }
                        sortie.println("Préférences manquantes pour : " + groupe.personnesSansPreferences());
                        sortie.println("Choisissez 2 pour renseigner ces classements, puis 3 pour continuer.");
                    }
                    case 4 -> sortie.println(PresentationGroupe.configuration(groupe));
                    default -> sortie.println("Choix inconnu : utilisez 1, 2, 3 ou 4.");
                }
            } catch (InputMismatchException e) {
                sortie.println("Saisie incorrecte : entrez un seul nombre entier.");
            }
        }
    }

    /**
     * Lit et valide une relation ; une erreur laisse le groupe inchangé.
     * @param groupe : groupe à compléter
     * @return faux uniquement en fin d'entrée
     * @throws IOException si la lecture échoue
     */
    private boolean ajouterRelation(Groupe groupe) throws IOException {
        sortie.println("Indiquez deux personnes qui ne s’aiment pas. Inutile de saisir la relation inverse.");
        sortie.println("Personnes disponibles : " + groupe.getPersonnes());
        String ligne = lireLigne("Deux noms en majuscules séparés par un espace : ");
        if (ligne == null) {
            return false;
        }
        try {
            Personne[] personnes = lireDeuxPersonnes(ligne, groupe);
            boolean existe = groupe.getVoisins(personnes[0]).contains(personnes[1]);
            groupe.ajouterRelation(personnes[0], personnes[1]);
            sortie.println(existe ? "Cette relation existe déjà. Aucune nouvelle relation ajoutée."
                    : "Relation enregistrée dans les deux sens. " + personnes[0] + " et " + personnes[1] + " ne s’aiment pas.");
        } catch (IllegalArgumentException e) {
            sortie.println("Relation refusée : " + e.getMessage());
            sortie.println("Relations inchangées. Choisissez 1 pour réessayer avec deux personnes distinctes du groupe.");
        }
        return true;
    }

    /**
     * Lit un classement complet et ne remplace l'ancien qu'après validation.
     * @param groupe : groupe à compléter
     * @return faux uniquement en fin d'entrée
     * @throws IOException si la lecture échoue
     */
    private boolean saisirPreferences(Groupe groupe) throws IOException {
        String exemple = "A " + groupe.getChambres().stream().map(c -> Integer.toString(c.getNumero()))
                .collect(java.util.stream.Collectors.joining(" "));
        sortie.println("Classez toutes les chambres de la plus appréciée à la moins appréciée, sans répétition.");
        sortie.println("Personnes disponibles : " + groupe.getPersonnes());
        sortie.println("Pour corriger un classement, saisissez à nouveau la personne et son classement complet.");
        String ligne = lireLigne("Nom puis classement (exemple : " + exemple + ") : ");
        if (ligne == null) {
            return false;
        }
        try {
            String[] mots = decouper(ligne);
            int nombre = groupe.getChambres().size();
            if (mots.length != nombre + 1) {
                throw new IllegalArgumentException("Indiquez un nom suivi de " + nombre + " numéros de chambre.");
            }
            Personne personne = rechercherPersonne(mots[0], groupe);
            List<Chambre> classement = new ArrayList<>();
            for (int i = 1; i < mots.length; i++) {
                int numero = Integer.parseInt(mots[i]);
                if (numero < 1 || numero > nombre) {
                    throw new IllegalArgumentException("La chambre " + numero + " n’existe pas. Utilisez les numéros de 1 à " + nombre + ".");
                }
                Chambre chambre = groupe.getChambre(numero);
                if (classement.contains(chambre)) {
                    throw new IllegalArgumentException("La chambre " + numero + " apparaît plusieurs fois. Chaque chambre doit apparaître une seule fois.");
                }
                classement.add(chambre);
            }
            groupe.definirPreferences(personne, new Preferences(classement));
            sortie.println("Préférences enregistrées pour " + personne + ". Classement : " + classement
                    + " (de la plus appréciée à la moins appréciée).");
        } catch (NumberFormatException e) {
            sortie.println("Préférences refusées : chaque chambre doit être un numéro entier valide.");
            expliquerCorrectionPreferences();
        } catch (IllegalArgumentException e) {
            sortie.println("Préférences refusées : " + e.getMessage());
            expliquerCorrectionPreferences();
        }
        return true;
    }

    /**
     * Propose échanges et calculs, puis rappelle l'affectation après chaque action.
     * @param groupe : groupe configuré
     * @param affectation : répartition modifiée par les échanges
     * @return vrai pour Quitter, faux en fin d'entrée
     * @throws IOException si la lecture échoue
     */
    private boolean gererAffectation(Groupe groupe, SessionAffectation affectation) throws IOException {
        while (true) {
            sortie.println("\n--- Affectation ---");
            sortie.println("1. Échanger les chambres de deux personnes");
            sortie.println("2. Afficher le nombre de personnes jalouses et les explications");
            sortie.println("3. Quitter");
            sortie.println("4. Annuler le dernier échange");
            sortie.println(affectation.peutAnnuler() ? "Une annulation est disponible (dernier échange uniquement)."
                    : "Aucun échange à annuler pour le moment.");
            String ligne = lireLigne("Votre choix : ");
            if (ligne == null) {
                return false;
            }
            try {
                switch (convertirEntier(ligne)) {
                    case 1 -> {
                        if (!echanger(groupe, affectation)) {
                            return false;
                        }
                    }
                    case 2 -> {
                        sortie.println("Nombre de personnes jalouses : " + affectation.cout());
                        sortie.println(PresentationGroupe.jalousies(groupe, affectation));
                    }
                    case 4 -> {
                        if (affectation.peutAnnuler()) {
                            sortie.println("Échange annulé. " + PresentationGroupe.bilan(affectation.annuler()));
                        } else {
                            sortie.println("Aucun échange à annuler.");
                        }
                    }
                    case 3 -> {
                        afficherAffectation(groupe, affectation);
                        sortie.println("Fin de session : aucune sauvegarde sur fichier n’est effectuée.");
                        sortie.println("Au revoir !");
                        return true;
                    }
                    default -> sortie.println("Choix inconnu : utilisez 1, 2, 3 ou 4.");
                }
            } catch (InputMismatchException e) {
                sortie.println("Saisie incorrecte : entrez un seul nombre entier.");
            }
            afficherAffectation(groupe, affectation);
        }
    }

    /**
     * Demande deux personnes et échange leurs chambres si les noms sont valides.
     * @param groupe : groupe utilisé pour résoudre les noms
     * @param affectation : répartition courante
     * @return faux uniquement en fin d'entrée
     * @throws IOException si la lecture échoue
     */
    private boolean echanger(Groupe groupe, SessionAffectation affectation) throws IOException {
        sortie.println("L’échange sera appliqué même s’il augmente la jalousie. L’option 4 permet de l’annuler.");
        sortie.println("Personnes disponibles : " + groupe.getPersonnes());
        String ligne = lireLigne("Deux noms en majuscules à échanger, séparés par un espace : ");
        if (ligne == null) {
            return false;
        }
        try {
            Personne[] personnes = lireDeuxPersonnes(ligne, groupe);
            BilanEchange bilan = affectation.echanger(personnes[0], personnes[1]);
            sortie.println(personnes[0].equals(personnes[1])
                    ? "Même personne : affectation inchangée." : "Chambres échangées.");
            sortie.println(PresentationGroupe.bilan(bilan));
            if (bilan.modification()) {
                sortie.println(personnes[0] + " reçoit la chambre " + affectation.chambreDe(personnes[0]).getNumero()
                        + " ; " + personnes[1] + " reçoit la chambre " + affectation.chambreDe(personnes[1]).getNumero() + ".");
            }
        } catch (IllegalArgumentException e) {
            sortie.println("Échange refusé : " + e.getMessage());
            sortie.println("Répartition inchangée. Choisissez 1 pour réessayer ; l’annulation précédente reste disponible si elle existait.");
        }
        return true;
    }

    /**
     * Résout deux noms sans modifier le groupe ni l'affectation.
     * @param ligne : saisie contenant les deux noms
     * @param groupe : groupe où rechercher les personnes
     * @return tableau des deux personnes dans l'ordre saisi
     * @throws IllegalArgumentException si le format ou un nom est invalide
     */
    private Personne[] lireDeuxPersonnes(String ligne, Groupe groupe) {
        String[] mots = decouper(ligne);
        if (mots.length != 2) {
            throw new IllegalArgumentException("Indiquez exactement deux noms séparés par un espace.");
        }
        return new Personne[]{rechercherPersonne(mots[0], groupe), rechercherPersonne(mots[1], groupe)};
    }

    /** Explique comment réessayer sans laisser croire qu'un classement a été effacé. */
    private void expliquerCorrectionPreferences() {
        sortie.println("Aucun classement modifié : les préférences déjà enregistrées sont conservées.");
        sortie.println("Choisissez 2 pour réessayer avec toutes les chambres, séparées par des espaces.");
    }

    /**
     * Résout un nom et fournit une aide adaptée au groupe en cas d'erreur.
     * @param nom : nom saisi, sensible à la casse
     * @param groupe : groupe courant
     * @return personne enregistrée
     * @throws IllegalArgumentException si le nom est inconnu
     */
    private Personne rechercherPersonne(String nom, Groupe groupe) {
        try {
            return groupe.getPersonne(nom);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Personne inconnue : « " + nom
                    + " ». Utilisez un nom en majuscules parmi " + groupe.getPersonnes() + ".", e);
        }
    }

    /**
     * Sépare une ligne sur les espaces ou tabulations, en ignorant ses bords.
     * @param ligne : ligne non nulle
     * @return mots saisis, ou tableau vide pour une ligne blanche
     */
    private String[] decouper(String ligne) {
        String texte = ligne.strip();
        return texte.isEmpty() ? new String[0] : texte.split("\\s+");
    }

    /**
     * Convertit une ligne entière en entier décimal, sans accepter de second jeton.
     * @param ligne : ligne non nulle à convertir
     * @return valeur entière
     * @throws InputMismatchException si le format est invalide ou dépasse la capacité d'un int
     */
    private int convertirEntier(String ligne) {
        try {
            return Integer.parseInt(ligne.strip());
        } catch (NumberFormatException e) {
            throw new InputMismatchException("Un entier décimal est attendu.");
        }
    }

    /**
     * Écrit une invite puis lit une ligne sans mélanger lecture de mots et de lignes.
     * Les erreurs conservées par Scanner sont propagées au point d'entrée.
     * @param invite : texte précédant la saisie
     * @return ligne saisie, ou null si l'entrée se termine normalement
     * @throws IOException si la source de caractères échoue
     */
    private String lireLigne(String invite) throws IOException {
        sortie.print(invite);
        sortie.flush();
        boolean disponible = entree.hasNextLine();
        if (entree.ioException() != null) {
            throw entree.ioException();
        }
        return disponible ? entree.nextLine() : null;
    }

    /**
     * Affiche chaque attribution sur une ligne, au format A:2 demandé par le sujet.
     * @param groupe : groupe donnant l'ordre des personnes
     * @param affectation : répartition à afficher
     */
    private void afficherAffectation(Groupe groupe, SessionAffectation affectation) {
        sortie.println("Répartition actuelle :");
        for (Personne personne : groupe.getPersonnes()) {
            sortie.println(personne.getNom() + ":" + affectation.chambreDe(personne).getNumero());
        }
    }

    /** Signale une fin d'entrée normale et vide le tampon de sortie. */
    private void terminerSurFinEntree() {
        sortie.println("\nFin de l'entrée. Programme terminé.");
        sortie.println("La saisie inachevée n’a pas été appliquée. Aucune sauvegarde sur fichier n’est effectuée.");
        sortie.flush();
    }
}
