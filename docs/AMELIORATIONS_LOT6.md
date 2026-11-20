# Lot 6 — Explications et interfaces

Fonctionnalités : fenêtre Swing, explications de jalousie, récapitulatif avant
résolution, bilan des échanges et annulation du dernier échange effectif.

## Règles importantes

Une relation est symétrique ; une jalousie est orientée et dépend du classement de
la personne qui jalouse. Une personne compte une seule fois dans le coût, même si
plusieurs voisins sont jalousés. Le détail les affiche tous.
Les échanges manuels sont autorisés même si le coût augmente.
L’annulation ne remonte qu’un échange et ne fait pas office de sauvegarde.
Un échange invalide ou avec soi-même ne détruit pas l’annulation disponible.

## Console

Le récapitulatif (option 4 du premier menu) peut être consulté à tout moment de la
configuration, même si les préférences sont incomplètes. La progression indique
combien de classements ont été enregistrés. Les exemples utilisent le nombre réel
de chambres. Chaque erreur est suivie d’une instruction pour réessayer.
Une correction de préférences est validée entièrement avant le remplacement.
Les noms sont sensibles à la casse, conformément aux personnes A à Z.
Une fin d’entrée termine la session, sans exécuter la saisie incomplète.

## Parcours de démonstration

Rejouer exemples/demo-lot6.txt : créer deux personnes, consulter le récapitulatif,
ajouter A–B, attribuer à chacun son premier choix (coût 0), échanger A et B
(coût 2), consulter les raisons, tenter un échange avec Z (refus), puis annuler
(coût 0). Une deuxième annulation est refusée. Quitter.
Dans Swing, reproduire ce parcours avec les boutons et sélectionner une ligne
pour voir les raisons. Tester aussi un classement 1 1 : le précédent est conservé.

## Périmètre

Pas d’import/export métier, pas de recherche d’optimum, pas de dessin du graphe
à ce stade. La console reste utilisable indépendamment de la fenêtre.
L’étude du glouton en O(n²) est conservée dans COMPLEXITE.md.
