# Validation du lot 6 — 17 novembre 2026

## Résultat

199 tests exécutés avec succès : aucun échec, aucun test ignoré.
JUnit Platform Console Standalone 1.11.4 (JUnit Jupiter 5.11.4), JDK 21.0.13.
Les sources principales et les tests compilent avec `-Xlint:all -Werror`.
La Javadoc privée est générée avec `-Xdoclint:all -Werror`, sans avertissement.

## Méthode et limite de l'environnement

Les vérifications ont été réalisées directement avec javac, le lanceur officiel
JUnit et javadoc. La commande Maven `clean verify javadoc:javadoc` a été tentée,
mais la résolution des plugins a échoué à cause de l'accès réseau de Maven dans
l'environnement. Il ne s'agit pas d'un échec de test ; le cycle Maven complet
n'est pas déclaré validé ici. Le POM conserve les versions prévues pour Eclipse.
Sur les machines du binôme, lancer `mvn clean verify` puis `mvn javadoc:javadoc`.

## Couverture

- 167 cas du lot 5, conservés.
- 11 cas sur les explications, les bilans, les invariants et l'annulation partagée.
- 9 cas Swing exécutant les événements des boutons sur l'EDT : blocage des actions,
  exemple du sujet, annulation, préférences invalides sans écrasement, relations,
  reconfiguration, groupe minimal, 26 personnes et effectifs incorrects.
- 1 parcours console enrichi : échange, annulation et refus d'une seconde annulation.
- 11 nouveaux cas de console guidée : récapitulatif partiel/complet, exemple adapté,
  sept corrections invalides avec conservation du classement, échange refusé suivi
  d'une annulation et relation corrigée.

## Exécution du programme

JAR construit avec l'outil jar du JDK, manifeste désignant Main.
Quatre processus réels terminent avec code 0 et sans sortie d'erreur :
exemple du sujet, démonstration du lot 6 (0 → 2 → 0), fin d'entrée immédiate,
saisies incorrectes suivies d'une correction valide.
Le panneau Swing a également été rendu en image hors écran et inspecté : commandes,
explications et bilan visibles à 1200 × 820. Les tests Swing ne nécessitent pas d'écran.
La vérification native sous Eclipse Windows et les dialogues de confirmation
sur les machines du binôme restent à effectuer.
