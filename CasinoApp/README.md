# 🎰 Tutorial: Tvorba Casino Aplikace v Kotlinu

Kompletní průvodce tvorbou casino aplikace pro Android s přihlašovacím systémem, databází a hrami.

---

## 📋 Co budeme vytvářet

- **Přihlašovací systém** s registrací a více uživateli
- **Room databázi** pro ukládání uživatelů a jejich balanceu
- **Ruletu** - klasická casino hra
- **Kostky** - hra s jednoduchými pravidly
- **Správu účtu** - dobíjení, reset, odhlášení

---

## 🛠️ Příprava projektu

### 1. Vytvoření projektu
1. V Android Studiu: **File → New → New Project**
2. Vyber **Empty Views Activity**
3. Název: `CasinoApp`
4. Language: **Kotlin**
5. Minimum SDK: **API 24 (Android 7.0)**

### 2. Přidání závislostí

V souboru `build.gradle.kts (Module: app)` přidej:

```kotlin
dependencies {
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    
    // ViewBinding
    buildFeatures {
        viewBinding = true
    }
}
```

A v `build.gradle.kts (Project)` přidej KSP plugin:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
}
```

---

## 📁 Struktura projektu

```
app/src/main/java/com/example/casinoapp/
├── data/
│   ├── User.kt                      # Datová třída pro uživatele
│   ├── UserDao.kt                   # Databázové operace
│   ├── CasinoDatabase.kt            # Room databáze
│   └── CasinoDatabaseInstance.kt    # Singleton instance
├── SessionManager.kt                # Správa přihlášení
├── LoginActivity.kt                 # Přihlášení a registrace
├── MainActivity.kt                  # Hlavní obrazovka
├── RouletteActivity.kt              # Hra - Ruleta
└── DiceActivity.kt                  # Hra - Kostky
```

---

## 🗄️ Krok 1: Vytvoření databáze

### User.kt - Datový model
```kotlin
@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val balance: Double
)
```

**Co dělá:**
- `@Entity` - označuje třídu jako databázovou tabulku
- `@PrimaryKey(autoGenerate = true)` - automatické generování ID
- Uživatel má: jméno, heslo a balance (peníze)

### UserDao.kt - Databázové operace
```kotlin
@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)
    
    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?
    
    @Query("SELECT * FROM user_table WHERE id = :userId")
    fun getUserById(userId: Int): Flow<User?>
    
    @Update
    suspend fun updateUser(user: User)
}
```

**Co dělá:**
- `@Insert` - vložení nového uživatele do databáze
- `@Query` - SQL dotazy pro načítání dat
- `@Update` - aktualizace existujícího uživatele
- `Flow<User?>` - automatická aktualizace při změně dat

### CasinoDatabase.kt - Definice databáze
```kotlin
@Database(entities = [User::class], version = 2, exportSchema = false)
abstract class CasinoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

**Co dělá:**
- Vytváří Room databázi s verzí 2
- Obsahuje tabulku `User`
- Poskytuje přístup k `UserDao`

### CasinoDatabaseInstance.kt - Singleton
```kotlin
object CasinoDatabaseInstance {
    @Volatile
    private var INSTANCE: CasinoDatabase? = null

    fun getDatabase(context: Context): CasinoDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                CasinoDatabase::class.java,
                "casino_database"
            ).fallbackToDestructiveMigration().build()
            INSTANCE = instance
            instance
        }
    }
}
```

**Co dělá:**
- Vytváří jen jednu instanci databáze (Singleton pattern)
- `fallbackToDestructiveMigration()` - při změně verze smaže starou databázi

---

## 🔐 Krok 2: Správa přihlášení

### SessionManager.kt
```kotlin
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("casino_session", Context.MODE_PRIVATE)

    fun saveLogin(userId: Int) {
        prefs.edit().apply {
            putInt("user_id", userId)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt("user_id", -1)
    
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)
    
    fun logout() {
        prefs.edit().clear().apply()
    }
}
```

**Co dělá:**
- Ukládá přihlášení do `SharedPreferences` (přetrvá i po restartu)
- `saveLogin()` - uloží ID přihlášeného uživatele
- `isLoggedIn()` - kontroluje, zda je někdo přihlášen
- `logout()` - odhlásí uživatele

---

## 🎨 Krok 3: Layouts (XML)

