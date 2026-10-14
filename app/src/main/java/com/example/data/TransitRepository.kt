package com.example.data

import com.example.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class TransitRepository {

    var currentServiceAreaName: String = "Bengaluru City & Metropolitan Area"
        private set

    private val _locations = mutableListOf<TransitLocation>()
    val locations: List<TransitLocation> get() = _locations.toList()

    private val _customLines = mutableListOf<CustomTransitLine>()
    val customLines: List<CustomTransitLine> get() = _customLines.toList()

    private val _liveVehicles = mutableListOf<LiveVehicle>()
    val liveVehicles: List<LiveVehicle> get() = _liveVehicles.toList()

    private val _bookings = mutableListOf<BookingRecord>()
    val bookings: List<BookingRecord> get() = _bookings.toList()

    // Default Home, Work and Custom locations
    var defaultHomeLocation: TransitLocation
        private set

    var defaultWorkLocation: TransitLocation
        private set

    private val _customDefaultPlaces = mutableMapOf<String, TransitLocation>()
    val customDefaultPlaces: Map<String, TransitLocation> get() = _customDefaultPlaces.toMap()

    val operatorStatuses: List<OperatorStatus> = listOf(
        OperatorStatus("BMRCL Namma Metro", TransportMode.METRO, "OPERATIONAL", 32, "Just now", "GTFS-RT v2.1 Live", 64),
        OperatorStatus("BMTC Smart Bus Fleet", TransportMode.BUS, "OPERATIONAL", 55, "Just now", "Fleet GPS Telemetry", 420),
        OperatorStatus("Namma Yatri Open Mobility", TransportMode.AUTO, "OPERATIONAL", 88, "Just now", "ONDC Beckn Gateway", 1850),
        OperatorStatus("QuickRide & City Taxi Pool", TransportMode.CAB, "OPERATIONAL", 110, "Just now", "Live Fleet Dispatch", 430)
    )

    val savedPlaces: List<SavedPlace>
        get() = listOf(
            SavedPlace("sp_home", "Home", defaultHomeLocation.area, defaultHomeLocation, "HOME"),
            SavedPlace("sp_work", "Work", defaultWorkLocation.area, defaultWorkLocation, "WORK"),
            SavedPlace("sp_gym", "Fitness Gym", "HSR Layout Sector 1", _locations.find { it.id == "blr_hsr" } ?: defaultHomeLocation, "GYM"),
            SavedPlace("sp_park", "Cubbon Park", "Central Bengaluru", _locations.find { it.id == "blr_cubbon" } ?: defaultHomeLocation, "FAVORITE")
        )

    val dailyCommutes: List<DailyCommutePlan>
        get() = listOf(
            DailyCommutePlan("dc_work", "Daily Office Commute", defaultHomeLocation, defaultWorkLocation, "09:15 AM", RoutePreference.BEST_BALANCED, "Mon - Fri"),
            DailyCommutePlan("dc_return", "Evening Return Commute", defaultWorkLocation, defaultHomeLocation, "06:30 PM", RoutePreference.FASTEST, "Mon - Fri")
        )

    init {
        // Build the rich Bangalore stations, famous parks, malls, hospitals, tech parks, and heritage catalog
        loadBangaloreNetwork()

        // Set initial default Home and Work locations
        defaultHomeLocation = _locations.find { it.id == "blr_indiranagar" } ?: _locations[0]
        defaultWorkLocation = _locations.find { it.id == "blr_itpl" } ?: _locations.getOrElse(1) { _locations[0] }

        _customDefaultPlaces["Gym"] = _locations.find { it.id == "blr_hsr" } ?: _locations[0]
        _customDefaultPlaces["College"] = _locations.find { it.id == "blr_cubbon" } ?: _locations[0]

        seedDemoBooking()
    }

    /**
     * Complete dataset of Bangalore transport hubs, parks, malls, hospitals, and tech parks.
     */
    fun loadBangaloreNetwork() {
        _locations.clear()
        _customLines.clear()
        _liveVehicles.clear()
        currentServiceAreaName = "Bengaluru City & Metropolitan Area"

        val catalog = listOf(
            // 1. TRANSIT HUBS & RAIL TERMINALS
            TransitLocation("blr_majestic", "Majestic Kempegowda & KSR Station", "Central Bengaluru", 12.9781, 77.5726, "KSRTC, BMTC & Namma Metro Interchange", "Transit Hub", LocationCategory.TRANSIT, "560009", "Tank Bund Rd, Sevashrama, Bengaluru", "Open 24/7", "Platforms 1-10, Purple & Green Metro lines"),
            TransitLocation("blr_yeshwanthpur", "Yeshwanthpur Junction Railway Station", "Yeshwanthpur", 13.0238, 77.5503, "Near Yeshwanthpur Metro Station", "Rail Terminus", LocationCategory.TRANSIT, "560022", "Tumkur Road, Yeshwanthpur, Bengaluru", "Open 24/7", "Major express & interstate train terminus"),
            TransitLocation("blr_cantonment", "Bengaluru Cantonment Railway Station", "Vasanth Nagar", 12.9930, 77.5990, "Near Shivaji Nagar Bus Terminal", "Rail Station", LocationCategory.TRANSIT, "560052", "Station Rd, Vasanth Nagar, Bengaluru", "Open 24/7", "Suburban and South Western Railway hub"),
            TransitLocation("blr_indiranagar", "Indiranagar 100ft Road Metro", "East Bengaluru", 12.9784, 77.6408, "CMH Road Crossing", "Transit Hub", LocationCategory.TRANSIT, "560038", "CMH Rd & 100ft Rd, Indiranagar, Bengaluru", "05:00 AM – 11:30 PM", "Purple Line Metro & feeder electric autos"),
            TransitLocation("blr_mg_road", "MG Road & Trinity Metro Junction", "Central Bengaluru", 12.9756, 77.6066, "Brigade Road Crossing", "Transit Hub", LocationCategory.TRANSIT, "560001", "Mahatma Gandhi Rd, Ashok Nagar, Bengaluru", "05:00 AM – 11:30 PM", "CBD access & walking boulevard"),
            TransitLocation("blr_airport", "Kempegowda Int'l Airport (BLR)", "North Bengaluru", 13.1986, 77.7066, "Terminals 1 & 2 Departure Curb", "Airport Hub", LocationCategory.TRANSIT, "560300", "KIAL Rd, Devanahalli, Bengaluru", "Open 24/7", "Vayu Vajra KIA-8 airport bus bays"),
            TransitLocation("blr_silkboard", "Central Silk Board Interchange", "South Bengaluru", 12.9176, 77.6238, "Hosur Road Ring Road Link", "Interchange", LocationCategory.TRANSIT, "560068", "Hosur Rd, BTM 2nd Stage, Bengaluru", "Open 24/7", "Yellow Line Metro & Volvo AC bus depot"),
            TransitLocation("blr_hsr", "HSR Layout Sector 1 BDA Complex", "South Bengaluru", 12.9121, 77.6446, "Ring Road Complex", "Residential Hub", LocationCategory.TRANSIT, "560102", "27th Main Rd, HSR Layout, Bengaluru", "Open 24/7", "Startups, cafes & residential hub"),

            // 2. PARKS & GARDENS
            TransitLocation("blr_cubbon", "Cubbon Park Green Reserve", "Central Bengaluru", 12.9763, 77.5929, "Near High Court / Kasturba Rd", "City Park", LocationCategory.PARK, "560001", "Kasturba Road, Sampangi Rama Nagar, Bengaluru", "06:00 AM – 08:00 PM", "Lush heritage flora, walking trails & dog park"),
            TransitLocation("blr_lalbagh", "Lalbagh Botanical Garden", "South Bengaluru", 12.9507, 77.5848, "Glass House West Gate (Lalbagh Metro)", "Botanical Park", LocationCategory.PARK, "560004", "Mavalli, South Bengaluru, Bengaluru", "06:00 AM – 07:00 PM", "Centuries-old botanical trees & scenic lake"),
            TransitLocation("blr_bannerghatta", "Bannerghatta National Park & Safari", "South Bengaluru", 12.8003, 77.5777, "Zoo & Safari Main Gate", "Wildlife Reserve", LocationCategory.PARK, "560083", "Bannerghatta Main Rd, Anekal, Bengaluru", "09:30 AM – 05:00 PM (Closed Tue)", "Tiger & Lion safari, butterfly enclosure"),
            TransitLocation("blr_sankey", "Sankey Tank & Promenade", "North-West Bengaluru", 13.0068, 77.5753, "11th Cross Sadashivanagar", "Lake Park", LocationCategory.PARK, "560003", "Kodandarampura, Malleshwaram, Bengaluru", "06:00 AM – 08:00 PM", "Lakeside jogging tracks and quiet boating"),

            // 3. SHOPPING MALLS & MARKETS
            TransitLocation("blr_phoenix", "Phoenix Marketcity & VR Bengaluru", "Whitefield Main Rd", 12.9960, 77.6967, "Opp. Singayyanapalya Metro", "Shopping Mall", LocationCategory.MALL, "560048", "Whitefield Main Rd, Mahadevapura, Bengaluru", "10:00 AM – 10:30 PM", "300+ lifestyle stores, IMAX cinema & dining"),
            TransitLocation("blr_orion", "Orion Mall & Brigade Gateway", "Rajajinagar", 13.0112, 77.5550, "Sandal Soap Factory Metro Link", "Shopping Mall", LocationCategory.MALL, "560055", "Dr Rajkumar Rd, Rajajinagar, Bengaluru", "10:00 AM – 10:00 PM", "Lake promenade, World Trade Center & bowling"),
            TransitLocation("blr_nexus", "Nexus Mall Koramangala", "Koramangala 5th Block", 12.9352, 77.6180, "Hosur Main Road near Forum Signal", "Shopping Mall", LocationCategory.MALL, "560095", "Hosur Rd, Chikku Lakshmaiah Layout, Bengaluru", "10:00 AM – 10:00 PM", "Popular central youth retail & entertainment hub"),
            TransitLocation("blr_commercial", "Commercial Street & Russell Market", "Shivaji Nagar", 12.9822, 77.6080, "Near Shivaji Nagar Bus Terminal", "Shopping Market", LocationCategory.MALL, "560051", "Tasker Town, Shivaji Nagar, Bengaluru", "10:30 AM – 09:30 PM", "Vibrant bustling street markets, fashion & spice bazaar"),
            TransitLocation("blr_mantri", "Mantri Square Mall", "Malleshwaram", 12.9920, 77.5714, "Sampige Road Metro Connected", "Shopping Mall", LocationCategory.MALL, "560003", "Sampige Rd, Malleshwaram, Bengaluru", "10:00 AM – 10:00 PM", "Direct air-conditioned metro station bridge"),

            // 4. HOSPITALS & CLINICS
            TransitLocation("blr_manipal", "Manipal Hospital Old Airport Road", "HAL / Kodihalli", 12.9592, 77.6499, "HAL Main Gate Road", "Super Specialty Hospital", LocationCategory.HOSPITAL, "560017", "98 HAL Old Airport Rd, Kodihalli, Bengaluru", "Emergency 24/7", "Tertiary care multispecialty, cardiology & oncology"),
            TransitLocation("blr_apollo", "Apollo Hospitals Bannerghatta", "Bannerghatta Main Rd", 12.8938, 77.5985, "Opp. IIM Bangalore", "Multispecialty Hospital", LocationCategory.HOSPITAL, "560076", "154/11 Bannerghatta Main Rd, Bengaluru", "Emergency 24/7", "Joint Commission accredited cardiac & organ transplant"),
            TransitLocation("blr_fortis", "Fortis Hospital Cunningham Road", "Vasanth Nagar", 12.9880, 77.5950, "Near Chandrika Hotel", "Hospital", LocationCategory.HOSPITAL, "560052", "14 Cunningham Rd, Vasanth Nagar, Bengaluru", "Emergency 24/7", "Cardiovascular institute, neurology & nephrology"),
            TransitLocation("blr_nimhans", "NIMHANS Neuro & Brain Center", "Lakkasandra", 12.9390, 77.5960, "Near Dairy Circle", "Specialty Hospital", LocationCategory.HOSPITAL, "560029", "Hosur Rd, Lakkasandra, Bengaluru", "Emergency 24/7", "Premier national center for neuroscience and mental health"),
            TransitLocation("blr_narayana", "Narayana Institute of Cardiac Sciences", "Electronic City", 12.8420, 77.6830, "Hosur Road Health City", "Heart Hospital", LocationCategory.HOSPITAL, "560099", "258/A Bommasandra Industrial Area, Bengaluru", "Emergency 24/7", "World's largest pediatric & adult cardiac center"),

            // 5. COLLEGES & UNIVERSITIES
            TransitLocation("blr_iisc", "Indian Institute of Science (IISc)", "Malleshwaram / Yeshwanthpur", 13.0219, 77.5671, "Near Maramma Circle & Yeshwanthpur", "Premier University", LocationCategory.COLLEGE, "560012", "CV Raman Rd, Mathikere, Bengaluru", "09:00 AM – 05:30 PM", "India's highest-ranked scientific research institution"),
            TransitLocation("blr_rvce", "RV College of Engineering (RVCE)", "Mysore Road", 12.9237, 77.4987, "Kengeri Metro Corridor Link", "Engineering College", LocationCategory.COLLEGE, "560059", "Mysore Rd, RV Vidyaniketan, Bengaluru", "08:30 AM – 04:30 PM", "Top engineering college with research centers"),
            TransitLocation("blr_christ", "Christ University Central Campus", "Hosur Road", 12.9344, 77.6060, "Near Dairy Circle & Forum Mall", "University Campus", LocationCategory.COLLEGE, "560029", "Hosur Rd, Bhavani Nagar, Bengaluru", "08:00 AM – 05:00 PM", "Vibrant campus for arts, law, science & management"),
            TransitLocation("blr_mountcarmel", "Mount Carmel College (MCC)", "Vasanth Nagar", 12.9925, 77.5910, "Near Palace Road", "Women's College", LocationCategory.COLLEGE, "560052", "Palace Rd, Vasanth Nagar, Bengaluru", "08:30 AM – 04:30 PM", "Autonomous educational institution for women"),
            TransitLocation("blr_bmsce", "BMS College of Engineering", "Basavanagudi", 12.9410, 77.5655, "Near Bull Temple", "Engineering College", LocationCategory.COLLEGE, "560019", "Bull Temple Rd, Basavanagudi, Bengaluru", "08:30 AM – 04:30 PM", "First private engineering institution in independent India"),

            // 6. DINING & SOCIAL
            TransitLocation("blr_vidyarthibhavan", "Vidyarthi Bhavan Gandhi Bazaar", "Basavanagudi", 12.9450, 77.5720, "Heritage Gandhi Bazaar Street", "Iconic Restaurant", LocationCategory.DINING, "560004", "32 Gandhi Bazaar Main Rd, Basavanagudi, Bengaluru", "06:30 AM – 11:30 AM, 02:00 PM – 08:00 PM", "Legendary crispy Benne Masala Dosa established in 1943"),
            TransitLocation("blr_toit", "Toit Brewpub Indiranagar", "Indiranagar", 12.9790, 77.6400, "100ft Road near Metro", "Microbrewery & Diner", LocationCategory.DINING, "560038", "298 100ft Rd, Indiranagar, Bengaluru", "12:00 PM – 01:00 AM", "Bengaluru's pioneer craft microbrewery & wood-fired pizza"),
            TransitLocation("blr_ctr", "Shri Sagar (CTR) Malleshwaram", "Malleshwaram", 12.9980, 77.5700, "7th Cross Margosa Road", "Heritage Cafe", LocationCategory.DINING, "560003", "7th Cross Margosa Rd, Malleshwaram, Bengaluru", "07:00 AM – 12:30 PM, 04:00 PM – 09:00 PM", "Gold-standard butter dosa & steaming South Indian filter coffee"),
            TransitLocation("blr_social_koramangala", "Koramangala Social", "Koramangala 7th Block", 12.9360, 77.6140, "Industrial Layout near Forum", "Cafe & Lounge", LocationCategory.DINING, "560095", "118 7th Block Industrial Layout, Bengaluru", "09:00 AM – 01:00 AM", "Modern co-working space, fusion food & live music"),

            // 7. TECH PARKS & CBD
            TransitLocation("blr_manyata", "Manyata Embassy Tech Park", "Nagavara / Hebbal", 13.0475, 77.6200, "Hebbal Outer Ring Road", "Tech Park", LocationCategory.TECH_PARK, "560045", "Outer Ring Rd, Nagavara, Bengaluru", "Open 24/7", "Over 120,000 tech employees across global enterprises"),
            TransitLocation("blr_itpl", "ITPL International Tech Park", "Whitefield", 12.9863, 77.7380, "Pattandur Agrahara Metro", "Tech Park", LocationCategory.TECH_PARK, "560066", "Whitefield Main Rd, Pattandur Agrahara, Bengaluru", "Open 24/7", "Pioneering technology park connected directly to Purple Line"),
            TransitLocation("blr_ecospace", "RMZ Ecospace & Bellandur Corridor", "Outer Ring Road", 12.9260, 77.6820, "Bellandur Flyover", "Tech Park", LocationCategory.TECH_PARK, "560103", "Outer Ring Rd, Bellandur, Bengaluru", "Open 24/7", "Heart of Bengaluru's software & IT services corridor"),
            TransitLocation("blr_ecity", "Electronic City Phase 1 Infosys Campus", "South Bengaluru", 12.8399, 77.6770, "Infosys Main Gate 1", "Tech Park", LocationCategory.TECH_PARK, "560100", "Electronics City Phase 1, Hosur Rd, Bengaluru", "Open 24/7", "Pioneering campus with elevated expressway rapid bus link"),
            TransitLocation("blr_ubcity", "UB City Luxury Commercial CBD", "Central Bengaluru", 12.9716, 77.5960, "Vittal Mallya Road near Cubbon Park", "Luxury CBD", LocationCategory.TECH_PARK, "560001", "24 Vittal Mallya Rd, KG Halli, Bengaluru", "10:30 AM – 11:30 PM", "Luxury commercial towers, amphitheater & fine dining"),

            // 8. TOURIST ATTRACTIONS & HISTORICAL LANDMARKS
            TransitLocation("blr_palace", "Bangalore Palace & Royal Grounds", "Vasanth Nagar", 12.9988, 77.5921, "Palace Road Entrance", "Heritage Monument", LocationCategory.HERITAGE, "560052", "Palace Rd, Vasanth Nagar, Bengaluru", "10:00 AM – 05:30 PM", "19th century Tudor-style royal palace with wooden carvings"),
            TransitLocation("blr_vidhanasoudha", "Vidhana Soudha & State Secretariat", "Central Bengaluru", 12.9797, 77.5907, "Dr. Ambedkar Veedhi Metro", "State Capitol", LocationCategory.HERITAGE, "560001", "Dr. Ambedkar Veedhi, Sampangi Rama Nagar, Bengaluru", "09:00 AM – 05:30 PM", "Magnificent granite Neo-Dravidian seat of state legislature"),
            TransitLocation("blr_iskcon", "ISKCON Temple Bangalore", "Rajajinagar", 13.0098, 77.5511, "Mahalakshmi Metro Link", "Spiritual Heritage", LocationCategory.HERITAGE, "560010", "Hare Krishna Hill, Chord Rd, Rajajinagar, Bengaluru", "04:15 AM – 01:00 PM, 04:15 PM – 08:30 PM", "Towering cultural and spiritual shrine with panoramic views"),
            TransitLocation("blr_tipupalace", "Tipu Sultan's Summer Palace", "Chamrajpet", 12.9593, 77.5737, "Near KR Market Metro", "Heritage Palace", LocationCategory.HERITAGE, "560018", "Tippu Sultan Palace Rd, Chamrajpet, Bengaluru", "08:30 AM – 05:30 PM", "Historic Indo-Islamic wooden palace completed in 1791")
        )
        _locations.addAll(catalog)

        // Metro & Bus Lines
        _customLines.addAll(
            listOf(
                CustomTransitLine("line_purple", "Purple Line", "Namma Metro Purple Line", TransportMode.METRO, "#9333EA", listOf("blr_majestic", "blr_cubbon", "blr_vidhanasoudha", "blr_mg_road", "blr_indiranagar", "blr_phoenix", "blr_itpl"), 4, 35.0),
                CustomTransitLine("line_green", "Green Line", "Namma Metro Green Line", TransportMode.METRO, "#16A34A", listOf("blr_orion", "blr_mantri", "blr_majestic", "blr_lalbagh"), 5, 30.0),
                CustomTransitLine("line_b500d", "Bus 500D", "BMTC Volvo Outer Ring Road Express", TransportMode.BUS, "#EA580C", listOf("blr_silkboard", "blr_hsr", "blr_ecospace", "blr_itpl", "blr_manyata"), 8, 25.0),
                CustomTransitLine("line_b335e", "Bus 335E", "BMTC Vajra Majestic to Whitefield", TransportMode.BUS, "#0284C7", listOf("blr_majestic", "blr_mg_road", "blr_indiranagar", "blr_manipal", "blr_itpl"), 10, 30.0),
                CustomTransitLine("line_kia8", "Vayu Vajra KIA-8", "Airport Express Shuttle", TransportMode.BUS, "#2563EB", listOf("blr_ecity", "blr_silkboard", "blr_hsr", "blr_indiranagar", "blr_airport"), 15, 250.0)
            )
        )

        initLiveVehicles()
    }

    private fun initLiveVehicles() {
        _liveVehicles.clear()
        _liveVehicles.addAll(
            listOf(
                LiveVehicle("bus_500d_1", "BMTC 500D-01", TransportMode.BUS, "500D ORR Vajra AC", 12.9260, 77.6820, 85f, 34, "RMZ Ecospace Bellandur", 3, "SEATS_AVAILABLE", "Girish M.", 4.8f, 25.0),
                LiveVehicle("bus_335e_2", "BMTC 335E-14", TransportMode.BUS, "335E Majestic-ITPL", 12.9780, 77.6410, 180f, 28, "Indiranagar 100ft Road Metro", 4, "STANDING_ONLY", "Basavaraj N.", 4.7f, 30.0),
                LiveVehicle("bus_kia8_3", "Vayu Vajra KIA-8", TransportMode.BUS, "Airport Non-Stop", 13.0480, 77.6200, 15f, 54, "Hebbal Flyover Link", 8, "SEATS_AVAILABLE", "Shivanna K.", 4.9f, 250.0),
                LiveVehicle("cab_ka05_1", "Cab KA-05-MM-2914", TransportMode.CAB, "Sedan Prime AC", 12.9750, 77.6060, 240f, 42, "MG Road Trinity Crossing", 2, "AVAILABLE_NOW", "Arun Kumar", 4.9f, 110.0),
                LiveVehicle("cab_ka01_2", "Cab KA-01-EE-8821", TransportMode.CAB, "EV Green Cab", 12.9960, 77.6970, 110f, 38, "Phoenix Marketcity Main Curb", 3, "AVAILABLE_NOW", "Pradeep S.", 4.8f, 95.0),
                LiveVehicle("auto_ny_1", "Auto NY-714", TransportMode.AUTO, "Smart EV Auto", 12.9780, 77.5930, 90f, 26, "Cubbon Park Gate 2", 2, "AVAILABLE_NOW", "Ramesh Gowda", 4.9f, 40.0),
                LiveVehicle("auto_ny_2", "Auto NY-890", TransportMode.AUTO, "Namma Yatri Auto", 12.9350, 77.6180, 310f, 24, "Nexus Mall Koramangala", 3, "AVAILABLE_NOW", "Venkatesh R.", 4.8f, 45.0)
            )
        )
    }

    /**
     * Updates default locations for Home or Work.
     */
    fun setDefaultHome(location: TransitLocation) {
        defaultHomeLocation = location
    }

    fun setDefaultWork(location: TransitLocation) {
        defaultWorkLocation = location
    }

    fun setCustomDefault(label: String, location: TransitLocation) {
        _customDefaultPlaces[label] = location
    }

    /**
     * Generates 1-tap Commute Ride Recommendations:
     * - Home to Work Morning Commute
     * - Work to Home Evening Commute
     */
    fun getCommuteRecommendations(): List<CommuteRideRecommendation> {
        val home = defaultHomeLocation
        val work = defaultWorkLocation

        val homeToWork = CommuteRideRecommendation(
            id = "commute_morning",
            title = "Morning Office Commute",
            type = "HOME_TO_WORK",
            origin = home,
            destination = work,
            recommendedDepartureTime = "08:30 AM",
            estimatedDurationMinutes = 42,
            estimatedFare = 65.0,
            routeSummary = "Auto feeder (8m) → Purple Line Metro (24m) → Covered Walk",
            trafficStatus = "Green / Normal Metro Transit"
        )

        val workToHome = CommuteRideRecommendation(
            id = "commute_evening",
            title = "Evening Return Home",
            type = "WORK_TO_HOME",
            origin = work,
            destination = home,
            recommendedDepartureTime = "06:15 PM",
            estimatedDurationMinutes = 45,
            estimatedFare = 65.0,
            routeSummary = "Metro Express to ${home.area} → Auto Last-Mile",
            trafficStatus = "Moderate ORR road congestion; Metro bypass active"
        )

        return listOf(homeToWork, workToHome)
    }

    /**
     * Real-time search engine with instant query matching across names, areas, landmarks, and categories.
     */
    fun searchLocations(query: String, category: LocationCategory? = null): List<TransitLocation> {
        if (query.isBlank() && (category == null || category == LocationCategory.ALL)) {
            return _locations
        }

        val q = query.trim().lowercase()
        return _locations.filter { loc ->
            val matchesCat = category == null || category == LocationCategory.ALL || loc.category == category
            val matchesQuery = q.isEmpty() ||
                loc.name.lowercase().contains(q) ||
                loc.area.lowercase().contains(q) ||
                loc.landmark.lowercase().contains(q) ||
                loc.pinCode.contains(q) ||
                loc.fullAddress.lowercase().contains(q) ||
                loc.category.label.lowercase().contains(q) ||
                (loc.tag != null && loc.tag.lowercase().contains(q))

            matchesCat && matchesQuery
        }
    }

    fun addStation(
        name: String,
        area: String,
        lat: Double = 12.9716,
        lng: Double = 77.5946,
        landmark: String = "Local Station",
        tag: String? = "Custom",
        category: LocationCategory = LocationCategory.TRANSIT
    ): TransitLocation {
        val id = "blr_${UUID.randomUUID().toString().take(6)}"
        val station = TransitLocation(id, name, area, lat, lng, landmark, tag, category)
        _locations.add(station)
        return station
    }

    fun removeStation(stationId: String) {
        _locations.removeAll { it.id == stationId }
    }

    fun addTransitLine(
        lineCode: String,
        lineName: String,
        mode: TransportMode,
        colorHex: String,
        stationIds: List<String>,
        frequencyMinutes: Int = 10,
        baseFare: Double = 20.0
    ): CustomTransitLine {
        val line = CustomTransitLine(
            id = "line_${UUID.randomUUID().toString().take(6)}",
            lineCode = lineCode,
            lineName = lineName,
            mode = mode,
            colorHex = colorHex,
            stationIds = stationIds,
            frequencyMinutes = frequencyMinutes,
            baseFare = baseFare
        )
        _customLines.add(line)
        return line
    }

    fun clearAllData(newAreaName: String = "My Service Area") {
        currentServiceAreaName = newAreaName
        _locations.clear()
        _customLines.clear()
        _liveVehicles.clear()
        _bookings.clear()
        _customDefaultPlaces.clear()
    }

    fun loadStarterLocalTemplate(newAreaName: String = "My City District") {
        clearAllData(newAreaName)
        val s1 = addStation("City Center Terminal", "Downtown", 12.9716, 77.5946, "Central Square", "Transit Hub", LocationCategory.TRANSIT)
        val s2 = addStation("North Tech Campus", "North Sector", 13.0120, 77.6200, "Innovation Park", "Tech Park", LocationCategory.TECH_PARK)
        val s3 = addStation("Riverside Green Park", "South Sector", 12.9250, 77.5800, "Botanical Promenade", "City Park", LocationCategory.PARK)
        val s4 = addStation("Metro Junction Plaza", "East Sector", 12.9750, 77.6400, "Commercial Arcade", "Shopping Mall", LocationCategory.MALL)
        val s5 = addStation("City Health Care Center", "West Sector", 12.9550, 77.5600, "General Hospital", "Hospital", LocationCategory.HOSPITAL)

        addTransitLine("Bus 10", "Downtown - Tech Shuttle", TransportMode.BUS, "#0284C7", listOf(s1.id, s4.id, s2.id), 8, 20.0)
        addTransitLine("Metro Line 1", "North-South Rapid Line", TransportMode.METRO, "#16A34A", listOf(s3.id, s1.id, s2.id), 6, 25.0)

        defaultHomeLocation = s3
        defaultWorkLocation = s2
        initLiveVehicles()
    }

    fun importFromBatchText(batchText: String): Pair<Int, Int> {
        var stationCount = 0
        var lineCount = 0
        val lines = batchText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        for (line in lines) {
            val upper = line.uppercase()
            if (upper.startsWith("STATION:") || upper.startsWith("STOP:")) {
                val parts = line.substringAfter(":").split(",").map { it.trim() }
                if (parts.isNotEmpty()) {
                    val name = parts.getOrNull(0) ?: "Stop"
                    val area = parts.getOrNull(1) ?: "Local Area"
                    val lat = parts.getOrNull(2)?.toDoubleOrNull() ?: 12.9716
                    val lng = parts.getOrNull(3)?.toDoubleOrNull() ?: 77.5946
                    val landmark = parts.getOrNull(4) ?: "Local Stop"
                    addStation(name, area, lat, lng, landmark)
                    stationCount++
                }
            } else if (upper.startsWith("BUS:") || upper.startsWith("METRO:") || upper.startsWith("LINE:")) {
                val isMetro = upper.startsWith("METRO:")
                val parts = line.substringAfter(":").split(",").map { it.trim() }
                if (parts.isNotEmpty()) {
                    val code = parts.getOrNull(0) ?: "Line"
                    val name = parts.getOrNull(1) ?: "$code Route"
                    val stopNames = parts.drop(2)
                    val stopIds = stopNames.mapNotNull { sn -> _locations.find { it.name.contains(sn, ignoreCase = true) }?.id }
                    val validStopIds = if (stopIds.size >= 2) stopIds else _locations.take(4).map { it.id }
                    addTransitLine(
                        lineCode = code,
                        lineName = name,
                        mode = if (isMetro) TransportMode.METRO else TransportMode.BUS,
                        colorHex = if (isMetro) "#9333EA" else "#EA580C",
                        stationIds = validStopIds
                    )
                    lineCount++
                }
            }
        }

        if (_locations.size >= 2) {
            defaultHomeLocation = _locations[0]
            defaultWorkLocation = _locations[1]
        }
        initLiveVehicles()
        return Pair(stationCount, lineCount)
    }

    fun tickLiveVehicleTelemetry() {
        for (i in _liveVehicles.indices) {
            val v = _liveVehicles[i]
            val angleRad = Math.toRadians(v.headingDegrees.toDouble())
            val deltaLat = sin(angleRad) * 0.00025
            val deltaLng = cos(angleRad) * 0.00025

            val headingChange = (-15..15).random()
            val newHeading = (v.headingDegrees + headingChange + 360) % 360

            val speedChange = (-3..3).random()
            val newSpeed = (v.speedKmh + speedChange).coerceIn(15, 60)

            _liveVehicles[i] = v.copy(
                lat = v.lat + deltaLat,
                lng = v.lng + deltaLng,
                headingDegrees = newHeading,
                speedKmh = newSpeed,
                etaMinutes = (v.etaMinutes - 1).coerceAtLeast(1)
            )
        }
    }

    /**
     * Multimodal journey route calculator incorporating selected ticket booking departure time.
     */
    fun searchRoutes(
        origin: TransitLocation,
        destination: TransitLocation,
        departureTime: String = "08:30 AM",
        budgetMax: Double? = null,
        isDisruptionActive: Boolean = false
    ): List<RouteOption> {
        val baseRoutes = generateRoutesForLocations(origin, destination, departureTime)

        return baseRoutes.map { route ->
            if (isDisruptionActive && route.preferenceType == RoutePreference.BEST_BALANCED) {
                val delayedSegments = route.segments.map { seg ->
                    if (seg.mode == TransportMode.BUS) {
                        seg.copy(
                            delayMinutes = 12,
                            liveStatus = "DELAYED (+12m)",
                            bufferNotice = "⚠️ High surface road congestion. Buffer adjusted."
                        )
                    } else seg
                }
                route.copy(
                    totalDurationMinutes = route.totalDurationMinutes + 5,
                    segments = delayedSegments,
                    disruptionAlert = DisruptionAlert(
                        headline = "Congestion detected on Outer Ring Road",
                        details = "Bus leg delayed by 12m. AI dynamically adjusted connection buffer.",
                        alternativeExplanation = "Switched to Purple Line express corridor to recover 7 minutes.",
                        originalRouteTitle = route.title,
                        timeDiffNotice = "+5 mins vs original schedule"
                    )
                )
            } else {
                route
            }
        }.filter { route ->
            budgetMax == null || route.totalFare <= budgetMax
        }
    }

    private fun generateRoutesForLocations(
        origin: TransitLocation,
        destination: TransitLocation,
        departureTime: String
    ): List<RouteOption> {
        val dLat = abs(origin.lat - destination.lat) * 111.0
        val dLng = abs(origin.lng - destination.lng) * 111.0
        val approxDistanceKm = sqrt(dLat * dLat + dLng * dLng).coerceAtLeast(3.2)
        val distanceRound = (approxDistanceKm * 10).roundToInt() / 10.0

        val matchingMetroLine = _customLines.find { it.mode == TransportMode.METRO }
        val matchingBusLine = _customLines.find { it.mode == TransportMode.BUS }

        val metroLineName = matchingMetroLine?.lineName ?: "Purple Line Express"
        val busLineName = matchingBusLine?.lineName ?: "BMTC 500D AC Vajra"

        // Route 1: BEST BALANCED (Auto first-mile + Metro + Walk)
        val balancedDuration = (approxDistanceKm * 2.6).roundToInt().coerceIn(28, 55)
        val balancedFare = (25.0 + (approxDistanceKm * 3.2)).roundToInt().toDouble()

        val balancedSegments = listOf(
            JourneySegment(
                id = "seg_bal_1",
                mode = TransportMode.AUTO,
                operatorName = "Namma Yatri EV Auto",
                routeLabel = "First-Mile Pickup",
                fromStation = origin.name,
                toStation = "${origin.area} Metro Station",
                departureTime = departureTime,
                arrivalTime = computeArrivalTime(departureTime, 10),
                durationMinutes = 10,
                distanceKm = 1.8,
                fare = 35.0,
                isBookingSupported = true,
                bookingType = "AUTO_FIXED_FARE",
                intermediateStopsCount = 0,
                liveStatus = "ON_TIME",
                lineHexColor = "#EAB308"
            ),
            JourneySegment(
                id = "seg_bal_2",
                mode = TransportMode.METRO,
                operatorName = metroLineName,
                routeLabel = matchingMetroLine?.lineCode ?: "Purple Line",
                fromStation = "${origin.area} Metro Station",
                toStation = "${destination.area} Metro Station",
                departureTime = computeArrivalTime(departureTime, 13),
                arrivalTime = computeArrivalTime(departureTime, balancedDuration - 6),
                durationMinutes = (balancedDuration - 19).coerceAtLeast(14),
                distanceKm = distanceRound * 0.75,
                fare = matchingMetroLine?.baseFare ?: 35.0,
                isBookingSupported = true,
                bookingType = "DIGITAL_QR_TOKEN",
                intermediateStopsCount = 5,
                intermediateStopNames = listOf("Trinity", "Halasuru", "Indiranagar", "Swami Vivekananda Rd", "Baiyappanahalli"),
                liveStatus = "HIGH_FREQUENCY (every 4 min)",
                lineHexColor = "#9333EA"
            ),
            JourneySegment(
                id = "seg_bal_3",
                mode = TransportMode.WALK,
                operatorName = "Pedestrian Skywalk",
                routeLabel = "Direct Covered Walk",
                fromStation = "${destination.area} Metro Station Exit",
                toStation = destination.name,
                departureTime = computeArrivalTime(departureTime, balancedDuration - 6),
                arrivalTime = computeArrivalTime(departureTime, balancedDuration),
                durationMinutes = 6,
                distanceKm = 0.4,
                fare = 0.0,
                isBookingSupported = false,
                bookingType = "FREE",
                intermediateStopsCount = 0,
                walkingInstructions = "Take Exit 2 skywalk directly towards ${destination.name}",
                lineHexColor = "#64748B"
            )
        )

        val balancedOption = RouteOption(
            id = "route_balanced",
            preferenceType = RoutePreference.BEST_BALANCED,
            title = "Auto + $metroLineName",
            badgeTag = "Best Balanced",
            totalDurationMinutes = balancedDuration,
            totalFare = balancedFare,
            totalWalkMeters = 400,
            transfersCount = 1,
            totalDistanceKm = distanceRound,
            departureTime = departureTime,
            arrivalTime = computeArrivalTime(departureTime, balancedDuration),
            segments = balancedSegments,
            keyHighlights = listOf(
                "Avoids 80% surface traffic via dedicated Metro line",
                "Direct connection to ${destination.name}",
                "Short 400m covered pedestrian walkway",
                "Saves ₹180 compared to direct cab"
            ),
            carbonSavedKg = (distanceRound * 0.16).roundToInt().toDouble().coerceAtLeast(1.8),
            caloriesBurned = 50,
            reliabilityScorePercent = 96
        )

        // Route 2: FASTEST (Direct Cab)
        val cabDuration = (distanceRound * 2.1).roundToInt().coerceIn(20, 42)
        val cabFare = (60.0 + (distanceRound * 16.0)).roundToInt().toDouble()

        val fastestSegments = listOf(
            JourneySegment(
                id = "seg_fst_1",
                mode = TransportMode.CAB,
                operatorName = "Live Fleet Cab AC",
                routeLabel = "Door-to-Door Priority Ride",
                fromStation = origin.name,
                toStation = destination.name,
                departureTime = departureTime,
                arrivalTime = computeArrivalTime(departureTime, cabDuration),
                durationMinutes = cabDuration,
                distanceKm = distanceRound,
                fare = cabFare,
                isBookingSupported = true,
                bookingType = "CAB_INSTANT_BOOK",
                intermediateStopsCount = 0,
                liveStatus = "ON_TIME",
                lineHexColor = "#2563EB"
            )
        )

        val fastestOption = RouteOption(
            id = "route_fastest",
            preferenceType = RoutePreference.FASTEST,
            title = "Direct Priority Cab",
            badgeTag = "Fastest",
            totalDurationMinutes = cabDuration,
            totalFare = cabFare,
            totalWalkMeters = 30,
            transfersCount = 0,
            totalDistanceKm = distanceRound,
            departureTime = departureTime,
            arrivalTime = computeArrivalTime(departureTime, cabDuration),
            segments = fastestSegments,
            keyHighlights = listOf(
                "Shortest travel time: ${cabDuration} mins",
                "Zero transfers or walking required",
                "Door-to-door direct AC ride",
                "Earliest arrival time"
            ),
            carbonSavedKg = 0.4,
            caloriesBurned = 10,
            reliabilityScorePercent = 92
        )

        // Route 3: CHEAPEST (BMTC Bus)
        val busDuration = (distanceRound * 3.2).roundToInt().coerceIn(35, 68)
        val busFare = 30.0

        val cheapestSegments = listOf(
            JourneySegment(
                id = "seg_chp_1",
                mode = TransportMode.WALK,
                operatorName = "Walk to Stop",
                routeLabel = "Feeder Walk",
                fromStation = origin.name,
                toStation = "${origin.area} Main Bus Stand",
                departureTime = departureTime,
                arrivalTime = computeArrivalTime(departureTime, 6),
                durationMinutes = 6,
                distanceKm = 0.4,
                fare = 0.0,
                isBookingSupported = false,
                bookingType = "FREE",
                intermediateStopsCount = 0,
                lineHexColor = "#64748B"
            ),
            JourneySegment(
                id = "seg_chp_2",
                mode = TransportMode.BUS,
                operatorName = busLineName,
                routeLabel = "BMTC Electric AC",
                fromStation = "${origin.area} Main Bus Stand",
                toStation = "${destination.area} Bus Stop",
                departureTime = computeArrivalTime(departureTime, 8),
                arrivalTime = computeArrivalTime(departureTime, busDuration - 5),
                durationMinutes = (busDuration - 13).coerceAtLeast(20),
                distanceKm = distanceRound * 0.9,
                fare = busFare,
                isBookingSupported = true,
                bookingType = "BUS_DIGITAL_PASS",
                intermediateStopsCount = 7,
                intermediateStopNames = listOf("Stop 1", "Stop 2", "Stop 3", "Stop 4", "Stop 5", "Stop 6", "Stop 7"),
                liveStatus = "ON_TIME",
                lineHexColor = "#EA580C"
            ),
            JourneySegment(
                id = "seg_chp_3",
                mode = TransportMode.WALK,
                operatorName = "Final Walk",
                routeLabel = "Arrival Walk",
                fromStation = "${destination.area} Bus Stop",
                toStation = destination.name,
                departureTime = computeArrivalTime(departureTime, busDuration - 5),
                arrivalTime = computeArrivalTime(departureTime, busDuration),
                durationMinutes = 5,
                distanceKm = 0.3,
                fare = 0.0,
                isBookingSupported = false,
                bookingType = "FREE",
                intermediateStopsCount = 0,
                lineHexColor = "#64748B"
            )
        )

        val cheapestOption = RouteOption(
            id = "route_cheapest",
            preferenceType = RoutePreference.CHEAPEST,
            title = "$busLineName + Walk",
            badgeTag = "Cheapest",
            totalDurationMinutes = busDuration,
            totalFare = busFare,
            totalWalkMeters = 700,
            transfersCount = 0,
            totalDistanceKm = distanceRound,
            departureTime = departureTime,
            arrivalTime = computeArrivalTime(departureTime, busDuration),
            segments = cheapestSegments,
            keyHighlights = listOf(
                "Lowest out-of-pocket budget: Only ₹${busFare.toInt()}",
                "85% cheaper than direct cab",
                "High frequency clean electric buses",
                "Direct corridor routing"
            ),
            carbonSavedKg = (distanceRound * 0.22).roundToInt().toDouble().coerceAtLeast(2.4),
            caloriesBurned = 65,
            reliabilityScorePercent = 90
        )

        return listOf(balancedOption, fastestOption, cheapestOption)
    }

    private fun computeArrivalTime(departure: String, addMinutes: Int): String {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(departure) ?: Date()
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.MINUTE, addMinutes)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) {
            "09:15 AM"
        }
    }

    fun computeDepartureForArrival(arrivalTime: String, durationMinutes: Int): String {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(arrivalTime) ?: Date()
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.MINUTE, -durationMinutes)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) {
            "08:00 AM"
        }
    }

    private fun seedDemoBooking() {
        if (_locations.size < 2) return
        val origin = _locations[0]
        val destination = _locations[1]
        val sampleRoute = generateRoutesForLocations(origin, destination, "08:30 AM").first()
        val tickets = sampleRoute.segments.filter { it.isBookingSupported }.map { seg ->
            TransitTicket(
                ticketId = "TKT-${UUID.randomUUID().toString().take(8).uppercase()}",
                operatorName = seg.operatorName,
                mode = seg.mode,
                legName = "${seg.fromStation} → ${seg.toStation}",
                fare = seg.fare,
                qrPayload = "GOWEAVE:${seg.operatorName}:${seg.routeLabel}:PASS-${System.currentTimeMillis()}",
                pnr = "GW-${(100000..999999).random()}",
                gateAccessText = "Scan QR at entry gate or validator machine",
                validityWindow = "Valid today for slot ${seg.departureTime} – ${seg.arrivalTime}"
            )
        }

        _bookings.add(
            BookingRecord(
                bookingId = "GW-BK-98421",
                routeOption = sampleRoute,
                originName = origin.name,
                destinationName = destination.name,
                bookedAt = "Today, 08:15 AM",
                totalAmount = sampleRoute.totalFare,
                paymentMethod = "Unified Transit Pay",
                status = "CONFIRMED",
                tickets = tickets
            )
        )
    }

    fun completeBooking(routeOption: RouteOption, paymentMethod: String): BookingRecord {
        val now = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
        val bookingId = "GW-BK-${(10000..99999).random()}"

        val tickets = routeOption.segments.filter { it.isBookingSupported }.map { seg ->
            TransitTicket(
                ticketId = "TKT-${UUID.randomUUID().toString().take(8).uppercase()}",
                operatorName = seg.operatorName,
                mode = seg.mode,
                legName = "${seg.fromStation} → ${seg.toStation}",
                fare = seg.fare,
                qrPayload = "GOWEAVE:${seg.operatorName}:${seg.routeLabel}:${System.currentTimeMillis()}",
                pnr = "GW-${(100000..999999).random()}",
                gateAccessText = when (seg.mode) {
                    TransportMode.METRO -> "Scan at AFC Turnstile QR Reader"
                    TransportMode.BUS -> "Display to Bus Validator / Conductor"
                    TransportMode.AUTO -> "Share OTP with driver on boarding"
                    TransportMode.CAB -> "Share ride PIN with cab driver"
                    else -> "No pass required"
                },
                validityWindow = "Valid for booked slot ${seg.departureTime} – ${seg.arrivalTime}"
            )
        }

        val booking = BookingRecord(
            bookingId = bookingId,
            routeOption = routeOption,
            originName = routeOption.segments.firstOrNull()?.fromStation ?: "Origin",
            destinationName = routeOption.segments.lastOrNull()?.toStation ?: "Destination",
            bookedAt = now,
            totalAmount = routeOption.totalFare,
            paymentMethod = paymentMethod,
            status = "CONFIRMED",
            tickets = tickets
        )

        _bookings.add(0, booking)
        return booking
    }

    fun cancelBooking(bookingId: String): Boolean {
        val index = _bookings.indexOfFirst { it.bookingId == bookingId }
        if (index != -1) {
            val old = _bookings[index]
            _bookings[index] = old.copy(
                status = "CANCELLED",
                refundAmount = old.totalAmount
            )
            return true
        }
        return false
    }
}
