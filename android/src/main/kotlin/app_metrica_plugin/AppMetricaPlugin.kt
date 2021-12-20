package app_metrica_plugin

import androidx.annotation.RequiresApi
import android.os.Build
import android.util.Log
import android.app.Application
import android.content.Context
import androidx.annotation.NonNull
import com.yandex.metrica.Revenue
import java.util.function.Consumer
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.yandex.metrica.ecommerce.*
import com.yandex.metrica.profile.Attribute
import com.yandex.metrica.profile.UserProfile
import java.util.*
import java.util.Map

/**
 * AppmetricaSdkPlugin
 */
class AppMetricaPlugin : MethodCallHandler, FlutterPlugin {
  private var methodChannel: MethodChannel? = null
  private var context: Context? = null
  private var application: Application? = null

  @Override
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    onAttachedToEngine(
            flutterPluginBinding.getApplicationContext(),
            flutterPluginBinding.getBinaryMessenger()
    )
  }

  private fun onAttachedToEngine(applicationContext: Context, binaryMessenger: BinaryMessenger) {
    application = applicationContext as Application
    context = applicationContext
    methodChannel = MethodChannel(binaryMessenger, "app_metrica_plugin")
    methodChannel!!.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel?.setMethodCallHandler(null)
    methodChannel = null
    context = null
    application = null
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "activate" -> handleActivate(call, result)
      "reportEvent" -> handleReportEvent(call, result)
      "reportUserProfileCustomString" -> handleReportUserProfileCustomString(call, result)
      "reportUserProfileCustomNumber" -> handleReportUserProfileCustomNumber(call, result)
      "reportUserProfileCustomBoolean" -> handleReportUserProfileCustomBoolean(call, result)
      "reportUserProfileCustomCounter" -> handleReportUserProfileCustomCounter(call, result)
      "reportUserProfileUserName" -> handleReportUserProfileUserName(call, result)
      "reportUserProfileNotificationsEnabled" -> handleReportUserProfileNotificationsEnabled(
              call,
              result
      )
      "setStatisticsSending" -> handleSetStatisticsSending(call, result)
      "getLibraryVersion" -> handleGetLibraryVersion(call, result)
      "setUserProfileID" -> handleSetUserProfileID(call, result)
      "sendEventsBuffer" -> handleSendEventsBuffer(call, result)
      "reportReferralUrl" -> handleReportReferralUrl(call, result)
      "showProductCardEvent" -> handleShowProductCardEvent(call, result)
      "showScreenEvent" -> handleShowScreenEvent(call, result)
      "showProductDetailsEvent" -> handleShowProductDetailsEvent(call, result)
      "addCartItemEvent" -> handleAddCartItemEvent(call, result)
      "removeCartItemEvent" -> handleRemoveCartItemEvent(call, result)
      "beginCheckoutEvent" -> handleBeginCheckoutEvent(call, result)
      "purchaseEvent" -> handlePurchaseEvent(call, result)
      "reportRevenue" -> reportRevenue(call, result)
      else -> result.notImplemented()
    }
  }

  private fun reportRevenue(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val productID = arguments["productID"] as String?
      val productQuantity = arguments["productQuantity"] as Int?
      val productPrice = arguments["productPrice"] as Long

      YandexMetrica.reportRevenue(Revenue.newBuilderWithMicros(productPrice, Currency.getInstance("RUB")).withProductID(productID).withQuantity(productQuantity).build())
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending reportRevenue", e.message, null)
    }
    result.success(null)
  }

  private fun handlePurchaseEvent(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val orderID = arguments["orderID"] as String?
      val products: List<List<Any>> = arguments["products"] as List<List<Any>>
      val cartedItems: ArrayList<ECommerceCartItem> = ArrayList()

      products.forEach{
        val actualPrice =
          ECommercePrice(ECommerceAmount(it[2] as Double, "RUB"))
        val originalPrice =
          ECommercePrice(ECommerceAmount(it[3] as Double, "RUB"))
        val product: ECommerceProduct =
          ECommerceProduct(it[0] as String).setName(it[1] as String)
            .setOriginalPrice(originalPrice).setActualPrice(actualPrice)
        val addedItems = ECommerceCartItem(product, actualPrice, 1.0)
        cartedItems.add(addedItems)

      }
      val order = ECommerceOrder(orderID!!, cartedItems)
      val purchaseEvent = ECommerceEvent.purchaseEvent(order)
      YandexMetrica.reportECommerce(purchaseEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending purchaseEvent", e.message, null)
    }
    result.success(null)
  }

  private fun handleBeginCheckoutEvent(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val orderID = arguments["orderID"] as String?
      val products: List<List<Any>> = arguments["products"] as List<List<Any>>
      val cartedItems: ArrayList<ECommerceCartItem> = ArrayList()

      products.forEach{
        val actualPrice = ECommercePrice(ECommerceAmount(it[2] as Double, "RUB"))
        val originalPrice =
          ECommercePrice(ECommerceAmount(it[3] as Double, "RUB"))
        val product: ECommerceProduct =
          ECommerceProduct(it[0] as String).setName(it[1] as String)
            .setOriginalPrice(originalPrice).setActualPrice(actualPrice)
        val addedItems = ECommerceCartItem(product, actualPrice, 1.0)
        cartedItems.add(addedItems)

      }

      val order = ECommerceOrder(orderID!!, cartedItems)
      val beginCheckoutEvent: ECommerceEvent = ECommerceEvent.beginCheckoutEvent(order)
      YandexMetrica.reportECommerce(beginCheckoutEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending beginCheckoutEvent", e.message, null)
    }
    result.success(null)
  }

  private fun handleAddCartItemEvent(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val productActualPrice = arguments["actualPrice"] as Double
      val productOriginalPrice = arguments["productOriginalPrice"] as Double
      val productName = arguments["productName"] as String?
      val productID = arguments["productID"] as String
      val actualPrice = ECommercePrice(ECommerceAmount(productActualPrice, "RUB"))
      val originalPrice = ECommercePrice(ECommerceAmount(productOriginalPrice, "RUB"))
      val product: ECommerceProduct = ECommerceProduct(productID).setActualPrice(actualPrice)
              .setOriginalPrice(originalPrice).setName(productName)
      val addedItems = ECommerceCartItem(product, actualPrice, 1.0)
      val addCartItemEvent: ECommerceEvent = ECommerceEvent.addCartItemEvent(addedItems)
      YandexMetrica.reportECommerce(addCartItemEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending addCartItemEvent", e.message, null)
    }
    result.success(null)
  }

  private fun handleRemoveCartItemEvent(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val productActualPrice = arguments["actualPrice"] as Double
      val productOriginalPrice = arguments["productOriginalPrice"] as Double
      val productName = arguments["productName"] as String?
      val productID = arguments["productID"] as String
      val actualPrice = ECommercePrice(ECommerceAmount(productActualPrice, "RUB"))
      val originalPrice = ECommercePrice(ECommerceAmount(productOriginalPrice, "RUB"))
      val product: ECommerceProduct = ECommerceProduct(productID).setActualPrice(actualPrice)
              .setOriginalPrice(originalPrice).setName(productName)
      val removedItems = ECommerceCartItem(product, actualPrice, 1.0)
      val removeCartItemEvent: ECommerceEvent =
              ECommerceEvent.removeCartItemEvent(removedItems)
      YandexMetrica.reportECommerce(removeCartItemEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending removeCartItemEvent", e.message, null)
    }
    result.success(null)
  }

  private fun handleShowProductCardEvent(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val productActualPrice = arguments["actualPrice"] as Double
      val productOriginalPrice = arguments["productOriginalPrice"] as Double
      val screenWhereFromOpen = arguments["screenWhereFromOpen"] as String?
      val productName = arguments["productName"] as String?
      val productID = arguments["productID"] as String
      val screen: ECommerceScreen = ECommerceScreen().setName(screenWhereFromOpen)
      val actualPrice = ECommercePrice(ECommerceAmount(productActualPrice, "RUB"))
      val originalPrice = ECommercePrice(ECommerceAmount(productOriginalPrice, "RUB"))
      val product: ECommerceProduct = ECommerceProduct(productID).setActualPrice(actualPrice)
              .setOriginalPrice(originalPrice).setName(productName)
      val showProductCardEvent: ECommerceEvent =
              ECommerceEvent.showProductCardEvent(product, screen)
      YandexMetrica.reportECommerce(showProductCardEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending showProductCardEvent", e.message, null)
    }
    result.success(null)
  }

  private fun handleShowScreenEvent(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val screenWhereFromOpen = arguments["screenWhereFromOpen"] as String
      val screen: ECommerceScreen = ECommerceScreen().setName(screenWhereFromOpen)
      val showScreenEvent: ECommerceEvent = ECommerceEvent.showScreenEvent(screen)
      YandexMetrica.reportECommerce(showScreenEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending showScreenEvent", e.message, null)
    }
    result.success(null)
  }

  private fun handleShowProductDetailsEvent(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val productActualPrice= arguments["actualPrice"] as Double
      val productOriginalPrice = arguments["productOriginalPrice"] as Double
      val productName = arguments["productName"] as String?
      val productID = arguments["productID"] as String
      val actualPrice = ECommercePrice(ECommerceAmount(productActualPrice, "RUB"))
      val originalPrice = ECommercePrice(ECommerceAmount(productOriginalPrice, "RUB"))
      val referrer = ECommerceReferrer()
      val product: ECommerceProduct = ECommerceProduct(productID).setActualPrice(actualPrice)
              .setOriginalPrice(originalPrice).setName(productName)
      val showProductDetailsEvent: ECommerceEvent =
              ECommerceEvent.showProductDetailsEvent(product, referrer)
      YandexMetrica.reportECommerce(showProductDetailsEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending showProductDetailsEvent", e.message, null)
    }
  }

  private fun handleActivate(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      // Get activation parameters.
      val apiKey = arguments["apiKey"] as String
      val sessionTimeout = arguments["sessionTimeout"] as Int
      val locationTracking = arguments["locationTracking"] as Boolean
      val statisticsSending = arguments["statisticsSending"] as Boolean
      val crashReporting = arguments["crashReporting"] as Boolean
      val revenueAutoTrackingEnabled = arguments["revenueAutoTrackingEnabled"] as Boolean
      val maxReportsInDatabaseCount = arguments["maxReportsInDatabaseCount"] as Int
      // Creating an extended library configuration.
      val config: YandexMetricaConfig = YandexMetricaConfig.newConfigBuilder(apiKey)
              .withLogs()
              .withSessionTimeout(sessionTimeout)
              .withLocationTracking(locationTracking)
              .withStatisticsSending(statisticsSending)
              .withCrashReporting(crashReporting)
              .withMaxReportsInDatabaseCount(maxReportsInDatabaseCount)
              .withRevenueAutoTrackingEnabled(revenueAutoTrackingEnabled)
              .build()
      // Initializing the AppMetrica SDK.
      YandexMetrica.activate(context!!, config)
      // Automatic tracking of user activity.
      YandexMetrica.enableActivityAutoTracking(application!!)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error performing activation", e.message, null)
    }
    result.success(null)
  }

  private fun handleReportEvent(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val eventName = arguments["name"] as String?
      val attributes = arguments["attributes"] as MutableMap<String, Any>?
      if (attributes == null) {
        YandexMetrica.reportEvent(eventName!!)
      } else {
        YandexMetrica.reportEvent(eventName!!, attributes)
      }

    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error reporing event", e.message, null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileCustomString(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val key = arguments["key"] as String?
      val value = arguments["value"] as String?
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (value != null) {
        profileBuilder.apply(Attribute.customString(key!!).withValue(value))
      } else {
        profileBuilder.apply(Attribute.customString(key!!).withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error reporing user profile custom string", e.message, null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileCustomNumber(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val key = arguments["key"] as String?
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (arguments["value"] != null) {
        val value = arguments["value"] as Double
        profileBuilder.apply(Attribute.customNumber(key!!).withValue(value))
      } else {
        profileBuilder.apply(Attribute.customNumber(key!!).withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error reporing user profile custom number", e.message, null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileCustomBoolean(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val key = arguments["key"] as String?
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (arguments["value"] != null) {
        val value = arguments["value"] as Boolean
        profileBuilder.apply(Attribute.customBoolean(key!!).withValue(value))
      } else {
        profileBuilder.apply(Attribute.customBoolean(key!!).withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error reporing user profile custom boolean", e.message, null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileCustomCounter(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val key = arguments["key"] as String?
      val delta = arguments["delta"] as Double
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      profileBuilder.apply(Attribute.customCounter(key!!).withDelta(delta))
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error reporing user profile custom counter", e.message, null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileUserName(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (arguments["userName"] != null) {
        val userName = arguments["userName"] as String?
        profileBuilder.apply(Attribute.name().withValue(userName!!))
      } else {
        profileBuilder.apply(Attribute.name().withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error reporing user profile user name", e.message, null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileNotificationsEnabled(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (arguments["notificationsEnabled"] != null) {
        val notificationsEnabled = arguments["notificationsEnabled"] as Boolean
        profileBuilder.apply(
                Attribute.notificationsEnabled().withValue(notificationsEnabled)
        )
      } else {
        profileBuilder.apply(Attribute.notificationsEnabled().withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error reporing user profile user name", e.message, null)
    }
    result.success(null)
  }

  private fun handleSetStatisticsSending(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val statisticsSending = arguments["statisticsSending"] as Boolean
      YandexMetrica.setStatisticsSending(context!!, statisticsSending)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error enable sending statistics", e.message, null)
    }
    result.success(null)
  }

  private fun handleGetLibraryVersion(call: MethodCall, result: Result) {
    try {
      result.success(YandexMetrica.getLibraryVersion())
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error enable sending statistics", e.message, null)
    }
  }

  private fun handleSetUserProfileID(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val userProfileID = arguments["userProfileID"] as String?
      YandexMetrica.setUserProfileID(userProfileID)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sets the ID of the user profile", e.message, null)
    }
    result.success(null)
  }

  private fun handleSendEventsBuffer(call: MethodCall, result: Result) {
    try {
      YandexMetrica.sendEventsBuffer()
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sending stored events from the buffer", e.message, null)
    }
    result.success(null)
  }

  private fun handleReportReferralUrl(call: MethodCall, result: Result) {
    try {
      val arguments = call.arguments as Map<String, Any>
      val referral = arguments["referral"] as String?
      YandexMetrica.reportReferralUrl(referral!!)
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      result.error("Error sets the ID of the user profile", e.message, null)
    }
    result.success(null)
  }

  companion object {
    private const val TAG = "AppMetricaPlugin"

    /**
     * Plugin registration for v1 embedder.
     */
    fun registerWith(registrar: Registrar) {
      val instance = AppMetricaPlugin()
      instance.onAttachedToEngine(registrar.context(), registrar.messenger())
    }
  }
}
