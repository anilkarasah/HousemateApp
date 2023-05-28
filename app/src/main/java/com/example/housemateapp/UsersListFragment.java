package com.example.housemateapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.housemateapp.entities.User;
import com.example.housemateapp.utilities.UserAdapter;

import java.util.ArrayList;

public class UsersListFragment extends Fragment {

    RecyclerView view_users;
    UserAdapter userAdapter;

    ArrayList<User> users;

    public UsersListFragment() {}

    public UsersListFragment(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);

        view_users = view.findViewById(R.id.usersList);
        userAdapter = new UserAdapter(users, view.getContext(), user -> {
            Intent userPageIntent = new Intent(view.getContext(), UserPageActivity.class);
            userPageIntent.putExtra(User.UID, user.uid);
            startActivity(userPageIntent);
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false);
        view_users.setAdapter(userAdapter);
        view_users.setItemAnimator(new DefaultItemAnimator());
        view_users.setLayoutManager(linearLayoutManager);

        return view;
    }

    public void notifyUserAdapter(ArrayList<User> usersList) {
        userAdapter.setUsers(usersList);
        userAdapter.notifyDataSetChanged();
    }

    public void notifyUserAdapter(int index) {
        userAdapter.notifyItemChanged(index);
    }
}