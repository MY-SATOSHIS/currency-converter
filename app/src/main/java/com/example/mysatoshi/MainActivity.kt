package com.example.mysatoshi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.util.*

object Strings {
    var texts = mapOf(
        "fr" to mapOf(
            "title" to "CONVERTISSEUR DE MONNAIE",
            "amount" to "Montant en",
            "result" to "Résultat",
            "settings" to "Paramètres",
            "about" to "À propos",
            "footer" to "Copyrights © My Satoshis  v2.0, 2025",
            "conversion_settings" to "Paramètres de conversion",
            "taux" to "Taux USD/BIF",
            "prix" to "Prix du BTC en USD",
            "langue" to "Langue",
            "retour" to "Retour",
            "about_content" to "Convertisseur numérique BTC, SATS, USD, BIF\n Cette application vous permet de fixer le taux et faire les conversions variantes. \nDéveloppé à Gitega, par Advaxe pour la communauté burundaise vivant dans les localités à faire connexion internet\n  2025\nMy Satoshis"
        ),
        "en" to mapOf(
            "title" to "DIGITAL CONVERTER",
            "amount" to "Amount in",
            "result" to "Result",
            "settings" to "Settings",
            "about" to "About",
            "footer" to "Copyrights © My Satoshis v2.0, 2025",
            "conversion_settings" to "Conversion Settings",
            "taux" to "USD/BIF Rate",
            "prix" to "BTC Price in USD",
            "langue" to "Language",
            "retour" to "Back",
            "about_content" to "Digital converter BTC, SATS, USD, BIF\nDeveloped in Gitega, 2025\nMy Satoshis"
        ),
        "rn" to mapOf(
            "title" to "UKUVUNJA AMAFARANGA",
            "amount" to "Amafaranga muma",
            "result" to "Atanga",
            "settings" to "Ibiciro",
            "about" to "Ikoreshwa",
            "footer" to "Copyrights © My Satoshis v2.0, 2025",
            "conversion_settings" to "IHINDURWA RY'IBICIRO",
            "taux" to "Ikiguzi ca USD muma BIF",
            "prix" to "Ikiguzi ca BTC muma USD",
            "langue" to "Ururimi",
            "retour" to "Subira inyuma",
            "about_content" to "Ivunjwa ry'amafaranga \nn'ihindagurika ry'ibiciro BTC, SATS, USD, BIF\nYakorewe i Gitega, 2025\nMy satoshis"
        ),
        "sw" to mapOf(
            "title" to "KUGEUZA FEDHA",
            "amount" to "Kiasi katika",
            "result" to "Matokeo",
            "settings" to "Mipangilio",
            "about" to "Kuhusu",
            "footer" to "Copyrights © My Satoshis v2.0, 2025",
            "conversion_settings" to "Mipangilio ya Kugeuza",
            "taux" to "Kiwango cha USD/BIF",
            "prix" to "Bei ya BTC kwa USD",
            "langue" to "Lugha",
            "retour" to "Rudi",
            "about_content" to "Kigeuza fedha BTC, SATS, USD, BIF\nKimeandaliwa Gitega, 2025\nMy Satoshis"
        )
    )

    fun get(key: String, language: String): String {
        return texts[language]?.get(key) ?: key
    }
}

interface YadioApiService {
    @GET("convert/{amount}/{from}/{to}")
    fun convertCurrency(
        @Path("amount") amount: Double,
        @Path("from") from: String,
        @Path("to") to: String
    ): Call<ConversionResponse>
}

data class ConversionResponse(
    var result: Double
)

class MainActivity : ComponentActivity() {

