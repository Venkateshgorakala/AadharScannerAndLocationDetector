package notifications.test.com.aadhardemo.AadharValidation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import notifications.test.com.aadhardemo.R;
import notifications.test.com.aadhardemo.helpers.NetworkHelper;

public class AadharValidationActivity extends AppCompatActivity implements LocationListener {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private String uid = "", name = "", gender = "", house = "", street = "", lm = "", loc = "", yearOfBirth = "", careOf = "",
            villageTehsil = "", postOffice = "", district = "", state = "", postCode = "";
    private EditText et_aadhaar_no, et_name, et_gender, et_dob, et_address_line_one, et_address_line_two, et_address_line_three,
            et_location_line_one, et_loc_add_line_two, et_loc_lat_log;
    public static final long MIN_DISTANCE_BWT_METERS = 10;
    public static final long TIME_BTW_UPDATES = 1000 * 60 * 1;
    private LocationManager locationManager;
    private Location location;
    private double latitude, longitute;
    private Button btn_fetch_location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aadhar_validation);
        init();
    }

    public void init() {
        et_aadhaar_no = findViewById(R.id.et_aadhaar_no);
        et_name = findViewById(R.id.et_name);
        et_gender = findViewById(R.id.et_gender);
        et_dob = findViewById(R.id.et_dob);
        et_address_line_one = findViewById(R.id.et_address_line_one);
        et_address_line_two = findViewById(R.id.et_address_line_two);
        et_address_line_three = findViewById(R.id.et_address_line_three);
        et_location_line_one = findViewById(R.id.et_location_line_one);
        et_loc_add_line_two = findViewById(R.id.et_loc_add_line_two);
        et_loc_lat_log = findViewById(R.id.et_loc_lat_log);
        btn_fetch_location = findViewById(R.id.btn_fetch_location);

        et_name.setInputType(InputType.TYPE_NULL);
        et_dob.setInputType(InputType.TYPE_NULL);
        et_aadhaar_no.setInputType(InputType.TYPE_NULL);
        et_address_line_one.setInputType(InputType.TYPE_NULL);
        et_address_line_two.setInputType(InputType.TYPE_NULL);
        et_address_line_three.setInputType(InputType.TYPE_NULL);
        et_location_line_one.setInputType(InputType.TYPE_NULL);
        et_loc_lat_log.setInputType(InputType.TYPE_NULL);
        et_loc_add_line_two.setInputType(InputType.TYPE_NULL);
        et_gender.setInputType(InputType.TYPE_NULL);


    }

    public void ScanAadhar(View view) {
        Button btn_scan_aadhar = findViewById(R.id.btn_scan_aadhar);
        et_name.setText("");
        et_gender.setText("");
        et_dob.setText("");
        et_aadhaar_no.setText("");
        et_address_line_one.setText("");
        et_address_line_two.setText("");
        et_address_line_three.setText("");
        if (ContextCompat.checkSelfPermission(AadharValidationActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(AadharValidationActivity.this,
                    new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            return;
        }
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan a Aadharcard QR Code");
        //integrator.setResultDisplayDuration(500);
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.initiateScan();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            // process received data
            if (scanContent != null && !scanContent.isEmpty()) {
                processScannedData(scanContent);
            } else {
                Toast.makeText(getApplicationContext(), "Scan Cancelled", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * process xml string received from aadhaar card QR code
     *
     * @param scanData
     */

    protected void processScannedData(String scanData) {
        XmlPullParserFactory pullParserFactory;
        //String uid="",name="",gender="",yearOfBirth="",careOf="",villageTehsil="",postOffice="",district="",state="",postCode="";
        try {
            // init the parserfactory
            pullParserFactory = XmlPullParserFactory.newInstance();
            // get the parser
            XmlPullParser parser = pullParserFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(scanData));

            // parse the XML
            int eventType = parser.getEventType();
            Log.e("Aadhaar", "Name:- " + parser.getName());
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d("Rajdeol", "Start document");
                } else if ("PrintLetterBarcodeData".equals(parser.getName())) {
                    // extract data from tag
                    //uid
                    uid = parser.getAttributeValue("", "uid");
                    //name
                    name = parser.getAttributeValue("", "name");
                    //gender
                    gender = parser.getAttributeValue("", "gender");
                    //house
                    house = parser.getAttributeValue("", "house");
                    //street
                    street = parser.getAttributeValue("", "Street");
                    //survey no
                    lm = parser.getAttributeValue("", "lm");
                    //Locality
                    loc = parser.getAttributeValue("", "loc");

                    if (parser.getAttributeValue("", "dob") != null) {
                        yearOfBirth = parser.getAttributeValue("", "dob");
                    } else if (parser.getAttributeValue("", "yob") != null) {
                        yearOfBirth = parser.getAttributeValue("", "yob");
                    }
                    // year of birth

                    // care of
                    careOf = parser.getAttributeValue("", "co");
                    // village Tehsil
                    villageTehsil = parser.getAttributeValue("", "vtc");
                    // Post Office
                    postOffice = parser.getAttributeValue("", "po");
                    // district
                    district = parser.getAttributeValue("", "dist");
                    // state
                    state = parser.getAttributeValue("", "state");
                    // Post Code
                    postCode = parser.getAttributeValue("", "pc");

                } else if (eventType == XmlPullParser.END_TAG) {
                    Log.d("Rajdeol", "End tag " + parser.getName());

                } else if (eventType == XmlPullParser.TEXT) {
                    Log.d("Rajdeol", "Text " + parser.getText());

                }
                // update eventType
                eventType = parser.next();
            }
            // display the data on screen
            displayScannedData();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            Toast.makeText(AadharValidationActivity.this, "Invalid QR Code", Toast.LENGTH_SHORT).show();

        }

    }// EO function

    /**
     * show scanned information
     */
    public void ReplaceNullVal() {
        if (uid == null) {
            uid = "";
        }
        if (name == null) {
            name = "";
        }
        if (gender == null) {
            gender = "";
        }
        if (yearOfBirth == null) {
            yearOfBirth = "";
        }
        if (careOf == null) {
            careOf = "";
        }
        if (villageTehsil == null) {
            villageTehsil = "";
        }
        if (house == null) {
            house = "";
        }
        if (lm == null) {
            lm = "";
        }
        if (loc == null) {
            loc = "";
        }
        if (district == null) {
            district = "";
        }
        if (state == null) {
            state = "";
        }
        if (street == null) {
            street = "";
        }
        if (postCode == null) {
            postCode = "";
        }

    }

    public void displayScannedData() {
        ReplaceNullVal();
        if (gender.equals("M")) {
            et_gender.setText("Male");
        } else if (gender.equals("F")) {
            et_gender.setText("Female");
        }
        et_name.setText(name);
        et_dob.setText("Year Of Birth : " + yearOfBirth);
        et_aadhaar_no.setText(uid);
        et_address_line_one.setText("Address: \n" + careOf + ", " + house + ", ");
        et_address_line_two.setText(street + ", " + lm + ", " + loc + ", " + villageTehsil + ", " + district);
        et_address_line_three.setText(state + ", " + postCode);



      /*  Toast.makeText(AadharValidationActivity.this, "UID:- " + uid + "\n" + name + "\n" + gender + "\n"
                        + yearOfBirth + "\n" + careOf + "\n" + villageTehsil + "\n"
                        + postOffice + "\n" + district + "\n" + state + "\n" + postCode,
                Toast.LENGTH_SHORT).show();*/
    }

    /*public void saveData(View view) {
        // We are going to use json to save our data
        // create json object
        JSONObject aadhaarData = new JSONObject();
        try {
            aadhaarData.put("uid", uid);

            if (name == null) {
                name = "";
            }
            aadhaarData.put("name", name);

            if (gender == null) {
                gender = "";
            }
            aadhaarData.put("gender", gender);

            if (yearOfBirth == null) {
                yearOfBirth = "";
            }
            aadhaarData.put("yearOfBirth", yearOfBirth);

            if (careOf == null) {
                careOf = "";
            }
            aadhaarData.put("co", careOf);

            if (villageTehsil == null) {
                villageTehsil = "";
            }
            aadhaarData.put("vtc", villageTehsil);

            if (postOffice == null) {
                postOffice = "";
            }
            aadhaarData.put("po", postOffice);

            if (district == null) {
                district = "";
            }
            aadhaarData.put("dist", district);

            if (state == null) {
                state = "";
            }
            aadhaarData.put("state", state);

            if (postCode == null) {
                postCode = "";
            }
            aadhaarData.put("pc", postCode);

            // read data from storage
            // String storageData = storage.readFromFile();

            //JSONArray storageDataArray;
            //check if file is empty
            *//*if(storageData.length() > 0){
                storageDataArray = new JSONArray(storageData);
            }else{
                storageDataArray = new JSONArray();
            }
            // check if storage is empty
            if(storageDataArray.length() > 0){
                // check if data already exists
                for(int i = 0; i<storageDataArray.length();i++){
                    String dataUid = storageDataArray.getJSONObject(i).getString(DataAttributes.AADHAR_UID_ATTR);
                    if(uid.equals(dataUid)){
                        // do not save anything and go back
                        // show home screen
                        tv_cancel_action.performClick();

                        return;
                    }
                }
            }
            // add the aadhaar data
            storageDataArray.put(aadhaarData);
            // save the aadhaardata
            storage.writeToFile(storageDataArray.toString());

            // show home screen
            tv_cancel_action.performClick();
*//*
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/


    public void getlocations() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsisEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isnetworkEnaled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (!isGpsisEnabled && !isnetworkEnaled) {
            try {
                Toast.makeText(AadharValidationActivity.this, "Please enable Location and GPS ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(AadharValidationActivity.this, "Location Denied at isnetworkEnaled ", Toast.LENGTH_SHORT).show();

            }


        } else if (isGpsisEnabled) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(AadharValidationActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_CAMERA_REQUEST_CODE);
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_BTW_UPDATES, MIN_DISTANCE_BWT_METERS, (LocationListener) AadharValidationActivity.this);

            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location != null) {
                try {
                    latitude = location.getLatitude();
                    longitute = location.getLongitude();
                    Geocoder geocoder = new Geocoder(AadharValidationActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitute, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert addresses != null;
                    String city = addresses.get(0).getLocality();
                  /* String state = addresses.get(0).getSubLocality();
                    String zip = addresses.get(0).getPostalCode();
                    String country = addresses.get(0).getCountryName();
                    String StateLoc = addresses.get(0).getAdminArea();*/
                    String m1 = addresses.get(0).getAddressLine(0);
                    et_location_line_one.setText(city);
                    et_loc_add_line_two.setText(m1);
                    et_loc_lat_log.setText("Lat : " + latitude + ", " + "Long : " + longitute);

                } catch (Exception e) {
                    //callLocationSnackBar();
                }


            }
        }
    }

    public void Fetch_Location(View view) {
        if (NetworkHelper.isConnected(AadharValidationActivity.this)) {
            getlocations();
        } else {
            Toast.makeText(AadharValidationActivity.this, "Please Connect to Internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(AadharValidationActivity.this, "New Location detected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(AadharValidationActivity.this, "Connected to internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(AadharValidationActivity.this, "Please connect to internet", Toast.LENGTH_SHORT).show();
    }
}
