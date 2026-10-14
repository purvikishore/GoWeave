package com.example.model

enum class TransportMode(val displayName: String) {
    BUS("Bus"),
    METRO("Metro Train"),
    AUTO("Auto Rickshaw"),
    CAB("Cab / Taxi"),
    WALK("Walk")
}

enum class RoutePreference(val label: String, val description: String) {
    BEST_BALANCED("Best Balanced", "Optimal trade-off between time, fare, and walking distance"),
    FASTEST("Fastest Route", "Shortest commute time with priority express links"),
    CHEAPEST("Cheapest Route", "Lowest out-of-pocket fare utilizing public bus & metro")
}

enum class LocationCategory(val label: String, val emoji: String) {
    ALL("All Places", "📍"),
    PARK("Parks & Gardens", "🌳"),
    MALL("Shopping & Markets", "🛍️"),
    HOSPITAL("Hospitals & Clinics", "🏥"),
    COLLEGE("Colleges & Univs", "🎓"),
    HERITAGE("Heritage & Tourism", "🏛️"),
    DINING("Dining & Social", "🍽️"),
    TRANSIT("Metro, Bus & Rail", "🚇"),
    TECH_PARK("Tech Parks & CBD", "💼")
}

enum class TimingType(val label: String) {
    LEAVE_NOW("Leave Now"),
    DEPART_AT("Depart At"),
    ARRIVE_BY("Arrive By")
}

data class TransitLocation(
    val id: String,
    val name: String,
    val area: String,
    val lat: Double,
    val lng: Double,
    val landmark: String,
    val tag: String? = null,
    val category: LocationCategory = LocationCategory.TRANSIT,
    val pinCode: String = "560001",
    val fullAddress: String = "",
    val openingHours: String? = null,
    val usefulDetails: String? = null
)

data class CommuteRideRecommendation(
    val id: String,
    val title: String,
    val type: String, // "HOME_TO_WORK", "WORK_TO_HOME"
    val origin: TransitLocation,
    val destination: TransitLocation,
    val recommendedDepartureTime: String,
    val timingType: TimingType = TimingType.DEPART_AT,
    val estimatedDurationMinutes: Int,
    val estimatedFare: Double,
    val routeSummary: String,
    val trafficStatus: String = "Normal Flow",
    val cheapestFare: Double = 35.0,
    val fastestDuration: Int = 32
)

data class CustomTransitLine(
    val id: String,
    val lineCode: String,
    val lineName: String,
    val mode: TransportMode,
    val colorHex: String,
    val stationIds: List<String>,
    val frequencyMinutes: Int = 8,
    val baseFare: Double = 15.0
)

data class LiveVehicle(
    val id: String,
    val vehicleNumber: String,
    val mode: TransportMode,
    val routeName: String,
    val lat: Double,
    val lng: Double,
    val headingDegrees: Float,
    val speedKmh: Int,
    val nextStop: String,
    val etaMinutes: Int,
    val occupancyStatus: String, // "SEATS_AVAILABLE", "STANDING_ONLY", "AVAILABLE_NOW"
    val driverName: String? = null,
    val rating: Float = 4.8f,
    val baseFare: Double = 25.0
)

data class JourneySegment(
    val id: String,
    val mode: TransportMode,
    val operatorName: String,
    val routeLabel: String,
    val fromStation: String,
    val toStation: String,
    val departureTime: String,
    val arrivalTime: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val fare: Double,
    val isBookingSupported: Boolean,
    val bookingType: String, // e.g. "DIGITAL_QR", "IN_APP_TOKEN", "METER_ESTIMATE", "FREE"
    val intermediateStopsCount: Int,
    val intermediateStopNames: List<String> = emptyList(),
    val delayMinutes: Int = 0,
    val liveStatus: String = "ON_TIME", // "ON_TIME", "DELAYED", "HIGH_FREQUENCY"
    val bufferNotice: String? = null,
    val walkingInstructions: String? = null,
    val lineHexColor: String? = null
)

data class DisruptionAlert(
    val headline: String,
    val details: String,
    val alternativeExplanation: String,
    val originalRouteTitle: String,
    val timeDiffNotice: String
)

data class RouteOption(
    val id: String,
    val preferenceType: RoutePreference,
    val title: String,
    val badgeTag: String,
    val totalDurationMinutes: Int,
    val totalFare: Double,
    val totalWalkMeters: Int,
    val transfersCount: Int,
    val totalDistanceKm: Double,
    val departureTime: String,
    val arrivalTime: String,
    val segments: List<JourneySegment>,
    val keyHighlights: List<String>,
    val carbonSavedKg: Double,
    val caloriesBurned: Int,
    val reliabilityScorePercent: Int,
    val disruptionAlert: DisruptionAlert? = null
)

data class TransitTicket(
    val ticketId: String,
    val operatorName: String,
    val mode: TransportMode,
    val legName: String,
    val fare: Double,
    val qrPayload: String,
    val pnr: String,
    val gateAccessText: String,
    val validityWindow: String,
    val status: String = "VALID"
)

data class BookingRecord(
    val bookingId: String,
    val routeOption: RouteOption,
    val originName: String,
    val destinationName: String,
    val bookedAt: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val status: String = "CONFIRMED", // "CONFIRMED", "ACTIVE", "CANCELLED", "REFUNDED"
    val tickets: List<TransitTicket>,
    val cancellationPolicy: String = "Free cancellation up to 15 mins before scheduled departure",
    val refundAmount: Double = 0.0
)

data class SavedPlace(
    val id: String,
    val title: String,
    val address: String,
    val location: TransitLocation,
    val category: String // "HOME", "WORK", "GYM", "FAVORITE"
)

data class DailyCommutePlan(
    val id: String,
    val title: String,
    val origin: TransitLocation,
    val destination: TransitLocation,
    val targetArrivalTime: String,
    val preferredMode: RoutePreference,
    val activeDays: String,
    val alertEnabled: Boolean = true
)

data class OperatorStatus(
    val operatorName: String,
    val mode: TransportMode,
    val status: String, // "OPERATIONAL", "DEGRADED", "SIMULATED_PROTOTYPE"
    val latencyMs: Int,
    val lastSyncTime: String,
    val feedType: String,
    val activeVehicles: Int
)

data class AiCommuteMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val suggestedPromptOptions: List<String> = emptyList()
)
