package com.dlefh3.android.cannongame;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class CannonGameFragment extends Fragment {
    private CannonView cannonView; // custom view to display the game


    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.fragment_cannon_game, container, false);
        setHasOptionsMenu(true);
        // get the CannonView
        cannonView = (CannonView) view.findViewById(R.id.cannonView);
        return view;
    }

    // set up volume control once Activity is created
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // allow volume keys to set game volume
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    // when MainActivity is paused, CannonGameFragment terminates the game
    @Override
    public void onPause() {
        super.onPause();
        cannonView.stopGame(); // terminates the game
    }

    // when MainActivity is paused, CannonGameFragment releases resources
    @Override
    public void onDestroy() {
        super.onDestroy();
        cannonView.releaseResources();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {

        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.game_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.clear_scores) {
            LevelDatabaseHelper databaseHelper = new LevelDatabaseHelper(getActivity().getApplicationContext());
            databaseHelper.clearScores();


            return true;
        }
        if (id == R.id.about) {
            cannonView.stopGame();
            SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.pref_file), 0);

            Intent i = new Intent(getActivity(), AboutActivity.class);
            //Bring the activity to the front instead of making a new one
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);

            return true;
        }
        if (id == R.id.scores)
        {
            Intent i = new Intent(getActivity(), ScoresActivity.class);
            //Bring the activity to the front instead of making a new one
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
} // end class CannonGameFragment