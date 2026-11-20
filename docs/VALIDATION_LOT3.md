# Validation du lot 3

Vérification effectuée le 9 novembre 2026 sous Linux avec Temurin JDK 21.0.13
et Maven 3.9.9. Compilation ciblant Java 21 (`--release 21`).

| Classe de test | Cas réussis |
| --- | ---: |
| PersonneTest | 19 |
| ChambreTest | 14 |
| PreferencesTest | 10 |
| GroupeTest | 22 |
| AffectationTest | 15 |
| CalculateurJalousieTest | 10 |
| ConfigurationGroupeTest | 2 |
| AffectationJalousieTest | 3 |
| **Total** | **95** |

- Aucun échec, aucune erreur, aucun test ignoré.
- Compilation avec `-Xlint:all` et `-Werror` : réussie.
- Création du JAR 0.3.0 : réussie (sans point d'entrée à ce stade).
- JavaDoc avec contrôle complet et avertissements bloquants : réussie.
- Aucun avertissement dans le journal de construction.
- Sources et tests Java du lot 2 conservés à l'identique, comparaison effectuée.
- ZIP contrôlé : intégrité et comparaison de chaque fichier archivé avec sa source ;
  aucun contenu de `target` embarqué.

Commande exécutée :

```sh
mvn clean verify org.apache.maven.plugins:maven-javadoc-plugin:3.11.2:javadoc
```

Les tests d'intégration en mémoire sont découverts par Surefire car leurs noms
se terminent par `Test`. L'exemple du sujet valide les coûts 1 puis 2 après échange.

L'import dans Eclipse Windows n'a pas été exécuté ici. Les vérifications portent
sur Java, Maven, JUnit et JavaDoc. Le README décrit l'import et la mise à jour.
Les menus et le solveur ne sont pas encore implémentés ni testés.
Les anciens comptes rendus sont conservés dans VALIDATION_LOT1.md et VALIDATION_LOT2.md.
