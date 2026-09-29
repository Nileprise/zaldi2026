Here is the modernized, professional version of your Redis caching layer.
I have applied several critical modern Kotlin and Android architecture practices to make this production-ready:
 * Dependency Injection over Singletons: Replaced the global object with a class. Global objects make unit testing incredibly difficult and tightly couple your code. This class should now be injected via Hilt, Dagger, or Koin.
 * Asynchronous Operations (suspend): Real Redis operations are network/I/O bound. The methods are now suspend functions using Dispatchers.IO to ensure they never block the main thread.
 * Thread Safety: Replaced standard mutableMapOf with ConcurrentHashMap. Standard maps will crash if multiple coroutines read and write to the cache simultaneously.
 * Kotlin Duration API: Replaced arbitrary integer TTLs with Kotlin's native kotlin.time.Duration API (e.g., 5.minutes, 60.seconds). This eliminates bugs caused by passing seconds into a function expecting milliseconds.
 * Reified Generics: Replaced Any? returns and manual, unsafe as? casting with an inline reified generic get<T>() function. This makes the call sites completely type-safe.
 * Mock TTL Enforcement: Added a CacheEntry wrapper so the mock actually respects and evaluates the TTL, simulating real Redis behavior accurately.
Modernized RedisCacheManager.kt
package com.example.data.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Modernized Caching Layer.
 * Designed to be injected via Dependency Injection (e.g., Hilt/Koin) as a Singleton.
 */
class RedisCacheManager {

    companion object {
        // Cache Keys / Prefixes
        private const val KEY_ACTIVE_DRIVERS_PREFIX = "drivers:active:"
        private const val KEY_DRIVER_LOCATION_PREFIX = "driver:location:"
        private const val KEY_ORDER_STATUS_PREFIX = "order:status:"
        private const val KEY_PRICING_RULES_PREFIX = "pricing:rules:"
        private const val KEY_SURGE_MULTIPLIER = "surge:multiplier"
        private const val KEY_DEMAND_SUPPLY_RATIO = "demand:supply:ratio"
        
        // Modern Kotlin Durations for TTL
        private val TTL_DRIVER_LOCATION = 60.seconds
        private val TTL_ORDER_STATUS = 5.minutes
        private val TTL_PRICING_RULES = 1.hours
        private val TTL_SURGE_DATA = 10.seconds
    }

    // Internal wrapper to simulate actual TTL expiration in the mock
    private data class CacheEntry(val value: Any, val expiresAtMillis: Long)

    // Thread-safe map for concurrent coroutine environments
    private val inMemoryCache = ConcurrentHashMap<String, CacheEntry>()

    /**
     * Core SET operation. 
     * Uses suspend and Dispatchers.IO because real Redis involves network I/O.
     */
    suspend fun set(key: String, value: Any, ttl: Duration = 1.hours) = withContext(Dispatchers.IO) {
        val expiresAt = System.currentTimeMillis() + ttl.inWholeMilliseconds
        inMemoryCache[key] = CacheEntry(value, expiresAt)
        
        // Production: jedis.setex(key, ttl.inWholeSeconds, serializedValue)
    }

    /**
     * Core GET operation. 
     * Uses reified types to eliminate unsafe 'as?' casting at the call site.
     */
    suspend inline fun <reified T> get(key: String): T? = withContext(Dispatchers.IO) {
        val entry = inMemoryCache[key] ?: return@withContext null
        
        // Enforce TTL in our mock
        if (System.currentTimeMillis() > entry.expiresAtMillis) {
            inMemoryCache.remove(key)
            return@withContext null
        }
        
        // Production: val data = jedis.get(key); return deserialize<T>(data)
        return@withContext entry.value as? T
    }

    suspend fun delete(key: String) = withContext(Dispatchers.IO) {
        inMemoryCache.remove(key)
        // Production: jedis.del(key)
    }

    // --- Domain Specific Operations ---

    suspend fun getActiveDriversByGeohash(geohash: String): List<String> = withContext(Dispatchers.IO) {
        // Production: redis.georadius(geohash, radius)
        emptyList()
    }

    suspend fun cacheOrderStatus(orderId: String, status: String) {
        set("$KEY_ORDER_STATUS_PREFIX$orderId", status, TTL_ORDER_STATUS)
    }

    suspend fun getOrderStatus(orderId: String): String? {
        // Reified generic automatically infers String return type
        return get("$KEY_ORDER_STATUS_PREFIX$orderId")
    }

    suspend fun cachePricingRules(vehicleId: String, rules: Map<String, Any>) {
        set("$KEY_PRICING_RULES_PREFIX$vehicleId", rules, TTL_PRICING_RULES)
    }

    suspend fun getPricingRules(vehicleId: String): Map<String, Any>? {
        return get("$KEY_PRICING_RULES_PREFIX$vehicleId")
    }

    suspend fun cacheSurgeMultiplier(multiplier: Float) {
        set(KEY_SURGE_MULTIPLIER, multiplier, TTL_SURGE_DATA)
    }

    suspend fun getSurgeMultiplier(): Float {
        // Fallback to 1.0f if null or missing
        return get(KEY_SURGE_MULTIPLIER) ?: 1.0f
    }

    suspend fun cacheDemandSupplyRatio(ratio: Float) {
        set(KEY_DEMAND_SUPPLY_RATIO, ratio, TTL_SURGE_DATA)
    }

    suspend fun getDemandSupplyRatio(): Float {
        return get(KEY_DEMAND_SUPPLY_RATIO) ?: 1.0f
    }
}

