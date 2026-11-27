# Solver : organisation des chambres

Projet de L3 Informatique MIAGE (Programmation Avancée et Application), réalisé par
**Cléomène More** et **William Ourfahli**.

## Présentation

Ce projet permet de répartir un groupe de personnes dans des chambres, avec une chambre par personne. Chaque personne peut exprimer ses préférences et indiquer les personnes avec lesquelles elle entretient une relation.

L'application propose ensuite une première répartition en attribuant à chaque personne sa meilleure chambre encore disponible, en suivant l'ordre du groupe. Il est ensuite possible d'échanger manuellement deux chambres et de consulter les explications liées à la jalousie. Le dernier échange effectif peut également être annulé.

Le projet propose deux façons d'utiliser l'application :
- une interface en console, avec des menus et des saisies guidées ;
- une interface graphique réalisée avec Swing.

Le but du programme est de répartir les chambres en limitant le nombre de personnes jalouses.

## Fonctionnalités principales

- Crée un groupe de 1 à 26 personnes (nommées A, B, C… Z) avec les chambres 1 à n.
- Permet d'ajouter des relations entre deux personnes (elles marchent dans les deux sens).
- Permet de saisir, et de corriger, le classement des chambres de chaque personne.
- Affiche un récapitulatif du groupe avant de lancer le calcul.
- Propose une première répartition automatique.
- Compte les personnes jalouses et explique pourquoi (qui jalouse qui).
- Permet d'échanger les chambres de deux personnes et montre le nombre de jaloux avant et après.
- Permet d'annuler le dernier échange (un seul niveau d'annulation).
- Fonctionne de deux façons : en console ou dans une fenêtre Swing.


## Technologies utilisées

- **Java 21** : langage de programmation.
- **Swing** : interface graphique.
- **Maven** : compilation et gestion du projet.
- **JUnit 5** : tests automatisés.

## Comment ça marche ?

La répartition automatique est un algorithme glouton. On passe les personnes dans
l'ordre (A, B, C…) et chacune reçoit sa chambre préférée parmi celles qui sont encore libres.
Il est rapide, mais il ne tient pas compte des relations. Il ne garantit donc pas le
plus petit nombre de jaloux. C'est pour cela que l'on peut ensuite faire des échanges
à la main et voir si le résultat s'améliore.

## Organisation du projet

Le code est découpé en cinq packages :

- `modele` qui contient : `Personne`, `Chambre`, `Preferences`, `Groupe`, `Affectation`
- `algorithme` qui contient l'interface `SolveurAffectation` et le solveur glouton `SolveurNaif`
- `service` qui s'occupe de : calcul de la jalousie (`CalculateurJalousie`), session d'échanges et d'annulation (`SessionAffectation`), bilan d'un échange (`BilanEchange`)
- `ui` : la console (`InterfaceConsole`), les textes communs (`PresentationGroupe`), et la fenêtre Swing dans `ui.graphique` (`PanneauApplication`, `TableauGroupe`)
- `app` : les deux points d'entrée : `Main` (console) et `MainGraphique` (fenêtre)

Le modèle ne dépend ni de la console ni de Swing. Les deux interfaces utilisent
les mêmes services.

## Exemple

Groupe de 4 personnes. B ne s'entend pas avec A, C et D. Classements :

```text
A : 1 2 3 4
B : 1 3 2 4
C : 3 2 1 4
D : 1 4 2 3
```

Le solveur donne `A:1, B:3, C:2, D:4`. B et C sont jalouses, donc 2 jaloux.
Si on échange les chambres de B et C, on obtient `A:1, B:2, C:3, D:4` et il ne reste que B.

Ce scénario est dans `exemples/demo-sujet.txt`.

## Lancer le projet
### Prérequis

- Un JDK 21.
- Maven 3.9 ou une version compatible.
- Une connexion Internet lors de la première compilation pour télécharger les dépendances.

Depuis le dossier qui contient `pom.xml`, lancer les commandes suivantes :

```bash
mvn clean verify
```

Pour démarrer l'interface console :

```bash
java -cp target/voyage-chambres-0.6.0.jar app.Main
```

Pour démarrer l'interface graphique :

```bash
java -cp target/voyage-chambres-0.6.0.jar app.MainGraphique
```

Avec Eclipse : **File > Import > Maven > Existing Maven Projects**, puis clic droit sur
`src/main/app/Main.java` (ou `MainGraphique.java`) > **Run As > Java Application**.
Dans la console, commencer par taper un nombre de personnes entre 1 et 26.
Les noms se tapent en majuscules (par exemple `A B` pour une relation).

## Tests

Les tests utilisent JUnit 5 et se lancent avec `mvn clean verify`.
Ils vérifient les classes du modèle, le solveur, le calcul de jalousie,
la session d'échanges et les parcours de la console. La fenêtre Swing n'a pas de test
automatique : elle se vérifie à la main en lançant `MainGraphique`.

## NB : ajouts aux menus de l'énoncé
Un quatrième choix complète les menus prévus dans l’énoncé : consulter le 
récapitulatif du groupe pendant la configuration, ou annuler le dernier 
échange pendant l’affectation. Ces ajouts facilitent la vérification 
des saisies et l’essai de différentes répartitions.

## Limites

- Les données ne sont pas sauvegardées : tout reste en mémoire pendant la session.
- Il n'y a pas d'import ni d'export de fichier.
- Le groupe est limité à 26 personnes.
- Le solveur ne cherche pas la meilleure solution possible.

## Pour aller plus loin

Le dossier `docs` contient :
- une présentation de l'architecture (`ARCHITECTURE.md`) ;
- un schéma UML (`Architecture_Partie1.drawio`) ;
- une analyse de la complexité de l'algorithme (`COMPLEXITE.md`) ;
- un déroulé de démonstration (`DEMONSTRATION.md`).

Le dossier `exemples` contient les saisies des deux scénarios de démonstration.
