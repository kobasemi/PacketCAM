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
    private final String DIRPATH_INROOTASSETS = "cap";
    private AssetManager am;

    /**
     * res/raw/ディレクトリ配下にある全てのファイルをSDカードにコピーする
     * @param context MainActivity.this
     */
    public CopyAllPcapFileToSd(Context context){
        this.am = context.getAssets();

        try {
            for(String filename: am.list(DIRPATH_INROOTASSETS)){
//                if(CreateDirectory.createDirectory("cap"))
                    if(copyPcapFileToSd(filename))
                        Log.d(TAG, "File Copy Success: " + filename);
                    else
                        Log.d(TAG, "File Copy Failed... " + filename);
//                else
//                    Log.d(TAG, "Create Directory Failed... " + "cap");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private boolean copyPcapFileToSd(String filepath) {
        File sdPathToCopyFile = new File(Path.PACKETFOLDER_PATH + File.separator + filepath);
        InputStream  is = null;
        OutputStream os = null;

        try {
            is = am.open(DIRPATH_INROOTASSETS + File.separator + filepath);
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
            try{
                if (os != null) os.close();
                if (is != null) is.close();

                return false;
            }
            catch (Exception ee)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * res/rawにあるファイルをSDカードのPacketフォルダにコピーする
     * @param resourceId res/rawにあるコピー元のファイル（R.raw.hogeで指定）
     * @param fileName 保存先のファイル名（任意）
     * @return コピーが正常に完了したかをbooleanで返す
     */
    /*
    public boolean copyRawFileToSd(int resourceId, String fileName)
    {
        // コピー先のファイルパスを指定
        File copyFile = new File (Path.PACKETFOLDER_PATH + File.separator + fileName);
        InputStream input = res.openRawResource(resourceId);

        OutputStream output = null;

        try
        {
            output = new FileOutputStream(copyFile);
            byte[] buff = new byte[1024];
            while (input.read(buff) >= 0)
            {
                output.write(buff);
            }

            output.flush();
            output.close();
            input.close();
            output = null;
            input = null;
        }
        catch (Exception e)
        {
            try
            {
                if (output != null) output.close();
                if (input != null) input.close();

                return false;
            }
            catch (Exception ee)
            {
                return false;
            }
        }

        return true;
    }*/
}
