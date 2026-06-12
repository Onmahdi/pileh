package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class PilehViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = PilehRepository(
        context = application,
        inspirationDao = database.inspirationDao(),
        ideaDao = database.ideaDao(),
        postDao = database.postDao(),
        taskDao = database.taskDao(),
        wikiDao = database.wikiDao(),
        brandAssetDao = database.brandAssetDao()
    )

    // Navigator & Custom Theme State State
    var activeScreen by mutableStateOf("dashboard")
    var isDarkMode by mutableStateOf(true) // defaults to Modern Dark styling
    var searchQuery by mutableStateOf("")

    // Status state
    var backupStatusMsg by mutableStateOf<String?>(null)

    // CRUD Selected Entity details (for edit dialogs or add view)
    var selectedInspiration by mutableStateOf<InspirationEntity?>(null)
    var selectedIdea by mutableStateOf<IdeaEntity?>(null)
    var selectedPost by mutableStateOf<PostEntity?>(null)
    var selectedTask by mutableStateOf<TaskEntity?>(null)
    var selectedWiki by mutableStateOf<WikiEntity?>(null)
    var selectedBrandAsset by mutableStateOf<BrandAssetEntity?>(null)

    // Base flows
    val inspirations: StateFlow<List<InspirationEntity>> = repository.allInspirations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ideas: StateFlow<List<IdeaEntity>> = repository.allIdeas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val posts: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wikis: StateFlow<List<WikiEntity>> = repository.allWikis
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brandAssets: StateFlow<List<BrandAssetEntity>> = repository.allBrandAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Content engine statistics (قانون طلایی پیله - 20% ratios)
    val contentMetrics: StateFlow<ContentMetrics> = combine(ideas, posts) { ideasList, postsList ->
        val totalCount = ideasList.size + postsList.size
        
        val categories = listOf("آگاهی", "ابزار", "جذب ممبر", "لید مگنت", "ویترینی")
        val counts = mutableMapOf<String, Int>()
        categories.forEach { counts[it] = 0 }

        ideasList.forEach {
            val key = it.subject.trim()
            if (counts.containsKey(key)) {
                counts[key] = counts[key]!! + 1
            }
        }
        postsList.forEach {
            val key = it.category.trim()
            if (counts.containsKey(key)) {
                counts[key] = counts[key]!! + 1
            }
        }

        val ratios = counts.mapValues { entry ->
            if (totalCount > 0) (entry.value.toFloat() / totalCount) * 100f else 0f
        }

        // Warnings based on deviation from golden ratio 20% (±5% threshold buffer: warning if out of 15% - 25% bracket)
        val warnings = counts.mapValues { entry ->
            val ratio = if (totalCount > 0) (entry.value.toFloat() / totalCount) * 100f else 0f
            if (totalCount >= 5) {
                if (ratio < 15f) "کمتر از نسبت طلایی ۲۰٪" else if (ratio > 25f) "بیشتر از نسبت طلایی ۲۰٪" else null
            } else {
                null // skip warning on very low sample size to avoid visual clutter
            }
        }

        ContentMetrics(
            totalCount = totalCount,
            categoryCounts = counts,
            categoryPercentages = ratios,
            warnings = warnings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContentMetrics())

    init {
        viewModelScope.launch {
            repository.populateDefaults()
        }
    }

    // Helper to get local copied files
    fun getLocalFile(fileName: String): File {
        return repository.getFileByName(fileName)
    }

    // Copying files to sandbox
    fun copyFileToSandbox(uri: Uri): String? {
        return repository.copyUriToLocal(uri)
    }

    // Database Actions - Inspirations
    fun saveInspiration(title: String, desc: String, source: String, link: String, tags: List<String>, files: List<String>, status: String) {
        viewModelScope.launch {
            val entity = selectedInspiration?.copy(
                title = title,
                description = desc,
                source = source,
                sourceLink = link,
                tags = tags,
                status = status,
                attachments = files
            ) ?: InspirationEntity(
                title = title,
                description = desc,
                source = source,
                sourceLink = link,
                tags = tags,
                status = status,
                attachments = files
            )
            if (entity.id > 0) {
                repository.updateInspiration(entity)
            } else {
                repository.insertInspiration(entity)
            }
            selectedInspiration = null
        }
    }

    fun deleteInspiration(inspiration: InspirationEntity) {
        viewModelScope.launch {
            repository.deleteInspiration(inspiration)
        }
    }

    // Database Actions - Ideas
    fun saveIdea(title: String, desc: String, category: String, priority: String, tags: List<String>, files: List<String>, linkedInspirationIds: List<Int>) {
        viewModelScope.launch {
            val entity = selectedIdea?.copy(
                title = title,
                description = desc,
                subject = category,
                priority = priority,
                tags = tags,
                attachments = files,
                linkedInspirationIds = linkedInspirationIds
            ) ?: IdeaEntity(
                title = title,
                description = desc,
                subject = category,
                priority = priority,
                tags = tags,
                attachments = files,
                linkedInspirationIds = linkedInspirationIds
            )
            if (entity.id > 0) {
                repository.updateIdea(entity)
            } else {
                repository.insertIdea(entity)
            }
            selectedIdea = null
        }
    }

    fun deleteIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            repository.deleteIdea(idea)
        }
    }

    // Database Actions - Posts & Checklist togglers
    fun savePost(
        title: String,
        category: String,
        caption: String,
        hashtags: List<String>,
        cta: String,
        pubDate: String,
        pubTime: String,
        status: String,
        files: List<String>
    ) {
        viewModelScope.launch {
            val entity = selectedPost?.copy(
                title = title,
                category = category,
                caption = caption,
                hashtags = hashtags,
                cta = cta,
                publishDate = pubDate,
                publishTime = pubTime,
                status = status,
                attachments = files
            ) ?: PostEntity(
                title = title,
                category = category,
                caption = caption,
                hashtags = hashtags,
                cta = cta,
                publishDate = pubDate,
                publishTime = pubTime,
                status = status,
                attachments = files
            )
            if (entity.id > 0) {
                repository.updatePost(entity)
            } else {
                repository.insertPost(entity)
            }
            selectedPost = null
        }
    }

    fun toggleChecklistItem(post: PostEntity, itemCode: String, value: Boolean) {
        viewModelScope.launch {
            val updated = when (itemCode) {
                "design" -> post.copy(checklistDesignCompleted = value)
                "caption" -> post.copy(checklistCaptionWritten = value)
                "hashtag" -> post.copy(checklistHashtagsReady = value)
                "cover" -> post.copy(checklistCoverReady = value)
                "schedule" -> post.copy(checklistSchedulingDone = value)
                "publish" -> post.copy(checklistPublished = value, status = if (value) "منتشر شده" else post.status)
                else -> post
            }
            repository.updatePost(updated)
        }
    }

    fun deletePost(post: PostEntity) {
        viewModelScope.launch {
            repository.deletePost(post)
        }
    }

    // Database Actions - Tasks
    fun saveTask(title: String, desc: String, assignee: String, dueDate: String, priority: String, status: String) {
        viewModelScope.launch {
            val entity = selectedTask?.copy(
                title = title,
                description = desc,
                assignee = assignee,
                dueDate = dueDate,
                priority = priority,
                status = status
            ) ?: TaskEntity(
                title = title,
                description = desc,
                assignee = assignee,
                dueDate = dueDate,
                priority = priority,
                status = status
            )
            if (entity.id > 0) {
                repository.updateTask(entity)
            } else {
                repository.insertTask(entity)
            }
            selectedTask = null
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Database Actions - Wikis
    fun saveWiki(title: String, content: String, category: String) {
        viewModelScope.launch {
            val entity = selectedWiki?.copy(
                title = title,
                content = content,
                category = category
            ) ?: WikiEntity(
                title = title,
                content = content,
                category = category
            )
            if (entity.id > 0) {
                repository.updateWiki(entity)
            } else {
                repository.insertWiki(entity)
            }
            selectedWiki = null
        }
    }

    fun deleteWiki(wiki: WikiEntity) {
        viewModelScope.launch {
            repository.deleteWiki(wiki)
        }
    }

    // Database Actions - Brand Assets
    fun saveBrandAsset(name: String, value: String, category: String) {
        viewModelScope.launch {
            val entity = selectedBrandAsset?.copy(
                name = name,
                value = value,
                category = category
            ) ?: BrandAssetEntity(
                name = name,
                value = value,
                category = category
            )
            if (entity.id > 0) {
                repository.updateBrandAsset(entity)
            } else {
                repository.insertBrandAsset(entity)
            }
            selectedBrandAsset = null
        }
    }

    fun deleteBrandAsset(asset: BrandAssetEntity) {
        viewModelScope.launch {
            repository.deleteBrandAsset(asset)
        }
    }

    // Backup Export & Restore Triggering
    fun triggerBackupExport(uri: Uri) {
        viewModelScope.launch {
            backupStatusMsg = "در حال پشتیبان‌گیری..."
            val success = repository.exportBackup(uri)
            backupStatusMsg = if (success) "فایل پشتیبان با موفقیت صادر شد." else "خطا در فرآیند پشتیبان‌گیری!"
        }
    }

    fun triggerBackupRestore(uri: Uri) {
        viewModelScope.launch {
            backupStatusMsg = "در حال بازیابی اطلاعات..."
            val success = repository.importBackup(uri)
            backupStatusMsg = if (success) "پشتیبان با موفقیت بازیابی شد." else "خطا در بازیابی اطلاعات پشتیبان!"
        }
    }

    // Scan & build dynamic directory structure
    fun getVirtualFiles(): Map<String, List<FileItem>> {
        val lists = mutableMapOf<String, MutableList<FileItem>>()
        val folderNames = listOf("Posts", "Ideas", "Inspirations", "Documents", "Videos", "Images", "Brand Assets")
        folderNames.forEach { lists[it] = mutableListOf() }

        // Compile from state
        val inspList = inspirations.value
        val ideaList = ideas.value
        val postList = posts.value
        val brandList = brandAssets.value

        fun categoriseByName(fileName: String, folderCategory: String) {
            val file = getLocalFile(fileName)
            val ext = file.extension.lowercase()
            
            // Primary folder addition
            val item = FileItem(name = fileName, originalName = fileName.substringAfter('_'), localFile = file, size = formatSize(file.length()))
            lists[folderCategory]?.add(item)

            // Categorised type folders addition
            if (ext in listOf("png", "jpg", "jpeg", "webp", "gif")) {
                lists["Images"]?.add(item)
            } else if (ext in listOf("mp4", "mkv", "avi", "mov", "3gp")) {
                lists["Videos"]?.add(item)
            } else if (ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "zip", "psd", "ai", "txt")) {
                lists["Documents"]?.add(item)
            }
        }

        inspList.forEach { insp ->
            insp.attachments.forEach { categoriseByName(it, "Inspirations") }
        }
        ideaList.forEach { id ->
            id.attachments.forEach { categoriseByName(it, "Ideas") }
        }
        postList.forEach { pt ->
            pt.attachments.forEach { categoriseByName(it, "Posts") }
        }
        brandList.forEach { asset ->
            if (asset.value.startsWith("BrandAsset_") || asset.value.substringAfterLast('.', "").isNotEmpty()) {
                val file = getLocalFile(asset.value)
                if (file.exists()) {
                    val item = FileItem(name = asset.value, originalName = asset.name, localFile = file, size = formatSize(file.length()))
                    lists["Brand Assets"]?.add(item)
                }
            }
        }

        return lists
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}

// Helper models for aggregation states
data class ContentMetrics(
    val totalCount: Int = 0,
    val categoryCounts: Map<String, Int> = emptyMap(),
    val categoryPercentage: Map<String, Float> = emptyMap(), // compatibility
    val categoryPercentages: Map<String, Float> = emptyMap(),
    val warnings: Map<String, String?> = emptyMap()
)

data class FileItem(
    val name: String,
    val originalName: String,
    val localFile: File,
    val size: String
)
