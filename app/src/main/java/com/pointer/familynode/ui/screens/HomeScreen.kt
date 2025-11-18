package com.pointer.familynode.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Http
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pointer.familynode.model.Group
import com.pointer.familynode.model.Note
import com.pointer.familynode.model.NoteItemType
import com.pointer.familynode.ui.theme.*
import com.pointer.familynode.viewmodel.AppUiState
import com.pointer.familynode.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val windowSizeClass = calculateWindowSizeClass(activity = LocalContext.current as android.app.Activity)
    val uiState by viewModel.uiState.collectAsState()

    val onAddNewNote = {
        val newNoteId = viewModel.createNewNote("Nueva Nota")
        if (newNoteId.isNotBlank()) navController.navigate("note/$newNoteId")
    }
    val onNoteClicked = { noteId: String -> navController.navigate("note/$noteId") }
    val onPostsClicked = { navController.navigate("posts") }

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            CompactHomeScreen(
                uiState = uiState,
                onGroupSelected = viewModel::selectGroup,
                onAddNewNote = onAddNewNote,
                onNoteClicked = onNoteClicked,
                onPostsClicked = onPostsClicked
            )
        }
        else -> {
            ExpandedHomeScreen(
                uiState = uiState,
                onGroupSelected = viewModel::selectGroup,
                onAddNewNote = onAddNewNote,
                onNoteClicked = onNoteClicked,
                onPostsClicked = onPostsClicked
            )
        }
    }
}

@Composable
fun ExpandedHomeScreen(
    uiState: AppUiState,
    onGroupSelected: (String) -> Unit,
    onAddNewNote: () -> Unit,
    onNoteClicked: (String) -> Unit,
    onPostsClicked: () -> Unit
) {
    val selectedGroup = uiState.groups.find { it.id == uiState.selectedGroupId }

    Scaffold(containerColor = AppBackground) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SideNavigationPanel(
                modifier = Modifier.width(150.dp),
                groups = uiState.groups,
                selectedGroupId = uiState.selectedGroupId,
                onGroupSelected = onGroupSelected,
                isVisible = true
            )
            NotesContent(
                modifier = Modifier.weight(1f),
                group = selectedGroup,
                showMenuButton = false,
                onMenuClick = { },
                onAddNoteClicked = onAddNewNote,
                onNoteClicked = onNoteClicked,
                onPostsClicked = onPostsClicked
            )
        }
    }
}

@Composable
fun CompactHomeScreen(
    uiState: AppUiState,
    onGroupSelected: (String) -> Unit,
    onAddNewNote: () -> Unit,
    onNoteClicked: (String) -> Unit,
    onPostsClicked: () -> Unit
) {
    var isSidePanelVisible by remember { mutableStateOf(false) }
    val selectedGroup = uiState.groups.find { it.id == uiState.selectedGroupId }

    val panelWidth by animateDpAsState(
        targetValue = if (isSidePanelVisible) 150.dp else 0.dp,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "panelWidth"
    )
    val panelAlpha by animateFloatAsState(
        targetValue = if (isSidePanelVisible) 1f else 0f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "panelAlpha"
    )

    Scaffold(containerColor = AppBackground) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .width(panelWidth)
                    .fillMaxHeight()
                    .background(AppBackground)
            ) {
                if (isSidePanelVisible) {
                    SideNavigationPanel(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = panelAlpha },
                        groups = uiState.groups,
                        selectedGroupId = uiState.selectedGroupId,
                        onGroupSelected = onGroupSelected,
                        isVisible = panelAlpha > 0.01f
                    )
                }
            }
            NotesContent(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize(spring(dampingRatio = 0.75f, stiffness = 400f)),
                group = selectedGroup,
                showMenuButton = true,
                onMenuClick = { isSidePanelVisible = !isSidePanelVisible },
                onAddNoteClicked = onAddNewNote,
                onNoteClicked = onNoteClicked,
                onPostsClicked = onPostsClicked
            )
        }
    }
}

@Composable
fun SideNavigationPanel(
    modifier: Modifier = Modifier,
    groups: List<Group>,
    selectedGroupId: String?,
    onGroupSelected: (String) -> Unit,
    isVisible: Boolean = true
) {
    val groupColors = mapOf(
        "Familia" to GroupPurple, "Amigos" to GroupPink, "Trabajo" to GroupYellow
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groups.forEachIndexed { index, group ->
            val isSelected = selectedGroupId == group.id

            val buttonAlpha by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 200,
                    delayMillis = index * 50
                ),
                label = "buttonAlpha_$index"
            )

            Box(modifier = Modifier.graphicsLayer { alpha = buttonAlpha }) {
                GroupButton(
                    group = group,
                    isSelected = isSelected,
                    color = groupColors[group.name] ?: Color.LightGray,
                    onClick = { onGroupSelected(group.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val foldersAlpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = groups.size * 50
            ),
            label = "foldersAlpha"
        )

        Column(modifier = Modifier.graphicsLayer { alpha = foldersAlpha }) {
            Text(
                "Folders",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            GroupButton(
                group = Group(name = "Ideas", icon = Icons.Default.Folder, notes = emptyList()),
                isSelected = false,
                color = GroupBlue,
                onClick = { /* TODO */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupButton(group: Group, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) color else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )

    val fontWeight by animateFloatAsState(
        targetValue = if (isSelected) 700f else 400f,
        animationSpec = tween(durationMillis = 200),
        label = "fontWeight"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = group.icon,
                contentDescription = group.name,
                tint = if (isSelected) TextPrimary else TextSecondary
            )
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight(fontWeight.toInt())
                ),
                color = if (isSelected) TextPrimary else TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesContent(
    modifier: Modifier = Modifier,
    group: Group?,
    showMenuButton: Boolean,
    onMenuClick: () -> Unit,
    onAddNoteClicked: () -> Unit,
    onNoteClicked: (String) -> Unit,
    onPostsClicked: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showMenuButton) {
                    IconButton(onClick = onMenuClick) {
                        Text(text = "|", fontSize = 28.sp, fontWeight = FontWeight.Light, color = TextSecondary)
                    }
                }
                Text("Notas", style = MaterialTheme.typography.headlineLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPostsClicked,
                ) {
                    Icon(Icons.Default.Http, contentDescription = "Ver Posts API", tint = TextPrimary)
                }

                IconButton(
                    onClick = onAddNoteClicked,
                    modifier = Modifier.clip(CircleShape).background(PlusButtonBackground)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir Nota", tint = TextPrimary)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = SearchBarBackground,
                focusedContainerColor = SearchBarBackground,
                cursorColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (group != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(
                    items = group.notes,
                    key = { it.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        group = group,
                        onClick = { onNoteClicked(note.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(note: Note, group: Group, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            val firstTextItem = note.items.firstOrNull { it.type == NoteItemType.TEXT }
            val checklistItems = note.items.filter { it.type == NoteItemType.CHECKLIST }

            if (firstTextItem != null) {
                Text(text = firstTextItem.text ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            } else if (checklistItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    checklistItems.take(3).forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.text ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val participants = note.items.map { it.authorId }.distinct()
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                participants.forEach { authorId ->
                    val author = group.members.find { it.id == authorId }
                    if (author != null) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(author.color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun animateColorAsState(
    targetValue: Color,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Color>,
    label: String
): State<Color> {
    return androidx.compose.animation.animateColorAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = label
    )
}