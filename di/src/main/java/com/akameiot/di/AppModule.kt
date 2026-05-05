package com.akameiot.di

import com.akameiot.data.remote.CognitoAuthRemoteDataSource
import com.akameiot.data.repository_impl.AuthRepositoryImpl
import com.akameiot.data.session.CognitoAuthSessionManager
import com.akameiot.domain.session.AuthSessionManager
import com.akameiot.domain.usecase.RegisterUseCase
import com.akameiot.domain.validation.PasswordValidator
import com.akameiot.domain.usecase.ConfirmSignUpUseCase
import com.akameiot.domain.usecase.AutoLoginUseCase
import com.akameiot.domain.usecase.CheckAuthSessionUseCase
import com.akameiot.domain.usecase.ConfirmResetPasswordUseCase
import com.akameiot.domain.usecase.ResendConfirmationCodeUseCase
import com.akameiot.domain.usecase.StartResetPasswordUseCase
import com.akameiot.data.repository_impl.DeviceRepositoryImpl
import com.akameiot.domain.repository.DeviceRepository
import com.akameiot.domain.usecase.ActivateDeviceUseCase
import com.akameiot.data.remote.NetworkModule
import com.akameiot.data.repository_impl.ConsumeTokenUseCaseImpl
import com.akameiot.data.repository_impl.GeneratePairingTokenUseCaseImpl
import com.akameiot.domain.usecase.ConsumeTokenUseCase
import com.akameiot.domain.usecase.GeneratePairingTokenUseCase
import com.akameiot.domain.usecase.GetAppUserUseCase
import android.content.Context
import com.akameiot.data.session.SessionDataStore
import com.akameiot.data.fcm.FcmTokenProvider
import com.akameiot.data.local.db.DatabaseProvider
import com.akameiot.data.repository_impl.SnsRepositoryImpl
import com.akameiot.data.session.DeviceNetworkStore
import com.akameiot.domain.repository.SnsRepository
import com.akameiot.domain.usecase.SubscribeToDeviceTopicUseCase
import com.akameiot.data.network.NetworkManager
import com.akameiot.data.repository.TelemetryAggRepositoryImpl
import com.akameiot.data.repository.TelemetryRepository
import com.akameiot.data.session.FcmTokenStore
import com.akameiot.data.repository_impl.SessionRepositoryImpl
import com.akameiot.data.repository_impl.SyncRecentTelemetryUseCaseImpl
import com.akameiot.data.repository_impl.TelemetryWindowRepositoryImpl
import com.akameiot.data.session.FilterPreferencesStore
import com.akameiot.data.session.GlobalTimeStore
import com.akameiot.data.session.MeshUpdateWindowStore
import com.akameiot.data.session.ThemePreferencesStore
import com.akameiot.domain.repository.SessionRepository
import com.akameiot.domain.repository.TelemetryAggRepository
import com.akameiot.domain.usecase.AggregateInsertUseCase
import com.akameiot.domain.usecase.CalculateMeshWindowUseCase
import com.akameiot.domain.usecase.ChartPointsUseCase
import com.akameiot.domain.usecase.CheckLocalSessionUseCase
import com.akameiot.domain.usecase.PropagateAggBucketsUseCase
import com.akameiot.domain.usecase.SyncRecentTelemetryUseCase
import com.akameiot.domain.usecase.SyncUserDevicesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.Channel

object AppModule {

    lateinit var appContext: Context
        private set
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val cognitoRemote by lazy {
        CognitoAuthRemoteDataSource()
    }

    private val sessionDataStore by lazy {
        SessionDataStore(appContext)
    }

    private val deviceNetworkStore by lazy {
        DeviceNetworkStore(appContext)
    }

    private val fcmTokenStore by lazy {
        FcmTokenStore(appContext)
    }

    val tokenStore by lazy {
        fcmTokenStore
    }

    val networkStore by lazy {
        deviceNetworkStore
    }

    val authRepository by lazy {
        AuthRepositoryImpl(cognitoRemote,
            authSessionManager)
    }

    val checkLocalSessionUseCase by lazy {
        CheckLocalSessionUseCase(authRepository)
    }

    val registerUseCase by lazy {
        RegisterUseCase(authRepository)
    }

    val authSessionManager: AuthSessionManager by lazy {
        CognitoAuthSessionManager(sessionDataStore)
    }

    val passwordValidator: PasswordValidator by lazy {
        PasswordValidator()
    }

    val confirmSignUpUseCase by lazy {
        ConfirmSignUpUseCase(authRepository)
    }

    val autoLoginUseCase by lazy {
        AutoLoginUseCase(authRepository)
    }

    val checkAuthSessionUseCase by lazy {
        CheckAuthSessionUseCase(authRepository)
    }

