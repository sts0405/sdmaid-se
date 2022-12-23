package eu.darken.sdmse.systemcleaner.core.filter

import eu.darken.sdmse.common.areas.DataArea
import eu.darken.sdmse.common.areas.DataArea.Type
import eu.darken.sdmse.common.areas.DataAreaManager
import eu.darken.sdmse.common.areas.currentAreas
import eu.darken.sdmse.common.areas.hasFlags
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.files.core.APathLookup
import eu.darken.sdmse.common.files.core.FileType
import eu.darken.sdmse.common.files.core.GatewaySwitch
import eu.darken.sdmse.common.files.core.local.LocalPath
import eu.darken.sdmse.common.forensics.AreaInfo
import eu.darken.sdmse.common.forensics.FileForensics
import eu.darken.sdmse.common.pkgs.features.Installed
import eu.darken.sdmse.common.pkgs.pkgops.PkgOps
import eu.darken.sdmse.common.randomString
import eu.darken.sdmse.common.storage.StorageEnvironment
import eu.darken.sdmse.systemcleaner.core.SystemCleanerSettings
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest
import java.util.*

abstract class SystemCleanerFilterTest : BaseTest() {

    @MockK lateinit var pkgOps: PkgOps
    @MockK lateinit var areaManager: DataAreaManager
    @MockK lateinit var fileForensics: FileForensics
    @MockK lateinit var gatewaySwitch: GatewaySwitch
    @MockK lateinit var storageEnvironment: StorageEnvironment
    @MockK lateinit var systemCleanerSettings: SystemCleanerSettings


    private val storageData1 = mockk<DataArea>().apply {
        every { flags } returns setOf(DataArea.Flag.PRIMARY)
        every { type } returns Type.DATA
        every { path } returns LocalPath.build("/data")
    }

    private val storageData2 = mockk<DataArea>().apply {
        every { flags } returns setOf(DataArea.Flag.SECONDARY)
        every { type } returns Type.DATA
        every { path } returns LocalPath.build("/mnt/expand", randomString())
    }

