package com.example.locationcamera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    val PERMISSION_REQUEST_ACCESS_LOCATION = 100

    // location on camera
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvAddressTitle: TextView
    private lateinit var tvFullAddress: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvLong: TextView
    private lateinit var tvDate: TextView
    private lateinit var llDisplayAddress: LinearLayout
    val rotate = 0
    private lateinit var mLastLocation : Location

//     lateinit var llDisplayAddress: LinearLayout


    var addresses: List<Address>? = null
    private lateinit var  geocoder: Geocoder
    private lateinit var  mMap: GoogleMap

    private var village: String = ""
    private var state: String = ""
    private var district: String = ""
    private var country: String = ""
    private var area: String = ""
    private var lati: Double = 0.0
    private var longi: Double = 0.0

    private var utils : Utils = Utils()
    private  var locationBitmap : Bitmap? = null
    private  var bitmap : ImageCapture? = null
    private  var new : Bitmap? = null


//location on camera

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //        Initialization

        tvAddressTitle = findViewById(R.id.tvAddressTitle)
        tvFullAddress = findViewById(R.id.tvFullAddress)
        tvLat = findViewById(R.id.tvLat)
        tvLong = findViewById(R.id.tvLong)
        tvDate = findViewById(R.id.tvDate)
        llDisplayAddress = findViewById(R.id.llDisplayAddress)
        geocoder = Geocoder(this@MainActivity)
//        llDisplayAddress = findViewById(R.id.llDisplayAddress)

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)



        Log.i("ADDRESS","before getting loc is ${tvFullAddress.text}")
        getCurrentLocation()

        Log.i("ADDRESS","after got loc is ${tvFullAddress.text}")
//        bitmap = utils.createBitmapFromLayout(llDisplayAddress)


//        utils.saveToGallery(this, locationBitmap!!,"DCIM")

//





//        Initialization

        // hide the action bar
        supportActionBar?.hide()

        // Check camera permissions if all permission granted
        // start camera else ask for the permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // set on click listener for the button of capture photo
        // it calls a method which is implemented below

        findViewById<Button>(R.id.camera_capture_button).setOnClickListener {
            takePhoto()
            new?.let { it1 -> utils.saveToGallery(this, it1,"DCIM") }
//            bitmap?.let { it1 -> utils.saveToGallery(this, it1,"DCIM") }

        }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the
        // modifiable image capture use case
        val imageCapture = imageCapture ?: return
        bitmap = imageCapture

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener,
        // which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    runOnUiThread {
                        val yo: Bitmap = BitmapFactory.decodeFile(photoFile.path)


//                       new  =  utils.overlay(yo,locationBitmap!!)
//                       new  =  utils.bitmapOverlayToCenter(yo,locationBitmap!!)
                     val   newLocation = utils.getResizedBitmap(locationBitmap!!, locationBitmap!!.width*2,locationBitmap!!.height*2)
//                        val resizedLocation = utils.getResizedBitmap(newLocation!!,100,50)!!
                        new = utils.mark(yo,newLocation!!)
                        Log.i("NOOOW","yo i s$yo")
                        Log.i("NOOOW","loco i s$locationBitmap")
                        Log.i("NOOOW","new i s$new")
                        Log.i("ADDRESS","after captured in getLoc fun i s${tvFullAddress.text}")

                                utils.saveToGallery(applicationContext,new!!,"DCIM")
                        Toast.makeText(this@MainActivity, "Image saved!", Toast.LENGTH_LONG).show()
                    }

                    // set the saved uri to the image view
//                    findViewById<ImageView>(R.id.iv_capture).visibility = View.VISIBLE
//                    findViewById<ImageView>(R.id.iv_capture).setImageURI(savedUri)

                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {

            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // creates a folder inside internal storage
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    // checks the camera permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // If all permissions granted , then start Camera
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // If permissions are not granted,
                // present a toast to notify the user that
                // the permissions were not granted.
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> getCurrentLocation()
                PackageManager.PERMISSION_DENIED -> Toast.makeText(this@MainActivity,"Permission Denied",Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXGFG"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync(OnMapReadyCallback {
                    var latLng = LatLng(location.latitude, location.longitude)

                    Log.i("LATI", " current funct is $lati & Longi is $longi")
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))


                    Log.e("getCurrentLocation", latLng.latitude.toString() + "-" + latLng.longitude)
                })
            }
        }




    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.setAllGesturesEnabled(false)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mMap.isMyLocationEnabled = true

     fusedLocationProviderClient.lastLocation.addOnSuccessListener { location->

         if (location != null){
             mLastLocation = location
             val currentLatLng = LatLng(location.latitude,location.longitude)
             Log.i("LATI", " ready is ${currentLatLng.latitude} & Longi is ${currentLatLng.longitude}")

             lati = currentLatLng.latitude
             longi = currentLatLng.longitude

             mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

             addresses = geocoder.getFromLocation(lati, longi, 1)
             state = addresses!![0].adminArea
             district = addresses!![0].locality
             country = addresses!![0].countryName
             area =  addresses!![0].getAddressLine(0)

             val sdf = SimpleDateFormat("dd/M/yyyy")
             val currentDate = sdf.format(Date())

             tvDate.text = currentDate
             tvFullAddress.text = area
             tvAddressTitle.text = "$district, $state, $country"
             tvLat.text = "Lat ${lati}"
             tvLong.text = "Lat ${longi}"

             locationBitmap = utils.createBitmapFromLayout(llDisplayAddress)

         }
     }
        val currentLocation = LatLng(lati, longi)
        Log.i("LATI", "Onmap ready is $lati & Longi is $longi")

        mMap.addMarker(MarkerOptions().position(currentLocation).visible(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10f))
        mMap.moveCamera(CameraUpdateFactory.zoomIn())
        mMap.animateCamera(CameraUpdateFactory.zoomTo(50f), 2000, null)
    }




}