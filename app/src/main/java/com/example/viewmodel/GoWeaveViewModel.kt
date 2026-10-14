package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiTransitService
import com.example.data.TransitRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GoWeaveUiState(
    val serviceAreaName: String = "Bengaluru City & Metropolitan Area",
    val locations: List<TransitLocation> = emptyList(),
    val customLines: List<CustomTransitLine> = emptyList(),
    val liveVehicles: List<LiveVehicle> = emptyList(),
    val defaultHome: TransitLocation,
    val defaultWork: TransitLocation,
    val commuteRecommendations: List<CommuteRideRecommendation> = emptyList(),
    val origin: TransitLocation,
    val destination: TransitLocation,
    val travelDate: String = "Today",
    val timingType: TimingType = TimingType.DEPART_AT,
    val departureTime: String = "08:30 AM",
    val desiredArrivalTime: String = "09:15 AM",
    val selectedPreference: RoutePreference = RoutePreference.BEST_BALANCED,
    val budgetFilter: Double? = null,
    val routes: List<RouteOption> = emptyList(),
    val selectedRoute: RouteOption? = null,
    val isDisruptionSimulated: Boolean = false,
    val isSearching: Boolean = false,
    val bookings: List<BookingRecord> = emptyList(),
    val activeConfirmedBooking: BookingRecord? = null,
    val savedPlaces: List<SavedPlace> = emptyList(),
    val dailyCommutes: List<DailyCommutePlan> = emptyList(),
    val operatorStatuses: List<OperatorStatus> = emptyList(),
    val aiMessages: List<AiCommuteMessage> = emptyList(),
    val isAiLoading: Boolean = false,
    val isLiveNavActive: Boolean = false,
    val activeNavSegmentIndex: Int = 0,
    val toastMessage: String? = null
)

