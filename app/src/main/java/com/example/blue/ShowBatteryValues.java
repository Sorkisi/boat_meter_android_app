package com.example.blue;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;





public class ShowBatteryValues extends AppCompatActivity {


    private EditText editText;
    private Button button;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_battery_values);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        GlobalClass.battery1 = new Battery(0.0f, 0.0f, 0.0f);
        GlobalClass.battery2 = new Battery(0.0f, 0.0f, 0.0f);

        setTextViewValues();


        button = findViewById(R.id.commands_button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Test set values function here before bluetooth is done
                setTextViewValues();

                // New window where commands are shown
                Intent intent = new Intent(ShowBatteryValues.this, ShowCommands.class);
                startActivity(intent);
            }
        });


        // This is the Runnable that will be called every second
        runnable = new Runnable() {
            @Override
            public void run() {
                setTextViewValues();
                handler.postDelayed(this, 1000);
            }
        };

        // Start the Runnable immediately
        handler.post(runnable);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the Runnable when the Activity is destroyed
        handler.removeCallbacks(runnable);
    }
    public void setTextViewValues() {
        // for battery 1
        TextView voltageView = findViewById(R.id.voltage1);

        TextView currentView = findViewById(R.id.current1);

        TextView ampereHoursView = findViewById(R.id.ampere_hours1);

        GlobalClass.battery1.updateTextViews(voltageView, currentView, ampereHoursView);

        // for battery 2
        voltageView = findViewById(R.id.voltage2);

        currentView = findViewById(R.id.current2);

        ampereHoursView = findViewById(R.id.ampere_hours2);

        GlobalClass.battery2.updateTextViews(voltageView, currentView, ampereHoursView);

    }
}