    private lateinit var apiService: YadioApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration de Retrofit
        var retrofit = Retrofit.Builder()
            .baseUrl("https://api.yadio.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(YadioApiService::class.java)

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                var screen by remember { mutableStateOf("main") }
                var tauxUsdBif by remember { mutableStateOf("7700") }
                var prixBtcUsd by remember { mutableStateOf("116500") }
                var language by remember { mutableStateOf("fr") }
                var conversionResult by remember { mutableStateOf(0.0) }
                var montant by remember { mutableStateOf("") }
                var sourceDevise by remember { mutableStateOf("BTC") }
                var targetDevise by remember { mutableStateOf("SATS") }

                LaunchedEffect(Unit) {
                    // Charger les taux initiaux
                    loadRates(this@MainActivity) { loadedTaux, loadedPrix ->
                        tauxUsdBif = loadedTaux
                        prixBtcUsd = loadedPrix
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (screen) {
                        "main" -> MainScreen(
                            tauxUsdBif,
                            prixBtcUsd,
                            language,
                            montant,
                            conversionResult,
                            sourceDevise,
                            targetDevise,
                            onConvert = { amount ->
                                performConversion(amount.toDoubleOrNull() ?: 0.0, sourceDevise, targetDevise) { result ->
                                    conversionResult = result
                                }
                            },
                            onSettingsClick = { screen = "settings" },
                            onAboutClick = { screen = "about" }
                        )

                        "settings" -> SettingsScreen(
                            tauxUsdBif,
                            prixBtcUsd,
                            language,
                            onTauxChange = {
                                tauxUsdBif = it
                                saveRates(this@MainActivity, it, prixBtcUsd)
                            },
                            onPrixChange = {
                                prixBtcUsd = it
                                saveRates(this@MainActivity, tauxUsdBif, it)
                            },
                            onLanguageChange = { language = it },
                            onBack = { screen = "main" }
                        )

                        "about" -> AboutScreen(language, onBack = { screen = "main" })
                    }
                }
            }
        }
    }

    private fun performConversion(amount: Double, from: String, to: String, callback: (Double) -> Unit) {
        apiService.convertCurrency(amount, from, to).enqueue(object : Callback<ConversionResponse> {
            override fun onResponse(call: Call<ConversionResponse>, response: Response<ConversionResponse>) {
                if (response.isSuccessful) {
                    var result = response.body()?.result ?: 0.0
                    callback(result)
                } else {
                    callback(0.0) // Gestion d'erreur
                }
            }

            override fun onFailure(call: Call<ConversionResponse>, t: Throwable) {
                callback(0.0) // Gestion d'erreur
            }
        })
    }

    private fun loadRates(context: Context, callback: (String, String) -> Unit) {
        var prefs = context.getSharedPreferences("rates", Context.MODE_PRIVATE)
        var taux = prefs.getString("taux", "7700") ?: "7700"
        var prix = prefs.getString("prix", "116500") ?: "116500"
        callback(taux, prix)
    }

    private fun saveRates(context: Context, taux: String, prix: String) {
        var prefs = context.getSharedPreferences("rates", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("taux", taux)
            putString("prix", prix)
            apply()
        }
    }

    private fun lightColorScheme(): ColorScheme {
        return lightColorScheme(
            primary = Color(0xFF4CAF50), // Couleur principale (vert)
            onPrimary = Color.White,
            secondary = Color(0xFF81C784), // Couleur secondaire (vert clair)
            onSecondary = Color.Black
        )
    }
}

@Composable
fun MainScreen(
    tauxUsdBif: String,
    prixBtcUsd: String,
    language: String,
    montant: String,
    conversionResult: Double,
    sourceDevise: String,
    targetDevise: String,
    onConvert: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    var t = { key: String -> Strings.get(key, language) }
    var devises = listOf("BTC", "SATS", "USD", "BIF")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                t("title"),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                devises.forEach { base ->
                  //var expanded by remember { mutableStateOf(false) }
//                    val backgroundColor by animateColorAsState(
//                        if (sourceDevise == base) MaterialTheme.colorScheme.primary
//                        else MaterialTheme.colorScheme.secondaryContainer
//                    )
//                    val contentColor by animateColorAsState(
//                        if (sourceDevise == base) MaterialTheme.colorScheme.onPrimary
//                        else MaterialTheme.colorScheme.onSecondaryContainer
//                    )
                    var expanded by remember { mutableStateOf(false) }

                    // Récupérer les couleurs animées sans réaffectation
                    val backgroundColor = animateColorAsState(
                        targetValue = if (sourceDevise == base) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondaryContainer
                    ).value

                    val contentColor = animateColorAsState(
                        targetValue = if (sourceDevise == base) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    ).value


                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = backgroundColor,
                                contentColor = contentColor
                            )
                        ) {
                            Text(base)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            devises.filter { it != base }.forEach { target ->
                                DropdownMenuItem(
                                    text = { Text("→ $target") },
                                    onClick = {
                                        sourceDevise = base
                                        targetDevise = target
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = montant,
                onValueChange = { montant = it },
                label = { Text("${t("amount")} $sourceDevise") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { onConvert(montant) }) {
                Text("Convertir")
            }

            SelectionContainer {
                Text(
                    "${t("result")} : ${NumberFormat.getNumberInstance(Locale.US).format(conversionResult)} $targetDevise",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = t("settings"))
                }
                IconButton(onClick = onAboutClick) {
                    Icon(Icons.Default.Info, contentDescription = t("about"))
                }
            }

            Text(
                t("footer"),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }
    }
}

@Composable
fun SettingsScreen(
    tauxUsdBif: String,
    prixBtcUsd: String,
    language: String,
    onTauxChange: (String) -> Unit,
    onPrixChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var t = { key: String -> Strings.get(key, language) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = t("retour"))
            }
            Text(
                text = t("settings"),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(t("conversion_settings"), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = tauxUsdBif,
            onValueChange = onTauxChange,
            label = { Text(t("taux")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = prixBtcUsd,
            onValueChange = onPrixChange,
            label = { Text(t("prix")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text(t("langue"), style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onLanguageChange("fr") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (language == "fr") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("FR")
            }
            Button(
                onClick = { onLanguageChange("en") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (language == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("EN")
            }
            Button(
                onClick = { onLanguageChange("sw") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (language == "sw") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("SW")
            }
            Button(
                onClick = { onLanguageChange("rn") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (language == "rn") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("KI")
            }
        }
    }
}

@Composable
fun AboutScreen(language: String, onBack: () -> Unit) {
    var t = { key: String -> Strings.get(key, language) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = t("retour"))
            }
            Text(
                text = t("about"),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = t("about_content"),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}