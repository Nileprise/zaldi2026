package com.example.util

object LocationSearchHelper {

    val popularLocations = listOf(
        "Indiranagar 100ft Rd, Bengaluru",
        "Koramangala 4th Block, Bengaluru",
        "Whitefield EPIP Zone, Bengaluru",
        "Electronic City Phase 1, Bengaluru",
        "Peenya Industrial Area Phase 1, Bengaluru",
        "HSR Layout Sector 2, Bengaluru",
        "Rajajinagar 2nd Stage, Bengaluru",
        "Yeshwanthpur APMC Yard, Bengaluru",
        "Jayanagar 4th Block, Bengaluru",
        "Marathahalli Bridge, Bengaluru",
        "Bellandur Outer Ring Road, Bengaluru",
        "Majestic Bus Terminal, Bengaluru",
        "Kempegowda International Airport (BLR), Bengaluru",
        "Hebbal Flyover, Bengaluru",
        "BTM Layout 2nd Stage, Bengaluru",
        "MG Road Metro Station, Bengaluru",
        "Silk Board Junction, Bengaluru",
        "KR Puram Railway Station, Bengaluru",
        "Bannerghatta Main Rd, Bengaluru",
        "Yelahanka New Town, Bengaluru",
        "Bommasandra Industrial Area, Bengaluru",
        "Domlur Flyover, Bengaluru",
        "Manyata Tech Park, Nagavara, Bengaluru",
        "JP Nagar 6th Phase, Bengaluru",
        "Sarjapur Road Wipro Gate, Bengaluru",
        "Malleshwaram 8th Cross, Bengaluru",
        "Basavanagudi Gandhi Bazaar, Bengaluru",
        "Hosur SIPCOT Industrial Area",
        "Nelamangala Highway Toll, Bengaluru",
        "Bidadi Industrial Area, Bengaluru"
    )

    fun search(query: String): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return popularLocations.take(6)
        val filtered = popularLocations.filter {
            it.contains(trimmed, ignoreCase = true)
        }
        return if (filtered.isEmpty()) {
            listOf("$trimmed, Bengaluru")
        } else {
            filtered.take(6)
        }
    }
}
