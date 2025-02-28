package com.rutasdeautobuses.transmileniositp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huawei.wearengine.device.Device
import com.rutasdeautobuses.transmileniositp.R
import com.rutasdeautobuses.transmileniositp.ui.models.EntPermission
import com.rutasdeautobuses.transmileniositp.ui.theme.Typography
import org.json.JSONObject

@Composable
fun HomeScreen(paddingValues: PaddingValues) {
    val vm: HomeVM = viewModel()
    val scrollState = rememberScrollState()
    // View Model Vars
    val hasAvailableDevice by vm.hasAvailableDevice.collectAsState()
    val permissionList by vm.permissionList.collectAsState()
    val watchDevices by vm.deviceList.collectAsState()
    val selectedDevice by vm.selectedDevice.collectAsState()
    val watchPackageName by vm.watchPackageName.collectAsState()
    val watchAppIsInstalled by vm.watchAppIsInstalled.collectAsState()
    val watchAppVersion by vm.watchAppVersion.collectAsState()
    val pingResult by vm.pingResult.collectAsState()
    val message by vm.message.collectAsState()
    val sentMessageResultCode by vm.sentMessageCodeResult.collectAsState()
    val receiveMessagesStatus by vm.receiveMessage.collectAsState()
    val watchMessage by vm.watchMessage.collectAsState()
    val dataMessage by vm.dataMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {

        HasAvailableDevicesView(hasAvailableDevice, vm::onRefreshHasAvailableDevicesStatus)
        Spacer(modifier = Modifier.height(8.dp))
        PermissionsView(
            permissions = permissionList,
            vm::onPermissionCheckChange,
            vm::onRequestPermissions
        )
        Spacer(modifier = Modifier.height(8.dp))
        WatchesView(
            watchDevices,
            selectedDevice,
            vm::onSelectedDeviceChange,
            vm::onRefreshWatchList
        )
        Spacer(modifier = Modifier.height(8.dp))
        WatchAppView(
            packageName = watchPackageName,
            isInstalled = watchAppIsInstalled,
            version = watchAppVersion,
            pingResult = pingResult,
            onPackageNameChange = vm::onWatchPackageNameChange,
            onClickVerifyApp = vm::onVerifyWatchAppIsInstalledClick,
            onClickPing = vm::onSentPingToWatch
        )
        Spacer(modifier = Modifier.height(8.dp))
        SentDataMessage(
            dataMessage = dataMessage,
            messageResult = sentMessageResultCode,
            onMessageChange = vm::onDataMessageParamChange,
            onClickSentMessage = vm::onSentMessageClick
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatusLabel(text: String, status: Boolean) {
    Text(
        text = "$text: ${if (status) "\uD83D\uDFE2" else "\uD83D\uDD34"}"
    )
}

@Composable
private fun CollapsableView(
    title: String,
    buttons: @Composable() () -> Unit,
    content: @Composable () -> Unit,
) {
    var collapsed by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(modifier = Modifier.weight(1f), text = title, style = Typography.titleLarge)
                buttons()
                IconButton(onClick = { collapsed = !collapsed }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = ""
                    )
                }
            }
            AnimatedVisibility(visible = !collapsed) {
                content()
            }
        }
    }
}

@Composable
private fun HasAvailableDevicesView(hasAvailableDevices: Boolean, refreshStatus: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Step 1: Has available devices ${if (hasAvailableDevices) "\uD83D\uDFE2" else "\uD83D\uDD34"}",
                    style = Typography.titleLarge
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionsView(
    permissions: List<EntPermission>,
    onPermissionChecked: (EntPermission) -> Unit,
    onClickRequestPermissions: () -> Unit,
) {
    CollapsableView(title = "Step 2: Request permissions", buttons = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            permissions.forEach { p ->
                PermissionItemView(
                    p.name,
                    p.checked,
                    p.granted
                ) { onPermissionChecked(p) }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClickRequestPermissions
            ) { Text(text = "Request permissions") }
        }
    }
}

@Composable
private fun PermissionItemView(
    text: String,
    isChecked: Boolean,
    status: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(),
            enabled = !status
        )
        Spacer(modifier = Modifier.width(8.dp))
        StatusLabel(text = text, status)
    }
}


@Composable
private fun WatchesView(
    watches: List<Device>,
    selected: Device?,
    onSelectWatch: (Device) -> Unit,
    onClickRefreshList: () -> Unit,
) {
    CollapsableView(
        title = "Step 3: Choose watch",
        buttons = {
            IconButton(onClick = onClickRefreshList) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = ""
                )
            }
        }
    ) {
        if (watches.isEmpty())
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "😭", fontSize = 48.sp)
                Text("There are not watches")
            }
        else
            LazyRow {
                items(items = watches) {
                    WatchItem(it, it == selected, onClick = { onSelectWatch(it) })
                }
            }
    }
}

@Composable
private fun WatchItem(watch: Device, active: Boolean = false, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .wrapContentWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_watch),
            contentDescription = "Watch",
            colorFilter = ColorFilter.tint(color = if (active) Color.Green else Color.LightGray)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = watch.name)
        Spacer(modifier = Modifier.height(8.dp))
        StatusLabel(text = "Connected", status = watch.isConnected)
    }
}

@Composable
private fun WatchAppView(
    packageName: String,
    isInstalled: Boolean,
    version: Int?,
    pingResult: Int,
    onPackageNameChange: (String) -> Unit,
    onClickVerifyApp: () -> Unit,
    onClickPing: () -> Unit,
) {
    CollapsableView(title = "Step 4: App", buttons = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    label = { Text("Watch App Package Name") },
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentSize(),
                    value = packageName,
                    onValueChange = onPackageNameChange
                )
                IconButton(onClick = onClickVerifyApp) {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            StatusLabel(text = "App is intalled", isInstalled)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "App version ${version ?: ""}")

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ping to app",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = onClickPing) { Text("Sent pint to app") }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Ping result $pingResult")

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SentDataMessage(
    dataMessage: JSONObject,
    messageResult: Int,
    onMessageChange: (String, String) -> Unit,
    onClickSentMessage: () -> Unit,
) {
    CollapsableView(
        title = "STEP 5: Sent Message to App",
        buttons = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Message to app",
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            dataMessage.keys().forEach {
                key ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "$key:")
                    TextField(
                        value = dataMessage.get(key).toString(),
                        onValueChange = { newValue -> onMessageChange(key, newValue) }
                    )
                }
            }

            Button(onClick = onClickSentMessage) { Text("Sent message") }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "MESSAGE RESULT: $messageResult")
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ReciveMessageView(
    message: String,
    receiveMessages: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    CollapsableView(
        title = "Recive Message",
        buttons = {}
    ) {
        Column() {
            Row(modifier = Modifier.fillMaxWidth()) {
                Switch(checked = receiveMessages, onCheckedChange = onCheckedChange)
                Text("Receive messsages")
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(), text = message
            )
        }
    }
}