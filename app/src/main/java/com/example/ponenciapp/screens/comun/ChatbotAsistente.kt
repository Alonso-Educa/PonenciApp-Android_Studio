package com.example.ponenciapp.screens.comun

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.room.Room
import com.example.ponenciapp.R
import com.example.ponenciapp.data.Estructura
import com.example.ponenciapp.data.bbdd.AppDB
import com.example.ponenciapp.data.bbdd.entities.ParticipanteData
import com.example.ponenciapp.chatbot.ChatViewModel
import com.example.ponenciapp.chatbot.Mensaje
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotAsistente(navController: NavController) {
    // Variables usadas
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    val uid = Firebase.auth.currentUser?.uid ?: ""

    // base de datos de room
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext, AppDB::class.java, Estructura.DB.NAME
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    // Daos de room
    val mensajeDao = db.mensajeDao()
    val participanteDao = db.participanteDao()

    // Datos del participante
    var participante by remember { mutableStateOf<ParticipanteData?>(null) }

    // Variables de control
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var showDialog by remember { mutableStateOf(false) }
    val viewModel: ChatViewModel = viewModel(
        LocalContext.current as androidx.activity.ComponentActivity
    )


    LaunchedEffect(Unit) {
        // Carga el participante desde Room
        participante = participanteDao.getParticipantePorId(uid)
        isLoading = false
        // Se sabe que el usuario está en la ventana
        viewModel.usuarioEnChat = true
    }

    // Al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            // El usuario ya no esta en la ventana
            viewModel.usuarioEnChat = false
        }
    }

    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }, topBar = {
        TopAppBar(
            title = {
                Column {
                    Text("PonenciApp", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Asistente virtual",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White
            ), navigationIcon = {
                // Botón para volver atrás
                IconButton(
                    onClick = {
                        navController.navigate("Ajustes") {
                            popUpTo("ChatbotAsistente") { inclusive = true }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        // muestra el diálogo para borrar el historial de mensajes
                        showDialog = true
                    }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Borrar la conversación",
                        tint = Color.White
                    )
                }
                // Icono circular con la inicial del usuario
                // Al pulsar muestra un dialog con los datos del participante
                participante?.let { IconoUsuario(participante = it) }
            }
        )
    }) { innerPadding ->

        // Si está cargando los datos muestra un iconito de carga
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // PantallaChat recibe el viewModel, estado de lista y snackbarHostState
            PantallaChat(
                viewModel = viewModel, listState = listState
            )
        }
    }
    if (showDialog) {
        DialogEliminarChat(onDismiss = { showDialog = false }, onConfirm = {
            viewModel.borrarMensajes()
            Toast.makeText(
                context, "Historial de mensajes eliminado", Toast.LENGTH_SHORT
            ).show()
            showDialog = false
        })
    }
}


// Clase para la pantalla del chat
@Composable
fun PantallaChat(
    viewModel: ChatViewModel, listState: LazyListState
) {
    var texto by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val animatedColor by animateColorAsState(
        targetValue = if (texto.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = ""
    )
    val scale by animateFloatAsState(
        targetValue = if (texto.isNotBlank()) 1f else 0.9f, animationSpec = tween(200), label = ""
    )
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp), state = listState
        ) {
            // Lista de mensajes
            itemsIndexed(
                viewModel.mensajes,
                key = { _, mensaje -> mensaje.fecha }) { index, mensaje ->
                AnimatedVisibility(
                    visible = true, enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                        initialOffsetY = { it / 2 }, animationSpec = tween(300)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        MensajeChat(
                            mensaje = mensaje, onCopy = { textToCopy ->

                                clipboardManager.setText(AnnotatedString(textToCopy))
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Mensaje copiado al portapapeles",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            })

                        // Recoge la ultima posicion del ultimo mensaje
                        val esUltimo = index == viewModel.mensajes.lastIndex
                        // Se muestran sugerencias de respuesta al mensaje del bot
                        if (mensaje.rol == "assistant" && !viewModel.escribiendo && esUltimo) {
                            Row(
                                modifier = Modifier.padding(start = 46.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AssistChip(
                                    onClick = {
                                        viewModel.enviarMensaje("Explícame más acerca del tema.")
                                    },
                                    enabled = !viewModel.escribiendo,
                                    label = { Text("Explicar", color = Color.Black) },
                                    colors = AssistChipDefaults.assistChipColors()
                                )
                                AssistChip(
                                    onClick = {
                                        viewModel.enviarMensaje("¿Podrías resumirlo?")
                                    },
                                    enabled = !viewModel.escribiendo,
                                    label = { Text("Resumir", color = Color.Black) },
                                    colors = AssistChipDefaults.assistChipColors()
                                )
                                AssistChip(
                                    onClick = {
                                        viewModel.enviarMensaje("Dame un ejemplo.")
                                    },
                                    enabled = !viewModel.escribiendo,
                                    label = { Text("Ejemplo", color = Color.Black) },
                                    colors = AssistChipDefaults.assistChipColors()
                                )
                            }
                        }
                    }
                }
            }

            if (viewModel.pensando) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                            initialOffsetY = { it / 2 }, animationSpec = tween(300)
                        )
                    ) {
                        IndicadorEscritura()
                    }
                }
            }
        }

        // Scroll automático al enviar un mensaje
        LaunchedEffect(viewModel.mensajes.size) {
            listState.animateScrollToItem(viewModel.mensajes.size)
        }

        // Se muestra el snackbar al querer enviar un mensaje cuando el bot está todavía hablando
        if (showSnackbar) {
            Snackbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(color = Color.White), action = {}) {
                Text("Espera a que el chatbot termine de responder")
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            val estaAbajo by remember {
                derivedStateOf {
                    val ultimoVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    ultimoVisible == viewModel.mensajes.lastIndex
                }
            }

            // Botón de acción flotante arriba del row
            // usar androidx.compose.animation.AnimatedVisibility porque lo confunde con el de columnas
            androidx.compose.animation.AnimatedVisibility(
                visible = !estaAbajo,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
                    .offset(y = if (showSnackbar) (-122).dp else (-64).dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch { listState.animateScrollToItem(viewModel.mensajes.size) }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.background(Color.Transparent)
                ) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "Bajar al final",
                        tint = Color.White
                    )
                }
            }

            // Row con TextField + Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Campo de texto para introducir mensajes
                TextField(
                    value = texto,
                    onValueChange = { texto = it },
                    placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(36.dp))
                        .background(Color.Transparent),
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (texto.isNotBlank() && !viewModel.escribiendo) {
                                viewModel.enviarMensaje(texto)
                                texto = ""
                            }
                        })
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Botón de enviar
                Button(
                    onClick = {
                        if (viewModel.escribiendo) {
                            scope.launch {
                                showSnackbar = true
                                delay(1500) // duración del mensaje
                                showSnackbar = false
                            }
                        } else {
                            if (texto.isNotBlank()) {
                                viewModel.enviarMensaje(texto)
                                texto = ""
                            }
                        }
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = animatedColor
                    ), enabled = texto.isNotBlank(), modifier = Modifier.scale(scale)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar", color = Color.White)
                }
            }
        }
    }
}

