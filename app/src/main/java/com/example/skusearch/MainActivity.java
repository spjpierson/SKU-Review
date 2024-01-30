
package com.example.skusearch;

import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    FirebaseUser user = null;

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
                String email = "spjpierson@gmail.com";
                String password = "password";

                if(user == null) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    user = task.getResult().getUser();
                                    String message = "Database was logged in by: " + user.getEmail();
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                } else {
                                    // Handle failure gracefully
                                    String message = "Login failed: " + task.getException().getMessage();
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }
                            });
                }else{
                     //Look key up value
                    String searchKey = input_edit_text.getText().toString();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.child(searchKey).get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            DataSnapshot snapshot = task.getResult();
                            if(snapshot.exists()){
                                String value = snapshot.getValue(String.class);
                                webView.loadData(value,"text.html","UTF-8");
                            }else{
                                webView.loadData("No item found","text.html","UTF-8");
                                openProductDescriptionDialog();
                            }
                        }else{
                            String message = "Database error: " + task.getException().getMessage();
                            Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
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

    public void openProductDescriptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_description, null);
        final EditText inputDialog = dialogView.findViewById(R.id.input_dialog); // Assuming EditText ID

        builder.setView(dialogView)
                .setPositiveButton("Enter", (dialog, which) -> {
                    String productDescription = inputDialog.getText().toString();
                    String productKey = input_edit_text.getText().toString(); // Assuming EditText ID

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.child(productKey).setValue(productDescription)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Product description added successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Error adding product description", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}