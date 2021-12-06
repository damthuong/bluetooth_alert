package com.example.bluetoothscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Lich_su_quet extends AppCompatActivity {
    Button btnBack;
    ListView listView;
    FirebaseDatabase database;
    DatabaseReference ref;
    User user;
    ArrayList<String> list;
    ArrayList<String> listTemp;
    ArrayAdapter<String> adapter;

    // ArrayList<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su_quet);

        user = new User();
        String DeviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        listView = (ListView) findViewById(R.id.listView);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("User: " + DeviceID);
        list = new ArrayList<>();
        listTemp = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        //list.add("Lịch sử tiếp xúc gần: ");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    user = ds.getValue(User.class);
                    list.add("name: " + user.getName());
                    listTemp.add("Name:" + " " + user.getName() + "\n" + "Số điện thoại: " + user.getPhone()+ "\nAddress:" + " " + user.getAddress());
                }
                listView.setAdapter(adapter);
                //listView.setAdapter(adapterTemp);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText()
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showInfo(i);
            }
        });

        btnBack = (Button) findViewById(R.id.btnbackHis);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showInfo(int i) {
        String user = listTemp.get(i);
        Intent intent = new Intent(this, InfoActivity.class);

        intent.putExtra("name", user);
        startActivity(intent);
    }
}
