package com.srizwan.islamipedia

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Drawer : AppCompatActivity() {
    private var a = ""
    private var b = ""
    private val share = Intent()
    private val rate = Intent()
    private lateinit var back: LinearLayout
    private lateinit var Share: LinearLayout
    private lateinit var Rate: LinearLayout
    private lateinit var privacypolicy: LinearLayout
    private lateinit var exit: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout._drawer_main)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.parseColor(getString(R.string.color))
                navigationBarColor = Color.parseColor(getString(R.string.color))
            }
        }
        Share = findViewById(R.id.Share)
        Rate = findViewById(R.id.Rate)
        privacypolicy = findViewById(R.id.privacypolicy)
        exit = findViewById(R.id.exit)
        back = findViewById(R.id.off)
        Rate.setOnClickListener {
            rate.apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.srizwan.islamipedia")
            }
            startActivity(rate)
            Toast.makeText(this@Drawer, "Rate us", Toast.LENGTH_SHORT).show()
        }
        back.setOnClickListener{
            finish()
        }
        Share.setOnClickListener {
            a = "Share app now"
            b = "আসসালামু আলাইকুম ইসলামী বিশ্বকোষ ও আল হাদিস S2 : https://play.google.com/store/apps/details?id=com.srizwan.islamipedia"
            share.apply {
                type = "text/plain"
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, a)
                putExtra(Intent.EXTRA_TEXT, b)
            }
            startActivity(Intent.createChooser(share, "Share app now"))
            Toast.makeText(this@Drawer, "Share app", Toast.LENGTH_SHORT).show()
        }

        exit.setOnClickListener { showExitDialog() }
        privacypolicy.setOnClickListener { showpolicy() }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

    private fun showpolicy() {
        AlertDialog.Builder(this).apply {
            setTitle("Privacy Policy")
            setMessage(
                "Privacy Policy for ইসলামী বিশ্বকোষ ও আল হাদিস S2\n" +
                        "\n" +
                        "Effective Date: 29 October 2024\n" +
                        "\n" +
                        "1. Introduction\n" +
                        "Welcome to ইসলামী বিশ্বকোষ ও আল হাদিস S2 (previously known as মুফতী আলাউদ্দিন জেহাদী গ্রন্থ). We are committed to protecting your privacy and ensuring that your personal information is handled securely and responsibly.\n" +
                        "\n" +
                        "2. Information Collection\n" +
                        "Our app does not directly collect personal information from users. We may gather information from your device, including but not limited to:\n" +
                        "\n" +
                        "Device Information (model, operating system version)\n" +
                        "Usage Data (how you interact with the app)\n" +
                        "3. Use of Information\n" +
                        "The information collected is used for the following purposes:\n" +
                        "\n" +
                        "To provide and maintain our app\n" +
                        "To improve user experience\n" +
                        "To monitor app usage and trends\n" +
                        "4. Advertising\n" +
                        "We use Google AdMob to display advertisements in our app. Google AdMob may collect information about your device and usage for advertising purposes. Please review the Google AdMob Privacy Policy for more information on their data practices: Google AdMob Privacy Policy.\n" +
                        "\n" +
                        "5. Cookies\n" +
                        "Our app does not use cookies or similar tracking technologies. However, third-party services, such as Google AdMob, may use cookies to collect data.\n" +
                        "\n" +
                        "6. Data Security\n" +
                        "We take the security of your information seriously. While we strive to protect your data, please remember that no method of transmission over the internet or method of electronic storage is 100% secure.\n" +
                        "\n" +
                        "7. Changes to This Privacy Policy\n" +
                        "We may update our Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy in the app. You are advised to review this Privacy Policy periodically for any changes.\n" +
                        "\n" +
                        "8. Contact Us\n" +
                        "If you have any questions or concerns about this Privacy Policy, please contact us at:\n" +
                        "Email: rajibhossain0684@gmail.com"
            )
            setPositiveButton("হ্যাঁ") { _, _ -> }
            create()
            show()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("আপনি কি বের হতে চান?")
            setPositiveButton("হ্যাঁ") { _, _ -> finishAffinity() }
            setNegativeButton("না") { _, _ -> }
            create()
            show()
        }
    }
}