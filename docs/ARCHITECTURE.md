# Architecture du projet

Le projet est découpé en cinq packages. Chaque package a un rôle précis et les
dépendances vont toujours dans le même sens : les interfaces (console et Swing) utilisent
les services, qui utilisent le modèle.

Le schéma `Architecture_Partie1.drawio` reprend cette organisation sur trois pages :
le modèle, la résolution (solveur, calcul et session), puis les deux interfaces.

## Le modèle (`modele`)

- `Personne` et `Chambre` sont des objets immuables : une personne est identifiée par son nom,
  une chambre par son numéro.
- `Preferences` est le classement des chambres d'une personne, de la préférée à la moins aimée.
  On ne peut plus le modifier une fois créé.
- `Groupe` contient les personnes, les chambres, les relations entre personnes et les préférences
  de chacun. Toutes les modifications passent par des méthodes qui vérifient les valeurs avant
  de changer quoi que ce soit.
- `Affectation` associe une chambre à chaque personne (une chambre par personne). On peut
  échanger les chambres de deux personnes, ce qui garde toujours une répartition valide.

Le modèle ne connaît ni la console ni Swing.

## Le solveur (`algorithme`)

`SolveurAffectation` est une interface avec une seule méthode, `resoudre(groupe)`.
`SolveurNaif` l'implémente avec l'algorithme glouton : dans l'ordre du groupe, chaque personne
prend sa meilleure chambre encore libre. Les interfaces ne connaissent que l'interface
`SolveurAffectation`, donc on pourrait ajouter un autre algorithme sans les modifier.

## Les services (`service`)

- `CalculateurJalousie` calcule qui est jaloux et pourquoi, sans rien modifier.
  Une personne est jalouse si elle préfère la chambre d'un voisin (une personne avec qui elle a
  une relation) à la sienne.
- `SessionAffectation` garde la répartition en cours. Elle permet de calculer le coût,
  d'échanger deux personnes et d'annuler le dernier échange (un seul niveau d'annulation).
- `BilanEchange` est un petit `record` qui donne le nombre de jaloux avant et après un échange
  ou une annulation.

La console et la fenêtre Swing utilisent exactement les mêmes services : il n'y a donc pas de
règle de jalousie écrite deux fois.

## Les interfaces (`ui` et `ui.graphique`)

- `InterfaceConsole` gère les menus et les saisies au clavier. Elle lit les lignes, appelle le
  modèle et les services, puis affiche le résultat ou un message d'erreur. L'entrée et la sortie
  sont passées au constructeur (un `Readable` et un `PrintWriter`), ce qui permet de la tester
  sans clavier.
- `PresentationGroupe` fabrique les textes communs : récapitulatif du groupe, explications de
  jalousie et bilan d'un échange. La console et Swing affichent donc les mêmes messages.
- `ui.graphique.PanneauApplication` est la fenêtre Swing. Elle contient les champs, les boutons et
  les zones de texte. Chaque bouton appelle le modèle ou `SessionAffectation`, puis la fenêtre
  se met à jour. Elle ne calcule jamais la jalousie elle-même.
- `ui.graphique.TableauGroupe` est le modèle du `JTable` : une ligne par personne, avec les
  colonnes Personne, Préférences, Relations, Chambre et État.

Pendant qu'une répartition est en cours, la configuration du groupe est bloquée dans la
fenêtre. Le bouton « Revenir à la configuration » abandonne la répartition.

## Le lancement (`app`)

- `Main` crée un `SolveurNaif` et un `CalculateurJalousie`, puis lance la console.
  Avec l'argument `--gui`, il lance la fenêtre à la place.
- `MainGraphique` ouvre la fenêtre Swing. Si aucun écran n'est disponible, il affiche
  un message et s'arrête.

## Gestion des erreurs

Les méthodes du modèle et des services refusent les valeurs invalides en lançant
`IllegalArgumentException` (argument incorrect) ou `IllegalStateException` (par exemple, des
préférences manquantes), avant de modifier quoi que ce soit. La console et Swing
attrapent ces erreurs attendues pour afficher un message et revenir au menu. Une panne de
lecture (`IOException`) remonte jusqu'à `Main`, qui arrête le programme.
