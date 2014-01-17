package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * res/raw/ディレクトリ以下にあるPcapファイルをSDカードへコピーする
 * @author akasaka
 */
public class CopyAllRawFieldToSd{
    private Resources res;

    /**
     * res/raw/ディレクトリ配下にある全てのファイルをSDカードにコピーする
     * @param context MainActivity.this
     */
    public CopyAllRawFieldToSd(Context context){
        this.res = context.getResources();

        // res/rawディレクトリ配下にある全フィールドを取得
        Field[] fields = R.raw.class.getFields();

        for(Field f: fields){
            // Field Name
            String fname = f.getName();
            // ResourceID <= (FieldName, DirectoryName, PackageName)
            int id = res.getIdentifier(fname, "raw", context.getPackageName());

            if(!copyRawFileToSd(id, fname + ".cap"))
                // ファイルコピーに失敗した場合
                Log.d("CopyAllRawFieldToSd.java", "File Copy Failed... " + fname + ".cap");
        }
    }

    /**
     * res/rawにあるファイルをSDカードのPacketフォルダにコピーする
     * @param resourceId res/rawにあるコピー元のファイル（R.raw.hogeで指定）
     * @param fileName 保存先のファイル名（任意）
     * @return コピーが正常に完了したかをbooleanで返す
     */
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
    }
}
