package io.rocketguys.dokey.network

import android.content.Context
import java.util.*
import java.net.Inet4Address
import java.net.NetworkInterface


class NetworkPayloadResolver(val context: Context) {
    /**
     * Parse the given payload and determine which combination of address/port
     * is working
     */
    fun parse(payload: String) : Result?  {
        getInterfacesNetPrefixes()
        return null
    }

    private fun getInterfacesNetPrefixes() : List<String> {
        val output = mutableListOf<String>()

        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement() as NetworkInterface

            if (networkInterface.isLoopback || !networkInterface.isUp) {
                continue // Don't want to broadcast to the loopback interface
            }

            for (interfaceAddress in networkInterface.interfaceAddresses) {
                val address = interfaceAddress.address ?: continue
                if (address is Inet4Address) {
                    val networkPrefix = getNetworkPrefixForAddress(address.hostAddress, interfaceAddress.networkPrefixLength.toInt())
                    output.add(networkPrefix)
                }
            }
        }

        return output
    }

    /**
     * The result from the payload parsing
     */
    data class Result(val address: String, val port : Int, val key: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Result

            if (address != other.address) return false
            if (port != other.port) return false
            if (!Arrays.equals(key, other.key)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = address.hashCode()
            result = 31 * result + port
            result = 31 * result + Arrays.hashCode(key)
            return result
        }
    }

    companion object {
        fun getNetworkPrefixForAddress(address: String, networkPrefixLength: Int): String {
            val parts = address.split(".")
            var addressBits = ""
            for (part in parts) {
                val byte = part.toInt()
                val bitString = String.format("%8s", Integer.toBinaryString(byte and 0xFF)).replace(' ','0')
                addressBits += bitString
            }
            val networkPrefix = addressBits.substring(0, networkPrefixLength)
            return networkPrefix
        }
    }
}