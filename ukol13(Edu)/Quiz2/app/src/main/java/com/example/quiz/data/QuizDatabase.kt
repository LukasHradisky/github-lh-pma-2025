package com.example.quiz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Question::class, GameSession::class, UserAnswer::class],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "kotlin_quest_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.quizDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(dao: QuizDao) {

            val questions = listOf(

                Question(
                    questionText = "Co znamená klíčové slovo 'val' v Kotlinu?",
                    optionA = "Proměnná, kterou lze měnit",
                    optionB = "Proměnná, kterou nelze měnit (immutable)",
                    optionC = "Funkce",
                    optionD = "Třída",
                    correctAnswer = "B",
                    difficulty = 1,
                    category = "Kotlin"
                ),
                Question(
                    questionText = "Jak se v Kotlinu vytvoří proměnná, kterou lze měnit?",
                    optionA = "val",
                    optionB = "const",
                    optionC = "var",
                    optionD = "let",
                    correctAnswer = "C",
                    difficulty = 1,
                    category = "Kotlin"
                ),
                Question(
                    questionText = "Jaký je správný způsob definice funkce v Kotlinu?",
                    optionA = "function myFunc() {}",
                    optionB = "fun myFunc() {}",
                    optionC = "def myFunc() {}",
                    optionD = "func myFunc() {}",
                    correctAnswer = "B",
                    difficulty = 1,
                    category = "Kotlin"
                ),
                Question(
                    questionText = "Co vrátí výraz: 'null ?: 5' v Kotlinu?",
                    optionA = "null",
                    optionB = "5",
                    optionC = "Chybu",
                    optionD = "0",
                    correctAnswer = "B",
                    difficulty = 1,
                    category = "Kotlin"
                ),

                // KOTLIN - MEDIUM
                Question(
                    questionText = "Co je data class v Kotlinu?",
                    optionA = "Třída pro práci s databází",
                    optionB = "Třída automaticky generující equals(), hashCode(), toString()",
                    optionC = "Abstraktní třída",
                    optionD = "Interface",
                    correctAnswer = "B",
                    difficulty = 2,
                    category = "Kotlin"
                ),
                Question(
                    questionText = "K čemu slouží 'suspend' klíčové slovo?",
                    optionA = "Pozastavení aplikace",
                    optionB = "Označení funkce pro korutiny",
                    optionC = "Usnutí vlákna",
                    optionD = "Zastavení animace",
                    correctAnswer = "B",
                    difficulty = 2,
                    category = "Kotlin"
                ),
                Question(
                    questionText = "Co dělá 'sealed class' v Kotlinu?",
                    optionA = "Skrývá třídu před ostatními moduly",
                    optionB = "Omezuje možné podtřídy na známé typy",
                    optionC = "Zamyká třídu proti změnám",
                    optionD = "Vytváří singleton",
                    correctAnswer = "B",
                    difficulty = 2,
                    category = "Kotlin"
                ),

                // ANDROID - EASY
                Question(
                    questionText = "Co je Activity v Androidu?",
                    optionA = "Databáze",
                    optionB = "Jedna obrazovka aplikace",
                    optionC = "Síťový požadavek",
                    optionD = "Animace",
                    correctAnswer = "B",
                    difficulty = 1,
                    category = "Android"
                ),
                Question(
                    questionText = "Který soubor obsahuje UI layout v Androidu?",
                    optionA = "Java/Kotlin soubor",
                    optionB = "Manifest",
                    optionC = "XML soubor",
                    optionD = "Gradle soubor",
                    correctAnswer = "C",
                    difficulty = 1,
                    category = "Android"
                ),
                Question(
                    questionText = "Co je RecyclerView?",
                    optionA = "Komponenta pro efektivní zobrazení seznamů",
                    optionB = "Databáze",
                    optionC = "Fragment",
                    optionD = "Service",
                    correctAnswer = "A",
                    difficulty = 1,
                    category = "Android"
                ),

                // ANDROID - MEDIUM
                Question(
                    questionText = "Co je ViewBinding?",
                    optionA = "Knihovna pro databázi",
                    optionB = "Bezpečný přístup k views bez findViewById()",
                    optionC = "Animační framework",
                    optionD = "Síťová knihovna",
                    correctAnswer = "B",
                    difficulty = 2,
                    category = "Android"
                ),
                Question(
                    questionText = "K čemu slouží LifecycleScope v Androidu?",
                    optionA = "Měření životnosti baterie",
                    optionB = "Spuštění korutin vázaných na životní cyklus",
                    optionC = "Debug nástroj",
                    optionD = "UI animace",
                    correctAnswer = "B",
                    difficulty = 2,
                    category = "Android"
                ),

                // ROOM - EASY
                Question(
                    questionText = "Co je Room v Androidu?",
                    optionA = "UI komponenta",
                    optionB = "Abstrakční vrstva nad SQLite databází",
                    optionC = "Fragment manager",
                    optionD = "Síťová knihovna",
                    correctAnswer = "B",
                    difficulty = 1,
                    category = "Room"
                ),
                Question(
                    questionText = "Jakou anotací označíme třídu jako databázovou tabulku?",
                    optionA = "@Table",
                    optionB = "@Database",
                    optionC = "@Entity",
                    optionD = "@Room",
                    correctAnswer = "C",
                    difficulty = 1,
                    category = "Room"
                ),
                Question(
                    questionText = "Co je DAO v Room?",
                    optionA = "Database Access Object - rozhraní pro operace s DB",
                    optionB = "Data Analysis Object",
                    optionC = "Direct Android Operation",
                    optionD = "Dynamic App Observer",
                    correctAnswer = "A",
                    difficulty = 1,
                    category = "Room"
                ),

                // ROOM - MEDIUM
                Question(
                    questionText = "Proč používáme Flow v Room DAO?",
                    optionA = "Pro rychlejší dotazy",
                    optionB = "Pro automatické sledování změn v databázi",
                    optionC = "Pro šifrování dat",
                    optionD = "Pro backup databáze",
                    correctAnswer = "B",
                    difficulty = 2,
                    category = "Room"
                ),
                Question(
                    questionText = "Co dělá anotace @ForeignKey?",
                    optionA = "Importuje cizí knihovnu",
                    optionB = "Vytváří vztah mezi tabulkami",
                    optionC = "Překládá text do jiného jazyka",
                    optionD = "Exportuje data",
                    correctAnswer = "B",
                    difficulty = 2,
                    category = "Room"
                ),

                // HARD QUESTIONS
                Question(
                    questionText = "Co je Dispatchers.IO v korutinách?",
                    optionA = "Hlavní UI vlákno",
                    optionB = "Vlákno pro výpočetně náročné operace",
                    optionC = "Vlákno pro I/O operace (síť, databáze)",
                    optionD = "Debug dispatcher",
                    correctAnswer = "C",
                    difficulty = 3,
                    category = "Kotlin"
                ),
                Question(
                    questionText = "Co znamená @Volatile anotace?",
                    optionA = "Proměnná je nestabilní",
                    optionB = "Změny jsou okamžitě viditelné pro všechna vlákna",
                    optionC = "Proměnná je rychlejší",
                    optionD = "Proměnná je šifrovaná",
                    correctAnswer = "B",
                    difficulty = 3,
                    category = "Kotlin"
                ),
                Question(
                    questionText = "Co je Migration v Room?",
                    optionA = "Přesun aplikace na jiné zařízení",
                    optionB = "Proces aktualizace schématu databáze mezi verzemi",
                    optionC = "Export dat",
                    optionD = "Backup databáze",
                    correctAnswer = "B",
                    difficulty = 3,
                    category = "Room"
                )
            )

            dao.insertQuestions(questions)
        }
    }
}