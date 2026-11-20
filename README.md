# Solver : organisation des chambres

Projet de L3 Informatique MIAGE (Programmation Avancée et Application), réalisé par
**William Oufahli** et **Cléomène More**.

## L'objectif du projet

Un groupe de personnes part en voyage et chacune doit avoir sa propre chambre.
Il y a autant de chambres que de personnes. Chaque personne classe les chambres
de celle qu'elle préfère à celle qu'elle aime le moins.

Certaines personnes ne s'entendent pas : on appelle ça une **relation**.
Une personne est **jalouse** si elle préfère la chambre de quelqu'un avec qui elle
a une relation à la sienne.

Le but du programme est de répartir les chambres en limitant le nombre de personnes jalouses.

## Ce que fait le programme

- Crée un groupe de 1 à 26 personnes (nommées A, B, C… Z) avec les chambres 1 à n.
- Permet d'ajouter des relations entre deux personnes (elles marchent dans les deux sens).
- Permet de saisir, et de corriger, le classement des chambres de chaque personne.
- Affiche un récapitulatif du groupe avant de lancer le calcul.
- Propose une première répartition automatique.
- Compte les personnes jalouses et explique pourquoi (qui jalouse qui).
- Permet d'échanger les chambres de deux personnes et montre le nombre de jaloux avant et après.
- Permet d'annuler le dernier échange (un seul niveau d'annulation).
- Fonctionne de deux façons : en console ou dans une fenêtre Swing.

## Comment ça marche ?

La répartition automatique est un algorithme glouton. On passe les personnes dans
l'ordre (A, B, C…) et chacune reçoit sa chambre préférée parmi celles qui sont encore libres.
Il est rapide, mais il ne tient pas compte des relations. Il ne garantit donc pas le
plus petit nombre de jaloux. C'est pour cela que l'on peut ensuite faire des échanges
à la main et voir si le résultat s'améliore.

Le code est découpé en cinq packages :

- `modele` qui contient : `Personne`, `Chambre`, `Preferences`, `Groupe`, `Affectation`
- `algorithme` qui contient l'interface `SolveurAffectation` et le solveur glouton `SolveurNaif` 
- `service` qui s'occupe de : calcul de la jalousie, session d'échanges et d'annulation, bilan d'un échange
- `ui` : la console, les textes communs, et la fenêtre Swing dans `ui.graphique`
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

Il faut un **JDK 21**. Maven (3.9) est utile pour construire le projet.

```sh
mvn clean verify
java -jar target/voyage-chambres-0.6.0.jar # console
java -jar target/voyage-chambres-0.6.0.jar --gui # fenêtre Swing
```

On peut aussi rejouer l'exemple (Linux, macOS ou `cmd` sous Windows) :

```sh
java -jar target/voyage-chambres-0.6.0.jar < exemples/demo-sujet.txt
```

Avec Eclipse : **File > Import > Maven > Existing Maven Projects**, puis clic droit sur
`src/main/app/Main.java` > **Run As > Java Application**.
Dans la console, commencer par taper un nombre de personnes entre 1 et 26.
Les noms se tapent en majuscules (par exemple `A B` pour une relation).

## Tests

Les tests utilisent JUnit 5 et se lancent avec `mvn clean verify` (199 tests).
Ils vérifient les classes du modèle, le solveur, le calcul de jalousie,
les parcours de la console et les boutons de la fenêtre.

## Limites

- Les données ne sont pas sauvegardées : tout reste en mémoire pendant la session.
- Il n'y a pas d'import ni d'export de fichier.
- Le groupe est limité à 26 personnes.
- Le solveur ne cherche pas la meilleure solution possible.

## Pour aller plus loin

Le dossier `docs/` contient l'étude de complexité du solveur, un schéma UML
(`Architecture_Partie1.drawio`) et des notes de conception.
