
package com.example.animcalc;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
    // 写真を選択するREQUEST_CODE
    private static final int REQUEST_CODE_GALLERY = 1;

    // カメラで撮影するREQUEST_CODE
    private static final int REQUEST_CODE_CAMERA = 2;

    // カメラで撮影した写真のUri
    private Uri mImageUri;

    // SoundPoolインスタンス
    private SoundPool mSoundPool;

    // 効果音の数
    private static final int SOUND_COUNT = 10;

    // サウンドIDを保持する配列
    private int[] mSoundIds = new int[SOUND_COUNT];

    // UIスレッドのHandler
    private Handler mHandler = new Handler();

    // 入力中の値や計算結果
    private BigDecimal mCalcValue = BigDecimal.ZERO;

    // 四則演算を押す前に入力された値
    private BigDecimal mPreValue = BigDecimal.ZERO;

    // どの四則演算か
    private int mOp;

    // 計算結果の表示部
    private TextView mResult;

    // 飛ばす数字のTextViewを乗せるFrameLayout
    private FrameLayout mFrame;

    // 背景用のImageView
    private ImageView mBgImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 表示部のTextViewインスタンスを取得
        mResult = (TextView) findViewById(R.id.result);
        // FrameLayoutインスタンスを取得
        mFrame = (FrameLayout) findViewById(R.id.frame);
        // 背景用のImageViewインスタンスを取得
        mBgImage = (ImageView) findViewById(R.id.bg_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // res/menu/activity_main.xmlを使うように指定
        getMenuInflater().inflate(R.menu.activity_main, menu); // (1)
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                showSelectDialog();
                break;
        }
        return false;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // resultCodeがRESULT_OKではない場合は何もしない
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        // ギャラリーの呼び出しの場合
        if (requestCode == REQUEST_CODE_GALLERY) {
            // 背景画像をセットする
            setBgImage(data.getData());
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
            setBgImage(uri);
        }
    }

    private void setBgImage(Uri uri) {
        // 正しく回転されたBitmapを取得
        Bitmap bitmap = BitmapUtils.decodeUri(this, uri, 800);
        // 背景用のImageViewにBitmapをセット
        mBgImage.setImageBitmap(bitmap);
        // フェードイン!
        Animation a = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mBgImage.startAnimation(a);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // SoundPoolインスタンスを生成
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        // 音声ファイルをロードしておく
        mSoundIds[0] = mSoundPool.load(this, R.raw.sound_0, 1);
        mSoundIds[1] = mSoundPool.load(this, R.raw.sound_1, 1);
        mSoundIds[2] = mSoundPool.load(this, R.raw.sound_2, 1);
        mSoundIds[3] = mSoundPool.load(this, R.raw.sound_3, 1);
        mSoundIds[4] = mSoundPool.load(this, R.raw.sound_4, 1);
        mSoundIds[5] = mSoundPool.load(this, R.raw.sound_5, 1);
        mSoundIds[6] = mSoundPool.load(this, R.raw.sound_6, 1);
        mSoundIds[7] = mSoundPool.load(this, R.raw.sound_7, 1);
        mSoundIds[8] = mSoundPool.load(this, R.raw.sound_8, 1);
        mSoundIds[9] = mSoundPool.load(this, R.raw.sound_9, 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // SoundPoolインスタンスを解放
        mSoundPool.release();
    }

    public void onNumberClick(View view) {
        // 大きくするアニメーション
        scaleAnimation(view);
        // 移動するアニメーション
        translateAnimation(view);

        // ボタンのタグを取得してint型に変換
        int value = Integer.parseInt(view.getTag().toString());
        // 効果音を再生
        mSoundPool.play(mSoundIds[value], 1.0f, 1.0f, 0, 0, 1);

        if (mOp == R.id.button_equal) {
            // 「=」ボタンの後の場合は、そのまま代入する
            mCalcValue = new BigDecimal(value);
            // mOp変数に他の値を入れておかないとずっと
            // この条件式に該当してしまうのでクリアする
            mOp = 0;
        } else {
            // それ以外の場合は10倍して
            mCalcValue = mCalcValue.multiply(BigDecimal.TEN);
            // 足す
            mCalcValue = mCalcValue.add(new BigDecimal(value));
        }
    }

    public void onOpClick(View view) {
        // 大きくするアニメーション
        scaleAnimation(view);

        // 「=」ボタンのメソッドを呼び出して先に計算させる
        onEqualClick(null);
        // どの四則演算かidを入れておく
        mOp = view.getId();
        // 現在の値を入れておく
        mPreValue = mCalcValue;
        // 現在の値には0を入れておく
        mCalcValue = BigDecimal.ZERO;
    }

    public void onEqualClick(View view) {
        // 大きくするアニメーション
        scaleAnimation(view);

        switch (mOp) {
            case R.id.button_plus:
                // 足し算
                mCalcValue = mPreValue.add(mCalcValue);
                break;
            case R.id.button_subtract:
                // 引き算
                mCalcValue = mPreValue.subtract(mCalcValue);
                break;
            case R.id.button_multiply:
                // 掛け算
                mCalcValue = mPreValue.multiply(mCalcValue);
                break;
            case R.id.button_divide:
                // 現在の値が0ではないかチェック
                if (!BigDecimal.ZERO.equals(mCalcValue)) {
                    // 割り算
                    mCalcValue = mPreValue.divide(mCalcValue, 11,
                            BigDecimal.ROUND_HALF_UP);
                }
                break;
        }
        // イコールのidを入れておく
        mOp = R.id.button_equal;
        // 結果を表示
        updateResult();
    }

    public void onClearClick(View view) {
        // 大きくするアニメーション
        scaleAnimation(view);

        mOp = 0;
        mCalcValue = BigDecimal.ZERO;
        mPreValue = BigDecimal.ZERO;

        updateResult();
    }

    private void updateResult() {
        mResult.setText(format(mCalcValue));
    }

    private String format(BigDecimal value) {
        DecimalFormat df = new DecimalFormat(",###.###########");
        return df.format(value);
    }

    private void scaleAnimation(View view) {
        if (view == null) {
            return;
        }
        Animation a = AnimationUtils.loadAnimation(this, R.anim.scale);
        view.startAnimation(a);
    }

    private void translateAnimation(View view) {
        // 飛ばすTextViewインスタンスを生成
        final TextView textView = new TextView(this);
        // 数字の文字をセット
        textView.setText(view.getTag().toString());
        // 文字の大きさをセット
        textView.setTextSize(22);
        // 文字の色をセット
        textView.setTextColor(Color.parseColor("#ff33b5e5"));
        // FrameLayoutに追加
        mFrame.addView(textView);

        // 表示部の位置を取得
        Rect resultRect = new Rect();
        Point offset = new Point();
        mResult.getGlobalVisibleRect(resultRect, offset);
        // ステータスバーとタイトルバーの高さを取得
        int offsetY = offset.y;

        // 押されたボタンの位置を取得
        Rect buttonRect = new Rect();
        view.getGlobalVisibleRect(buttonRect);

        // 数字を飛ばす座標を計算
        // ボタンのX方向の中心座標から
        float fromX = buttonRect.centerX();
        // 表示部の右側の座標 - 表示部の右のpaddingを引いて
        float toX = resultRect.right - mResult.getPaddingRight();
        // さらに文字の横幅を計算して引いて文字の位置に飛ぶように
        toX = toX - measureText(textView);
        // ボタンのY方向の中心座標から //(ステータスバーとタイトルバーの高さを引かないとずれるので引く)
        float fromY = buttonRect.centerY() - offsetY;
        // 表示部のY方向の中心座標へ //(ステータスバーとタイトルバーの高さを引かないとずれるので引く)
        float toY = resultRect.top + mResult.getPaddingTop() - offsetY;
        // TranslateAnimationインスタンス(移動するアニメーション)を生成
        TranslateAnimation ta = new TranslateAnimation(fromX, toX, fromY,
                toY);

        // 500ミリ秒(0.5秒)で移動する
        ta.setDuration(500);
        // アニメーションが変化したときの処理をセット
        ta.setAnimationListener(new AnimationListener() {
            // アニメーションが開始したときに呼び出されるメソッド
            public void onAnimationStart(Animation animation) {
                // 今回は何もしない
            }

            // アニメーションが繰り返すときに呼び出されるメソッド
            public void onAnimationRepeat(Animation animation) {
                // 今回は何もしない
            }

            // アニメーションが終了したときに呼び出されるメソッド
            public void onAnimationEnd(Animation animation) {
                mHandler.post(new Runnable() {
                    public void run() {
                        // FrameLayoutからTextViewを削除
                        mFrame.removeView(textView);
                        // 表示部の表示を更新する
                        updateResult();
                    }
                });
            }
        });

        // アニメーション開始！
        textView.startAnimation(ta);
    }

    // TextViewの文字列のサイズを計測して返します。
    private float measureText(TextView textView) {
        Paint paint = new Paint();
        paint.setTextSize(textView.getTextSize());
        return paint.measureText(textView.getText().toString());
    }
}
