package cn.pengshao.dfs.syncer;

import java.io.File;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/7/14 21:38
 */
public class FileSyncerImpl implements FileSyncer {

    HttpSyncer httpSyncer = new HttpSyncer();

    @Override
    public boolean sync(File file, String backupUrl, boolean sync) {
        if (backupUrl == null || "null".equals(backupUrl) || backupUrl.isEmpty()) {
            return false;
        }
        if (sync) {
            httpSyncer.sync(file, backupUrl, sync);
        }
        return false;
    }
}
