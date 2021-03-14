@file:Suppress("DEPRECATION")

package id.indosw.ytbsubscribebutton.lib.easypermissions

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import androidx.fragment.app.Fragment
import id.indosw.ytbsubscribebutton.lib.easypermissions.EasyPermissions.PermissionCallbacks
import id.indosw.ytbsubscribebutton.lib.easypermissions.EasyPermissions.RationaleCallbacks
import id.indosw.ytbsubscribebutton.lib.easypermissions.helper.PermissionHelper

/**
 * Click listener for either [RationaleDialogFragment] or [RationaleDialogFragmentCompat].
 */
internal class RationaleDialogClickListener : DialogInterface.OnClickListener {
    private var mHost: Any?
    private var mConfig: RationaleDialogConfig
    private var mCallbacks: PermissionCallbacks?
    private var mRationaleCallbacks: RationaleCallbacks?

    constructor(
        compatDialogFragment: RationaleDialogFragmentCompat,
        config: RationaleDialogConfig,
        callbacks: PermissionCallbacks?,
        rationaleCallbacks: RationaleCallbacks?
    ) {
        mHost =
            if (compatDialogFragment.parentFragment != null) compatDialogFragment.parentFragment else compatDialogFragment.activity
        mConfig = config
        mCallbacks = callbacks
        mRationaleCallbacks = rationaleCallbacks
    }

    constructor(
        dialogFragment: RationaleDialogFragment,
        config: RationaleDialogConfig,
        callbacks: PermissionCallbacks?,
        dialogCallback: RationaleCallbacks?
    ) {
        mHost = dialogFragment.activity
        mConfig = config
        mCallbacks = callbacks
        mRationaleCallbacks = dialogCallback
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val requestCode = mConfig.requestCode
        if (which == Dialog.BUTTON_POSITIVE) {
            val permissions = mConfig.permissions
            if (mRationaleCallbacks != null) {
                mRationaleCallbacks!!.onRationaleAccepted(requestCode)
            }
            when (mHost) {
                is Fragment -> {
                    PermissionHelper.newInstance(mHost as Fragment?)
                        .directRequestPermissions(requestCode, *permissions)
                }
                is Activity -> {
                    PermissionHelper.newInstance(mHost as Activity?)
                        .directRequestPermissions(requestCode, *permissions)
                }
                else -> {
                    throw RuntimeException("Host must be an Activity or Fragment!")
                }
            }
        } else {
            if (mRationaleCallbacks != null) {
                mRationaleCallbacks!!.onRationaleDenied(requestCode)
            }
            notifyPermissionDenied()
        }
    }

    private fun notifyPermissionDenied() {
        if (mCallbacks != null) {
            mCallbacks!!.onPermissionsDenied(
                mConfig.requestCode,
                mutableListOf(*mConfig.permissions)
            )
        }
    }
}