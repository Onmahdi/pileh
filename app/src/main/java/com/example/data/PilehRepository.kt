package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.flow.first

class PilehRepository(
    private val context: Context,
    val inspirationDao: InspirationDao,
    val ideaDao: IdeaDao,
    val postDao: PostDao,
    val taskDao: TaskDao,
    val wikiDao: WikiDao,
    val brandAssetDao: BrandAssetDao
) {
    // Flows
    val allInspirations: Flow<List<InspirationEntity>> = inspirationDao.getAllInspirations()
    val allIdeas: Flow<List<IdeaEntity>> = ideaDao.getAllIdeas()
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allWikis: Flow<List<WikiEntity>> = wikiDao.getAllWikis()
    val allBrandAssets: Flow<List<BrandAssetEntity>> = brandAssetDao.getAllBrandAssets()

    // File Management
    fun getAssetsDirectory(): File {
        val dir = File(context.filesDir, "pileh_assets")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getFileByName(fileName: String): File {
        return File(getAssetsDirectory(), fileName)
    }

    fun copyUriToLocal(uri: Uri): String? {
        return try {
            val originalName = getFileNameFromUri(uri) ?: "unnamed_file"
            // clean filename of bad chars
            val cleanName = originalName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val safeName = "${System.currentTimeMillis()}_$cleanName"
            val targetFile = File(getAssetsDirectory(), safeName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            safeName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = cursor.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                name = name?.substring(cut + 1)
            }
        }
        return name
    }

    // Database Actions - Inspirations
    suspend fun insertInspiration(inspiration: InspirationEntity) = inspirationDao.insertInspiration(inspiration)
    suspend fun updateInspiration(inspiration: InspirationEntity) = inspirationDao.updateInspiration(inspiration)
    suspend fun deleteInspiration(inspiration: InspirationEntity) = inspirationDao.deleteInspiration(inspiration)

    // Database Actions - Ideas
    suspend fun insertIdea(idea: IdeaEntity) = ideaDao.insertIdea(idea)
    suspend fun updateIdea(idea: IdeaEntity) = ideaDao.updateIdea(idea)
    suspend fun deleteIdea(idea: IdeaEntity) = ideaDao.deleteIdea(idea)

    // Database Actions - Posts
    suspend fun insertPost(post: PostEntity) = postDao.insertPost(post)
    suspend fun updatePost(post: PostEntity) = postDao.updatePost(post)
    suspend fun deletePost(post: PostEntity) = postDao.deletePost(post)

    // Database Actions - Tasks
    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    // Database Actions - Wikis
    suspend fun insertWiki(wiki: WikiEntity) = wikiDao.insertWiki(wiki)
    suspend fun updateWiki(wiki: WikiEntity) = wikiDao.updateWiki(wiki)
    suspend fun deleteWiki(wiki: WikiEntity) = wikiDao.deleteWiki(wiki)

    // Database Actions - BrandAssets
    suspend fun insertBrandAsset(asset: BrandAssetEntity) = brandAssetDao.insertBrandAsset(asset)
    suspend fun updateBrandAsset(asset: BrandAssetEntity) = brandAssetDao.updateBrandAsset(asset)
    suspend fun deleteBrandAsset(asset: BrandAssetEntity) = brandAssetDao.deleteBrandAsset(asset)

    // Pre-populate with initial assets on creation if database is empty
    suspend fun populateDefaults() {
        // Only run if empty
        val currentAssets = allBrandAssets.first()
        if (currentAssets.isEmpty()) {
            // Add default Brand Assets
            brandAssetDao.insertBrandAsset(BrandAssetEntity(name = "نام برند", value = "پیله", category = "هویت بصری"))
            brandAssetDao.insertBrandAsset(BrandAssetEntity(name = "وب‌سایت", value = "pilehapp.ir", category = "هویت بصری"))
            brandAssetDao.insertBrandAsset(BrandAssetEntity(name = "اینستاگرام", value = "@pilehapp", category = "هویت بصری"))
            brandAssetDao.insertBrandAsset(BrandAssetEntity(name = "رنگ سازمانی اصلی", value = "#2E7D32 (Emerald Green)", category = "رنگ‌ها"))
            brandAssetDao.insertBrandAsset(BrandAssetEntity(name = "رنگ ثانویه", value = "#FF9100 (Cocoon Gold)", category = "رنگ‌ها"))
            brandAssetDao.insertBrandAsset(BrandAssetEntity(name = "برند گاید کلی", value = "برند پیله بر بستر تحول، خودشناسی و آگاهی فردی بنا شده است. تمامی محتواها و دارایی‌های بصری باید حس رشد، ظرافت و مینیمالیسم خلاقانه را القا کنند.", category = "راهنمای تولید"))

            // Add default Wikis
            wikiDao.insertWiki(WikiEntity(
                title = "استراتژی محتوایی پیله",
                content = "استراتژی محتوایی پیله بر اساس قانون طلایی نسبت‌های ۲۰ درصدی دسته‌بندی‌ها طراحی شده است. آگاهی، ابزار معرفی، جذب ممبر، لید مگنت و در نهایت برند ویترینی. هدف رشد مستمر جامعه از طریق محتوای غنی آفلاین است.",
                category = "استراتژی‌ها"
            ))
            wikiDao.insertWiki(WikiEntity(
                title = "قوانین طراحی و لحن برند",
                content = "لحن برند پیله: صمیمی، مقتدر، آگاهی‌بخش و الهام‌بخش. از به کار بردن کلمات انگلیسی نامتعارف خودداری کنید. طراحی باید مدرن، با فضای منفی زیاد، استفاده از ترکیب فونت‌های خوانا و تم‌های جذاب تیره و روشن باشد.",
                category = "قوانین"
            ))
            
            // Add some sample inspirations/ideas to populate Dashboard elegantly on first launch
            val testInspId = inspirationDao.insertInspiration(InspirationEntity(
                title = "تمرین تمرکز ذهن از کتاب آگاهی عمیق",
                description = "یک روش ۵ مرحله‌ای برای بازگرداندن توجه به نفس در محیط شلوغ کارگاهی.",
                source = "کتاب صوتی راه ذن",
                sourceLink = "https://example.com/zen",
                tags = listOf("تمرکز", "ذهن‌آگاهی", "آموزش"),
                status = "بررسی شده"
            ))

            ideaDao.insertIdea(IdeaEntity(
                title = "تکنیک پیله تنهایی ۳ دقیقه‌ای",
                description = "ساخت یک پست ابزاری درباره ۳ دقیقه خلوت شخصی در فواصل کاری فشرده.",
                subject = "ابزار",
                priority = "متوسط",
                tags = listOf("تکنیک", "بهره‌وری", "ریلز"),
                linkedInspirationIds = listOf(testInspId.toInt())
            ))
        }
    }

    // Multi-Table ZIP Backup Exporter
    suspend fun exportBackup(targetUri: Uri): Boolean {
        return try {
            val contentResolver = context.contentResolver
            contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    
                    // Fetch data
                    val inspirations = allInspirations.first()
                    val ideas = allIdeas.first()
                    val posts = allPosts.first()
                    val tasks = allTasks.first()
                    val wikis = allWikis.first()
                    val brandAssets = allBrandAssets.first()

                    // Convert entities to simple text-based format for reliable standalone recovery
                    val converter = DatabaseConverters()
                    
                    // 1. Inspirations
                    val instText = inspirations.joinToString("\n---\n") {
                        "ID:${it.id}\nTITLE:${it.title}\nDESC:${it.description}\nSRC:${it.source}\nLINK:${it.sourceLink}\nTAGS:${converter.fromStringList(it.tags)}\nDATE:${it.dateCreated}\nSTATUS:${it.status}\nFILES:${converter.fromStringList(it.attachments)}"
                    }
                    writeZipEntry(zipOut, "inspirations.txt", instText)

                    // 2. Ideas
                    val ideasText = ideas.joinToString("\n---\n") {
                        "ID:${it.id}\nTITLE:${it.title}\nDESC:${it.description}\nSUBJ:${it.subject}\nPRI:${it.priority}\nDATE:${it.dateCreated}\nTAGS:${converter.fromStringList(it.tags)}\nFILES:${converter.fromStringList(it.attachments)}\nLINKS:${converter.fromIntList(it.linkedInspirationIds)}"
                    }
                    writeZipEntry(zipOut, "ideas.txt", ideasText)

                    // 3. Posts
                    val postsText = posts.joinToString("\n---\n") {
                        "ID:${it.id}\nTITLE:${it.title}\nCAT:${it.category}\nCAPT:${it.caption}\nTAGS:${converter.fromStringList(it.hashtags)}\nCTA:${it.cta}\nDATE:${it.publishDate}\nTIME:${it.publishTime}\nSTATUS:${it.status}\nFILES:${converter.fromStringList(it.attachments)}\n" +
                        "CK_DESIGN:${it.checklistDesignCompleted}\nCK_CAPT:${it.checklistCaptionWritten}\nCK_TAG:${it.checklistHashtagsReady}\nCK_COV:${it.checklistCoverReady}\nCK_SCH:${it.checklistSchedulingDone}\nCK_PUB:${it.checklistPublished}"
                    }
                    writeZipEntry(zipOut, "posts.txt", postsText)

                    // 4. Tasks
                    val tasksText = tasks.joinToString("\n---\n") {
                        "ID:${it.id}\nTITLE:${it.title}\nDESC:${it.description}\nASS:${it.assignee}\nDUE:${it.dueDate}\nPRI:${it.priority}\nSTATUS:${it.status}"
                    }
                    writeZipEntry(zipOut, "tasks.txt", tasksText)

                    // 5. Wikis
                    val wikisText = wikis.joinToString("\n---\n") {
                        "ID:${it.id}\nTITLE:${it.title}\nCONT:${it.content}\nCAT:${it.category}\nDATE:${it.dateCreated}"
                    }
                    writeZipEntry(zipOut, "wikis.txt", wikisText)

                    // 6. Brand Assets
                    val brandText = brandAssets.joinToString("\n---\n") {
                        "ID:${it.id}\nNAME:${it.name}\nVAL:${it.value}\nCAT:${it.category}"
                    }
                    writeZipEntry(zipOut, "brand_assets.txt", brandText)

                    // 7. Add copied media files from file system to the zip inside "media/" directory
                    val mediaDir = getAssetsDirectory()
                    mediaDir.listFiles()?.forEach { mediaFile ->
                        if (mediaFile.isFile) {
                            try {
                                zipOut.putNextEntry(ZipEntry("media/${mediaFile.name}"))
                                mediaFile.inputStream().use { input ->
                                    input.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun writeZipEntry(zipOut: ZipOutputStream, fileName: String, content: String) {
        val entry = ZipEntry(fileName)
        zipOut.putNextEntry(entry)
        zipOut.write(content.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()
    }

    // ZIP Backup Importer & Restorer
    suspend fun importBackup(sourceUri: Uri): Boolean {
        return try {
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    
                    val converter = DatabaseConverters()
                    val mediaDir = getAssetsDirectory()

                    while (entry != null) {
                        val name = entry.name
                        if (name.startsWith("media/")) {
                            // Extract file
                            val fileName = name.substring(6)
                            if (fileName.isNotEmpty()) {
                                val targetFile = File(mediaDir, fileName)
                                targetFile.outputStream().use { output ->
                                    zipIn.copyTo(output)
                                }
                            }
                        } else {
                            // Read text tables
                            val content = zipIn.readBytes().toString(Charsets.UTF_8)
                            when (name) {
                                "inspirations.txt" -> {
                                    val blocks = content.split("\n---\n").filter { it.trim().isNotEmpty() }
                                    blocks.forEach { block ->
                                        val map = parseBlock(block)
                                        val entity = InspirationEntity(
                                            title = map["TITLE"] ?: "",
                                            description = map["DESC"] ?: "",
                                            source = map["SRC"] ?: "",
                                            sourceLink = map["LINK"] ?: "",
                                            tags = converter.toStringList(map["TAGS"]),
                                            dateCreated = map["DATE"]?.toLongOrNull() ?: System.currentTimeMillis(),
                                            status = map["STATUS"] ?: "خام",
                                            attachments = converter.toStringList(map["FILES"])
                                        )
                                        inspirationDao.insertInspiration(entity)
                                    }
                                }
                                "ideas.txt" -> {
                                    val blocks = content.split("\n---\n").filter { it.trim().isNotEmpty() }
                                    blocks.forEach { block ->
                                        val map = parseBlock(block)
                                        val entity = IdeaEntity(
                                            title = map["TITLE"] ?: "",
                                            description = map["DESC"] ?: "",
                                            subject = map["SUBJ"] ?: "آگاهی",
                                            priority = map["PRI"] ?: "متوسط",
                                            dateCreated = map["DATE"]?.toLongOrNull() ?: System.currentTimeMillis(),
                                            tags = converter.toStringList(map["TAGS"]),
                                            attachments = converter.toStringList(map["FILES"]),
                                            linkedInspirationIds = converter.toIntList(map["LINKS"])
                                        )
                                        ideaDao.insertIdea(entity)
                                    }
                                }
                                "posts.txt" -> {
                                    val blocks = content.split("\n---\n").filter { it.trim().isNotEmpty() }
                                    blocks.forEach { block ->
                                        val map = parseBlock(block)
                                        val entity = PostEntity(
                                            title = map["TITLE"] ?: "",
                                            category = map["CAT"] ?: "آگاهی",
                                            caption = map["CAPT"] ?: "",
                                            hashtags = converter.toStringList(map["TAGS"]),
                                            cta = map["CTA"] ?: "",
                                            publishDate = map["DATE"] ?: "",
                                            publishTime = map["TIME"] ?: "",
                                            status = map["STATUS"] ?: "ایده",
                                            attachments = converter.toStringList(map["FILES"]),
                                            checklistDesignCompleted = map["CK_DESIGN"]?.toBoolean() ?: false,
                                            checklistCaptionWritten = map["CK_CAPT"]?.toBoolean() ?: false,
                                            checklistHashtagsReady = map["CK_TAG"]?.toBoolean() ?: false,
                                            checklistCoverReady = map["CK_COV"]?.toBoolean() ?: false,
                                            checklistSchedulingDone = map["CK_SCH"]?.toBoolean() ?: false,
                                            checklistPublished = map["CK_PUB"]?.toBoolean() ?: false
                                        )
                                        postDao.insertPost(entity)
                                    }
                                }
                                "tasks.txt" -> {
                                    val blocks = content.split("\n---\n").filter { it.trim().isNotEmpty() }
                                    blocks.forEach { block ->
                                        val map = parseBlock(block)
                                        val entity = TaskEntity(
                                            title = map["TITLE"] ?: "",
                                            description = map["DESC"] ?: "",
                                            assignee = map["ASS"] ?: "",
                                            dueDate = map["DUE"] ?: "",
                                            priority = map["PRI"] ?: "متوسط",
                                            status = map["STATUS"] ?: "باز"
                                        )
                                        taskDao.insertTask(entity)
                                    }
                                }
                                "wikis.txt" -> {
                                    val blocks = content.split("\n---\n").filter { it.trim().isNotEmpty() }
                                    blocks.forEach { block ->
                                        val map = parseBlock(block)
                                        val entity = WikiEntity(
                                            title = map["TITLE"] ?: "",
                                            content = map["CONT"] ?: "",
                                            category = map["CAT"] ?: "استراتژی‌ها",
                                            dateCreated = map["DATE"]?.toLongOrNull() ?: System.currentTimeMillis()
                                        )
                                        wikiDao.insertWiki(entity)
                                    }
                                }
                                "brand_assets.txt" -> {
                                    val blocks = content.split("\n---\n").filter { it.trim().isNotEmpty() }
                                    blocks.forEach { block ->
                                        val map = parseBlock(block)
                                        val entity = BrandAssetEntity(
                                            name = map["NAME"] ?: "",
                                            value = map["VAL"] ?: "",
                                            category = map["CAT"] ?: "غیره"
                                        )
                                        brandAssetDao.insertBrandAsset(entity)
                                    }
                                }
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun parseBlock(block: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val lines = block.split("\n")
        lines.forEach { line ->
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.substring(0, colonIndex).trim()
                val value = line.substring(colonIndex + 1).trim()
                map[key] = value
            }
        }
        return map
    }
}
