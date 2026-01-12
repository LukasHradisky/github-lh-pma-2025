\# Casino App - Android Studio Project



Tato aplikace je simulátorem kasina vyvinutým v jazyce \*\*Kotlin\*\*. Projekt se zaměřuje na implementaci lokální databáze pro správu uživatelského konta a herní logiku jednoduchých hazardních her.



\## 🚀 Hlavní funkce

\- \*\*Uživatelský Hub:\*\* Přehled o aktuálním stavu konta.

\- \*\*Správa financí:\*\* Možnost "vkladu" peněz do virtuální peněženky.

\- \*\*Ruleta:\*\* Plně funkční simulace evropské rulety.

\- \*\*Další hry:\*\* (Např. Hrací automat / Větší bere).



\## 🛠 Technologický Stack

\- \*\*Kotlin:\*\* Hlavní programovací jazyk.

\- \*\*Jetpack Compose:\*\* Moderní deklarativní UI.

\- \*\*Room Database:\*\* SQLite knihovna pro ukládání dat o hráči.

\- \*\*ViewModel \& LiveData/Flow:\*\* Správa stavu aplikace a reaktivní propojení s DB.



\## 💾 Databáze a CRUD operace

Projekt využívá knihovnu \*\*Room\*\* pro splnění požadavku na práci s databází.



| Operace | Implementace v aplikaci |

| :--- | :--- |

| \*\*Create\*\* | Vytvoření nového profilu hráče při prvním spuštění aplikace. |

| \*\*Read\*\* | Načítání aktuálního zůstatku v Hubu a v jednotlivých hrách. |

| \*\*Update\*\* | Přičítání/odečítání peněz po sázkách a při vkladu financí. |

| \*\*Delete\*\* | Funkce "Reset účtu", která vymaže data a nastaví počáteční stav. |



\### Schéma Entity (User)

```kotlin

@Entity(tableName = "user\_table")

data class User(

&nbsp;   @PrimaryKey(autoGenerate = true) val id: Int = 0,

&nbsp;   val username: String,

&nbsp;   val balance: Double

)

