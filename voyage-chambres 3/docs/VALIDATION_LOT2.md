# Validation du lot 2

Vérification effectuée le 5 novembre 2026 sous Linux, avec Temurin JDK 21.0.13
et Maven 3.9.9. Compilation ciblant Java 21 (`--release 21`).

## Résultats

| Classe de test | Cas réussis |
| --- | ---: |
| ConfigurationGroupeTest | 2 |
| ChambreTest | 14 |
| GroupeTest | 22 |
| PersonneTest | 19 |
| PreferencesTest | 10 |
| **Total** | **67** |

- Aucun échec, aucune erreur, aucun test ignoré.
- Compilation avec `-Xlint:all` et `-Werror` : réussie.
- Création du JAR 0.2.0 : réussie (sans point d'entrée à ce stade).
- JavaDoc avec contrôle complet et avertissements bloquants : réussie.
- Aucun avertissement dans le journal de construction.
- Sources et tests Java du lot 1 conservés à l'identique, comparaison des fichiers effectuée.
- ZIP contrôlé : intégrité de l'archive et comparaison de chaque fichier archivé
  avec le fichier source correspondant ; aucun contenu de `target` embarqué.

La commande de validation est `mvn clean verify javadoc:javadoc` (le plugin JavaDoc
est fixé à la version 3.11.2 dans le POM). Les deux tests du package `integration`
sont découverts automatiquement, car leur nom de classe se termine par `Test`.

L'import dans Eclipse Windows n'a pas été exécuté ici. Le README fournit les
instructions d'import et de mise à jour ; les vérifications portent sur Java,
Maven, JUnit et JavaDoc.

L'ancien compte rendu est conservé dans VALIDATION_LOT1.md. Les cas testés ne
couvrent pas encore l'affectation, la jalousie ou les menus, non implémentés dans ce lot.
