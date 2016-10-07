package com.example.pascal.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;

/**
 * Created by pascal on 07.10.16.
 */
public abstract class SliderDialog {
    SeekBar slider;
    public SliderDialog(String title, Activity activity) {
        Dialog dialog = new Dialog(activity);
        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        LayoutInflater inflater = (LayoutInflater)
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slider_dialog, null);
        view.setMinimumWidth((int) (displayRectangle.width() * 0.9f));
        dialog.setContentView(view);
        dialog.setTitle(title);
        slider = (SeekBar) dialog.findViewById(R.id.seekBar);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onValueChanged(value(((double) progress) / seekBar.getMax()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }

        );
        dialog.show();
    }

    public void setValue(double value) {
        value = fromValue(value) * slider.getMax();
        slider.setProgress((int) value);
    }

    abstract double value(double linear01);
    abstract double fromValue(double value);
    abstract void onValueChanged(double value);

    public static abstract class ExpSliderDialog extends SliderDialog {
        private final double min, max, curvature;
        public ExpSliderDialog(String title, double min, double max, double curvature,
                        Activity activity) {
            super(title, activity);
            this.min = min;
            this.max = max;
            this.curvature = curvature;
        }

        double value(double linear01) {
            linear01 *= curvature;
            linear01 = Math.exp(linear01);
            linear01 -= Math.exp(0.0);
            linear01 /= (Math.exp(curvature) - Math.exp(0.0));
            linear01 *= (max - min);
            linear01 += min;
            return linear01;
        }

        double fromValue(double value) {
            value -= min;
            value /= (max - min);
            value *= (Math.exp(curvature) - Math.exp(0.0));
            value += Math.exp(0.0);
            value = Math.log(value);
            value /= curvature;
            return value;
        }
    }

}
