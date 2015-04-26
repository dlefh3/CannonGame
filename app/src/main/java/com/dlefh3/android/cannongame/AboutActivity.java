package com.dlefh3.android.cannongame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class AboutActivity extends ActionBarActivity
{
    private Button okayButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        SharedPreferences howToPlay = getSharedPreferences(getString(R.string.pref_file), 0);
        SharedPreferences.Editor editor = howToPlay.edit();
        editor.putBoolean(getString(R.string.how_key), false);
        editor.commit();
        okayButton = (Button) findViewById(R.id.button);
        okayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(AboutActivity.this, MainActivity.class);

                //Bring the activity to the front instead of making a new one
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });
    }



}