### activity_login.xml - Přihlašovací obrazovka
```xml
<LinearLayout>
    <MaterialToolbar /> <!-- Horní lišta -->
    
    <TextInputEditText id="etUsername" /> <!-- Jméno -->
    <TextInputEditText id="etPassword" /> <!-- Heslo -->
    
    <Button id="btnLogin" /> <!-- Přihlásit se -->
    <Button id="btnRegister" /> <!-- Registrovat -->
    <TextView id="tvGuestLogin" /> <!-- Host režim -->
</LinearLayout>
```

### activity_main.xml - Hlavní obrazovka
```xml
<LinearLayout>
    <MaterialToolbar /> <!-- Horní lišta s menu -->
    
    <CardView> <!-- Karta s balanceem -->
        <TextView id="tvUserDisplay" /> <!-- Jméno hráče -->
        <TextView id="tvBalance" /> <!-- Balance -->
    </CardView>
    
    <Button id="btnAddMoney" /> <!-- Dobít konto -->
    <Button id="btnPlayRoulette" /> <!-- Hrát ruletu -->
    <Button id="btnPlayDice" /> <!-- Hrát kostky -->
</LinearLayout>
```

### activity_roulette.xml - Ruleta
```xml
<LinearLayout>
    <MaterialToolbar /> <!-- Zpět na hlavní -->
    
    <TextView id="tvRouletteBalance" /> <!-- Balance -->
    <EditText id="etBetAmount" /> <!-- Sázka -->
    <EditText id="etBetNumber" /> <!-- Číslo 0-36 -->
    
    <Button id="btnBetNumber" /> <!-- Vsadit na číslo -->
    <Button id="btnBetRed" /> <!-- Vsadit na červenou -->
    <Button id="btnBetBlack" /> <!-- Vsadit na černou -->
    
    <TextView id="tvResultNumber" /> <!-- Výsledné číslo -->
    <TextView id="tvGameStatus" /> <!-- Výhra/prohra -->
</LinearLayout>
```

### activity_dice.xml - Kostky
```xml
<LinearLayout>
    <MaterialToolbar /> <!-- Zpět na hlavní -->
    
    <TextView id="tvDiceBalance" /> <!-- Balance -->
    <EditText id="etDiceBet" /> <!-- Sázka -->
    
    <Button id="btnRollDice" /> <!-- Hodit kostkami -->
    
    <TextView id="tvDice1" /> <!-- Kostka 1 -->
    <TextView id="tvDice2" /> <!-- Kostka 2 -->
    <TextView id="tvDiceSum" /> <!-- Součet -->
    <TextView id="tvDiceResult" /> <!-- Výsledek -->
</LinearLayout>
```

---

## 🔑 Krok 4: LoginActivity - Přihlášení

### Základní struktura
```kotlin
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        setSupportActionBar(binding.toolbarLogin)
        
        setupButtons()
    }
}
```

### Registrace nového uživatele
```kotlin
private fun registerUser(username: String, password: String) {
    lifecycleScope.launch(Dispatchers.IO) {
        val dao = CasinoDatabaseInstance.getDatabase(this@LoginActivity).userDao()
        
        // Zkontroluj, zda jméno už neexistuje
        if (dao.userExists(username) > 0) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "Jméno už existuje!", Toast.LENGTH_SHORT).show()
            }
            return@launch
        }
        
        // Vytvoř nového uživatele se startovním balanceem 1000 Kč
        val newUser = User(username = username, password = password, balance = 1000.0)
        dao.insertUser(newUser)
        
        // Automaticky ho přihlas
        val createdUser = dao.login(username, password)
        
        withContext(Dispatchers.Main) {
            if (createdUser != null) {
                sessionManager.saveLogin(createdUser.id)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
```

**Co se děje:**
1. Zkontroluje, zda jméno už neexistuje
2. Vytvoří nového uživatele s 1000 Kč
3. Automaticky ho přihlásí
4. Přesměruje na hlavní obrazovku

### Přihlášení
```kotlin
private fun loginUser(username: String, password: String) {
    lifecycleScope.launch(Dispatchers.IO) {
        val dao = CasinoDatabaseInstance.getDatabase(this@LoginActivity).userDao()
        val user = dao.login(username, password)
        
        withContext(Dispatchers.Main) {
            if (user != null) {
                sessionManager.saveLogin(user.id)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Špatné údaje!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

---

## 🏠 Krok 5: MainActivity - Hlavní obrazovka

### Kontrola přihlášení
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    sessionManager = SessionManager(this)
    
    // Pokud není přihlášen, přesměruj na login
    if (!sessionManager.isLoggedIn()) {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        return
    }
    
    // Pokračuj s načtením UI...
}
```

