package com.example.housemateapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.housemateapp.entities.MatchingRequest;
import com.example.housemateapp.entities.User;
import com.example.housemateapp.utilities.AuthUtils;
import com.example.housemateapp.utilities.RequestAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RequestsPageActivity extends AppCompatActivity {
    RecyclerView view_requests;
    RequestAdapter requestAdapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public final ArrayList<MatchingRequest> matchingRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_page);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        AuthUtils.redirectToLoginIfNotAuthenticated(this, mAuth);

        view_requests = findViewById(R.id.requestsList);
        requestAdapter = new RequestAdapter(matchingRequests, this, request -> {
            Intent requestIntent = new Intent(this, RequestActivity.class);
            requestIntent.putExtra(MatchingRequest.FROM_UID, request.fromUid);
            startActivity(requestIntent);
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        view_requests.setAdapter(requestAdapter);
        view_requests.setItemAnimator(new DefaultItemAnimator());
        view_requests.setLayoutManager(linearLayoutManager);

        String currentUid = mAuth.getCurrentUser().getUid();

        db.collection(MatchingRequest.COLLECTION_NAME)
            .get()
            .addOnFailureListener(e -> Log.e("RequestsPageActivity", "onCreate: GetMatchingRequests", e))
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    MatchingRequest matchingRequest = MatchingRequest.parseDocumentSnapshot(snapshot);

                    if (!currentUid.equals(matchingRequest.fromUid) && !currentUid.equals(matchingRequest.toUid))
                        continue;

                    matchingRequests.add(matchingRequest);
                    requestAdapter.notifyItemChanged(matchingRequests.indexOf(matchingRequest));

                    db.collection(User.COLLECTION_NAME)
                        .document(matchingRequest.fromUid)
                        .get()
                        .addOnFailureListener(e -> Log.e("RequestsPageActivity", "onCreate: GetUser FromId " + matchingRequest.fromUid, e))
                        .addOnSuccessListener(documentSnapshot -> {
                            matchingRequest.fromUser = User.parseDocumentSnapshot(documentSnapshot);
                            requestAdapter.notifyItemChanged(matchingRequests.indexOf(matchingRequest));
                        });
                }

                requestAdapter.notifyDataSetChanged();
            });
    }
}