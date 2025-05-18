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

# Javítói segédlet – Szabaduló szoba Angular projekt

**Fordítási hiba nincs:**  
- `ng serve` futtatásakor nincs hiba.

**Futtatási hiba nincs:**  
- A böngésző konzolban sem jelenik meg hiba.

---

## Követelmények és megvalósításuk helye

### 1. Adatmodell definiálása (legalább 4 TypeScript interfész vagy class)
- `src/app/models/score.model.ts` – `Score` interface
- `src/app/models/level.model.ts` – `Level` interface
- `src/app/models/tile.model.ts` – `Tile` interface
- `src/app/models/progress.model.ts` – `Progress` interface

### 2. Reszponzív, mobile-first felület
- Globális: `src/styles.scss`
- Példák: `app.component.scss`, `game.component.scss`, `profile.component.scss`, `registration.component.scss`
- Minden oldal mobilon is jól jelenik meg.

### 3. Legalább 4, de 2 különböző attribútum direktíva használata
- `[ngClass]`, `[ngStyle]`, `[disabled]`, `[routerLink]` – pl. `level-selector.component.html`, `profile.component.html`, `statistics.component.html`

### 4. Legalább 4, de 2 különböző beépített vezérlési folyamat használata
- `*ngIf`, `*ngFor` – pl. `statistics.component.html`, `profile.component.html`, `levels.component.html`
- (Ha van: `*ngSwitch`, `*ngSwitchCase`)

### 5. Adatátadás szülő és gyermek komponensek között (legalább 3 @Input és 3 @Output)
- `level-selector.component.ts`:  
  - `@Input() levels`, `@Input() selectedLevelId`, `@Input() showLocked`
  - `@Output() selectLevel`, `@Output() filterLevels`, `@Output() addLevel`
- Használat: `levels.component.html`

### 6. Legalább 10 különböző Material elem helyes használata
- `MatCardModule`, `MatFormFieldModule`, `MatInputModule`, `MatButtonModule`, `MatIconModule`, `MatTableModule`, `MatSelectModule`, `MatSnackBarModule`, `MatDialogModule`, `MatProgressBarModule` stb.
- Példák: `registration.component.html`, `login.component.html`, `profile.component.html`, `levels.component.html`

### 7. Legalább 2 saját Pipe osztály írása és használata
- `src/app/pipe/tile-icon.pipe.ts` – `TileIconPipe`
- `src/app/pipe/difficulty-color.pipe.ts` – `DifficultyColorPipe`
- Használat: `game.component.html`

### 8. Adatbevitel Angular form-ok segítségével (legalább 4)
- `registration.component.html` – regisztrációs űrlap
- `login.component.html` – bejelentkezési űrlap
- `profile.component.html` – profil szerkesztés
- (Ha van: szint létrehozás vagy pálya szerkesztés)

### 9. Legalább 2 különböző Lifecycle Hook használata
- `ngOnInit` – pl. minden fő komponensben (`game.component.ts`, `statistics.component.ts`, `levels.component.ts`)
- (Ha van: `ngOnDestroy`, `ngAfterViewInit`)

### 10. CRUD műveletek mindegyike megvalósult legalább a projekt fő entitásához (Promise, Observable használattal)
- Példa:  
  - Létrehozás: `game.component.ts` – pont mentése Firestore-ba (`addDoc`)
  - Olvasás: `statistics.component.ts` – leaderboard lekérdezés (`getDocs`)
  - Módosítás: `profile.component.ts` – profil frissítés (`updateDoc`)
  - Törlés: `profile.component.ts` – profil törlés (`deleteDoc`)
- Promise és Observable is használatban.

### 11. CRUD műveletek service-ekbe vannak kiszervezve és megfelelő módon injektálva lettek
- Példa: `auth.service.ts` – bejelentkezés, regisztráció, kijelentkezés
- Service injektálás: pl. minden fő komponens konstruktorában

### 12. Legalább 4 komplex Firestore lekérdezés (where, rendezés, limit, stb.)
- `statistics.component.ts` – `loadLeaderboard()` metódus:
  - Top 10: where + orderBy + limit
  - Utolsó 7 nap: where + where + orderBy + limit
  - Saját eredmények: where + where + orderBy + limit
  - Legjobb mindenkitől: where + orderBy + frontenden aggregálva

### 13. Legalább 4 különböző route a különböző oldalak eléréséhez
- `app.routes.ts`:
  - `/login`
  - `/registration`
  - `/levels`
  - `/game/:levelId`
  - `/profile`
  - `/statistics`

### 14. AuthGuard implementációja
- `guards/auth.guard.ts` – és használata: `app.routes.ts` (pl. `canActivate: [authGuard]`)

### 15. Legalább 2 route levédése azonosítással (AuthGuard)
- `app.routes.ts`:  
  - `/levels`, `/game/:levelId`, `/profile`, `/statistics` route-ok AuthGuard-dal védve

### 16. Szubjektív pontozás
- A projekt teljes egészében a szabaduló szoba témakörhöz kapcsolódik, minden fő funkció (szint, játék, statisztika, profil) megvalósult, a kód strukturált, a felület reszponzív, a Firestore használat komplex.

---

**Ha valamelyik követelmény nem lenne egyértelműen megtalálható, kérlek jelezd!**

---

## 🔍 Ellenőrzéshez tippek

- **Teszteléshez**: próbálj regisztrálni, majd jelentkezz be, nézd meg az oldalak közti navigálást.
- **Responsiveness**: próbáld ki elforgatással (ahol engedélyezve van) vagy tablet emulátorral. (Ha rámész egy xml fájlra, akkor a jobb sávon megjelenik egy 'Layout Validation' gomb.)
- **Animációk**: `overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)` pl. `LoginActivity` → `RegisterActivity` váltáskor. Nézd meg a `MainActivity.java`-ban a '+' gombot.
- **Notifications**: Az egyik notification fájl feltöltéskor jön létre. A scheduler minden nap dél után 5 perccel ad értesítést, hogy előző nap dél óta hány recept feltöltés volt.
---

## 🚀 Futtatás

1. Nyisd meg Android Studio-ban.
2. Csatlakoztass egy emulátort vagy fizikai eszközt (én Samsung fizikai eszközön futtattam).
3. Kattints a zöld **Run** gombra.
4. Enjoy! 😊

---

Készült beadandóként Android fejlesztés témában – 2025.
