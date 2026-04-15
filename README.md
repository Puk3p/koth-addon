# FKothWins

Acesta este un proiect Minecraft (Kotlin + Maven), pregătit ca structură, fără logică implementată în clase.

## Ce face pluginul (ca direcție)
Pluginul urmărește câte KOTH-uri câștigă fiecare facțiune.
Câștigul este acordat facțiunii jucătorului care câștigă efectiv KOTH-ul.

## Reguli stabilite
- Dacă jucătorul nu are facțiune, câștigul este ignorat.
- Câștigurile rămân la facțiunea care le-a primit (nu se mută cu jucătorul).
- Dacă facțiunea se desființează (disband), câștigurile ei se șterg.
- Comanda de tip `add` trebuie să poată funcționa și pentru jucători offline.
- Există suport pentru placeholder la win-urile facțiunii (util la `/f who`).
- Top 5 este pregătit pentru hologramă (ex: la warp PvP).

## Comenzi planificate
- Admin / Consolă: `/fkoth add`, `/fkoth remove`, `/fkoth set`
- Jucători: `/fkoth stats`, `/fkoth top`

## Git Hooks
Acest proiect folosește hooks versionate în `.githooks`:
- `pre-commit`: rulează `mvn -q -DskipTests validate`
- `commit-msg`: validează formatul mesajului de commit (`feat:`, `fix:`, `chore:` etc.)
- `pre-push`: rulează `mvn -q -DskipTests clean package`

Activare hooks:
- `./scripts/setup-githooks.sh`

## CI/CD
Workflow-uri GitHub Actions:
- `.github/workflows/build.yml`
  - rulează la `push` pe `master/develop` și la `pull_request` spre `master`
  - face `validate`, `compile`, `package`
  - urcă artifact-ul JAR
- `.github/workflows/release.yml`
  - rulează la tag-uri `v*` (ex: `v1.0.1`)
  - setează versiunea Maven din tag
  - face build și creează GitHub Release cu JAR-ul
  - opțional postează pe Discord dacă există secretele:
    - `URL_WEBHOOK_OPERATOR`
    - `OPERATOR_ROLE_ID`
