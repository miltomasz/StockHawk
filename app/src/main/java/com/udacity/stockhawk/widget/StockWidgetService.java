package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by miltomasz on 21/05/17.
 */

public class StockWidgetService extends RemoteViewsService {

    private static final String[] STOCK_HAWK_COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_HISTORY
    };

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI,
                        STOCK_HAWK_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION
                        || data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                Float price = data.getFloat(Contract.Quote.POSITION_PRICE);
                Float change = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setMaximumFractionDigits(2);
                dollarFormatWithPlus.setMinimumFractionDigits(2);
                dollarFormat.setMaximumFractionDigits(2);
                dollarFormat.setMinimumFractionDigits(2);
                if (change > 0) {
                    dollarFormatWithPlus.setPositivePrefix("+");
                    remoteViews.setInt(R.id.change, "setBackgroundColor", Color.GREEN);
                } else {
                    dollarFormatWithPlus.setNegativePrefix("-");
                    remoteViews.setInt(R.id.change, "setBackgroundColor", Color.RED);
                }

                remoteViews.setTextViewText(R.id.symbol, symbol);
                remoteViews.setTextViewText(R.id.price, dollarFormat.format(price));
                remoteViews.setTextViewText(R.id.change, dollarFormatWithPlus.format(change));
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return data.moveToPosition(position)
                        ? data.getLong(Contract.Quote.POSITION_ID) : position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