// Clase para los mensajes del chat
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MensajeChat(mensaje: Mensaje, onCopy: (String) -> Unit) {

    // Boolean que comprueba si el autor del mensaje es el usuario o no
    val isUser = mensaje.rol == "user"
    val clipboardManager = LocalClipboardManager.current
    val hora = remember(mensaje.fecha) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(mensaje.fecha))
    }

//    onLongClick = {
//        clipboardManager.setText(AnnotatedString(mensaje.content))
//    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        // distinta orientación para cada parte
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Icono del bot
        if (!isUser) {
            Image(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape),
                painter = painterResource(R.drawable.bot),
                contentDescription = "Imagen del bot",
                contentScale = ContentScale.Crop
            )
        }
        // Icono de copiar del usuario abajo a la derecha
        if (isUser) {
            IconButton(
                onClick = { onCopy(mensaje.contenido) },
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .size(20.dp)
                    .align(Alignment.Bottom)
                    .clip(RectangleShape)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copiar mensaje",
                    tint = Color.Black
                )
            }
        }
        // Mensaje
        Box(
            modifier = Modifier
                .padding(4.dp)
                .combinedClickable(onClick = {}, onLongClick = {
                    onCopy(mensaje.contenido)
                })
                .background(
                    //Color(0xFF1976D2) 0xFF4CAF50 0xFFA5D6A7
                    // distinto color de mensaje para cada parte
                    if (isUser) Color(0xFF90CAF9) else Color.White, RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
                .widthIn(max = 250.dp)
                .animateContentSize()
        ) {
            Text(
                text = mensaje.contenido, color = if (isUser) Color.White else Color.Black
            )
        }

        // Icono de copiar abajo a la derecha
        if (!isUser) {
            IconButton(
                onClick = { onCopy(mensaje.contenido) },
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .size(20.dp)
                    .align(Alignment.Bottom)
                    .clip(RectangleShape)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copiar mensaje",
                    tint = Color.Black
                )
            }
        }
        // Pendiente meter la foto del usuario TODO()
        if (isUser) {
            Image(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(40.dp)
                    .align(Alignment.Top)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                painter = painterResource(R.drawable.user),
                contentDescription = "Imagen del usuario",
                contentScale = ContentScale.Crop
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        // distinta orientación para cada parte
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Muestra la hora del mensaje
        Text(
            text = hora,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}

@Composable
fun IndicadorEscritura() {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
    ) {
        Image(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Black, CircleShape),
            painter = painterResource(R.drawable.bot),
            contentDescription = "Imagen del bot",
            //contentScale = ContentScale.FillBounds
        )
        Box(
            modifier = Modifier
                .padding(4.dp)
                .background(
                    Color.White, RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = "Pensando...", color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogEliminarChat(
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Info, contentDescription = "Aviso")
        },
        title = { Text("¿Desea proceder con la eliminación del historial del chat?") },
        text = { Text("Esta acción es irreversible y no se podrá deshacer. Se perderán todos los mensajes") },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        })
}