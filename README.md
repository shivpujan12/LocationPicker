<h1>Introduction</h1>
LocationPicker is a simple and easy to use library that can be integrated into your project.
You can get latitude,longitude and full address for selected location
Please follow below instruction to know how to use it in your project

<br/><br/>
<h1>Features</h1>

1. Search any location using Google Places Library<br/>
2. Pick any location from the map<br/>
3. Open the picked location on Google Maps<br/>
4. Search the Direction to the picked location from current location (using Google Maps)<br/> 

<div style="float:left">
<img src="Screenshots/Screen1.png" width="200">
<img src="Screenshots/Screen2.png" width="200">
<img src="Screenshots/Screen3.png" width="200">
</div>


<br/><br/>
<h1>Getting Started</h1>

To use this component in your project you need to perform below steps:

1) Configure your app in  Google API Console  to get API Key and enable services.

> https://console.cloud.google.com/apis/dashboard

2) Enable below services in API Console.

```
  Google Maps Android API
  
  Google Places API for Android
  
```

3) Declare the following things in manifest in AndroidManifest.xml file

```
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

inside ```<application>``` tag add ```<meta-data>``` as shown below

```
  <application
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        ....
        ....
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="@string/your_api_key" />
    </application>
```

<br/>
<p><b>Note:</b> Create the 'your_api_key' string resource and add your api key there</p>

4) To use the LocationPicker in your activity add the following code:

    i) Inside onCreate method intialize your api key as below:<br/>
    ```
          MapUtility.apiKey = getResources().getString(R.string.your_api_key);
    ```
      <p><b>Note:</b> Create the 'your_api_key' string resource and add your api key there</p>
    
    ii) Start Location picker request by putting below code in your view<br/>
    ```
                  Intent i = new Intent(MainActivity.this, LocationPickerActivity.class);
                  startActivityForResult(i, ADDRESS_PICKER_REQUEST);
    ```

5) Handle your onActivityResult for getting address, latitude and longitude as:

```
     @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDRESS_PICKER_REQUEST) {
            try {
                if (data != null && data.getStringExtra(MapUtility.ADDRESS) != null) {
                    String address = data.getStringExtra(MapUtility.ADDRESS);
                    double selectedLatitude = data.getDoubleExtra(MapUtility.LATITUDE, 0.0);
                    double selectedLongitude = data.getDoubleExtra(MapUtility.LONGITUDE, 0.0);
                    txtAddress.setText("Address: "+address);
                    txtLatLong.setText("Lat:"+selectedLatitude+"  Long:"+selectedLongitude);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
```

<h1>Extra Feature ;)</h1>
<br/>
To open the LocationPicker with pre-selected location, just add extras to the Intent as below: <br/>

```
      Intent intent = new Intent(EditorActivity.this, LocationPickerActivity.class);
      intent.putExtra(MapUtility.ADDRESS,address);
      intent.putExtra(MapUtility.LATITUDE, latitude);
      intent.putExtra(MapUtility.LONGITUDE, longitude);
      startActivityForResult(intent, ADDRESS_PICKER_REQUEST);
```
<br/><br/>

<h1>Bugs and Feedback</h1>
For bugs, questions and discussions please use the Github Issues.
<br/><br/>

<h1>Reference</h1>
The library is referenced from https://github.com/Intuz-production/AddressPicker-Android<br/>
<br/><br/>
