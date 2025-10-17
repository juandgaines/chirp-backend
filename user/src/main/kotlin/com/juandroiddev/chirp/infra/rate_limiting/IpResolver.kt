package com.juandroiddev.chirp.infra.rate_limiting

import com.juandroiddev.chirp.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Resolves the real client IP address when the application is behind an nginx reverse proxy.
 * 
 * In production, nginx acts as a reverse proxy and forwards the real client IP via headers.
 * This class ensures we get the actual client IP for rate limiting and security purposes.
 */
@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {

    companion object{
        // RFC1918 private IP ranges that should not be considered as valid client IPs
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",       // Class A private
            "172.16.0.0/12",    // Class B private
            "192.168.0.0/16",   // Class C private
            "127.0.0.0/8",      // Loopback
            "::1/128",          // IPv6 loopback
            "fc00::/7",         // IPv6 unique local
            "fe80::/10"         // IPv6 link local
        ).map { IpAddressMatcher(it) }

        // Common invalid IP values that might be sent in spoofed headers
        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }

    // Pre-compile trusted proxy IP matchers from configuration for performance
    // Converts IPs to CIDR notation: single IPs become /32 (IPv4) or /128 (IPv6)
    private val trustedMatchers: List<IpAddressMatcher> = nginxConfig
        .trustedIps
        .filter {
            it.isNotBlank()
        }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy        // Already in CIDR notation
                proxy.contains(":") -> "$proxy/128" // IPv6 single host
                else -> "$proxy/32"                 // IPv4 single host
            }
            IpAddressMatcher(cidr)
        }

    private val logger  = LoggerFactory.getLogger(IpResolver::class.java)

    /**
     * Main entry point: Extracts the real client IP from the request.
     * 
     * Flow:
     * 1. Check if request comes from a trusted proxy (nginx)
     * 2. If yes -> extract real IP from X-Real-IP header
     * 3. If no -> use direct connection IP (only in dev mode)
     * 4. Validate and return the IP
     */
    fun getClientIp(request: HttpServletRequest): String{
        val remoteAddr = request.remoteAddr

        // Step 1: Is this request coming through our nginx proxy?
        if (!isFromTrustedProxy(remoteAddr)){
            // Direct connection - only allowed in development
            if (nginxConfig.requireProxy){
                logger.warn("Direct connection attempt from $remoteAddr")
                throw SecurityException("No valid client IP in proxy headers")
            }
            return remoteAddr
        }

        // Step 2: Extract real client IP from nginx header
        val clientIp = extractFromXRealIp(
            request = request,
            proxyIp = remoteAddr
        )
        
        // Step 3: Validate we got a real IP from nginx
        if (clientIp == null){
            logger.warn("No valid client IP in proxy headers")
            if (nginxConfig.requireProxy)
                throw SecurityException("No valid client IP in proxy headers")
        }

        return clientIp ?: remoteAddr
    }

    /**
     * Extracts the client IP from the X-Real-IP header that nginx sets.
     * Nginx configuration: proxy_set_header X-Real-IP $remote_addr;
     */
    private fun extractFromXRealIp(
        request: HttpServletRequest,
        proxyIp:String
    ): String? {
        return request.getHeader("X-Real-IP")?.let { header ->
            validateAndNormalizeIp(
                ip = header,
                headerName = "X-Real-IP",
                proxyIp = proxyIp
            )
        }
    }

    /**
     * Validates and normalizes IP addresses to prevent spoofing.
     * 
     * Security checks:
     * - Rejects invalid IPs like "unknown", "0.0.0.0"
     * - Validates IP format (IPv4 and IPv6)
     * - Warns about private IPs (but still accepts them for logging)
     * - Returns normalized IP address
     */
    private fun validateAndNormalizeIp(
        ip: String,
        headerName: String,
        proxyIp: String
    ): String? {
        val trimmedIp = ip.trim()
        
        // Check against blacklist of invalid values
        if (trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)){
            logger.debug("Invalid IP in $headerName $ip from proxy $proxyIp")
            return null
        }

        return try {
            // Parse and validate IP format
            val inetAddr = when{
                trimmedIp.contains(":") -> InetAddress.getByName(trimmedIp)      // IPv6
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) ->       // IPv4
                    Inet4Address.getByName(trimmedIp)
                else -> {
                    logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp")
                    return null
                }
            }
            
            // Log if it's a private IP (might indicate misconfiguration)
            if(isPrivateIp(inetAddr.hostAddress)) {
                logger.debug("Private IP in $headerName: $trimmedIp from proxy $proxyIp")
            }
            
            inetAddr.hostAddress  // Return normalized IP
        }catch (e:Exception){
            logger.warn("Invalid IP format in $headerName: $ip from proxy $proxyIp")
            null
        }
    }



    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { matcher ->
            matcher.matches(ip)
        }
    }

    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { matcher ->
            matcher.matches(ip)
        }
    }

}

/**
 * ARCHITECTURE DIAGRAM: How IP Resolution Works with Nginx
 * =========================================================
 * 
 * PRODUCTION SETUP (require-proxy: true):
 * ----------------------------------------
 *                                                     
 *    [Client Browser]                                
 *          |                                         
 *          | Real IP: 89.123.45.67                  
 *          ↓                                         
 *    [Internet/Firewall]                            
 *          |                                         
 *          ↓                                         
 *    [Nginx Reverse Proxy] ← Trusted IP: 10.0.0.5  
 *          |                                         
 *          | Sets header: X-Real-IP: 89.123.45.67   
 *          | RemoteAddr becomes: 10.0.0.5           
 *          ↓                                         
 *    [Spring Application]                            
 *          |                                         
 *          ↓                                         
 *    [IpResolver.getClientIp()]                     
 *          |                                         
 *          | 1. Checks remoteAddr (10.0.0.5)        
 *          | 2. Matches trusted-ips list ✓           
 *          | 3. Reads X-Real-IP header               
 *          | 4. Validates IP format                  
 *          | 5. Returns: 89.123.45.67                
 *          ↓                                         
 *    [Rate Limiting uses real IP]                   
 * 
 * 
 * DEVELOPMENT SETUP (require-proxy: false):
 * ------------------------------------------
 * 
 *    [Developer Browser]                             
 *          |                                         
 *          | Direct connection: 127.0.0.1            
 *          ↓                                         
 *    [Spring Application]                            
 *          |                                         
 *          ↓                                         
 *    [IpResolver.getClientIp()]                     
 *          |                                         
 *          | 1. Checks remoteAddr (127.0.0.1)       
 *          | 2. NOT in trusted-ips list              
 *          | 3. require-proxy = false                
 *          | 4. Returns: 127.0.0.1                   
 *          ↓                                         
 *    [Rate Limiting uses direct IP]                 
 * 
 * 
 * SECURITY FEATURES:
 * ------------------
 * • In production, direct connections are blocked (SecurityException)
 * • Invalid IPs like "unknown", "0.0.0.0" are rejected
 * • IP format is validated (prevents injection attacks)
 * • Private IPs are logged but allowed (for debugging)
 * • Supports both IPv4 and IPv6 addresses
 * 
 * CONFIGURATION (application.yml):
 * ---------------------------------
 * nginx:
 *   trusted-ips:
 *     - "127.0.0.1"       # Local development
 *     - "172.17.0.0/16"   # Docker network
 *     - "10.0.0.5/32"     # Production nginx server
 *   require-proxy: true   # Enforced in production
 */