package com.hakangeyik.mymapsproject

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private val REQUEST_CODE = 30
    private lateinit var criteria: Criteria
    private lateinit var builder: LatLngBounds.Builder
    private val burganLocation = LatLng(41.1085804,29.0161468)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        builder = LatLngBounds.Builder()

        criteria = Criteria()
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        var looper: Looper? = null

        locationListener = object: LocationListener{
            override fun onLocationChanged(p0: Location) {
                if (p0!=null){
                    val userLocation = LatLng(p0.latitude,p0.longitude)
                    builder.include(burganLocation)
                    builder.include(userLocation)
                    val bounds: LatLngBounds = builder.build()

                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(userLocation).title("You are here"))
                    mMap.addMarker(MarkerOptions().position(burganLocation).title("Burgan Bank"))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 2000, null)
                }
            }

            //Gps kapalıysa ne olacak
            override fun onProviderDisabled(provider: String) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    alertNoGpsMessage()
                }

            }

            //Kişi gps'i açtığında ne olacak
            override fun onProviderEnabled(provider: String) {
                //Low battery mode'da aktif edersem bu aptal yere girmiyor uygulama saçma bir şekilde acaba criteria problemi mi
                //Alakası yok test edildi garip bir durum kesin reise sor
                Toast.makeText(applicationContext, "Gps has been activated Please wait until we reach your location", Toast.LENGTH_LONG).show()
                val intent = Intent(applicationContext, MapsActivity::class.java)
                startActivity(intent)
            }

        }

        //Permission
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_CODE)
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            locationManager.requestSingleUpdate(criteria,locationListener,looper)
        }

    }

    private fun alertNoGpsMessage(){
        val dialog = AlertDialog.Builder(this)
                .setMessage("Your gps seems disabled. Do you want to enable your gps?")
                .setCancelable(false)
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                })
                .setNegativeButton("No", DialogInterface.OnClickListener{ dialog, id ->
                    Toast.makeText(applicationContext,"You must open your gps!",Toast.LENGTH_LONG).show()
                    dialog.cancel()
                })
        val alert = dialog.create()
        alert.show()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.size > 0) {
                if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    var looper: Looper? = null
                    locationManager.requestSingleUpdate(criteria,locationListener,looper)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)

                }
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            }
        }
    }

}