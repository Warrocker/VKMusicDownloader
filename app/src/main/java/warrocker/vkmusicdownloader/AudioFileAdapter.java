package warrocker.vkmusicdownloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import warrocker.vkmusicdownloader.services.DownloadService;

class AudioFileAdapter extends ArrayAdapter<JSONObject> {
    private boolean bound = false;
    private DownloadService downloadService;

    AudioFileAdapter(Context context, ArrayList<JSONObject> audioList) {
        super(context, R.layout.audio_list_item, audioList);
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if(!bound) {
            Intent intent = new Intent(getContext(), DownloadService.class);
            getContext().bindService(intent, sConn, Context.BIND_AUTO_CREATE);
            bound = true;
        }
        final JSONObject audioItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.audio_list_item, parent, false);
        }
        if(audioItem != null) {
            try {
                ((TextView) convertView.findViewById(R.id.audio_title_text)).setText(audioItem.getString("title"));
                ((TextView) convertView.findViewById(R.id.artist_text_view)).setText(audioItem.getString("artist"));
                ((TextView) convertView.findViewById(R.id.duration_text_view)).setText(String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.SECONDS.toMinutes(audioItem.getInt("duration")),
                        audioItem.getInt("duration") - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(audioItem.getInt("duration")))));
                convertView.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        downloadService.downloadFile(audioItem);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return convertView;
    }
    private ServiceConnection sConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            bound = true;
            DownloadService.LocalBinder localBinder = (DownloadService.LocalBinder) binder;
            downloadService = localBinder.getService();

        }

        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };
}
