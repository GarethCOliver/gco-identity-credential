package com.android.mdl.app.util

import android.app.PendingIntent
import android.content.Intent
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreateCredentialResponse
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetCredentialResponse
import androidx.credentials.provider.CreateEntry
import androidx.credentials.provider.CredentialProviderService
import androidx.credentials.provider.ProviderClearCredentialStateRequest
import com.android.mdl.app.MainActivity

@RequiresApi(31)
class CredentialProviderHandler : CredentialProviderService() {
  override fun onBeginCreateCredentialRequest(
    request: BeginCreateCredentialRequest,
    cancellationSignal: CancellationSignal,
    callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>,
  ) {
    Log.e("MYTAG", "CreateCredential request $request")
    val holderAppPendingIntent = PendingIntent.getActivity(
      applicationContext,
      1000,
      Intent(applicationContext, MainActivity::class.java),
      PendingIntent.FLAG_IMMUTABLE
    )
    callback.onResult(
      BeginCreateCredentialResponse.Builder().setCreateEntries(listOf(
        CreateEntry.Builder("foo@google.com", holderAppPendingIntent)
          .setDescription("This is a credential for type: ${request.type}")
          .build()
      )).build()
    )
  }

  override fun onBeginGetCredentialRequest(
    request: BeginGetCredentialRequest,
    cancellationSignal: CancellationSignal,
    callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>,
  ) {

  }

  override fun onClearCredentialStateRequest(
    request: ProviderClearCredentialStateRequest,
    cancellationSignal: CancellationSignal,
    callback: OutcomeReceiver<Void?, ClearCredentialException>
  ) {}
}