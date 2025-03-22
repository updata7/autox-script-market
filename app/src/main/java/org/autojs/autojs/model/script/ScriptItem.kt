package org.autojs.autojs.model.script

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


data class ScriptItem(
    val id: Long, val name: String, val url: String,
    val logoUrl: String, val desc: String, val version: String = "",
    val updateTimestamp: Long = 0, val buildNum: Long = 0,
    var downloadstate: Int = 0,
    var isExist: Boolean = false,
    var runstate: Int = 0
) {
    var runState by mutableIntStateOf(runstate)
    var downloadState by mutableIntStateOf(downloadstate)
}