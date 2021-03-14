@file:Suppress("DEPRECATION")

package id.indosw.ytbsubscribebutton.lib

import android.annotation.SuppressLint
import android.os.AsyncTask
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.ResourceId
import com.google.api.services.youtube.model.Subscription
import com.google.api.services.youtube.model.SubscriptionSnippet
import java.io.IOException
import java.util.*

class YouTubeActivityPresenter(
    //private final YouTubeSubscribeActivity activity;
    private val view: YouTubeActivityView, private val appName: String
) {
    fun subscribeToYouTubeChannel(mCredential: GoogleAccountCredential?, channelId: String?) {
        MakeRequestTask(mCredential, channelId).execute()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class MakeRequestTask(
        credential: GoogleAccountCredential?,
        private val channelId: String?
    ) : AsyncTask<Any?, Any?, Subscription?>() {
        private val mService: YouTube
        override fun doInBackground(vararg params: Any?): Subscription? {
            //code for channel subscribe
            var response: Subscription? = null
            val parameters = HashMap<String, String>()
            parameters["part"] = "snippet"
            // if you could not able to import the classes then check the dependency in build.gradle
            val subscription = Subscription()
            val snippet = SubscriptionSnippet()
            val resourceId = ResourceId()
            resourceId["channelId"] = channelId
            resourceId["kind"] = "youtube#channel"
            snippet.resourceId = resourceId
            subscription.snippet = snippet
            val subscriptionsInsertRequest: YouTube.Subscriptions.Insert
            try {
                subscriptionsInsertRequest =
                    mService.subscriptions().insert(parameters["part"], subscription)
                response = subscriptionsInsertRequest.execute()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return response
        }

        override fun onPostExecute(subscription: Subscription?) {
            super.onPostExecute(subscription)
            if (subscription != null) {
                view.onSubscribetionSuccess(subscription.snippet.title)
            } else {
                view.onSubscribetionFail()
            }
        }

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = YouTube.Builder(
                transport, jsonFactory, credential
            )
                .setApplicationName(appName)
                .build()
        }
    }
}