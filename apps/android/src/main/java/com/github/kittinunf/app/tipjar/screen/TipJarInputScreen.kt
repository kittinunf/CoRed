package com.github.kittinunf.app.tipjar.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.kittinunf.app.tipjar.util.getInstance
import com.github.kittinunf.tipjar.api.input.InputUiState
import com.github.kittinunf.tipjar.api.input.TipJarInputViewModel
import java.text.DecimalFormat

@Composable
fun TipJarInputScreen(onSave: (state: InputUiState) -> Unit) {
    val vm = remember { getInstance<TipJarInputViewModel>() }
    val states by vm.states.collectAsState(rememberCoroutineScope().coroutineContext)

    // initial set
    LaunchedEffect(Unit) { vm.setInitialTip() }

    InputLayout(state = states,
        isSaveEnabled = vm.isCurrentStateValid(),
        onUpdateAmount = {
            vm.updateAmount(it)
        },
        onUpdatePeopleCount = {
            vm.updatePeopleCount(it)
        },
        onUpdateTipPercentage = {
            vm.updateTipPercentage(it)
        },
        onUpdatePhotoEnabled = {
            vm.updatePhotoEnabled(it)
        },
        onSaveTip = {
            if (vm.currentState.isPhotoEnabled.not()) {
                vm.saveTip()
            }
            onSave(vm.currentState)
        }
    )
}

@Composable
fun InputLayout(state: InputUiState, isSaveEnabled: Boolean, onUpdateAmount: (Float) -> Unit, onUpdatePeopleCount: (Int) -> Unit, onUpdateTipPercentage: (Int) -> Unit, onUpdatePhotoEnabled: (Boolean) -> Unit, onSaveTip: () -> Unit) {
    val focusManager = LocalFocusManager.current
    Column(modifier = Modifier
        .clickable(indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            focusManager.clearFocus()
        }
        .padding(16.dp)
        .fillMaxWidth()
    ) {
        AmountComponent(state.amount, onValueChange = {
            onUpdateAmount(it)
        })
        Spacer(modifier = Modifier.size(16.dp))
        PeopleCountComponent(state.peopleCount, onValueChange = {
            onUpdatePeopleCount(it)
        })
        Spacer(modifier = Modifier.size(16.dp))
        TipPercentageComponent(state.tipPercentage, onValueChange = {
            onUpdateTipPercentage(it)
        })
        Spacer(modifier = Modifier.size(16.dp))
        TipInfoComponent(state.totalTipAmount, state.tipPerPerson)
        Spacer(modifier = Modifier.size(8.dp))
        PhotoEnabledComponent(state.isPhotoEnabled, onCheckedChange = {
            onUpdatePhotoEnabled(it)
        })
        Spacer(modifier = Modifier.size(8.dp))
        SaveTipComponent(isEnabled = isSaveEnabled,
            onClick = {
                onSaveTip()
            }
        )
    }
}

@Composable
@Preview
fun AmountComponent(amount: Float, onValueChange: (Float) -> Unit = {}) {
    InputTextField(text = amount.toString(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = "Enter amount",
        leadingIcon = { Text("$") },
        transform = { it },
        onValueChange = {
            val value = it.toFloatOrNull() ?: 0f
            if (value < 0) {
                onValueChange(0f)
            } else {
                onValueChange(value)
            }
        }
    )
}

@Composable
@Preview
fun PeopleCountComponent(peopleCount: Int, onValueChange: (Int) -> Unit = {}) {
    var _peopleCount by rememberSaveable { mutableStateOf(peopleCount) }
    _peopleCount = peopleCount

    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "How many people?")
        Spacer(modifier = Modifier.size(4.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(modifier = Modifier.requiredSize(56.dp),
                onClick = {
                    if (_peopleCount == 100) return@OutlinedButton

                    onValueChange(++_peopleCount)
                }, shape = CircleShape) {
                Text(text = "+")
            }
            Text(text = _peopleCount.toString(), textAlign = TextAlign.Center)
            OutlinedButton(modifier = Modifier.requiredSize(56.dp),
                onClick = {
                    if (peopleCount == 1) return@OutlinedButton

                    onValueChange(--_peopleCount)
                }, shape = CircleShape) {
                Text(text = "-")
            }
        }
    }
}

@Composable
@Preview
fun TipPercentageComponent(percentage: Int, onValueChange: (Int) -> Unit = {}) {
    InputTextField(text = percentage.toString(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        label = "% Tip",
        trailingIcon = { Text("%") },
        transform = {
            // if tip is less than 0 -> 0, bigger than 100 -> 100
            val value = it.toIntOrNull()
            val transformValue = value?.coerceIn(0, 100) ?: ""
            transformValue.toString()
        },
        onValueChange = {
            val value = it.toIntOrNull()
            onValueChange(value ?: 0)
        }
    )
}

@Composable
@Preview
private fun InputTextField(text: String, label: String, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, leadingIcon: @Composable (() -> Unit)? = null, trailingIcon: @Composable (() -> Unit)? = null, transform: (String) -> String = { it }, onValueChange: (String) -> Unit = {}) {
    var _text by rememberSaveable { mutableStateOf(text) }

    OutlinedTextField(modifier = Modifier.fillMaxWidth(),
        value = _text,
        onValueChange = {
            _text = transform(it)
            onValueChange(_text)
        },
        singleLine = true,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions
    )
}

@Composable
@Preview
fun TipInfoComponent(totalTipAmount: Float, tipPerPerson: Float) {
    val formatter = DecimalFormat("###,###.00")

    Column(modifier = Modifier.padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Total Tip")
            Text(text = "$ ${formatter.format(totalTipAmount)}")
        }
        Spacer(modifier = Modifier.size(8.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Per Person")
            Text(text = "$ ${formatter.format(tipPerPerson)}")
        }
    }
}

@Composable
@Preview
fun PhotoEnabledComponent(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit = {}) {
    var _isChecked by rememberSaveable { mutableStateOf(isChecked) }

    val focusManager = LocalFocusManager.current

    Row(modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = _isChecked,
            onCheckedChange = {
                focusManager.clearFocus()

                _isChecked = it
                onCheckedChange(_isChecked)
            })
        Spacer(modifier = Modifier.size(8.dp))
        Text(modifier = Modifier.clickable(indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            focusManager.clearFocus()

            _isChecked = _isChecked.not()
            onCheckedChange(_isChecked)
        }, text = "Take photo of receipt")
    }
}

@Composable
@Preview
fun SaveTipComponent(isEnabled: Boolean, onClick: () -> Unit = {}) {
    Button(modifier = Modifier
        .padding(8.dp)
        .sizeIn(minHeight = 48.dp)
        .fillMaxWidth(),
        enabled = isEnabled,
        onClick = { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(text = "Save Payment")
    }
}


