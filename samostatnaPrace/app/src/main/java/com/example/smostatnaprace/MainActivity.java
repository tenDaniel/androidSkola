package com.example.smostatnaprace;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    CalendarView kalendar;
    TimePicker cas;
    EditText udalost;
    ListView list;
    ArrayAdapter<String> adapter;
    List<String> reminders;
    int rok, mesic, den;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kalendar = findViewById(R.id.kalendar);
        cas = findViewById(R.id.picker);
        udalost = findViewById(R.id.udalost);
        list = findViewById(R.id.list);

        cas.setIs24HourView(true);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String reminder = reminders.get(position);
                Log.d("MainActivity", "Clicked reminder: " + reminder);
                String[] parts = reminder.split(" => ", 2);
                String dateTimeStr = parts[0];
                String event = parts[1];

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("d.M. yyyy HH:mm", Locale.getDefault());
                    Date reminderDate = sdf.parse(dateTimeStr);
                    Date currentDate = new Date();

                    Log.d("MainActivity", "Reminder date: " + reminderDate);
                    Log.d("MainActivity", "Current date: " + currentDate);

                    long diffInMillis = reminderDate.getTime() - currentDate.getTime();
                    Log.d("MainActivity", "Difference in millis: " + diffInMillis);

                    if (diffInMillis > 0) {
                        long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                        long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis) % 24;
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60;

                        if (days > 0) {
                            Toast.makeText(MainActivity.this, "Zbývá " + days + " dní", Toast.LENGTH_SHORT).show();
                        } else if (hours > 0) {
                            Toast.makeText(MainActivity.this, "Zbývá " + hours + " hodin", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Zbývá " + minutes + " minut", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Provedeno", Toast.LENGTH_SHORT).show();
                        reminders.remove(position);
                        adapter.notifyDataSetChanged();
                        ulozPripominky();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "Error while parsing date", e);
                }
            }
        });

        Calendar c = Calendar.getInstance();
        rok = c.get(Calendar.YEAR);
        mesic = c.get(Calendar.MONTH) + 1;
        den = c.get(Calendar.DAY_OF_MONTH);

        kalendar.setOnDateChangeListener(
                new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(
                            @NonNull CalendarView view,
                            int year,
                            int month,
                            int dayOfMonth) {
                        rok = year;
                        mesic = month + 1;
                        den = dayOfMonth;
                    }
                });

        // Načtení připomínek při spuštění aplikace
        nactiPripominky();
    }

    public void pripomen(View view) {
        try {
            // Získání data a času
            long date = kalendar.getDate();
            int hour = cas.getCurrentHour();
            int minute = cas.getCurrentMinute();
            String reminderText = udalost.getText().toString();

            // Formátování připomínky
            String reminder = den + "." + mesic + ". " + rok + " " + hour + ":" + minute + " => " + reminderText + "\n";

            // Uložení připomínky do souboru
            FileOutputStream fos = openFileOutput("udalosti.txt", MODE_APPEND);
            OutputStreamWriter wr = new OutputStreamWriter(fos);
            wr.write(reminder);
            wr.flush();
            wr.close();
            fos.close();

            udalost.setText("");

            // Aktualizace ListView
            nactiPripominky();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nactiPripominky() {
        try {
            FileInputStream fis = openFileInput("udalosti.txt");
            InputStreamReader ir = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(ir);

            reminders = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ", 3);
                String dateTimeStr = parts[0] + " " + parts[1];
                String event = parts[2];

                reminders.add(dateTimeStr + " " + event);
            }

            br.close();
            ir.close();
            fis.close();

            // Nastavení adapteru pro ListView
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reminders);
            list.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ulozPripominky() {
        try {
            FileOutputStream fos = openFileOutput("udalosti.txt", MODE_PRIVATE);
            OutputStreamWriter wr = new OutputStreamWriter(fos);
            for (String reminder : reminders) {
                wr.write(reminder + "\n");
            }
            wr.flush();
            wr.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}