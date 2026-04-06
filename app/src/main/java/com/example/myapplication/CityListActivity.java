package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CityListActivity extends AppCompatActivity {

    public static final String EXTRA_FOCUS_SEARCH = "extra_focus_search";

    private static final String PROVINCES_API =
            "https://provinces.open-api.vn/api/v2/?depth=1";
    private static final String PROVINCES_REQUEST_TAG = "vn_provinces";

    private RecyclerView rvCities;
    private ProgressBar progressCityList;
    private TextInputEditText editSearchCity;
    private CityAdapter cityAdapter;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCityList);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rvCities = findViewById(R.id.rvCities);
        progressCityList = findViewById(R.id.progressCityList);
        editSearchCity = findViewById(R.id.editSearchCity);
        rvCities.setHasFixedSize(true);
        rvCities.setLayoutManager(new LinearLayoutManager(this));

        cityAdapter = new CityAdapter(item -> {
            Intent intent = new Intent(CityListActivity.this, DetailActivity.class);
            intent.putExtra("CITY_NAME", item.displayName);
            intent.putExtra("WEATHER_QUERY", item.weatherQuery);
            startActivity(intent);
        });
        rvCities.setAdapter(cityAdapter);

        editSearchCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cityAdapter.filter(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_FOCUS_SEARCH, false)) {
            editSearchCity.post(() -> {
                editSearchCity.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editSearchCity, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }

        requestQueue = Volley.newRequestQueue(this);
        loadProvinces();
    }

    private void loadProvinces() {
        progressCityList.setVisibility(View.VISIBLE);
        rvCities.setVisibility(View.GONE);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                PROVINCES_API,
                null,
                response -> {
                    progressCityList.setVisibility(View.GONE);
                    rvCities.setVisibility(View.VISIBLE);
                    try {
                        List<ProvinceItem> parsed = parseProvinces(response);
                        cityAdapter.setMasterList(parsed);
                        cityAdapter.filter(editSearchCity.getText() != null
                                ? editSearchCity.getText().toString()
                                : "");
                    } catch (JSONException e) {
                        Toast.makeText(this, R.string.city_list_error, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressCityList.setVisibility(View.GONE);
                    rvCities.setVisibility(View.VISIBLE);
                    Toast.makeText(this, R.string.city_list_error, Toast.LENGTH_LONG).show();
                }
        );
        request.setTag(PROVINCES_REQUEST_TAG);
        requestQueue.add(request);
    }

    private List<ProvinceItem> parseProvinces(JSONArray arr) throws JSONException {
        List<ProvinceItem> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String name = o.optString("name", "").trim();
            String codename = o.optString("codename", "").trim();
            if (name.isEmpty()) {
                continue;
            }
            list.add(new ProvinceItem(name, VietnamProvinceHelper.toOpenWeatherQuery(name, codename)));
        }
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        collator.setStrength(Collator.PRIMARY);
        list.sort((a, b) -> collator.compare(a.displayName, b.displayName));
        return list;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(PROVINCES_REQUEST_TAG);
        }
    }
}
