package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspirations")
data class InspirationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val source: String,
    val sourceLink: String,
    val tags: List<String> = emptyList(),
    val dateCreated: Long = System.currentTimeMillis(),
    val attachments: List<String> = emptyList(), // stores relative names of copied files
    val status: String // "خام", "بررسی شده", "تبدیل به ایده"
)

@Entity(tableName = "ideas")
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val subject: String, // "آگاهی", "ابزار", "جذب ممبر", "لید مگنت", "ویترینی"
    val priority: String, // "کم", "متوسط", "زیاد"
    val dateCreated: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val linkedInspirationIds: List<Int> = emptyList() // linked Inspirations
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "آگاهی", "ابزار", "جذب ممبر", "لید مگنت", "ویترینی"
    val caption: String,
    val hashtags: List<String> = emptyList(),
    val cta: String,
    val publishDate: String, // "1405-03-24" etc.
    val publishTime: String, // "18:00"
    val status: String, // "ایده", "در حال تولید", "در حال طراحی", "آماده انتشار", "زمان بندی شده", "منتشر شده", "آرشیو شده"
    val attachments: List<String> = emptyList(),
    // Checklist
    val checklistDesignCompleted: Boolean = false,
    val checklistCaptionWritten: Boolean = false,
    val checklistHashtagsReady: Boolean = false,
    val checklistCoverReady: Boolean = false,
    val checklistSchedulingDone: Boolean = false,
    val checklistPublished: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val assignee: String,
    val dueDate: String, // Date string
    val priority: String, // "کم", "متوسط", "زیاد"
    val status: String // "باز", "در حال انجام", "انجام شده", "لغو شده"
)

@Entity(tableName = "wikis")
data class WikiEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "تصمیمات", "استراتژی‌ها", "فرآیندها", "جلسات", "قوانین", "اسناد مهم"
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "brand_assets")
data class BrandAssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val value: String, // content or hex or file name
    val category: String // "رنگ‌ها", "لوگوها", "فونت‌ها", "هویت بصری", "راهنمای تولید", "غیره"
)
