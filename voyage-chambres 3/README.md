# Organisation des chambres — Partie 1, lot 6

Projet de Programmation Avancée et Application, Licence 3. Version 0.6.0.
Auteurs du groupe : **à compléter avant le rendu**.

Cette version reprend les lots précédents et permet maintenant une session complète :
création du groupe, relations, préférences, affectation gloutonne, échanges et calcul
de la jalousie. Les entrées du mauvais type sont déjà gérées.

## Prérequis

- JDK 21 ; le JDK intégré à Eclipse convient s'il contient `javac` et `javadoc`.
- Eclipse avec support Java et Maven (m2e), ou Maven 3.9.x pour travailler au terminal.
- Internet pour le premier téléchargement des dépendances Maven.
- Sources et documentation en UTF-8. JDK 21 utilise UTF-8 par défaut pour les fichiers.

## Importer ou mettre à jour dans Eclipse

Pour un nouveau projet : extraire complètement le ZIP, puis **File > Import > Maven >
Existing Maven Projects**. Sélectionner le dossier `voyage-chambres` contenant `pom.xml`,
puis **Finish**. Ne pas importer le ZIP comme un projet Java ordinaire.

Pour mettre à jour le lot 5 : conserver une copie de vos modifications personnelles,
puis comparer/copier `src`, `docs`, `exemples`, `pom.xml` et `README.md` depuis l'archive.
Conserver les réglages Eclipse de votre machine. Faire **Refresh (F5)**, puis
**Maven > Update Project**. Ne pas importer deux projets de même nom dans le workspace.

Vérifier **Window > Preferences > Java > Installed JREs**, puis l'environnement
**JavaSE-21** dans **Execution Environments**. Ne pas ajouter de `module-info.java`.
Les fichiers Eclipse propres à une machine et le dossier `target` ne sont pas fournis.

## Démarrer dans Eclipse

1. Ouvrir `src/main/java/fr/univ/paris/voyage/app/Main.java`.
2. Clic droit > **Run As > Java Application**.
3. Cliquer dans la vue **Console**, saisir une réponse et appuyer sur Entrée.
4. Commencer par un nombre entier compris entre 1 et 26.

Les personnes sont créées dans l'ordre A à Z et les chambres de 1 à n.
La console exige les noms en majuscules. Les espaces multiples et tabulations
sont acceptés entre les éléments ; les espaces en début et fin sont ignorés.

### Premier menu : configuration

```text
1. Ajouter une relation entre deux personnes
2. Ajouter ou modifier les préférences d'une personne
3. Terminer la configuration et attribuer les chambres
4. Consulter le récapitulatif du groupe
```

Après le choix 1, saisir deux noms : `A B`. La relation est symétrique. Un doublon
est signalé sans ajouter de nouvelle relation ; une relation avec soi-même est refusée.

Après le choix 2, saisir le nom puis toutes les chambres dans l'ordre préféré.
Pour trois personnes, `A 3 1 2` signifie que A préfère 3, puis 1, puis 2.
Chaque chambre doit apparaître exactement une fois. Une saisie valide remplace
l'ancien classement ; une erreur le laisse intact. Le menu réapparaît après la saisie.

Le choix 4 affiche les relations et classements déjà renseignés, sans lancer la résolution.
Le choix 3 reste bloqué tant que des préférences manquent et affiche les personnes
concernées. Quand le groupe est complet, le solveur propose automatiquement une
répartition et l'affiche. Il attribue à chacun sa meilleure chambre encore libre.

### Second menu : affectation

```text
1. Échanger les chambres de deux personnes
2. Afficher le nombre de personnes jalouses et les explications
3. Quitter
4. Annuler le dernier échange
```

Après le choix 1, saisir deux noms, par exemple `B C`. L'échange a lieu même s'il
augmente la jalousie : il s'agit d'une décision manuelle. Un nom invalide ne change
rien ; deux noms identiques donnent une opération sans effet, signalée à l'écran.
Le choix 2 calcule le coût courant, sans le mettre en cache. Chaque personne jalouse
compte une fois, même si elle jalouse plusieurs voisins. Les raisons détaillées sont affichées.
Le choix 4 annule le dernier échange effectif, une seule fois. Un échange refusé ou
un échange avec soi-même conserve l’annulation précédente. Un nouvel échange effectif la remplace.

