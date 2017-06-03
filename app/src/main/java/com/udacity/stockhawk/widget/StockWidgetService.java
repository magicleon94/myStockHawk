package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by magicleon on 29/05/17.
 */

public class StockWidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return  new RemoteViewsFactory() {
            Cursor data;
            private  DecimalFormat dollarFormatWithPlus;
            private  DecimalFormat dollarFormat;
            private  DecimalFormat percentageFormat;

            @Override
            public void onCreate() {
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null){
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(Contract.Quote.URI,
                                                  Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                                                  null,
                                                  null,
                                                  Contract.Quote.COLUMN_SYMBOL);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null){
                    data.close();
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) return null;

                RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.list_item_widget);
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);
                String price = dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE));

                remoteViews.setTextViewText(R.id.symbol,symbol);
                remoteViews.setTextViewText(R.id.price,price);
                if (rawAbsoluteChange<0){
                    remoteViews.setInt(R.id.change,"setBackgroundResource",R.drawable.percent_change_pill_red);
                }else{
                    remoteViews.setInt(R.id.change,"setBackgroundResource",R.drawable.percent_change_pill_green);
                }
                if (PrefUtils.getDisplayMode(StockWidgetService.this).equals(getString(R.string.pref_display_mode_absolute_key))){
                    remoteViews.setTextViewText(R.id.change,change);
                }else{
                    remoteViews.setTextViewText(R.id.change,percentage);
                }

                final Intent fillIntent = new Intent();
                fillIntent.putExtra("SYMBOL",symbol);
                remoteViews.setOnClickFillInIntent(R.id.widget_list_item,fillIntent);
                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)){
                    return  data.getLong(Contract.Quote.POSITION_ID);
                }
                return  position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
