package com.example.boat_meter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

/*Imports for button*/
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.boat_meter.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'boat_meter' library on application startup.
    static {
        System.loadLibrary("boat_meter");
    }

    private ActivityMainBinding binding;
    Button button;
    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Set the content view here

        // Example of a call to a native method
        tv = binding.sampleText;
        tv.setText(stringFromJNI());

        button = findViewById(R.id.commands_button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                button_handler();
                tv.setText(stringFromJNI());

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

    /**
     * C++ code that is called when button is pressed
     */
    public native void button_handler();
}