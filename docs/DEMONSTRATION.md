# Démonstration

## Préparation

Dans le dossier qui contient `pom.xml` :

```bash
mvn clean verify
java -cp target/voyage-chambres-0.6.0.jar app.Main
```

Pour la fenêtre Swing, on remplace `app.Main` par `app.MainGraphique`.
On peut aussi lancer `Main` ou `MainGraphique` directement depuis Eclipse.

Les fichiers du dossier `exemples` contiennent les réponses à taper, une par ligne. On peut les
rejouer sans rien saisir :

```bash
java -cp target/voyage-chambres-0.6.0.jar app.Main < exemples/demo-sujet.txt
```

## Scénario principal (environ cinq minutes)

C'est le scénario de `exemples/demo-sujet.txt`.

1. Créer 4 personnes : A, B, C et D.
2. Ajouter les relations A B, B C et B D avec l'option 1 du premier menu.
3. Enregistrer les préférences avec l'option 2, une personne à la fois :

```text
A 1 2 3 4
B 1 3 2 4
C 3 2 1 4
D 1 4 2 3
```

4. Choisir 3 pour terminer la configuration. Le solveur glouton donne :

```text
A:1
B:3
C:2
D:4
```

5. Choisir 2 : le coût est 2, car B et C sont jalouses.
6. Choisir 1 et saisir `B C`. La répartition devient A:1, B:2, C:3, D:4.
7. Choisir 2 : le coût passe à 1, car il ne reste que B de jalouse.
8. Choisir 3 pour quitter.

Cet exemple montre que le solveur est rapide mais ne cherche pas le minimum de jaloux : un
échange à la main a amélioré le résultat. Le tableau de disponibilité et la borne de 351
vérifications pour 26 personnes sont expliqués dans `COMPLEXITE.md`.

## Explications et annulation

Le fichier `exemples/demo-lot6.txt` montre un deuxième parcours avec 2 personnes : récapitulatif
du groupe, échange de A et B, explications de jalousie, échange refusé (`A Z`) puis annulation du
dernier échange. La même démonstration se fait à la main dans la fenêtre Swing avec les boutons
« Échanger » et « Annuler le dernier échange ».

## Cas d'erreur à essayer

- Saisir `abc`, `0` ou `27` comme nombre de personnes, puis une valeur correcte.
- Terminer la configuration trop tôt : les noms qui n'ont pas encore de préférences sont affichés.
- Essayer `A 1 1 3 4`, puis un classement valide : rien n'est enregistré avec le classement faux.
- Corriger un classement déjà saisi : c'est le nouveau qui est utilisé.
- Ajouter deux fois la relation A B : elle n'existe qu'une fois et marche dans les deux sens.
- Tenter un échange `A Z` : la répartition ne change pas.
- Choisir un numéro de menu qui n'existe pas, ou taper du texte : le menu reste utilisable.
