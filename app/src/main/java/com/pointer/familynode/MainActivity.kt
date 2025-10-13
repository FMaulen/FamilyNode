package com.pointer.familynode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointer.familynode.ui.theme.FamilyNodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComponent()
        }
    }
}

@Composable
fun MyText(text: String) {
    Text(text)
}

@Composable
fun MyTexts() {
    Column() {
        MyText("Hola pepe")
        MyText("vamo a ra perea")
    }
}

@Composable
fun MyImage() {
    Image(
        painterResource(R.drawable.ic_launcher_foreground),
        "Test Image"
    )
}

@Composable
fun MyComponent() {
    Row() {
        MyImage()
        MyTexts()
    }
}

@Composable
fun Pantalla() {
    LazyColumn (modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)) {
        item {
            Image (
                modifier = Modifier.fillMaxWidth().height(400.dp),
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Logo android"
            )
            Text (
                text ="Titulo",
                fontSize = 32.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Row {
                Button(onClick = {}) { Text("Aceptar") }
                Button(onClick = {}) { Text("Cancelar") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewComponent() {
    Pantalla()
}