La répartition est rappelée après chaque action, y compris un choix invalide ou
l'option Quitter, avec une attribution par ligne :

```text
A:1
B:3
C:2
D:4
```

Une fin d'entrée à n'importe quelle invite termine proprement le programme.
Un entier invalide, un choix inconnu ou une saisie incorrecte ne produit pas de trace
technique à l'écran. Une véritable erreur de lecture est signalée par `Main`.

## Construire et lancer au terminal

Depuis le dossier contenant `pom.xml`, avec Java et Maven accessibles dans le PATH :

```sh
mvn clean verify
java -jar target/voyage-chambres-0.6.0.jar
mvn javadoc:javadoc
```

Dans Eclipse, la construction équivalente est **Run As > Maven build...**, Goals
`clean verify`, avec Java 21 dans l'onglet JRE. Le JAR est maintenant exécutable ;
son manifeste désigne `fr.univ.paris.voyage.app.Main`. Il n'a aucune dépendance
externe à l'exécution. JUnit est réservé aux tests.

Le programme n'accepte pas encore d'argument de fichier : cela relève de la partie 2.
Un argument supplémentaire affiche l'usage sans démarrer la session.
La redirection ci-dessous rejoue uniquement des réponses console, pas un format
d'import de groupe :

```sh
# Linux/macOS ou invite de commandes Windows (cmd)
java -jar target/voyage-chambres-0.6.0.jar < exemples/demo-sujet.txt
```

PowerShell n'utilise pas cette syntaxe de redirection ; utiliser cmd ou saisir la démo
manuellement dans Eclipse. Le déroulement et les résultats attendus sont détaillés
dans [docs/DEMONSTRATION.md](docs/DEMONSTRATION.md).

## Tests et documentation

Dans Eclipse : clic droit sur `src/test/java` > **Run As > JUnit Test**.
Avec Maven : `mvn clean verify`. Résultat attendu : **199 tests réussis**.
Les tests paramétrés exécutent plusieurs cas par méthode.
Le bilan exact des tests et du lancement du JAR se trouve dans [docs/VALIDATION.md](docs/VALIDATION.md).

Les tests couvrent les invariants métier, la résolution et les parcours console
avec des lecteurs/rédacteurs en mémoire. Tous les tests d'intégration se terminent
par `Test` et sont découverts automatiquement par Surefire.

`mvn javadoc:javadoc` produit `target/reports/apidocs/index.html`. Le contrôle porte
sur les classes, attributs, constructeurs et méthodes, y compris privés.
La compilation utilise `-Xlint:all` et `-Werror` ; les avertissements Javadoc sont bloquants.

## Organisation et responsabilités

| Package / chemin | Rôle |
| --- | --- |
| `modele` | Personne, Chambre, Preferences, Groupe, Affectation |
| `service` | CalculateurJalousie, SessionAffectation et BilanEchange |
| `algorithme` | SolveurAffectation et SolveurNaif |
| `ui` | InterfaceConsole et PresentationGroupe : menus et explications communes |
| `ui.graphique` | PanneauApplication et TableauGroupe : fenêtre Swing |
| `app` | Main (console) et MainGraphique (fenêtre) |
| `src/test/java` | Tests métier, du solveur et de la console |
| `docs/COMPLEXITE.md` | Correction et complexité du solveur, conservées depuis le lot 5 |
| `docs/ARCHITECTURE.md` | Choix de conception et lecture du schéma |
| `docs/Architecture_Partie1.drawio` | UML du code actuel, ouvrable dans draw.io |
| `docs/CHECKLIST.md` | Suivi des fonctionnalités et vérifications avant rendu |
| `docs/DEMONSTRATION.md` | Scénario de présentation reproductible |
| `exemples/demo-sujet.txt` | Réponses console de l'exemple du sujet |

`Personne` et `Chambre` sont immuables, identifiées par nom ou numéro. `Preferences`
conserve une liste pour le parcours et une map pour les rangs. `Groupe` conserve
les relations symétriques et les préférences. `Affectation` conserve une map indépendante
personne → chambre. Seuls les échanges modifient la répartition après sa construction.
Les collections exposées sont protégées. Les opérations invalides ne modifient pas l'état.

