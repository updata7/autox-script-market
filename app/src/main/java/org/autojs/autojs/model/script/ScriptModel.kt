package org.autojs.autojs.model.script

import com.google.gson.Gson
import org.autojs.autojs.storage.database.BaseModel


class ScriptModel : BaseModel() {
    var desc: String? = null    // 描述
    var version: String? = null // 版本信息
    var name: String? = null    // 脚本名
    var logoUrl: String? = null     // logo 链接
    var updateTimestamp: Long? = 0L  // 作者更新脚本时间
    var buildNum: Long? = 0L

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
    companion object {
        const val TABLE = "as_script"
    }
}