package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;
    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, position, l) -> {
            City selectedCity = cityArrayAdapter.getItem(position);
            new AlertDialog.Builder(this)
                    .setTitle("Manage City")
                    .setMessage("What do you want to do with \"" + selectedCity.getName() + "\"?")
                    .setPositiveButton("Edit", (dialog, which) -> {
                        CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(selectedCity);
                        cityDialogFragment.show(getSupportFragmentManager(), "City Details");
                    })
                    .setNegativeButton("Delete", (dialog, which) -> {
                        deleteCity(selectedCity);
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                return;
            }

            if (value != null) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");
                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void addCity(City city) {
        citiesRef.document(city.getName()).set(city);
    }

    @Override
    public void updateCity(City originalCity, String newName, String newProvince) {
        citiesRef.document(originalCity.getName()).delete();
        City newCity = new City(newName, newProvince);
        citiesRef.document(newName).set(newCity);
    }

    private void deleteCity(City city) {
        citiesRef.document(city.getName()).delete();
    }
}