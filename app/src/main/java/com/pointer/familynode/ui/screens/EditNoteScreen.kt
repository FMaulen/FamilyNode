package com.pointer.familynode.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pointer.familynode.model.*
import com.pointer.familynode.ui.theme.*
import com.pointer.familynode.viewmodel.HomeViewModel
import java.io.File
import java.util.*

data class ItemFormat(
    val activeFormats: Set<TextFormat> = emptySet()
)

class ComposeFileProvider : FileProvider() {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val file = File.createTempFile("selected_image_", ".jpg", directory)
            val authority = context.packageName + ".provider"
            return getUriForFile(context, authority, file)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    noteId: String?
) {
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.groups.find { it.id == uiState.selectedGroupId }
    val note = noteId?.let { nId -> group?.notes?.find { it.id == nId } }

    var showFormatToolbar by remember { mutableStateOf(false) }
    var focusedItemId by remember { mutableStateOf<String?>(null) }
    var currentSelection by remember { mutableStateOf(TextRange.Zero) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && focusedItemId != null && note?.id != null) {
                viewModel.addImageItemAfter(note.id, focusedItemId!!, uri)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null && focusedItemId != null && note?.id != null) {
                viewModel.addImageItemAfter(note.id, focusedItemId!!, tempCameraUri!!)
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                val newUri = ComposeFileProvider.getImageUri(context)
                tempCameraUri = newUri
                cameraLauncher.launch(newUri)
            } else { /* xd */ }
        }
    )

    val focusRequesters = remember(note?.items) {
        note?.items?.associate { it.id to FocusRequester() } ?: emptyMap()
    }

    LaunchedEffect(uiState.focusRequest) {
        uiState.focusRequest?.let { request ->
            focusRequesters[request.itemId]?.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, "Mas opciones", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showFormatToolbar,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing))
            ) {
                val currentFormat = remember(focusedItemId, note, currentSelection) {
                    focusedItemId?.let { itemId ->
                        note?.items?.find { it.id == itemId }?.let { item ->
                            if (currentSelection.length > 0) {
                                val activeFormats = mutableSetOf<TextFormat>()
                                item.textSpans.forEach { span ->
                                    if (span.start < currentSelection.end && span.end > currentSelection.start) {
                                        activeFormats.add(span.format)
                                    }
                                }
                                ItemFormat(activeFormats)
                            } else {
                                ItemFormat(emptySet())
                            }
                        }
                    } ?: ItemFormat()
                }

                IOSFormatToolbar(
                    currentFormat = currentFormat,
                    hasSelection = currentSelection.length > 0,
                    onFormatSelected = { format ->
                        focusedItemId?.let { itemId ->
                            if (currentSelection.length > 0) {
                                viewModel.applyFormatToRange(
                                    note?.id ?: "",
                                    itemId,
                                    format,
                                    currentSelection.start,
                                    currentSelection.end
                                )
                            }
                        }
                    },
                    onAddCheckbox = {
                        focusedItemId?.let { currentItemId ->
                            note?.let { currentNote ->
                                viewModel.addChecklistItemAfter(currentNote.id, currentItemId)
                            }
                        }
                    },
                    onGalleryClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onCameraClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onDismiss = {
                        focusManager.clearFocus()
                        showFormatToolbar = false
                    }
                )
            }
        },
        containerColor = AppBackground,
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            HorizontalDivider(color = TextSecondary.copy(alpha = 0.2f), thickness = 1.dp)

            if (note != null && group != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            var title by remember(note.title) { mutableStateOf(note.title) }
                            BasicTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    viewModel.updateNoteTitle(note.id, it)
                                },
                                textStyle = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary),
                                cursorBrush = SolidColor(TextPrimary),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                            )
                        }

                        itemsIndexed(
                            items = note.items,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val showAuthorDot = index == 0 || note.items.getOrNull(index - 1)?.authorId != item.authorId
                            val author = group.members.find { it.id == item.authorId }

                            Row(
                                modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(top = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (showAuthorDot) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(author?.color ?: Color.Gray)
                                        )
                                    }
                                }

                                EditableItemRow(
                                    item = item,
                                    focusRequester = focusRequesters.getOrDefault(item.id, remember { FocusRequester() }),
                                    focusRequest = uiState.focusRequest,
                                    onConsumeFocusRequest = viewModel::consumeFocusRequest,
                                    onTextChange = { newText ->
                                        viewModel.updateNoteItemText(note.id, item.id, newText)
                                    },
                                    onFocusChanged = { isFocused ->
                                        if (isFocused) {
                                            focusedItemId = item.id
                                            showFormatToolbar = true
                                        }
                                    },
                                    onSelectionChanged = { selection ->
                                        currentSelection = selection
                                    },
                                    onEnterPressed = { textFieldValue ->
                                        val cursorPosition = textFieldValue.selection.start
                                        val text = textFieldValue.text
                                        val textBeforeCursor = text.substring(0, cursorPosition)
                                        val textAfterCursor = text.substring(cursorPosition)
                                        if(item.text != null) {
                                            viewModel.splitNoteItem(note.id, item.id, textBeforeCursor, textAfterCursor)
                                        }
                                    },
                                    onBackspacePressed = {
                                        if (index > 0) {
                                            val prevItem = note.items[index - 1]
                                            viewModel.mergeNoteItems(note.id, item.id, prevItem.id)
                                        }
                                    },
                                    onCheckedChange = {
                                        viewModel.toggleNoteItemChecked(note.id, item.id)
                                    }
                                )
                            }
                        }
                    }
                    if (note.items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {},
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                "Toca para empezar a escribir...",
                                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
                                modifier = Modifier.padding(start = 24.dp, top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando nota...", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun EditableItemRow(
    item: NoteItem,
    focusRequester: FocusRequester,
    focusRequest: FocusRequest?,
    onConsumeFocusRequest: () -> Unit,
    onTextChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onEnterPressed: (TextFieldValue) -> Unit,
    onBackspacePressed: () -> Unit,
    onCheckedChange: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(annotatedString = buildStyledText(item)))
    }

    LaunchedEffect(item) {
        val newStyledText = buildStyledText(item)
        if (newStyledText.toString() != textFieldValue.text || newStyledText.spanStyles != textFieldValue.annotatedString.spanStyles) {
            val selectionStart = textFieldValue.selection.start.coerceAtMost(item.text?.length ?: 0)
            val selectionEnd = textFieldValue.selection.end.coerceAtMost(item.text?.length ?: 0)
            textFieldValue = textFieldValue.copy(
                annotatedString = newStyledText,
                selection = TextRange(selectionStart, selectionEnd)
            )
        }
    }

    LaunchedEffect(isFocused) {
        onFocusChanged(isFocused)
    }

    LaunchedEffect(textFieldValue.selection) {
        onSelectionChanged(textFieldValue.selection)
    }

    LaunchedEffect(focusRequest) {
        if (focusRequest != null && focusRequest.itemId == item.id) {
            textFieldValue = textFieldValue.copy(selection = TextRange(focusRequest.cursorPosition))
            onConsumeFocusRequest()
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        when (item.type) {
            NoteItemType.IMAGE -> {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = "Imagen de la nota",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            NoteItemType.CHECKLIST -> {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onCheckedChange() }
                )
                EditableTextField(
                    item = item,
                    textFieldValue = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        if (newValue.text != item.text) onTextChange(newValue.text)
                    },
                    interactionSource = interactionSource,
                    focusRequester = focusRequester,
                    onEnterPressed = onEnterPressed,
                    onBackspacePressed = onBackspacePressed
                )
            }
            NoteItemType.TEXT -> {
                Spacer(modifier = Modifier.width(32.dp))
                EditableTextField(
                    item = item,
                    textFieldValue = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        if (newValue.text != item.text) onTextChange(newValue.text)
                    },
                    interactionSource = interactionSource,
                    focusRequester = focusRequester,
                    onEnterPressed = onEnterPressed,
                    onBackspacePressed = onBackspacePressed
                )
            }
        }
    }
}

