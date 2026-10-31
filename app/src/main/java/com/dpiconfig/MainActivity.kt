package com.dpiconfig

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private val FFM_PACKAGE = "com.dts.freefiremax"
    private val SHIZUKU_CODE = 100
    private var userService: IUserService? = null

    private val presets = linkedMapOf(
        "Nhẹ tay" to -2, "Cân bằng" to 0,
        "Bám tâm" to 2, "Kéo mượt" to 3, "Siêu bá" to 5
    )

    private val serviceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name)
    ).daemon(false).processNameSuffix("service")
     .debuggable(BuildConfig.DEBUG).version(BuildConfig.VERSION_CODE)

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            userService = IUserService.Stub.asInterface(binder)
            updateShizukuStatus(true)
        }
        override fun onServiceDisconnected(name: ComponentName) {
            userService = null
            updateShizukuStatus(false)
        }
    }

    private val permListener = Shizuku.OnRequestPermissionResultListener { code, result ->
        if (code == SHIZUKU_CODE && result == android.content.pm.PackageManager.PERMISSION_GRANTED)
            bindSvc()
        else toast("Shizuku: từ chối quyền")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Shizuku.addRequestPermissionResultListener(permListener)

        val seekBar   = findViewById<SeekBar>(R.id.seekBarDpi)
        val tvValue   = findViewById<TextView>(R.id.tvDpiValue)
        val tvStatus  = findViewById<TextView>(R.id.tvStatus)
        val btnShizuku = findViewById<Button>(R.id.btnShizuku)
        val btnApply  = findViewById<Button>(R.id.btnApply)
        val btnLaunch = findViewById<Button>(R.id.btnLaunchFFM)
        val radioGroup = findViewById<RadioGroup>(R.id.radioPresets)

        seekBar.max = 14; seekBar.progress = 7
        tvValue.text = "DPI Config: 0"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, f: Boolean) { tvValue.text = "DPI Config: ${p - 7}" }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        radioGroup.setOnCheckedChangeListener { _, id ->
            val btn = findViewById<RadioButton>(id) ?: return@setOnCheckedChangeListener
            val sp = presets[btn.text.toString()] ?: 0
            seekBar.progress = sp + 7; tvValue.text = "DPI Config: $sp"
        }

        btnShizuku.setOnClickListener {
            when {
                !isAvail() -> toast("Mở Shizuku app trước")
                !hasPerm() -> Shizuku.requestPermission(SHIZUKU_CODE)
                else -> bindSvc()
            }
        }

        btnApply.setOnClickListener { apply(seekBar.progress - 7, tvStatus) }

        btnLaunch.setOnClickListener {
            apply(seekBar.progress - 7, tvStatus)
            val i = packageManager.getLaunchIntentForPackage(FFM_PACKAGE)
            if (i != null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i) }
            else { tvStatus.text = "Free Fire Max chưa cài"; tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light)) }
        }

        if (isAvail() && hasPerm()) bindSvc()
        updateShizukuStatus(userService != null)
    }

    private fun bindSvc() {
        try { Shizuku.bindUserService(serviceArgs, serviceConnection) }
        catch (e: Exception) { toast("Bind thất bại: ${e.message}") }
    }

    private fun isAvail() = try { Shizuku.pingBinder() } catch (e: Exception) { false }
    private fun hasPerm() = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }

    private fun apply(speed: Int, tv: TextView) {
        val svc = userService ?: run {
            tv.text = "Shizuku chưa kết nối"
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
            return
        }
        if (svc.applyPointerSpeed(speed) == 0) {
            tv.text = "DPI Config $speed — áp dụng thành công"
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        } else {
            tv.text = "Lỗi apply"
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        }
    }

    private fun updateShizukuStatus(on: Boolean) {
        val tv = findViewById<TextView>(R.id.tvShizukuStatus)
        tv.text = if (on) "Shizuku: Đã kết nối" else "Shizuku: Chưa kết nối"
        tv.setTextColor(ContextCompat.getColor(this, if (on) android.R.color.holo_green_light else android.R.color.holo_red_light))
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permListener)
        try { Shizuku.unbindUserService(serviceArgs, serviceConnection, true) } catch (_: Exception) {}
    }
}
EOF# 7. MainActivity.kt
cat > app/src/main/java/com/dpiconfig/MainActivity.kt << 'EOF'
package com.dpiconfig

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private val FFM_PACKAGE = "com.dts.freefiremax"
    private val SHIZUKU_CODE = 100
    private var userService: IUserService? = null

    private val presets = linkedMapOf(
        "Nhẹ tay" to -2, "Cân bằng" to 0,
        "Bám tâm" to 2, "Kéo mượt" to 3, "Siêu bá" to 5
    )

    private val serviceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name)
    ).daemon(false).processNameSuffix("service")
     .debuggable(BuildConfig.DEBUG).version(BuildConfig.VERSION_CODE)

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            userService = IUserService.Stub.asInterface(binder)
            updateShizukuStatus(true)
        }
        override fun onServiceDisconnected(name: ComponentName) {
            userService = null
            updateShizukuStatus(false)
        }
    }

    private val permListener = Shizuku.OnRequestPermissionResultListener { code, result ->
        if (code == SHIZUKU_CODE && result == android.content.pm.PackageManager.PERMISSION_GRANTED)
            bindSvc()
        else toast("Shizuku: từ chối quyền")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Shizuku.addRequestPermissionResultListener(permListener)

        val seekBar   = findViewById<SeekBar>(R.id.seekBarDpi)
        val tvValue   = findViewById<TextView>(R.id.tvDpiValue)
        val tvStatus  = findViewById<TextView>(R.id.tvStatus)
        val btnShizuku = findViewById<Button>(R.id.btnShizuku)
        val btnApply  = findViewById<Button>(R.id.btnApply)
        val btnLaunch = findViewById<Button>(R.id.btnLaunchFFM)
        val radioGroup = findViewById<RadioGroup>(R.id.radioPresets)

        seekBar.max = 14; seekBar.progress = 7
        tvValue.text = "DPI Config: 0"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, f: Boolean) { tvValue.text = "DPI Config: ${p - 7}" }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        radioGroup.setOnCheckedChangeListener { _, id ->
            val btn = findViewById<RadioButton>(id) ?: return@setOnCheckedChangeListener
            val sp = presets[btn.text.toString()] ?: 0
            seekBar.progress = sp + 7; tvValue.text = "DPI Config: $sp"
        }

        btnShizuku.setOnClickListener {
            when {
                !isAvail() -> toast("Mở Shizuku app trước")
                !hasPerm() -> Shizuku.requestPermission(SHIZUKU_CODE)
                else -> bindSvc()
            }
        }

        btnApply.setOnClickListener { apply(seekBar.progress - 7, tvStatus) }

        btnLaunch.setOnClickListener {
            apply(seekBar.progress - 7, tvStatus)
            val i = packageManager.getLaunchIntentForPackage(FFM_PACKAGE)
            if (i != null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i) }
            else { tvStatus.text = "Free Fire Max chưa cài"; tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light)) }
        }

        if (isAvail() && hasPerm()) bindSvc()
        updateShizukuStatus(userService != null)
    }

    private fun bindSvc() {
        try { Shizuku.bindUserService(serviceArgs, serviceConnection) }
        catch (e: Exception) { toast("Bind thất bại: ${e.message}") }
    }

    private fun isAvail() = try { Shizuku.pingBinder() } catch (e: Exception) { false }
    private fun hasPerm() = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }

    private fun apply(speed: Int, tv: TextView) {
        val svc = userService ?: run {
            tv.text = "Shizuku chưa kết nối"
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
            return
        }
        if (svc.applyPointerSpeed(speed) == 0) {
            tv.text = "DPI Config $speed — áp dụng thành công"
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
        } else {
            tv.text = "Lỗi apply"
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        }
    }

    private fun updateShizukuStatus(on: Boolean) {
        val tv = findViewById<TextView>(R.id.tvShizukuStatus)
        tv.text = if (on) "Shizuku: Đã kết nối" else "Shizuku: Chưa kết nối"
        tv.setTextColor(ContextCompat.getColor(this, if (on) android.R.color.holo_green_light else android.R.color.holo_red_light))
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permListener)
        try { Shizuku.unbindUserService(serviceArgs, serviceConnection, true) } catch (_: Exception) {}
    }
}
