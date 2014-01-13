package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.*;

/**
 * assets/cap ディレクトリ以下の全てのファイル（ディレクトリを含む）をSDカードにコピーする
 * @author akasaka
 */
public class CopyAllPcapFileToSd {
    private final String TAG = "CopyAllPcapFileToSd";
    private final String DIRNAME_FROMASSETSROOT = "cap";  // assets/"cap" ディレクトリ
    private AssetManager am;

    /**
     * assets/capディレクトリ配下にある全てのファイルをSDカードにコピーする
     * @param context MainActivity.this
     */
    public CopyAllPcapFileToSd(Context context){
        this.am = context.getAssets();

        try {
            for(String filename: am.list(DIRNAME_FROMASSETSROOT)){
                if(isDirectory(DIRNAME_FROMASSETSROOT + File.separator + filename)){
                    if(CreateDirectory.createDirectory(filename, Path.PACKETFOLDER_PATH)){
                        for(String fnameInDir: am.list(DIRNAME_FROMASSETSROOT + File.separator + filename)){
                            if(copyPcapFileToSd(filename + File.separator + fnameInDir))
                                Log.d(TAG, "File Copy Success: " + fnameInDir);
                            else
                                Log.d(TAG, "File Copy Failed... " + fnameInDir);
                        }
                    }else
                        Log.d(TAG, "Create Directory Failed... " + filename);
                }else{
                    if(copyPcapFileToSd(filename))
                        Log.d(TAG, "File Copy Success: " + filename);
                    else
                        Log.d(TAG, "File Copy Failed... " + filename);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * assets以下のファイルがディレクトリがどうかを調べる
     * Ref: http://d.hatena.ne.jp/h_mori/20121002/1349134592
     * @param path ファイルまでのパス
     * @return ディレクトリか否か
     */
    public boolean isDirectory(final String path){
        boolean isDirectory = false;
        try {
            if (am.list(path).length > 0){
                // ディレクトリ内のファイルが0より大きい場合
                isDirectory = true;
            } else {
                // ファイル，または空のディレクトリの場合
                am.open(path);
            }
        } catch(FileNotFoundException e){
            // 空のディレクトリをオープンしたとき，この例外が出るのでそれをキャッチ
            isDirectory = true;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return isDirectory;
    }

    /**
     * assets/cap 以下にあるファイルをSDカードのPacketディレクトリにコピーする
     * @param filepath 保存元及び保存先のファイルパス
     * @return コピーが正常に完了したかをbooleanで返す
     */
    private boolean copyPcapFileToSd(String filepath) {
        File sdPathToCopyFile = new File(Path.PACKETFOLDER_PATH + File.separator + filepath);
        InputStream  is = null;
        OutputStream os = null;

        try {
            is = am.open(DIRNAME_FROMASSETSROOT + File.separator + filepath);
            os = new FileOutputStream(sdPathToCopyFile);
            byte[] buff = new byte[1024];
            while (is.read(buff) >= 0){
                os.write(buff);
            }

            os.flush();
            os.close();
            is.close();
            os = null;
            is = null;
        } catch(Exception e){
            try {
                if (os != null) os.close();
                if (is != null) is.close();

                return false;
            } catch (Exception ee){
                return false;
            }
        }

        return true;
    }
}
