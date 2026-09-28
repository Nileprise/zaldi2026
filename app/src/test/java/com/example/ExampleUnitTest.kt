package com.example

import com.example.data.model.VehicleCatalog
import com.example.ui.components.DriverAvailabilityStatus
import com.example.util.GoogleMapsRoutingService
import com.example.util.RoutingProfile
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for app business logic, routing profiles, Place IDs, and snap-to-roads.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun driverAvailabilityStatus_parsesCorrectly() {
        assertEquals(DriverAvailabilityStatus.AVAILABLE, DriverAvailabilityStatus.fromString("AVAILABLE"))
        assertEquals(DriverAvailabilityStatus.AVAILABLE, DriverAvailabilityStatus.fromString("Available"))
        assertEquals(DriverAvailabilityStatus.AVAILABLE, DriverAvailabilityStatus.fromString("ONLINE"))

        assertEquals(DriverAvailabilityStatus.IN_TRANSIT, DriverAvailabilityStatus.fromString("IN_TRANSIT"))
        assertEquals(DriverAvailabilityStatus.IN_TRANSIT, DriverAvailabilityStatus.fromString("In Transit"))
        assertEquals(DriverAvailabilityStatus.IN_TRANSIT, DriverAvailabilityStatus.fromString("BUSY"))

        assertEquals(DriverAvailabilityStatus.OFF_DUTY, DriverAvailabilityStatus.fromString("OFF_DUTY"))
        assertEquals(DriverAvailabilityStatus.OFF_DUTY, DriverAvailabilityStatus.fromString("OFFLINE"))
        assertEquals(DriverAvailabilityStatus.OFF_DUTY, DriverAvailabilityStatus.fromString(null))
    }

    @Test
    fun googleMapsRouting_placeCatalogHasValidPlaceIds() {
        val places = GoogleMapsRoutingService.placeCatalog
        assertTrue(places.isNotEmpty())
        for (place in places) {
            assertTrue(place.placeId.startsWith("ChIJ"))
            assertTrue(place.latitude > 12.0 && place.latitude < 14.0)
            assertTrue(place.longitude > 77.0 && place.longitude < 78.0)
        }
    }

    @Test
    fun googleMapsRouting_directionsCalculatesActualRoadKmAndEta() {
        val pickup = GoogleMapsRoutingService.placeCatalog[0] // Indiranagar
        val dropoff = GoogleMapsRoutingService.placeCatalog[1] // Koramangala

        val twoWheelerDirections = GoogleMapsRoutingService.calculateDirections(pickup, dropoff, RoutingProfile.TWO_WHEELER)
        val tataDirections = GoogleMapsRoutingService.calculateDirections(pickup, dropoff, RoutingProfile.TATA_ACE_COMMERCIAL)

        assertTrue(twoWheelerDirections.actualRoadKm > 3.0)
        assertTrue(tataDirections.actualRoadKm > 3.0)
        assertTrue(twoWheelerDirections.etaMinutes <= tataDirections.etaMinutes)
        assertTrue(twoWheelerDirections.waypoints.size >= 2)
    }

    @Test
    fun vehiclePricing_calibratedCorrectly() {
        val bike = VehicleCatalog.tiers.find { it.id == "bike" }
        assertNotNull(bike)
        assertEquals(45.0, bike!!.baseFare, 0.01)
        assertEquals(10.0, bike.perKmRate, 0.01)

        val auto = VehicleCatalog.tiers.find { it.id == "auto" }
        assertNotNull(auto)
        assertEquals(99.0, auto!!.baseFare, 0.01)
        assertEquals(16.0, auto.perKmRate, 0.01)

        val tata = VehicleCatalog.tiers.find { it.id == "tata" }
        assertNotNull(tata)
        assertEquals(249.0, tata!!.baseFare, 0.01)
        assertEquals(24.0, tata.perKmRate, 0.01)
    }

    @Test
    fun roadsApi_snapToRoadProjectCorrectly() {
        val waypoints = listOf(
            LatLng(12.9784, 77.6408),
            LatLng(12.9550, 77.6350),
            LatLng(12.9352, 77.6245)
        )
        // Point slightly off-center
        val offPoint = LatLng(12.9600, 77.6450)
        val snapped = GoogleMapsRoutingService.snapToRoad(offPoint, waypoints)

        assertNotNull(snapped)
        assertTrue(snapped.latitude > 12.93 && snapped.latitude < 12.98)
    }
}
