package horse.amazin.my.stratum0.statuswidget.interactors

import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import horse.amazin.my.stratum0.statuswidget.BuildConfig
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.ExecutionException

class StatusUpdater {
    fun update(name: String?) {
        val queryString = if (name != null) {
            UPDATE_OPEN
        } else {
            UPDATE_CLOSE
        }.toString()

        if (BuildConfig.DEBUG) {
            Thread.sleep(500)
            Timber.d("Skipping actual status update in debug build")
            return
        }

        try {
            val okHttpClient = OkHttpClient()

            val response = okHttpClient.newCall(Request.Builder().url(queryString).build()).execute()

            if(response.code() != 200) {
                Timber.e("Could not update space status!")
            }
        } catch (e: IOException) {
            Timber.e(e, "IOException " + e.message)
        } catch (e: InterruptedException) {
            Timber.e(e, "Wait for new status didn't finish:")
        } catch (e: ExecutionException) {
            Timber.e(e, "Error executing update task inside change task:")
        }
    }

    companion object {
        private val UPDATE_OPEN = Uri.parse("https://wittgenstein/open")
        private val UPDATE_CLOSE = Uri.parse("https://wittgenstein/close")

    }
}