package com.github.kittinunf.app.tipjar.screen

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.kittinunf.app.tipjar.typography
import com.github.kittinunf.app.tipjar.util.getInstance
import com.github.kittinunf.tipjar.api.list.ListUiItemState
import com.github.kittinunf.tipjar.api.list.TipJarListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class DialogState {
    object NotShown : DialogState()
    class Show(val value: ListUiItemState) : DialogState()
}

@Composable
fun TipJarListScreen() {
    val vm = remember { getInstance<TipJarListViewModel>() }
    val states by vm.states.collectAsState(rememberCoroutineScope().coroutineContext)

    var dialogState by remember { mutableStateOf<DialogState>(DialogState.NotShown) }

    LaunchedEffect(Unit) { vm.loadTips() }

    ListComponent(states = states.list,
        onClick = { _, state ->
            dialogState = DialogState.Show(state)
        })

    when (val state = dialogState) {
        is DialogState.Show -> {
            if (state.value.image == null) {
                val context = LocalContext.current
                Toast.makeText(context, "We don't have image for that tip record.", Toast.LENGTH_SHORT).show()
                dialogState = DialogState.NotShown
            } else {
                DialogComponent(state = state.value) {
                    dialogState = DialogState.NotShown
                }
            }
        }
        else -> {
        }
    }
}

@Composable
@Preview
fun ListComponent(states: List<ListUiItemState>, onClick: (Int, ListUiItemState) -> Unit = { _, _ -> }) {
    LazyColumn {
        itemsIndexed(items = states,
            key = { _, state -> state.id }
        ) { index, rowState ->
            RowComponent(rowState, onClick = { onClick(index, rowState) })
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun RowComponent(state: ListUiItemState, onClick: () -> Unit) {
    var _bitmap by remember(key1 = state.id) { mutableStateOf<ImageBitmap?>(null) }

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = { onClick() })
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = state.timestamp, style = typography.overline)
            Text(text = "$${state.amount}", style = typography.h6)
            Text(text = "Tip: $${state.tipAmount}", style = typography.subtitle1.copy(Color.DarkGray))
        }
        if (state.image != null) {
            Spacer(modifier = Modifier.fillMaxWidth(0.6f))

            LaunchedEffect(state.id) {
                _bitmap = loadImage(state.image!!)
            }

            val loadedBitmap = _bitmap
            if (loadedBitmap == null) {
                Image(modifier = Modifier.requiredSize(80.dp),
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null
                )
            } else {
                Image(modifier = Modifier.requiredSize(80.dp),
                    bitmap = loadedBitmap,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun DialogComponent(state: ListUiItemState, onDismiss: () -> Unit) {
    var _bitmap by remember(key1 = state.id) { mutableStateOf<ImageBitmap?>(null) }

    Dialog(onDismissRequest = { onDismiss() }) {
        LaunchedEffect(state.id) {
            _bitmap = loadImage(state.image!!)
        }

        val modifier = Modifier.requiredSize(208.dp, 416.dp)
        val loadedBitmap = _bitmap
        if (loadedBitmap == null) {
            Image(modifier = modifier,
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null
            )
        } else {
            Image(modifier = modifier,
                bitmap = loadedBitmap,
                contentDescription = null
            )
        }
    }
}

private suspend fun loadImage(path: String) = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(path).asImageBitmap() }
