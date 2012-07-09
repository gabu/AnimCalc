
package com.example.animcalc;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
    // 背景設定のキー
    public static final String KEY_BACKGROUND = "background";

    // 写真を選択するREQUEST_CODE
    private static final int REQUEST_CODE_GALLERY = 1;

    // カメラで撮影するREQUEST_CODE
    private static final int REQUEST_CODE_CAMERA = 2;

    // カメラで撮影した写真のUri
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // 背景設定がタップされたとき
        findPreference(KEY_BACKGROUND).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showSelectDialog();
                        return false;
                    }
                });

        // 背景設定解除がタップされたとき
        findPreference("remove").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        removeImageUri();
                        return false;
                    }
                });
    }

    private void showSelectDialog() {
        // 選択肢の文字列
        String[] items = new String[] {
                "ギャラリーから選択", "カメラで撮影"
        };
        // ダイアログで選択されたときに呼び出されるリスナー
        OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // 「ギャラリーから選択」が選ばれた場合
                        startGallery();
                        break;
                    case 1: // 「カメラで撮影」が選ばれた場合
                        startCamera();
                        break;
                }
            }
        };
        // Builderを作って
        Builder builder = new AlertDialog.Builder(this);
        // 選択肢とリスナーをセットして
        builder.setItems(items, listener);
        // AlertDialogを作って
        AlertDialog dialog = builder.create();
        // ダイアログを表示!
        dialog.show();
    }

    private void startGallery() {
        // ギャラリーを呼び出す
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    private void startCamera() {
        // 写真のファイル名
        String filename = System.currentTimeMillis() + ".jpg";
        // 写真を保存するディレクトリ
        File dir = new File(Environment.getExternalStorageDirectory(), "AnimCalc");
        // 写真のファイルパス
        File file = new File(dir, filename); // FileからUriを生成
        mImageUri = Uri.fromFile(file);
        // カメラを呼び出す
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // resultCodeがRESULT_OKではない場合は何もしない
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        // ギャラリーの呼び出しの場合
        if (requestCode == REQUEST_CODE_GALLERY) {
            // 背景画像をセットする
            saveImageUri(data.getData());
        }

        // カメラで撮影した場合
        if (requestCode == REQUEST_CODE_CAMERA) {
            Uri uri;
            if (data == null || data.getData() == null) {
                // dataもしくはdata.getData()がnullの場合は、
                // IntentにセットしたUriを使う
                uri = mImageUri;
            } else {
                // data.getData()がnullでない場合は、それを使う
                uri = data.getData();
            }
            // 背景画像をセットする
            saveImageUri(uri);
        }
    }

    /**
     * Uriを文字列にしてプリファレンスに保存します。
     * 
     * @param uri Uriインスタンス
     */
    private void saveImageUri(Uri uri) {
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        // プリファレンスを編集するEditorを取得
        Editor editor = sp.edit();
        // Uriを文字列にしてセット
        editor.putString(KEY_BACKGROUND, uri.toString());
        // 保存!
        editor.commit();
    }

    private void removeImageUri() {
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sp.edit();
        // キーを指定して削除
        editor.remove(KEY_BACKGROUND);
        // 保存!
        editor.commit();
        // トーストを表示
        String text = "背景の設定を解除しました。";
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
