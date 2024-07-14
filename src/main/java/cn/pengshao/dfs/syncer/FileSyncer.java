package cn.pengshao.dfs.syncer;

import java.io.File;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/7/14 21:36
 */
public interface FileSyncer {

    boolean sync(File file, String backupUrl, boolean sync);

}
