package com.nullpointexecutioners.buzzfilms.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nullpointexecutioners.buzzfilms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchMovieResultsActivity extends AppCompatActivity {

    @Bind(R.id.listview_movie_search) ListView mSearchList;
    @Bind(R.id.search_toolbar) Toolbar toolbar;

    private ArrayAdapter<String> mSearchAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        ButterKnife.bind(this);

        initToolbar();

        //TODO, redo with Volley
//        FetchSearch search = new FetchSearch();
//        search.execute();

        mSearchAdapter = new ArrayAdapter<>(this,
                R.layout.list_item_film,
                R.id.list_item_film,
                new ArrayList<String>());
        mSearchList.setAdapter(mSearchAdapter);

        List<Integer> x = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        for (int i : x) {
            mSearchAdapter.add("Dummy data " + i);
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Handles search from the MainActivity
     * Gets query from the intent and passes it in to search
     * @param intent to handle search from
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle(query);
//            doSearch(query);
        }
    }

    private void doSearch(String query) {
        // Testing Volley
//        this.tomato = TomatoVolley.getInstance(this);
//        RequestQueue queue = this.tomato.getRequestQueue();

        // Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        Log.v("Response", response);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("Volley Error", error.toString());
//            }
//        });
//        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
    }

    /**
     * Helper method that inits all of the Toolbar stuff
     */
    private void initToolbar() {
        assert getSupportActionBar() != null;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //Simulate a system's "Back" button functionality.
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_overflow, menu);

        //Set Search icon
        menu.findItem(R.id.action_search).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_search)
                .color(Color.WHITE)
                .sizeDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_DP)
                .paddingDp(4));

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    /**
     * Class for Fetching data (JSON) using RottenTomatoes API asynchronously
     */
    public class FetchSearch extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchSearch.class.getSimpleName();

        private String[] getDataFromJson(String FilmJsonStr, int num)
                throws JSONException {

            JSONObject forecastJson = new JSONObject(FilmJsonStr);
            JSONArray FilmArray = forecastJson.getJSONArray("movies");

            String[] resultStrs = new String[FilmArray.length()];
            for (int i = 0; i < FilmArray.length(); i++) {

                // Get the JSON object representing the title
                JSONObject titleObject = FilmArray.getJSONObject(i);
                resultStrs[i] = titleObject.getString("title");
            }

            //Disable logging for debugging TODO re-enable
//            for (String s : resultStrs) {
//                Log.v(LOG_TAG, "Movie: " + s);
//            }
            return resultStrs;

        }

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String FilmJsonStr = null;

            try {
                //FIXME broken af--waiting for Volley Rewrite
//                URL url = StringHelper.searchURL(search);
                URL url = new URL("");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                FilmJsonStr = buffer.toString();

                //Disable logging for debugging, TODO re-enable this
//                Log.v(LOG_TAG, "Forecast JSON String: " + FilmJsonStr);

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                return getDataFromJson(FilmJsonStr, 10);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mSearchAdapter.clear();
                for (String movie : result) {
                    mSearchAdapter.add(movie);
                }
            }
        }
    }
}
