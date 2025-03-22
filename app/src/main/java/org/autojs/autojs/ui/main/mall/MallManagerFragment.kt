package org.autojs.autojs.ui.main.mall

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.stardust.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.autojs.autojs.model.script.ScriptItem
import org.autojs.autojs.model.script.ScriptModel
import org.autojs.autojs.net.HttpClient
import org.autojs.autojs.tool.BitmapTool.createImageWithInitials
import org.autojs.autojs.tool.Constant
import org.autojs.autojs.tool.FileUtils
import org.autojs.autojs.tool.GsonUtils.parseResponseToItems
import org.autojs.autojs.tool.MyLog
import org.autojs.autojs.tool.ScriptManager
import org.autojs.autojs.ui.compose.util.InvisibleSwipeRefreshIndicator
import org.autojs.autoxjs.R

val TAG: String = "MallManagerFragment"

class MallManagerFragment : Fragment() {
    private val items = mutableStateListOf<ScriptItem>()
    private var isRefreshing by mutableStateOf(false)
    private var currentPage by mutableIntStateOf(1)
    private var totalPages by mutableLongStateOf(1)
    private var isLoadingMore by mutableStateOf(false)
    private var coroutineScope: CoroutineScope? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MyLog.d("mall onCreateView")
        coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope?.cancel(null)  // 确保在视图销毁时取消协程
    }
    override fun onResume() {
        super.onResume()
        MyLog.d("Mall onResume")
        isRefreshing = false
        refreshData() // 切换到Fragment时刷新数据
        (view as? ComposeView)?.setContent {
            ItemList(items = items, isRefreshing = isRefreshing, onRefresh = { refreshData() })
        }
    }

    private fun refreshData() {
        if (isRefreshing) {
            return
        }
        isRefreshing = true
        currentPage = 1
        fetchData(currentPage)
    }

    private fun fetchData(page: Int) {
        MyLog.d("fetchData ing")
        coroutineScope?.launch {
            val json = """{"page_number": $page, "page_size": 20}"""
            try {
                withTimeout(30000) { // 30秒超时
                    val response = withContext(Dispatchers.IO) {
                        HttpClient.post("/api/scripts/list", json = json)
                    }
                    response.onSuccess {
                        val fetchedItems = parseResponseToItems(requireContext(), it)
                        if (page == 1) {
                            items.clear()
                        }
                        totalPages = fetchedItems.totalPage
                        items.addAll(fetchedItems.items)
//                        Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        MyLog.e("Network request failed: ${it.message}")
                        Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                MyLog.e("Request timeout")
                Toast.makeText(requireContext(), "Request timeout", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                MyLog.e("Network request failed: ${e.message}")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isRefreshing = false  // 确保无论请求成功还是失败，都会更新刷新状态
            }
        }
    }

    fun loadMoreData() {
        if (!isLoadingMore && currentPage < totalPages) {
            isLoadingMore = true
            currentPage++
            fetchData(currentPage)
        }
    }
}


@Composable
fun ItemList(items: List<ScriptItem>, isRefreshing: Boolean, onRefresh: () -> Unit) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh,
        indicator = { state, refreshTrigger ->
            MyLog.d("state ==> $state")
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = refreshTrigger,
                scale = true,
                contentColor = Color.Transparent //MaterialTheme.colorScheme.primary
            )
        }
        // 隐藏刷新图标
//        indicator = { state, refreshTrigger ->
//            InvisibleSwipeRefreshIndicator(
//                state = state,
//                refreshTriggerDistance = refreshTrigger
//            )
//        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                itemView(item = item, onClick = { selectedItem ->
                    println("Clicked: ${selectedItem.name}")
                })
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleItemIndex ->
                if (firstVisibleItemIndex >= items.size - 1) {
                    (context as? MallManagerFragment)?.loadMoreData()
                }
            }
    }
}

fun ScriptItem.toScriptModel(): ScriptModel {
    return ScriptModel().apply {
        name = this@toScriptModel.name
        id = this@toScriptModel.id
        desc = this@toScriptModel.desc
        logoUrl = this@toScriptModel.logoUrl
        version = this@toScriptModel.version
        updateTimestamp = this@toScriptModel.updateTimestamp
        buildNum = this@toScriptModel.buildNum
    }
}

