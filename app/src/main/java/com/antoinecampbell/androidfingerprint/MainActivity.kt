package com.antoinecampbell.androidfingerprint

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var keyguardManager: KeyguardManager
    private lateinit var fingerprintManager: FingerprintManagerCompat
    private lateinit var fingerprintDialog: AlertDialog
    private val callback = object : FingerprintManagerCompat.AuthenticationCallback() {
        var cancellationSignal = CancellationSignal()
            private set(value) {
                field = value
            }

        fun cancel() {
            cancellationSignal.cancel()
            cancellationSignal = CancellationSignal()
        }

        override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
            super.onAuthenticationError(errMsgId, errString)
            Toast.makeText(this@MainActivity, "onAuthenticationError: $errString", Toast.LENGTH_LONG).show()
            fingerprintDialog.hide()
        }

        override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            Toast.makeText(this@MainActivity, "onAuthenticationSucceeded:", Toast.LENGTH_LONG).show()
            fingerprintDialog.hide()
        }

        override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
            super.onAuthenticationHelp(helpMsgId, helpString)
            Toast.makeText(this@MainActivity, "onAuthenticationHelp:", Toast.LENGTH_LONG).show()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Toast.makeText(this@MainActivity, "onAuthenticationFailed:", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        fingerprintManager = FingerprintManagerCompat.from(this)

        if (!keyguardManager.isKeyguardSecure) {

            Toast.makeText(this, "Lock screen security not enabled in Settings", Toast.LENGTH_LONG).show()
            return
        }

        @Suppress("DEPRECATION")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Fingerprint authentication permission not enabled", Toast.LENGTH_LONG).show()
            return
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            // This happens when no fingerprints are registered.
            Toast.makeText(this, "Register at least one fingerprint in Settings", Toast.LENGTH_LONG).show()
            return
        }

        fingerprintDialog = AlertDialog.Builder(this)
                .setTitle("Touch the Sensor")
                .setIcon(R.drawable.ic_fingerprint_black_24dp)
                .setNegativeButton("Cancel") { _, _ ->
                    callback.cancel()
                }
                .create()

        button_auth.setOnClickListener { _ ->
            fingerprintManager.authenticate(null, 0, callback.cancellationSignal, callback, null)
            fingerprintDialog.show()
        }
    }

}
