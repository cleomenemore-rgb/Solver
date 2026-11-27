# Algorithme naïf : fonctionnement et complexité

## Objectif et périmètre
Pour cette première partie, il fallait mettre en place une affectation initiale gloutonne. Chaque personne reçoit sa meilleure chambre encore disponible.

SolveurNaif suit l'ordre des personnes dans Groupe. Il ne trie pas les personnes et ne prend pas en compte leurs relations. La console ajoutera ensuite les personnes dans l'ordre A, B, C, etc.

Il faut distinguer le temps d'exécution et le niveau de jalousie. L'algorithme cherche à attribuer la meilleure chambre possible à chaque personne, au moment où elle passe. En revanche, cela ne garantit pas d'obtenir le nombre minimum de jaloux à la fin.

## Étapes

1. Vérifier que le groupe n'est pas nul et que les préférences sont complètes.
2. Créer une map pour stocker les attributions et un tableau boolean[n + 1], initialisé à false.
3. Parcourir les personnes dans l'ordre du groupe.
4. Parcourir les préférences de chaque personne, de sa chambre préférée à la moins bien classée.
5. Dès qu'une chambre est libre, l'attribuer à la personne, indiquer qu'elle est occupée et sortir de la boucle avec break.
6. Construire l'objet Affectation, qui copie la map et vérifie que chaque personne possède une chambre différente.

La case 0 du tableau n'est pas utilisée, car les chambres sont numérotées de 1 à n. Le groupe garantit déjà cette numérotation.

Les préférences sont déjà classées dans le bon ordre. Il suffit donc de parcourir la liste de Preferences, sans effectuer de tri. La map des rangs est toujours utilisée pour comparer les préférences lors du calcul de la jalousie.

## Pourquoi l'affectation est correcte

Avant de traiter la k-ième personne, les k − 1 premières chambres ont déjà été attribuées. 
Elles sont toutes différentes. Comme le nombre de personnes est égal au nombre de chambres 
et que k est inférieur ou égal à n, il reste forcément au moins une chambre libre.

De plus, chaque classement contient toutes les chambres, sans doublon. L'algorithme trouvera 
donc forcément une chambre disponible.

À chaque passage, une seule chambre libre est attribuée. Après les n passages, toutes les 
personnes ont une chambre et toutes les chambres sont occupées une seule fois. L'affectation 
est donc correcte.

## Temps d'exécution

On note n le nombre de personnes, qui correspond aussi au nombre de chambres.

L'algorithme utilise principalement un tableau, une liste et une map. Les accès au tableau et à la liste prennent un temps constant. Pour la map, on considère que les opérations de recherche et d'ajout prennent un temps constant en moyenne, avec un hachage efficace.

Avant de traiter une personne, k − 1 chambres sont déjà occupées. Elle peut donc trouver une chambre libre au plus tard à son k-ième choix.

Par exemple :
- La première personne examine au maximum 1 chambre.
- La deuxième en examine au maximum 2.
- La troisième en examine au maximum 3.
- Et ainsi de suite jusqu'à la dernière personne, qui peut en examiner n.

Au total, le nombre maximum de vérifications est donc :

\[
1 + 2 + 3 + \ldots + n = \frac{n(n+1)}{2}
\]

Ce maximum est atteint lorsque toutes les personnes ont exactement le même classement. À l'inverse, si chacune choisit une chambre différente en premier, l'algorithme n'a besoin d'examiner que n chambres au total.

| Opération | Temps |
| --- | --- |
| Vérification de l'état complet du groupe | O(1) |
| Initialisation du tableau et copie de la liste des personnes | O(n) |
| Recherche des chambres disponibles | O(n²) au pire, O(n) au mieux |
| Construction et vérification de l'affectation | O(n) en moyenne |
| **Total** | **O(n²) au pire, O(n) au mieux** |

On peut donc retenir que le temps d'exécution est en Θ(n²) dans le pire des cas et en Θ(n) dans le meilleur des cas.

La formule précédente donne le nombre de chambres examinées, et non le nombre total d'instructions exécutées par Java. Pour n = 26, on obtient au maximum 351 vérifications.

Il s'agit d'un calcul théorique et non d'une mesure du temps réel d'exécution. Les tests ne cherchent donc pas à mesurer une durée en nanosecondes.

## Mémoire supplémentaire

L'algorithme utilise un tableau de disponibilité de n + 1 cases et une map contenant n attributions.

Il faut également prendre en compte la liste des personnes retournée par le groupe, la copie de la map lors de la création de l'affectation et les ensembles temporaires utilisés pour les vérifications.

Toutes ces structures occupent chacune O(n) cases. Leur nombre étant constant, la mémoire supplémentaire utilisée est donc de **O(n)**, résultat compris.

Les préférences sont déjà enregistrées dans le programme. Elles occupent O(n²) en mémoire, mais le solveur ne les recrée pas et ne les copie pas.

## Optimisations retenues

Plusieurs choix permettent d'éviter des opérations inutiles :

- Utiliser un tableau booléen pour vérifier directement si une chambre est libre en O(1), sans parcourir les valeurs de la map.
- S'arrêter dès qu'une chambre disponible est trouvée, grâce à `break`.
- Utiliser directement les préférences déjà classées, sans effectuer de tri.
- Ne pas recalculer la jalousie pendant l'affectation.
- Ne pas supprimer d'éléments dans les listes de préférences.
- Ne conserver aucun état dans le solveur. Chaque appel fonctionne indépendamment des précédents.

Une autre solution aurait été d'utiliser `attribution.containsValue(chambre)` pour vérifier si une chambre est déjà attribuée.

Cependant, cette méthode parcourt les valeurs de la map à chaque vérification. Avec des classements identiques, le temps d'exécution pourrait alors atteindre O(n³).

Le tableau booléen permet d'éviter ce problème. Il convient ici car les chambres sont numérotées de 1 à n. Si cette règle changeait, il faudrait revoir ce choix.

## Limite sur la qualité et exemple

Avec les données du sujet et l'ordre de passage A, B, C, D, on obtient le résultat suivant :

| Personne | Préférences | Chambre attribuée |
| --- | --- | --- |
| A | 1, 2, 3, 4 | 1 |
| B | 1, 3, 2, 4 | 3 |
| C | 3, 2, 1, 4 | 2 |
| D | 1, 4, 2, 3 | 4 |

Dans cette affectation, B et C sont jalouses. Le coût total est donc de 2.

Si on échange les chambres de B et C, on obtient :

- A : 1
- B : 2
- C : 3
- D : 4

Le coût passe alors à 1. Cela montre que le résultat de l'algorithme glouton n'est pas toujours optimal.

Ce comportement est vérifié dans `ResolutionNaiveTest`.

Il serait possible d'améliorer l'algorithme en testant plusieurs ordres de passage ou en recherchant des échanges de chambres qui réduisent la jalousie. Ces améliorations ne sont pas encore implémentées dans cette partie.

Il faudrait comparer ces différentes méthodes en prenant en compte le coût final et le temps de calcul. Elles ne garantissent pas forcément de trouver la meilleure affectation possible.

La classe `SolveurAffectation` permettra d'ajouter d'autres stratégies par la suite.
