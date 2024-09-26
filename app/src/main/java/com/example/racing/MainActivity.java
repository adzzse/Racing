package com.example.racing;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final List<Handler> handlerList = new ArrayList<>();
    final List<Runnable> runnableList = new ArrayList<>();

    private final List<Boolean> finished = new ArrayList<>();
    private SeekBar sbContent1;
    private SeekBar sbContent2;
    private SeekBar sbContent3;
    private Button btnStart;

    private Button btnRefresh;
    private Button btnReset;
    private TextView txtFinish;
    private CheckBox cb1;
    private CheckBox cb2;
    private CheckBox cb3;
    private TextView moneyResult;
    private TextView profitValue;
    private TextView betValue;
    private TextView sp1;
    private TextView sp2;
    private TextView sp3;
    private TextView moneyLeftTotal;
    private EditText bet1;
    private EditText bet2;
    private EditText bet3;
    private TextView winnerText;
    private TextView profitText;
    private TextView moneyLeftText;
    private Button btnBack;
    int winner = -1;
    double profitTotal = 0.0;
    double moneyLeft = 0.0;
    private SeekBar[] seekBars;
    private static final int RESULT_REQUEST_CODE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        sbContent1 = findViewById(R.id.seekBar);
        sbContent2 = findViewById(R.id.seekBar2);
        sbContent3 = findViewById(R.id.seekBar3);

        btnStart = findViewById(R.id.btnStart);
        btnRefresh = findViewById(R.id.btnRefresh);
        txtFinish = findViewById(R.id.finish);

        cb1 = findViewById(R.id.checkBox1);
        cb2 = findViewById(R.id.checkBox2);
        cb3 = findViewById(R.id.checkBox3);

        moneyResult = findViewById(R.id.moneyResult);
        profitValue = findViewById(R.id.profitValue);
        betValue = findViewById(R.id.betValue);

        sp1 = findViewById(R.id.sp1);
        sp2 = findViewById(R.id.sp2);
        sp3 = findViewById(R.id.sp3);
        bet1 = findViewById(R.id.bet1);
        bet2 = findViewById(R.id.bet2);
        bet3 = findViewById(R.id.bet3);

        moneyLeftTotal = findViewById(R.id.moneyLeft);
        btnReset = findViewById(R.id.reset);

        winnerText = findViewById(R.id.winnerText);
        profitText = findViewById(R.id.profitText);
        moneyLeftText = findViewById(R.id.moneyLeftText);

        btnBack = findViewById(R.id.btnBack);

        // Initialize SeekBars and TextViews Arrays
        seekBars = new SeekBar[]{sbContent1, sbContent2, sbContent3};
        final TextView[] textSpeed = {sp1, sp2, sp3};
        LinearLayout backgroundView = findViewById(R.id.layoutRacing);

        // Create an animation
        ObjectAnimator backgroundAnimator = ObjectAnimator.ofFloat(backgroundView, "translationX", 0f, -backgroundView.getWidth());
        backgroundAnimator.setDuration(5000); // duration 5 seconds
        backgroundAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        backgroundAnimator.setRepeatMode(ObjectAnimator.RESTART);
        backgroundAnimator.start();

        // Initialize
        initFinishedList();

        // Initialize Handlers and Runnables for Each SeekBar
        for (int i = 0; i < seekBars.length; i++) {
            int finalI = i;
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                int process = 0; // Local progress for each Runnable
                @Override
                public void run() {
                    btnStart.setVisibility(View.INVISIBLE);
                    Random rd = new Random();
                    int rdRace = rd.nextInt(16); // Random value between 0 and 15
                    process += rdRace;
                    seekBars[finalI].setProgress(process);
                    if (process < 100) {
                        handler.postDelayed(this, 1000);
                    } else {
                        btnRefresh.setVisibility(View.INVISIBLE);
                        finished.set(finalI, true);
                        if (winner == -1) {
                            winner = finalI;
                        }
                        if (!finished.contains(false)) {
                            displayResults();
                        }
                    }
                }
            };
            handlerList.add(handler);
            runnableList.add(runnable);
        }

        // Set SeekBar Change Listeners to Display Speed
        for (int i = 0; i < seekBars.length; i++) {
            int index = i;
            seekBars[index].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textSpeed[index].setText(String.valueOf(progress) + " m");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        // Handle Bet CheckBoxes Visibility
        cb1.setOnCheckedChangeListener((buttonView, isChecked) -> bet1.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE));
        cb2.setOnCheckedChangeListener((buttonView, isChecked) -> bet2.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE));
        cb3.setOnCheckedChangeListener((buttonView, isChecked) -> bet3.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE));

        // Handle Start Button Click
        btnStart.setOnClickListener(v -> startRace());

        // Handle Refresh Button Click
        btnRefresh.setOnClickListener(v -> refreshGame());

        // Handle Reset Button Click
        btnReset.setOnClickListener(v -> resetGame());
    }

    public void handleRun(double moneyTotal, double totalBet) {
        moneyLeft = moneyTotal - totalBet;
        if (moneyLeft < 0) {
            Toast.makeText(MainActivity.this, "Not enough $", Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < handlerList.size(); i++) {
                handlerList.get(i).post(runnableList.get(i));
            }
            betValue.setText(String.valueOf(totalBet));
            moneyResult.setText(String.valueOf(moneyLeft));
            moneyLeftTotal.setText(String.valueOf(moneyLeft));
        }
    }

    private void startRace() {
        double betValue1 = parseDouble(bet1.getText().toString());
        double betValue2 = parseDouble(bet2.getText().toString());
        double betValue3 = parseDouble(bet3.getText().toString());

        double moneyTotal = parseDouble(moneyResult.getText().toString());

        if (betValue1 == 0.0 && betValue2 == 0.0 && betValue3 == 0.0) {
            Toast.makeText(getApplicationContext(), "Please bet before starting!", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalBet = betValue1 + betValue2 + betValue3;
        handleRun(moneyTotal, totalBet);
    }

    // Parse Double
    private double parseDouble(String value) {
        return value.isEmpty() ? 0 : Double.parseDouble(value);
    }

    // Display Results
    private void displayResults() {
        double betNum1 = parseDouble(bet1.getText().toString().isEmpty() ? "0" : bet1.getText().toString());
        double betNum2 = parseDouble(bet2.getText().toString().isEmpty() ? "0" : bet2.getText().toString());
        double betNum3 = parseDouble(bet3.getText().toString().isEmpty() ? "0" : bet3.getText().toString());
        double betValueTotal = parseDouble(betValue.getText().toString().isEmpty() ? "0" : betValue.getText().toString());
        double profit = 0.0;

        if (winner == 0) {
            profitTotal = betNum1 * 2;
        } else if (winner == 1) {
            profitTotal = betNum2 * 2;
        } else if (winner == 2) {
            profitTotal = betNum3 * 2;
        }

        profit = profitTotal - betValueTotal;

        SharedPreferences sharedPreferences = getSharedPreferences("ResultData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("winner","The winner is: " + winner+1);
        editor.putFloat("profit", (float) profit);
        editor.putFloat("moneyLeft", (float) (moneyLeft + profitTotal)); // Update money left with profit
        editor.apply();

        // Start ResultActivity and pass the result data
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }
    // Calculate Profit
    private double calculateProfit(int winner, double betNum1, double betNum2, double profitTotal, double betValueTotal) {
        double profit = 0.0;
        if (winner == 0) {
            profitTotal += betNum1 * 1.5;
            txtFinish.setText("The winner is " + winner+1);
            profit = profitTotal - betValueTotal;
        } else if (winner == 1) {
            profitTotal += betNum2 * 1.5;
            txtFinish.setText("The winner is " + winner+1);
            profit = profitTotal - betValueTotal;
        } else if (winner == 2) {
            profitTotal += betNum2 * 1.5;
            txtFinish.setText("The winner is " + winner+1);
            profit = profitTotal - betValueTotal;
        }
        return profit;
    }

    // Refresh Game
    private void refreshGame() {
        updateUI();
        double allMoney = parseDouble(moneyResult.getText().toString());
        if (allMoney <= 1.0) {
            btnReset.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.INVISIBLE);
        }
    }

    // Reset Game
    private void resetGame() {
        btnReset.setVisibility(View.INVISIBLE);
        btnStart.setVisibility(View.VISIBLE);
        profitValue.setText("0.0");
        winner = -1;
        resetSeekBars();
        resetBetsAndCheckBoxes();
        txtFinish.setText("");
        moneyResult.setText("100.0");
        updateUI();
    }

    // Reset SeekBars
    private void resetSeekBars() {
        sbContent1.setProgress(0);
        sbContent2.setProgress(0);
        sbContent3.setProgress(0);
    }

    // Reset Bets and CheckBoxes
    private void resetBetsAndCheckBoxes() {
        bet1.setText("");
        bet2.setText("");
        bet3.setText("");
        betValue.setText("0.0");
        cb1.setChecked(false);
        cb2.setChecked(false);
        cb3.setChecked(false);
    }

    // Check Game Over Condition
    private void checkGameOver() {
        double allMoney = parseDouble(moneyResult.getText().toString());
        if (allMoney <= 1.0) {
            btnReset.setVisibility(View.INVISIBLE);
            btnStart.setVisibility(View.INVISIBLE);
        }
    }

    private void updateUI() {
        btnStart.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.INVISIBLE);
        profitValue.setText("0.0");
        winner = -1;
        resetSeekBars();
        resetBetsAndCheckBoxes();
        txtFinish.setText("");
        double allMoney = parseDouble(moneyResult.getText().toString());
        if (allMoney <= 1.0) {
            btnReset.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.INVISIBLE);
        } else {
            btnReset.setVisibility(View.INVISIBLE);
        }
    }

    // Reset lại Handlers và Runnables khi quay lại từ ResultActivity
    private void resetHandlersAndRunnables() {
        // Đảm bảo rằng tất cả các Handler và Runnable đã được xóa trước khi tạo mới
        handlerList.clear();
        runnableList.clear();

        // Tạo lại Handlers và Runnables cho mỗi SeekBar
        for (int i = 0; i < seekBars.length; i++) {
            int finalI = i;
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                int process = 0; // Local progress for each Runnable
                @Override
                public void run() {
                    btnStart.setVisibility(View.INVISIBLE);
                    Random rd = new Random();
                    int rdRace = rd.nextInt(16); // Random value between 0 and 15
                    process += rdRace;
                    seekBars[finalI].setProgress(process);
                    if (process < 100) {
                        handler.postDelayed(this, 1000);
                    } else {
                        btnRefresh.setVisibility(View.INVISIBLE);
                        finished.set(finalI, true);
                        if (winner == -1) {
                            winner = finalI;
                        }
                        if (!finished.contains(false)) {
                            displayResults();
                        }
                    }
                }
            };
            handlerList.add(handler);
            runnableList.add(runnable);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        resetHandlersAndRunnables();
    }

    private void initFinishedList() {
        finished.clear();
        for (int i = 0; i < seekBars.length; i++) {
            finished.add(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                double moneyLeft = data.getDoubleExtra("moneyLeft", 0.0);
                moneyResult.setText(String.valueOf(moneyLeft));
                updateUI();
            }
        }
    }

}
