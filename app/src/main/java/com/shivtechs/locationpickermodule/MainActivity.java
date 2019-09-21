package com.shivtechs.locationpickermodule;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shivtechs.maplocationpicker.LocationPickerActivity;
import com.shivtechs.maplocationpicker.MapUtility;


public class MainActivity extends Activity implements View.OnClickListener {

    private TextView txtLatLong;
    private TextView txtAddress;
    private static final int ADDRESS_PICKER_REQUEST = 1020;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapUtility.apiKey = getResources().getString(R.string.api_key);
        findViewById(R.id.btnAddressPicker).setOnClickListener(this);
        txtLatLong = findViewById(R.id.txtLatLong);
        txtAddress = findViewById(R.id.txtAddress);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddressPicker:
                Intent intent = new Intent(MainActivity.this, LocationPickerActivity.class);
                startActivityForResult(intent, ADDRESS_PICKER_REQUEST);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADDRESS_PICKER_REQUEST) {
            try {
                if (data != null && data.getStringExtra(MapUtility.ADDRESS) != null) {
                   // String address = data.getStringExtra(MapUtility.ADDRESS);
                    double currentLatitude = data.getDoubleExtra(MapUtility.LATITUDE, 0.0);
                    double currentLongitude = data.getDoubleExtra(MapUtility.LONGITUDE, 0.0);
                    Bundle completeAddress =data.getBundleExtra("fullAddress");
                    /* data in completeAddress bundle
                    "fulladdress"
                    "city"
                    "state"
                    "postalcode"
                    "country"
                    "addressline1"
                    "addressline2"
                     */

                    txtAddress.setText(new StringBuilder().append("addressline2: ").append
                            (completeAddress.getString("addressline2")).append("\ncity: ").append
                            (completeAddress.getString("city")).append("\npostalcode: ").append
                            (completeAddress.getString("postalcode")).append("\nstate: ").append
                            (completeAddress.getString("state")).toString());

                    txtLatLong.setText(new StringBuilder().append("Lat:").append(currentLatitude).append
                            ("  Long:").append(currentLongitude).toString());

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
