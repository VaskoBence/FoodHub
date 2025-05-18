# FoodHub – Receptmegosztó Android Alkalmazás

## 📱 Alkalmazás leírása

**FoodHub** egy Android alapú receptmegosztó alkalmazás, amely receptek készítésére, megtekintésére, közzétételére szolgál.

***Mevalósított funkciók***
  Bejelentkezés és Regisztráció Firebase Authentikációval.
  Receptek listaszerű megtekintése bejelentkezett felhasználóként
  A + gomb megnyomásával átvisz egy oldalra, ahol recepteket adhatunk hozzá.
  A profil ikonnal megnyitja a profil oldalt.
  A kijelentkezés gombra nyomva kijelentkezik, és visszakerül a bejelentkezési oldalra.
  Egy receptre kattintva megnyit egy részletes recept oldalt az adott recept adataival.

***Megvalósítaindó funkciók***
  Recept hozzáadás oldal funkcionális megvalósítása
  Receptek felhasználókhoz kötése
  Receptek kedvencekhez adása
  Receptek keresése
  Receptek szűrése
  Receptek tárolása adatbázisban (jelenleg csak lokálisan működik dummy adatokkal)

Az alkalmazás reszponzív, több kijelzőmérethez és elforgatáshoz is alkalmazkodik, valamint egy animáció is megtalálható benne az aktivitásváltás során.


---

## 🗂 Felépítés és segédlet a javítókulcshoz

| Kritérium | Megvalósítás | Hol található |
|----------|---------------|----------------|
| **Fordítási és futtatási hibák** | Nincsenek | Projekt fut Android Studio alatt |
| **Firebase Autentikáció** | Regisztráció és bejelentkezés | `LoginActivity.java`, `RegisterActivity.java` |
| **Beviteli mezők típusa** | E-mail mezőhöz e-mail keyboard, jelszó csillagozva | `activity_login.xml`, `activity_register.xml` |
| **Kétféle layout** | ConstraintLayout és LinearLayout | `activity_profile.xml`, `activity_recipe_detail.xml`, stb. |
| **Reszponzív GUI** | ScrollView, wrap_content, megfelelő margók és paddingek | Minden fő layout XML |
| **Elforgatás kezelése** | Layout igényes marad elforgatáskor | Pl. `activity_profile.xml` |
| **Animáció használata** | Slide animáció két activity váltáskor | `/res/anim/slide_in_right.xml`, `slide_out_left.xml` és `overridePendingTransition()` hívások |
| **Intentek használata** | Activityk között navigáció | `Intent` hívások minden fő képernyőn |

---

## 🔍 Ellenőrzéshez tippek

- **Teszteléshez**: próbálj regisztrálni, majd jelentkezz be, nézd meg az oldalak közti navigálást.
- **Responsiveness**: próbáld ki elforgatással vagy tablet emulátorral.
- **Animációk**: `overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)` pl. `LoginActivity` → `RegisterActivity` váltáskor.

---

## 🚀 Futtatás

1. Nyisd meg Android Studio-ban.
2. Csatlakoztass egy emulátort vagy fizikai eszközt (én Samsung fizikai eszközön futtattam).
3. Kattints a zöld **Run** gombra.
4. Enjoy! 😊

---

Készült beadandóként Android fejlesztés témában – 2025.
