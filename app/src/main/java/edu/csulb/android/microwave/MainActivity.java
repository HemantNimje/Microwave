package edu.csulb.android.microwave;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private int time = 1000;
    private TextView status_display, timer_display;
    private Switch switch_light, switch_door;
    private Button btn_cook, btn_cancel;
    MyCountDownTimer timer;
    MediaPlayer playerAck, playerComplt;
    String status = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        timer = new MyCountDownTimer(time, 1000);

    }

    /* Initialize and bind the components from the xml file activity_main.xml */
    public void init() {
        status_display = (TextView) findViewById(R.id.status_display);
        timer_display = (TextView) findViewById(R.id.timer_display);
        switch_light = (Switch) findViewById(R.id.switch_light);
        switch_door = (Switch) findViewById(R.id.switch_door);
        btn_cook = (Button) findViewById(R.id.btn_cook);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        /* Create the MediaPlayer objects to play acknowledgment, completion ping */
        playerAck = MediaPlayer.create(this, R.raw.microwaveping);
        playerComplt = MediaPlayer.create(this, R.raw.completion_ping);

    }

    /* Handle Cook button click event */
    public void cook(View v) {
        // Play the acknowledgement beep
        playerAck.start();

        // Update status
        status += "Light is on\n" + "Added 1 minute of cooking time\n" + "Power tube is on\n" +
                "Beep\n";

        /* Start cooking only if the door is closed */
        if (!isDoorOpen()) {
            // Clear the previous countdown timer before creating a new one to avoid timer overlap
            timer.cancel();
            // Update status
            status += "Cooking Started" + "\n";

            time += 60000;
            timer = new MyCountDownTimer(time, 1000);
            timer.start();

            // Turn the light On while cooking
            if (!switch_light.isChecked()) {
                switch_light.toggle();
            }
        } else {
            Toast.makeText(this, "Door is Open. Can't cook", Toast.LENGTH_SHORT).show();
        }

        showStatus();

    }

    /* Handle Cancel button click event */
    public void cancel(View v) {
        // Play the acknowledgement beep
        playerAck.start();

        /* Cancel the cooking timer */
        if (!isDoorOpen()) {

            // Reset the timer
            timer.reset();

            // Turn the light off
            if (switch_light.isChecked()) {
                switch_light.toggle();
            }

           /* if (switch_door.isChecked()) {
                switch_door.toggle();
            }*/
        } else {
            Toast.makeText(this, "Door is Open. Your cooking is already cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    public void showStatus() {
        status_display.setMovementMethod(new ScrollingMovementMethod());
        status_display.setText(status);
    }

    public class MyCountDownTimer extends CountDownTimer {

        private boolean hasStarted = false;

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            hasStarted = true;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // Update the time variable for every tick to update the timer_display at each tick
            time = (int) millisUntilFinished;

            timer_display.setText("" + String.format("%02d",(millisUntilFinished / 60000))
                    + ":" + String.format("%02d",((millisUntilFinished / 1000) % 60)));
        }

        @Override
        public void onFinish() {
            // Play completion beep
            playerComplt.start();

            switch_light.toggle();
            time = 1000;
            timer_display.setText("00:00");
            hasStarted = false;

            // Update status
            status += "Light is off\n" + "Beep\n" + "Beep\n" + "Beep\n";
            showStatus();

        }

        /* Reset the timer */
        public void reset() {
            timer.cancel();
            time = 1000;
            timer_display.setText("00:00");
        }
    }

    /* Check whether door is open or close
    * @return true if the door is open
    * @return false if the door is close */

    public boolean isDoorOpen() {
        if (switch_door.isChecked()) {
            return true;
        }
        return false;
    }

    /* For a click on door switch handle the door closing and opening as well as light on and off */
    public void doorToggle(View v) {

        /* If door is opened reset the timer and switch on the light */
        if (switch_door.isChecked()) {

            /* If the cooking is in progress i.e. timer is on, if door is opened at this moment
            * the timer should reset
            * */
            if (timer.hasStarted) {
                timer.reset();

                status += "Door is Opened\n";
                //Toast.makeText(this, "Door is opened", Toast.LENGTH_SHORT).show();
            }

            /* When food is cooked, once we remove the food from microwave as we close the
            * microwave the light should turn off
            * */
            if (!switch_light.isChecked()) {
                switch_light.toggle();
            }
        }
        /* If door is closed and light is on, switch off the light */
        else if (!switch_door.isChecked() && switch_light.isChecked()) {
            switch_light.toggle();
        }
        showStatus();
    }

}

