# Architecture de la partie 1

Le schéma `Architecture_Partie1.drawio` contient trois pages : modèle métier, résolution et interfaces enrichies.
Il présente les attributs et méthodes utiles à la compréhension ; les constructeurs,
les validations privées et les méthodes equals/hashCode sont omis pour la lisibilité.
Les flèches pleines représentent des associations ; les flèches en pointillés sont
des dépendances d'utilisation. Le triangle creux indique l'implémentation d'une interface.

## Modèle

- Personne et Chambre sont des valeurs immuables identifiées par nom et numéro.
- Preferences est un classement immuable : liste ordonnée + map des rangs.
- Groupe possède des membres et chambres fixes. Les relations et préférences évoluent
  uniquement par des méthodes validées. Chaque membre peut n'avoir aucune préférence
  pendant la configuration, puis exactement un classement complet.
- Affectation possède sa propre map et conserve la bijection lors des échanges.
  Son constructeur consulte Groupe pour valider le périmètre, sans le stocker.

## Application

Main crée les dépendances concrètes. InterfaceConsole dépend du contrat SolveurAffectation,
du calculateur et du modèle. Elle ne calcule ni les rangs, ni la jalousie, ni l'affectation.
Son rôle est d'interpréter une ligne, appeler l'opération correspondante et afficher
le résultat ou une erreur compréhensible. Les flux injectés rendent les menus testables.

SolveurNaif implémente SolveurAffectation ; l'interface permettra de substituer une autre
stratégie. CalculateurJalousie évalue une affectation sans la modifier. Ces services
ne connaissent ni Scanner, ni les menus, ni System.out.

## Erreurs et cycle de vie

Les méthodes métier refusent les paramètres invalides avant toute mutation.
La console traduit les erreurs attendues en messages et revient au menu. Elle ne
capture pas toutes les exceptions indistinctement : un bug de programmation doit
rester visible aux tests. Une fin d'entrée normale n'est pas une exception.
Une IOException de la source remonte à Main, qui arrête la session avec un message.
Les flux sont créés/fournis par l'appelant et ne sont pas fermés par InterfaceConsole.
La console est conçue pour piloter une session dans un seul thread.

## Interfaces enrichies (lot 6)

Les deux interfaces délèguent à `SessionAffectation`, qui détient une copie indépendante
de la répartition. `echanger` et `annuler` renvoient un `BilanEchange` immuable.
L’historique garde uniquement les deux personnes du dernier échange effectif.
`PresentationGroupe` produit les textes communs du récapitulatif, des jalousies et des bilans.
`PanneauApplication` et `TableauGroupe` ne calculent pas les règles de jalousie.
Les composants Swing sont manipulés sur l’EDT, y compris dans les tests.
L’interface fige la configuration pendant une session d’affectation ; revenir à
la configuration abandonne la session. Le service lit le groupe fourni : un appelant
extérieur ne doit donc pas modifier ce groupe pendant qu’il explore les échanges.