class GoWeaveViewModel(
    private val repository: TransitRepository = TransitRepository(),
    private val aiService: GeminiTransitService = GeminiTransitService()
) : ViewModel() {

    private val home = repository.defaultHomeLocation
    private val work = repository.defaultWorkLocation

    private val _uiState = MutableStateFlow(
        GoWeaveUiState(
            serviceAreaName = repository.currentServiceAreaName,
            locations = repository.locations,
            customLines = repository.customLines,
            liveVehicles = repository.liveVehicles,
            defaultHome = home,
            defaultWork = work,
            commuteRecommendations = repository.getCommuteRecommendations(),
            origin = home,
            destination = work,
            savedPlaces = repository.savedPlaces,
            dailyCommutes = repository.dailyCommutes,
            operatorStatuses = repository.operatorStatuses,
            bookings = repository.bookings
        )
    )
    val uiState: StateFlow<GoWeaveUiState> = _uiState.asStateFlow()

    val allLocations: List<TransitLocation> get() = repository.locations

    init {
        searchRoutes()
        initAiWelcome()
        startLiveVehicleTelemetryStream()
    }

    private fun startLiveVehicleTelemetryStream() {
        viewModelScope.launch {
            while (isActive) {
                delay(3000)
                repository.tickLiveVehicleTelemetry()
                _uiState.value = _uiState.value.copy(
                    liveVehicles = repository.liveVehicles
                )
            }
        }
    }

    private fun initAiWelcome() {
        val welcome = AiCommuteMessage(
            id = "ai_welcome",
            isUser = false,
            text = "👋 Hello! I am your **GoWeave Commute Companion**.\n\nI can help you navigate Bangalore's metro lines, buses, autos, and cabs with live travel advice. You can also explore famous places like Cubbon Park, Phoenix Marketcity, Manipal Hospital, or set your default Home and Work commute!",
            timestamp = "Just now",
            suggestedPromptOptions = listOf(
                "Why is Best Balanced recommended?",
                "How to reach Cubbon Park?",
                "What is the best route to ITPL?"
            )
        )
        _uiState.value = _uiState.value.copy(aiMessages = listOf(welcome))
    }

    // --- Search Engine with Autocomplete ---

    fun searchLocations(query: String, category: LocationCategory? = null): List<TransitLocation> {
        return repository.searchLocations(query, category)
    }

    // --- Default Home & Work Management ---

    fun setDefaultHomeLocation(location: TransitLocation) {
        repository.setDefaultHome(location)
        _uiState.value = _uiState.value.copy(
            defaultHome = location,
            commuteRecommendations = repository.getCommuteRecommendations(),
            toastMessage = "Set ${location.name} as default Home!"
        )
    }

    fun setDefaultWorkLocation(location: TransitLocation) {
        repository.setDefaultWork(location)
        _uiState.value = _uiState.value.copy(
            defaultWork = location,
            commuteRecommendations = repository.getCommuteRecommendations(),
            toastMessage = "Set ${location.name} as default Work!"
        )
    }

    fun planHomeToWorkCommute() {
        val home = repository.defaultHomeLocation
        val work = repository.defaultWorkLocation
        _uiState.value = _uiState.value.copy(
            origin = home,
            destination = work,
            departureTime = "08:30 AM",
            selectedPreference = RoutePreference.BEST_BALANCED
        )
        searchRoutes()
    }

    fun planWorkToHomeCommute() {
        val home = repository.defaultHomeLocation
        val work = repository.defaultWorkLocation
        _uiState.value = _uiState.value.copy(
            origin = work,
            destination = home,
            departureTime = "06:15 PM",
            selectedPreference = RoutePreference.BEST_BALANCED
        )
        searchRoutes()
    }

    fun setCustomDefaultLocation(label: String, location: TransitLocation) {
        repository.setCustomDefault(label, location)
        _uiState.value = _uiState.value.copy(
            toastMessage = "Saved $label as ${location.name}!"
        )
    }

    fun clearMockTransportData(areaName: String = "My Local City") {
        repository.clearAllData(areaName)
        _uiState.value = _uiState.value.copy(
            serviceAreaName = areaName,
            locations = emptyList(),
            customLines = emptyList(),
            liveVehicles = emptyList(),
            routes = emptyList(),
            selectedRoute = null,
            commuteRecommendations = emptyList(),
            toastMessage = "Mock transport data wiped clean. Ready for your local area!"
        )
    }

    fun loadStarterLocalTemplate(areaName: String = "My City District") {
        repository.loadStarterLocalTemplate(areaName)
        _uiState.value = _uiState.value.copy(
            serviceAreaName = repository.currentServiceAreaName,
            locations = repository.locations,
            customLines = repository.customLines,
            liveVehicles = repository.liveVehicles,
            defaultHome = repository.defaultHomeLocation,
            defaultWork = repository.defaultWorkLocation,
            origin = repository.defaultHomeLocation,
            destination = repository.defaultWorkLocation,
            commuteRecommendations = repository.getCommuteRecommendations(),
            toastMessage = "Loaded starter local transit network!"
        )
        searchRoutes()
    }

    fun importFromBatchText(batchText: String): Pair<Int, Int> {
        val result = repository.importFromBatchText(batchText)
        _uiState.value = _uiState.value.copy(
            locations = repository.locations,
            customLines = repository.customLines,
            liveVehicles = repository.liveVehicles,
            defaultHome = repository.defaultHomeLocation,
            defaultWork = repository.defaultWorkLocation,
            commuteRecommendations = repository.getCommuteRecommendations(),
            toastMessage = "Imported ${result.first} stops and ${result.second} transit lines!"
        )
        searchRoutes()
        return result
    }

    fun addNewStation(name: String, area: String, landmark: String) {
        val newStation = repository.addStation(name = name, area = area, landmark = landmark)
        _uiState.value = _uiState.value.copy(
            locations = repository.locations,
            toastMessage = "Added stop '${newStation.name}'!"
        )
    }

    fun removeStation(stationId: String) {
        repository.removeStation(stationId)
        _uiState.value = _uiState.value.copy(
            locations = repository.locations,
            toastMessage = "Removed station."
        )
    }

    fun addNewLine(code: String, name: String, mode: TransportMode, fare: Double) {
        val stationIds = repository.locations.take(4).map { it.id }
        repository.addTransitLine(
            lineCode = code,
            lineName = name,
            mode = mode,
            colorHex = if (mode == TransportMode.METRO) "#9333EA" else "#EA580C",
            stationIds = stationIds,
            baseFare = fare
        )
        _uiState.value = _uiState.value.copy(
            customLines = repository.customLines,
            toastMessage = "Added ${mode.displayName} line '$code'!"
        )
    }

    // --- Flexible Travel Date and Time Selection ---

    fun setDepartureTime(time: String) {
        _uiState.value = _uiState.value.copy(
            timingType = TimingType.DEPART_AT,
            departureTime = time
        )
        searchRoutes()
    }

    fun setTravelTiming(date: String, type: TimingType, time: String) {
        val calcDeparture = if (type == TimingType.ARRIVE_BY) {
            repository.computeDepartureForArrival(time, 42)
        } else time

        _uiState.value = _uiState.value.copy(
            travelDate = date,
            timingType = type,
            departureTime = calcDeparture,
            desiredArrivalTime = time,
            toastMessage = "Updated to $date • ${type.label} $time"
        )
        searchRoutes()
    }

    fun setLeaveNow() {
        val now = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        _uiState.value = _uiState.value.copy(
            travelDate = "Today",
            timingType = TimingType.LEAVE_NOW,
            departureTime = now,
            toastMessage = "Set to Leave Now ($now)"
        )
        searchRoutes()
    }

    fun saveCustomPlace(title: String, location: TransitLocation, category: String) {
        val newSaved = SavedPlace(
            id = "sp_${System.currentTimeMillis()}",
            title = title,
            address = location.fullAddress.ifBlank { location.area },
            location = location,
            category = category
        )
        val updated = _uiState.value.savedPlaces.toMutableList()
        updated.add(0, newSaved)
        _uiState.value = _uiState.value.copy(
            savedPlaces = updated,
            toastMessage = "Saved '$title' to your favourite places!"
        )
    }

    fun deleteSavedPlace(id: String) {
        val updated = _uiState.value.savedPlaces.filterNot { it.id == id }
        _uiState.value = _uiState.value.copy(
            savedPlaces = updated,
            toastMessage = "Removed saved place."
        )
    }

    fun refreshCommuteRecommendations() {
        val recs = repository.getCommuteRecommendations()
        _uiState.value = _uiState.value.copy(
            commuteRecommendations = recs,
            toastMessage = "Refreshed live commute recommendations with current transit status!"
        )
    }

    // --- Search & Route Actions ---

    fun setOrigin(location: TransitLocation) {
        _uiState.value = _uiState.value.copy(origin = location)
        searchRoutes()
    }

    fun setDestination(location: TransitLocation) {
        _uiState.value = _uiState.value.copy(destination = location)
        searchRoutes()
    }

    fun swapOriginDestination() {
        val current = _uiState.value
        _uiState.value = current.copy(
            origin = current.destination,
            destination = current.origin
        )
        searchRoutes()
    }

    fun setPreference(pref: RoutePreference) {
        _uiState.value = _uiState.value.copy(selectedPreference = pref)
        val matching = _uiState.value.routes.find { it.preferenceType == pref }
        if (matching != null) {
            _uiState.value = _uiState.value.copy(selectedRoute = matching)
        }
    }

    fun setBudgetFilter(budget: Double?) {
        _uiState.value = _uiState.value.copy(budgetFilter = budget)
        searchRoutes()
    }

    fun searchRoutes() {
        val current = _uiState.value
        if (repository.locations.isEmpty()) {
            _uiState.value = current.copy(routes = emptyList(), selectedRoute = null)
            return
        }

        val result = repository.searchRoutes(
            origin = current.origin,
            destination = current.destination,
            departureTime = current.departureTime,
            budgetMax = current.budgetFilter,
            isDisruptionActive = current.isDisruptionSimulated
        )
        val selected = result.find { it.preferenceType == current.selectedPreference } ?: result.firstOrNull()

        _uiState.value = current.copy(
            routes = result,
            selectedRoute = selected
        )
    }

    fun selectRoute(route: RouteOption) {
        _uiState.value = _uiState.value.copy(
            selectedRoute = route,
            selectedPreference = route.preferenceType
        )
    }

    fun toggleDisruptionSimulation() {
        val newState = !_uiState.value.isDisruptionSimulated
        _uiState.value = _uiState.value.copy(isDisruptionSimulated = newState)
        searchRoutes()
    }

    fun completeUnifiedCheckout(paymentMethod: String, onComplete: () -> Unit) {
        val currentRoute = _uiState.value.selectedRoute ?: return
        val newBooking = repository.completeBooking(currentRoute, paymentMethod)

        _uiState.value = _uiState.value.copy(
            bookings = repository.bookings,
            activeConfirmedBooking = newBooking,
            toastMessage = "Booking confirmed for slot ${currentRoute.departureTime}! Passes generated."
        )
        onComplete()
    }

    fun cancelBooking(bookingId: String) {
        repository.cancelBooking(bookingId)
        _uiState.value = _uiState.value.copy(
            bookings = repository.bookings,
            toastMessage = "Booking cancelled. Refund processed to original payment method."
        )
    }

    fun askAi(prompt: String) {
        val currentMessages = _uiState.value.aiMessages.toMutableList()
        val userMsg = AiCommuteMessage(
            id = System.currentTimeMillis().toString(),
            isUser = true,
            text = prompt,
            timestamp = "Just now"
        )
        currentMessages.add(userMsg)
        _uiState.value = _uiState.value.copy(
            aiMessages = currentMessages,
            isAiLoading = true
        )

        viewModelScope.launch {
            val response = aiService.consultTransitAi(prompt, _uiState.value.selectedRoute)
            val updated = _uiState.value.aiMessages.toMutableList()
            updated.add(response)
            _uiState.value = _uiState.value.copy(
                aiMessages = updated,
                isAiLoading = false
            )
        }
    }

    fun startNavigation() {
        _uiState.value = _uiState.value.copy(
            isLiveNavActive = true,
            activeNavSegmentIndex = 0
        )
    }

    fun stopNavigation() {
        _uiState.value = _uiState.value.copy(isLiveNavActive = false)
    }

    fun nextNavSegment() {
        val current = _uiState.value
        val route = current.selectedRoute ?: return
        if (current.activeNavSegmentIndex < route.segments.size - 1) {
            _uiState.value = current.copy(activeNavSegmentIndex = current.activeNavSegmentIndex + 1)
        } else {
            _uiState.value = current.copy(isLiveNavActive = false)
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
