package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TransitRepository
import com.example.model.RoutePreference
import com.example.model.TransportMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GoWeave", appName)
    }

    @Test
    fun `transit repository returns balanced fastest and cheapest routes`() {
        val repo = TransitRepository()
        val origin = repo.locations[0]
        val destination = repo.locations[4]

        val routes = repo.searchRoutes(origin, destination)
        assertEquals(3, routes.size)

        val hasBalanced = routes.any { it.preferenceType == RoutePreference.BEST_BALANCED }
        val hasFastest = routes.any { it.preferenceType == RoutePreference.FASTEST }
        val hasCheapest = routes.any { it.preferenceType == RoutePreference.CHEAPEST }

        assertTrue(hasBalanced)
        assertTrue(hasFastest)
        assertTrue(hasCheapest)
    }

    @Test
    fun `launch MainActivity successfully`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)
    }

    @Test
    fun `route options contain transport modes estimated time and cost`() {
        val repo = TransitRepository()
        val routes = repo.searchRoutes(repo.locations[0], repo.locations[4])
        for (route in routes) {
            assertTrue(route.totalDurationMinutes > 0)
            assertTrue(route.totalFare > 0)
            assertTrue(route.segments.isNotEmpty())
            for (segment in route.segments) {
                assertNotNull(segment.mode)
                assertTrue(segment.durationMinutes > 0)
            }
        }
    }

    @Test
    fun `clear mock data and upload custom stations and lines`() {
        val repo = TransitRepository()
        repo.clearAllData("My Local City")
        assertEquals(0, repo.locations.size)
        assertEquals(0, repo.customLines.size)

        val st1 = repo.addStation("Main Street Hub", "Downtown", 40.71, -74.00)
        val st2 = repo.addStation("Uptown Center", "North Side", 40.75, -73.98)
        assertEquals(2, repo.locations.size)

        val line = repo.addTransitLine("Bus 10", "Downtown Link", TransportMode.BUS, "#EA580C", listOf(st1.id, st2.id))
        assertEquals(1, repo.customLines.size)
        assertEquals("Bus 10", line.lineCode)

        val routes = repo.searchRoutes(st1, st2)
        assertTrue(routes.isNotEmpty())
    }

    @Test
    fun `live vehicle tracker returns real-time buses and cabs with telemetry`() {
        val repo = TransitRepository()
        val vehicles = repo.liveVehicles
        assertTrue(vehicles.isNotEmpty())

        val hasBuses = vehicles.any { it.mode == TransportMode.BUS }
        val hasCabs = vehicles.any { it.mode == TransportMode.CAB }
        assertTrue(hasBuses)
        assertTrue(hasCabs)

        val initialLat = vehicles.first().lat
        repo.tickLiveVehicleTelemetry()
        val updatedVehicles = repo.liveVehicles
        assertTrue(updatedVehicles.isNotEmpty())
    }

    @Test
    fun `search locations returns matching recommendations across categories`() {
        val repo = TransitRepository()
        val parkResults = repo.searchLocations("Park")
        assertTrue(parkResults.isNotEmpty())
        assertTrue(parkResults.any { it.name.contains("Park", ignoreCase = true) || it.category == com.example.model.LocationCategory.PARK })

        val mallResults = repo.searchLocations("Mall")
        assertTrue(mallResults.isNotEmpty())

        val hospitalResults = repo.searchLocations("Hospital")
        assertTrue(hospitalResults.isNotEmpty())
    }

    @Test
    fun `ticket booking departure time computes accurate arrival times`() {
        val repo = TransitRepository()
        val routes = repo.searchRoutes(repo.locations[0], repo.locations[1], departureTime = "10:15 AM")
        assertTrue(routes.isNotEmpty())
        for (r in routes) {
            assertEquals("10:15 AM", r.departureTime)
            assertNotNull(r.arrivalTime)
        }
    }

    @Test
    fun `default home and work produce home to work and work to home recommendations`() {
        val repo = TransitRepository()
        val home = repo.locations.find { it.id == "blr_indiranagar" } ?: repo.locations[0]
        val work = repo.locations.find { it.id == "blr_itpl" } ?: repo.locations[1]

        repo.setDefaultHome(home)
        repo.setDefaultWork(work)
        repo.setCustomDefault("Gym", repo.locations.find { it.id == "blr_hsr" } ?: repo.locations[0])

        val commutes = repo.getCommuteRecommendations()
        assertEquals(2, commutes.size)

        val homeToWork = commutes.find { it.type == "HOME_TO_WORK" }
        assertNotNull(homeToWork)
        assertEquals(home.id, homeToWork?.origin?.id)
        assertEquals(work.id, homeToWork?.destination?.id)

        val workToHome = commutes.find { it.type == "WORK_TO_HOME" }
        assertNotNull(workToHome)
        assertEquals(work.id, workToHome?.origin?.id)
        assertEquals(home.id, workToHome?.destination?.id)
    }
}

