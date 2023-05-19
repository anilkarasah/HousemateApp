package com.example.housemateapp.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemateapp.R;
import com.example.housemateapp.entities.User;

import java.util.ArrayList;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<User> users;
    Context mContext;
    OnClickCallback callback;

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_userHolder;
        ImageView image_avatar;
        TextView text_fullName;
        TextView text_studentInfo;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            layout_userHolder = itemView.findViewById(R.id.userCard);
            image_avatar = itemView.findViewById(R.id.imageUserAvatar);
            text_fullName = itemView.findViewById(R.id.textUserFullName);
            text_studentInfo = itemView.findViewById(R.id.textUserStudentInfo);
        }
    }

    public UserAdapter(ArrayList<User> users, Context context, OnClickCallback callback) {
        this.users = users;
        this.mContext = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_user_holder, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user = users.get(position);

        if (user == null) return;

        UserViewHolder viewHolder = (UserViewHolder) holder;
        viewHolder.text_fullName.setText(user.fullName);
        viewHolder.image_avatar.setImageBitmap(user.profilePicture);

        String studentInfo = String.format(Locale.ENGLISH, "%s, %d. sınıf öğrencisi", user.department, user.grade);
        viewHolder.text_studentInfo.setText(studentInfo);

        viewHolder.layout_userHolder.setOnClickListener(view -> callback.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public interface OnClickCallback {
        void onItemClick(User user);
    }
}
