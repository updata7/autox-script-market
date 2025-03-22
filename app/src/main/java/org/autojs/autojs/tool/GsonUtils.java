package org.autojs.autojs.tool;

import static com.stardust.ToastKt.toast;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.autojs.autojs.model.script.ScriptItem;
import org.autojs.autojs.model.script.ScriptModel;
import org.autojs.autojs.net.ASResponse;
import org.autojs.autojs.net.BotItem;
import org.autojs.autojs.net.ItemListResponse;
import org.autojs.autojs.net.PageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stardust on 2017/7/21.
 */

public class GsonUtils {

    private static final String TAG = "scriptstoreX_GsonUtils";

    public static List<String> toReservedStringList(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        List<String> list = new ArrayList<>(array.size());
        for (int i = array.size() - 1; i >= 0; i--) {
            list.add(array.get(i).getAsString());
        }
        return list;
    }

    public static List<String> toStringList(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        List<String> list = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            list.add(array.get(i).getAsString());
        }
        return list;
    }

    public static ItemListResponse<ScriptItem> parseResponseToItems(Context context, String json) {
        List<ScriptItem> scriptItems = new ArrayList<>();
        long totalPage = 0L;
        try {
            Gson gson = new Gson();
            ASResponse asResponse = gson.fromJson(json, ASResponse.class);
            List<BotItem> botItems = asResponse.getData().getList();
            PageInfo pageInfo = asResponse.getData().getPage_info();
            totalPage = pageInfo.getPages();
            for (int i = 0; i < botItems.size(); i++) {
                BotItem botItem = botItems.get(i);
                ScriptModel scriptModel = ScriptManager.INSTANCE.getById(botItem.getId());
                Boolean isExist = scriptModel != null;
                int downloadState = Constant.DownloadStateType.toDownload;
                if (scriptModel != null) {
                    if (scriptModel.getBuildNum() < botItem.getBuild_number()) {
                        // 需要更新
                        downloadState = Constant.DownloadStateType.toUpdate;
                    } else {
                        downloadState = Constant.DownloadStateType.downloaded;
                    }
                }

                scriptItems.add(new ScriptItem(
                        botItem.getId(), botItem.getName(),
                        botItem.getScript_download_url(),
                        botItem.getLogo(),
                        botItem.getDescription(),
                        botItem.getVersion(),
                        botItem.getScript_update_timestamp(),
                        botItem.getBuild_number(),
                        downloadState,
                        isExist,
                        0
                ));
            }
        } catch (Exception e) {
            MyLog.d(TAG, "error ==> " + e);
            toast(context, "get data err " + e, true);
        }
        return new ItemListResponse(scriptItems, totalPage);
    }
}