### Načítání dat uživatele
```kotlin
lifecycleScope.launch {
    userDao.getUserById(userId).collect { user ->
        if (user != null) {
            currentUser = user
            binding.tvBalance.text = "${user.balance.toInt()} Kč"
            binding.tvUserDisplay.text = "Hráč: ${user.username}"
        }
    }
}
```

**Proč Flow?**
- `Flow` automaticky aktualizuje UI při změně dat v databázi
- Když změníš balance, okamžitě se zobrazí nová hodnota

### Menu s odhlášením
```kotlin
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.nav_login -> {
            showAccountDialog() // Dialog s odhlášením
            true
        }
        R.id.nav_reset -> {
            confirmReset() // Reset balance
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
```

### Dobíjení účtu
```kotlin
private fun showDepositDialog() {
    val castky = arrayOf("100 Kč", "500 Kč", "1000 Kč", "5000 Kč")
    AlertDialog.Builder(this)
        .setTitle("Dobít konto")
        .setItems(castky) { _, index ->
            val vklad = when (index) {
                0 -> 100.0
                1 -> 500.0
                2 -> 1000.0
                3 -> 5000.0
                else -> 0.0
            }
            updateBalance(vklad)
        }.show()
}

private fun updateBalance(amount: Double) {
    currentUser?.let { user ->
        lifecycleScope.launch(Dispatchers.IO) {
            val db = CasinoDatabaseInstance.getDatabase(this@MainActivity)
            db.userDao().updateUser(user.copy(balance = user.balance + amount))
        }
    }
}
```

---

## 🎲 Krok 6: RouletteActivity - Hra ruleta

### Pravidla rulety
- **Vsadit na číslo (0-36)**: Výhra 35x sázky
- **Vsadit na barvu (červená/černá)**: Výhra 1x sázky
- **0 je zelené** - prohra pro obě barvy

### Implementace hry
```kotlin
private fun provestHru(sazka: Double, kontrolaVyhry: (Int) -> Boolean) {
    val balance = currentUser?.balance ?: 0.0
    
    // Kontrola balance
    if (sazka > balance) {
        Toast.makeText(this, "Nemáš dostatek peněz!", Toast.LENGTH_SHORT).show()
        return
    }
    
    // Vylosuj číslo 0-36
    val vylosovane = (0..36).random()
    binding.tvResultNumber.text = vylosovane.toString()
    
    // Vyhodnoť výhru/prohru
    val vyhra = if (kontrolaVyhry(vylosovane)) {
        if (binding.etBetNumber.text.isNotEmpty()) 
            sazka * 35  // Číslo = 35x
        else 
            sazka       // Barva = 1x
    } else {
        -sazka
    }
    
    // Aktualizuj balance
    lifecycleScope.launch(Dispatchers.IO) {
        currentUser?.let {
            CasinoDatabaseInstance.getDatabase(this@RouletteActivity).userDao()
                .updateUser(it.copy(balance = it.balance + vyhra))
        }
    }
    
    // Zobraz výsledek
    binding.tvGameStatus.text = if (vyhra > 0) 
        "VÝHRA: ${vyhra.toInt()} Kč" 
    else 
        "PROHRA: ${(-vyhra).toInt()} Kč"
}
```

### Sázka na barvu
```kotlin
private fun hrajBarvu(vsazenaCervena: Boolean) {
    val sazka = binding.etBetAmount.text.toString().toDoubleOrNull() ?: 0.0
    if (sazka <= 0.0) return
    
    val cervenaCisla = setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
    
    provestHru(sazka) { vylosovane ->
        vylosovane != 0 && cervenaCisla.contains(vylosovane) == vsazenaCervena
    }
}
```

---

## 🎲 Krok 7: DiceActivity - Hra kostky

### Pravidla kostek
- **Součet 7 nebo 11**: Výhra 2x sázky
- **Součet 2, 3 nebo 12**: Prohra
- **Ostatní čísla**: Neutrální (vrátí sázku)

