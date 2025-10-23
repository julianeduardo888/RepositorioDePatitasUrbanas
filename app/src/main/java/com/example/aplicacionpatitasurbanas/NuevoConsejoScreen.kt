package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoConsejoScreen(
    onPublicarSuccess: () -> Unit,
    onRegresar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoMascota by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val categorias = listOf(stringResource(R.string.cat_alimentacion),
        stringResource(R.string.cat_salud),
        stringResource(R.string.cat_comportamiento),
        stringResource(R.string.cat_higiene),
        stringResource(R.string.cat_curiosidades))

    var categoriaSeleccionada by remember { mutableStateOf(categorias[0]) }
    var isExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val formIsValid = titulo.isNotBlank() && descripcion.isNotBlank() && tipoMascota.isNotBlank()

    fun handlePublicar() {
        if (!formIsValid) {
            Toast.makeText(context, context.getString(R.string.campos_obligatorios), Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser

        val nuevoConsejo = Consejo(
            titulo = titulo,
            alias = alias.ifBlank { context.getString(R.string.alias_anonimo) },
            categoria = categoriaSeleccionada,
            descripcion = descripcion,
            tipoMascota = tipoMascota,
            autorId = currentUser?.uid
        )

        db.collection("consejos")
            .add(nuevoConsejo)
            .addOnSuccessListener {
                isLoading = false
                Toast.makeText(context, context.getString(R.string.consejo_publicado), Toast.LENGTH_SHORT).show()
                onPublicarSuccess()
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(context, context.getString(R.string.error_publicar, e.message), Toast.LENGTH_LONG).show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
            .padding(24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ellipse_1),
            contentDescription = "Logo",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            Text(
                text = stringResource(id = R.string.nuevo_consejo),
                style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
                color = Color(0xFF2E2E2E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.Start
            ) {
                // ... (Los TextField se quedan igual que antes)
                Text(stringResource(id = R.string.titulo_del_consejo), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_titulo_consejo)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Text(stringResource(id = R.string.alias), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = alias,
                    onValueChange = { alias = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_alias_consejo)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Text(stringResource(id = R.string.categoria), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it }
                ) {
                    TextField(
                        value = categoriaSeleccionada,
                        shape = RoundedCornerShape(12.dp),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        colors = getTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        shape = RoundedCornerShape(16.dp),
                        onDismissRequest = { isExpanded = false }
                    ) {
                        categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria) },
                                onClick = {
                                    categoriaSeleccionada = categoria
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(stringResource(id = R.string.descripcion), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_descripcion_consejo)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(stringResource(id = R.string.tipo_mascota), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = tipoMascota,
                    onValueChange = { tipoMascota = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_tipo_mascota)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

            }

            Spacer(Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { handlePublicar() },
                    enabled = formIsValid,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B195))
                ) {
                    Text(stringResource(id = R.string.publicar_flecha), fontSize = 18.sp, color = Color(0xFF2E2E2E))
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onRegresar,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0B4BE))
            ) {
                Text(stringResource(id = R.string.regresar), fontSize = 18.sp, color = Color(0xFF2E2E2E))
            }
        }
    }
}