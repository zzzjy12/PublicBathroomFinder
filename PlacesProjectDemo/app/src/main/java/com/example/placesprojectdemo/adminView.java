package com.example.placesprojectdemo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import authentication.UserAccount;
import authentication.UsersAdapter;

public class adminView extends AppCompatActivity implements UsersAdapter.OnItemClickListener{

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<UserAccount> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view);

        Toolbar toolbar = findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        userList = new ArrayList<>();
        adapter = new UsersAdapter(this, userList, this);
        recyclerView.setAdapter(adapter);

        fetchUsers();
    }

    private void fetchUsers() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear(); // Clear the old list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserAccount user = snapshot.getValue(UserAccount.class);
                    if (user != null) {
                        userList.add(user);
                    } else {
                        Log.e("adminView", "User data is null");
                    }
                }
                adapter.notifyDataSetChanged(); // Notify adapter to refresh the list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("adminView", "Failed to load users: " + databaseError.getMessage());
                Toast.makeText(adminView.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(UserAccount user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUser(user))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteUser(UserAccount user) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(user.getEmail().replace(".", ",")).removeValue()
                .addOnSuccessListener(aVoid -> {
                    userList.remove(user);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(adminView.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(adminView.this, "Failed to delete user", Toast.LENGTH_SHORT).show());
    }
}