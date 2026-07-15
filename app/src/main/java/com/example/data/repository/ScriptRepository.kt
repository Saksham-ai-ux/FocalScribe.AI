package com.example.data.repository

import com.example.data.database.ScriptDao
import com.example.data.database.ScriptEntity
import kotlinx.coroutines.flow.Flow

class ScriptRepository(private val scriptDao: ScriptDao) {
    val allScripts: Flow<List<ScriptEntity>> = scriptDao.getAllScripts()

    suspend fun getScriptById(id: Int): ScriptEntity? {
        return scriptDao.getScriptById(id)
    }

    suspend fun insertScript(script: ScriptEntity): Long {
        return scriptDao.insertScript(script)
    }

    suspend fun updateScript(script: ScriptEntity) {
        scriptDao.updateScript(script)
    }

    suspend fun deleteScript(script: ScriptEntity) {
        scriptDao.deleteScript(script)
    }

    suspend fun deleteScriptById(id: Int) {
        scriptDao.deleteScriptById(id)
    }
}
