package com.example.expensesharing

import android.os.Parcelable
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
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.Parcelize
import java.util.*
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
    fun MoneyAmountTextField() {
        OutlinedTextField(
            value = spentMoneyText,
            placeholder = { Text("Money Amount") },
            onValueChange = {
                if (it.toFloatOrNull() != null || it.isBlank()) spentMoneyText = it
            }
        )
    }

    @Composable
    fun PersonCountButtons() {
        val isDecrementAllowed = persons.any { it.isAnonymous }
        Row() {
            Button(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth(.5f),
                onClick = { incrementPersonCount() }) {
                Text(text = "+")
            }
            Spacer(Modifier.width(10.dp))
            Button(modifier =
            Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth(.5f),
                enabled = isDecrementAllowed,
                onClick = { decrementPersonCount() }) {
                Text(text = "-")
            }
        }
    }

    @Composable
    fun InfoBox() {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text("Persons: $personCount")
            Text("Each person pays: $individualDue")
            Spacer(Modifier.height(10.dp))
            Text("Total Paid: $totalPaid")
            Text(text = "Total Due: $totalDue")
            Text(text = "Total Owed: $totalOwed")
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun PersonCard(i: Int, person: Person) {
        val isExpanded = (person == selectedPerson)
        Card(modifier = Modifier
            .padding(10.dp)
            .clickable {
                if (selectedPerson == person) {
                    selectedPerson = null
                } else {
                    selectedPerson = person
                }
            }, elevation = 5.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,modifier = Modifier.padding(10.dp)) {
                Text(text = person.name ?: "Person ${i + 1}")
                AnimatedVisibility(isExpanded) {
                    PersonTextFields(i, person)
                }
                Text("Due Amount: ${max(0f, individualDue - person.paidAmount)}")
                Text("Owed Amount: ${min(0f, individualDue - person.paidAmount).withSign(1)}")
                AnimatedVisibility(isExpanded) {
                    Button({ persons.remove(person) }) {
                        Text("Remove")
                    }
                }
            }
        }
    }

    @Composable
    fun PersonTextFields(i: Int, person: Person) {
        Column {
            TextField(
                value = person.name ?: "",
                placeholder = { Text("Name") },
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
                onValueChange = {
                    if (it.toFloatOrNull() != null || it.isBlank()) {
                        person.paidAmountString = it
                    }
                    person.touched()
                    persons.set(i, person)
                }
            )
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