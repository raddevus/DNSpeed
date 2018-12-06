package us.raddev.dnspeed;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView outText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        outText = (TextView) findViewById(R.id.large_text);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setText(R.id.urlText);
            }
        });
    }

    private void setText(int id){
        LayoutInflater li = LayoutInflater.from(getBaseContext());
        final View v = li.inflate(R.layout.configvalues, null);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(v.getContext());

        builder.setMessage( "Add new URL").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences urls = getApplicationContext().getSharedPreferences("urls", MODE_PRIVATE);
                        String outValues = urls.getString("urls", "");
                        Log.d("MainActivity", urls.getString("urls", ""));
                        SharedPreferences.Editor edit = urls.edit();

                        //edit.clear();

                        EditText input = (EditText) v.findViewById(R.id.urlText);
                        String currentValue = input.getText().toString();
                        if (currentValue != "") {
                            if (outValues != "") {
                                outValues += "," + currentValue;
                            } else {
                                outValues += currentValue;
                            }
                        }
                        edit.putString("urls", outValues);
                        edit.commit();
                        Log.d("MainActivity", "final outValues : " + outValues);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.setView(v);
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("MainActivity", "onOptionsSelected...");
        Log.d("MainActivity", "id : " + String.valueOf(id));
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d("MainActivity", "clicked!");
            SharedPreferences urlPrefs = getApplicationContext().getSharedPreferences("urls", MODE_PRIVATE);
            String urls = urlPrefs.getString("urls", "");

            String[] allUrls = urls.split(",");
            outText.append("\n");
            for (int i = 0; i < allUrls.length; i++)
            {
                new DNSWorker(this).execute(allUrls[i]);
            }
            return true;
        }
        if (id == R.id.clear_all) {
            SharedPreferences urls = getApplicationContext().getSharedPreferences("urls", MODE_PRIVATE);
            SharedPreferences.Editor edit = urls.edit();
            edit.clear();
            edit.commit();
            return true;
        }

        if (id == R.id.clear_main_view) {
            outText.setText("");
        }

        return super.onOptionsItemSelected(item);
    }
}
