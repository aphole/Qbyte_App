package com.example.qbyte;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ActivityLogsAdapter extends RecyclerView.Adapter<ActivityLogsAdapter.ActivityLogViewHolder> {

    private Context context;
    private List<ActivityLog> activityLogs;
    private DatabaseReference logsRef;

    public ActivityLogsAdapter(Context context, List<ActivityLog> activityLogs) {
        this.context = context;
        this.activityLogs = activityLogs;
        this.logsRef = FirebaseDatabase.getInstance().getReference("activityLogs");
    }

    @Override
    public ActivityLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.activity_log_item, parent, false);
        return new ActivityLogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ActivityLogViewHolder holder, int position) {
        ActivityLog log = activityLogs.get(position);
        holder.actionText.setText(log.getAction());
        holder.fullNameText.setText(log.getFullName());
        holder.deviceText.setText(log.getDevice());
        holder.timestampText.setText(log.getTimestamp());

        holder.deleteButton.setOnClickListener(v -> {
            // Delete the log from Firebase
            deleteLog(log.getLogId()); // Using the logId to delete the log
        });
    }

    @Override
    public int getItemCount() {
        return activityLogs.size();
    }

    private void deleteLog(String logId) {
        logsRef.child(logId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Notify the user about successful deletion
                    Toast.makeText(context, "Log deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Notify the user in case of failure
                    Toast.makeText(context, "Failed to delete log", Toast.LENGTH_SHORT).show();
                });
    }

    public static class ActivityLogViewHolder extends RecyclerView.ViewHolder {
        public TextView actionText, fullNameText, deviceText, timestampText;
        public Button deleteButton;

        public ActivityLogViewHolder(View view) {
            super(view);
            actionText = view.findViewById(R.id.actionText);
            fullNameText = view.findViewById(R.id.fullNameText);
            deviceText = view.findViewById(R.id.deviceText);
            timestampText = view.findViewById(R.id.timestampText);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}
