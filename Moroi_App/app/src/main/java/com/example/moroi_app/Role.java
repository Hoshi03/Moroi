package com.example.moroi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Role extends AppCompatActivity {
    private Button playerBtn;
    private Button managerBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);
        Button playerBtn = findViewById(R.id.playerBtn);
        Button managerBtn = findViewById(R.id.managerBtn);

        playerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Role.this, MainActivity.class);
                startActivity(intent);
            }
        });

        managerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