@Composable
fun itemView(item: ScriptItem, onClick: (ScriptItem) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    var downloadState by remember { mutableIntStateOf(item.downloadState) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val surfaceColor by animateColorAsState(
        targetValue = if (isExpanded) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surface
    )

    LaunchedEffect(item.downloadState) {
        downloadState = item.downloadState
    }

    MyLog.d("itemView ==> ${item.name} -> ${item.downloadState} -> $downloadState")

//    val initials = item.name.firstOrNull()?.uppercase() ?: ""
//    val painter = if (item.logoUrl.isNullOrEmpty() || item.logoUrl == "1") {
//        rememberImagePainter(data = createImageWithInitials(initials))
//    } else {
//        rememberImagePainter(
//            data = item.logoUrl,
//            builder = {
//                crossfade(true) // 图片淡入效果
//                transformations(CircleCropTransformation())
//                // Handle error case
////                error(BitmapPainter(createImageWithInitials(initials)))
//            }
//        )
//    }
    Surface(
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 5.dp,
        modifier = Modifier
            .padding(all = 5.dp)
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberImagePainter(data = item.logoUrl),//painter,
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
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
            val imageModifier = Modifier
                .size(30.dp)
                .padding(end = 10.dp)

            when (item.downloadState) {
                Constant.DownloadStateType.toDownload -> {
                    DownloadImage(
                        imageModifier = imageModifier,
                        context = context,
                        item = item,
                        coroutineScope = coroutineScope,
                    )
                }
                Constant.DownloadStateType.downloading -> {
                    IconImage(
                        painterResource(id = R.drawable.ic_file_download_black_48dp),
                        contentDescription = "downloading",
                        modifier = imageModifier
                    )
                }
                Constant.DownloadStateType.toUpdate -> {
                    UpdateImage(
                        imageModifier = imageModifier,
                        context = context,
                        item = item,
                        coroutineScope = coroutineScope,
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadImage(
    imageModifier: Modifier,
    context: Context,
    item: ScriptItem,
    coroutineScope: CoroutineScope,
) {
    Image(
        painterResource(id = R.drawable.download),
        contentDescription = "download",
        modifier = imageModifier.clickable {
            toast(context, "downloading ${item.name}")
            item.downloadState = Constant.DownloadStateType.downloading
            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) {
                    HttpClient.download(item.url, FileUtils.getDefaultScriptPath() + "${item.id}.js")
                }
                if (success) {
                    item.downloadState = Constant.DownloadStateType.downloaded
                    ScriptManager.add(item.toScriptModel())
                }
            }
        }
    )
}

@Composable
fun UpdateImage(
    imageModifier: Modifier,
    context: Context,
    item: ScriptItem,
    coroutineScope: CoroutineScope,
) {
    Image(
        painterResource(id = R.drawable.ic_update),
        contentDescription = "updating",
        modifier = imageModifier.clickable {
            toast(context, "updating ${item.name}")
            item.downloadState = Constant.DownloadStateType.downloading
            coroutineScope.launch {
                val success = withContext(Dispatchers.IO) {
                    HttpClient.download(item.url, FileUtils.getDefaultScriptPath() + "${item.id}.js")
                }
                if (success) {
                    item.downloadState = Constant.DownloadStateType.downloaded
                    ScriptManager.add(item.toScriptModel())
                }
            }
        }

    )
}

@Composable
fun IconImage(painter: Painter, contentDescription: String?, modifier: Modifier) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val items = remember { mutableStateListOf<ScriptItem>() }
    items.addAll(
        listOf(
            ScriptItem(
                1,
                "Script1",
                "url1",
                "logoUrl1",
                "This is a test script1, you can touch it to see the details, or touch the download icon to download."
            ),
            ScriptItem(2, "Script2", "url2", "logoUrl2", "This is a test script2"),
            ScriptItem(3, "Script3", "url3", "logoUrl3", "This is a test script3"),
        )
    )
    ItemList(items = items, isRefreshing = false, onRefresh = {})
}