package com.example.mysatoshi

import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontStyle
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
import com.google.gson.Gson
import retrofit2.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.util.*
import android.util.Log

data class ConversionResponse(
    val result: Double
)

interface YadioApiService {
    @GET("convert/{amount}/{from}/{to}")
    fun convertCurrency(
        @Path("amount") amount: Double,
        @Path("from") from: String,
        @Path("to") to: String
    ): Call<ConversionResponse>

    @GET("convert/1/BTC/USD")  // Prix 1 BTC en USD
    fun getBtcUsdRate(): Call<ConversionResponse>

    @GET("convert/1/USD/BIF")  // Taux USD/BIF direct depuis Yadio
    fun getUsdBifRate(): Call<ConversionResponse>
}

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
            "convert" to "Convertir",
            "quote" to "Le voyage de mille kilomètres commence par un pas. - Lao Tseu",
            "about_content" to "MySatoshi est une application de conversion numérique conçue pour faciliter les échanges financiers dans les pays africains. Elle permet de convertir en temps réel entre Bitcoin (BTC), Satoshi (SATS), Dollar américain (USD) et Franc burundais (BIF) à l'aide de l'API fiable Yadio.io. Fonctionnalités clés : taux dynamiques synchronisés, interface multilingue, et support offline via taux manuels sauvegardés. Développée à Gitega en 2025  pour répondre aux besoins de la communauté burundaise, même dans les zones rurales avec connexion limitée.\n  2025 My Satoshis,v2.0"
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
            "convert" to "Convert",
            "quote" to "The journey of a thousand miles begins with a single step. - Lao Tzu",
            "about_content" to "MySatoshi is a digital converter app built to simplify financial transactions in African countries. It enables real-time conversions between Bitcoin (BTC), Satoshi (SATS), US Dollar (USD), and Burundian Franc (BIF) using the reliable Yadio.io API. Key features: live synchronized rates, multi-language interface, and offline support with saved manual rates. Developed in Gitega, 2025  to meet the needs of the Burundian community, even in rural areas with limited connectivity.\n2025 My Satoshis,v2.0"
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
            "convert" to "Vunga",
            "quote" to "Urugendo rw'ibilometero igihumbi rutangurirwa n'intambwe imwe. - Lao Tseu",
            "about_content" to "MySatoshi ni porogaramu y'ukuvunja amafaranga yakozwe kugira ngo yorohereze uguhanahana amafarnga mu bihugu vyo muri Afrika. Idufasha kuvunja amafaranga ya: Bitcoin (BTC), Satoshis (SATS), Dorari ry'Amerika (USD), na Amafaranga y'Amarundi (BIF) ukoresheza API ya YADIO. Ibiyigize ahakuru ni: Ukumenya igiciro c'ako kanya, mu ndimi zitandukanye harimwo ikirundi,icongereza igifarnsa n'igiswahili, hamwe n'ugufasha uburere butagira internet imeze neza gukora ata internete. Yakorewe i Gitega mu 2025 kugira ikoreshwe muri Africa, no muburere bufise internet itanyaruka.\n2025,My Satoshis v2.0"
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
            "convert" to "Badilisha",
            "quote" to "Safari ya kilomita elfu hutaka kivumbi cha kwanza. - Lao Tzu",
            "about_content" to "MySatoshi ni programu ya ubadilishaji pesa mtandaoni iliyoundwa ili kurahisisha shughuli za kifedha katika nchi za Afrika. Inaruhusu ubadilishaji wa hali ya juu kati ya Bitcoin (BTC), Satoshi (SATS), Dola ya Marekani (USD), na Shilingi ya Burundi (BIF) kwa kutumia API thabiti ya Yadio.io. Vipengele kuu: viwango vilivyosawazishwa wakati halisi, kiolesura cha lugha nyingi, na msaada wa mtandaoni kwa kutumia-viwango vilivyohifadhiwa kiwekwa mkono. Iliandaliwa Gitega, 2025  ili kukidhi mahitaji ya jamii ya Burundi, hata katika maeneo ya mashamba yenye mtandao mdogo.\n2025,My Satoshis v2.0"
        )
    )

    fun get(key: String, language: String): String {
        return texts[language]?.get(key) ?: key
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var apiService: YadioApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.yadio.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(YadioApiService::class.java)

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                var screen by remember { mutableStateOf("main") }
                var tauxUsdBif by remember { mutableStateOf("6500.0") }
                var prixBtcUsd by remember { mutableStateOf("89500.0") }
                var language by remember { mutableStateOf("fr") }
                var conversionResult by remember { mutableStateOf(0.0) }
                var montant by remember { mutableStateOf("") }
                var sourceDevise by remember { mutableStateOf("BTC") }
                var targetDevise by remember { mutableStateOf("SATS") }
                var isLoadingRates by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    loadRates(this@MainActivity) { loadedTaux, loadedPrix ->
                        tauxUsdBif = loadedTaux
                        prixBtcUsd = loadedPrix
                    }
                    loadRemoteRates(
                        onLoading = { loading -> isLoadingRates = loading },
                        onError = { err -> errorMessage = err },
                        callback = { btcPrice, bifRate ->
                            btcPrice?.let { prixBtcUsd = it }
                            bifRate?.let { tauxUsdBif = it }
                        }
                    )
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (errorMessage != null) {
                        Snackbar(
                            modifier = Modifier.padding(16.dp),
                            action = {
                                TextButton(onClick = { errorMessage = null }) {
                                    Text("OK")
                                }
                            }
                        ) {
                            Text(errorMessage!!)
                        }
                    }

                    when (screen) {
                        "main" -> MainScreen(
                            tauxUsdBif,
                            prixBtcUsd,
                            language,
                            montant,
                            conversionResult,
                            sourceDevise,
                            targetDevise,
                            isLoadingRates,
                            onMontantChange = { montant = it },
                            onDeviseChange = { src, tgt ->
                                sourceDevise = src
                                targetDevise = tgt
                            },
                            onConvert = { amount ->
                                performConversion(
                                    amount.toDoubleOrNull() ?: 0.0,
                                    sourceDevise,
                                    targetDevise,
                                    tauxUsdBif.toDoubleOrNull() ?: 7700.0,
                                    prixBtcUsd.toDoubleOrNull() ?: 116500.0,
                                    { result -> conversionResult = result },
                                    { error -> errorMessage = error }
                                )
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

    private fun performConversion(
        amount: Double,
        from: String,
        to: String,
        tauxUsdBif: Double,
        prixBtcUsd: Double,
        callback: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        var adjustedAmount = amount
        var adjustedFrom = from
        if (from == "SATS") {
            adjustedAmount /= 100000000.0  // SATS -> BTC
            adjustedFrom = "BTC"
        }

        var adjustedTo = to
        if (to == "SATS") {
            adjustedTo = "BTC"
        }

        apiService.convertCurrency(adjustedAmount, adjustedFrom, adjustedTo).enqueue(object : Callback<ConversionResponse> {
            override fun onResponse(call: Call<ConversionResponse>, response: Response<ConversionResponse>) {
                if (response.isSuccessful) {
                    var result = response.body()?.result ?: 0.0
                    if (to == "SATS") {
                        result *= 100000000.0  // BTC -> SATS
                    }
                    callback(result)
                } else onError("Erreur conversion API (${response.code()})")
            }
            override fun onFailure(call: Call<ConversionResponse>, t: Throwable) {
                //onError("Erreur réseau : ${t.message}")
                // Fallback : calcul manuel si API échoue
                var manualResult = 0.0
                if (from == "BTC" && to == "USD") {
                    manualResult = adjustedAmount * prixBtcUsd
                } else if (from == "BTC" && to == "BIF") {
                    manualResult = adjustedAmount * prixBtcUsd * tauxUsdBif
                } else if (from == "BTC" && to == "SATS") {
                    manualResult = adjustedAmount*100000000.0
                }  else if (from == "USD" && to == "BTC") {
                    manualResult = adjustedAmount / prixBtcUsd
                } else if (from == "USD" && to == "BIF") {
                    manualResult = adjustedAmount * tauxUsdBif
                }else if (from == "USD" && to == "SATS") {
                    manualResult = adjustedAmount / prixBtcUsd/100000000.0
                }else if (from == "SATS" && to == "USD") {
                    manualResult = (adjustedAmount /100000000.0)* prixBtcUsd
                }else if (from == "SATS" && to == "BIF") {
                    manualResult = (adjustedAmount /100000000.0)*prixBtcUsd*tauxUsdBif
                }else if (from == "SATS" && to == "BTC") {
                    manualResult = (adjustedAmount /100000000.0)
                }else if (from == "BIF" && to == "BTC") {
                    manualResult = adjustedAmount /tauxUsdBif/prixBtcUsd
                }else if (from == "BIF" && to == "SATS") {
                    manualResult = adjustedAmount /tauxUsdBif/prixBtcUsd*100000000.0
                }else if (from == "BIF" && to == "USD") {
                    manualResult = adjustedAmount /tauxUsdBif
                }
                // ... autres fallbacks
                if (manualResult > 0.0) {
                    callback(manualResult)
                } else {
                    onError("Conversion manuelle échouée")
                }
            }
        })
    }

    private fun loadRemoteRates(onLoading: (Boolean) -> Unit, onError: (String) -> Unit, callback: (String?, String?) -> Unit) {
        onLoading(true)
        var btcPrice: String? = null
        var bifRate: String? = null

        // Charge BTC/USD
        apiService.getBtcUsdRate().enqueue(object : Callback<ConversionResponse> {
            override fun onResponse(call: Call<ConversionResponse>, response: Response<ConversionResponse>) {
                val result = response.body()?.result
                Log.d("YADIO_DEBUG", "BTC/USD response: $result")
                btcPrice = result?.toInt()?.toString()
            }
            override fun onFailure(call: Call<ConversionResponse>, t: Throwable) {
                Log.e("YADIO_DEBUG", "Erreur BTC/USD: ${t.message}")
            }
        })

        // Charge USD/BIF
        apiService.getUsdBifRate().enqueue(object : Callback<ConversionResponse> {
            override fun onResponse(call: Call<ConversionResponse>, response: Response<ConversionResponse>) {
                val result = response.body()?.result
                Log.d("YADIO_DEBUG", "USD/BIF response: $result")
                bifRate = result?.toInt()?.toString()
                onLoading(false)
                callback(btcPrice, bifRate)
            }
            override fun onFailure(call: Call<ConversionResponse>, t: Throwable) {
                Log.e("YADIO_DEBUG", "Erreur USD/BIF: ${t.message}")
                onLoading(false)
                callback(btcPrice, bifRate)  // Peu importe si l'un échoue, passe ce qui est disponible
            }
        })
    }

    // loadRates et saveRates inchangés
    private fun loadRates(context: Context, callback: (String, String) -> Unit) {
        val prefs = context.getSharedPreferences("rates", Context.MODE_PRIVATE)
        val taux = prefs.getString("taux", "7700.0") ?: "7700.0"
        val prix = prefs.getString("prix", "116500.0") ?: "116500.0"
        callback(taux, prix)
    }

    private fun saveRates(context: Context, taux: String, prix: String) {
        val prefs = context.getSharedPreferences("rates", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("taux", taux)
            putString("prix", prix)
            apply()
        }
    }

    private fun lightColorScheme(): ColorScheme {
        return lightColorScheme(
            primary = Color(0xFF4CAF50),
            onPrimary = Color.White,
            secondary = Color(0xFF81C784),
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
    isLoadingRates: Boolean,
    onMontantChange: (String) -> Unit,
    onDeviseChange: (String, String) -> Unit,
    onConvert: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val t = { key: String -> Strings.get(key, language) }
    val devises = listOf("BTC", "SATS", "USD", "BIF")
    var localMontant by remember { mutableStateOf(montant) }
    var localSource by remember { mutableStateOf(sourceDevise) }
    var localTarget by remember { mutableStateOf(targetDevise) }

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

            if (isLoadingRates) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Mise à jour des taux en cours...", Modifier.align(Alignment.CenterHorizontally))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                devises.forEach { base ->
                    var expanded by remember { mutableStateOf(false) }
                    val backgroundColor = animateColorAsState(
                        targetValue = if (localSource == base) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondaryContainer
                    ).value

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = backgroundColor,
                                contentColor = if (localSource == base) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(base)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            devises.filter { it != base }.forEach { target ->
                                DropdownMenuItem(
                                    text = { Text("→ $target") },
                                    onClick = {
                                        localSource = base
                                        localTarget = target
                                        onDeviseChange(base, target)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = localMontant,
                onValueChange = {
                    localMontant = it
                    onMontantChange(it)
                },
                label = { Text("${t("amount")} $localSource") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { onConvert(localMontant) }) {
                Text(t("convert"))
            }

            SelectionContainer {
                Text(
                    "${t("result")} : ${NumberFormat.getNumberInstance(Locale.US).format(conversionResult)} $localTarget",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        //Ajout de la citation
        Spacer(modifier = Modifier.height(20.dp))  // Petit espace pour l'esthétique

        Text(
            text = t("quote"),  // Appelle la fonction t() pour traduire dynamiquement
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,  // En italique pour ressembler à une citation
            textAlign = TextAlign.Center,  // Centré, ou gauches si tu préfères
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )


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
    val t = { key: String -> Strings.get(key, language) }

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
    val t = { key: String -> Strings.get(key, language) }

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