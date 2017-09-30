package sk.hyll.dominik.exercise12_jsontogooglemaps

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.widget.TextView
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.google.android.gms.maps.model.Marker




class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
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
        mMap = googleMap

        mMap!!.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {

                val info = LinearLayout(applicationContext)
                info.orientation = LinearLayout.VERTICAL

                val title = TextView(applicationContext)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title

                val snippet = TextView(applicationContext)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet

                info.addView(title)
                info.addView(snippet)

                return info
            }
        })

        FetchJsonGolfInfoTask().execute("http://ptm.fi/materials/golfcourses/golf_courses.json")
    }

    internal inner class FetchJsonGolfInfoTask : AsyncTask<String, Void, JSONObject?>() {
        override fun doInBackground(vararg urls: String): JSONObject? {
            var urlConnection: HttpURLConnection? = null
            var json: JSONObject? = null
            try {
                val url = URL(urls[0])
                urlConnection = url.openConnection() as HttpURLConnection
                val bufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val stringBuilder = StringBuilder()
                var line = bufferedReader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = bufferedReader.readLine()
                }
                bufferedReader.close()
                json = JSONObject(stringBuilder.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                if (urlConnection != null) urlConnection.disconnect()
            }
            return json
        }

        override fun onPostExecute(json: JSONObject?) {

            if(json == null) return

            val colors = mapOf("Kulta" to BitmapDescriptorFactory.HUE_GREEN , "Kulta/Etu" to BitmapDescriptorFactory.HUE_ORANGE, "Etu" to BitmapDescriptorFactory.HUE_VIOLET, "?" to BitmapDescriptorFactory.HUE_CYAN)

            val courses = json.getJSONArray("courses")

            var latlng: LatLng? = null
            for ( i in 0..courses.length()-1){
                val course:JSONObject = courses[i] as JSONObject
                 latlng = LatLng(course.getDouble("lat"),course.getDouble("lng"))
                val color = colors[course.getString("type")] ?: BitmapDescriptorFactory.HUE_RED
                val snippet = course.getString("address") +"\n" + course.getString("phone") +"\n" + course.getString("email") +"\n" + course.getString("web")


                val marker = MarkerOptions().position(latlng)
                        .title(course.getString("course"))
                        .icon(BitmapDescriptorFactory.defaultMarker(color))
                        .snippet(snippet)
                mMap!!.addMarker(marker)
            }

            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,6f))

        }
    }


}
