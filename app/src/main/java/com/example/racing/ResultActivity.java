package com.example.racing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {
    private TextView winnerText;
    private TextView profitText;
    private TextView moneyLeftText;
    private SharedPreferences sharedPreferences;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        winnerText = findViewById(R.id.winnerText);
        profitText = findViewById(R.id.profitText);
        moneyLeftText = findViewById(R.id.moneyLeftText);
        btnBack = findViewById(R.id.btnBack);

        sharedPreferences = getSharedPreferences("ResultData", Context.MODE_PRIVATE);

        // Load result data from SharedPreferences
        loadResultData();
        // Set an OnClickListener for the btnBack button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the updated money left value
                double moneyLeft = Double.parseDouble(moneyLeftText.getText().toString());

                // Create an Intent to store the result data
                Intent resultIntent = new Intent();
                resultIntent.putExtra("moneyLeft", moneyLeft);

                // Set the result and finish the activity
                setResult(RESULT_OK, resultIntent);
                finish();
            } });

    }

    private void loadResultData() {
        String winner = sharedPreferences.getString("winner", "");
        double profit = sharedPreferences.getFloat("profit", 0.0f);
        double moneyLeft = sharedPreferences.getFloat("moneyLeft", 0.0f);

        // Display loaded result data
        winnerText.setText(winner);
        profitText.setText(String.format("%.2f", profit));
        moneyLeftText.setText(String.format("%.2f", moneyLeft));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadResultData();
    }
}
