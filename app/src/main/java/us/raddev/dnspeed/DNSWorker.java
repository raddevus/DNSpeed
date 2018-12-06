package us.raddev.dnspeed;
/**
 * Created by Roger on 3/6/2016.
 */
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;

public class DNSWorker extends AsyncTask<String, Void, String> {
private Exception exception;
private TextView largeText;
private Activity activity;

    public DNSWorker(Activity activity) {
        this.activity = activity;
    }

    protected String doInBackground(String... urls) {
        try {
            Log.d("MainActivity", urls[0]);
            long startTime = System.currentTimeMillis();
            InetAddress address = InetAddress.getByName(urls[0]);
            long difference = System.currentTimeMillis() - startTime;
            return urls[0] + " : " +  address.getHostAddress() + " : " + String.valueOf(difference) + "ms";

        } catch (Exception e) {
        this.exception = e;
        return urls[0] + " failed!";
        }
}

protected void onPostExecute(String output) {
        Log.d("MainActivity", output);
        output += "\n";
        largeText = (TextView) activity.findViewById(R.id.large_text);
        largeText.append(output);
    }
}