    private val storageDataApp1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.APP_APP
        every { path } returns storageData1.path.child("app")
    }

    private val storageDataApp2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.APP_APP
        every { path } returns storageData2.path.child("app")
    }

    private val storageDataAppAsec1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.APP_ASEC
        every { path } returns storageData1.path.child("app-asec")
    }

    private val storageDataAppAsec2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.APP_ASEC
        every { path } returns storageData2.path.child("app-asec")
    }

    private val storageDataAppPrivate1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.APP_APP_PRIVATE
        every { path } returns storageData1.path.child("app-private")
    }

    private val storageDataAppPrivate2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.APP_APP_PRIVATE
        every { path } returns storageData2.path.child("app-private")
    }

    private val storageDataAppLib1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.APP_LIB
        every { path } returns storageData1.path.child("app-lib")
    }

    private val storageDataAppLib2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.APP_LIB
        every { path } returns storageData2.path.child("app-lib")
    }

    private val storageDataSystem1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.DATA_SYSTEM
        every { path } returns storageData1.path.child("system")
    }

    private val storageDataSystem2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.DATA_SYSTEM
        every { path } returns storageData2.path.child("system")
    }

    private val storageDataSystemCE1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.DATA_SYSTEM_CE
        every { path } returns storageData1.path.child("system_ce")
    }

    private val storageDataSystemCE2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.DATA_SYSTEM_CE
        every { path } returns storageData2.path.child("system_ce")
    }

    private val storageDataSystemDE1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.DATA_SYSTEM_DE
        every { path } returns storageData1.path.child("system_de")
    }

    private val storageDataSystemDE2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.DATA_SYSTEM_DE
        every { path } returns storageData2.path.child("system_de")
    }

    private val storageDalvikDex1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.DALVIK_DEX
        every { path } returns storageData1.path.child("dalvik-cache", "arm64")
    }

    private val storageDalvikDex2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.DALVIK_DEX
        every { path } returns storageData2.path.child("dalvik-cache", "arm64")
    }

    private val storageDalvikProfile1 = mockk<DataArea>().apply {
        every { flags } returns storageData1.flags
        every { type } returns Type.DALVIK_PROFILE
        every { path } returns storageData1.path.child("dalvik-cache", "profiles")
    }

    private val storageDalvikProfile2 = mockk<DataArea>().apply {
        every { flags } returns storageData2.flags
        every { type } returns Type.DALVIK_PROFILE
        every { path } returns storageData2.path.child("dalvik-cache", "profiles")
    }

    private val storageSdcard1 = mockk<DataArea>().apply {
        every { flags } returns setOf(DataArea.Flag.PRIMARY)
        every { type } returns Type.SDCARD
        every { path } returns LocalPath.build("storage", "emulated", "0")
    }

    private val storageSdcard2 = mockk<DataArea>().apply {
        every { flags } returns setOf(DataArea.Flag.SECONDARY)
        every { type } returns Type.SDCARD
        every { path } returns LocalPath.build("/storage", "ABCD-EFGH")
    }

    private val storageAndroidData1 = mockk<DataArea>().apply {
        every { flags } returns storageSdcard1.flags
        every { type } returns Type.PUBLIC_DATA
        every { path } returns storageSdcard1.path.child("Android", "data")
    }

    private val storageAndroidData2 = mockk<DataArea>().apply {
        every { flags } returns storageSdcard2.flags
        every { type } returns Type.PUBLIC_DATA
        every { path } returns storageSdcard2.path.child("Android", "data")
    }

    private val storageAndroidObb1 = mockk<DataArea>().apply {
        every { flags } returns storageSdcard1.flags
        every { type } returns Type.PUBLIC_OBB
        every { path } returns storageSdcard1.path.child("Android", "obb")
    }

    private val storageAndroidObb2 = mockk<DataArea>().apply {
        every { flags } returns storageSdcard2.flags
        every { type } returns Type.PUBLIC_OBB
        every { path } returns storageSdcard2.path.child("Android", "obb")
    }

    private val storageAndroidMedia1 = mockk<DataArea>().apply {
        every { flags } returns storageSdcard1.flags
        every { type } returns Type.PUBLIC_MEDIA
        every { path } returns storageSdcard1.path.child("Android", "media")
    }

    private val storageAndroidMedia2 = mockk<DataArea>().apply {
        every { flags } returns storageSdcard2.flags
        every { type } returns Type.PUBLIC_MEDIA
        every { path } returns storageSdcard2.path.child("Android", "media")
    }

    private val pkgs = mutableSetOf<Installed>()

    @BeforeEach
    open fun setup() {
        if (!::pkgOps.isInitialized) {
            MockKAnnotations.init(this)
        }

        every { areaManager.state } returns flowOf(
            DataAreaManager.State(
                areas = setOf(
                    storageData1,
                    storageData2,
                    storageDataApp1,
                    storageDataApp2,
                    storageDataAppAsec1,
                    storageDataAppAsec2,
                    storageDataAppPrivate1,
                    storageDataAppPrivate2,
                    storageDataAppLib1,
                    storageDataAppLib2,
                    storageDataSystem1,
                    storageDataSystem2,
                    storageDataSystemCE1,
                    storageDataSystemCE2,
                    storageDataSystemDE1,
                    storageDataSystemDE2,
                    storageDalvikDex1,
                    storageDalvikDex2,
                    storageDalvikProfile1,
                    storageDalvikProfile2,
                    storageSdcard1,
                    storageSdcard2,
                    storageAndroidData1,
                    storageAndroidData2,
                    storageAndroidObb1,
                    storageAndroidObb2,
                    storageAndroidMedia1,
                    storageAndroidMedia2,
                )
            )
        )

        coEvery { gatewaySwitch.listFiles(any()) } returns emptyList()
        coEvery { gatewaySwitch.exists(any()) } returns false
        every { storageEnvironment.dataDir } returns LocalPath.build("/data")

        coEvery { pkgOps.viewArchive(any(), any()) } returns null
    }

    @AfterEach
    open fun teardown() {
        pkgs.clear()
    }

    val positives = mutableListOf<APathLookup<*>>()
    val negatives = mutableListOf<APathLookup<*>>()

    suspend fun mockDefaults() {
        mockNegative(Type.SDCARD, UUID.randomUUID().toString(), Flags.FILE)
        mockNegative(Type.SDCARD, UUID.randomUUID().toString(), Flags.DIR)
        mockNegative(Type.SDCARD, "DCIM", Flags.DIR)
        mockNegative(Type.SDCARD, "DCIM/Camera", Flags.DIR)
        mockNegative(Type.SDCARD, "Android", Flags.DIR)
        mockNegative(Type.SDCARD, "Android/data", Flags.DIR)
        mockNegative(Type.SDCARD, "Photos", Flags.DIR)
        mockNegative(Type.SDCARD, "Pictures", Flags.DIR)
        mockNegative(Type.SDCARD, "Camera", Flags.DIR)
        mockNegative(Type.SDCARD, "Music", Flags.DIR)
//        mockNegative(DataArea.Type.SDCARD, null, Flags.DIR)
//        if (rootContext!!.isRooted) {
//            mockNegative(DataArea.Type.SDCARD, null, Flags.DIR)
//            mockNegative(DataArea.Type.PRIVATE_DATA, null, Flags.DIR)
//            mockNegative(DataArea.Type.DATA, null, Flags.DIR)
//            mockNegative(DataArea.Type.SYSTEM_APP, null, Flags.DIR)
//            mockNegative(DataArea.Type.SYSTEM, null, Flags.DIR)
//            mockNegative(DataArea.Type.DOWNLOAD_CACHE, null, Flags.DIR)
//        }
    }

    suspend fun confirm(filter: SystemCleanerFilter) {
        positives.isNotEmpty() shouldBe true
        negatives.isNotEmpty() shouldBe true

        filter.initialize()

        positives.forEach { canidate ->
            withClue("Should match ${canidate.path}") {
                filter.sieve(canidate) shouldBe true
            }
            log { "Matched: ${canidate.path}" }
        }
        negatives.forEach { canidate ->
            withClue("Should NOT match ${canidate.path}") {
                filter.sieve(canidate) shouldBe false
            }
            log { "Didn't match: ${canidate.path}" }
        }
    }

    enum class Flags {
        FILE, DIR, EMPTY, ONLY_PRIMARY, ONLY_SECONDARY
    }

    suspend fun mockPositive(location: Type, path: String, vararg flags: Flags) {
        val mockedFiles = doMock(location, path, null, *flags)
        positives.addAll(mockedFiles)
    }

    suspend fun mockNegative(location: Type, path: String, vararg flags: Flags) {
        val mockedFiles = doMock(location, path, null, *flags)
        negatives.addAll(mockedFiles)
    }

    suspend fun doMock(
        areaType: Type,
        targetPath: String,
        callback: ((APathLookup<*>) -> Unit)?,
        vararg flags: Flags
    ): Collection<APathLookup<*>> {
        val flagsCollection = listOf(*flags)

        require(areaManager.currentAreas().any { it.type == areaType }) { "Area not mocked: $areaType" }

        return areaManager
            .currentAreas()
            .filter { it.type == areaType }
            .mapNotNull { area ->
                if (flagsCollection.contains(Flags.ONLY_PRIMARY) && !area.hasFlags(DataArea.Flag.PRIMARY)) {
                    return@mapNotNull null
                }
                if (flagsCollection.contains(Flags.ONLY_SECONDARY) && area.hasFlags(DataArea.Flag.PRIMARY)) {
                    return@mapNotNull null
                }
                require(!(flagsCollection.contains(Flags.DIR) && flagsCollection.contains(Flags.FILE))) { "Can't be both file and dir." }

                val mockPath = area.path.child(targetPath)
                mockk<APathLookup<*>>()
                    .apply {
                        callback?.invoke(this)
                        every { path } returns mockPath.path
                        every { name } returns mockPath.name

                        if (flagsCollection.contains(Flags.DIR)) {
                            every { fileType } returns FileType.DIRECTORY
                        } else if (flagsCollection.contains(Flags.FILE)) {
                            every { fileType } returns FileType.FILE
                        }

                        every { size } returns if (flagsCollection.contains(Flags.EMPTY)) 0L else 1024 * 1024L
                    }
                    .also {
                        coEvery { fileForensics.identifyArea(it) } returns mockk<AreaInfo>().apply {
                            every { type } returns areaType
                        }
                    }
            }
    }

}