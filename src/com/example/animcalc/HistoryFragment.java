
package com.example.animcalc;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

public class HistoryFragment extends ListFragment {
    private ArrayAdapter<String> mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // データが空のときに表示する文字列を追加
        setEmptyText("まだ履歴はありません。");

        // ArrayAdapter<String>を作って
        mAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1);
        // ListViewにセット
        setListAdapter(mAdapter);
    }

    /**
     * 履歴を追加します。
     * 
     * @param history 履歴の文字列
     */
    public void addHistory(String history) {
        // Adapterの先頭(0番目)に追加します。
        mAdapter.insert(history, 0);
    }
}
