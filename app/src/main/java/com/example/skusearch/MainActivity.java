
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

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ImageButton camera_button;
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

        camera_button = findViewById(R.id.camera_button);

        input_edit_text = findViewById(R.id.input_edit_text);

        search_online_button = findViewById(R.id.online_search_button);
        search_database_button = findViewById(R.id.database_search_button);

        history_container = findViewById(R.id.history_container);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.clearCache(true);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(), "Camera button was press", Toast.LENGTH_SHORT).show();

            }
        });

        search_online_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    String input = input_edit_text.getText().toString();
                    // Validate input length and format
                    if (isValidInput(input)) {
                        performSearch(input);
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
}