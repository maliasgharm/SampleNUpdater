package com.noandish.nupdate

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.noandish.nupdate.connection.Connection
import com.noandish.nupdate.utils.Utils
import kotlinx.android.synthetic.main.dialog_update.*
import java.lang.Exception

class Updater {

    companion object {
        /**@hide*/
        @SuppressLint("StaticFieldLeak")
        internal var connection: Connection? = null

        private lateinit var dialogUpdate: Dialog

        /**@hide*/
        @SuppressLint("StaticFieldLeak")
        internal lateinit var updateChecker: UpdateChecker

        fun install(context: Context, url: String? = null) {
            connection = Connection(context)
            connection?.urlMain = url ?: Utils.DEFAULT_URL_UPDATE

            updateChecker = UpdateChecker(context)
            dialogUpdate = Dialog(context)
            updateChecker.check { isUpdate, description ->
                if (isUpdate == null || !isUpdate) {
                    return@check
                }
                dialogUpdate.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogUpdate.setContentView(R.layout.dialog_update)
                dialogUpdate.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogUpdate.window!!.setLayout(
                    Utils.screenWidth - 80,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialogUpdate.tvDescription.text = description
                dialogUpdate.setCancelable(false)
                dialogUpdate.pbUpdate.max = 1000
                dialogUpdate.btnUpdate.setOnClickListener {
                    updateChecker.update({
                        dialogUpdate.tvSize.text =
                            "حجم برنامه ${Utils.convertToStringRepresentation(it)} "
                    }, {
                        dialogUpdate.pbUpdate.progress = it
                        dialogUpdate.tvPercentUpdate.text = "${it / 10}%"
                    }, { type, path ->
                        if (type == UpdateChecker.TypeDownload.START_DOWNLOAD) {
                            dialogUpdate.layerProgress.visibility = View.VISIBLE
                            dialogUpdate.btnUpdate.visibility = View.GONE
                        } else {
                            if (path == "") {
                                dialogUpdate.layerProgress.visibility = View.GONE
                                dialogUpdate.btnUpdate.visibility = View.VISIBLE
                            } else {
                                Utils.installApk(context, path)
                            }
                        }
                    })
                }
                dialogUpdate.btnCancelUpdate.setOnClickListener {
                    dialogUpdate.dismiss()
                    updateChecker.cancelDownload()
                }
                try {
                    dialogUpdate.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

        fun setUpdateUrl(url: String) {
            connection?.urlMain = url
        }

    }
}