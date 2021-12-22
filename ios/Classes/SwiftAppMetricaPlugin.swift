import Flutter
import UIKit
import YandexMobileMetrica

public class SwiftAppMetricaPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "app_metrica_plugin", binaryMessenger: registrar.messenger())
    let instance = SwiftAppMetricaPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let method = call.method
    switch method {
    case "activate":
        let args = call.arguments as! [String: Any]
        let apiKey = args["apiKey"] as! String
        let revenueAutoTrackingEnabled = args["revenueAutoTrackingEnabled"] as! Bool
        let sessionTimeout = args["sessionTimeout"] as! UInt
        let locationTracking = args["locationTracking"] as! Bool
        let statisticsSending = args["statisticsSending"] as! Bool
        let crashReporting = args["crashReporting"] as! Bool
        let maxReportsInDatabaseCount = args["maxReportsInDatabaseCount"] as! UInt
        
        let configuration = YMMYandexMetricaConfiguration.init(apiKey: apiKey)
        configuration?.revenueAutoTrackingEnabled = revenueAutoTrackingEnabled
        configuration?.sessionTimeout = sessionTimeout
        configuration?.locationTracking = locationTracking
        configuration?.statisticsSending = statisticsSending
        configuration?.crashReporting = crashReporting
        configuration?.maxReportsInDatabaseCount = maxReportsInDatabaseCount
        
        YMMYandexMetrica.activate(with: configuration!)

        result(nil)
    
    case "reportRevenue":
        let args = call.arguments as! [String: Any]
        let price = NSDecimalNumber(string: args["productPrice"] as? String)
        let priceQuantity = args["productQuantity"] as! UInt
        
        let revenueInfo = YMMMutableRevenueInfo.init(priceDecimal: price, currency: "RUB")
        revenueInfo.quantity = priceQuantity
        
        YMMYandexMetrica.reportRevenue(revenueInfo, onFailure: nil)
        result(nil)
    case "reportEvent":
        let args = call.arguments as! [String: Any]
        let attributes = args["attributes"] as? [String : Any]
        let name = args["name"] as! String

        if attributes != nil {
            YMMYandexMetrica.reportEvent(name, parameters: attributes, onFailure: { err in
                print("Failed to send event: \(err)")
            })
        } else {
            YMMYandexMetrica.reportEvent(name, onFailure: { err in
                print("Failed to send event: \(err)")
            })
        }

        result(nil)
    case "showProductCardEvent":
        let args = call.arguments as! [String: Any]
        let productActualPrice = args["actualPrice"] as! Double
        let productOriginalPrice = args["productOriginalPrice"] as! Double
        let screenWhereFromOpen = args["screenWhereFromOpen"] as! String
        let productName = args["productName"] as! String
        let productID = args["productID"] as! String
        
        
        let screen = YMMECommerceScreen(name: screenWhereFromOpen)
        
        let actualPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productActualPrice)")))
        
        let originalPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productOriginalPrice)")))
        
        let product = YMMECommerceProduct(
            sku: productID,
            name: productName,
            categoryComponents: nil,
            payload: nil,
            actualPrice: actualPrice,
            originalPrice: originalPrice,
            promoCodes: nil)
        
        YMMYandexMetrica.report(eCommerce: .showProductCardEvent(product: product, screen: screen), onFailure: nil)
        
        result(nil)
    case "showScreenEvent":
        let args = call.arguments as! [String: Any]
        let screenWhereFromOpen = args["screenWhereFromOpen"] as! String
        let screen = YMMECommerceScreen(name: screenWhereFromOpen)
        
        YMMYandexMetrica.report(eCommerce: .showScreenEvent(screen: screen), onFailure: nil)
        result(nil)
    case "showProductDetailsEvent":
        let args = call.arguments as! [String: Any]
        let productActualPrice = args["actualPrice"] as! Double
        let productOriginalPrice = args["productOriginalPrice"] as! Double
        let productName = args["productName"] as! String
        let productID = args["productID"] as! String
        let referrer = YMMECommerceReferrer(type: nil, identifier: nil, screen: nil)
        
        let actualPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productActualPrice)")))
        
        let originalPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productOriginalPrice)")))
        
        let product = YMMECommerceProduct(
            sku: productID,
            name: productName,
            categoryComponents: nil,
            payload: nil,
            actualPrice: actualPrice,
            originalPrice: originalPrice,
            promoCodes: nil)
        
        YMMYandexMetrica.report(eCommerce: .showProductDetailsEvent(product: product, referrer: referrer), onFailure: nil)
        result(nil)
    case "addCartItemEvent":
        let args = call.arguments as! [String: Any]
        let productActualPrice = args["actualPrice"] as! Double
        let productOriginalPrice = args["productOriginalPrice"] as! Double
        let productName = args["productName"] as! String
        let productID = args["productID"] as! String
        let referrer = YMMECommerceReferrer(type: nil, identifier: nil, screen: nil)

        let actualPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productActualPrice)")))
        
        let originalPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productOriginalPrice)")))
        
        let product = YMMECommerceProduct(
            sku: productID,
            name: productName,
            categoryComponents: nil,
            payload: nil,
            actualPrice: actualPrice,
            originalPrice: originalPrice,
            promoCodes: nil)
        
        let cartedItems = YMMECommerceCartItem(product: product, quantity: .init(string: "1"), revenue: actualPrice, referrer: referrer)
        
        YMMYandexMetrica.report(eCommerce: .addCartItemEvent(cartItem: cartedItems), onFailure: nil)
        result(nil)
    case "removeCartItemEvent":
        let args = call.arguments as! [String: Any]
        let productActualPrice = args["actualPrice"] as! Double
        let productOriginalPrice = args["productOriginalPrice"] as! Double
        let productName = args["productName"] as! String
        let productID = args["productID"] as! String
        let referrer = YMMECommerceReferrer(type: nil, identifier: nil, screen: nil)

        let actualPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productActualPrice)")))
        
        let originalPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productOriginalPrice)")))
        
        let product = YMMECommerceProduct(
            sku: productID,
            name: productName,
            categoryComponents: nil,
            payload: nil,
            actualPrice: actualPrice,
            originalPrice: originalPrice,
            promoCodes: nil)
        
        let removedCartedItems = YMMECommerceCartItem(product: product, quantity: .init(string: "1"), revenue: actualPrice, referrer: referrer)
        
        YMMYandexMetrica.report(eCommerce: .removeCartItemEvent(cartItem: removedCartedItems), onFailure: nil)
        result(nil)
    case "beginCheckoutEvent":
        let args = call.arguments as! [String: Any]
        let products = args["products"] as! [[Any]]
        var cartedItems = [YMMECommerceCartItem]()
        
        products.forEach { item in
            let actualPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(item[2])")))
            let originalPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(item[3])")))
            
            let product = YMMECommerceProduct(
                sku: item[0] as! String,
                name: item[1] as? String,
                categoryComponents: nil,
                payload: nil,
                actualPrice: actualPrice,
                originalPrice: originalPrice,
                promoCodes: nil)
            
            let addedItems = YMMECommerceCartItem(product: product, quantity: .init(string: (item[4] as! String)), revenue: actualPrice, referrer: nil)
            cartedItems.append(addedItems)
        }
        
        let order = YMMECommerceOrder(identifier: "", cartItems: cartedItems, payload: nil)
        
        YMMYandexMetrica.report(eCommerce: .beginCheckoutEvent(order: order), onFailure: nil)
        
        result(nil)
    case "purchaseEvent":
        let args = call.arguments as! [String: Any]
        let products = args["products"] as! [[Any]]
        var cartedItems = [YMMECommerceCartItem]()
        
        products.forEach{ item in
            let actualPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(item[2])")))
            let originalPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(item[3])")))
            
            let product = YMMECommerceProduct(
                sku: item[0] as! String,
                name: item[1] as? String,
                categoryComponents: nil,
                payload: nil,
                actualPrice: actualPrice,
                originalPrice: originalPrice,
                promoCodes: nil)
            
            let addedItems = YMMECommerceCartItem(product: product, quantity: .init(string: (item[4] as! String)), revenue: actualPrice, referrer: nil)
            cartedItems.append(addedItems)
        }
        let order = YMMECommerceOrder(identifier: "", cartItems: cartedItems, payload: nil)
        YMMYandexMetrica.report(eCommerce: .purchaseEvent(order: order), onFailure: nil)

        result(nil)
    case "sendEventsBuffer":
        YMMYandexMetrica.sendEventsBuffer()
        result(nil)
    default:
        result(nil)
    }
  }
}
