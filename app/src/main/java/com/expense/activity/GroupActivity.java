package com.expense.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.expense.R;
import com.expense.model.Group;
import com.expense.setting.AuthenticationManager;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GroupActivity extends AppCompatActivity {

    private AuthenticationManager authentication;

    private Button createGroupButton;

    private EditText groupNameInput;

    private EditText groupDescriptionInput;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setTitle(R.string.group_activity_title);

        authentication = new AuthenticationManager(getApplicationContext());

        // Return to the Sign In screen if the user is not logged
        if (!authentication.isValid()) {
            Intent signInIntent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(signInIntent);
            finish();
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        progress = new ProgressDialog(this);

        // Set back button at the application toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupNameInput = findViewById(R.id.groupNameInput);
        groupDescriptionInput = findViewById(R.id.groupDescriptionInput);

        createGroupButton = findViewById(R.id.createGroupButtonId);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGroup();
            }
        });

    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.deleteGroupButtonId:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveGroup() {

        displayLoading("Wait while we create the group...");

        OkHttpClient client = new OkHttpClient();

        Gson gson = new Gson();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, gson.toJson(new Group(
                groupNameInput.getText().toString(),
                groupDescriptionInput.getText().toString()
        )));
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/group")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + authentication.getToken().getAccessToken())
                .addHeader("cache-control", "no-cache")
                //.addHeader("Postman-Token", "bea0be33-3cf1-47cd-b48c-8f030990b52f")
                .build();

        try {
            client.newCall(request).execute();
            Toast.makeText(getApplicationContext(),
                    "Group successfully created.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Error in the group creation.", Toast.LENGTH_LONG).show();
        }

        dismissLoading();
        finish(); // Return to the Main activity

    }

    public void displayLoading(String loadingMessage) {
        progress.setTitle("Creating the group...");
        progress.setMessage(loadingMessage);
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
    }

    public void dismissLoading() {
        // Dismiss the loading dialog
        progress.dismiss();
    }

}