    val resendConfirmationCodeUseCase by lazy {
        ResendConfirmationCodeUseCase(authRepository)
    }

    val startResetPasswordUseCase by lazy {
        StartResetPasswordUseCase(authRepository)
    }

    val confirmResetPasswordUseCase by lazy {
        ConfirmResetPasswordUseCase(authRepository)
    }

    private val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(NetworkModule.deviceApi)
    }

    val activateDeviceUseCase by lazy {
        ActivateDeviceUseCase(deviceRepository)
    }

    val consumeTokenUseCase: ConsumeTokenUseCase by lazy {
        ConsumeTokenUseCaseImpl(NetworkModule.pairingPublicApi)
    }

    val generatePairingTokenUseCase: GeneratePairingTokenUseCase by lazy {
        GeneratePairingTokenUseCaseImpl(NetworkModule.pairingPrivateApi)
    }

    val getAppUserUseCase by lazy {
        GetAppUserUseCase(authSessionManager)
    }

    val fcmTokenProvider by lazy {
        FcmTokenProvider()
    }

    private val snsRepository: SnsRepository by lazy {
        SnsRepositoryImpl(NetworkModule.snsApi)
    }

    val subscribeToDeviceTopicUseCase by lazy {
        SubscribeToDeviceTopicUseCase(snsRepository)
    }

    val networkManager by lazy {
        NetworkManager(
            networkStore = deviceNetworkStore,
            subscribeToDeviceTopicUseCase = subscribeToDeviceTopicUseCase,
            fcmTokenProvider = fcmTokenProvider
        )
    }

    private val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(NetworkModule.sessionApi)
    }

    val syncUserDevicesUseCase by lazy {
        SyncUserDevicesUseCase(
            sessionRepository = sessionRepository,
            networkStore      = deviceNetworkStore,
        )
    }

    val telemetryDao by lazy {
        DatabaseProvider.getDatabase(appContext).telemetryDao()
    }

    val nodeLimitDao by lazy {
        DatabaseProvider.getDatabase(appContext).nodeLimitDao()
    }

    val nodeLimitRepository: com.akameiot.domain.repository.NodeLimitRepository by lazy {
        com.akameiot.data.repository_impl.NodeLimitRepositoryImpl(
            dao         = nodeLimitDao,
            api         = NetworkModule.nodeLimitApi,
            authSession = authSessionManager,
        )
    }

    val calculateIndexUseCase by lazy {
        com.akameiot.domain.usecase.CalculateIndexUseCase(nodeLimitRepository)
    }

    val telemetryRepository by lazy {
        TelemetryRepository(
            dao                        = telemetryDao,
            api                        = NetworkModule.telemetryApi,
            aggregateInsertUseCase     = aggregateInsertUseCase,
            propagateAggBucketsUseCase = propagateAggBucketsUseCase,
            calculateIndexUseCase      = calculateIndexUseCase,
        )
    }

    val syncRecentTelemetryUseCase: SyncRecentTelemetryUseCase by lazy {
        SyncRecentTelemetryUseCaseImpl(
            repository = telemetryRepository,
            authSessionManager = authSessionManager,
            networkStore = deviceNetworkStore
        )
    }

    val filterPreferencesStore by lazy {
        FilterPreferencesStore(appContext)
    }

    val meshWindowStore by lazy {
        MeshUpdateWindowStore(appContext) }

    val telemetryWindowRepository by lazy {
        TelemetryWindowRepositoryImpl(telemetryDao)
    }

    val calculateMeshWindowUseCase by lazy {
        CalculateMeshWindowUseCase(
            telemetryWindowRepository = telemetryWindowRepository,
            meshWindowRepository = meshWindowStore
        )
    }

    val telemetryAggRepository: TelemetryAggRepository by lazy {
        TelemetryAggRepositoryImpl(telemetryDao)
    }

    val aggregateInsertUseCase: AggregateInsertUseCase by lazy {
        AggregateInsertUseCase(telemetryAggRepository)
    }

    val chartPointsUseCase: ChartPointsUseCase by lazy {
        ChartPointsUseCase(telemetryRepository, telemetryAggRepository)
    }

    val propagateAggBucketsUseCase: PropagateAggBucketsUseCase by lazy {
        PropagateAggBucketsUseCase(telemetryAggRepository)
    }

    val globalTimeStore by lazy {
        GlobalTimeStore(appContext)
    }

    val themeStore by lazy {
        ThemePreferencesStore(appContext)
    }

    val lastSeenPerMesh = MutableStateFlow<Map<String, Long>>(emptyMap())
    enum class NetworkStatus { ALL_ONLINE, PARTIAL, ALL_OFFLINE }
    val networkStatusFlow = MutableStateFlow(NetworkStatus.ALL_ONLINE)
    val freshnessWakeUp = Channel<Unit>(Channel.CONFLATED)

}