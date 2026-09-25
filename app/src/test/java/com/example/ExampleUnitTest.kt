package com.example

import com.example.ui.components.DriverAvailabilityStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for app business logic and driver availability statuses.
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
        assertEquals(DriverAvailabilityStatus.IN_TRANSIT, DriverAvailabilityStatus.fromString("ON_TRIP"))

        assertEquals(DriverAvailabilityStatus.OFF_DUTY, DriverAvailabilityStatus.fromString("OFF_DUTY"))
        assertEquals(DriverAvailabilityStatus.OFF_DUTY, DriverAvailabilityStatus.fromString("Off Duty"))
        assertEquals(DriverAvailabilityStatus.OFF_DUTY, DriverAvailabilityStatus.fromString("OFFLINE"))
        assertEquals(DriverAvailabilityStatus.OFF_DUTY, DriverAvailabilityStatus.fromString(null))
        assertEquals(DriverAvailabilityStatus.OFF_DUTY, DriverAvailabilityStatus.fromString(""))
    }

    @Test
    fun driverAvailabilityStatus_displayNames() {
        assertEquals("Available", DriverAvailabilityStatus.AVAILABLE.displayName)
        assertEquals("In Transit", DriverAvailabilityStatus.IN_TRANSIT.displayName)
        assertEquals("Off Duty", DriverAvailabilityStatus.OFF_DUTY.displayName)
    }
}

