package com.example.housemateapp.utilities;

import android.content.Context;
import android.content.res.Resources;
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

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<User> users;
    Context mContext;
    OnClickCallback callback;

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_userHolder;
        ImageView image_profilePicture;
        TextView text_fullName;
        TextView text_department;
        TextView text_grade;
        TextView text_rangeInKilometers;
        TextView text_willStayForDays;
        TextView text_statusType;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            layout_userHolder = itemView.findViewById(R.id.userCard);
            image_profilePicture = itemView.findViewById(R.id.imageUserHolderProfilePicture);
            text_fullName = itemView.findViewById(R.id.textUserHolderFullName);
            text_department = itemView.findViewById(R.id.textUserHolderDepartment);
            text_grade = itemView.findViewById(R.id.textUserHolderGrade);
            text_rangeInKilometers = itemView.findViewById(R.id.textUserHolderRangeInKilometers);
            text_willStayForDays = itemView.findViewById(R.id.textUserHolderWillStayForDays);
            text_statusType = itemView.findViewById(R.id.textUserHolderStatusType);
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
        Resources resources = mContext.getResources();

        viewHolder.text_fullName.setText(user.fullName);
        viewHolder.image_profilePicture.setImageBitmap(user.profilePicture);
        viewHolder.text_department.setText(user.department);

        String gradeString = resources.getString(R.string.display_grade, user.grade);
        viewHolder.text_grade.setText(gradeString);

        String rangeInKilometersString = resources.getString(R.string.display_range_in_kilometers, user.rangeInKilometers);
        viewHolder.text_rangeInKilometers.setText(rangeInKilometersString);

        String willStayForDaysString = resources.getString(R.string.display_will_stay_for_days, user.willStayForDays);
        viewHolder.text_willStayForDays.setText(willStayForDaysString);

        viewHolder.text_statusType.setText(user.statusType);

        viewHolder.layout_userHolder.setOnClickListener(view -> callback.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public interface OnClickCallback {
        void onItemClick(User user);
    }
}
