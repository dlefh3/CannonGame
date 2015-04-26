package com.dlefh3.android.cannongame.score_adapter;

import android.widget.TextView;

/**
 * Created by Daniel on 4/26/2015.
 */
public class ScoreHolder
{
    public TextView getScore()
    {
        return score;
    }

    public void setScore(TextView score)
    {
        this.score = score;
    }

    private TextView score;
}
