package com.example.expensesharing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.RoundingMode
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

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun MainView() {
        Column(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PersonCountButtons()
            MoneyAmountTextField()
            InfoBox()
            LazyColumn {
                itemsIndexed(persons) { i, person ->
                    PersonCard(i, person)
                }
            }
        }
    }

    @Composable
    fun PersonCountButtons() {
        val isDecrementAllowed = persons.any { it.isAnonymous }
        Row(modifier = Modifier.fillMaxWidth(0.9f)) {
            Button(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth(0.5f),
                onClick = { incrementPersonCount() }) {
                Text(text = "+")
            }
            Spacer(Modifier.width(10.dp))
            Button(modifier =
            Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth(1f),
                enabled = isDecrementAllowed,
                onClick = { decrementPersonCount() }) {
                Text(text = "-")
            }
        }
    }

    @Composable
    fun MoneyAmountTextField() {
        val fm = LocalFocusManager.current

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(0.9f),
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
    fun InfoBox() {
        val owedAmountColor = if (totalOwed >= 1) Color.Red else Color.Unspecified
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text("Persons: $personCount")
            Text("Each person pays: ${individualDue.round()}")
            Spacer(Modifier.height(10.dp))
            Text("Total Paid: $totalPaid")
            Text(text = "Total Due: ${totalDue.round()}")
            Text(text = "Total Owed: ${totalOwed.round()}",
                color = owedAmountColor
            )
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
            .padding(10.dp)
            .fillMaxWidth(0.9f)
            .clickable {
                if (selectedPerson == person) {
                    selectedPerson = null
                } else {
                    selectedPerson = person
                }
            }, elevation = 5.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,modifier = Modifier.padding(10.dp)) {
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

        Column(modifier = Modifier.fillMaxWidth(0.9f)
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