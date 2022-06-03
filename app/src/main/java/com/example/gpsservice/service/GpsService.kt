package com.example.gpsservice.service
import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.os.Looper
import com.example.gpsservice.db.LocationDatabase
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.gpsservice.entity.Location

class GpsService : IntentService("GpsService") {
    lateinit var locationCallback: LocationCallback
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationDatabase: LocationDatabase
    private lateinit var locationRequest : LocationRequest

    companion object{
        var GPS= "cr.ac.GpsService.GPS_EVENT"
        //val GPS= "cr.ac.gpsservice.GPS"
    }

    override fun onHandleIntent(intent: Intent?) {
        locationDatabase = LocationDatabase.getInstance(this)
        getLocation()
    }


    /*
     * Inicializa los atributos locationCallback y fusedLocationClient
     * Coloca un intervalo de actualización de 1000 y una prioridad de PRIORITY_HIGH_ACCURACY
     * Recibe ubicación de gps mediante un onLocationResult
     * Envía un broadcast con una instancia de localización y la acción gps (cr.ac.gpsservice.GPS.EVENT
     * Guarda la localización en la BD)
     *
     */
    @SuppressLint("MissingPermission")
    fun getLocation(){

        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)

        //val locationRequest=LocationRequest.create()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 1000 // If not here
        locationRequest.fastestInterval = 1000  // If it can it'll do it here
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            // determina que se hace cuando hay una ubicacion gps
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationRequest==null) {
                    return
                } // Dibujar en el mapa los puntos
                for (location in locationResult.locations) {
                    val localizacion= Location(null,location.latitude,location.longitude)
                    val bcIntent=Intent()
                    bcIntent.action=GPS
                    //bcIntent.putExtra("location", locationResult.lastLocation)
                    bcIntent.putExtra("localizacion", localizacion)
                    sendBroadcast(bcIntent)
                    locationDatabase.locationDao.insert(Location(null, localizacion.latitude, localizacion.longitude))
                    LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
        Looper.loop()

    }

}
