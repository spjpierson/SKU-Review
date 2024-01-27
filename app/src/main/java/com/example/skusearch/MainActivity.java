
package com.example.skusearch;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ImageButton camera_button;
    EditText input_edit_text;
    Button search_online_button;
    Button search_database_button;
    LinearLayout history_container;

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
    }
}