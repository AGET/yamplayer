/**
 * Created by Rafael Rendon Pablo on 30/09/2016.
 */

package letmethink.mx.yamplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String ERROR_TAG = "ERROR";
    private static final int HISTORY_SIZE = 100;
    private static final int UNDEFINED = -1;
    private FileSystem fs = new FileSystem();
    private Random random = new Random(System.currentTimeMillis());
    private ListView songList;
    private MediaPlayer player;
    private File[] library;
    private LinkedList<Integer> playbackHistory;
    private int currentSong = UNDEFINED;

    private Button prevButton;
    private Button nextButton;
    private Button playButton;
    private static final String YAMPLAYER_SETTINGS_FILE = "yamplayer_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        songList = (ListView) findViewById(R.id.list_view);
        playbackHistory = new LinkedList<>();

        String path = System.getenv("SECONDARY_STORAGE") + "/music";
        new FileLoader().execute(path);

        player = new MediaPlayer();
        player.setOnPreparedListener(new PlayerPreparer());

        prevButton = (Button) findViewById(R.id.player_prev);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playbackHistory.isEmpty()) {
                    player.seekTo(0);
                } else {
                    Integer prev = playbackHistory.removeLast();
                    playSong(prev);
                }
            }
        });

        playButton = (Button) findViewById(R.id.player_play_pause);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                    playButton.setText("Play");
                } else {
                    player.start();
                    playButton.setText("Pause");
                }
            }
        });

        nextButton = (Button) findViewById(R.id.player_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNext();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if (player.isPlaying() && currentSong != UNDEFINED) {
            player.pause();
            editor.putInt("current_song", currentSong);
            editor.putInt("current_position", player.getCurrentPosition());
            editor.commit();
        }
        player.release();
    }

    private void populateSongList(final File[] files) {
        library = files;
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.song_item, fs.getFileNames(files));
        songList.setAdapter(adapter);
        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playNext(position);
            }
        });
    }

    private void playNext() {
        int position = random.nextInt(library.length);
        playNext(position);
    }

    private void playNext(int position) {
        if (currentSong != UNDEFINED) {
            playbackHistory.addLast(currentSong);
        }

        // History size is fixed, if full remove the oldest element.
        if (playbackHistory.size() > HISTORY_SIZE) {
            playbackHistory.removeFirst();
        }
        playSong(position);
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
        Toast.makeText(getApplicationContext(), title, Toast.LENGTH_LONG).show();
        songList.setSelection(position);
        currentSong = position;
        playButton.setText("Pause");
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
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            int song = settings.getInt("current_song", UNDEFINED);
            if (song != UNDEFINED) {
                String songFile = library[song].getAbsolutePath();
                try {
                    player.setDataSource(songFile);
                    currentSong = song;
                    player.prepareAsync();
                } catch (IOException ioe) {
                    Log.e(ERROR_TAG, "Failed to load media files: " + ioe.getMessage());
                }
            }
        }
    }

    private class PlayerPreparer implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer player) {
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            int position = settings.getInt("current_position", UNDEFINED);
            if (position != UNDEFINED) {
                player.seekTo(position);
                settings.edit().clear().commit();
            }
            player.start();
            playButton.setText("Pause");
        }
    }

    public class RemoteControlReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                    if (player.isPlaying()) {
                        player.pause();
                        playButton.setText("Play");
                    } else {
                        player.start();
                        playButton.setText("Pause");
                    }
                }
            }
        }
    }
}
