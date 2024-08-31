package fman.ge.smart_auth

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.app.Activity.RESULT_OK

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts

import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class PhoneNumberHintActivity : ComponentActivity() {
    private lateinit var phoneNumberHintIntentResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    companion object {
        val TAG: String = PhoneNumberHintActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivityResultLauncher()
        handleIntent()
    }

    private fun setupActivityResultLauncher() {
        phoneNumberHintIntentResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            try {
                val phoneNumber = try {
                    val rawPhoneNumber = Identity.getSignInClient(this)
                        .getPhoneNumberFromIntent(result.data)
                    formatPhoneNumber(rawPhoneNumber)
                } catch (e: Exception) {
                    Log.e(TAG, "Phone Number Hint failed", e)
                    null
                }
                sendResultBack(phoneNumber)
            } catch (e: Exception) {
                Log.e(TAG, "Phone Number Hint failed", e)
                finish()
            }
        }
    }

    private fun handleIntent() {
        val request: GetPhoneNumberHintIntentRequest = GetPhoneNumberHintIntentRequest
            .builder().build()
        Identity.getSignInClient(this)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener { result ->
                try {
                    phoneNumberHintIntentResultLauncher.launch(
                        IntentSenderRequest.Builder(result).build()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Launching the PendingIntent failed", e)
                    finish()
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Phone Number Hint failed")
                finish()
            }
    }

    private fun formatPhoneNumber(rawPhoneNumber: String?): String? {
        return try {
            val countryCode = intent.getStringExtra("COUNTRY_CODE")
            Log.d(TAG, "Phone Number Country Code: " + countryCode)

            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber: Phonenumber.PhoneNumber = phoneNumberUtil.parse(
                rawPhoneNumber, countryCode
            )

            phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to format phone number", e)
            null
        }
    }

    private fun sendResultBack(phoneNumber: String?) {
        val intent = Intent().apply {
            putExtra("PHONE_NUMBER", phoneNumber)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}
