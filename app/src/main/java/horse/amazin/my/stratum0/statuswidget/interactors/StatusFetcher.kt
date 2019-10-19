package horse.amazin.my.stratum0.statuswidget.interactors

import android.net.Uri
import android.os.SystemClock
import horse.amazin.my.stratum0.statuswidget.BuildConfig
import horse.amazin.my.stratum0.statuswidget.SpaceStatusData
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class StatusFetcher {
    private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

    fun fetch(minDelay: Int): SpaceStatusData {
        val startTime = SystemClock.elapsedRealtime()

        val result = fetch()

        val elapsedTime = SystemClock.elapsedRealtime() - startTime
        if (elapsedTime < minDelay) {
            Thread.sleep(minDelay - elapsedTime)
        }

        return result
    }

    fun fetch(): SpaceStatusData {
        val result: String
        val request = Request.Builder()
                .url(STATUS_URL.toString())
                .build()

        if (false && BuildConfig.DEBUG) {
            if (debugListIndex == DEBUG_STATUS_LIST.size) {
                debugListIndex = 0
            } else {
                Thread.sleep(200)
                debugListIndex += 1
                return DEBUG_STATUS_LIST[debugListIndex - 1]
            }
        }

        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.code() == 200) {
                result = response.body()!!.string()
            } else {
                Timber.d("Got negative http reply " + response.code())
                return SpaceStatusData.createErrorStatus()
            }
        } catch (e: IOException) {
            Timber.e(e, "IOException: " + e.message)
            return SpaceStatusData.createErrorStatus()
        }

        try {
            val jsonRoot = JSONObject(result)
            val spaceStatus = jsonRoot.getJSONObject("state")

            val lastChange = GregorianCalendar.getInstance()
            lastChange.timeInMillis = spaceStatus.getLong("lastchange") * 1000

            if (spaceStatus.getBoolean("open")) {
                return SpaceStatusData.createOpenStatus("", lastChange, lastChange)
            } else {
                return SpaceStatusData.createClosedStatus(lastChange)
            }
        } catch (e: JSONException) {
            Timber.d(e, "Error creating JSON object")
            return SpaceStatusData.createErrorStatus()
        }

    }

    companion object {
        private val STATUS_URL = Uri.parse("http://spaceapi.n39.eu/json")

        private val DEBUG_STATUS_LIST: Array<SpaceStatusData> = arrayOf(
                SpaceStatusData.createErrorStatus(),
                SpaceStatusData.createClosedStatus(Calendar.getInstance()),
                SpaceStatusData.createOpenStatus("Valodim", Calendar.getInstance(), Calendar.getInstance()))
        private var debugListIndex = DEBUG_STATUS_LIST.size
    }

}