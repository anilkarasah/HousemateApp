package com.example.housemateapp.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemateapp.R;
import com.example.housemateapp.entities.MatchingRequest;

import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<MatchingRequest> matchingRequests;
    Context mContext;
    OnClickCallback callback;

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_requestHolder;
        TextView text_type;
        TextView text_fullName;
        TextView text_statusType;
        TextView text_grade;
        TextView text_range;
        TextView text_days;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            layout_requestHolder = itemView.findViewById(R.id.requestCard);
            text_type = itemView.findViewById(R.id.textRequestHolderType);
            text_fullName = itemView.findViewById(R.id.textRequestHolderFullName);
            text_statusType = itemView.findViewById(R.id.textRequestHolderStatusType);
            text_grade = itemView.findViewById(R.id.textUserHolderGrade);
            text_range = itemView.findViewById(R.id.textUserHolderRangeInKilometers);
            text_days = itemView.findViewById(R.id.textUserHolderWillStayForDays);
        }
    }

    public RequestAdapter(ArrayList<MatchingRequest> matchingRequests, Context context, OnClickCallback callback) {
        this.matchingRequests = matchingRequests;
        this.mContext = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_request_holder, parent, false);

        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MatchingRequest matchingRequest = matchingRequests.get(position);

        if (matchingRequest == null) return;

        RequestAdapter.RequestViewHolder viewHolder = (RequestAdapter.RequestViewHolder) holder;
        Resources resources = mContext.getResources();

        String type;
        Drawable typeIcon;

        if (!matchingRequest.isNotified && !matchingRequest.isAccepted) {
            // waiting for the response from the other user
            type = resources.getString(R.string.prompt_request_waiting);
            typeIcon = AppCompatResources.getDrawable(mContext, R.drawable.ic_clock_black);
        } else if (matchingRequest.isNotified && !matchingRequest.isAccepted) {
            // waiting for this user's response
            type = resources.getString(R.string.prompt_request_sent);
            typeIcon = AppCompatResources.getDrawable(mContext, R.drawable.ic_send_black);
        } else {
            // request has been resolved
            type = resources.getString(R.string.prompt_request_done);
            typeIcon = AppCompatResources.getDrawable(mContext, R.drawable.ic_done_black);
        }

        viewHolder.text_fullName.setText(matchingRequest.fromUser.fullName);
        viewHolder.text_type.setText(type);
        viewHolder.text_type.setCompoundDrawables(typeIcon, null, null, null);

        String gradeString = resources.getString(R.string.display_grade, matchingRequest.fromUser.grade);
        viewHolder.text_grade.setText(gradeString);

        String rangeInKilometersString = resources.getString(R.string.display_range_in_kilometers, matchingRequest.fromUser.rangeInKilometers);
        viewHolder.text_range.setText(rangeInKilometersString);

        String willStayForDaysString = resources.getString(R.string.display_will_stay_for_days, matchingRequest.fromUser.willStayForDays);
        viewHolder.text_days.setText(willStayForDaysString);

        viewHolder.text_statusType.setText(matchingRequest.fromUser.statusType);

        viewHolder.layout_requestHolder.setOnClickListener(view -> callback.onItemClick(matchingRequest));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public interface OnClickCallback {
        void onItemClick(MatchingRequest matchingRequest);
    }
}
