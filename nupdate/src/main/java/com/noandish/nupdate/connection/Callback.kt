package com.noandish.nupdate.connection

class Callback(val type: Int, val response: String) {
  private var tryAgainRequest: (() -> Unit)? = null

  fun setListenerTryAgain(tryAgainRequest: (() -> Unit)) {
    this.tryAgainRequest = tryAgainRequest
  }

  fun tryAgainRequest() {
    tryAgainRequest?.invoke()
  }

  companion object {
    const val TYPE_SUCCESS = 0
    const val TYPE_FAILURE = 1
  }
}