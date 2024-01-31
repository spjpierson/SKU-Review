
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
    private String file = "history_sku_search.txt";


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

        String email = "spjpierson@gmail.com";
        String password = "password";
            if (user == null) {
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
            }

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
                        appendHistory(input,"Online");
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
                if(isValidInput(input_edit_text.getText().toString())) {
                    if (user == null) {
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
                    } else {
                        //Look key up value
                        String searchKey = input_edit_text.getText().toString();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child(searchKey).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DataSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    String value = snapshot.getValue(String.class);
                                    webView.loadData(value, "text.html", "UTF-8");
                                    String input = input_edit_text.getText().toString() + " : " + value;
                                    appendHistory(input, "Database");
                                    history_container.removeAllViews();
                                    readHistory();
                                } else {
                                    webView.loadData("No item found", "text.html", "UTF-8");
                                    openProductDescriptionDialog();
                                    String input = input_edit_text.getText().toString() + " :  No item found";
                                }
                            } else {
                                String message = "Database error: " + task.getException().getMessage();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                } else{
                    Toast.makeText(getApplicationContext(),"Please Enter In 11-12 digital Number",Toast.LENGTH_SHORT).show();
                }
            }
        });

        search_database_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String searchKey = input_edit_text.getText().toString(); // Get key from input

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child(searchKey).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            // Key exists, show dialog to update value
                            showUpdateDialog(searchKey, snapshot.getValue(String.class));
                        } else {
                            // Key doesn't exist, show Toast
                            Toast.makeText(getApplicationContext(), "No item associated with that value", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle database error
                        Toast.makeText(getApplicationContext(), "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                return true; // Consume the long click event
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

    private void appendHistory(String skuNumber, String searchType) {
        try {
            FileOutputStream fos = openFileOutput(file, MODE_APPEND);
            fos.write((searchType + "_" + skuNumber + "\n").getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> readHistory() {
        List<String> historyItems = new ArrayList<>();

        try {
            FileInputStream fis = openFileInput(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            ArrayList sku = new ArrayList<String>();

            String line;
            while ((line = reader.readLine()) != null) {
                sku.add(line);


            }

            for(int i = sku.size()-1; i > -1; --i){
                String searchType = ((String) sku.get(i)).substring(0, ((String) sku.get(i)).indexOf("_"));
                String entry = ((String) sku.get(i)).substring(((String) sku.get(i)).indexOf("_") + 1);




                if (searchType.equals("Online")) {
                    historyItems.add(sku.get(i).toString());

                    TextView state = new TextView(getApplicationContext());
                    state.setText(searchType);

                    history_container.addView(state);

                    TextView sku_number = new TextView(getApplicationContext());
                    sku_number.setText(entry);

                    history_container.addView(sku_number);
                    WebView history = new WebView(getApplicationContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 6000);

                    history.setLayoutParams(params);
                    history.clearCache(true);
                    String searchQuery = "https://www.google.com/search?q=" + entry;
                    WebSettings settings = history.getSettings();
                    settings.setJavaScriptEnabled(true);
                    history.loadUrl(searchQuery);
                    history_container.addView(history);
                } else if (searchType.equals("Database")) {

                    TextView databaseText = new TextView(getApplicationContext());
                    databaseText.setText(searchType);

                    TextView sku_number_and_value = new TextView(getApplicationContext());
                    sku_number_and_value.setText(entry);

                    history_container.addView(databaseText);
                    history_container.addView(sku_number_and_value);
                }
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
                                    String input = input_edit_text.getText().toString() + " :  Insert new Item -> " + productDescription;
                                    history_container.removeAllViews();
                                    appendHistory(input,"Database");
                                    history_container.removeAllViews();
                                    readHistory();
                                } else {
                                    Toast.makeText(this, "Error adding product description", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    String input = input_edit_text.getText().toString() + " :  No item found";
                    history_container.removeAllViews();
                    appendHistory(input,"Database");
                    history_container.removeAllViews();
                    readHistory();
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Define the showUpdateDialog method
    private void showUpdateDialog(String searchKey, String currentValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText inputDialog = new EditText(this); // Input field for new value
        inputDialog.setText(currentValue); // Pre-fill with current value

        builder.setView(inputDialog)
                .setTitle("Update Value")
                .setPositiveButton("Update", (dialog, which) -> {
                    String newValue = inputDialog.getText().toString();
                    updateDatabaseValue(searchKey, newValue);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Define the updateDatabaseValue method
    private void updateDatabaseValue(String searchKey, String newValue) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(searchKey).setValue(newValue)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Value updated successfully", Toast.LENGTH_SHORT).show();
                        appendHistory(input_edit_text.getText().toString() + "Update Value ->"+newValue,"Database");
                        history_container.removeAllViews();
                        readHistory(); // Refresh history display
                    } else {
                        Toast.makeText(this, "Error updating value", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}