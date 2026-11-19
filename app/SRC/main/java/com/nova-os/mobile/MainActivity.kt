package com.novaos.mobile

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var page: LinearLayout
    private lateinit var terminalOutput: TextView
    private lateinit var commandBox: EditText
    private val sessionHistory = mutableListOf<String>()
    private val startedAt = SystemClock.elapsedRealtime()
    private var pendingApk: Uri? = null
    private val prefs by lazy { getSharedPreferences("nova_preferences", MODE_PRIVATE) }

    private val apkPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingApk = uri
            continueInstall()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 8, 12)
        window.navigationBarColor = Color.rgb(8, 8, 12)
        buildLayout()
        dashboard()
        sessionHistory += "NOVA OS Mobile 1.0.0 initialized."
        sessionHistory += "Type help in Terminal to list commands."
    }

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun buildLayout() {
        val shell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(8), dp(14), dp(8))
            setBackgroundColor(Color.rgb(8, 8, 12))
        }
        val masthead = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(14))
        }
        masthead.addView(TextView(this).apply {
            text = "NOVA OS"
            textSize = 25f
            setTextColor(Color.rgb(255, 65, 83))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        masthead.addView(TextView(this).apply {
            text = "   ● ONLINE"
            textSize = 11f
            setTextColor(Color.rgb(100, 220, 155))
        })
        shell.addView(masthead)

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("HOME", "TERMINAL", "APPS", "SYSTEM", "SETTINGS").forEach { section ->
            nav.addView(Button(this).apply {
                text = section
                textSize = 9f
                isAllCaps = false
                setTextColor(Color.WHITE)
                setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(75, 20, 33)))
                setOnClickListener { navigate(section) }
            }, LinearLayout.LayoutParams(0, dp(46), 1f))
        }
        shell.addView(nav)

        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(14), 0, 0)
        }
        shell.addView(page, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(shell)
    }

    private fun navigate(section: String) {
        when (section) {
            "HOME" -> dashboard()
            "TERMINAL" -> terminal()
            "APPS" -> apps()
            "SYSTEM" -> systemInfo()
            "SETTINGS" -> settings()
        }
    }

    private fun heading(title: String) {
        page.removeAllViews()
        page.addView(TextView(this).apply {
            text = title
            textSize = 21f
            setTextColor(Color.rgb(255, 75, 91))
            setPadding(0, 0, 0, dp(12))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
    }

    private fun label(text: String, size: Float = 14f) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(Color.rgb(225, 225, 232))
        setPadding(0, dp(5), 0, dp(5))
    }

    private fun actionButton(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        isAllCaps = false
        setTextColor(Color.WHITE)
        setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(105, 25, 43)))
        setOnClickListener { action() }
    }

    private fun dashboard() {
        heading("COMMAND DECK")
        page.addView(label("A customizable Android command environment."))
        page.addView(label("Version 1.0.0  •  Kotlin + Java + C++"))
        page.addView(actionButton("Open Terminal") { terminal() })
        page.addView(actionButton("Manage Applications") { apps() })
        page.addView(actionButton("Device Information") { systemInfo() })
        page.addView(actionButton("Settings") { settings() })
        page.addView(label("NOVA is an application running on Android, not a replacement operating system.", 12f))
    }

    private fun terminal() {
        heading("NOVA TERMINAL")
        val scroll = ScrollView(this)
        terminalOutput = label("", 12f).apply {
            typeface = android.graphics.Typeface.MONOSPACE
            setTextColor(Color.rgb(150, 245, 175))
            setBackgroundColor(Color.rgb(15, 17, 20))
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        scroll.addView(terminalOutput)
        page.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val inputRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        commandBox = EditText(this).apply {
            hint = "Enter command..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            singleLine = true
            imeOptions = EditorInfo.IME_ACTION_SEND
            setBackgroundColor(Color.rgb(28, 28, 34))
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    executeCommand(text.toString())
                    text.clear()
                    true
                } else false
            }
        }
        inputRow.addView(commandBox, LinearLayout.LayoutParams(0, dp(54), 1f))
        inputRow.addView(actionButton("RUN") {
            executeCommand(commandBox.text.toString())
            commandBox.text.clear()
        })
        page.addView(inputRow)
        sessionHistory.forEach { terminalLine(it) }
    }

    private fun terminalLine(text: String) {
        if (::terminalOutput.isInitialized) terminalOutput.append("$text\n")
    }

    private fun executeCommand(raw: String) {
        val commandLine = raw.trim()
        if (commandLine.isEmpty()) return
        sessionHistory += "> $commandLine"
        terminalLine("> $commandLine")
        val pieces = commandLine.split(Regex("\\s+"), limit = 2)
        val command = pieces[0].lowercase(Locale.ROOT)
        val argument = pieces.getOrNull(1).orEmpty()

        if (command == "clear") {
            terminalOutput.text = ""
            sessionHistory.clear()
            return
        }

        val result = when (command) {
            "help" -> "help | clear | date | time | device | apps | uptime | version\nsettings | install | uninstall <package> | open <package>\nOnly built-in NOVA commands are supported; arbitrary shell execution is disabled."
            "date" -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            "time" -> SimpleDateFormat("HH:mm:ss z", Locale.getDefault()).format(Date())
            "device" -> DeviceInfo.summary() + "\n" + nativeSummary()
            "apps" -> "Launchable applications found: ${launcherApps().size}"
            "uptime" -> "NOVA session uptime: ${(SystemClock.elapsedRealtime() - startedAt) / 1000} seconds"
            "version" -> "NOVA OS Mobile 1.0.0"
            "settings" -> { startActivity(Intent(Settings.ACTION_SETTINGS)); "Opening Android Settings..." }
            "install" -> { pickApk(); "Opening APK file picker..." }
            "uninstall" -> {
                if (argument.isBlank()) "Usage: uninstall <package.name>"
                else { uninstallPackage(argument); "Requested Android uninstall confirmation for $argument." }
            }
            "open" -> {
                if (argument.isBlank()) "Usage: open <package.name>"
                else { launchPackage(argument); "Requested launch for $argument." }
            }
            else -> "Unknown command: $command. Type help."
        }
        sessionHistory += result
        terminalLine(result)
    }

    private fun launcherApps(): List<android.content.pm.ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.ROOT) }
    }

    private fun apps() {
        heading("APPLICATION MANAGER")
        page.addView(label("Open apps or request removal using Android's own confirmation screens."))
        page.addView(actionButton("Install APK") { pickApk() })
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this)
        launcherApps().forEach { entry ->
            val packageName = entry.activityInfo.packageName
            val appName = entry.loadLabel(packageManager).toString()
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(6), 0, dp(6))
            }
            card.addView(label("$appName\n$packageName", 13f))
            val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            buttons.addView(actionButton("OPEN") { launchPackage(packageName) })
            if (packageName != this.packageName) {
                buttons.addView(actionButton("UNINSTALL") { uninstallPackage(packageName) })
            }
            card.addView(buttons)
            list.addView(card)
        }
        scroll.addView(list)
        page.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
    }

    private fun launchPackage(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            terminalLine("No launchable activity found for $packageName")
            return
        }
        try { startActivity(launchIntent) }
        catch (error: Exception) { terminalLine("Launch failed: ${error.message}") }
    }

    private fun uninstallPackage(packageName: String) {
        if (packageName == this.packageName) {
            terminalLine("NOVA cannot uninstall itself from its own running session.")
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")))
        } catch (error: Exception) {
            terminalLine("Could not open Android uninstall screen: ${error.message}")
        }
    }

    private fun pickApk() {
        apkPicker.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream"))
    }

    private fun continueInstall() {
        val selectedUri = pendingApk ?: return
        if (android.os.Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
            startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
            Toast.makeText(this, "Allow installs for NOVA, then return to continue.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = selectedUri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
            pendingApk = null
        } catch (error: Exception) {
            Toast.makeText(this, "Package installer could not be opened: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (pendingApk != null &&
            (android.os.Build.VERSION.SDK_INT < 26 || packageManager.canRequestPackageInstalls())) {
            continueInstall()
        }
    }

    private fun systemInfo() {
        heading("SYSTEM INFORMATION")
        page.addView(label(DeviceInfo.summary()))
        page.addView(label("Native module: ${nativeSummary()}"))
        page.addView(label("Android enforces application sandboxing and permission boundaries."))
        page.addView(actionButton("Open Android Settings") {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        })
    }

    private fun settings() {
        heading("NOVA SETTINGS")
        page.addView(Switch(this).apply {
            text = "Enable crimson interface preference"
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean("crimson", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("crimson", checked).apply()
                Toast.makeText(this@MainActivity, "Preference saved.", Toast.LENGTH_SHORT).show()
            }
        })
        page.addView(label("Terminal history is kept for this app session."))
        page.addView(label("Installation and removal always require Android user approval."))
    }

    private external fun nativeSummary(): String

    companion object {
        init { System.loadLibrary("nova_native") }
    }
}
