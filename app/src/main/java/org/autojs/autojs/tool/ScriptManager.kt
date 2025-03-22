package org.autojs.autojs.tool

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.AssetManager
import io.reactivex.Flowable
import org.autojs.autojs.App
import org.autojs.autojs.model.script.ScriptModel
import org.autojs.autojs.storage.database.ScriptDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Arrays

object ScriptManager {
    private var context: Application = App.app
    private val scriptDatabase: ScriptDatabase = ScriptDatabase(context)
    private var items: MutableList<ScriptModel> = mutableListOf()

    @SuppressLint("CheckResult")
    fun add(scriptModel: ScriptModel) {
        scriptDatabase.insertWithOnConflict(scriptModel)
        addOrUpdate(scriptModel)
    }

    fun addOrUpdate(scriptModel: ScriptModel) {
        // 查找 items 中是否存在具有相同 id 的记录
        val existingItemIndex: Int = items.indexOfFirst { it.id == scriptModel.id }

        if (existingItemIndex != -1) {
            // 如果存在，更新现有记录
            items[existingItemIndex] = scriptModel
        } else {
            // 如果不存在，插入新记录
            items.add(scriptModel)
        }
    }

    @SuppressLint("CheckResult")
    fun deleteById(id: Long) {
        scriptDatabase.deleteById(id);

        // 查找 items 中具有相同 id 的记录的索引
        val index = items.indexOfFirst { it.id == id }

        if (index != -1) {
            // 如果存在记录，移除该记录
            items.removeAt(index)
        }
    }

    fun getById(id: Long): ScriptModel? {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return null
        return items[index]
    }

    fun isExistWithId(id: Long): Boolean {
        val index = items.indexOfFirst { it.id == id }
        MyLog.d("id是否存在 : id($id) -> index($index)")
        return index != -1
    }

    fun getAll(): List<ScriptModel> {
        if (items.isNotEmpty()) return items
        items = scriptDatabase.queryAll()
        return items
    }

    fun loadByIds(ids: MutableList<Int>): List<ScriptModel> {
        if (items.isNotEmpty()) return items
        val sql = "id IN (${ids.joinToString(", ")})"
        items = (query(sql, null) ?: emptyList()).toMutableList()

        if (items.size > 0) {
            MyLog.d("ont item => ${items[0].toString()}")
        }
        return items
    }
    fun query(sql: String, vararg args: Any?): List<ScriptModel>? {
        return scriptDatabase.query2(sql)?.toList()?.blockingGet()
    }

    fun deleteNotInIds(ids: List<Int>) {
        if (ids.isEmpty()) {
            // 如果 ids 列表为空，不需要执行删除操作
            return
        }
        val sql = "DELETE FROM ${ScriptModel.TABLE} WHERE id NOT IN (${ids.joinToString(", ")})"
        MyLog.d("deleteNotInIds => $sql")
        scriptDatabase.executeSql(sql)
    }

    fun deleteAll() {
        scriptDatabase.deleteAll()
    }
}