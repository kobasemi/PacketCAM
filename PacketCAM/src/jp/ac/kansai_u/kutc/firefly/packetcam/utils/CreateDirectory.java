package jp.ac.kansai_u.kutc.firefly.packetcam.utils;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import jp.ac.kansai_u.kutc.firefly.packetcam.R;

import java.io.File;

/**
 * SDカードにディレクトリを作成する
 * @author akasaka
 */
public class CreateDirectory {

    Resources res;

    public CreateDirectory(Resources res){
        this.res = res;
        if(createFolder())
            Log.d("CreateDirectory.java", "Create Directory Success");
        else
            Log.d("CreateDirectory.java", "Create Directory Filed...");
    }

    /**
     * 画像保存フォルダの作成
     *
     * @return 正常に作成できればtrue，できなければfalseを返す
     */
    private boolean createFolder ()
    {

        // SDカードのフォルダパスの取得
        Path.SD_PATH = Environment.getExternalStorageDirectory ().getPath ();

        // SDカードにアプリ名でフォルダを新規作成
        Path.PICFOLDER_PATH = Path.SD_PATH + File.separator + res.getString(R.string.app_name) + File.separator + "Pictures";
        Path.PACKETFOLDER_PATH = Path.SD_PATH + File.separator + res.getString(R.string.app_name) + File.separator + "Packet";

        File picFile = new File (Path.PICFOLDER_PATH);
        File pacFile = new File (Path.PACKETFOLDER_PATH);

        try
        {
            if (!picFile.exists ())
            {
                picFile.mkdirs ();
            }
            if (!pacFile.exists())
            {
                pacFile.mkdirs();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return false;
        }
        return true;
    }

    /**
     * SDカードが端末にマウントされているか確認するメソッド
     *
     * @param status
     *        Environment.getExternalStorageStateメソッドで取得したString型の値
     * @return マウントされていればtrue, マウントされていなければfalseが返される
     */
    //TODO: SDカードがマウントされているか，都度都度確認しないなら不必要
    private boolean isSdCardMounted (String status)
    {
        return status.equals (Environment.MEDIA_MOUNTED);
    }
}
