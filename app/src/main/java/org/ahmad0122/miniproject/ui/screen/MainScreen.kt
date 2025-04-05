package org.ahmad0122.miniproject.ui.screen

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.ahmad0122.miniproject.R
import org.ahmad0122.miniproject.navigation.Screen
import org.ahmad0122.miniproject.ui.theme.MiniProjectTheme
import java.text.NumberFormat
import java.util.Locale


private fun formatCurrency(value: Float, currency: String): String {
    val formatter = when (currency) {
        "IDR" -> NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        "USD" -> NumberFormat.getCurrencyInstance(Locale.US)
        else -> NumberFormat.getCurrencyInstance()
    }
    return formatter.format(value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {

    var showMenu by rememberSaveable { mutableStateOf(false) }
    var currentResult by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        if (currentResult.isNotEmpty()) {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject))
                                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, currentResult))
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.share),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_rate)) },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "market://details?id=${context.packageName}".toUri()
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                                    }
                                    context.startActivity(webIntent)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_about)) },
                            onClick = {
                                showMenu = false
                                navController.navigate(Screen.About.route)
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContent(
    onResultChanged: (String) -> Unit = {}
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var amountError by rememberSaveable { mutableStateOf(false) }
    var fromCurrency by rememberSaveable { mutableStateOf("IDR") }
    var toCurrency by rememberSaveable { mutableStateOf("USD") }
    var result by rememberSaveable { mutableFloatStateOf(0f) }
    var isExpandedFrom by rememberSaveable { mutableStateOf(false) }
    var isExpandedTo by rememberSaveable { mutableStateOf(false) }

    val currencies = listOf("IDR", "USD")
    val exchangeRate = 16555f // 1 USD = 16.555 IDR

    fun convertCurrency() {
        if (amount.isEmpty()) {
            amountError = true
            result = 0f
            return
        }

        val amountValue = amount.toFloatOrNull()
        if (amountValue == null || amountValue <= 0) {
            amountError = true
            result = 0f
            return
        }

        result = when {
            fromCurrency == "IDR" && toCurrency == "USD" -> amountValue / exchangeRate
            fromCurrency == "USD" && toCurrency == "IDR" -> amountValue * exchangeRate
            else -> amountValue
        }
        amountError = false

        // Update result text for sharing
        val formattedAmount = formatCurrency(amountValue, fromCurrency)
        val formattedResult = formatCurrency(result, toCurrency)
        onResultChanged("$formattedAmount = $formattedResult")
    }

    fun resetValues() {
        amount = ""
        amountError = false
        result = 0f
        onResultChanged("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_bio),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        if (it.isEmpty()) {
                            result = 0f
                        }
                    },
                    label = { Text(text = stringResource(R.string.amount)) },
                    supportingText = { ErrorHint(amountError) },
                    isError = amountError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = isExpandedFrom,
                        onExpandedChange = { isExpandedFrom = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = fromCurrency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.from_currency)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpandedFrom) },
                            modifier = Modifier
                                .menuAnchor()
                                .clip(RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isExpandedFrom,
                            onDismissRequest = { isExpandedFrom = false }
                        ) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(
                                                    id = when (currency) {
                                                        "IDR" -> R.drawable.flag_idr
                                                        "USD" -> R.drawable.flag_usd
                                                        else -> R.drawable.flag_idr
                                                    }
                                                ),
                                                contentDescription = currency,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(currency)
                                        }
                                    },
                                    onClick = {
                                        fromCurrency = currency
                                        isExpandedFrom = false
                                    }
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Convert to",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    ExposedDropdownMenuBox(
                        expanded = isExpandedTo,
                        onExpandedChange = { isExpandedTo = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = toCurrency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.to_currency)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpandedTo) },
                            modifier = Modifier
                                .menuAnchor()
                                .clip(RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isExpandedTo,
                            onDismissRequest = { isExpandedTo = false }
                        ) {
                            currencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(
                                                    id = when (currency) {
                                                        "IDR" -> R.drawable.flag_idr
                                                        "USD" -> R.drawable.flag_usd
                                                        else -> R.drawable.flag_idr
                                                    }
                                                ),
                                                contentDescription = currency,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(currency)
                                        }
                                    },
                                    onClick = {
                                        toCurrency = currency
                                        isExpandedTo = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { convertCurrency() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.convert))
            }

            Button(
                onClick = { resetValues() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.reset))
            }
        }

        if (result > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val amountValue = amount.toFloatOrNull()
                    if (amountValue != null && amountValue > 0) {
                        Text(
                            text = "${formatCurrency(amountValue, fromCurrency)} = ${formatCurrency(result, toCurrency)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(R.string.exchange_rate, formatCurrency(exchangeRate, "IDR")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorHint(error: Boolean) {
    if (error) {
        Text(
            text = stringResource(R.string.input_invalid),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    MiniProjectTheme {
        MainScreen(rememberNavController())
    }
}