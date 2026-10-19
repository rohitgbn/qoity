package com.example.quoteverse.data.repository

import com.example.quoteverse.core.model.Category
import com.example.quoteverse.core.result.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CategoryRepository {

    override fun getCategories(): Flow<Resource<List<Category>>> = flow {
        try {
            val snapshot = firestore.collection("categories")
                .orderBy("sortOrder", Query.Direction.ASCENDING)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val remoteCategories = snapshot.toObjects(Category::class.java)
                emit(Resource.Success(remoteCategories))
            } else {
                // Fallback to rich preconfigured categories
                emit(Resource.Success(Category.DEFAULT_CATEGORIES))
            }
        } catch (_: Exception) {
            // Offline or network error fallback
            emit(Resource.Success(Category.DEFAULT_CATEGORIES))
        }
    }
}
