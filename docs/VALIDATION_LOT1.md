# Validation du lot 1

Vérification effectuée le 3 novembre 2026 sous Linux, avec JDK Temurin 21.0.13
et Maven 3.9.9. Le code cible Java 21 et ne dépend pas du correctif 21.0.13.

- Compilation du modèle et des tests avec `--release 21`, `-Xlint:all`, `-Werror` : réussie.
- PersonneTest : 19 cas réussis.
- ChambreTest : 14 cas réussis.
- Total : 33 cas, 0 échec, 0 erreur, 0 ignoré.
- Création du JAR : réussie (JAR non exécutable à ce stade).
- JavaDoc avec contrôle complet et avertissements bloquants : réussie.
- Aucun avertissement dans le journal de cette construction.

Les dépendances sont déclarées dans le POM et ne sont pas embarquées dans le ZIP.
La configuration réseau temporaire utilisée pour les téléchargements de validation
ne fait pas partie du projet livré.

L'import dans une interface Eclipse Windows n'a pas été exécuté ici ; le README
fournit la procédure standard d'import d'un projet Maven. La validation porte sur
la construction Maven, les tests et la génération de documentation.

Une seconde construction depuis un dossier neuf extrait du ZIP a également
réussi : `mvn -o clean verify javadoc:javadoc`, avec les 33 cas réussis.
Le mode hors ligne utilisait les dépendances déjà téléchargées pour la première validation.
