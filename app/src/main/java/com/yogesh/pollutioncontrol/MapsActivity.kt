package com.yogesh.pollutioncontrol

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

	private lateinit var map: GoogleMap

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_maps)

		AndroidNetworking.initialize(applicationContext)

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		val mapFragment = supportFragmentManager
				.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync(this)
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	override fun onMapReady(googleMap: GoogleMap) {
		map = googleMap
		map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(12.0, 77.0)))

		fetchThingSpeakApi()
	}

	private fun fetchThingSpeakApi() {
		AndroidNetworking.get("https://api.thingspeak.com/channels/457238/fields/1/last?api_key=HUGTIR2KWKSEUE3I")
				.build()
				.getAsString(object : StringRequestListener {
					override fun onResponse(pollution1: String) {
						Log.i("MapsActivity", "pollution1: $pollution1")
						val pollutionLevel1 = pollution1.toDouble()

						AndroidNetworking.get("https://api.thingspeak.com/channels/457238/fields/2/last?api_key=HUGTIR2KWKSEUE3I")
								.build()
								.getAsString(object : StringRequestListener {
									override fun onResponse(pollution2: String) {
										Log.i("MapsActivity", "pollution2: $pollution2")
										val pollutionLevel2 = pollution2.toDouble()

										val chikmaglur = LatLng(13.1285, 77.5873)
										val bangalore = LatLng(12.9716, 77.5946)

										val heatmap = HeatmapTileProvider.Builder()
												.weightedData(listOf(
														WeightedLatLng(chikmaglur, pollutionLevel1),
														WeightedLatLng(bangalore, pollutionLevel2)
												))
												.gradient(Gradient(intArrayOf(Color.GREEN, Color.BLACK), floatArrayOf(0.25F, 0.5F)))
												.build()

										val marker1 = MarkerOptions().position(chikmaglur).title(pollution1)
										val marker2 = MarkerOptions().position(bangalore).title(pollution2)

										map.clear()
										map.addTileOverlay(TileOverlayOptions().tileProvider(heatmap))
										map.addMarker(marker1)
										map.addMarker(marker2)

										val builder = LatLngBounds.Builder()
										builder.include(chikmaglur)
										builder.include(bangalore)
										val bounds = builder.build()
										val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 150)
										map.moveCamera(cameraUpdate)
									}

									override fun onError(anError: ANError?) {
									}

								})
					}

					override fun onError(anError: ANError?) {
					}

				})
	}

	fun refreshData(view: View) {
		fetchThingSpeakApi()
	}
}