@Composable
fun RowScope.EditableTextField(
    item: NoteItem,
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    interactionSource: MutableInteractionSource,
    focusRequester: FocusRequester,
    onEnterPressed: (TextFieldValue) -> Unit,
    onBackspacePressed: () -> Unit
) {
    Box(modifier = Modifier.weight(1f)) {
        CompositionLocalProvider(
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = AccentColor,
                backgroundColor = AccentColor.copy(alpha = 0.4f)
            )
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = if (item.isChecked) TextSecondary else TextPrimary
                ),
                cursorBrush = SolidColor(TextPrimary),
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.Enter -> {
                                    val lineStart = textFieldValue.text.lastIndexOf('\n', textFieldValue.selection.start - 1) + 1
                                    val lineEnd = textFieldValue.text.indexOf('\n', textFieldValue.selection.start).let { if (it == -1) textFieldValue.text.length else it }
                                    val currentLine = textFieldValue.text.substring(lineStart, lineEnd)
                                    if (currentLine.isEmpty()) {
                                        onEnterPressed(textFieldValue)
                                        return@onPreviewKeyEvent true
                                    }
                                    return@onPreviewKeyEvent false
                                }
                                Key.Backspace -> {
                                    if (textFieldValue.selection.start == 0 && textFieldValue.selection.end == 0) {
                                        onBackspacePressed()
                                        return@onPreviewKeyEvent true
                                    }
                                    return@onPreviewKeyEvent false
                                }
                                else -> return@onPreviewKeyEvent false
                            }
                        }
                        false
                    }
            )
        }
    }
}


