/**
 * Created by Rafael Rendon Pablo on 30/09/2016.
 */

package letmethink.mx.yamplayer;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String ERROR_TAG = "ERROR";
    private FileSystem fs = new FileSystem();
    private Random random = new Random(System.currentTimeMillis());


    private ListView songList;
    private MediaPlayer player;
    private File[] library;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songList = (ListView) findViewById(R.id.list_view);

        String path = System.getenv("SECONDARY_STORAGE") + "/music";
        new FileLoader().execute(path);

        player = new MediaPlayer();
        player.setOnPreparedListener(new PlayerPreparer());

        Button prevButton = (Button) findViewById(R.id.player_prev);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong(random.nextInt(library.length));
            }
        });

        Button playButton = (Button) findViewById(R.id.player_play_pause);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            }
        });

        Button nextButton = (Button) findViewById(R.id.player_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong(random.nextInt(library.length));
            }
        });

    }

    private void populateSongList(final File[] files) {
        library = files;
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.song_item, fs.getFileNames(files));
        songList.setAdapter(adapter);
        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playSong(position);
            }
        });
    }

    private void playSong(int position) {
        String song = library[position].getAbsolutePath();

        if (player.isPlaying()) {
            player.stop();
        }

        player.reset();

        try {
            player.setDataSource(song);
        } catch (IOException ioe) {
            Log.e(ERROR_TAG, "Failed to set payer's data source: " + ioe.getMessage());
            return;
        }

        String title = library[position].getName();
        Toast.makeText(getApplicationContext(), "Playing \"" + title + "\"", Toast.LENGTH_LONG).show();
        player.prepareAsync();
    }


    private class FileLoader extends AsyncTask<String, Void, File[]> {
        @Override
        protected File[] doInBackground(String... paths) {
            try {
                return fs.findMediaFiles(paths[0]);
            } catch (IOException ioe) {
                Log.e(ERROR_TAG, "Failed to load media files: " + ioe.getMessage());
                return new File[0];
            }
        }

        @Override
        protected void onPostExecute(File[] files) {
            populateSongList(files);
        }
    }

    private class PlayerPreparer implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer player) {
            player.start();
        }
    }
}
