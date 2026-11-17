# Validation du lot 4

Vérification du 9 novembre 2026 sous Linux, Temurin JDK 21.0.13 et Maven 3.9.9.
Compilation ciblant Java 21. Commande : `mvn clean verify javadoc:javadoc`.

| Classe de test | Cas réussis |
| --- | ---: |
| SolveurNaifTest | 9 |
| AffectationJalousieTest | 3 |
| ConfigurationGroupeTest | 2 |
| ResolutionNaiveTest | 2 |
| AffectationTest | 15 |
| ChambreTest | 14 |
| GroupeTest | 22 |
| PersonneTest | 19 |
| PreferencesTest | 10 |
| CalculateurJalousieTest | 10 |
| **Total** | **106** |

- Aucun échec, aucune erreur et aucun test ignoré.
- Compilation avec `-Xlint:all` et `-Werror` réussie, sans avertissement.
- JAR 0.4.0 construit ; aucun point d'entrée console à ce stade.
- Javadoc générée avec contrôle complet, sans avertissement.
- Comportement Java et tests du lot 3 conservés ; seule la formulation du commentaire
  de `Affectation.chambreDe` a changé dans les sources existantes.
- Archive contrôlée par vérification CRC et comparaison de chaque fichier avec sa source.
  Aucun dossier `target` ni configuration Eclipse propre à une machine n'est embarqué.

Les nouveaux tests couvrent les choix gloutons, l'ordre du groupe, les données invalides,
la préservation des données et les résolutions indépendantes. L'intégration vérifie
le coût 2 de la solution gloutonne du sujet, puis 1 après échange de B et C.
La complexité est démontrée dans COMPLEXITE.md ; aucun benchmark chronométré n'est revendiqué.

L'import sous Eclipse Windows n'a pas été exécuté ici. Le README donne les étapes
pour mettre à jour le projet et reproduire les vérifications. Les menus restent à réaliser.
Les comptes rendus des lots précédents sont conservés dans les fichiers VALIDATION_LOT*.md.
