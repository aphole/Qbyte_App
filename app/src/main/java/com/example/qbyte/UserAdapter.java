package com.example.qbyte;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnUserBlockListener blockListener;

    // Interface for blocking/unblocking users
    public interface OnUserBlockListener {
        void onBlockUser(User user);
        void onUnblockUser(User user);
    }

    public UserAdapter(List<User> userList, Context context, OnUserBlockListener blockListener) {
        this.userList = userList;
        this.context = context;
        this.blockListener = blockListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUserList(List<User> newUserList) {
        userList = newUserList;
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView blockButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name);
            emailTextView = itemView.findViewById(R.id.user_email);
            blockButton = itemView.findViewById(R.id.block_user_icon);
        }

        public void bind(User user) {
            nameTextView.setText(user.getName());
            emailTextView.setText(user.getEmail());

            // Set block/unblock icon based on user's blocked status
            updateBlockIcon(user);

            // Handle the block/unblock button click
            blockButton.setOnClickListener(v -> {
                // Show confirmation dialog before blocking/unblocking
                new AlertDialog.Builder(context)
                        .setTitle(user.isBlocked() ? "Unblock User" : "Block User")
                        .setMessage("Are you sure you want to " + (user.isBlocked() ? "unblock" : "block") + " this user?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Notify the activity to block/unblock the user
                            if (user.isBlocked()) {
                                blockListener.onUnblockUser(user);
                            } else {
                                blockListener.onBlockUser(user);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        private void updateBlockIcon(User user) {
            if (user.isBlocked()) {
                blockButton.setImageResource(R.drawable.unblock_icon); // Use an unblock icon
            } else {
                blockButton.setImageResource(R.drawable.block_icon); // Use a block icon
            }
        }
    }
}
