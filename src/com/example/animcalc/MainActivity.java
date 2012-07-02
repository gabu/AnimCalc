
package com.example.animcalc;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    // 入力中の値や計算結果
    private BigDecimal mCalcValue = BigDecimal.ZERO;

    // 四則演算を押す前に入力された値
    private BigDecimal mPreValue = BigDecimal.ZERO;

    // どの四則演算か
    private int mOp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onNumberClick(View view) {
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

        // 結果を表示
        updateResult();
    }

    public void onOpClick(View view) {
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
        mOp = 0;
        mCalcValue = BigDecimal.ZERO;
        mPreValue = BigDecimal.ZERO;

        updateResult();
    }

    private void updateResult() {
        TextView textView = (TextView) findViewById(R.id.textView1);
        textView.setText(format(mCalcValue));
    }

    private String format(BigDecimal value) {
        DecimalFormat df = new DecimalFormat(",###.###########");
        return df.format(value);
    }

}
