package com.example.boat_meter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

/*Imports for button*/
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import com.example.boat_meter.databinding.ActivityMainBinding;



public class MainActivity extends AppCompatActivity {

    // Used to load the 'boat_meter' library on application startup.
    static {
        System.loadLibrary("boat_meter");
    }

    Battery battery1 = new Battery(3.7f, 1.5f, 2000f);
    Battery battery2 = new Battery(12.7f, 1.5f, 2000f);

    private ActivityMainBinding binding;
    Button button;
    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the content view here

        button = findViewById(R.id.commands_button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Test set values function here before bluetooth is done
                setTextViewValues();


                // New window where commands are shown
                Intent intent = new Intent(MainActivity.this,  ShowCommands.class);
                startActivity(intent);
            }
        });
    }


    /**
     * A native method that is implemented by the 'boat_meter' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


     void setTextViewValues() {
         // for battery 1
         TextView voltageView = findViewById(R.id.voltage1);

         TextView currentView = findViewById(R.id.current1);

         TextView ampereHoursView = findViewById(R.id.ampere_hours1);

         battery1.updateTextViews(voltageView, currentView, ampereHoursView);

     // for battery 2
         voltageView = findViewById(R.id.voltage2);

         currentView = findViewById(R.id.current2);

         ampereHoursView = findViewById(R.id.ampere_hours2);

         battery2.updateTextViews(voltageView, currentView, ampereHoursView);

     }
}