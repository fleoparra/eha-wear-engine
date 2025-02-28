package com.rutasdeautobuses.transmileniositp.ui.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.wearengine.HiWear
import com.huawei.wearengine.auth.AuthCallback
import com.huawei.wearengine.auth.Permission
import com.huawei.wearengine.device.Device
import com.huawei.wearengine.p2p.Message
import com.huawei.wearengine.p2p.Receiver
import com.huawei.wearengine.p2p.SendCallback
import com.rutasdeautobuses.transmileniositp.ui.models.EntPermission
import com.rutasdeautobuses.transmileniositp.utils.ProgressVars
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class HomeVM @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {

    companion object {
        private const val PACKAGE_NAME = "com.higia.integrapp.watch"
//        private const val WATCH_APP_PUBLIC_KEY =
//            "BIr4oDBsapbz5709RuzBuwFzbN/JuD/4u0rko9wCAf7NDvFXdO7e8qPgTmX8GGXy6Vu4UnywDsGAf+2ztPv2qCQ="
        private const val WATCH_APP_PUBLIC_KEY = "682284312258685632"
    }

    private val deviceClient = HiWear.getDeviceClient(context)
    private val authClient = HiWear.getAuthClient(context)
    private val p2pClient = HiWear.getP2pClient(context)
    private val receiver = Receiver { message ->
        if (message.type == Message.MESSAGE_TYPE_DATA) {
            showMessage("Se recibio un mensaje")
            _watchMessage.value = message.description
        } else if (message.type == Message.MESSAGE_TYPE_FILE) {
            showMessage("Se recibio un archivo")
            _watchMessage.value = message.description
        }
    }

    /**
     * STATES
     */
    // Has available devices
    private val _hasAvailableDevice: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val hasAvailableDevice: StateFlow<Boolean> = _hasAvailableDevice

    // Permissions
    private val _permissionList: MutableStateFlow<List<EntPermission>> = MutableStateFlow(listOf())
    val permissionList: StateFlow<List<EntPermission>> = _permissionList

    // Device list
    private val _deviceLst: MutableStateFlow<List<Device>> = MutableStateFlow(listOf())
    val deviceList: StateFlow<List<Device>> = _deviceLst

    // Selected device
    private val _selectedDevice: MutableStateFlow<Device?> = MutableStateFlow(null)
    val selectedDevice: StateFlow<Device?> = _selectedDevice

    // Watch package name
    private val _watchPackageName: MutableStateFlow<String> = MutableStateFlow(PACKAGE_NAME)
    val watchPackageName: StateFlow<String> = _watchPackageName

    // Watch app is installed
    private val _watchAppIsInstalled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val watchAppIsInstalled: StateFlow<Boolean> = _watchAppIsInstalled

    // Watch app version
    private val _watchAppVersion: MutableStateFlow<Int?> = MutableStateFlow(null)
    val watchAppVersion: StateFlow<Int?> = _watchAppVersion

    // Ping to watch app result
    private val _pingResult: MutableStateFlow<Int> = MutableStateFlow(0)
    val pingResult: StateFlow<Int> = _pingResult

    // Message
    private val _message: MutableStateFlow<String> = MutableStateFlow("")
    val message: StateFlow<String> = _message

    // Sent message to watch app code result
    private val _sentMessageCodeResult: MutableStateFlow<Int> = MutableStateFlow(0)
    val sentMessageCodeResult: StateFlow<Int> = _sentMessageCodeResult

    // Receive messages from watch is active
    private val _receiveMessage: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val receiveMessage: StateFlow<Boolean> = _receiveMessage

    // Message from watch
    private val _watchMessage: MutableStateFlow<String> = MutableStateFlow("")
    val watchMessage: StateFlow<String> = _watchMessage

    // Dieta Balanceada data object
    private val _dataMessage: MutableStateFlow<JSONObject> = MutableStateFlow(JSONObject())
    val dataMessage: StateFlow<JSONObject> = _dataMessage

    init {
        createDataMessageObject()
        initPermissionList()
        hasAvailableDevices()
    }

    private fun initPermissionList() {
        _permissionList.value = listOf(
            EntPermission(Permission.DEVICE_MANAGER.name, false, false),
            EntPermission(Permission.NOTIFY.name, false, false)
        )
    }

    private fun createDataMessageObject() {
        val data = JSONObject()

        ProgressVars.entries.forEach {
            data.put(it.value, Random.nextInt(0, 10))
        }

        _dataMessage.value = data
    }

    /**
     * UI EVENTS
     */
    fun onRefreshHasAvailableDevicesStatus() {
        hasAvailableDevices()
    }

    fun onPermissionCheckChange(permission: EntPermission) {
        _permissionList.value = _permissionList.value.map { p ->
            if (p.name.equals(permission.name))
                p.copy(checked = p.checked.not())
            else p
        }
    }

    fun onRequestPermissions() {
        requestWearPermissions()
    }

    fun onRefreshWatchList() {
        getDeviceList()
    }

    fun onSelectedDeviceChange(device: Device) {
        _selectedDevice.value = device
    }

    fun onWatchPackageNameChange(newPackage: String) {
        _watchPackageName.value = newPackage
        isWatchAppInstalled()
    }

    fun onVerifyWatchAppIsInstalledClick() {
        isWatchAppInstalled()
    }

    fun onSentPingToWatch() {
        sentPingToWatch()
    }

    fun onMessageChange(newMessage: String) {
        _message.value = newMessage
    }

    fun onSentMessageClick() {
        sentMessageToWatchApp()
    }

    fun onChangeReceiveWatchMessageStatus(active: Boolean) {
        _receiveMessage.value = active
        changeReceiveWatchMessageStatus(active)
    }

    fun onDataMessageParamChange(key: String, value: String) {
        val data = JSONObject(_dataMessage.value.toString())
        data.put(key, value)
        _dataMessage.value = data
    }


    /**
     * WEAR ENGINE FUNCTIONS
     */

    private fun hasAvailableDevices() {
        deviceClient.hasAvailableDevices().addOnSuccessListener { hasAvailable ->
            _hasAvailableDevice.value = hasAvailable
            if (hasAvailable) {
                requestWearPermissions()
            }
        }.addOnFailureListener {
            showMessage("No se encontraron dispositivos: $it")
        }
    }

    private fun requestWearPermissions() {
        val authCallback: AuthCallback = object : AuthCallback {
            override fun onOk(permissions: Array<Permission?>?) {
                showMessage("permisos: $permissions")
                permissions?.let {
                    _permissionList.value = permissions.map {
                        EntPermission(name = it?.name ?: "", checked = true, granted = true)
                    }
                }
                getDeviceList()
            }

            override fun onCancel() {
                showMessage("se cancelo la solicitud de permisos")
            }
        }

        authClient.requestPermission(authCallback, Permission.DEVICE_MANAGER, Permission.NOTIFY)
            .addOnSuccessListener {
                showMessage("se acepto el permiso: $it")
            }
            .addOnFailureListener {
                showMessage("Fallo la soclitud de permisos: $it")
            }
    }

    private fun getDeviceList() {
        deviceClient.getBondedDevices()
            .addOnSuccessListener { devices ->
                _deviceLst.value = devices
            }
            .addOnFailureListener {
                showMessage("No fue posible obtener la lista de watches: $it")
            }
    }

    private fun isWatchAppInstalled() {
        val device = selectedDevice.value
        device?.let {
            if (device.isConnected) {
                val packageName = watchPackageName.value
                if (packageName.isNotBlank()) {
                    p2pClient.isAppInstalled(device, packageName).addOnSuccessListener {
                        _watchAppIsInstalled.value = it
                        showMessage("La aplicacion esta instalada $it")
                        getAppVersion()
                    }.addOnFailureListener {
                        // Error to verify the installation
                        showMessage("No se pudo verificar si la aplicacion esta instalada: $it")
                    }
                } else {
                    showMessage("El packagename no puede estar vacio")
                }
            } else {
                showMessage("El dispositivo seleccionado no esta conectado")
            }
        }
    }

    private fun getAppVersion() {
        val device = selectedDevice.value
        device?.let {
            if (device.isConnected) {
                val packageName = watchPackageName.value
                if (packageName.isNotBlank()) {
                    p2pClient.getAppVersion(device, packageName)
                        .addOnSuccessListener {
                            showMessage("La applicacion se esta ejecutando: $it")
                            _watchAppVersion.value = it
                        }
                        .addOnFailureListener {
                            showMessage("No fue posible detectar si la aplicacion se esta ejecutando: $it")
                        }
                }
            }
        }
    }

    private fun sentPingToWatch() {
        val device = selectedDevice.value
        device?.let {
            if (device.isConnected) {
                val packageName = watchPackageName.value
                if (packageName.isNotBlank()) {
                    p2pClient.setPeerPkgName(packageName);
                    p2pClient.ping(device) {
                        _pingResult.value = it
                        showMessage("Ping Result: $it")
                    }.addOnSuccessListener {
                        showMessage("Ping success: $it")
                    }.addOnFailureListener {
                        showMessage("Ping fail: $it")
                    }
                }
            }
        }
    }

    private fun sentMessageToWatchApp() {
        val messageStr = dataMessage.value.toString()
        val sendMessage: Message = Message.Builder()
            .setPayload(messageStr.toByteArray(StandardCharsets.UTF_8))
            .build()

        val sendCallback: SendCallback = object : SendCallback {
            override fun onSendResult(resultCode: Int) {
                // If the resultCode value is 207, the messages have been sent successfully. Other values indicate that the messages fail to be sent.
                showMessage("SentMessageToWatchApp_SendCallback_onSendResult: $resultCode")
                _sentMessageCodeResult.value = resultCode
            }

            override fun onSendProgress(progress: Long) {
                showMessage("SentMessageToWatchApp_SendCallback_onSendProgress: $progress")
            }
        }

        val device = selectedDevice.value
        device?.let {
            if (device.isConnected) {
                val packageName = watchPackageName.value
                if (packageName.isNotBlank()) {
                    p2pClient.setPeerPkgName(packageName)
//                    p2pClient.setPeerFingerPrint(packageName + "_" + )
                    p2pClient.setPeerFingerPrint(WATCH_APP_PUBLIC_KEY)
                    p2pClient.send(device, sendMessage, sendCallback)
                        .addOnSuccessListener {
                            showMessage("SentMessageToWatchApp_p2pClient.send_onSuccess: $it")
                        }
                        .addOnFailureListener {
                            showMessage("SentMessageToWatchApp_p2pClient.send_onFaillure: $it")
                        }
                }
            }
        }
    }

    private fun changeReceiveWatchMessageStatus(active: Boolean) {
        if (active) {
            val device = selectedDevice.value
            device?.let {
                p2pClient.registerReceiver(device, receiver)
                    .addOnFailureListener(OnFailureListener {
                        // Your phone app fails to receive messages or files.
                        showMessage("No se puedo realizar el registro")
                    })
                    .addOnSuccessListener(OnSuccessListener<Void?> {
                        showMessage("Se realizo el registro con exito")
                    })
            }
        }
        else {
            p2pClient.unregisterReceiver(receiver)
                .addOnFailureListener(OnFailureListener {
                    // Your phone app fails to receive messages or files.
                    showMessage("No se puedo eliminar el registro")
                })
                .addOnSuccessListener(OnSuccessListener<Void?> {
                    showMessage("Se puedo eliminar el registro")
                })
        }
    }

    /**
     * OTHER FUNCTIONS
     */
    private fun showMessage(message: String) {
        Log.e("Oliver404", message)
    }
}