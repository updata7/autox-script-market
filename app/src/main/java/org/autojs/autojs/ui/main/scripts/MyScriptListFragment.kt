package org.autojs.autojs.ui.main.scripts

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.mutableIntSetOf
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.autojs.autojs.model.script.Scripts.run
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.stardust.app.GlobalAppContext.getString
import com.stardust.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.autojs.autojs.model.script.ScriptFile
import org.autojs.autojs.model.script.ScriptItem
import org.autojs.autojs.model.script.ScriptModel
import org.autojs.autojs.model.script.Scripts.stop
import org.autojs.autojs.tool.FileUtils
import org.autojs.autojs.tool.MyLog
import org.autojs.autojs.tool.ScriptManager
import org.autojs.autojs.ui.compose.util.InvisibleSwipeRefreshIndicator
import org.autojs.autojs.ui.main.mall.MallManagerFragment
import org.autojs.autoxjs.R
import java.io.File


val TAG: String = "MallManagerFragment"
class MyScriptListFragment : Fragment() {
    private val items = mutableStateListOf<ScriptItem>()
    private var isRefreshing by mutableStateOf(false)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView ==> ")
        // 复制整个文件夹
        FileUtils.copyAssets(
            "sample/res",
            Environment.getExternalStorageDirectory().absolutePath + getString(R.string.default_value_script_dir_path) + "res"
        )
        return ComposeView(requireContext()).apply {
            // 确保在视图树生命周期销毁时，视图的 Composition 也会被正确地清理。
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
    }

    override fun onResume() {
        super.onResume()
        MyLog.d("myscript onResume")
        isRefreshing = false
        refreshData()
        (view as? ComposeView)?.setContent {
            ItemList(items = items, isRefreshing = isRefreshing, onRefresh = { refreshData() })
        }
    }

    // 刷新数据
    fun refreshData() {
        MyLog.d("script refresh")
        if (isRefreshing) {
            return
        }
        isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val newItems = loadJsFilesFromDirectory()
                MyLog.d("newItems size ==> ${newItems.size}")

                if (newItems.isNotEmpty()) {
                    items.clear()
                    items.addAll(newItems)
                }
            } catch (e: Exception) {
                MyLog.e("Failed to refresh data: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

    private fun loadJsFilesFromDirectory(): List<ScriptItem> {
        val directory = File(Environment.getExternalStorageDirectory(),
            getString(R.string.default_value_script_dir_path))
        val files = directory.listFiles { file -> file.isFile && file.extension == "js" }
        val ids = mutableListOf<Int>()
        val newItems = mutableStateListOf<ScriptItem>()
        files?.map { file ->
            try {
                val id: Int = file.nameWithoutExtension.toInt()
                ids.add(id)
            } catch (e: Exception) {
                newItems.add(
                    ScriptItem(
                        id = 10001,
                        name = file.nameWithoutExtension,
                        url = file.absolutePath,  // 绝对路径
                        logoUrl = "res/logo.png",  // 可选的 logoUrl
                        desc = "Description for ${file.nameWithoutExtension}",  // 可选的描述
                    )
                )
            }

        }
        if (ids.isNotEmpty()) {
            // 获取信息
            val items2: List<ScriptModel> = ScriptManager.loadByIds(ids)
            MyLog.d("items2 ==> $items2")
            items2.map {scriptModel ->
                val scriptItem = ScriptItem(
                    id = scriptModel.id,
                    name = scriptModel.name ?: "Unknown",  // 默认值以防 name 为 null
                    url = FileUtils.getDefaultScriptPath() + "${scriptModel.id}.js",
                    desc = scriptModel.desc ?: "No Description",  // 默认值以防 desc 为 null
                    logoUrl = scriptModel.logoUrl ?: "res/logo.png",  // 默认值以防 logoUrl 为 null
                    version = scriptModel.version ?: "Unknown",  // 默认值以防 version 为 null
                    runstate = 0
                )
                newItems.add(scriptItem)
            }
            ScriptManager.deleteNotInIds(ids)
        } else {
            // 没有脚本 则清空数据库
            ScriptManager.deleteAll()
        }
        return newItems;
    }
}

@Composable
fun ItemList(items: MutableList<ScriptItem>, isRefreshing: Boolean, onRefresh: () -> Unit) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh,
        // 隐藏刷新图标
        indicator = { state, refreshTrigger ->
            InvisibleSwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = refreshTrigger
            )
        }
//        indicator = { state, refreshTrigger ->
//            SwipeRefreshIndicator(
//                state = state,
//                refreshTriggerDistance = refreshTrigger,
//                scale = true,
//                contentColor = MaterialTheme.colorScheme.primary
//            )
//        }
    ) {
        if (items.isEmpty()) {
            // Show a message when the list is empty
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = getString(R.string.text_when_my_script_empty),
                    color = Color(0xFFA9A9A9),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    itemView(item = item, onItemDeleted = { selectedItem ->
                        ScriptManager.deleteById(selectedItem.id)
                        items.remove(selectedItem)
                    })
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    item: ScriptItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(getString(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(getString(R.string.cancel))
            }
        },
        title = { Text("Delete File") },
        text = { Text(getString(R.string.text_are_you_sure_to_delete, item.name)) }
    )
}

