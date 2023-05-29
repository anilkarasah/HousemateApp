package com.example.housemateapp.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemateapp.R;
import com.example.housemateapp.entities.MatchingRequest;
import com.example.housemateapp.entities.RequestType;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<MatchingRequest> matchingRequests;
    Context mContext;
    OnClickCallback callback;

    public static class WaitingRequestViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_requestHolder;
        TextView text_fullName, text_statusType, text_grade, text_range, text_days;

        public WaitingRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            layout_requestHolder = itemView.findViewById(R.id.requestCardWaiting);
            text_fullName = itemView.findViewById(R.id.textRequestHolderFullNameWaiting);
            text_statusType = itemView.findViewById(R.id.textRequestHolderStatusTypeWaiting);
            text_grade = itemView.findViewById(R.id.textRequestGradeWaiting);
            text_range = itemView.findViewById(R.id.textRequestRangeInKilometersWaiting);
            text_days = itemView.findViewById(R.id.textRequestWillStayForDaysWaiting);
        }
    }

    public static class SentRequestViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_requestHolder;
        TextView text_fullName, text_statusType, text_grade, text_range, text_days;

        public SentRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            layout_requestHolder = itemView.findViewById(R.id.requestCardSent);
            text_fullName = itemView.findViewById(R.id.textRequestHolderFullNameSent);
            text_statusType = itemView.findViewById(R.id.textRequestHolderStatusTypeSent);
            text_grade = itemView.findViewById(R.id.textRequestGradeSent);
            text_range = itemView.findViewById(R.id.textRequestRangeInKilometersSent);
            text_days = itemView.findViewById(R.id.textRequestWillStayForDaysSent);
        }
    }

    public static class CompleteRequestViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout_requestHolder;
        TextView text_fullName;

        public CompleteRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            layout_requestHolder = itemView.findViewById(R.id.requestCardComplete);
            text_fullName = itemView.findViewById(R.id.textRequestHolderFullNameComplete);
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
        View view;

        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_request_waiting_holder, parent, false);
                return new WaitingRequestViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_request_sent_holder, parent, false);
                return new SentRequestViewHolder(view);
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_request_complete_holder, parent, false);
                return new CompleteRequestViewHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        switch (matchingRequests.get(position).requestType) {
            case Waiting:
                return 0;
            case Sent:
                return 1;
            case Complete:
                return 2;
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MatchingRequest matchingRequest = matchingRequests.get(position);

        if (matchingRequest == null || matchingRequest.fromUser == null)
            return;

        Resources resources = mContext.getResources();

        switch (matchingRequest.requestType) {
            case Waiting:
                WaitingRequestViewHolder waitingRequestViewHolder = (WaitingRequestViewHolder) holder;

                waitingRequestViewHolder.text_fullName.setText(matchingRequest.fromUser.fullName);

                String fromUserGradeString = resources.getString(R.string.display_grade, matchingRequest.fromUser.grade);
                waitingRequestViewHolder.text_grade.setText(fromUserGradeString);

                String fromUserRangeInKilometersString = resources.getString(R.string.display_range_in_kilometers, matchingRequest.fromUser.rangeInKilometers);
                waitingRequestViewHolder.text_range.setText(fromUserRangeInKilometersString);

                String fromUserWillStayForDaysString = resources.getString(R.string.display_will_stay_for_days, matchingRequest.fromUser.willStayForDays);
                waitingRequestViewHolder.text_days.setText(fromUserWillStayForDaysString);

                waitingRequestViewHolder.text_statusType.setText(matchingRequest.fromUser.statusType);

                waitingRequestViewHolder.layout_requestHolder.setOnClickListener(view -> callback.onItemClick(matchingRequest));
                break;
            case Sent:
                SentRequestViewHolder sentRequestViewHolder = (SentRequestViewHolder) holder;

                sentRequestViewHolder.text_fullName.setText(matchingRequest.toUser.fullName);

                String toUserGradeString = resources.getString(R.string.display_grade, matchingRequest.toUser.grade);
                sentRequestViewHolder.text_grade.setText(toUserGradeString);

                String toUserRangeInKilometersString = resources.getString(R.string.display_range_in_kilometers, matchingRequest.toUser.rangeInKilometers);
                sentRequestViewHolder.text_range.setText(toUserRangeInKilometersString);

                String toUserWillStayForDaysString = resources.getString(R.string.display_will_stay_for_days, matchingRequest.toUser.willStayForDays);
                sentRequestViewHolder.text_days.setText(toUserWillStayForDaysString);

                sentRequestViewHolder.text_statusType.setText(matchingRequest.toUser.statusType);

                sentRequestViewHolder.layout_requestHolder.setOnClickListener(view -> callback.onItemClick(matchingRequest));
                break;
            case Complete:
                CompleteRequestViewHolder completeRequestViewHolder = (CompleteRequestViewHolder) holder;

                String opposingUserFullName;
                String opposingUserId;

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String currentUserId = mAuth.getCurrentUser().getUid();
                if (matchingRequest.fromUid.equals(currentUserId)) {
                    opposingUserFullName = matchingRequest.toUser.fullName;
                    opposingUserId = matchingRequest.toUid;
                } else {
                    opposingUserFullName = matchingRequest.fromUser.fullName;
                    opposingUserId = matchingRequest.fromUid;
                }

                completeRequestViewHolder.text_fullName.setText(opposingUserFullName);

                completeRequestViewHolder.layout_requestHolder.setOnClickListener(view -> callback.onItemClick(matchingRequest));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return matchingRequests.size();
    }

    public interface OnClickCallback {
        void onItemClick(@NonNull MatchingRequest request);
    }
}
