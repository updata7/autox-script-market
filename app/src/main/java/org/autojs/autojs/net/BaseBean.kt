package org.autojs.autojs.net

data class ASResponse(
    val code: String,
    val msg: String,
    val request_id: String,
    val data: Data
)

data class Data(
    val list: List<BotItem>,
    val page_info: PageInfo
)

data class BotItem(
    val id: Long,
    val name: String,
    val logo: String,
    val description: String,
    val script_download_url: String,
    val sort: String,
    val status: String,
    val script_update_timestamp: Long,
    val version: String,
    val build_number: Long
)

data class PageInfo(
    val rows: Long,
    val page: Long,
    val total: Long,
    val pages: Long
)

data class ItemListResponse<T>(
    val items: MutableList<T>,
    val totalPage: Long
)