@Composable
fun itemView(item: ScriptItem, onItemDeleted: (ScriptItem) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // 有动画效果
    val surfaceColor by animateColorAsState(
        targetValue = if (isExpanded) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surface
    )

    fun deleteFile() {
        MyLog.d("deleteFile click")
        val file = File(item.url)
        if (file.exists()) {
            if (file.delete()) {
                MyLog.d("File deleted: ${item.url}")
                onItemDeleted(item)
            } else {
                MyLog.e("Failed to delete file: ${item.url}")
                toast(context, "Failed to delete file.")
            }
        }
    }
    Surface (
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 5.dp,
        modifier = Modifier
            .padding(all = 5.dp)
            .fillMaxWidth()
            .clickable {
                isExpanded = !isExpanded
            },
//        color = surfaceColor
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // 子项之间的距离最大化
        ) {
            Row(
                Modifier
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberImagePainter(data = item.logoUrl),
                    contentDescription = "url",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.secondary, shape = CircleShape)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.name,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "v${item.version} | ${item.desc}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        // Composable 大小动画效果
                        modifier = Modifier.animateContentSize()
                    )
                }

                Image(
                    painterResource(
                        id = if (item.runState == 0) R.drawable.ic_run else R.drawable.ic_stop
                    ),
                    contentDescription = if (item.runState == 0) "run" else "stop",
                    modifier = Modifier
                        .size(30.dp)
                        .padding(end = 8.dp)
                        .clickable {
                            if (item.runState == 0) {
                                toast(context, "run: ${item.name}")
                                item.runState = 1  // 正在运行
                                run(ScriptFile(item.url))
                            } else {
                                toast(context, "stop: ${item.name}")
                                item.runState = 0  // 停止
                                stop()
                            }
                        }
                )
            }
            Image(
                painterResource(id = R.drawable.ic_delete),
                contentDescription = "delete",
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 10.dp)
                    .clickable {
                        showDialog = true
                    }
            )
        }
    }
    if (showDialog) {
        DeleteConfirmationDialog(
            item = item,
            onConfirm = {
                showDialog = false
                deleteFile()
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
//    val items = remember { mutableStateListOf<ScriptItem>() }
//    var isRefreshing by  remember { mutableStateOf(false) }
//    items.addAll(listOf(
//        ScriptItem(1,"Script1", "url1", "logoUrl1", "This is a test script1, you can touch it to see the details, or touch the download icon to download.", "1.0"),
//        ScriptItem(2,"Script2", "url2", "logoUrl2", "This is a test script2", "1.1"),
//        ScriptItem(3,"Script3", "url3", "logoUrl3", "This is a test script3", "1.2"),
//    ))
//
//    fun onRefresh() {
//
//    }
//
//    ItemList(items = items, isRefreshing = isRefreshing, onRefresh = { onRefresh() } )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Haven’t downloaded the script yet? \nGo discover more features!\n\n" +
                    "Click on the script market, find the script that suit you, \nand make the app even better!",
            color = Color(0xFFA9A9A9),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
