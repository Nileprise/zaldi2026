    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .testTag(if (!driverId.isNullOrBlank()) "driver_card_$driverId" else "driver_availability_card"),
        shape = RoundedCornerShape(12.dp), // Sharpened from 18dp
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Removed shadow
        border = BorderStroke(1.dp, BorderLight) // Added structural border
    ) {
        // ... (Keep internal contents, but replace AmberContainer with AccentBlueContainer 
        // and AmberPrimary with AccentBlue for icons/text inside)
