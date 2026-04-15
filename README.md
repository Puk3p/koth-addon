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

## Observație
În acest moment sunt create doar fișierele și structura proiectului.
Clasele Kotlin sunt intenționat goale, conform cerinței.
