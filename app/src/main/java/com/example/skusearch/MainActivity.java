
package com.example.skusearch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    EditText input_edit_text;
    Button search_online_button;
    Button search_database_button;
    LinearLayout history_container;

    WebSettings webSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.web_view);

        input_edit_text = findViewById(R.id.input_edit_text);

        search_online_button = findViewById(R.id.online_search_button);
        search_database_button = findViewById(R.id.database_search_button);

        history_container = findViewById(R.id.history_container);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.clearCache(true);

        readHistory();


        search_online_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    String input = input_edit_text.getText().toString();
                    // Validate input length and format
                    if (isValidInput(input)) {
                        performSearch(input);
                        appendHistory(input);
                        history_container.removeAllViews();
                        readHistory();
                    } else {
                        displayErrorSnackbar();
                    }


            }
        });

        search_database_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = input_edit_text.getText().toString();
                String toastText = "Search Database: "+input;
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private boolean isValidInput(String input) {
        if (input.matches("[0-9]{11,12}")) { // Check for 11-12 digits
            return true;
        } else {
            return false;
        }
    }

    private void performSearch(String input) {
        String searchQuery = "https://www.google.com/search?q=" + input;
        webView.loadUrl(searchQuery);
    }

    private void displayErrorSnackbar() {
        //Snackbar.make(findViewById(R.id.root_layout), "Please enter a valid 11-12 digit number.", Snackbar.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(),"Please enter a valid 11-12 digit number.",Toast.LENGTH_LONG).show();
    }

    private void appendHistory(String skuNumber) {
        try {
            FileOutputStream fos = openFileOutput("history.txt", MODE_APPEND);
            fos.write((skuNumber + "\n").getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> readHistory() {
        List<String> historyItems = new ArrayList<>();

        try {
            FileInputStream fis = openFileInput("history.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            ArrayList sku = new ArrayList<String>();

            String line;
            while ((line = reader.readLine()) != null) {
                sku.add(line);


            }

            for(int i = sku.size()-1; i > -1; --i){
                historyItems.add(sku.get(i).toString());
                TextView sku_number = new TextView(getApplicationContext());
                sku_number.setText(sku.get(i).toString());
                history_container.addView(sku_number);
                WebView history = new WebView(getApplicationContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 6000);

                history.setLayoutParams(params);
                history.clearCache(true);
                String searchQuery = "https://www.google.com/search?q=" + sku.get(i).toString();
                WebSettings settings = history.getSettings();
                settings.setJavaScriptEnabled(true);
                history.loadUrl(searchQuery);
                history_container.addView(history);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        return historyItems;
    }

}