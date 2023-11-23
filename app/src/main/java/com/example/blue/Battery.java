package com.example.blue;

import android.widget.TextView;

public class Battery {
    private float voltage;
    private float current;
    private float ampereHours;

    public Battery(float voltage, float current, float ampereHours) {
        this.voltage = voltage;
        this.current = current;
        this.ampereHours = ampereHours;
    }

    public void updateTextViews(TextView voltageView, TextView currentView, TextView ampereHoursView) {
        voltageView.setText(String.format("%.2f", voltage));
        currentView.setText(String.format("%.2f", current));
        ampereHoursView.setText(String.format("%.2f", ampereHours));
    }

    // getters and setters for voltage, current, and ampereHours

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    public float getAmpereHours() {
        return ampereHours;
    }

    public void setAmpereHours(float ampereHours) {
        this.ampereHours = ampereHours;
    }
}