@Composable
fun IOSFormatToolbar(
    currentFormat: ItemFormat,
    hasSelection: Boolean,
    onFormatSelected: (TextFormat) -> Unit,
    onAddCheckbox: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SearchBarBackground,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            HorizontalDivider(color = TextSecondary.copy(alpha = 0.2f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FormatButton(
                    text = "Titulo",
                    isSelected = TextFormat.TITLE in currentFormat.activeFormats,
                    onClick = { onFormatSelected(TextFormat.TITLE) },
                    enabled = hasSelection
                )
                FormatButton(
                    text = "Encabezado",
                    isSelected = TextFormat.HEADING in currentFormat.activeFormats,
                    onClick = { onFormatSelected(TextFormat.HEADING) },
                    enabled = hasSelection
                )
                FormatButton(
                    text = "Sub",
                    isSelected = TextFormat.SUBHEADING in currentFormat.activeFormats,
                    onClick = { onFormatSelected(TextFormat.SUBHEADING) },
                    enabled = hasSelection
                )
                VerticalDivider(
                    modifier = Modifier
                        .height(32.dp)
                        .padding(horizontal = 4.dp),
                    color = TextSecondary.copy(alpha = 0.3f)
                )

                IconButton(onClick = onGalleryClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Image, "Galeria", tint = TextPrimary)
                }
                IconButton(onClick = onCameraClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.PhotoCamera, "Camara", tint = TextPrimary)
                }

                IconButton(
                    onClick = onAddCheckbox,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.CheckBox,
                        contentDescription = "Lista",
                        tint = TextPrimary
                    )
                }
            }

            HorizontalDivider(color = TextSecondary.copy(alpha = 0.2f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconFormatButton(
                    icon = Icons.Default.FormatBold,
                    contentDescription = "Negrita",
                    isSelected = TextFormat.BOLD in currentFormat.activeFormats,
                    onClick = { onFormatSelected(TextFormat.BOLD) },
                    enabled = hasSelection
                )
                IconFormatButton(
                    icon = Icons.Default.FormatItalic,
                    contentDescription = "Cursiva",
                    isSelected = TextFormat.ITALIC in currentFormat.activeFormats,
                    onClick = { onFormatSelected(TextFormat.ITALIC) },
                    enabled = hasSelection
                )
                IconFormatButton(
                    icon = Icons.Default.FormatUnderlined,
                    contentDescription = "Subrayado",
                    isSelected = TextFormat.UNDERLINE in currentFormat.activeFormats,
                    onClick = { onFormatSelected(TextFormat.UNDERLINE) },
                    enabled = hasSelection
                )
                IconFormatButton(
                    icon = Icons.Default.FormatStrikethrough,
                    contentDescription = "Tachado",
                    isSelected = TextFormat.STRIKETHROUGH in currentFormat.activeFormats,
                    onClick = { onFormatSelected(TextFormat.STRIKETHROUGH) },
                    enabled = hasSelection
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.KeyboardHide,
                        contentDescription = "Ocultar teclado",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun FormatButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AccentColor else SearchBarBackground.copy(alpha = 0.1f),
            contentColor = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.5f),
            disabledContainerColor = SearchBarBackground.copy(alpha = 0.5f),
            disabledContentColor = TextSecondary.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun IconFormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected && enabled) AccentColor else Color.Transparent)
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.5f)
        )
    }
}

fun buildStyledText(item: NoteItem) = buildAnnotatedString {
    val text = item.text ?: return@buildAnnotatedString
    append(text)

    item.textSpans.forEach { span ->
        val spanStyle = when (span.format) {
            TextFormat.TITLE -> SpanStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            TextFormat.HEADING -> SpanStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextFormat.SUBHEADING -> SpanStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            TextFormat.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
            TextFormat.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
            TextFormat.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
            TextFormat.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            else -> SpanStyle()
        }

        val start = span.start.coerceIn(0, text.length)
        val end = span.end.coerceIn(0, text.length)

        if (start < end) {
            addStyle(spanStyle, start, end)
        }
    }

    if (item.isChecked) {
        addStyle(
            SpanStyle(
                textDecoration = TextDecoration.LineThrough,
                color = TextSecondary
            ),
            0,
            text.length
        )
    }
}