package com.example.pascal.canlight;

import android.app.Activity;
import android.app.Dialog;
import android.view.WindowManager;
import android.widget.SeekBar;

import java.util.Locale;

/**
 * Created by pascal on 07.10.16.
 */
public abstract class SliderDialog {
    final SeekBar mSlider;
    final String mTitle;
    final Dialog mDialog;
    public SliderDialog(Activity activity, String title) {
        mTitle = title;

        mDialog = new Dialog(activity);
        mDialog.setContentView(R.layout.slider_layout);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        mDialog.getWindow().setAttributes(lp);

        mSlider = (SeekBar) mDialog.findViewById(R.id.seekBar);
        mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
          {
              @Override
              public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                  onValueChanged(getValue());
                  updateTitle();
              }

              @Override
              public void onStartTrackingTouch(SeekBar seekBar) {
              }

              @Override
              public void onStopTrackingTouch(SeekBar seekBar) {
              }
          });

        mDialog.show();
        mDialog.getWindow().setAttributes(lp);
        updateTitle();
    }

    public void setValue(double value) {
        value = fromValue(value) * mSlider.getMax();
        mSlider.setProgress((int) value);
    }

    public double getValue() {
        return value(((double) mSlider.getProgress()) / mSlider.getMax());
    }

    private void updateTitle() {
        final double value = getValue();
        final String valueString = String.format(Locale.getDefault(), "%.2f", value);
        mDialog.setTitle(mTitle + " (" + valueString + ")");
    }


    abstract double value(double linear01);
    abstract double fromValue(double value);
    protected abstract void onValueChanged(double value);

    public static abstract class ExpSliderDialog extends SliderDialog {
        private final double min, max, curvature;
        public ExpSliderDialog(double min, double max, double curvature,
                               Activity activity, String title) {
            super(activity, title);
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
