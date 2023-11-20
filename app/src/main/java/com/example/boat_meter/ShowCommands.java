package com.example.boat_meter;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;



public class ShowCommands extends AppCompatActivity {

    String heading = "Help commands";

    private EditText editText;
    private Button button;

    String helpCommand = "SET1CVALUE0 Do not enter value after the command. Reset ampere value to zero for battery 1. Can be used to calibrate current measurement \n" +
            "\n" +
            "SET2CVALUE0 Do not enter value after the command. Reset ampere value to zero for battery 2. Can be used to calibrate current measurement \n" +

            "\n" +
            "SET1C0 Do not enter value after the command. Reset ampere hours count to zero for battery 1\n" +

            "\n" +
            "SET2C0 Do not enter value after the command. Reset ampere hours count to zero for battery 2\n" +

            "\n" +
            "SET1C Enter a space and the value after the command. Set current value for battery 1. This can be used to calibrate current measurement\n" +
            "\n" +
            "SET2C Enter a space and the value after the command. Set current value for battery 2. This can be used to calibrate current measurement\n" +

            "\n" +
            "SETV1 Enter a space and the value after the command. Set voltage reading for battery 1. This can be used to calibrate voltage measurement\n" +

            "\n" +
            "SETV2 Enter a space and the value after the command. Set voltage reading for battery 2. This can be used to calibrate voltage measurement\n" +

            "\n" +
            "SETGAIN Enter a space and the value after to command. Set amplifier gain. This is configured at the factory, do not change \n" +

            "\n"+
            "SETSHUNT Enter a space and the value after the command. Set shunt1/resistance value. \n" +

            "\n" +
            "SAVE Do not enter value after the command. Save the settings.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_commands);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        editText = findViewById(R.id.commandText);

        TextView textViewHeading = findViewById(R.id.heading);
        textViewHeading.setText(heading);

        TextView textViewCommands = findViewById(R.id.commands);
        textViewCommands.setText(helpCommand);

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (!text.isEmpty()) {
                    editText.getText().clear();
                }
            }

        });
    }
}