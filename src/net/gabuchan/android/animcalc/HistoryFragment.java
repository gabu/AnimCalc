
package net.gabuchan.android.animcalc;

import java.math.BigDecimal;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

        // ListViewを取得して
        ListView listView = getListView();
        // タップされたときのリスナーをセット
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // 履歴の文字列を取得して
                String history = parent.getItemAtPosition(position).toString();
                // 文字列のうしろから半角スペースの位置を検索
                int index = history.lastIndexOf(" ");
                // 最後のスペース以降の文字列(=の後の数字)を取得
                String string = history.substring(index + 1);
                // 数字の文字列からBigDecimalインスタンスを作って
                BigDecimal value = new BigDecimal(string);
                // アクティビティのsetCalcValue()メソッドを使って
                // 値をセット!
                ((MainActivity) getActivity()).setCalcValue(value);
            }
        });
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
