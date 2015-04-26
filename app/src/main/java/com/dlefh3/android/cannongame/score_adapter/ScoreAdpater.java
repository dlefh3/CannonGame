package com.dlefh3.android.cannongame.score_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dlefh3.android.cannongame.R;

/**
 * Created by Daniel on 4/26/2015.
 */
public class ScoreAdpater extends ArrayAdapter<Score>
{

    public ScoreAdpater(Context context)
    {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ScoreHolder scoreHolder;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
            scoreHolder = new ScoreHolder();
            scoreHolder.setScore((TextView)convertView.findViewById(R.id.scoreTextView));

            convertView.setTag(scoreHolder);
        }
        else
        {
            scoreHolder = (ScoreHolder)convertView.getTag();
        }
        Score currentScore = getItem(position);
        scoreHolder.getScore().setText(String.valueOf(currentScore.getScore()));
        return convertView;
    }
}
