package warrocker.vkmusicdownloader;

import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class AudioListFragment extends ListFragment {
    ArrayList<JSONObject> audioList = new ArrayList<>();
    ArrayList<JSONObject> audioJsonObjects = new ArrayList<>();
    ArrayAdapter<JSONObject> adapter;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new AudioFileAdapter(getActivity(), audioList);
        setListAdapter(adapter);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.audio_list_fragment_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        VKRequest request = VKApi.audio().get();
        request.executeWithListener(new VKRequest.VKRequestListener() {

            @Override
            public void onComplete(VKResponse response) {
                audioList.clear();
                try {
                    JSONArray audioItems = response.json.getJSONObject("response").getJSONArray("items");
                    for(int i = 0; i<audioItems.length(); i++){
                        JSONObject audio = audioItems.getJSONObject(i);
                        audioJsonObjects.add(audio);
                    }
                    audioList.addAll(audioJsonObjects);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(VKError error) {
                Log.e("error", String.valueOf(error.errorCode));
            }
            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
//I don't really believe in progress
            }
        });


    }
}
