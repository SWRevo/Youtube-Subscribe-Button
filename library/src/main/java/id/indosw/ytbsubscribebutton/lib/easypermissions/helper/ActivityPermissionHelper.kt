@file:Suppress("DEPRECATION")

package id.indosw.ytbsubscribebutton.lib.easypermissions.helper

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.StyleRes
import androidx.core.app.ActivityCompat
import id.indosw.ytbsubscribebutton.lib.easypermissions.RationaleDialogFragment

/**
 * Permissions helper for [Activity].
 */
internal class ActivityPermissionHelper(host: Activity?) : PermissionHelper<Activity?>(
    host!!
) {
    override fun directRequestPermissions(requestCode: Int, vararg perms: String) {
        ActivityCompat.requestPermissions(host, perms, requestCode)
    }

    override fun shouldShowRequestPermissionRationale(perm: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(host, perm)
    }

    override fun getContext(): Context {
        return host
    }

    override fun showRequestPermissionRationale(
        rationale: String,
        positiveButton: String,
        negativeButton: String,
        @StyleRes theme: Int,
        requestCode: Int,
        vararg perms: String
    ) {
        val fm = host.fragmentManager

        // Check if fragment is already showing
        val fragment = fm.findFragmentByTag(RationaleDialogFragment.TAG)
        if (fragment is RationaleDialogFragment) {
            Log.d(TAG, "Found existing fragment, not showing rationale.")
            return
        }
        RationaleDialogFragment
            .newInstance(positiveButton, negativeButton, rationale, theme, requestCode, perms)
            .showAllowingStateLoss(fm, RationaleDialogFragment.TAG)
    }

    companion object {
        private const val TAG = "ActPermissionHelper"
    }
}