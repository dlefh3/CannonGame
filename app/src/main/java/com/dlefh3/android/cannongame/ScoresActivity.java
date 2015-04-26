package com.dlefh3.android.cannongame;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.dlefh3.android.cannongame.score_adapter.Score;
import com.dlefh3.android.cannongame.score_adapter.ScoreAdpater;

import java.util.ArrayList;


public class ScoresActivity extends ListActivity
{
    private ScoreAdpater scoreAdpater = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        ListView lv = getListView();
        scoreAdpater = new ScoreAdpater(ScoresActivity.this);
        setListAdapter(scoreAdpater);
        generateData();


    }

    private void generateData()
    {
        LevelDatabaseHelper db = new LevelDatabaseHelper(getApplicationContext());
        ArrayList<Double> scores = db.getAllScores();
        scoreAdpater.clear();
        for(double s: scores)
        {
            Score score = new Score(s);
            scoreAdpater.add(score);
        }
        scoreAdpater.notifyDataSetChanged();

    }


}
