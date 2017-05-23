package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.util.DateAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by miltomasz on 19/05/17.
 */

public class StockGraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final java.lang.String STOCK_SYMBOL = "symbol";
    private static final int SELECTED_STOCK_LOADER = 1;

    @BindView(R.id.chart)
    LineChart lineChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_graph);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(STOCK_SYMBOL)) {
            Bundle loaderBundle = new Bundle();
            loaderBundle.putString(STOCK_SYMBOL, intent.getStringExtra(STOCK_SYMBOL));
            getSupportLoaderManager().initLoader(SELECTED_STOCK_LOADER, loaderBundle, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String symbol = args.getString(STOCK_SYMBOL);
        return new CursorLoader(this,
                Contract.Quote.URI,
                null,
                Contract.Quote.COLUMN_SYMBOL + "=?",
                new String[] {symbol},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int symbolIndex = data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            String symbol = data.getString(symbolIndex);
            int historyIndex = data.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
            String history = data.getString(historyIndex);

            List<Entry> entries = new ArrayList<Entry>();

            String[] stocks = (String[]) ArrayHandle.reverse(history.split("\n"));

            long firstDate = Long.parseLong(stocks[0].split(", ")[0]);
            for (int i = 0; i < stocks.length; i++) {
                String[] dateCloseValue = stocks[i].split(", ");
                if (isCorrectEntry(dateCloseValue)) {
                    long dateInLong = Long.parseLong(dateCloseValue[0]) - firstDate;
                    float valueCloseInFloat = Float.parseFloat(dateCloseValue[1]);
                    entries.add(new Entry(dateInLong, valueCloseInFloat));
                }
            }

            IAxisValueFormatter xAxisFormatter = new DateAxisValueFormatter(firstDate);
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(xAxisFormatter);
            LineDataSet dataSet = new LineDataSet(entries, "Stock: " + symbol);
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate();
        }
    }

    public static class ArrayHandle {
        public static Object[] reverse(Object[] arr) {
            List<Object> list = Arrays.asList(arr);
            Collections.reverse(list);
            return list.toArray();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private boolean isCorrectEntry(String[] dateCloseValue) {
        return dateCloseValue.length == 2;
    }
}
