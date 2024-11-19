package com.example.qbyte;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ActivityLogsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActivityLogsAdapter adapter;
    private List<ActivityLog> activityLogsList;
    private DatabaseReference logsRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs_activity);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        activityLogsList = new ArrayList<>();
        adapter = new ActivityLogsAdapter(this, activityLogsList);
        recyclerView.setAdapter(adapter);

        logsRef = FirebaseDatabase.getInstance().getReference("activityLogs");

        FirebaseApp.initializeApp(this);

        // Fetch the activity logs from Firebase
        fetchActivityLogs();
    }

    private void fetchActivityLogs() {
        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activityLogsList.clear(); // Clear the list before updating
                if (dataSnapshot.exists()) {  // Check if data exists
                    for (DataSnapshot logSnapshot : dataSnapshot.getChildren()) {
                        ActivityLog log = logSnapshot.getValue(ActivityLog.class);
                        if (log != null) {
                            log.setLogId(logSnapshot.getKey()); // Set the logId from the Firebase key
                            activityLogsList.add(log);
                        }
                    }
                } else {
                    Toast.makeText(ActivityLogsActivity.this, "No activity logs found.", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged(); // Notify the adapter to update the UI
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ActivityLogsActivity.this, "Failed to load logs: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}