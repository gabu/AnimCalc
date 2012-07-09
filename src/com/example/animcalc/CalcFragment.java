
package com.example.animcalc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CalcFragment extends Fragment {
    // 履歴の文字列
    private String mHistory = "";

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // trueにセットしないとonCreateOptionsMenu()が呼び出されない
        setHasOptionsMenu(true);

        // レイアウトファイルからViewを作って
        View view = inflater.inflate(R.layout.fragment_calc, container, false);

        // 表示部のTextViewインスタンスを取得
        mResult = (TextView) view.findViewById(R.id.result);
        // FrameLayoutインスタンスを取得
        mFrame = (FrameLayout) view.findViewById(R.id.frame);
        // 背景用のImageViewインスタンスを取得
        mBgImage = (ImageView) view.findViewById(R.id.bg_image);

        // 数字のボタンが押された時のリスナー
        View.OnClickListener numberListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick(v);
            }
        };

        int resId;
        String pkg = getActivity().getPackageName();
        Resources r = getResources();
        for (int i = 0; i < 10; i++) {
            // idを動的に作って
            resId = r.getIdentifier("button_" + i, "id", pkg);
            // セットする
            view.findViewById(resId).setOnClickListener(numberListener);
        }

        // 「＋」「ー」「×」「÷」ボタンが押された時のリスナー
        View.OnClickListener opListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOpClick(v);
            }
        };
        view.findViewById(R.id.button_plus).setOnClickListener(opListener);
        view.findViewById(R.id.button_subtract).setOnClickListener(opListener);
        view.findViewById(R.id.button_multiply).setOnClickListener(opListener);
        view.findViewById(R.id.button_divide).setOnClickListener(opListener);

        // 「＝」ボタンが押された時のリスナー
        view.findViewById(R.id.button_equal).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onEqualClick(v);
                    }
                });

        // 「C」ボタンが押された時のリスナー
        view.findViewById(R.id.button_clear).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClearClick(v);
                    }
                });

        // 小数点ボタンが押されたときのリスナー
        view.findViewById(R.id.button_decimal).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDecimalClick(v);
                    }
                });

        // Viewを返す
        return view;
    }

    private void setBgImage(Uri uri) {
        // 正しく回転されたBitmapを取得
        Bitmap bitmap = BitmapUtils.decodeUri(getActivity(), uri, 800);
        // 背景用のImageViewにBitmapをセット
        mBgImage.setImageBitmap(bitmap);
        // フェードイン!
        Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        mBgImage.startAnimation(a);
    }

    @Override
    public void onResume() {
        super.onResume();
        // SoundPoolインスタンスを生成
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        // 音声ファイルをロードしておく
        mSoundIds[0] = mSoundPool.load(getActivity(), R.raw.sound_0, 1);
        mSoundIds[1] = mSoundPool.load(getActivity(), R.raw.sound_1, 1);
        mSoundIds[2] = mSoundPool.load(getActivity(), R.raw.sound_2, 1);
        mSoundIds[3] = mSoundPool.load(getActivity(), R.raw.sound_3, 1);
        mSoundIds[4] = mSoundPool.load(getActivity(), R.raw.sound_4, 1);
        mSoundIds[5] = mSoundPool.load(getActivity(), R.raw.sound_5, 1);
        mSoundIds[6] = mSoundPool.load(getActivity(), R.raw.sound_6, 1);
        mSoundIds[7] = mSoundPool.load(getActivity(), R.raw.sound_7, 1);
        mSoundIds[8] = mSoundPool.load(getActivity(), R.raw.sound_8, 1);
        mSoundIds[9] = mSoundPool.load(getActivity(), R.raw.sound_9, 1);

        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // 保存されている背景のUri文字列を取得
        String uri = sp.getString(SettingsActivity.KEY_BACKGROUND, null);
        if (uri != null) {
            // nullじゃなかったら背景を設定する
            setBgImage(Uri.parse(uri));
        } else {
            // 背景をクリアする
            mBgImage.setImageDrawable(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // SoundPoolインスタンスを解放
        mSoundPool.release();
    }

    public void setCalcValue(BigDecimal calcValue) {
        // 計算がおかしくならないようにクリアする
        onClearClick(null);
        // 値をセット
        mCalcValue = calcValue;
        // 表示を更新
        updateResult();
    }

    public void onNumberClick(View view) {
        // 大きくするアニメーション
        scaleAnimation(view);
        // 移動するアニメーション
        translateAnimation(view);

        // ボタンのタグを取得してint型に変換
        int value = Integer.parseInt(view.getTag().toString());

        // 効果音のON/OFFを取得
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean enableSound = sp.getBoolean("sound", false);

        // 効果音を再生
        if (enableSound) {
            mSoundPool.play(mSoundIds[value], 1.0f, 1.0f, 0, 0, 1);
        }

        if (mOp == R.id.button_equal) {
            // 「=」ボタンの後の場合は、そのまま代入する
            mCalcValue = new BigDecimal(value);
            // mOp変数に他の値を入れておかないとずっと
            // この条件式に該当してしまうのでクリアする
            mOp = 0;
        } else {
            if (mOp == R.id.button_decimal || mCalcValue.scale() != 0) {
                // 小数点を含む場合
                BigDecimal bd = new BigDecimal(BigInteger.valueOf(value),
                        mCalcValue.scale() + 1);
                mCalcValue = mCalcValue.add(bd);
            } else {
                // それ以外の場合は10倍して
                mCalcValue = mCalcValue.multiply(BigDecimal.TEN); // 足す
                mCalcValue = mCalcValue.add(new BigDecimal(value));
            }
        }
    }

    public void onOpClick(View view) {
        // ボタンの文字を取得
        String op = stringByViewId(view.getId());
        // 履歴の文字列に連結して追加
        mHistory += String.format("%s %s ", format(mCalcValue), op);

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
        // 計算する前に履歴用に値をとっておきます
        BigDecimal last = mCalcValue;

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

        // viewがnullではなくて、かつ、「＝」の連打ではない場合
        if (view != null && mOp != R.id.button_equal) {
            // 履歴の文字列に連結して追加
            mHistory += String.format("%s = %s",
                    format(last), format(mCalcValue));
            // アクティビティのメソッドで履歴を追加!
            ((MainActivity) getActivity()).addHistory(mHistory);
            // 履歴の文字列を空文字にしておく
            mHistory = "";
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

    public void onDecimalClick(View view) {
        // 小数点ボタンのidを入れておくだけ
        mOp = view.getId();
    }

    private String stringByViewId(int id) {
        switch (id) {
            case R.id.button_plus:
                return "+";
            case R.id.button_subtract:
                return "-";
            case R.id.button_multiply:
                return "×";
            case R.id.button_divide:
                return "÷";
            default:
                return "";
        }
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
        Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
        view.startAnimation(a);
    }

    private void translateAnimation(View view) {
        // 飛ばすTextViewインスタンスを生成
        final TextView textView = new TextView(getActivity());
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
