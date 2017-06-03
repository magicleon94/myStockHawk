package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

/**
 * Created by magicleon on 28/05/17.
 */

public class HistoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    @BindView(R.id.history_error)
    TextView history_error;

    @BindView(R.id.history_graph)
    GraphView graphView;

    String symbol;
    LineGraphSeries<DataPoint> mSeries;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSeries = new LineGraphSeries<>();
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));

        Intent callerIntent = getIntent();

        if (callerIntent.hasExtra("SYMBOL")){
            symbol = callerIntent.getStringExtra("SYMBOL");
            setTitle(String.format(getString(R.string.history_activity_title), symbol));
            Bundle args = new Bundle();
            args.putString("SYMBOL",symbol);
            getSupportLoaderManager().restartLoader(0,args,this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                new String[]{Contract.Quote.COLUMN_HISTORY},
                Contract.Quote.COLUMN_SYMBOL+"=?",
                new String[]{args.getString("SYMBOL")},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.moveToPosition(0)) {
            graphView.setVisibility(View.VISIBLE);
            history_error.setVisibility(GONE);

            String[] historyArray = data.getString(0).split("\n");
            Map<Date,Float> historyHashMap = new HashMap<Date, Float>();


            for (String entry : historyArray){
                String[] fields = entry.split(",");
                long date = Long.parseLong(fields[0]);

                float price = Float.parseFloat(fields[1]);
                historyHashMap.put(new Date(date),price);
            }


            TreeMap<Date,Float> historyTreeMap= new TreeMap<Date,Float>(historyHashMap);


            if (mSeries == null){
                mSeries = new LineGraphSeries<>();
            }

            mSeries.resetData(new DataPoint[]{});

            for (Map.Entry<Date,Float> entry : historyTreeMap.entrySet()){
                mSeries.appendData(new DataPoint(entry.getKey(),entry.getValue()),false,historyArray.length);
            }


            mSeries.setThickness(6);
            mSeries.setDrawBackground(true);


            graphView.addSeries(mSeries);
            graphView.getViewport().setMinX(historyTreeMap.firstEntry().getKey().getTime());


        }else{
            graphView.setVisibility(View.GONE);
            history_error.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        graphView.removeAllSeries();
    }
}
