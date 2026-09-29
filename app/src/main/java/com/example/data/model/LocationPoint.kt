package com.example.data.model

data class LocationPoint(
    val id: String,
    val title: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val category: String = "Commercial",
    // Indian Address Structured Details
    val flatPlotNo: String = "",
    val buildingName: String = "",
    val street: String = "",
    val landmark: String = "",
    val area: String = "",
    val city: String = "Bengaluru",
    val state: String = "Karnataka",
    val pincode: String = "560001",
    val contactPerson: String = "Prem Kumar",
    val contactPhone: String = "+91 98765 43210"
) {
    fun fullFormattedAddress(): String {
        val parts = mutableListOf<String>()
        if (flatPlotNo.isNotBlank()) parts.add(flatPlotNo)
        if (buildingName.isNotBlank()) parts.add(buildingName)
        if (street.isNotBlank()) parts.add(street)
        if (landmark.isNotBlank()) parts.add("Opp/Near $landmark")
        if (area.isNotBlank()) parts.add(area)
        if (city.isNotBlank()) parts.add(city)
        if (state.isNotBlank()) parts.add(state)
        if (pincode.isNotBlank()) parts.add(pincode)

        return if (parts.isNotEmpty()) parts.joinToString(", ") else address
    }

    companion object {
        val PRESET_LOCATIONS = listOf(
            // Bengaluru Logistics & Freight Hubs
            LocationPoint(
                id = "blr_peenya",
                title = "Peenya Industrial Area (Gate 3)",
                address = "Plot 42, 2nd Phase, Heavy Freight Corridor, Peenya, Bengaluru, Karnataka 560058",
                latitude = 13.0300,
                longitude = 77.5200,
                category = "Industrial & Machinery",
                flatPlotNo = "Plot 42/B",
                buildingName = "Apex Metal Fabrication Shed",
                street = "14th Cross, 2nd Stage",
                landmark = "Near Peenya 2nd Stage Police Station & Metro Pillar #412",
                area = "Peenya Industrial Area",
                city = "Bengaluru",
                state = "Karnataka",
                pincode = "560058",
                contactPerson = "Raghavan N.",
                contactPhone = "+91 98450 11992"
            ),
            LocationPoint(
                id = "blr_apmc",
                title = "Yeshwanthpur Wholesale APMC Yard",
                address = "Yard 14, Commercial Loading Bay, APMC Market, Yeshwanthpur, Bengaluru 560022",
                latitude = 13.0210,
                longitude = 77.5510,
                category = "APMC Mandi & Wholesale",
                flatPlotNo = "Shop #114-116",
                buildingName = "APMC Grain Merchant Block",
                street = "Subedarpalya Main Road",
                landmark = "Opposite Yeshwanthpur Railway Station Goods Shed",
                area = "Yeshwanthpur",
                city = "Bengaluru",
                state = "Karnataka",
                pincode = "560022",
                contactPerson = "Manjunath Traders",
                contactPhone = "+91 97412 88019"
            ),
            LocationPoint(
                id = "blr_whitefield",
                title = "Whitefield Cyber Tech Park",
                address = "Tower C, EPIP Zone, ITPL Main Rd, Whitefield, Bengaluru 560066",
                latitude = 12.9850,
                longitude = 77.6250,
                category = "Tech Park & Corporate",
                flatPlotNo = "Level 3, Loading Bay D",
                buildingName = "Prestige Cyber Tower",
                street = "Whitefield Ring Road",
                landmark = "Near Satya Sai Super Specialty Hospital & ITPL Gate 2",
                area = "Whitefield",
                city = "Bengaluru",
                state = "Karnataka",
                pincode = "560066",
                contactPerson = "Sunil Logistics Desk",
                contactPhone = "+91 99001 44521"
            ),
            LocationPoint(
                id = "blr_central_hub",
                title = "Central City Logistics Hub",
                address = "Gate 4, Metro Freight Corridor, Sector 18, Majestic, Bengaluru 560001",
                latitude = 12.9716,
                longitude = 77.5946,
                category = "Warehouse & Express",
                flatPlotNo = "Godown #4",
                buildingName = "Zaldi City Express Terminal",
                street = "Goods Shed Road, Majestic",
                landmark = "Behind KSR City Railway Cargo Office",
                area = "Majestic / Central",
                city = "Bengaluru",
                state = "Karnataka",
                pincode = "560001",
                contactPerson = "Prem Kumar",
                contactPhone = "+91 98765 43210"
            ),
            LocationPoint(
                id = "blr_electronic_city",
                title = "Electronic City Phase 1",
                address = "Plot 98, Velankani Drive, Electronic City Phase 1, Bengaluru 560100",
                latitude = 12.8450,
                longitude = 77.6600,
                category = "Electronics & Manufacturing",
                flatPlotNo = "Building 6, Ramp 2",
                buildingName = "Tech Innovation Center",
                street = "Hosur Elevated Expressway Junction",
                landmark = "Near Infosys Gate 1 & Toll Plaza",
                area = "Electronic City",
                city = "Bengaluru",
                state = "Karnataka",
                pincode = "560100",
                contactPerson = "Deepak S.",
                contactPhone = "+91 98863 77210"
            ),
            LocationPoint(
                id = "blr_koramangala",
                title = "Greenwood Heights Koramangala",
                address = "Flat 402, Block D, 80 Feet Road, 4th Block, Koramangala, Bengaluru 560034",
                latitude = 12.9340,
                longitude = 77.6180,
                category = "Residential Shifting",
                flatPlotNo = "Flat 402",
                buildingName = "Greenwood Heights Apartment",
                street = "80 Feet Main Road, 4th Block",
                landmark = "Opposite Maharaja Signal & Sony World Crossing",
                area = "Koramangala",
                city = "Bengaluru",
                state = "Karnataka",
                pincode = "560034",
                contactPerson = "Rajesh Sharma",
                contactPhone = "+91 98765 43210"
            ),
            LocationPoint(
                id = "blr_airport_cargo",
                title = "Kempegowda International Airport Air Cargo",
                address = "Menzies Bobba Air Cargo Complex, Devanahalli, Bengaluru 560300",
                latitude = 13.1986,
                longitude = 77.7066,
                category = "Cold Chain & Air Freight",
                flatPlotNo = "Air Cargo Bay 3",
                buildingName = "Domestic Express Freight Hub",
                street = "Logistics Boulevard, KIA Complex",
                landmark = "Adjacent to Menzies Bobba Aviation Security Gate",
                area = "Devanahalli",
                city = "Bengaluru",
                state = "Karnataka",
                pincode = "560300",
                contactPerson = "Airport Dispatcher",
                contactPhone = "+91 96112 00412"
            ),
            // Mumbai Logistics Hubs
            LocationPoint(
                id = "mum_vashi_apmc",
                title = "APMC Vashi Fruit & Vegetable Market",
                address = "Sector 19, APMC Market 2, Vashi, Navi Mumbai, Maharashtra 400703",
                latitude = 19.0760,
                longitude = 73.0030,
                category = "APMC Mandi & Wholesale",
                flatPlotNo = "Gala #82",
                buildingName = "Kanda Batata Market Yard",
                street = "Sion-Panvel Highway Express Link",
                landmark = "Near Turbhe Railway Station & Dana Bazar",
                area = "Vashi / Turbhe",
                city = "Navi Mumbai",
                state = "Maharashtra",
                pincode = "400703",
                contactPerson = "Ganesh Patil",
                contactPhone = "+91 98201 55410"
            ),
            LocationPoint(
                id = "mum_bhiwandi",
                title = "Bhiwandi Mega Logistics Park",
                address = "Unit 12, Indian Corporation Logistics Park, Mankoli Naka, Bhiwandi 421302",
                latitude = 19.2960,
                longitude = 73.0630,
                category = "Warehousing & Fulfillment",
                flatPlotNo = "Warehouse #A-4",
                buildingName = "Omni Logistics Hub",
                street = "Mumbai-Nashik Highway NH 160",
                landmark = "Opposite Mankoli Flyover Toll Point",
                area = "Bhiwandi",
                city = "Thane / Mumbai",
                state = "Maharashtra",
                pincode = "421302",
                contactPerson = "Sanjay Logistics",
                contactPhone = "+91 97690 12845"
            ),
            // Delhi NCR Logistics Hubs
            LocationPoint(
                id = "del_okhla",
                title = "Okhla Industrial Area Phase III",
                address = "Plot 64, Okhla Phase 3, Near Modi Mill, New Delhi 110020",
                latitude = 28.5355,
                longitude = 77.2730,
                category = "Industrial & Packaging",
                flatPlotNo = "Plot 64",
                buildingName = "Continental Cargo House",
                street = "Maa Anandmayee Marg",
                landmark = "Near Harkesh Nagar Okhla Metro Station & Modi Mill Flyover",
                area = "Okhla Phase 3",
                city = "New Delhi",
                state = "Delhi NCR",
                pincode = "110020",
                contactPerson = "Vikram Singh",
                contactPhone = "+91 98110 33819"
            ),
            LocationPoint(
                id = "del_gurgaon_udyog",
                title = "Udyog Vihar Phase IV (Cyber City)",
                address = "Plot 18, Udyog Vihar Phase 4, Sector 18, Gurugram, Haryana 122015",
                latitude = 28.5020,
                longitude = 77.0850,
                category = "Commercial & E-Commerce",
                flatPlotNo = "Plot 18",
                buildingName = "Freight Logistics Center",
                street = "NH-48 Service Road, DLF Cyber City Exit",
                landmark = "Near Shankar Chowk & Cyber Hub Crossing",
                area = "Udyog Vihar",
                city = "Gurugram",
                state = "Haryana",
                pincode = "122015",
                contactPerson = "Amit Sharma",
                contactPhone = "+91 98101 22910"
            )
        )

        /**
         * Calculates road distance taking into account urban road network tortuosity.
         * Aerial distance multiplied by 1.28x for realistic Indian city road curvature.
         */
        fun calculateDistanceKm(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val r = 6371.0 // Earth radius in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            val aerialDist = r * c
            // Realistic Indian city road factor (accounts for roundabouts, one-ways, ring roads)
            val roadFactor = 1.28
            val roadDistance = (aerialDist * roadFactor).coerceAtLeast(1.4)
            return String.format(java.util.Locale.US, "%.1f", roadDistance).toDouble()
        }
    }
}
