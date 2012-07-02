
package com.example.animcalc;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 表示部のTextViewインスタンスを取得
        mResult = (TextView) findViewById(R.id.result);
        // FrameLayoutインスタンスを取得
        mFrame = (FrameLayout) findViewById(R.id.frame);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onNumberClick(View view) {
        // 大きくするアニメーション
        scaleAnimation(view);
        // 移動するアニメーション
        translateAnimation(view);

        // ボタンのタグを取得してint型に変換
        int value = Integer.parseInt(view.getTag().toString());

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
