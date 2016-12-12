package warrocker.vkmusicdownloader;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    ArrayList<JSONObject> audioList = new ArrayList<>();
    ArrayAdapter adapter;
    ListView audioListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        adapter = new AudioFileAdapter(this, audioList);
        // Get the intent, verify the action and get the query
        audioListView = (ListView) findViewById(R.id.result_list);
        audioListView.setAdapter(adapter);
        onSearchRequested();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchAudio(query);
        }
    }

    private void searchAudio(String query){
        VKRequest request = VKApi.audio().search(VKParameters.from(VKApiConst.Q, query));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                ArrayList<JSONObject> audioJsonObjects = new ArrayList<>();
                audioList.clear();
                try {
                    JSONArray audioItems = response.json.getJSONObject("response").getJSONArray("items");
                    for(int i = 0; i<audioItems.length(); i++){
                        JSONObject audio = audioItems.getJSONObject(i);
                        audioJsonObjects.add(audio);
                    }
                    audioList.addAll(audioJsonObjects);
                    audioListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
