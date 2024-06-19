package com.example.moroi_app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.moroi_app.MainActivity;

public class P1Win extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p1win);
        String message = "Player 1 Wins!";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
