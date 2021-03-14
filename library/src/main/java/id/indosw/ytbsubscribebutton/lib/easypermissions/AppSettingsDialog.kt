@file:Suppress("DEPRECATION", "unused")

package id.indosw.ytbsubscribebutton.lib.easypermissions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import id.indosw.ytbsubscribebutton.lib.R

class AppSettingsDialog : Parcelable {
    @StyleRes
    private val mThemeResId: Int
    private val mRationale: String?
    private val mTitle: String?
    private val mPositiveButtonText: String?
    private val mNegativeButtonText: String?
    private val mRequestCode: Int
    val intentFlags: Int
    private var mActivityOrFragment: Any? = null
    private var mContext: Context? = null

    private constructor(`in`: Parcel) {
        mThemeResId = `in`.readInt()
        mRationale = `in`.readString()
        mTitle = `in`.readString()
        mPositiveButtonText = `in`.readString()
        mNegativeButtonText = `in`.readString()
        mRequestCode = `in`.readInt()
        intentFlags = `in`.readInt()
    }

    private constructor(
        activityOrFragment: Any,
        @StyleRes themeResId: Int,
        rationale: String?,
        title: String?,
        positiveButtonText: String?,
        negativeButtonText: String?,
        requestCode: Int,
        intentFlags: Int
    ) {
        setActivityOrFragment(activityOrFragment)
        mThemeResId = themeResId
        mRationale = rationale
        mTitle = title
        mPositiveButtonText = positiveButtonText
        mNegativeButtonText = negativeButtonText
        mRequestCode = requestCode
        this.intentFlags = intentFlags
    }

    private fun setActivityOrFragment(activityOrFragment: Any) {
        mActivityOrFragment = activityOrFragment
        mContext = when (activityOrFragment) {
            is Activity -> {
                activityOrFragment
            }
            is Fragment -> {
                activityOrFragment.context
            }
            else -> {
                throw IllegalStateException("Unknown object: $activityOrFragment")
            }
        }
    }

    private fun startForResult(intent: Intent) {
        if (mActivityOrFragment is Activity) {
            (mActivityOrFragment as Activity).startActivityForResult(intent, mRequestCode)
        } else if (mActivityOrFragment is Fragment) {
            (mActivityOrFragment as Fragment).startActivityForResult(intent, mRequestCode)
        }
    }

    fun show() {
        startForResult(AppSettingsDialogHolderActivity.createShowDialogIntent(mContext, this))
    }

    fun showDialog(
        positiveListener: DialogInterface.OnClickListener?,
        negativeListener: DialogInterface.OnClickListener?
    ): AlertDialog {
        val builder: AlertDialog.Builder = if (mThemeResId != -1) {
            AlertDialog.Builder(mContext!!, mThemeResId)
        } else {
            AlertDialog.Builder(mContext!!)
        }
        return builder
            .setCancelable(false)
            .setTitle(mTitle)
            .setMessage(mRationale)
            .setPositiveButton(mPositiveButtonText, positiveListener)
            .setNegativeButton(mNegativeButtonText, negativeListener)
            .show()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(mThemeResId)
        dest.writeString(mRationale)
        dest.writeString(mTitle)
        dest.writeString(mPositiveButtonText)
        dest.writeString(mNegativeButtonText)
        dest.writeInt(mRequestCode)
        dest.writeInt(intentFlags)
    }

    class Builder {
        private val mActivityOrFragment: Any
        private val mContext: Context?

        @StyleRes
        private var mThemeResId = -1
        private var mRationale: String? = null
        private var mTitle: String? = null
        private var mPositiveButtonText: String? = null
        private var mNegativeButtonText: String? = null
        private var mRequestCode = -1
        private var mOpenInNewTask = false

        constructor(activity: Activity) {
            mActivityOrFragment = activity
            mContext = activity
        }

        constructor(fragment: Fragment) {
            mActivityOrFragment = fragment
            mContext = fragment.context
        }

        fun setThemeResId(@StyleRes themeResId: Int): Builder {
            mThemeResId = themeResId
            return this
        }

        fun setTitle(title: String?): Builder {
            mTitle = title
            return this
        }

        fun setTitle(@StringRes title: Int): Builder {
            mTitle = mContext!!.getString(title)
            return this
        }

        fun setRationale(rationale: String?): Builder {
            mRationale = rationale
            return this
        }

        fun setRationale(@StringRes rationale: Int): Builder {
            mRationale = mContext!!.getString(rationale)
            return this
        }

        fun setPositiveButton(text: String?): Builder {
            mPositiveButtonText = text
            return this
        }

        fun setPositiveButton(@StringRes textId: Int): Builder {
            mPositiveButtonText = mContext!!.getString(textId)
            return this
        }

        fun setNegativeButton(text: String?): Builder {
            mNegativeButtonText = text
            return this
        }

        fun setNegativeButton(@StringRes textId: Int): Builder {
            mNegativeButtonText = mContext!!.getString(textId)
            return this
        }

        fun setRequestCode(requestCode: Int): Builder {
            mRequestCode = requestCode
            return this
        }

        fun setOpenInNewTask(openInNewTask: Boolean): Builder {
            mOpenInNewTask = openInNewTask
            return this
        }

        fun build(): AppSettingsDialog {
            mRationale =
                if (TextUtils.isEmpty(mRationale)) mContext!!.getString(R.string.rationale_ask_again) else mRationale
            mTitle =
                if (TextUtils.isEmpty(mTitle)) mContext!!.getString(R.string.title_settings_dialog) else mTitle
            mPositiveButtonText =
                if (TextUtils.isEmpty(mPositiveButtonText)) mContext!!.getString(android.R.string.ok) else mPositiveButtonText
            mNegativeButtonText =
                if (TextUtils.isEmpty(mNegativeButtonText)) mContext!!.getString(android.R.string.cancel) else mNegativeButtonText
            mRequestCode = if (mRequestCode > 0) mRequestCode else DEFAULT_SETTINGS_REQ_CODE
            var intentFlags = 0
            if (mOpenInNewTask) {
                intentFlags = intentFlags or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return AppSettingsDialog(
                mActivityOrFragment,
                mThemeResId,
                mRationale,
                mTitle,
                mPositiveButtonText,
                mNegativeButtonText,
                mRequestCode,
                intentFlags
            )
        }
    }

    companion object {
        const val DEFAULT_SETTINGS_REQ_CODE = 16061

        @SuppressLint("ParcelCreator")
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        val CREATOR: Parcelable.Creator<AppSettingsDialog?> =
            object : Parcelable.Creator<AppSettingsDialog?> {
                override fun createFromParcel(`in`: Parcel): AppSettingsDialog {
                    return AppSettingsDialog(`in`)
                }

                override fun newArray(size: Int): Array<AppSettingsDialog?> {
                    return arrayOfNulls(size)
                }
            }
        const val EXTRA_APP_SETTINGS = "extra_app_settings"
        @JvmStatic
        fun fromIntent(intent: Intent, activity: Activity): AppSettingsDialog {
            val dialog: AppSettingsDialog? = intent.getParcelableExtra(EXTRA_APP_SETTINGS)
            dialog!!.setActivityOrFragment(activity)
            return dialog
        }
    }
}