package app_metrica_plugin

import androidx.annotation.RequiresApi
import android.os.Build
import android.util.Log
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.SparseArray
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import java.util.Map
import java.util.function.Consumer
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.FlutterView
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.yandex.metrica.YandexMetricaDefaultValues
import com.yandex.metrica.ecommerce.ECommerceAmount
import com.yandex.metrica.ecommerce.ECommerceCartItem
import com.yandex.metrica.ecommerce.ECommerceEvent
import com.yandex.metrica.ecommerce.ECommerceOrder
import com.yandex.metrica.ecommerce.ECommercePrice
import com.yandex.metrica.ecommerce.ECommerceProduct
import com.yandex.metrica.ecommerce.ECommerceReferrer
import com.yandex.metrica.ecommerce.ECommerceScreen
import com.yandex.metrica.profile.Attribute
import com.yandex.metrica.profile.StringAttribute
import com.yandex.metrica.profile.UserProfile
import com.yandex.metrica.profile.UserProfileUpdate

/**
 * AppmetricaSdkPlugin
 */
class AppmetricaSdkPlugin : MethodCallHandler, FlutterPlugin {
  private var methodChannel: MethodChannel? = null
  private var context: Context? = null
  private var application: Application? = null
  @Override
  fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPluginBinding) {
    onAttachedToEngine(
            flutterPluginBinding.getApplicationContext(),
            flutterPluginBinding.getBinaryMessenger()
    )
  }

  private fun onAttachedToEngine(applicationContext: Context, binaryMessenger: BinaryMessenger) {
    application = applicationContext as Application
    context = applicationContext
    methodChannel = MethodChannel(binaryMessenger, "app_metrica_plugin")
    methodChannel.setMethodCallHandler(this)
  }

  @Override
  fun onDetachedFromEngine(@NonNull binding: FlutterPluginBinding?) {
    methodChannel.setMethodCallHandler(null)
    methodChannel = null
    context = null
    application = null
  }

  @Override
  fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
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
      else -> result.notImplemented()
    }
  }

  private fun handlePurchaseEvent(call: MethodCall, result: Result) {
    try {
      val arguments: Map<String, Object> = call.arguments
      val orderID = arguments["orderID"] as String?
      val cartedItems: List<ECommerceCartItem> = ArrayList()
      arguments["products"].forEach(object : Consumer<List?>() {
        @Override
        fun accept(singleProduct: List) {
          val actualPrice =
                  ECommercePrice(ECommerceAmount(singleProduct.get(2) as Integer, "RUB"))
          val originalPrice =
                  ECommercePrice(ECommerceAmount(singleProduct.get(3) as Integer, "RUB"))
          val product: ECommerceProduct =
                  ECommerceProduct(singleProduct.get(0) as String).setName(singleProduct.get(1) as String)
                          .setOriginalPrice(originalPrice).setActualPrice(actualPrice)
          val addedItems = ECommerceCartItem(product, actualPrice, 1.0)
          cartedItems.add(addedItems)
        }
      })
      val order = ECommerceOrder(orderID, cartedItems)
      val purchaseEvent: ECommerceEvent = ECommerceEvent.purchaseEvent(order)
      YandexMetrica.reportECommerce(purchaseEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sending purchaseEvent", e.getMessage(), null)
    }
    result.success(null)
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  private fun handleBeginCheckoutEvent(call: MethodCall, result: Result) {
    try {
      val arguments: Map<String, Object> = call.arguments
      val orderID = arguments["orderID"] as String?
      val cartedItems: List<ECommerceCartItem> = ArrayList()
      arguments["products"].forEach(object : Consumer<List?>() {
        @Override
        fun accept(singleProduct: List) {
          val actualPrice =
                  ECommercePrice(ECommerceAmount(singleProduct.get(2) as Integer, "RUB"))
          val originalPrice =
                  ECommercePrice(ECommerceAmount(singleProduct.get(3) as Integer, "RUB"))
          val product: ECommerceProduct =
                  ECommerceProduct(singleProduct.get(0) as String).setName(singleProduct.get(1) as String)
                          .setOriginalPrice(originalPrice).setActualPrice(actualPrice)
          val addedItems = ECommerceCartItem(product, actualPrice, 1.0)
          cartedItems.add(addedItems)
        }
      })
      val order = ECommerceOrder(orderID, cartedItems)
      val beginCheckoutEvent: ECommerceEvent = ECommerceEvent.beginCheckoutEvent(order)
      YandexMetrica.reportECommerce(beginCheckoutEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sending beginCheckoutEvent", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleAddCartItemEvent(call: MethodCall, result: Result) {
    try {
      val arguments: Map<String, Object> = call.arguments
      val productActualPrice: Integer? = arguments["actualPrice"] as Integer?
      val productOriginalPrice: Integer? = arguments["productOriginalPrice"] as Integer?
      val productName = arguments["productName"] as String?
      val productID = arguments["productID"] as String?
      val actualPrice = ECommercePrice(ECommerceAmount(productActualPrice, "RUB"))
      val originalPrice = ECommercePrice(ECommerceAmount(productOriginalPrice, "RUB"))
      val product: ECommerceProduct = ECommerceProduct(productID).setActualPrice(actualPrice)
              .setOriginalPrice(originalPrice).setName(productName)
      val addedItems = ECommerceCartItem(product, actualPrice, 1.0)
      val addCartItemEvent: ECommerceEvent = ECommerceEvent.addCartItemEvent(addedItems)
      YandexMetrica.reportECommerce(addCartItemEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sending addCartItemEvent", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleRemoveCartItemEvent(call: MethodCall, result: Result) {
    try {
      val arguments: Map<String, Object> = call.arguments
      val productActualPrice: Integer? = arguments["actualPrice"] as Integer?
      val productOriginalPrice: Integer? = arguments["productOriginalPrice"] as Integer?
      val productName = arguments["productName"] as String?
      val productID = arguments["productID"] as String?
      val actualPrice = ECommercePrice(ECommerceAmount(productActualPrice, "RUB"))
      val originalPrice = ECommercePrice(ECommerceAmount(productOriginalPrice, "RUB"))
      val product: ECommerceProduct = ECommerceProduct(productID).setActualPrice(actualPrice)
              .setOriginalPrice(originalPrice).setName(productName)
      val removedItems = ECommerceCartItem(product, actualPrice, 1.0)
      val removeCartItemEvent: ECommerceEvent =
              ECommerceEvent.removeCartItemEvent(removedItems)
      YandexMetrica.reportECommerce(removeCartItemEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sending removeCartItemEvent", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleShowProductCardEvent(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object> = call.arguments
      val productActualPrice: Integer? = arguments["actualPrice"] as Integer?
      val productOriginalPrice: Integer? = arguments["productOriginalPrice"] as Integer?
      val screenWhereFromOpen = arguments["screenWhereFromOpen"] as String?
      val productName = arguments["productName"] as String?
      val productID = arguments["productID"] as String?
      val screen: ECommerceScreen = ECommerceScreen().setName(screenWhereFromOpen)
      val actualPrice = ECommercePrice(ECommerceAmount(productActualPrice, "RUB"))
      val originalPrice = ECommercePrice(ECommerceAmount(productOriginalPrice, "RUB"))
      val product: ECommerceProduct = ECommerceProduct(productID).setActualPrice(actualPrice)
              .setOriginalPrice(originalPrice).setName(productName)
      val showProductCardEvent: ECommerceEvent =
              ECommerceEvent.showProductCardEvent(product, screen)
      YandexMetrica.reportECommerce(showProductCardEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sending showProductCardEvent", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleShowScreenEvent(call: MethodCall, result: Result) {
    try {
      val screenWhereFromOpen = call.arguments["screenWhereFromOpen"] as String?
      val screen: ECommerceScreen = ECommerceScreen().setName(screenWhereFromOpen)
      val showScreenEvent: ECommerceEvent = ECommerceEvent.showScreenEvent(screen)
      YandexMetrica.reportECommerce(showScreenEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sending showScreenEvent", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleShowProductDetailsEvent(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object> = call.arguments
      val productActualPrice: Integer? = arguments["actualPrice"] as Integer?
      val productOriginalPrice: Integer? = arguments["productOriginalPrice"] as Integer?
      val productName = arguments["productName"] as String?
      val productID = arguments["productID"] as String?
      val actualPrice = ECommercePrice(ECommerceAmount(productActualPrice, "RUB"))
      val originalPrice = ECommercePrice(ECommerceAmount(productOriginalPrice, "RUB"))
      val referrer = ECommerceReferrer()
      val product: ECommerceProduct = ECommerceProduct(productID).setActualPrice(actualPrice)
              .setOriginalPrice(originalPrice).setName(productName)
      val showProductDetailsEvent: ECommerceEvent =
              ECommerceEvent.showProductDetailsEvent(product, referrer)
      YandexMetrica.reportECommerce(showProductDetailsEvent)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sending showProductDetailsEvent", e.getMessage(), null)
    }
  }

  private fun handleActivate(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object> = call.arguments
      // Get activation parameters.
      val apiKey = arguments["apiKey"] as String?
      val sessionTimeout = arguments["sessionTimeout"] as Int
      val locationTracking = arguments["locationTracking"] as Boolean
      val statisticsSending = arguments["statisticsSending"] as Boolean
      val crashReporting = arguments["crashReporting"] as Boolean
      val maxReportsInDatabaseCount = arguments["maxReportsInDatabaseCount"] as Int
      // Creating an extended library configuration.
      val config: YandexMetricaConfig = YandexMetricaConfig.newConfigBuilder(apiKey)
              .withLogs()
              .withSessionTimeout(sessionTimeout)
              .withLocationTracking(locationTracking)
              .withStatisticsSending(statisticsSending)
              .withCrashReporting(crashReporting)
              .withMaxReportsInDatabaseCount(maxReportsInDatabaseCount)
              .build()
      // Initializing the AppMetrica SDK.
      YandexMetrica.activate(context, config)
      // Automatic tracking of user activity.
      YandexMetrica.enableActivityAutoTracking(application)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error performing activation", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleReportEvent(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object> = call.arguments
      val eventName = arguments["name"] as String?
      @SuppressWarnings("unchecked") val attributes: Map<String, Object>? =
              arguments["attributes"]
      if (attributes == null) {
        YandexMetrica.reportEvent(eventName)
      } else {
        YandexMetrica.reportEvent(eventName, attributes)
      }
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error reporing event", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileCustomString(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object> = call.arguments
      val key = arguments["key"] as String?
      val value = arguments["value"] as String?
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (value != null) {
        profileBuilder.apply(Attribute.customString(key).withValue(value))
      } else {
        profileBuilder.apply(Attribute.customString(key).withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error reporing user profile custom string", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileCustomNumber(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object?> = call.arguments
      val key = arguments["key"] as String?
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (arguments["value"] != null) {
        val value = arguments["value"] as Double
        profileBuilder.apply(Attribute.customNumber(key).withValue(value))
      } else {
        profileBuilder.apply(Attribute.customNumber(key).withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error reporing user profile custom number", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileCustomBoolean(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object?> = call.arguments
      val key = arguments["key"] as String?
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (arguments["value"] != null) {
        val value = arguments["value"] as Boolean
        profileBuilder.apply(Attribute.customBoolean(key).withValue(value))
      } else {
        profileBuilder.apply(Attribute.customBoolean(key).withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error reporing user profile custom boolean", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileCustomCounter(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object> = call.arguments
      val key = arguments["key"] as String?
      val delta = arguments["delta"] as Double
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      profileBuilder.apply(Attribute.customCounter(key).withDelta(delta))
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error reporing user profile custom counter", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileUserName(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object?> = call.arguments
      val profileBuilder: UserProfile.Builder = UserProfile.newBuilder()
      if (arguments["userName"] != null) {
        val userName = arguments["userName"] as String?
        profileBuilder.apply(Attribute.name().withValue(userName))
      } else {
        profileBuilder.apply(Attribute.name().withValueReset())
      }
      YandexMetrica.reportUserProfile(profileBuilder.build())
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error reporing user profile user name", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleReportUserProfileNotificationsEnabled(call: MethodCall, result: Result) {
    try {
      @SuppressWarnings("unchecked") val arguments: Map<String, Object?> = call.arguments
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
      Log.e(TAG, e.getMessage(), e)
      result.error("Error reporing user profile user name", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleSetStatisticsSending(call: MethodCall, result: Result) {
    try {
      val statisticsSending = call.arguments["statisticsSending"] as Boolean
      YandexMetrica.setStatisticsSending(context, statisticsSending)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error enable sending statistics", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleGetLibraryVersion(call: MethodCall, result: Result) {
    try {
      result.success(YandexMetrica.getLibraryVersion())
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error enable sending statistics", e.getMessage(), null)
    }
  }

  private fun handleSetUserProfileID(call: MethodCall, result: Result) {
    try {
      val userProfileID = call.arguments["userProfileID"] as String?
      YandexMetrica.setUserProfileID(userProfileID)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sets the ID of the user profile", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleSendEventsBuffer(call: MethodCall, result: Result) {
    try {
      YandexMetrica.sendEventsBuffer()
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sending stored events from the buffer", e.getMessage(), null)
    }
    result.success(null)
  }

  private fun handleReportReferralUrl(call: MethodCall, result: Result) {
    try {
      val referral = call.arguments["referral"] as String?
      YandexMetrica.reportReferralUrl(referral)
    } catch (e: Exception) {
      Log.e(TAG, e.getMessage(), e)
      result.error("Error sets the ID of the user profile", e.getMessage(), null)
    }
    result.success(null)
  }

  companion object {
    private const val TAG = "AppmetricaSdkPlugin"

    /**
     * Plugin registration for v1 embedder.
     */
    fun registerWith(registrar: Registrar) {
      val instance = AppmetricaSdkPlugin()
      instance.onAttachedToEngine(registrar.context(), registrar.messenger())
    }
  }
}
