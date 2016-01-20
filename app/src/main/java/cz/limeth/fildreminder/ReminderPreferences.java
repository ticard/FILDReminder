package cz.limeth.fildreminder;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.FileDescriptor;

public class ReminderPreferences {
    private int delaySeconds;
    private int vibratorDurationMillis;
    private MediaPlayer audioPlayer;

    @SuppressWarnings("ConstantConditions")
    public ReminderPreferences load(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // How long to wait after touching the screen to remind
        delaySeconds = preferences.getInt("pref_key_category_reminder_delay", R.integer.pref_default_category_reminder_delay);

        // Reminder vibrator duration
        vibratorDurationMillis = preferences.getInt("pref_key_category_reminder_vibrator", R.integer.pref_default_category_reminder_vibrator);

        // Prepare for playing an audio file as a reminder
        String reminderAudioPath = preferences.getString("pref_key_category_reminder_audio", null);

        if(reminderAudioPath != null) {
            Uri reminderAudioURI = Uri.parse(reminderAudioPath);

            try {
                ContentResolver contentResolver = context.getContentResolver();
                AssetFileDescriptor assetFileDescriptor = contentResolver.openAssetFileDescriptor(reminderAudioURI, "r");
                //NPE warning surpressed, because we're going to catch it anyway
                FileDescriptor fileDescriptor = assetFileDescriptor.getFileDescriptor();
                audioPlayer = new MediaPlayer();
                audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                audioPlayer.setDataSource(fileDescriptor);
                audioPlayer.prepare();
            } catch (Exception e) {
                if (audioPlayer != null) {
                    audioPlayer.release();
                    audioPlayer = null;
                }

                Log.e(context.getString(R.string.log_tag), "Could not prepare the reminder audio.", e);
            }
        }

        return this;
    }

    public void remind(Context context)
    {
        vibrate(context);
        playAudio();
    }

    private void vibrate(Context context)
    {
        if(vibratorDurationMillis > 0) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            vibrator.vibrate(vibratorDurationMillis);
        }
    }

    private void playAudio()
    {
        if(audioPlayer != null)
            audioPlayer.start();
    }

    public void release()
    {
        if(audioPlayer != null)
            audioPlayer.release();
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public int getVibratorDurationMillis() {
        return vibratorDurationMillis;
    }

    public MediaPlayer getAudioPlayer() {
        return audioPlayer;
    }
}
