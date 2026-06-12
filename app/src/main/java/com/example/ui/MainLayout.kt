package com.example.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.viewmodel.FileItem
import com.example.viewmodel.PilehViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: PilehViewModel) {
    val context = LocalContext.current
    
    // Always force RTL Layout for Persian consistency
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val currentScreen = viewModel.activeScreen
        val isDark = viewModel.isDarkMode

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth > 600.dp
                
                Row(modifier = Modifier.fillMaxSize()) {
                    // Wide Screen Rail Menu Sidebar
                    if (isWideScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            header = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Hive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = "پیله OS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            val menuItems = getNavigationItems()
                            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                                items(menuItems) { item ->
                                    NavigationRailItem(
                                        selected = currentScreen == item.id,
                                        onClick = { viewModel.activeScreen = item.id },
                                        icon = { Icon(if (currentScreen == item.id) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                                        label = { Text(item.label, fontSize = 11.sp) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Main content and Screen switcher
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "P",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "PILEH OS",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                                    Text("آفلاین", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                            }
                                            Text(
                                                text = getScreenTitle(currentScreen),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Quick Toggle Dark theme
                                    IconButton(onClick = { viewModel.isDarkMode = !viewModel.isDarkMode }) {
                                        Icon(
                                            imageVector = if (viewModel.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = "تم"
                                        )
                                    }
                                    // Status Badge info
                                    IconButton(onClick = {
                                        Toast.makeText(context, "سیستم عامل پیله - کاملاً ایمن و آفلاین", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = "ایمنی", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        bottomBar = {
                            if (!isWideScreen) {
                                // Bottom Navigation Bar on Mobile compact screens
                                val barItems = getNavigationItems()
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp
                                ) {
                                    barItems.forEach { item ->
                                        NavigationBarItem(
                                            selected = currentScreen == item.id,
                                            onClick = { viewModel.activeScreen = item.id },
                                            icon = { Icon(if (currentScreen == item.id) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                                            label = { Text(item.label, fontSize = 9.sp, maxLines = 1) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                "dashboard" -> DashboardScreen(viewModel)
                                "inspirations" -> InspirationsScreen(viewModel)
                                "ideas" -> IdeasScreen(viewModel)
                                "posts" -> PostsScreen(viewModel)
                                "calendar" -> CalendarScreen(viewModel)
                                "tasks" -> TasksScreen(viewModel)
                                "files" -> FilesScreen(viewModel)
                                "wiki" -> WikiScreen(viewModel)
                                "brand" -> BrandScreen(viewModel)
                                "settings" -> SettingsScreen(viewModel)
                                else -> DashboardScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Model for navigation
data class NavigationItem(
    val id: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private fun getNavigationItems() = listOf(
    NavigationItem("dashboard", "داشبورد", Icons.Default.GridView, Icons.Outlined.GridView),
    NavigationItem("inspirations", "الهام‌ها", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
    NavigationItem("ideas", "ایده‌ها", Icons.Default.Lightbulb, Icons.Outlined.Lightbulb),
    NavigationItem("posts", "پست‌ها", Icons.Default.Article, Icons.Outlined.Article),
    NavigationItem("calendar", "تقویم", Icons.Default.CalendarMonth, Icons.Outlined.CalendarMonth),
    NavigationItem("tasks", "وظایف", Icons.Default.TaskAlt, Icons.Outlined.TaskAlt),
    NavigationItem("files", "فایل‌ها", Icons.Default.FolderZip, Icons.Outlined.FolderZip),
    NavigationItem("wiki", "ویکی", Icons.Default.BookmarkBorder, Icons.Outlined.BookmarkBorder),
    NavigationItem("brand", "برند", Icons.Default.Stars, Icons.Outlined.Stars),
    NavigationItem("settings", "تنظیمات", Icons.Default.Settings, Icons.Outlined.Settings)
)

private fun getScreenTitle(id: String): String = when (id) {
    "dashboard" -> "مرکز فرماندهی پیله"
    "inspirations" -> "الهام‌های محتوایی"
    "ideas" -> "بانک ایده‌های پیله"
    "posts" -> "پست‌های آماده انتشار"
    "calendar" -> "تقویم زمان‌بندی محتوا"
    "tasks" -> "مدیریت وظایف تیم"
    "files" -> "مرکز فایل‌ها و مستندات"
    "wiki" -> "ویکی و دانش سازمانی"
    "brand" -> "کتابخانه هویت برند"
    "settings" -> "تنظیمات و پشتیبان‌گیری"
    else -> "سیستم عامل پیله"
}

// CATEGORY COLORS FOR THE PILEH CONTENT MODEL
fun getCategoryColor(category: String): Color = when (category.trim()) {
    "آگاهی" -> Color(0xFF3A6966)      // Primary Forest
    "ابزار" -> Color(0xFF6A9996)      // Muted Sage
    "جذب ممبر" -> Color(0xFFFF8C42)   // Warm Amber / Gold
    "لید مگنت" -> Color(0xFF8B4A23)   // Crimson Terracotta
    "ویترینی" -> Color(0xFF2E5351)    // Deep Forest Shadow
    else -> Color(0xFF6B7280)        // Neutral Muted Slate
}

// Utility to copy to clipboard
fun copyTextToClipboard(context: Context, text: String, label: String = "Caption") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "کپشن با موفقیت در حافظه کپی شد!", Toast.LENGTH_SHORT).show()
}

// 1. DASHBOARD SCREEN
@Composable
fun DashboardScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    val inspirations by viewModel.inspirations.collectAsState()
    val ideas by viewModel.ideas.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val wikis by viewModel.wikis.collectAsState()
    val metrics by viewModel.contentMetrics.collectAsState()

    var showQuickAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "امروز خوش‌آمدید، مدیر پیله 🌸",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سیستم مدیریت محتوا و دانش درون‌سازمانی برند پیله. تمامی داده‌ها به صورت کاملاً آفلاین و امن درون حافظه این گوشی نگهداری می‌شوند.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showQuickAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ثبت سریع ایده یا الهام جدید", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Summary Statistics Grid
        item {
            Text("خلاصه وضعیت سیستم", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard(
                    title = "الهام‌ها",
                    count = inspirations.size.toString(),
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary
                )
                DashboardStatCard(
                    title = "ایده‌ها",
                    count = ideas.size.toString(),
                    icon = Icons.Default.Lightbulb,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStatCard(
                    title = "پست‌های آماده",
                    count = posts.count { it.status == "آماده انتشار" || it.status == "زمان بندی شده" }.toString(),
                    icon = Icons.Default.Article,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF4CAF50)
                )
                DashboardStatCard(
                    title = "وظایف باز",
                    count = tasks.count { it.status == "باز" || it.status == "در حال انجام" }.toString(),
                    icon = Icons.Default.NotificationImportant,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE91E63)
                )
            }
        }

        // GOLDEN RATIO CALCULATOR AND WARNINGS (قانون طلایی پیله)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "قانون طلایی پیله (توزیع محتوا)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text("نسبت ایده‌آل هر دسته: ۲۰٪", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "برای موفقیت پیله، باید برابری ۲۰٪ بین تمامی دسته‌ها حفظ شود. عدم تعادل منجر به نمایش هشدار می‌شود:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val categories = listOf("آگاهی", "ابزار", "جذب ممبر", "لید مگنت", "ویترینی")
                    var hasAnyWarning = false

                    categories.forEach { cat ->
                        val count = metrics.categoryCounts[cat] ?: 0
                        val percent = metrics.categoryPercentages[cat] ?: 0f
                        val warning = metrics.warnings[cat]

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(getCategoryColor(cat))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("$count عدد (${String.format("%.1f", percent)}%)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { if (percent > 0) percent / 100f else 0.05f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = getCategoryColor(cat),
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            if (warning != null) {
                                hasAnyWarning = true
                                Text(
                                    text = "⚠️ $warning",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    if (!hasAnyWarning && metrics.totalCount >= 5) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "تعادل محتوایی پیله کاملاً رعایت شده است! نسبت طلایی برقرار است. 🦋",
                                color = Color(0xFF1B5E20),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else if (metrics.totalCount < 5) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "برای پایش دقیق نسبت طلایی، حداقل ۵ محتوا (ایده یا پست) ثبت کنید.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    // Quick Add dialog
    if (showQuickAddDialog) {
        Dialog(onDismissRequest = { showQuickAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                var isIdeaTab by remember { mutableStateOf(true) }
                var title by remember { mutableStateOf("") }
                var desc by remember { mutableStateOf("") }
                var category by remember { mutableStateOf("آگاهی") }
                
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ثبت جدید سریع", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    
                    TabRow(selectedTabIndex = if (isIdeaTab) 0 else 1) {
                        Tab(selected = isIdeaTab, onClick = { isIdeaTab = true }) {
                            Text("ایده جدید", modifier = Modifier.padding(10.dp))
                        }
                        Tab(selected = !isIdeaTab, onClick = { isIdeaTab = false }) {
                            Text("الهام جدید", modifier = Modifier.padding(10.dp))
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("توضیحات") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    if (isIdeaTab) {
                        Text("دسته بندی قانون طلایی پیله:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        val cats = listOf("آگاهی", "ابزار", "جذب ممبر", "لید مگنت", "ویترینی")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(cats) { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showQuickAddDialog = false }) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (title.isNotEmpty()) {
                                if (isIdeaTab) {
                                    viewModel.saveIdea(
                                        title = title,
                                        desc = desc,
                                        category = category,
                                        priority = "متوسط",
                                        tags = emptyList(),
                                        files = emptyList(),
                                        linkedInspirationIds = emptyList()
                                    )
                                } else {
                                    viewModel.saveInspiration(
                                        title = title,
                                        desc = desc,
                                        source = "ثبت دستی سریع",
                                        link = "",
                                        tags = emptyList(),
                                        files = emptyList(),
                                        status = "خام"
                                    )
                                }
                                Toast.makeText(context, "با موفقیت ثبت شد!", Toast.LENGTH_SHORT).show()
                                showQuickAddDialog = false
                            } else {
                                Toast.makeText(context, "لطفاً عنوان را وارد کنید", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("ذخیره")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(title: String, count: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    
    // Choose theme background & text colors based on the card type to match the "High Density" HTML design
    val (containerBg, contentColor, iconBgTint) = when (title) {
        "ایده‌ها", "ایده‌های باز" -> {
            if (isDark) {
                Triple(Color(0xFF1E3331), Color(0xFF87D4CF), Color(0xFF87D4CF).copy(alpha = 0.15f))
            } else {
                Triple(Color(0xFFE8F3F1), Color(0xFF3A6966), Color(0xFF3A6966).copy(alpha = 0.15f))
            }
        }
        "پست‌های آماده" -> {
            if (isDark) {
                Triple(Color(0xFF3B2418), Color(0xFFF5A475), Color(0xFFF5A475).copy(alpha = 0.15f))
            } else {
                Triple(Color(0xFFFDECE2), Color(0xFF8B4A23), Color(0xFF8B4A23).copy(alpha = 0.15f))
            }
        }
        else -> {
            // Default border style in the design
            Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, color.copy(alpha = 0.12f))
        }
    }

    val border = if (title == "ایده‌ها" || title == "ایده‌های باز" || title == "پست‌های آماده") {
        null
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = border,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (title == "ایده‌ها" || title == "ایده‌های باز" || title == "پست‌های آماده") contentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = count,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (title == "ایده‌ها" || title == "ایده‌های باز" || title == "پست‌های آماده") contentColor else MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (title == "ایده‌ها" || title == "ایده‌های باز" || title == "پست‌های آماده") contentColor else color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// 2. INSPIRATIONS SCREEN
@Composable
fun InspirationsScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    val inspirations by viewModel.inspirations.collectAsState()
    
    var showAddInspirationDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val copiedName = viewModel.copyFileToSandbox(it)
            if (copiedName != null) {
                val currentInp = viewModel.selectedInspiration
                if (currentInp != null) {
                    val currentAttachments = currentInp.attachments.toMutableList()
                    currentAttachments.add(copiedName)
                    viewModel.selectedInspiration = currentInp.copy(attachments = currentAttachments)
                    Toast.makeText(context, "فایل با موفقیت کپی و ضمیمه شد", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Add
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجو در الهام‌ها...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(onClick = {
                viewModel.selectedInspiration = null
                showAddInspirationDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredInspirations = inspirations.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.source.contains(searchQuery, ignoreCase = true)
        }

        if (filteredInspirations.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("هیچ الهامی یافت نشد. اولین الهام را ثبت کنید!", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredInspirations) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Badge(containerColor = when(item.status) {
                                    "خام" -> MaterialTheme.colorScheme.errorContainer
                                    "بررسی شده" -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                }) {
                                    Text(item.status, fontSize = 11.sp, modifier = Modifier.padding(2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                item.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Reference Source Row
                            if (item.source.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("منبع: ${item.source}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    if (item.sourceLink.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "(لینک)", 
                                            fontSize = 11.sp, 
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.clickable {
                                                try {
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.sourceLink))
                                                    browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    context.startActivity(browserIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "خطا در مدیریت لینک خارجی", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // Tags display
                            if (item.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(item.tags) { tg ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(tg, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            // Attachments list
                            if (item.attachments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${item.attachments.size} فایل پیوست شده", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Edit actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    viewModel.selectedInspiration = item
                                    showAddInspirationDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = {
                                    viewModel.deleteInspiration(item)
                                    Toast.makeText(context, "حذف شد", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal adding inspiration
    if (showAddInspirationDialog) {
        val editingItem = viewModel.selectedInspiration
        
        var title by remember { mutableStateOf(editingItem?.title ?: "") }
        var description by remember { mutableStateOf(editingItem?.description ?: "") }
        var source by remember { mutableStateOf(editingItem?.source ?: "") }
        var sourceLink by remember { mutableStateOf(editingItem?.sourceLink ?: "") }
        var tagInput by remember { mutableStateOf(editingItem?.tags?.joinToString("، ") ?: "") }
        var status by remember { mutableStateOf(editingItem?.status ?: "خام") }
        val atts = remember { mutableStateListOf<String>().apply { addAll(editingItem?.attachments ?: emptyList()) } }

        Dialog(onDismissRequest = { showAddInspirationDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(if (editingItem != null) "ویرایش الهام محتوایی" else "ثبت الهام محتوایی جدید", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الهام") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("توضیحات و ایده اولیه") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    }
                    item {
                        OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("منبع (شبکه اجتماعی، کتاب و...)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = sourceLink, onValueChange = { sourceLink = it }, label = { Text("لینک اینترنتی منبع") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = tagInput, onValueChange = { tagInput = it }, label = { Text("برچسب‌ها (با ویرگول جدا کنید)") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Text("چرخه بررسی:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val states = listOf("خام", "بررسی شده", "تبدیل به ایده")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            states.forEach { s ->
                                FilterChip(
                                    selected = status == s,
                                    onClick = { status = s },
                                    label = { Text(s) }
                                )
                            }
                        }
                    }

                    // Attach local copy files which are stored directly inside private app storage
                    item {
                        Text("پیوست‌ها (کپی اختصاصی در برنامه):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                fileLauncher.launch("*/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("انتخاب فایل برای کپی خصوصی")
                        }
                        
                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                            items(atts) { file ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(file.substringAfter('_'), fontSize = 11.sp, maxLines = 1)
                                    IconButton(onClick = { atts.remove(file) }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showAddInspirationDialog = false }) {
                                Text("انصراف")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (title.isNotEmpty()) {
                                    val tagsList = tagInput.split(Regex("[،,؛]")).map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.saveInspiration(
                                        title = title,
                                        desc = description,
                                        source = source,
                                        link = sourceLink,
                                        tags = tagsList,
                                        files = atts,
                                        status = status
                                    )
                                    Toast.makeText(context, "با موفقیت ذخیره شد!", Toast.LENGTH_SHORT).show()
                                    showAddInspirationDialog = false
                                } else {
                                    Toast.makeText(context, "لطفاً عنوان را وارد کنید", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("ذخیره نهایی")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. IDEAS SCREEN
@Composable
fun IdeasScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    val ideas by viewModel.ideas.collectAsState()
    val inspirations by viewModel.inspirations.collectAsState()

    var showAddIdeaDialog by remember { mutableStateOf(false) }
    var selectedCategoryTab by remember { mutableStateOf("همه") }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val copiedName = viewModel.copyFileToSandbox(it)
            if (copiedName != null) {
                val currentIdea = viewModel.selectedIdea
                if (currentIdea != null) {
                    val currentAttachments = currentIdea.attachments.toMutableList()
                    currentAttachments.add(copiedName)
                    viewModel.selectedIdea = currentIdea.copy(attachments = currentAttachments)
                    Toast.makeText(context, "فایل ضمیمه ایده شد", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tabs
        val categoriesTabList = listOf("همه", "آگاهی", "ابزار", "جذب ممبر", "لید مگنت", "ویترینی")
        ScrollableTabRow(
            selectedTabIndex = categoriesTabList.indexOf(selectedCategoryTab),
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            categoriesTabList.forEach { cat ->
                Tab(
                    selected = selectedCategoryTab == cat,
                    onClick = { selectedCategoryTab = cat }
                ) {
                    Text(cat, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger additions buttons
        Button(
            onClick = {
                viewModel.selectedIdea = null
                showAddIdeaDialog = true
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("ایده خلاق جدید")
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredIdeas = ideas.filter {
            selectedCategoryTab == "همه" || it.subject == selectedCategoryTab
        }

        if (filteredIdeas.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("هیچ ایده‌ای در این دسته‌بندی یافت نشد.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredIdeas) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(getCategoryColor(item.subject))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Badge(containerColor = when(item.priority) {
                                    "زیاد" -> MaterialTheme.colorScheme.errorContainer
                                    "متوسط" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }) {
                                    Text("اولویت: ${item.priority}", fontSize = 11.sp, modifier = Modifier.padding(3.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("دسته: ${item.subject}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = getCategoryColor(item.subject))
                                if (item.attachments.isNotEmpty()) {
                                    Text("📎 ${item.attachments.size} فایل ضمیمه شده", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            // Show linked inspirations if any
                            if (item.linkedInspirationIds.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, "الهام همراه", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مرتبط با الهام‌های کد شماره ${item.linkedInspirationIds.joinToString()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = {
                                    // Conver to post directly! This is a super clever helper!
                                    viewModel.selectedPost = PostEntity(
                                        title = item.title,
                                        category = item.subject,
                                        caption = item.description,
                                        hashtags = item.tags,
                                        cta = "برای عضویت کلیک کنید",
                                        publishDate = "",
                                        publishTime = "18:00",
                                        status = "در حال تولید",
                                        attachments = item.attachments
                                    )
                                    viewModel.activeScreen = "posts"
                                    Toast.makeText(context, "در حال تبدیل ایده به پست آماده...", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Transform, contentDescription = "تبدیل به پست", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    viewModel.selectedIdea = item
                                    showAddIdeaDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = {
                                    viewModel.deleteIdea(item)
                                    Toast.makeText(context, "ایده حذف شد", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal adding/editing Idea
    if (showAddIdeaDialog) {
        val editingItem = viewModel.selectedIdea
        
        var title by remember { mutableStateOf(editingItem?.title ?: "") }
        var description by remember { mutableStateOf(editingItem?.description ?: "") }
        var category by remember { mutableStateOf(editingItem?.subject ?: "آگاهی") }
        var priority by remember { mutableStateOf(editingItem?.priority ?: "متوسط") }
        var tagInput by remember { mutableStateOf(editingItem?.tags?.joinToString("، ") ?: "") }
        val atts = remember { mutableStateListOf<String>().apply { addAll(editingItem?.attachments ?: emptyList()) } }
        val linkedInspirationsSelected = remember { mutableStateListOf<Int>().apply { addAll(editingItem?.linkedInspirationIds ?: emptyList()) } }

        Dialog(onDismissRequest = { showAddIdeaDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(if (editingItem != null) "ویرایش ایده تولید محتوا" else "ایجاد ایده تولید محتوای پیله", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان ایده") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("شرح ایده و طرح کار") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    }

                    item {
                        Text("مدل محتوایی اجباری پیله:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val modelCats = listOf("آگاهی", "ابزار", "جذب ممبر", "لید مگنت", "ویترینی")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            modelCats.forEach { c ->
                                FilterChip(
                                    selected = category == c,
                                    onClick = { category = c },
                                    label = { Text(c) }
                                )
                            }
                        }
                    }

                    item {
                        Text("اولویت تولید:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val priorities = listOf("کم", "متوسط", "زیاد")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            priorities.forEach { p ->
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(p) }
                                )
                            }
                        }
                    }

                    // Linked Inspirations selector representation
                    item {
                        Text("ارتباط با الهام‌ها:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (inspirations.isEmpty()) {
                            Text("ابتدا چند الهام در مرکز ایده ثبت کنید", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(inspirations) { insp ->
                                    val isSelected = linkedInspirationsSelected.contains(insp.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) linkedInspirationsSelected.remove(insp.id)
                                            else linkedInspirationsSelected.add(insp.id)
                                        },
                                        label = { Text("${insp.id}: ${insp.title.take(15)}...") }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("پیوست‌ها (کپی مستقل از گوشی):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Button(
                            onClick = { fileLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بارگذاری کپی خصوصی")
                        }
                        
                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                            items(atts) { file ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(file.substringAfter('_'), fontSize = 11.sp)
                                    IconButton(onClick = { atts.remove(file) }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showAddIdeaDialog = false }) {
                                Text("انصراف")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (title.isNotEmpty()) {
                                    val tagsList = tagInput.split(Regex("[،,؛]")).map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.saveIdea(
                                        title = title,
                                        desc = description,
                                        category = category,
                                        priority = priority,
                                        tags = tagsList,
                                        files = atts,
                                        linkedInspirationIds = linkedInspirationsSelected
                                    )
                                    Toast.makeText(context, "با موفقیت ذخیره شد!", Toast.LENGTH_SHORT).show()
                                    showAddIdeaDialog = false
                                } else {
                                    Toast.makeText(context, "لطفاً عنوان ایده را بنویسید", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("ثبت نهایی ایده")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. POSTS SCREEN
@Composable
fun PostsScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    val posts by viewModel.posts.collectAsState()

    var showAddPostDialog by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("همه") }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val copiedName = viewModel.copyFileToSandbox(it)
            if (copiedName != null) {
                val currentPost = viewModel.selectedPost
                if (currentPost != null) {
                    val currentAttachments = currentPost.attachments.toMutableList()
                    currentAttachments.add(copiedName)
                    viewModel.selectedPost = currentPost.copy(attachments = currentAttachments)
                    Toast.makeText(context, "فایل با موفقیت در برنامه کپی و ضمیمه پست شد", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Status filter headers
        val statuses = listOf("همه", "ایده", "در حال تولید", "در حال طراحی", "آماده انتشار", "زمان بندی شده", "منتشر شده", "آرشیو شده")
        ScrollableTabRow(
            selectedTabIndex = statuses.indexOf(selectedStatusFilter),
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            statuses.forEach { st ->
                Tab(
                    selected = selectedStatusFilter == st,
                    onClick = { selectedStatusFilter = st }
                ) {
                    Text(st, modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.selectedPost = null
                showAddPostDialog = true
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("ایجاد پست جدید آماده")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Auto catch selected conversion from ideas
        LaunchedEffect(viewModel.selectedPost) {
            if (viewModel.selectedPost != null && viewModel.selectedPost?.id == 0) {
                showAddPostDialog = true
            }
        }

        val filteredPosts = posts.filter {
            selectedStatusFilter == "همه" || it.status == selectedStatusFilter
        }

        if (filteredPosts.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("هیچ پستی با این شرایط یافت نشد.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredPosts) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(getCategoryColor(item.category))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(item.status, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "دسته بندی قانون طلایی: ${item.category}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = getCategoryColor(item.category)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("📝 کپشن:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.caption, fontSize = 13.sp, lineHeight = 18.sp)
                                    if (item.hashtags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(item.hashtags.joinToString(" "), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                    }
                                    if (item.cta.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("CTA: ${item.cta}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { copyTextToClipboard(context, "${item.caption}\n\n${item.hashtags.joinToString(" ")}") },
                                        modifier = Modifier.height(36.dp).width(140.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("کپی سریع کپشن", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Checklist انتشار (Checklist feature requested)
                            Text("چک‌لیست انتشار پست:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    ChecklistRow("طراحی انجام شد", item.checklistDesignCompleted) { viewModel.toggleChecklistItem(item, "design", it) }
                                    ChecklistRow("کپشن نوشته شد", item.checklistCaptionWritten) { viewModel.toggleChecklistItem(item, "caption", it) }
                                    ChecklistRow("هشتگ‌ها آماده شد", item.checklistHashtagsReady) { viewModel.toggleChecklistItem(item, "hashtag", it) }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    ChecklistRow("کاور آماده شد", item.checklistCoverReady) { viewModel.toggleChecklistItem(item, "cover", it) }
                                    ChecklistRow("زمان‌بندی شد", item.checklistSchedulingDone) { viewModel.toggleChecklistItem(item, "schedule", it) }
                                    ChecklistRow("منتشر شد 🎉", item.checklistPublished) { viewModel.toggleChecklistItem(item, "publish", it) }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Calendar display details
                            if (item.publishDate.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("برنامه‌ریزی انتشار: ${item.publishDate} در ${item.publishTime}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            // Files count
                            if (item.attachments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("فایل‌های ضمیمه: ${item.attachments.size} عدد (ذخیره مستقل)", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = {
                                    viewModel.selectedPost = item
                                    showAddPostDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = {
                                    viewModel.deletePost(item)
                                    Toast.makeText(context, "پست حذف شد", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog adding or editing Post
    if (showAddPostDialog) {
        val editingItem = viewModel.selectedPost
        
        var title by remember { mutableStateOf(editingItem?.title ?: "") }
        var category by remember { mutableStateOf(editingItem?.category ?: "آگاهی") }
        var caption by remember { mutableStateOf(editingItem?.caption ?: "") }
        var tagsInput by remember { mutableStateOf(editingItem?.hashtags?.joinToString(" ") ?: "") }
        var cta by remember { mutableStateOf(editingItem?.cta ?: "") }
        var pubDate by remember { mutableStateOf(editingItem?.publishDate ?: "") }
        var pubTime by remember { mutableStateOf(editingItem?.publishTime ?: "18:00") }
        var status by remember { mutableStateOf(editingItem?.status ?: "ایده") }
        val atts = remember { mutableStateListOf<String>().apply { addAll(editingItem?.attachments ?: emptyList()) } }

        Dialog(onDismissRequest = {
            viewModel.selectedPost = null
            showAddPostDialog = false
        }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(if (editingItem != null && editingItem.id > 0) "ویرایش پست آماده" else "ایجاد پست آماده جدید در پیله", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان پست") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Text("دسته محتوایی:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val modelCats = listOf("آگاهی", "ابزار", "جذب ممبر", "لید مگنت", "ویترینی")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            modelCats.forEach { c ->
                                FilterChip(
                                    selected = category == c,
                                    onClick = { category = c },
                                    label = { Text(c) }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("کپشن پست") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    }
                    item {
                        OutlinedTextField(value = tagsInput, onValueChange = { tagsInput = it }, label = { Text("هشتگ‌ها (با فاصله جدا کنید)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = cta, onValueChange = { cta = it }, label = { Text("CTA (کال تو اکشن)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = pubDate, onValueChange = { pubDate = it }, label = { Text("تاریخ انتشار به شمسی (مثال: ۱۴۰۵/۰۳/۲۴)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = pubTime, onValueChange = { pubTime = it }, label = { Text("ساعت انتشار (مثال: ۱۸:۰۰)") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Text("وضعیت پست:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val postStatuses = listOf("ایده", "در حال تولید", "در حال طراحی", "آماده انتشار", "زمان بندی شده", "منتشر شده")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(postStatuses) { s ->
                                FilterChip(
                                    selected = status == s,
                                    onClick = { status = s },
                                    label = { Text(s) }
                                )
                            }
                        }
                    }

                    item {
                        Text("ضمیمه‌ها (کپی از گوشی):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Button(
                            onClick = { fileLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("کپی خصوصی فایل به پست")
                        }
                        
                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                            items(atts) { file ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(file.substringAfter('_'), fontSize = 11.sp, maxLines = 1)
                                    IconButton(onClick = { atts.remove(file) }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = {
                                viewModel.selectedPost = null
                                showAddPostDialog = false
                            }) {
                                Text("انصراف")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (title.isNotEmpty()) {
                                    val hts = tagsInput.split(" ").map { it.trim() }.filter { it.isNotEmpty() }.map { if (it.startsWith('#')) it else "#$it" }
                                    viewModel.savePost(
                                        title = title,
                                        category = category,
                                        caption = caption,
                                        hashtags = hts,
                                        cta = cta,
                                        pubDate = pubDate,
                                        pubTime = pubTime,
                                        status = status,
                                        files = atts
                                    )
                                    Toast.makeText(context, "پست با موفقیت ثبت شد!", Toast.LENGTH_SHORT).show()
                                    viewModel.selectedPost = null
                                    showAddPostDialog = false
                                } else {
                                    Toast.makeText(context, "لطفاً عنوان را وارد کنید", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("ذخیره نهایی")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, style = MaterialTheme.typography.bodyMedium)
    }
}

// 5. CALENDAR SCREEN
@Composable
fun CalendarScreen(viewModel: PilehViewModel) {
    val posts by viewModel.posts.collectAsState()
    var selectedCalendarMode by remember { mutableStateOf("ماهانه") } // ماهانه, هفتگی, روزانه
    var currentOffset by remember { mutableStateOf(0) } // For calendar shifting

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Mode Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                val modes = listOf("روزانه", "هفتگی", "ماهانه")
                modes.forEach { m ->
                    FilterChip(
                        selected = selectedCalendarMode == m,
                        onClick = {
                            selectedCalendarMode = m
                            currentOffset = 0
                        },
                        label = { Text(m) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Shifts Back/Forth
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentOffset-- }) {
                    Icon(Icons.Default.ArrowRight, contentDescription = "بعدی")
                }
                Text("تغییر بازه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                IconButton(onClick = { currentOffset++ }) {
                    Icon(Icons.Default.ArrowLeft, contentDescription = "قبلی")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Layout representation
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تقویم رنگی دسته بندی‌ها (آگاهی: سبز | ابزار: زرد | جذب: آبی | لید مگنت: ارغوانی | ویترینی: نارنجی)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (selectedCalendarMode == "ماهانه") {
                    // Quick simulation of Persian calendar grid
                    val daysOfWeek = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        daysOfWeek.forEach { d ->
                            Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..30).toList()) { day ->
                            // Map day to post if any
                            val dayPosts = posts.filter {
                                // Match hypothetical day tag
                                it.publishDate.contains("/$day") || it.publishDate.endsWith(day.toString())
                            }

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (dayPosts.isNotEmpty()) getCategoryColor(dayPosts.first().category).copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        1.dp,
                                        if (dayPosts.isNotEmpty()) getCategoryColor(dayPosts.first().category)
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                                    .clickable {
                                        if (dayPosts.isNotEmpty()) {
                                            Toast.makeText(context, "پست: ${dayPosts.joinToString { it.title }}", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "$day خرداد - بدون پست برنامه‌ریزی شده", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(day.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (dayPosts.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(getCategoryColor(dayPosts.first().category))
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Day or Weekly linear layouts
                    val listToShow = posts.filter { it.publishDate.isNotEmpty() }
                    if (listToShow.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("پست برنامه‌ریزی شده‌ای در این هفته یافت نشد.", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listToShow) { post ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(getCategoryColor(post.category).copy(alpha = 0.08f))
                                        .border(1.dp, getCategoryColor(post.category), RoundedCornerShape(8.dp))
                                        .clickable {
                                            Toast.makeText(context, "پست: ${post.title}\nتاریخ انتشار: ${post.publishDate}", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(getCategoryColor(post.category))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(post.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("برنامه دسته: ${post.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                    Text("${post.publishDate} ساعت ${post.publishTime}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. TASKS SCREEN
@Composable
fun TasksScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedStatusTab by remember { mutableStateOf("همه") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Kanban type tabs layout
        val statusTabs = listOf("همه", "باز", "در حال انجام", "انجام شده", "لغو شده")
        TabRow(selectedTabIndex = statusTabs.indexOf(selectedStatusTab)) {
            statusTabs.forEach { st ->
                Tab(selected = selectedStatusTab == st, onClick = { selectedStatusTab = st }) {
                    Text(st, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.selectedTask = null
                showAddTaskDialog = true
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("افزودن وظیفه جدید به تیم")
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredTasks = tasks.filter {
            selectedStatusTab == "همه" || it.status == selectedStatusTab
        }

        if (filteredTasks.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("هیچ خلاصه کاری یافت نشد.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTasks) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Badge(containerColor = when(item.status) {
                                    "انجام شده" -> Color(0xFFC8E6C9)
                                    "در حال انجام" -> Color(0xFFFFE082)
                                    "لغو شده" -> Color(0xFFFFCDD2)
                                    else -> Color(0xFFCFD8DC)
                                }) {
                                    Text(item.status, fontSize = 11.sp, color = Color.Black, modifier = Modifier.padding(3.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("مسئول اجرا: ${item.assignee.ifEmpty { "نابینا" }}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("سررسید: ${item.dueDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                if (item.status != "انجام شده") {
                                    IconButton(onClick = {
                                        viewModel.saveTask(
                                            title = item.title,
                                            desc = item.description,
                                            assignee = item.assignee,
                                            dueDate = item.dueDate,
                                            priority = item.priority,
                                            status = "انجام شده"
                                        )
                                        Toast.makeText(context, "تغییر وضعیت به انجام شده!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "تکمیل", tint = Color(0xFF4CAF50))
                                    }
                                }
                                IconButton(onClick = {
                                    viewModel.selectedTask = item
                                    showAddTaskDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = {
                                    viewModel.deleteTask(item)
                                    Toast.makeText(context, "حذف شد", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Task editor dialog
    if (showAddTaskDialog) {
        val editingItem = viewModel.selectedTask
        
        var title by remember { mutableStateOf(editingItem?.title ?: "") }
        var description by remember { mutableStateOf(editingItem?.description ?: "") }
        var assignee by remember { mutableStateOf(editingItem?.assignee ?: "") }
        var dueDate by remember { mutableStateOf(editingItem?.dueDate ?: "") }
        var priority by remember { mutableStateOf(editingItem?.priority ?: "متوسط") }
        var status by remember { mutableStateOf(editingItem?.status ?: "باز") }

        Dialog(onDismissRequest = { showAddTaskDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(if (editingItem != null) "ویرایش وظیفه تیمی" else "ثبت وظیفه کاری تیمی جدید", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان کار") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("شرح عملیات") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    }
                    item {
                        OutlinedTextField(value = assignee, onValueChange = { assignee = it }, label = { Text("مسئول انجام کار") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("تاریخ انجام کار یا سررسید (مثال: ۱۴۰۵/۰۳/۲۴)") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Text("اولویت وظیفه:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val priorities = listOf("کم", "متوسط", "زیاد")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            priorities.forEach { p ->
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(p) }
                                )
                            }
                        }
                    }

                    item {
                        Text("وضعیت جاری:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val statesList = listOf("باز", "در حال انجام", "انجام شده", "لغو شده")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            statesList.forEach { s ->
                                FilterChip(
                                    selected = status == s,
                                    onClick = { status = s },
                                    label = { Text(s) }
                                )
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showAddTaskDialog = false }) {
                                Text("انصراف")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (title.isNotEmpty()) {
                                    viewModel.saveTask(
                                        title = title,
                                        desc = description,
                                        assignee = assignee,
                                        dueDate = dueDate,
                                        priority = priority,
                                        status = status
                                    )
                                    Toast.makeText(context, "با موفقیت ثبت شد!", Toast.LENGTH_SHORT).show()
                                    showAddTaskDialog = false
                                } else {
                                    Toast.makeText(context, "عنوان کار را وارد کنید", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("ذخیره وظیفه")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. FILES SCREEN
@Composable
fun FilesScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    var selectedFolderCategory by remember { mutableStateOf("Posts") }
    
    val folderCategories = listOf("Posts", "Ideas", "Inspirations", "Documents", "Videos", "Images", "Brand Assets")
    val virtualFolders = viewModel.getVirtualFiles()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("پوشه‌های اختصاصی پیله", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        
        ScrollableTabRow(
            selectedTabIndex = folderCategories.indexOf(selectedFolderCategory),
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            folderCategories.forEach { folder ->
                val count = virtualFolders[folder]?.size ?: 0
                Tab(
                    selected = selectedFolderCategory == folder,
                    onClick = { selectedFolderCategory = folder }
                ) {
                    Text("$folder ($count)", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions to let team import random manual documents directly to Brand Assets folder
        if (selectedFolderCategory == "Brand Assets") {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                uri?.let {
                    val copiedName = viewModel.copyFileToSandbox(it)
                    if (copiedName != null) {
                        viewModel.saveBrandAsset(
                            name = copiedName.substringAfter('_'),
                            value = copiedName,
                            category = "هویت بصری"
                        )
                        Toast.makeText(context, "فایل برند با موفقیت در برنامه پیله ثبت شد", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            Button(
                onClick = { launcher.launch("*/*") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("بارگذاری دستی فایل برند")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        val fileList = virtualFolders[selectedFolderCategory] ?: emptyList()

        if (fileList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("این پوشه خالی است. فایل‌های پیوستی خود را در برگه‌ها بارگذاری کنید.", color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(fileList) { fileItem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "فایل: ${fileItem.originalName}\nحجم: ${fileItem.size}\nذخیره محلی: ${fileItem.localFile.absolutePath}", Toast.LENGTH_LONG).show()
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val ext = fileItem.localFile.extension.lowercase()
                            val icon = when {
                                ext in listOf("png", "jpg", "jpeg", "webp") -> Icons.Default.Image
                                ext in listOf("mp4", "mkv", "mov") -> Icons.Default.VideoFile
                                ext == "pdf" -> Icons.Default.PictureAsPdf
                                ext in listOf("zip", "rar") -> Icons.Default.Archive
                                else -> Icons.Default.Drafts
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = fileItem.originalName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = fileItem.size,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 8. WIKI SCREEN
@Composable
fun WikiScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    val wikis by viewModel.wikis.collectAsState()

    var showAddWikiDialog by remember { mutableStateOf(false) }
    var selectedWikiCategory by remember { mutableStateOf("همه") }

    val wikiCategories = listOf("همه", "تصمیمات", "استراتژی‌ها", "فرآیندها", "جلسات", "قوانین", "اسناد مهم")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScrollableTabRow(
            selectedTabIndex = wikiCategories.indexOf(selectedWikiCategory),
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            wikiCategories.forEach { cat ->
                Tab(
                    selected = selectedWikiCategory == cat,
                    onClick = { selectedWikiCategory = cat }
                ) {
                    Text(cat, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.selectedWiki = null
                showAddWikiDialog = true
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("افزودن مدخل ویکی جدید")
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredWikis = wikis.filter {
            selectedWikiCategory == "همه" || it.category == selectedWikiCategory
        }

        if (filteredWikis.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("هیچ مدخلی برای این دسته بندی یافت نشد.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredWikis) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(item.category, fontSize = 11.sp, modifier = Modifier.padding(3.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.content, fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = {
                                    viewModel.selectedWiki = item
                                    showAddWikiDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                }
                                IconButton(onClick = {
                                    viewModel.deleteWiki(item)
                                    Toast.makeText(context, "مدخل ویکی حذف شد", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Add Wiki
    if (showAddWikiDialog) {
        val editingItem = viewModel.selectedWiki
        
        var title by remember { mutableStateOf(editingItem?.title ?: "") }
        var content by remember { mutableStateOf(editingItem?.content ?: "") }
        var category by remember { mutableStateOf(editingItem?.category ?: "استراتژی‌ها") }

        Dialog(onDismissRequest = { showAddWikiDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(if (editingItem != null) "ویرایش مدخل ویکی" else "ایجاد دانش سازمانی (ویکی پیله)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان فایل دانش") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Text("دسته بندی ویکی:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val wikiCats = listOf("تصمیمات", "استراتژی‌ها", "فرآیندها", "جلسات", "قوانین", "اسناد مهم")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(wikiCats) { c ->
                                FilterChip(
                                    selected = category == c,
                                    onClick = { category = c },
                                    label = { Text(c) }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("محتوا یا متن سند") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showAddWikiDialog = false }) {
                                Text("انصراف")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (title.isNotEmpty()) {
                                    viewModel.saveWiki(title, content, category)
                                    Toast.makeText(context, "با موفقیت ثبت شد!", Toast.LENGTH_SHORT).show()
                                    showAddWikiDialog = false
                                } else {
                                    Toast.makeText(context, "لطفاً عنوان را وارد کنید", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("ذخیره در ویکی")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 9. BRAND SCREEN
@Composable
fun BrandScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    val brandAssets by viewModel.brandAssets.collectAsState()

    var showBrandAddDialog by remember { mutableStateOf(false) }
    val brandCategories = listOf("هویت بصری", "رنگ‌ها", "فونت‌ها", "راهنمای تولید", "غیره")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("کتابخانه یکپارچه برند پیله 🦋", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("هویت بصری، راهنمای تولید و دارایی‌ها", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
            Button(onClick = {
                viewModel.selectedBrandAsset = null
                showBrandAddDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن راهنما")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (brandAssets.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("درحال بارگذاری اطلاعات برند...", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                brandCategories.forEach { category ->
                    val categoryItems = brandAssets.filter { it.category == category }
                    if (categoryItems.isNotEmpty()) {
                        item {
                            Text(category, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(categoryItems) { asset ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(asset.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Row {
                                            IconButton(onClick = {
                                                copyTextToClipboard(context, asset.value, asset.name)
                                            }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "کپی", modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = {
                                                viewModel.selectedBrandAsset = asset
                                                showBrandAddDialog = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = {
                                                viewModel.deleteBrandAsset(asset)
                                                Toast.makeText(context, "دارایی برند حذف شد", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        asset.value,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Add Brand Asset
    if (showBrandAddDialog) {
        val editingItem = viewModel.selectedBrandAsset
        
        var name by remember { mutableStateOf(editingItem?.name ?: "") }
        var value by remember { mutableStateOf(editingItem?.value ?: "") }
        var category by remember { mutableStateOf(editingItem?.category ?: "هویت بصری") }

        Dialog(onDismissRequest = { showBrandAddDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (editingItem != null) "ویرایش دارایی برند" else "ثبت دارایی یا راهنمای تولید جدید پیله", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("عنوان راهنما (مثال: شعار برند)") }, modifier = Modifier.fillMaxWidth())

                    Text("دسته‌بندی دارایی:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(brandCategories) { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = c },
                                label = { Text(c) }
                            )
                        }
                    }

                    OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("محتوا، مقدار یا راهنمای تولید") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showBrandAddDialog = false }) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (name.isNotEmpty() && value.isNotEmpty()) {
                                viewModel.saveBrandAsset(name, value, category)
                                Toast.makeText(context, "با موفقیت ثبت شد!", Toast.LENGTH_SHORT).show()
                                showBrandAddDialog = false
                            } else {
                                Toast.makeText(context, "لطفاً تمامی فیلدها را پر کنید", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("ثبت دارایی")
                        }
                    }
                }
            }
        }
    }
}

// 10. SETTINGS SCREEN
@Composable
fun SettingsScreen(viewModel: PilehViewModel) {
    val context = LocalContext.current
    val isDark = viewModel.isDarkMode

    // Backup ZIP exporter/importer triggers
    val writeBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            viewModel.triggerBackupExport(it)
        }
    }

    val readBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.triggerBackupRestore(it)
        }
    }

    // React to status changes
    val statusMsg = viewModel.backupStatusMsg
    LaunchedEffect(statusMsg) {
        if (statusMsg != null) {
            Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show()
            viewModel.backupStatusMsg = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("تنظیمات ظاهری سیستم", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تم تاریک مدرن پیله (توصیه‌شده)")
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.isDarkMode = it }
                        )
                    }
                }
            }
        }

        // BACKUPS & RESTORE (Required by prompt)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("مدیریت پشتیبان‌گیری پیله (کاملاً آفلاین)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اطلاعات سیستم شامل دیتابیس کامل SQLite و تمامی فایل‌های صوتی، عکس، ویدیو، PSD، اسناد و غیره را در قالب یک فایل فشرده منفرد ZIP (با پسوند .zip یا .pileh) صادر کنید. همچنین می‌توانید در هر زمان آن را برای بازیابی کامل بارگذاری کنید.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
                                val currentDateTime = sdf.format(Date())
                                writeBackupLauncher.launch("PILEH_BACKUP_$currentDateTime.zip")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("خروجی پشتیبان (ZIP)")
                        }

                        Button(
                            onClick = {
                                readBackupLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بازیابی فایلی")
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("شناسنامه برند پیله 🦋", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("آدرس وب‌سایت: pilehapp.ir", fontSize = 12.sp)
                    Text("اینستاگرام: @pilehapp", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "طراحی شده با عشق عمیق برای مدیران پیله. سیستم عامل داخلی شما برای مدیریت دانش و ساخت محتوای تحول‌ساز.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
