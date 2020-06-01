package com.rishi.family.views.activities.sms;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rishi.family.R;
import com.rishi.family.dbhelper.SmsDatabaseHelper;
import com.rishi.family.receivers.MyBroadcastReceiver;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;


public class CreateSmsScheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, RadioGroup.OnCheckedChangeListener {

    private static final int PERMISSIONS_REQUEST_SEND_SMS = 101;
    Button btnSetSchedule;
    TextView textViewDate;
    RadioGroup radioGroup, radioGroup2;
    RadioButton radioButton1, radioButton2, message1, message2, message3, message4;
    TextView textViewTime;
    RelativeLayout relativeLayoutSelectTime;
    RelativeLayout relativeLayoutSelectDate;
    Calendar calendar;
    EditText editTextMessage;
    String message, message11;
    EditText editTextToRecipient;
    int mHour, mMinute, mYear, mMonth, mDay;
    SmsDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sms_schedule);
        calendar = Calendar.getInstance();
        btnSetSchedule = findViewById(R.id.btnSetSchedule);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup2 = findViewById(R.id.radioGroup1);
        message1 = findViewById(R.id.message1);
        message2 = findViewById(R.id.message2);
        message3 = findViewById(R.id.message3);
        message4 = findViewById(R.id.message4);
        radioButton1 = findViewById(R.id.radioButton1);
        radioButton2 = findViewById(R.id.radioButton2);
        relativeLayoutSelectDate = findViewById(R.id.relativeLayoutSelectDate);
        relativeLayoutSelectTime = findViewById(R.id.relativeLayoutSelectTime);
        editTextMessage = findViewById(R.id.editTextMessage);
        editTextToRecipient = findViewById(R.id.editTextToRecipient);
        databaseHelper = new SmsDatabaseHelper(this);


        radioGroup.setOnCheckedChangeListener(this);
        radioGroup2.setOnCheckedChangeListener(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                setSmsSchedule();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot set schedule", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setSmsSchedule() {
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton2) {
            message11 = editTextMessage.getText().toString();

        } else {
            message11 = message;
        }
        if (message11 == "") {
            message11 = message;
            //  Toast.makeText(getApplicationContext(), message11 + "in", Toast.LENGTH_SHORT).show();
        }

//        Toast.makeText(getApplicationContext(), message11 + "out", Toast.LENGTH_SHORT).show();
        String contactNumber = editTextToRecipient.getText().toString();
        Bundle bundle = new Bundle();
        bundle.putString("number", contactNumber);
        bundle.putString("message", message11);

        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        intent.putExtras(bundle);
        String actionUri = "com.scheduler.action.SMS_SEND";
        intent.setAction(actionUri);

        int _id = (int) System.currentTimeMillis();
        //Long time = new GregorianCalendar().getTimeInMillis() + 60 * 1000;
        calendar.set(mYear, mMonth, mDay, mHour, mMinute);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, _id, intent, 0);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(this, "Scheduled", Toast.LENGTH_SHORT).show();

        if (databaseHelper.addSms(_id, contactNumber, message11, textViewTime.getText().toString(),
                textViewDate.getText().toString(), (int) calendar.getTimeInMillis())) {
            Toast.makeText(this, "added to db", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, SmsSchedulerActivity.class));
        } else {
            Toast.makeText(this, "Something wrong", Toast.LENGTH_SHORT).show();
        }
    }


    public void getDate(View view) {
        // initialize
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, mYear, mMonth, mDay);
        datePickerDialog.show(getFragmentManager(), "datePickerDialog");
    }


    public void getTime(View view) {
        // initialize
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, mHour, mMinute, DateFormat.is24HourFormat(this));
        timePickerDialog.show(getFragmentManager(), "TimePickerDialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        calendar.add(Calendar.DATE, 0);

        final String[] MONTHS = {"Jan", "Feb", "Mar",
                "Apr", "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec"};
        String mon = MONTHS[monthOfYear];

        String selectedDate = dayOfMonth + "  " + mon
                + " " + year;

        SpannableString ss1 = new SpannableString(selectedDate);
        ss1.setSpan(new RelativeSizeSpan(1.5f), 0, 2, 0); // set
        textViewDate.setText(ss1);

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        mHour = hourOfDay;
        mMinute = minute;

        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR, hourOfDay);

        calendar.set(Calendar.AM_PM, Calendar.AM);

        String str;
        if (calendar.get(Calendar.AM_PM) == Calendar.AM)
            str = "AM";
        else
            str = "PM";

        String hours;
        if (calendar.get(Calendar.HOUR) > 9) {
            hours = String.valueOf(calendar.get(Calendar.HOUR));
        } else {
            hours = "0" + String.valueOf(calendar.get(Calendar.HOUR));
        }

        String minutes;
        if (minute > 9) {
            minutes = String.valueOf(minute);
        } else {
            minutes = "0" + String.valueOf(minute);
        }
        if (hours.equalsIgnoreCase("00")) {
            hours = "12";
        }

        String selectedTime = hours + ":" + minutes
                + "  " + str;

        SpannableString ss2 = new SpannableString(selectedTime);
        ss2.setSpan(new RelativeSizeSpan(1.5f), 0, 5, 0);

        textViewTime.setText(ss2);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR, hourOfDay);
    }

    public void setSchedule(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST_SEND_SMS);

            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else if (editTextToRecipient.getText().toString().length() == 0 || textViewDate.getText().toString().length() == 0 || textViewTime.getText().toString().length() == 0) {
            Toast.makeText(getApplicationContext(), "Kindly Enter all the fields", Toast.LENGTH_SHORT).show();
        } else {
            setSmsSchedule();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (radioButton1.isChecked()) {
            editTextMessage.setText("");
            radioGroup2.setVisibility(View.VISIBLE);
            editTextMessage.setVisibility(View.GONE);
            if (radioGroup2.getCheckedRadioButtonId() == -1) {
                Toast.makeText(getApplicationContext(), "Please select Template", Toast.LENGTH_SHORT).show();

            } else {

                if (message1.isChecked()) {
                    message = message1.getText().toString();
                } else if (message2.isChecked()) {
                    message = message2.getText().toString();
                } else if (message3.isChecked()) {
                    message = message3.getText().toString();
                } else if (message4.isChecked()) {
                    message = message4.getText().toString();
                }


            }
        } else if (radioButton2.isChecked()) {
            if (radioGroup2.getCheckedRadioButtonId() != -1) {
                radioGroup2.clearCheck();
            }
            radioGroup2.setVisibility(View.GONE);
            editTextMessage.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onClick(View view) {
        onBackPressed();
    }
}
