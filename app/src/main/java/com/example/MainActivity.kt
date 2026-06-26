package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.BiographyViewModel
import com.example.data.JournalEntry
import com.example.data.UiState
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: BiographyViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("STUDIO") }
    
    // States from ViewModel
    val entries by viewModel.journalEntries.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    
    val transcriptionState by viewModel.transcriptionState.collectAsState()
    val lowLatencyChatState by viewModel.lowLatencyChatState.collectAsState()
    val videoAnalysisState by viewModel.videoAnalysisState.collectAsState()
    val deepReflectionState by viewModel.deepReflectionState.collectAsState()
    val videoGenerationState by viewModel.videoGenerationState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Dynamic Permission Request launcher for microphone recording
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleRecording()
        } else {
            Toast.makeText(context, "Microphone permission is required to narrate your daily life logs", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0C10),
                            Color(0xFF12141C),
                            Color(0xFF1B1D2A)
                        )
                    )
                )
        ) {
            // Background ambient aura glowing effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF9F7AEA).copy(alpha = 0.08f),
                            radius = size.minDimension * 0.5f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f)
                        )
                        drawCircle(
                            color = Color(0xFF319795).copy(alpha = 0.05f),
                            radius = size.minDimension * 0.6f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.8f)
                        )
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Header with custom "YourFlix" Cinema identity
                HeaderSection()

                Spacer(modifier = Modifier.height(12.dp))

                // Smoothly switch between our cinema modules
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        "STUDIO" -> StudioTab(
                            selectedEntry = selectedEntry,
                            isRecording = isRecording,
                            videoGenerationState = videoGenerationState,
                            transcriptionState = transcriptionState,
                            onToggleRecording = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    viewModel.toggleRecording()
                                } else {
                                    recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onGenerateVideo = { prompt, isPortrait ->
                                viewModel.generateVeoVideo(prompt, if (isPortrait) "9:16" else "16:9")
                            },
                            onSaveLog = { title, content, mood ->
                                viewModel.updateJournalText(title, content, mood)
                            },
                            onClearVideoState = { viewModel.clearVideoState() },
                            onClearTranscriptionState = { viewModel.clearTranscriptionState() }
                        )
                        "BRAIN" -> BrainTab(
                            chatMessages = chatMessages,
                            lowLatencyChatState = lowLatencyChatState,
                            deepReflectionState = deepReflectionState,
                            selectedEntry = selectedEntry,
                            onSendMessage = { viewModel.sendLowLatencyChat(it) },
                            onClearChat = { viewModel.clearChat() },
                            onTriggerDeepReflection = { viewModel.runDeepReflection() },
                            onClearReflectionState = { viewModel.clearReflectionState() }
                        )
                        "VISION" -> VisionTab(
                            videoAnalysisState = videoAnalysisState,
                            onAnalyzeVideo = { url, prompt ->
                                viewModel.analyzeVideo(url, prompt)
                            },
                            onClearAnalysisState = { viewModel.clearAnalysisState() }
                        )
                        "LIBRARY" -> LibraryTab(
                            entries = entries,
                            selectedEntry = selectedEntry,
                            onSelectEntry = { viewModel.selectEntry(it) },
                            onDeleteEntry = { viewModel.deleteEntry(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "YourFlix Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "YourFlix",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                )
            }
            Text(
                text = "Turn Daily Life & Imagination Into Hollywood Realism",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.LightGray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            )
        }
        
        // Director Avatar Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Director Status",
                tint = Color(0xFFD69E2E),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "DIRECTOR",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
fun StudioTab(
    selectedEntry: JournalEntry?,
    isRecording: Boolean,
    videoGenerationState: UiState<String>,
    transcriptionState: UiState<String>,
    onToggleRecording: () -> Unit,
    onGenerateVideo: (String, Boolean) -> Unit,
    onSaveLog: (String, String, String) -> Unit,
    onClearVideoState: () -> Unit,
    onClearTranscriptionState: () -> Unit
) {
    var title by remember(selectedEntry) { mutableStateOf(selectedEntry?.title ?: "") }
    var content by remember(selectedEntry) { mutableStateOf(selectedEntry?.content ?: "") }
    var mood by remember(selectedEntry) { mutableStateOf(selectedEntry?.mood ?: "Neutral") }
    var moviePrompt by remember { mutableStateOf("") }
    var isPortraitVideo by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- 1. HERO BANNER ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_hero_banner),
                    contentDescription = "YourFlix Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark glass overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE PRODUCTION STUDIO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Text(
                        text = "Turn Daily Logs & Thoughts Into Cinematic Veo Movies",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // --- 2. ACTIVE CINEMATIC GENERATOR (VEO 3) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "VEO 3.1 CINEMATIC VIDEO ENGINE",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = "Craft high-fidelity cinematic imagery & motion scripts with photorealism.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = moviePrompt,
                        onValueChange = { moviePrompt = it },
                        placeholder = { Text("E.g., Rain-slicked cyber-streets of Neo Tokyo at dawn, echoing steps, reflections in puddles, cinematic look...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("movie_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Aspect Ratio Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aspect Ratio Format:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { isPortraitVideo = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isPortraitVideo) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("16:9 Landscape", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { isPortraitVideo = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPortraitVideo) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("9:16 Portrait", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onGenerateVideo(moviePrompt, isPortraitVideo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_video_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Veo 3 Generate")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Personalized Movie with Veo 3", fontWeight = FontWeight.Bold)
                    }

                    // Render / Loading state matching the Immersive UI design mockup
                    when (videoGenerationState) {
                        is UiState.Idle -> {}
                        is UiState.Loading -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Veo Rendering Pipeline Active",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                        Text(
                                            text = "Processing...",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = (videoGenerationState as UiState.Loading).message,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                        is UiState.Success -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Generated Cinematic Masterpiece:",
                                        style = MaterialTheme.typography.labelMedium.copy(color = Color.Green, fontWeight = FontWeight.Bold)
                                    )
                                    IconButton(onClick = onClearVideoState) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear State", tint = Color.LightGray)
                                    }
                                }
                                
                                // Interactive Cinema Frame Player
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(if (isPortraitVideo) 9f/16f else 16f/9f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.Black)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                                ) {
                                    // Custom visual poster placeholder or direct render
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data("https://images.unsplash.com/photo-1449824913935-59a10b8d2000?auto=format&fit=crop&q=80&w=600")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Cinematic video thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Glass overlay play icon
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Film",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    // Status details at bottom of frame
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                                )
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Movie File URL: ${(videoGenerationState as UiState.Success).data}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "PRODUCED IN ULTRA-REALISM HD • VEO 3",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = (videoGenerationState as UiState.Error).message,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. JOURNAL / DIARY & VOICE TRANSCRIPTION ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAILY LIFE JOURNAL LOG",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        
                        // Active Date
                        Text(
                            text = selectedEntry?.date ?: "No Active Entry",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Log Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("What happened today? (Your daily life or imagination details)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("journal_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mood selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mood:", style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray))
                        val moods = listOf("Neutral", "Epic", "Melancholy", "Mysterious", "Action")
                        Box(modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            Button(
                                onClick = { expanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(mood, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                moods.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m, color = Color.White) },
                                        onClick = {
                                            mood = m
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save / Sync with Room button
                    Button(
                        onClick = {
                            onSaveLog(title, content, mood)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Save Log")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Sync Biography Entry", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- AUDIO TRANSCRIPTION SECTION (gemini-3.5-flash) ---
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VOICE JOURNAL DICTATION (gemini-3.5-flash)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Speak naturally about your physical surrounding or imagination to transcribe it instantly.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onToggleRecording,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) Color.Red else Color.White.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mic_record_button")
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = "Mic",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRecording) "Recording... Tap to Stop" else "Record Voice Log",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Display transcription results
                    when (transcriptionState) {
                        is UiState.Idle -> {}
                        is UiState.Loading -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = (transcriptionState as UiState.Loading).message,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                                )
                            }
                        }
                        is UiState.Success -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Transcribed Text Auto-Saved:",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Green, fontWeight = FontWeight.Bold)
                                        )
                                        IconButton(
                                            onClick = onClearTranscriptionState,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = (transcriptionState as UiState.Success).data,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontStyle = FontStyle.Italic)
                                    )
                                }
                            }
                        }
                        is UiState.Error -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (transcriptionState as UiState.Error).message,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrainTab(
    chatMessages: List<com.example.data.ChatMessage>,
    lowLatencyChatState: UiState<String>,
    deepReflectionState: UiState<String>,
    selectedEntry: JournalEntry?,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onTriggerDeepReflection: () -> Unit,
    onClearReflectionState: () -> Unit
) {
    var queryText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- 1. HIGH-THINKING REFLECTION (gemini-3.1-pro-preview with HIGH Thinking) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "High Thinking",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HIGH-THINKING MOVIE SCRIPT",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            )
                        }

                        // Spark design tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD69E2E).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PRO-THINK", fontSize = 9.sp, color = Color(0xFFD69E2E), fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Analyze current biography logs using high-depth reasoning to synthesize a narrative Hollywood script.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Button(
                        onClick = onTriggerDeepReflection,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("deep_reflection_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Think", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesize Script with High Thinking", fontWeight = FontWeight.Bold)
                    }

                    when (deepReflectionState) {
                        is UiState.Idle -> {
                            if (selectedEntry?.deepReflection?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black.copy(alpha = 0.3f))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Last Generated Script:",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = selectedEntry.deepReflection,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                        )
                                    }
                                }
                            }
                        }
                        is UiState.Loading -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = (deepReflectionState as UiState.Loading).message,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        is UiState.Success -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .border(1.dp, Color.Green.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Deep Script Success:",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Green, fontWeight = FontWeight.Bold)
                                        )
                                        IconButton(onClick = onClearReflectionState, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = (deepReflectionState as UiState.Success).data,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                    )
                                }
                            }
                        }
                        is UiState.Error -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (deepReflectionState as UiState.Error).message,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. LOW-LATENCY SYNTHESIS CHAT (gemini-3.1-flash-lite) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOW-LATENCY CHAT (gemini-3.1-flash-lite)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        IconButton(onClick = onClearChat) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color.Gray)
                        }
                    }

                    Text(
                        text = "Swift answers or immediate movie plot feedback based on today's events.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Chat History
                    if (chatMessages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No messages. Ask to brainstorm a film concept!",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                        ) {
                            chatMessages.forEach { msg ->
                                val isUser = msg.sender == "User"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                                )
                                            )
                                            .background(
                                                if (isUser) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isUser) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                                                else Color.White.copy(alpha = 0.06f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(12.dp)
                                            .widthIn(max = 250.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = msg.sender,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUser) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = queryText,
                            onValueChange = { queryText = it },
                            placeholder = { Text("Ask something fast...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                if (queryText.isNotBlank()) {
                                    onSendMessage(queryText)
                                    queryText = ""
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("send_chat_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send Chat")
                        }
                    }

                    if (lowLatencyChatState is UiState.Loading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fast response generating...", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisionTab(
    videoAnalysisState: UiState<String>,
    onAnalyzeVideo: (String, String) -> Unit,
    onClearAnalysisState: () -> Unit
) {
    var videoUrl by remember { mutableStateOf("") }
    var visionPrompt by remember { mutableStateOf("Extract key artistic information, camera composition, and narrative insights to include in my biography movie script.") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video Vision",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GEMINI PRO VIDEO VISION",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }

                    Text(
                        text = "Input a video URL to run deep Pro video-understanding and extract cinematic direction cues.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("Video URL (MP4, Movie Stream, etc.)") },
                        placeholder = { Text("E.g., https://example.com/assets/sample.mp4") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = visionPrompt,
                        onValueChange = { visionPrompt = it },
                        label = { Text("Video Analysis Query") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("video_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (videoUrl.isNotBlank()) {
                                onAnalyzeVideo(videoUrl, visionPrompt)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("analyze_video_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Analyze")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Video with Gemini Pro", fontWeight = FontWeight.Bold)
                    }

                    when (videoAnalysisState) {
                        is UiState.Idle -> {}
                        is UiState.Loading -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = (videoAnalysisState as UiState.Loading).message,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        is UiState.Success -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .border(1.dp, Color.Green.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Gemini Pro Understanding Result:",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Green, fontWeight = FontWeight.Bold)
                                        )
                                        IconButton(onClick = onClearAnalysisState, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = (videoAnalysisState as UiState.Success).data,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                    )
                                }
                            }
                        }
                        is UiState.Error -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = (videoAnalysisState as UiState.Error).message,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryTab(
    entries: List<JournalEntry>,
    selectedEntry: JournalEntry?,
    onSelectEntry: (JournalEntry) -> Unit,
    onDeleteEntry: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "YOUR MOVIE HISTORY & DIARY LOGS",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (entries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history log found. Tap 'Studio' to build your first log entry!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(entries) { entry ->
                val isSelected = entry.id == selectedEntry?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectEntry(entry) }
                        .testTag("entry_card_${entry.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.date,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            
                            // Mood chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(entry.mood, fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = entry.content,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                            maxLines = 2
                        )

                        // If video exists, show a movie reel indicator
                        if (entry.generatedVideoUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Has Video",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Veo 3 movie clip attached",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { onDeleteEntry(entry.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0F1115),
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = selectedTab == "STUDIO",
            onClick = { onTabSelected("STUDIO") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Studio") },
            label = { Text("Studio", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == "BRAIN",
            onClick = { onTabSelected("BRAIN") },
            icon = { Icon(Icons.Default.Star, contentDescription = "Brain") },
            label = { Text("Brain", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == "VISION",
            onClick = { onTabSelected("VISION") },
            icon = { Icon(Icons.Default.Search, contentDescription = "Vision") },
            label = { Text("Vision", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == "LIBRARY",
            onClick = { onTabSelected("LIBRARY") },
            icon = { Icon(Icons.Default.List, contentDescription = "Library") },
            label = { Text("Library", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )
    }
}
