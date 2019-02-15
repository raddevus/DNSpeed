package us.raddev.dnspeed;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import static android.telephony.TelephonyManager.PHONE_TYPE_CDMA;
import static android.telephony.TelephonyManager.PHONE_TYPE_GSM;
import static android.telephony.TelephonyManager.PHONE_TYPE_NONE;
import static android.telephony.TelephonyManager.PHONE_TYPE_SIP;
import static android.telephony.TelephonyManager.SIM_STATE_ABSENT;
import static android.telephony.TelephonyManager.SIM_STATE_CARD_IO_ERROR;
import static android.telephony.TelephonyManager.SIM_STATE_CARD_RESTRICTED;
import static android.telephony.TelephonyManager.SIM_STATE_NETWORK_LOCKED;
import static android.telephony.TelephonyManager.SIM_STATE_NOT_READY;
import static android.telephony.TelephonyManager.SIM_STATE_PERM_DISABLED;
import static android.telephony.TelephonyManager.SIM_STATE_PIN_REQUIRED;
import static android.telephony.TelephonyManager.SIM_STATE_PUK_REQUIRED;
import static android.telephony.TelephonyManager.SIM_STATE_READY;
import static android.telephony.TelephonyManager.SIM_STATE_UNKNOWN;

public class MainActivity extends AppCompatActivity {

    TextView outText;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mainImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        outText = (TextView) findViewById(R.id.large_text);
        mainImageView = (ImageView)findViewById(R.id.main_image);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setText(R.id.urlText);
            }
        });
    }

    private void setText(int id) {
        LayoutInflater li = LayoutInflater.from(getBaseContext());
        final View v = li.inflate(R.layout.configvalues, null);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(v.getContext());

        builder.setMessage("Add new URL").setCancelable(false)
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

            for (int i = 0; i < allUrls.length; i++) {
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

        if (id == R.id.take_picture) {
            dispatchTakePictureIntent();
        }
        if (id == R.id.get_phone_details) {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            String phoneDetails = tMgr.getLine1Number() + "\n";
            phoneDetails += "MMS User agent: " + tMgr.getMmsUserAgent() + "\n";
            phoneDetails += "Device software ver. - " + tMgr.getDeviceSoftwareVersion() + "\n";
            phoneDetails += "Phone type: ";
            switch (tMgr.getPhoneType()){
                case PHONE_TYPE_CDMA :
                {
                    phoneDetails += "CDMA\n";
                    break;
                }
                case PHONE_TYPE_GSM :{
                    phoneDetails += "GSM\n";
                    break;
                }
                case PHONE_TYPE_NONE:{
                    phoneDetails += "NONE\n";
                    break;
                }
                case PHONE_TYPE_SIP:{
                    phoneDetails += "SIP\n";
                    break;
                }
            }
            phoneDetails += "SIM State - ";
            switch (tMgr.getSimState()){
                case SIM_STATE_UNKNOWN :{
                    phoneDetails += "UNKNOWN\n";
                    break;
                }
                case SIM_STATE_ABSENT :{
                    phoneDetails += "ABSENT\n";
                    break;
                }
                case SIM_STATE_PIN_REQUIRED :{
                    phoneDetails += "PIN REQUIRED\n";
                    break;
                }
                case SIM_STATE_PUK_REQUIRED:{
                    phoneDetails += "PUK REQUIRED\n";
                    break;
                }
                case SIM_STATE_NETWORK_LOCKED :{
                    phoneDetails += "NETWORK LOCKED\n";
                    break;
                }
                case SIM_STATE_READY :{
                    phoneDetails += "READY\n";
                    break;
                }
                case SIM_STATE_NOT_READY:{
                    phoneDetails += "NOT READY\n";
                    break;
                }
                case SIM_STATE_PERM_DISABLED: {
                    phoneDetails += "PERM DISABLED\n";
                    break;
                }
                case SIM_STATE_CARD_IO_ERROR :{
                    phoneDetails += "CARD IO ERROR\n";
                    break;
                }
                case SIM_STATE_CARD_RESTRICTED:{
                    phoneDetails += "CARD RESTRICTED\n";
                    break;
                }
            }
            phoneDetails += "voicemail # : " + tMgr.getVoiceMailNumber() + "\n";
            phoneDetails += "SIM Serial #: " + tMgr.getSimSerialNumber() + "\n";
            phoneDetails += "Network Operator Name: " + tMgr.getNetworkOperatorName() + "\n";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                phoneDetails += "IMEI: " + tMgr.getImei() + "\n";
                phoneDetails += "MEID: " + tMgr.getMeid() + "\n";
                phoneDetails += "Visual VMail Pkg Name: " + tMgr.getVisualVoicemailPackageName() + "\n";
            }
            outText.setText(phoneDetails);
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mainImageView.setImageBitmap(imageBitmap);
        }
    }
}
