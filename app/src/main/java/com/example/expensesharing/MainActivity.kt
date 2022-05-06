package com.example.expensesharing

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.expensesharing.ui.theme.ExpenseSharingTheme
import kotlin.math.max
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.math.min
import kotlin.math.withSign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Parcelize
data class Person(
    var name: String? = null,
    var uuid: String? = UUID.randomUUID().toString(),
    var paidAmountString: String = "",
    var isAnonymous: Boolean = true,
    ) : Parcelable {
    val paidAmount: Float get() {
        return paidAmountString.toFloatOrNull() ?: 0f
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {
    var spentMoneyText by remember { mutableStateOf("") }
    var spentMoney = { spentMoneyText.toFloatOrNull() ?: 0f }

    var selectedPerson by remember { mutableStateOf<Person?>(null) }

    val persons = remember {
        mutableStateListOf<Person>()
    }

    fun addPerson() {
        persons.add(Person())
    }

    fun removePerson() {
        val last = persons.lastOrNull {
            it.isAnonymous
        }
        if (last != null) {
            persons.remove(last)
        }
    }

    fun personCount() : Int {
        return persons.size
    }

    fun calculateEqualDuePay() =
        spentMoney() / max(1, personCount())

    ExpenseSharingTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row() {
                    Button(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .padding(end = 10.dp)
                            .fillMaxWidth(.5f),
                        onClick = { addPerson() }) {
                        Text(text = "+")
                    }
                    Button(modifier =
                    Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxWidth(.5f),
                        enabled = { (persons.filter { it.isAnonymous }.size - 1) >= 0 }(),
                        onClick = { removePerson() }) {
                        Text(text = "-")
                    }
                }
                OutlinedTextField(
                    value = spentMoneyText,
                    placeholder = { Text("Money Amount") },
                    onValueChange = {
                        if (it.toFloatOrNull() != null || it.isBlank()) spentMoneyText = it
                    },
                    modifier = Modifier.selectable(selected = false, enabled = true) {

                    })
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    PersonCount(personCount())
                    Text(
                        text = "Each person pays: ${calculateEqualDuePay()}",
                    )
                    val paidAmounts = persons.map { it.paidAmount }
                    val totalPaid = paidAmounts.sum()

                    val totalDue = if (personCount() > 0) {
                        paidAmounts.map { max(0f, calculateEqualDuePay() - it) }.sum()
                    } else {
                        spentMoney()
                    }

                    val totalOwed = paidAmounts.map { min(0f, calculateEqualDuePay() - it).withSign(1) }.sum()
                    Text(
                        text = "Total Paid: $totalPaid",
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = "Total Due: $totalDue",
                    )
                    Text(
                        text = "Total Owed: $totalOwed",
                    )
                }
                LazyColumn {
                    itemsIndexed(persons) { i, person ->
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
                                AnimatedVisibility(visible = (person == selectedPerson)) {
                                    Column {
                                        TextField(
                                            value = person.name ?: "",
                                            placeholder = { Text("Name") },
                                            onValueChange = {
                                                val newName = it.replace('\n', ' ')
                                                if (newName.isNotBlank()) {
                                                    person.name = newName
                                                    if (person.isAnonymous) {
                                                        person.isAnonymous = false
                                                    }
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
                                                if (person.isAnonymous) {
                                                    person.isAnonymous = false
                                                }
                                                persons.set(i, person)
                                            }
                                        )
                                    }
                                }
                                Text("Due Amount: ${max(0f, calculateEqualDuePay() - person.paidAmount)}")
                                Text("Owed Amount: ${min(0f, calculateEqualDuePay() - person.paidAmount).withSign(1)}")
                                AnimatedVisibility(visible = (person == selectedPerson)) {
                                    Button({ persons.remove(person) }) {
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PersonCount(count: Int) {
    Text(text = "Persons: ${count.toString()}")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    App()
}