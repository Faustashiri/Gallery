package com.example.galleryapp

import com.example.galleryapp.ui.theme.GalleryTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GalleryTheme {  // вся тема подключена здесь
                GalleryApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryApp() {
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchText by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Загружаем примеры при старте
    LaunchedEffect(Unit) {
        PictureRepository.generateSamplePictures()
    }

    val gallery = PictureRepository.getAll()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
            ) {
                // Поиск
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Поиск по автору...") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Панель управления
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = "Переключить вид",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Очистить всю галерею?",
                                    actionLabel = "Да",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    PictureRepository.clearAll()
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Очистить всё")
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить картинку")
            }
        }
    ) { padding ->

        val filteredGallery = remember(searchText, gallery.size) {
            gallery.filter { it.author.contains(searchText, ignoreCase = true) }
        }

        Crossfade(targetState = isGridView, modifier = Modifier.padding(padding)) { grid ->
            if (grid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(filteredGallery) { picture ->
                        PictureItem(
                            picture = picture,
                            onPictureClick = { PictureRepository.removePicture(picture) }
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    items(filteredGallery) { picture ->
                        PictureItem(
                            picture = picture,
                            onPictureClick = { PictureRepository.removePicture(picture) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddPictureDialog(
                onAdd = { author, url ->
                    when (PictureRepository.addPicture(author, url)) {
                        PictureRepository.AddResult.Success -> {
                            scope.launch { snackbarHostState.showSnackbar("✅ Добавлено!") }
                            showAddDialog = false
                        }
                        PictureRepository.AddResult.Duplicate -> {
                            scope.launch { snackbarHostState.showSnackbar("❗ Такая картинка уже есть") }
                        }
                        PictureRepository.AddResult.EmptyFields -> {
                            scope.launch { snackbarHostState.showSnackbar("⚠️ Заполни поля") }
                        }
                    }
                },
                onCancel = { showAddDialog = false }
            )
        }
    }
}

@Composable
fun PictureItem(picture: Picture, onPictureClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onPictureClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            AsyncImage(
                model = picture.url,
                contentDescription = "Картинка от ${picture.author}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "📸 Автор: ${picture.author}",
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPictureDialog(onAdd: (String, String) -> Unit, onCancel: () -> Unit) {
    var author by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Добавить картинку") },
        text = {
            Column {
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Автор") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL картинки") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(author, url) }) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Отмена")
            }
        }
    )
}
