package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InspirationDao {
    @Query("SELECT * FROM inspirations ORDER BY dateCreated DESC")
    fun getAllInspirations(): Flow<List<InspirationEntity>>

    @Query("SELECT * FROM inspirations WHERE id = :id")
    suspend fun getInspirationById(id: Int): InspirationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspiration(inspiration: InspirationEntity): Long

    @Update
    suspend fun updateInspiration(inspiration: InspirationEntity)

    @Delete
    suspend fun deleteInspiration(inspiration: InspirationEntity)

    @Query("DELETE FROM inspirations WHERE id = :id")
    suspend fun deleteInspirationById(id: Int)
}

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas ORDER BY dateCreated DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE id = :id")
    suspend fun getIdeaById(id: Int): IdeaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity): Long

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Delete
    suspend fun deleteIdea(idea: IdeaEntity)

    @Query("DELETE FROM ideas WHERE id = :id")
    suspend fun deleteIdeaById(id: Int)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY publishDate ASC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: Int): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePostById(id: Int)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Dao
interface WikiDao {
    @Query("SELECT * FROM wikis ORDER BY dateCreated DESC")
    fun getAllWikis(): Flow<List<WikiEntity>>

    @Query("SELECT * FROM wikis WHERE id = :id")
    suspend fun getWikiById(id: Int): WikiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWiki(wiki: WikiEntity): Long

    @Update
    suspend fun updateWiki(wiki: WikiEntity)

    @Delete
    suspend fun deleteWiki(wiki: WikiEntity)

    @Query("DELETE FROM wikis WHERE id = :id")
    suspend fun deleteWikiById(id: Int)
}

@Dao
interface BrandAssetDao {
    @Query("SELECT * FROM brand_assets ORDER BY category ASC")
    fun getAllBrandAssets(): Flow<List<BrandAssetEntity>>

    @Query("SELECT * FROM brand_assets WHERE id = :id")
    suspend fun getBrandAssetById(id: Int): BrandAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrandAsset(brandAsset: BrandAssetEntity): Long

    @Update
    suspend fun updateBrandAsset(brandAsset: BrandAssetEntity)

    @Delete
    suspend fun deleteBrandAsset(brandAsset: BrandAssetEntity)

    @Query("DELETE FROM brand_assets WHERE id = :id")
    suspend fun deleteBrandAssetById(id: Int)
}
