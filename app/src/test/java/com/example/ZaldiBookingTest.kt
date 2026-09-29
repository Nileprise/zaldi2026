package com.example

import com.example.data.model.LocationPoint
import com.example.data.model.PromoCode
import com.example.data.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZaldiBookingTest {

    @Test
    fun testDistanceCalculation() {
        val loc1 = LocationPoint.PRESET_LOCATIONS[0]
        val loc2 = LocationPoint.PRESET_LOCATIONS[1]
        val distance = LocationPoint.calculateDistanceKm(
            loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude
        )
        assertTrue("Distance should be greater than 1km", distance > 1.0)
    }

    @Test
    fun testPromoCodeDiscountCalculation() {
        val flatPromo = PromoCode(
            code = "ZALDI50",
            flatDiscount = 50.0,
            minOrderValue = 100.0,
            maxDiscount = 50.0,
            isActive = true
        )
        val discount = flatPromo.calculateDiscount(300.0)
        assertEquals(50.0, discount, 0.01)

        val percentPromo = PromoCode(
            code = "FIRSTMOVE",
            discountPercent = 20,
            minOrderValue = 150.0,
            maxDiscount = 100.0,
            isActive = true
        )
        val percentDiscount = percentPromo.calculateDiscount(400.0)
        assertEquals(80.0, percentDiscount, 0.01)
    }

    @Test
    fun testVehicleTypeAttributes() {
        assertEquals("Bike", VehicleType.BIKE.title)
        assertEquals(40.0, VehicleType.BIKE.defaultBaseFare, 0.01)
        assertEquals(150.0, VehicleType.MINI_TRUCK.defaultBaseFare, 0.01)
        assertEquals(300.0, VehicleType.TRUCK.defaultBaseFare, 0.01)
    }
}
