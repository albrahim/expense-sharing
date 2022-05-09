package com.example.expensesharing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.expensesharing.ui.theme.ExpenseSharingTheme
import kotlin.math.max
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensesharing.ui.theme.Teal200
import kotlin.math.min
import kotlin.math.withSign

class App {
    var spentMoneyText by mutableStateOf("")
    val spentMoney get() = spentMoneyText.toFloatOrNull() ?: 0f

    // Persons
    val persons = mutableStateListOf<Person>()
    var selectedPerson by mutableStateOf<Person?>(null)
    val personCount get() = persons.size
    fun incrementPersonCount() {
        persons.add(Person())
    }

    fun decrementPersonCount() {
        val last = persons.lastOrNull {
            it.isAnonymous
        }
        if (last != null) {
            persons.remove(last)
        }
    }

    val paidAmounts get() = persons.map { it.paidAmount }
    val individualDue get() = spentMoney / max(1, personCount)
    val totalPaid get() = paidAmounts.sum()
    val totalDue get() = if (personCount > 0) paidAmounts.map { max(0f, individualDue - it) }.sum() else spentMoney
    val totalOwed get() = paidAmounts.map { min(0f, individualDue - it).withSign(1) }.sum()

    constructor(isPreview: Boolean = false) {
        if (isPreview) {
            (1..3).forEach {
                incrementPersonCount()
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun MainView() {
        Column(
            modifier = Modifier
                .padding(vertical = 30.dp, horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PersonCountButtons()
            LazyColumn {
                items(1) {
                    MoneyAmountTextField()
                    InfoRow()
                }
                itemsIndexed(persons) { i, person ->
                    PersonCard(i, person)
                }
            }
        }
    }

    @Composable
    fun PersonCountButtons() {
        val isDecrementAllowed = persons.any { it.isAnonymous }
        Row(modifier = Modifier
            .height(75.dp)
            .padding(vertical = 10.dp)
        ) {
            Text("${personCount} persons",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight()
                    .wrapContentHeight()
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                onClick = { incrementPersonCount() }) {
                Text(text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(10.dp))
            Button(modifier =
            Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(),
                enabled = isDecrementAllowed,
                onClick = { decrementPersonCount() }) {
                Text(text = "-",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    fun MoneyAmountTextField() {
        val fm = LocalFocusManager.current

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = spentMoneyText,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { fm.clearFocus() }
            ),
            placeholder = { Text("Money Amount") },
            onValueChange = {
                if (it.toFloatOrNull() != null || it.isBlank()) spentMoneyText = it
            }
        )
    }

    @Composable
    fun InfoRow() {
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
        ) {
            InfoCard(cardName = "Individual", numericalValue = individualDue)
            InfoCard(cardName = "Paid", numericalValue = totalPaid)
            InfoCard(cardName = "Due", numericalValue = totalDue)
            InfoCard(cardName = "Owed", numericalValue = totalOwed)
        }
    }

    @Composable
    fun InfoCard(cardName: String, numericalValue: Float) {
        Card(modifier = Modifier
            .width(IntrinsicSize.Min)
            .widthIn(min = 75.dp, max = Dp.Infinity),
            border = BorderStroke(width = 1.dp, Color.Unspecified),
            backgroundColor = Color.LightGray
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(all = 4.dp)
            ) {
                Text(
                    "$cardName",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${numericalValue.round()}",
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun PersonCard(i: Int, person: Person) {
        val isExpanded = (person == selectedPerson)
        val dueAmount = max(0f, individualDue - person.paidAmount)
        val owedAmount = min(0f, individualDue - person.paidAmount).withSign(1)
        val isOwedAmountShown = owedAmount > 0
        val owedAmountColor = if (totalOwed >= 1) Color.Red else Color.Unspecified
        Card(modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .clickable {
                if (selectedPerson == person) {
                    selectedPerson = null
                } else {
                    selectedPerson = person
                }
            }, elevation = 5.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(10.dp)
            ) {
                Text(text = person.name ?: "Person ${i + 1}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                val shownAmountText = if (isOwedAmountShown) "Owed ${owedAmount.round()}" else "${dueAmount.round()}"
                val shownAmountColor = if (isOwedAmountShown) owedAmountColor else Color.Unspecified
                Text(shownAmountText,
                    color = shownAmountColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light
                )
                AnimatedVisibility(isExpanded) {
                    PersonFields(i, person)
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun PersonFields(i: Int, person: Person) {
        val fm = LocalFocusManager.current

        Column(modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 13.dp)
        ) {
            TextField(
                value = person.name ?: "",
                placeholder = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { fm.clearFocus() }
                ),
                onValueChange = {
                    val newName = it.replace('\n', ' ')
                    if (newName.isNotBlank()) {
                        person.name = newName
                        person.touched()
                    } else {
                        person.name = null
                    }
                    persons.set(i, person)
                })
            TextField(
                value = person.paidAmountString,
                placeholder = { Text("Paid Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { fm.clearFocus() }
                ),
                onValueChange = {
                    if (it.toFloatOrNull() != null || it.isBlank()) {
                        person.paidAmountString = it
                    }
                    person.touched()
                    persons.set(i, person)
                }
            )
            Button({ persons.remove(person) }, Modifier.fillMaxWidth()) {
                Text("Remove")
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun View() {
        ExpenseSharingTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                MainView()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val app = App(isPreview = true)
    app.View()
}