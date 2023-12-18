package io.nerdythings.chapter01.ktor.esp32

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NetworkMonitor @Inject constructor(@ApplicationContext context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    // Mutable state
    private val _ipAddress = MutableStateFlow("")
    // Expose immutable state
    val ipAddress = _ipAddress.asStateFlow()

    private val networkCallback = CustomNetworkCallback(connectivityManager)

    fun start() {
        // Get Wi-Fi connection network request
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        // Register to events from Wi-Fi network
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun stop() {
        // Unregister event listener
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    inner class CustomNetworkCallback(private val connectivityManager: ConnectivityManager) :
        NetworkCallback() {

        private val ipv4Address = Regex("/\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            val props: LinkProperties? = connectivityManager.getLinkProperties(network)
            props?.linkAddresses?.forEach {
                // Filter ipV4 address aka (192.168.X.X)
                val ip = it.address.toString()
                if (ip.matches(ipv4Address)) {
                    _ipAddress.value = ip.replace("/", "")
                }
            }
        }

        override fun onUnavailable() {
            super.onUnavailable()
            _ipAddress.value = ""
        }
    }
}