### Implementace s animací
```kotlin
private fun hratKostky() {
    val sazka = binding.etDiceBet.text.toString().toDoubleOrNull() ?: 0.0
    val balance = currentUser?.balance ?: 0.0
    
    if (sazka <= 0.0 || sazka > balance) return
    
    lifecycleScope.launch {
        binding.btnRollDice.isEnabled = false
        
        // Animace házení (10x, každých 100ms)
        repeat(10) {
            binding.tvDice1.text = (1..6).random().toString()
            binding.tvDice2.text = (1..6).random().toString()
            delay(100)
        }
        
        // Finální hod
        val dice1 = (1..6).random()
        val dice2 = (1..6).random()
        val suma = dice1 + dice2
        
        binding.tvDice1.text = dice1.toString()
        binding.tvDice2.text = dice2.toString()
        binding.tvDiceSum.text = "Součet: $suma"
        
        // Vyhodnocení
        val vysledek = when (suma) {
            7, 11 -> sazka * 2      // Výhra
            2, 3, 12 -> -sazka      // Prohra
            else -> 0.0             // Neutrální
        }
        
        // Aktualizace balance
        if (vysledek != 0.0) {
            withContext(Dispatchers.IO) {
                currentUser?.let {
                    CasinoDatabaseInstance.getDatabase(this@DiceActivity).userDao()
                        .updateUser(it.copy(balance = it.balance + vysledek))
                }
            }
        }
        
        binding.btnRollDice.isEnabled = true
    }
}
```

---

## 📱 Krok 8: AndroidManifest.xml

```xml
<manifest>
    <application>
        <!-- Startovací aktivita = LoginActivity -->
        <activity
            android:name=".LoginActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Ostatní aktivity -->
        <activity android:name=".MainActivity" android:exported="false" />
        <activity android:name=".RouletteActivity" android:exported="false" />
        <activity android:name=".DiceActivity" android:exported="false" />
    </application>
</manifest>
```

---

## ✅ Checklist před spuštěním

- [ ] Všechny závislosti přidány v `build.gradle.kts`
- [ ] ViewBinding aktivován
- [ ] Všechny soubory v `data/` složce vytvořeny
- [ ] `SessionManager.kt` vytvořen
- [ ] Všechny aktivity vytvořeny
- [ ] Všechny XML layouty vytvořeny
- [ ] `AndroidManifest.xml` správně nakonfigurován
- [ ] Build → Clean Project
- [ ] Build → Rebuild Project
- [ ] Odinstalovat starou verzi z emulátoru

---

## 🎮 Jak aplikace funguje

### 1. Spuštění
- Aplikace zkontroluje, zda je někdo přihlášen
- Pokud ne → zobrazí `LoginActivity`
- Pokud ano → zobrazí `MainActivity`

### 2. Přihlášení/Registrace
- Uživatel zadá jméno a heslo
- Při registraci se vytvoří účet s 1000 Kč
- `SessionManager` uloží ID přihlášeného uživatele

### 3. Hlavní obrazovka
- Načte data přihlášeného uživatele z databáze
- Zobrazí aktuální balance
- Tlačítka pro dobíjení a spuštění her

### 4. Hry
- Každá hra načte aktuální balance
- Po skončení kola aktualizuje balance v databázi
- Díky `Flow` se změna okamžitě projeví všude

### 5. Odhlášení
- `SessionManager` vymaže přihlášení
- Aplikace přesměruje na `LoginActivity`

---

## 🔧 Časté problémy a řešení

### Aplikace spadne při spuštění
- **Řešení**: Build → Clean Project, pak Rebuild
- Odinstaluj aplikaci z emulátoru a spusť znovu

### Balance se neaktualizuje
- **Problém**: Používáš `suspend fun` místo `Flow`
- **Řešení**: Použij `Flow<User?>` pro automatickou aktualizaci

### Přihlášení nefunguje
- **Problém**: Chybně implementovaný `SessionManager`
- **Řešení**: Zkontroluj, že správně ukládáš a načítáš userId

### Databáze se nezměnila
- **Problém**: Zapomněl jsi změnit verzi v `@Database`
- **Řešení**: Zvyš `version = X` a přidej `fallbackToDestructiveMigration()`

---

## 🚀 Možná vylepšení

- **Více her**: Blackjack, poker, automaty
- **Žebříčky**: Uložení nejlepších hráčů
- **Historie**: Zobrazení historie sázek
- **Animace**: Lepší vizuální efekty
- **Zvuky**: Zvukové efekty pro hry
- **Denní bonus**: Automatické dobíjení každý den
- **Achievements**: Odznaky za dosažení cílů

---

## 📚 Použité technologie

- **Kotlin** - programovací jazyk
- **Room Database** - lokální databáze
- **Coroutines** - asynchronní operace
- **Flow** - reaktivní datové toky
- **ViewBinding** - bezpečný přístup k views
- **Material Design** - moderní UI komponenty
- **SharedPreferences** - ukládání přihlášení

---

**Vytvořeno v Android Studiu | Kotlin | 2025**
