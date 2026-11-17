# Algorithme naïf : fonctionnement et complexité

## Objectif et périmètre

La partie 1 demande une affectation initiale gloutonne : chaque personne reçoit
sa meilleure chambre encore libre. `SolveurNaif` respecte l'ordre de `Groupe`,
sans trier les personnes et sans consulter leurs relations. La console créera
ultérieurement les personnes dans l'ordre A, B, C, etc.

Une exécution rapide et une faible jalousie sont deux objectifs distincts.
Cet algorithme optimise le choix immédiat de chaque personne, sans garantir
le minimum global du nombre de personnes jalouses.

## Étapes

1. Refuser un groupe nul ou des préférences incomplètes.
2. Créer une map d'attribution et un tableau `boolean[n + 1]`, initialement faux.
3. Parcourir les personnes dans l'ordre du groupe.
4. Parcourir le classement de la personne, de la meilleure chambre à la moins bonne.
5. À la première chambre libre, enregistrer l'attribution, marquer la chambre occupée
   et quitter la boucle intérieure avec `break`.
6. Construire `Affectation`, qui copie la map et vérifie la bijection.

La case 0 du tableau est inutilisée. Le groupe garantit déjà les numéros 1 à n.
La liste de `Preferences` fournit l'ordre de parcours ; sa map des rangs reste
utile pour les comparaisons dans le calculateur de jalousie. Aucun tri n'est nécessaire.

## Pourquoi l'affectation est correcte

Avant le passage de la k-ième personne, exactement k − 1 chambres distinctes
sont occupées. Comme k ≤ n, il reste au moins une chambre libre. Le classement
contient toutes les chambres sans doublon : une chambre libre sera forcément trouvée.
Chaque passage attribue une seule chambre, jusqu'alors libre. Après n passages,
chaque personne et chaque chambre apparaissent donc exactement une fois.

## Temps d'exécution

On note n le nombre de personnes, égal au nombre de chambres.
Les accès au tableau et à la liste sont constants ; les opérations sur les maps
sont analysées sous l'hypothèse usuelle d'un hachage efficace (temps attendu/amorti
constant). Les comparaisons et hachages des identifiants sont ici considérés constants,
ce qui convient notamment aux noms d'une lettre de la partie 1.

Avant la k-ième personne, seules k − 1 chambres sont occupées. Ses k premiers
choix ne peuvent donc pas tous être occupés : elle examine au plus k chambres.
Le nombre total de vérifications de disponibilité est au maximum :

    1 + 2 + ... + n = n(n + 1) / 2.

Cette borne est atteinte lorsque toutes les personnes ont le même classement.
Si les premiers choix sont tous distincts, seules n chambres sont examinées.

| Opération | Temps |
| --- | --- |
| Vérification de l'état complet du groupe actuel | O(1) |
| Initialisation du tableau et copie de la liste des personnes | O(n) |
| Recherche des chambres libres | O(n²) au pire, O(n) au mieux |
| Construction et validation de l'affectation | O(n) attendu |
| **Total** | **O(n²) au pire ; O(n) au mieux, avec hachage efficace** |

Plus précisément, le parcours est en Θ(n²) dans le pire cas et Θ(n) dans le meilleur.
La borne n(n+1)/2 est une borne sur les chambres examinées, pas un comptage de toutes
les instructions Java. Pour n = 26, elle vaut 351. Ce résultat est une démonstration,
pas une mesure de durée. Les tests ne prétendent pas mesurer des nanosecondes.

## Mémoire supplémentaire

Le tableau de disponibilité possède n + 1 cases et la map d'attribution n entrées.
La liste des personnes retournée par le groupe, la copie défensive de la map et
les ensembles temporaires de validation occupent également O(n) cases chacun.
Un nombre constant de structures de taille linéaire donne **O(n) mémoire supplémentaire**,
y compris le résultat. Les préférences déjà stockées occupent O(n²) dans le modèle,
mais ne sont ni recréées ni copiées par le solveur.

## Optimisations retenues

- Tableau booléen : disponibilité en O(1), sans rechercher dans les valeurs de la map.
- Arrêt au premier choix libre, sans parcourir la fin du classement.
- Utilisation directe des classements déjà ordonnés, sans tri.
- Aucun recalcul de jalousie pendant l'affectation initiale.
- Aucun retrait d'élément dans les listes de préférences.
- Aucun état conservé dans le solveur : chaque appel produit un résultat indépendant.

Une implémentation fondée sur `attribution.containsValue(chambre)` ferait une recherche
linéaire parmi les chambres attribuées pour chaque candidat. Avec des classements
identiques, elle peut atteindre O(n³). Le tableau évite ce surcoût. Il est adapté
aux identifiants continus 1 à n ; ce choix serait à revoir si cette garantie changeait.

## Limite sur la qualité et exemple

Avec les données du sujet et l'ordre A, B, C, D :

| Personne | Préférences | Chambre attribuée |
| --- | --- | --- |
| A | 1, 2, 3, 4 | 1 |
| B | 1, 3, 2, 4 | 3 |
| C | 3, 2, 1, 4 | 2 |
| D | 1, 4, 2, 3 | 4 |

B et C sont jalouses : coût 2. L'échange des chambres de B et C produit
A:1, B:2, C:3, D:4, de coût 1. Le résultat glouton n'est donc pas toujours optimal.
Ce parcours est vérifié par `ResolutionNaiveTest`.

Essayer plusieurs ordres de passage ou rechercher des échanges améliorants sont
des évolutions possibles, non implémentées dans ce lot. Elles doivent être comparées
sur le coût final et le temps de calcul. Elles ne garantissent pas à elles seules
l'optimum global. `SolveurAffectation` permettra d'ajouter d'autres stratégies.
