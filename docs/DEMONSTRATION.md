# Démonstration de la partie 1

## Préparation

Dans Eclipse, lancer tous les tests, puis Main comme application Java.
Au terminal, construire avec `mvn clean verify`, puis lancer le JAR.
Le fichier `exemples/demo-sujet.txt` contient les réponses, une ligne par saisie.
Il peut être rejoué par redirection avec cmd ou un shell Unix ; il ne constitue
pas encore un fichier d'import métier de la partie 2.

## Parcours principal (environ cinq minutes)

1. Créer 4 personnes : A, B, C et D.
2. Ajouter les relations A B, B C et B D avec l'option 1 du premier menu.
3. Enregistrer les préférences avec l'option 2, une personne par action :

```text
A 1 2 3 4
B 1 3 2 4
C 3 2 1 4
D 1 4 2 3
```

4. Choisir 3 pour terminer la configuration. L'affectation gloutonne attendue est :

```text
A:1
B:3
C:2
D:4
```

5. Choisir 2 : le coût est 2, car B et C sont jalouses.
6. Choisir 1 et saisir `B C`. L'affectation devient A:1, B:2, C:3, D:4.
7. Choisir 2 : le coût devient 1, car seule B est jalouse.
8. Choisir 3 pour quitter.

Expliquer que le solveur est rapide, mais ne minimise pas directement la jalousie.
Le tableau de disponibilité et la borne de 351 vérifications pour 26 personnes
sont justifiés dans COMPLEXITE.md.

## Vérifications supplémentaires à montrer

- Saisir `abc`, `0` ou `27` au nombre de personnes, puis une valeur correcte.
- Terminer la configuration trop tôt : les noms sans préférences sont affichés.
- Essayer `A 1 1 3 4`, puis un classement valide : aucun état incohérent n'est enregistré.
- Corriger un classement déjà saisi et vérifier que le nouveau est utilisé.
- Ajouter deux fois A B : la relation reste unique et symétrique.
- Tenter un échange `A Z` : l'affectation est conservée.
- Essayer un choix de menu inexistant ou du texte : le menu reste utilisable.

## Points à expliquer au chargé de TP

- Répartition des responsabilités entre modèle, services, solveur et interface.
- Encapsulation des collections et validation avant mutation.
- Tests des règles métier et des sessions utilisateur en mémoire.
- Différence entre temps de calcul et qualité de la solution.
- Aucun import/export de groupe en partie 1 ; ces fonctions viendront ensuite.


## Extension lot 6

Voir AMELIORATIONS_LOT6.md et exemples/demo-lot6.txt pour les explications,
le récapitulatif, le bilan et l’annulation. Lancer MainGraphique pour la fenêtre Swing.
