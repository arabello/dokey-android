package io.rocketguys.dokey.network

import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.*

/**
 * Used to parse the QR code payload, extracting the correct ip address and the key
 */
class NetworkPayloadResolver {
    /**
     * Parse the given payload and determine which combination of address/port
     * is working
     */
    fun parse(payload: String) : Result?  {
        val tokens = payload.split(";")
        val ipTokens = tokens[1]
        val keyToken = tokens[2]

        // Decode the ips
        val ipPairs = ipTokens.split(":").map {ipToken ->
            val ip = ipToken.split("/")[0]
            val networkPrefixLength = ipToken.split("/")[1].toInt()
            Pair(ip, networkPrefixLength)
        }

        // Decode the key
        val key = keyToken.split(",").map { it.toByte() }.toByteArray()

        // Get the phone network interfaces prefixes
        val prefixes = getInterfacesNetPrefixes()

        // Check if one of the ips is in the same network of the phone
        var correctIp : String? = null
        ipPairs.forEach {
            val prefix = getNetworkPrefixForAddress(it.first, it.second)
            if (prefix in prefixes) {
                correctIp = it.first
            }
        }

        if (correctIp != null) {
            return Result(correctIp!!, key)
        }

        return null
    }

    /**
     * Return the list of network prefixes of all network interfaces of the device.
     * This is later used to check if one of the given ip addresses is in the same network.
     */
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
    data class Result(val address: String, val key: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Result

            if (address != other.address) return false
            if (!Arrays.equals(key, other.key)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = address.hashCode()
            result = 31 * result + Arrays.hashCode(key)
            return result
        }
    }

    companion object {
        /**
         * Given an IP and a network prefix length, extract the network part of the
         * address in binary form.
         */
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