Le solveur est sans état et ne modifie pas le groupe. Sa disponibilité est indexée
par numéro de chambre : temps O(n²) au pire, mémoire supplémentaire O(n).
La [démonstration complète](docs/COMPLEXITE.md) reste incluse ; le glouton ne garantit
pas le minimum du nombre de personnes jalouses.

## Lire le nouveau code

1. `Main.main` : assemblage des services et gestion d'une erreur de lecture.
2. `InterfaceConsole.executer` : déroulement de la session.
3. `creerGroupe`, `configurer`, puis `gererAffectation` : les trois étapes.
4. `lireLigne` et les méthodes de saisie : validation sans mélanger mots et lignes.
5. `InterfaceConsoleTest` et `SessionConsoleTest` : erreurs puis parcours complets.

Les flux appartiennent à l'appelant et ne sont pas fermés par la console.
Les saisies de menus sont converties en entiers décimaux : une erreur de conversion
est traduite en `InputMismatchException`, traitée au niveau du menu. Les préférences
utilisent `NumberFormatException` pour distinguer les numéros mal formés des erreurs
métier. Les `IOException` conservées par `Scanner` sont propagées jusqu'à `Main`.
Il n'y a pas encore de lecture de fichier métier, donc pas de `FileNotFoundException`
à traiter à ce stade. La gestion des fichiers appartient à la partie 2.

## Dépannage

- `java` non reconnu : le terminal n'a pas Java dans son PATH ; Eclipse peut toujours
  utiliser son JDK configuré. Pour lancer le JAR, utiliser le chemin complet de `java.exe`.
- `release version 21 not supported` / `No compiler is provided` : sélectionner un JDK 21.
- Imports JUnit non résolus : vérifier la connexion, puis **Maven > Update Project**.
- `no main manifest attribute` : reconstruire avec le POM du lot 6 via `mvn clean verify`.
- Affichage d'une ancienne version : arrêter la session précédente et relancer `Main`.
- Accents incorrects : vérifier l'encodage UTF-8 dans Eclipse et le terminal utilisé.
- Aucune saisie possible : sélectionner la vue Console du processus en cours.

## Avant le rendu

Compléter les auteurs, vérifier le projet sur les deux machines et répéter la démo.
Les contrôles automatisés ont été réalisés sous Linux ; la vérification visuelle dans
Eclipse Windows reste à effectuer par le groupe. Livrer les sources, le POM, la doc
et les exemples, sans `target` ni chemins propres à une machine.
Les améliorations automatiques de la solution, l'import/export métier et les chemins
de fichiers passés en arguments seront traités lors de la partie 2.

## Fenêtre graphique (lot 6)

Dans Eclipse, lancer `MainGraphique.java` avec **Run As > Java Application**.
Au terminal, après construction :

```sh
java -cp target/voyage-chambres-0.6.0.jar fr.univ.paris.voyage.app.MainGraphique
```

Créer le groupe, saisir les relations puis les classements de chaque personne.
Le bouton **Proposer une affectation** devient disponible lorsque tous les classements
sont renseignés. Le tableau et l’onglet récapitulatif permettent de les relire avant résolution.
Sélectionner une ligne pour comprendre la jalousie de la personne. Les listes d’échange,
le bilan et le bouton d’annulation permettent ensuite de comparer deux répartitions.
Le retour à la configuration conserve les classements et relations mais abandonne
l’affectation et son historique. Créer un autre groupe demande confirmation avant
l’effacement du groupe précédent. Rien n’est sauvegardé sur disque en quittant.

## Messages et scénarios du lot 6

La console conserve les options 1 à 3 des menus initiaux et ajoute l’option 4.
Les exemples de classement s’adaptent au nombre de chambres ; les erreurs indiquent
la cause, les valeurs attendues et l’action pour réessayer. Les noms restent en majuscules.
Les affectations gardent le format `A:1` demandé. Le score mesure un nombre de personnes,
non un nombre de relations. Un échange peut améliorer, dégrader ou laisser ce score inchangé.
Voir `docs/AMELIORATIONS_LOT6.md` et `exemples/demo-lot6.txt`.
